package dev.ua.leavepulse.pingfix;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

@Plugin(
    id = "velocity-ping-fix",
    name = "Velocity Ping Fix",
    version = "1.0.0",
    description = "Fixes force-host ping passthrough showing wrong player count when backend is offline.",
    authors = {"LeavePulse"}
)
public class PingFixPlugin {

    private final ProxyServer proxy;
    private final Logger logger;

    @Inject
    public PingFixPlugin(ProxyServer proxy, Logger logger) {
        this.proxy = proxy;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        proxy.getEventManager().register(this, new ForceHostPingListener(proxy, logger));
        logger.info("Velocity Ping Fix loaded — force-host ping passthrough fix active");
    }
}
