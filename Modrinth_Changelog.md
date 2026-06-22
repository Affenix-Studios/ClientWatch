# ClientWatch 0.1.7

## Added

- Channel-log (startup): log the registered plugin-message channels.
- UNKNOWN/missing-loader login protection (unchanged): deny join when the loader cannot be verified.

## Fixed

- Fix/cleanup YAML config keys to keep config.yml parseable.
- Fix Paper/Purpur async safety: `PlayerClientDetectEvent` (and `PlayerBlacklistedModEvent`) are now dispatched on the main thread, preventing `PlayerClientDetectEvent may only be triggered asynchronously`.

# ClientWatch 0.1.6

## Fixed

- Fix crash on startup: invalid plugin-messaging channel `FML|HS` (now uses `fml:hs`).

## Added

- Login-time protection: kick player when loader resolves to `UNKNOWN` or when loader cannot be verified (modhider-like behavior).

Fabric mod detection fix: correctly parse and recognize Fabric loader mod lists from client-reported plugin channels.

## Fixed

- Fix Fabric client mod list parsing: decode plugin channel payloads (`FML|HS` handshake) to extract and validate mod metadata (mod ID, name, version).
- Ensure Fabric mod IDs are correctly matched against the blacklist (e.g., `meteor-client`, `baritone`, etc.).
- Prevent "No reliable client-provided mod list is available" false negatives for Fabric clients with mods installed.

## Changed

- Improved mod detection logging for Fabric handshake payloads to aid troubleshooting.
- Bumped plugin version to `0.1.5`.


# ClientWatch 0.1.4

Configuration and startup robustness fixes to prevent YAML parsing errors and plugin enable failures.

## Fixed

- Ensure default `config.yml` and `messages.yml` are valid YAML: quote strings containing brace placeholders (for example `"{player}"` and `"{matches}"`) to avoid SnakeYAML parsing errors such as "mapping values are not allowed here".
- Prevent plugin enable failure when an existing `plugins/ClientWatch/config.yml` or `messages.yml` contains unquoted placeholders; plugin now logs a clear warning and falls back to safe defaults.

## Changed

- Improved startup logging for YAML parse issues with guidance to fix malformed config/messages files.
- Bumped plugin version to `0.1.4`.


# ClientWatch 0.1.3

Debugging and developer visibility improvements.

## Added

- Top-level `debug` config option to enable verbose plugin debug logs.
- Debug logging in the brand detection pipeline (plugin messages, VarInt parsing, and source selection).



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
