package dev.ua.leavepulse.pingfix;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerPing;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Intercepts ProxyPingEvent for force-host connections.
 * <p>
 * When {@code ping-passthrough = "all"} is set in velocity.toml, Velocity tries to
 * forward the ping to the backend server. If the backend is offline, Velocity falls
 * back to its own global response — which includes the total online player count
 * across all backends, not zero.
 * <p>
 * This listener detects that scenario: it resolves which backend the force-host
 * maps to, pings it directly, and if unreachable, rewrites the response to show
 * {@code online = 0} with an empty player sample.
 */
public class ForceHostPingListener {

    private final ProxyServer proxy;
    private final Logger logger;

    public ForceHostPingListener(ProxyServer proxy, Logger logger) {
        this.proxy = proxy;
        this.logger = logger;
    }

    @Subscribe
    public EventTask onProxyPing(ProxyPingEvent event) {
        InetSocketAddress virtualHost = event.getConnection().getVirtualHost().orElse(null);
        if (virtualHost == null) {
            return null;
        }

        String hostname = virtualHost.getHostString().toLowerCase(Locale.ROOT);

        Map<String, List<String>> forcedHosts = proxy.getConfiguration().getForcedHosts();
        List<String> serverNames = forcedHosts.get(hostname);
        if (serverNames == null || serverNames.isEmpty()) {
            return null;
        }

        // Find the first registered backend for this force-host.
        Optional<RegisteredServer> target = Optional.empty();
        for (String name : serverNames) {
            target = proxy.getServer(name);
            if (target.isPresent()) {
                break;
            }
        }

        if (target.isEmpty()) {
            return null;
        }

        RegisteredServer backend = target.get();

        // Ping the backend directly. If it fails, the backend is offline —
        // rewrite the response to show 0 players instead of the proxy's global count.
        return EventTask.resumeWhenComplete(
            backend.ping().thenAccept(backendPing -> {
                // Backend responded — it's online.
                // Velocity's ping-passthrough should have handled this correctly,
                // but just in case, we leave the event unchanged.
            }).exceptionally(throwable -> {
                // Backend is unreachable — fix the player count.
                ServerPing original = event.getPing();
                ServerPing fixed = original.asBuilder()
                    .onlinePlayers(0)
                    .clearSamplePlayers()
                    .build();
                event.setPing(fixed);

                logger.debug(
                    "Force-host {} -> backend {} is offline, set online=0",
                    hostname, backend.getServerInfo().getName()
                );
                return null;
            })
        );
    }
}
