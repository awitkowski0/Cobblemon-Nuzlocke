# Cobblemon: Nuzlocke

A server-side Nuzlocke challenge mod for [Cobblemon](https://cobblemon.com/) on Minecraft 1.21.1.

## Features

- **Perma-Death Rule** - When a Pokémon faints, it's considered dead and automatically released from your party
- **Auto-Start** - Nuzlocke runs start automatically when you join the server
- **Graveyard** - View all your fallen Pokémon with `/nuzlocke graveyard`
- **Server-Side** - Works entirely on the server, no client mod needed
- **Configurable** - Admins can customize rules via commands or config file

## Requirements

- Minecraft 1.21.1
- Cobblemon 1.6.0+
- Fabric Loader 0.17.3+ (for Fabric version)

## Commands

### Player Commands

| Command | Description |
|---------|-------------|
| `/nuzlocke start` | Start a new Nuzlocke run |
| `/nuzlocke stop` | End your current run (requires confirmation) |
| `/nuzlocke status` | View your current run stats |
| `/nuzlocke graveyard` | View all fallen Pokémon |
| `/nuzlocke rules` | View current Nuzlocke rules |

### Admin Commands (Permission Level 2 required)

| Command | Description |
|---------|-------------|
| `/nuzlocke config reload` | Reload config from file |
| `/nuzlocke config show` | Show current config values |
| `/nuzlocke config permadeath <true/false>` | Toggle perma-death rule |
| `/nuzlocke config announcedeaths <true/false>` | Toggle death announcements |
| `/nuzlocke config deathhandling release` | Auto-release fainted Pokémon |
| `/nuzlocke config deathhandling markonly` | Only mark as dead (no release) |
| `/nuzlocke config deathmessage <message>` | Set custom death message |

## Configuration

Config file: `.minecraft/config/nuzlocke/nuzlocke.json`

```json
{
  "permaDeathRule": true,
  "announceDeaths": true,
  "deathMessage": "%pokemon% has fallen in battle...",
  "deathHandling": "RELEASE"
}
```

### Options

| Option | Default | Description |
|--------|---------|-------------|
| `permaDeathRule` | `true` | Enable perma-death (fainted = dead) |
| `announceDeaths` | `true` | Show death messages in chat |
| `deathMessage` | `%pokemon% has fallen in battle...` | Death announcement message |
| `deathHandling` | `RELEASE` | `RELEASE` = auto-release, `MARK_ONLY` = just block healing |

### Death Message Variables

- `%pokemon%` - Pokémon's nickname (or species if no nickname)
- `%species%` - Pokémon's species name
- `%level%` - Pokémon's level

## How It Works

1. When you join the server, a Nuzlocke run starts automatically
2. If any of your Pokémon faints (in battle or outside), it's marked as dead
3. Dead Pokémon are automatically released from your party
4. Healing dead Pokémon is blocked as a safety net
5. View your fallen friends anytime with `/nuzlocke graveyard`

Built to be used with [Cobblemon](https://cobblemon.com/)

PRs are welcome.
