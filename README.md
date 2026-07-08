# Velocity Ping Fix

A tiny [Velocity](https://papermc.io/software/velocity) proxy plugin that fixes
the **player count shown for force-host (forced-host) entries** when the backend
they point to is offline.

## The problem

With Velocity's ping passthrough, a forced host (e.g. `smp.example.com` →
`survival` backend) can end up showing the proxy's *global* player count even
when its backend server is actually down, making an offline server look online
and populated.

## What it does

On every `ProxyPingEvent`, the plugin:

1. Reads the connection's virtual host and looks up the forced host in the
   proxy configuration.
2. Pings the mapped backend directly.
3. If the backend responds, the ping is left unchanged (passthrough was
   correct).
4. If the backend is **unreachable**, it rewrites the response to show
   **0 online players** instead of the proxy's global count.

No configuration required; it reads Velocity's existing forced-host mapping.

## Requirements

- Velocity 3.3.0+.

## Building

```bash
./gradlew build
```

The plugin jar is produced under `build/libs/`.

## License

Licensed under the [Mozilla Public License 2.0](LICENSE).
