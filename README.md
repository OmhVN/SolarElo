![solarelo](https://cdn.modrinth.com/data/cached_images/5bc184719d98ac36e88cd40eacde0bb7f9388033.png)

### Why SolarElo?
Say goodbye to unfair PvP farming, win-trading, and complex setup headaches. SolarElo calculates, tracks, and manages PvP ELO with zero performance impact - fully compatible and optimized for **Paper** and **Folia**. This plugin provides a highly scalable and secure competitive experience, complete with dynamic ranking formulas, robust anti-abuse algorithms, interactive GUIs, and seamless Discord integration.

> **WARNING — AI-Assisted Development**
>
> This plugin's codebase has been developed, refactored, and optimized with the assistance of advanced AI coding agents. Please test updates in a development or staging environment before deploying to production.

---

## Main Feature Showcase

| | |
| :--- | :--- |
| ![Main GUI](https://cdn.modrinth.com/data/cached_images/6271845ecabf9719668a33c3998ae6e87a8fa222.png) | **Advanced PvP Menu** <br><br> Intuitive GUI system that makes PvP tracking simple, with quick controls, clear ELO metrics, and easy access to other features. |
| **Global Leaderboard & Stats** <br><br> View top players asynchronously using player skulls, supporting sorting options (High to Low, Low to High, Online only). Inspect detailed PvP profiles by clicking player heads. | ![Leaderboard](https://cdn.modrinth.com/data/cached_images/f7e84b5e46f148585d4f92364b13f70acc28b555.png) |
| ![Bounty Board](https://cdn.modrinth.com/data/cached_images/f83b1876ec5844f49eea69c848a6adce7a8d7d04.png) | **Dynamic Bounty Contracts & Quests** <br><br> Accept random PvP assassination contracts directly from the Bounty GUI. Hunt online targets to claim ELO, money, and custom command rewards. |
| **Tiered Rank Rewards** <br><br> View rank requirements, progression milestones, and claimable bracket rewards all inside a visual tiered dashboard. | ![Rank Rewards](https://cdn.modrinth.com/data/cached_images/46d3ff5726eec406959089780f002275cd18421d.png) |
| ![Player Settings](https://cdn.modrinth.com/data/cached_images/11c41ab1a8dea56af89681896dd153f7d8452090.png) | **Player Settings GUI** <br><br> Personalized control panel for players. Toggle PvP notifications, customize sound effects, configure visual particles, and manage personal PvP preferences directly. |
| **Admin Player List** <br><br> Easily browse online and offline players. Teleport to target players, view active bounty counts, and access individual profiles with a single click. | ![Admin Player List](https://cdn.modrinth.com/data/cached_images/e2b8f599c7a11f86881cbd2027920f8fff53d265.png) |
| ![Admin Player Editor](https://cdn.modrinth.com/data/cached_images/16acea286a4c80ee47cdbb9f6380756376439c48.png) | **Admin Player Editor** <br><br> Modify ELO balances, reset K/D stats, lock or unlock accounts, and execute immediate ELO adjustments in real-time. |

### 🛡️ Intelligent Anti-Farm & Security
Security is at the heart of SolarElo. Our anti-exploit algorithms detect and neutralize win-trading and alt-farming instantly:
* **IP & Subnet Check**: Blocks ELO changes when players defeat alts sharing the same IP address or `/24` subnet.
* **AFK Detection**: Blocks ELO gains if the victim hasn't moved (`no-move-seconds`) or fought back (`no-attack`) recently. Move checks apply strictly to the victim's first death after logging in.
* **Diminished Kill Returns**: ELO gain is halved for repeated kills on the same player within a rolling hour.
* **Spawn Camping Protection**: Restricts ELO gains near spawn points and shortly after resurrection.
* **ELO Difference Capping**: Configurable threshold (e.g., maximum 200 ELO difference) prevents high-ranked veterans from farming new players.

```yaml
# config.yml
anti-farm:
  enabled: true
  same-player-cooldown: 300
  repeat-kill-threshold: 3
  diminished-return-percent: 50
  elo-difference:
    enabled: true
    max-difference: 200
  ip-check:
    enabled: true
    prevent-same-ip: true
    prevent-same-subnet: true
  activity-check:
    enabled: true
    no-move-seconds: 15
    no-attack-seconds: 15
    spawn-camping:
      enabled: true
      protection-seconds: 10
      protection-radius: 15
      action: BLOCK
```

### Dynamic Bounty Contracts & Streaks
Motivate players to dominate the leaderboards with rewarding PvP systems:
* **Killstreak Rewards**: Earn extra ELO multiplier bonuses for high streaks, display epic titles, and risk losing more ELO if slain while carrying a massive streak.
* **Bounty Quests**: Accept random contracts to assassinate specific players for custom item rewards, money, or bonus ELO.
* **Top Player Bounties**: Hunt the server's Top 1, Top 2, or Top 3 players to claim specialized bounty payouts automatically.

### 🤖 Discord & Season Automation
Sync server seasons and PvP activity automatically to keep the competition fresh:
* **Rich Discord Webhooks (`discord.yml`)**: Dispatch gorgeous Rich Embeds for player kills, rankups, or dethroning the Top 1 player.
* **Season Resets (`season.yml`)**: Program seasonal resets that soft-reset ELO (compressing scores to balance new and veteran players) and execute custom console commands to reward rank brackets.

```yaml
# discord.yml
discord-webhook:
  enabled: false
  url: "https://discord.com/api/webhooks/..."
  events:
    kill:
      enabled: true
      use-embed: true
      embed:
        title: "⚔️ Player Defeated"
        description: "**{killer}** ({killer_elo} Elo) has defeated **{victim}** ({victim_elo} Elo)!"
        color: "#ff3c3c"
    top-1-defeat:
      enabled: true
      use-embed: true
      embed:
        title: "👑 TOP 1 Defeated"
        color: "#ffaa00"
    rank-up:
      enabled: true
      use-embed: true
      embed:
        title: "⚡ Rank Up"
        color: "#00ffcc"
```

```yaml
# season.yml
season:
  end-date: "2026-07-01 00:00:00"
  soft-reset:
    enabled: true
    multiplier: 0.4
    reset-stats: true
  rewards:
    ranks:
      "1":
        - "broadcast #ffaa00★ MÙA GIẢI KẾT THÚC! #e0e0e0Người chơi #00ff3c{player} đạt TOP 1!"
      "2":
        - "broadcast #ffaa00★ MÙA GIẢI KẾT THÚC! #e0e0e0Người chơi #00ff3c{player} đạt TOP 2!"
    brackets:
      "4-10":
        - "give {player} gold_ingot 5"
```

---

## Highlighted Features

| Feature | Description |
| :--- | :--- |
| **4 ELO Scoring Modes** | Choose between standard `FORMULA` (K-factor based), `RANDOM` ranges, `KD`-ratio scaling, or write your own mathematical formulas (`CUSTOM`). |
| **ELO Decay** | Prevent leaderboard stagnation by automatically decaying ELO from inactive Top 10 players. |
| **ELO Lock System** | Automatically lock a player's ELO if it drops below a threshold to prevent negative point abuse. |
| **Interactive PvP Chat** | Hover over kill feed messages to view detailed player stats or click names to open their ELO profile. |
| **Database Engines** | Store data safely in SQLite or high-performance MySQL/MariaDB for multi-lobby networks. |
| **Actionbar & Titles** | Show real-time ELO gain/loss floating popups, titles, and sound effects on PvP events. |
| **Streak Milestones** | Track killstreaks with extra ELO multipliers, epic title announcements, and severe death streak penalties. |
| **Dynamic Rank Actions** | Execute console commands when a player thangs rank (e.g. giving ranks, permissions, items). |
| **Effects & Particles** | Display unique visual particle effects (e.g., Totem of Undying, Enchantment Table) and play configurable sounds on PvP actions. |
| **Hex & MiniMessage** | Style all ingame chat feeds, titles, and GUI descriptions using Hex codes and modern MiniMessage formatting. |
| **Developer API** | Seamlessly integrate with external plugins using the robust developer API and a cache-optimized PlaceholderAPI suite. |

---

## Placeholders

SolarElo provides full native **PlaceholderAPI** support:

| Placeholder | Description |
| :--- | :--- |
| `%solarelo_elo%` | Current ELO score |
| `%solarelo_kills%` | Total PvP kills |
| `%solarelo_deaths%` | Total PvP deaths |
| `%solarelo_kd%` | Kill/Death ratio |
| `%solarelo_streak%` | Current killstreak |
| `%solarelo_best_streak%` | All-time highest killstreak |
| `%solarelo_rank%` | Current rank name |
| `%solarelo_rank_prefix%` | Current rank prefix |

---

## 💻 Commands & Permissions

| Command | Description | Permission | Default |
| :--- | :--- | :--- | :--- |
| `/elo` | Open the main ELO menu GUI | *None* | Players |
| `/elo <player>` | View another player's PvP stats | *None* | Players |
| `/topelo` | Open the global ELO leaderboard GUI | *None* | Players |
| `/bounty` | Open the active ELO bounty GUI | *None* | Players |
| `/eloadmin` | Open the admin settings panel GUI | `solarelo.admin` | OP |
| `/eloadmin search <player>` | Open the admin player search & edit editor | `solarelo.admin` | OP |
| `/eloadmin set/add/remove <player> <val>` | Directly modify player ELO scores | `solarelo.admin` | OP |
| `/eloadmin lock/unlock <player>` | Lock/unlock player ELO states | `solarelo.admin` | OP |
| `/eloadmin reset <player>` | Reset all PvP stats for a player | `solarelo.admin` | OP |
| `/eloadmin season reset` | Force-reset season and trigger rewards | `solarelo.admin` | OP |
| `/eloadmin reload` | Reload all YAML configs instantly | `solarelo.admin` | OP |

---

## Statistics

[![bStats](https://bstats.org/signatures/bukkit/SolarElo.svg)](https://bstats.org/plugin/bukkit/SolarElo/31740)
