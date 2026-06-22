# ClientWatch

ClientWatch is a Paper 1.21+ plugin for reliable server-side Minecraft client inspection.

The plugin intentionally reports only information that a server can actually observe. If a client does not provide a reliable mod list, ClientWatch marks the mod list as unavailable instead of inventing detections.

## Features

- Join-time client inspection
- Client brand and protocol capture where available through Paper
- Loader inference from trustworthy server-visible data
- Configurable mod, client, and loader blacklists
- Configurable kick, ban command, console, and staff alert actions
- Online overview, mod search, and JSON export commands
- Daily detection logs with plugin version, mod count, match count, and detection duration
- Discord webhook alerts for blacklist matches
- Persistent service interfaces ready for SQLite, MySQL, and MariaDB implementations
- Public Bukkit events for other plugins
- Modrinth update checks without automatic downloads

## Commands

- `/clientwatch`
- `/clientwatch reload`
- `/clientwatch version`
- `/clientwatch update`
- `/clientwatch inspect <player>`
- `/clientwatch mods <player>`
- `/clientwatch info <player>`
- `/clientwatch history <player>`
- `/clientwatch online`
- `/clientwatch search <mod>`
- `/clientwatch export <player>`
- `/clientwatch alerts`
- `/clientwatch blacklist`
- `/clientwatch whitelist`

## Build

```bash
mvn clean verify
```
