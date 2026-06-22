# ClientWatch

ClientWatch is a modern Paper plugin for reliable Minecraft client inspection.

It helps server administrators collect and review the client information that a Minecraft server can actually observe, while clearly marking information that is not available or cannot be detected reliably.

## What ClientWatch Detects

- Player name and UUID
- Join time
- Ping
- Online mode
- Server brand
- Client brand, when provided by the client
- Protocol version, when exposed by the server API
- Loader inference from reliable observable data
- Installed mods only when a trustworthy client-provided mod list is available

## Honest Detection

ClientWatch does not fake detections.

Minecraft servers cannot reliably read every client, mod, or modification without client cooperation. If a mod list is not provided through a trustworthy server-visible channel, ClientWatch reports it as unavailable instead of guessing.

## Blacklist System

Server owners can configure blacklists for:

- Mods
- Mod IDs
- Client brands
- Client names
- Mod loaders

When a blacklist match is detected, ClientWatch can run multiple actions at the same time.

## Actions

Supported actions in the current development release:

- Kick
- Console message
- Staff message
- Ban command
- Discord webhook alert

Richer GUI workflows and persistent database backends are planned for later releases.

## Commands

- `/clientwatch`
- `/clientwatch reload`
- `/clientwatch version`
- `/clientwatch update`
- `/clientwatch inspect <player>`
- `/clientwatch info <player>`
- `/clientwatch mods <player>`
- `/clientwatch history <player>`
- `/clientwatch online`
- `/clientwatch search <mod>`
- `/clientwatch export <player>`
- `/clientwatch alerts`
- `/clientwatch blacklist`
- `/clientwatch whitelist`

## API

ClientWatch exposes Bukkit events for other plugins:

- `PlayerClientDetectEvent`
- `PlayerModsLoadedEvent`
- `PlayerBlacklistedModEvent`
- `PlayerInspectionEvent`

ClientWatch also provides an API facade for latest detections, online detection snapshots, mod searches, and JSON exports.

## Requirements

- Paper 1.21+
- Java 21+

## Development Status

ClientWatch is currently in early development. The foundation is implemented, and future releases are planned to add persistent database backends, an inventory GUI, and more advanced detection modules.
