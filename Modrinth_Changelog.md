# ClientWatch 0.1.2

Reliability and detection fixes: prefer Paper brand API, avoid async player access, and robustly decode plugin-brand messages.

## Fixed

- Prefer `Player#getClientBrandName()` on Paper as the primary brand source.
- Ensure detection snapshots and saves run on the main server thread to avoid async Bukkit API access.
- Improve VarInt/string decoding for `minecraft:brand` plugin messages and ignore invalid payloads.
- Reduce chances of storing premature `Unknown` brand values by preferring Paper API when available.

# ClientWatch 0.1.1

Maintenance and feature update focused on command usability, blacklist coverage, exports, and more transparent detection diagnostics.

## Added

- Added `/clientwatch online` to list currently connected players with client, loader, and mod count.
- Added `/clientwatch search <mod>` to search currently cached online player detections for a specific mod.
- Added `/clientwatch export <player>` to export a player's latest ClientWatch data as JSON.
- Added daily detection log entries with plugin version, mod count, blacklist match count, and detection duration.
- Added conservative detection warnings for unavailable brands, unusual brands, duplicate mod IDs, suspicious mod names, and unusual version values.
- Added GUI configuration placeholders for search, loader filters, blacklist filters, sorting, and quick refresh.
- Added Discord embed configuration placeholders for server name, plugin version, player avatar, timestamp, and severity colors.

## Changed

- Expanded the default blacklist with many known hacked clients, utility mods, exploit modules, and suspicious mod identifiers.
- Improved in-memory detection storage with thread-safe history lists.
- Improved command tab completion for the new commands.
- Updated the project version to `0.1.1`.

## Notes

- ClientWatch still only acts on information visible to the server.
- GUI and Discord configuration options are prepared for the next implementation stage; this release does not claim complete GUI or webhook rendering yet.

# ClientWatch 0.1.0

Initial development release of ClientWatch.

## Added

- Paper 1.21+ plugin foundation.
- Java 21 Maven build setup.
- Join-time client inspection pipeline.
- Client brand detection through the `minecraft:brand` plugin message channel.
- Server-visible protocol and ping capture.
- Loader inference from reliable observable data.
- Blacklist checks for client brands, client names, loaders, and mods.
- Configurable actions for blacklist matches:
  - Kick
  - Console message
  - Staff message
  - Ban command
- Main `/clientwatch` command with reload, version, update, inspect, info, mods, history, alerts, blacklist, and whitelist subcommands.
- Public Bukkit events for integrations:
  - `PlayerClientDetectEvent`
  - `PlayerModsLoadedEvent`
  - `PlayerBlacklistedModEvent`
  - `PlayerInspectionEvent`
- Modrinth update-checking scaffold without automatic downloads.
- English-only default configuration and messages.

## Notes

- ClientWatch only reports information that can be observed reliably from the server.
- If a client does not provide a trustworthy mod list, the plugin marks the mod list as unavailable instead of simulating detections.
- SQLite, MySQL, MariaDB, Discord embeds, inventory GUI, and advanced loader-specific mod-list handling are planned for later development.
