<div align="center">
  <img src="https://cdn.modrinth.com/data/cached_images/6271845ecabf9719668a33c3998ae6e87a8fa222.png" alt="SolarElo Banner" width="100%" />

  # ☀️ SolarElo — Advanced PvP ELO & Rank Solution

  [![Minecraft 1.21+](https://img.shields.io/badge/Minecraft-1.21%2B-brightgreen.svg)](https://papermc.io)
  [![Paper & Folia Ready](https://img.shields.io/badge/Server-Paper%20%7C%20Leaf%20%7C%20Folia-blue.svg)](https://papermc.io)
  [![Built with Triumph-GUI](https://img.shields.io/badge/GUI-Triumph--GUI-orange.svg)](https://github.com/TriumphTeam/triumph-gui)
  [![Database](https://img.shields.io/badge/Database-H2%20%7C%20MySQL%20%7C%20SQLite-yellow.svg)](https://github.com/brettwooldridge/HikariCP)
  [![bStats](https://bstats.org/signatures/bukkit/SolarElo.svg)](https://bstats.org/plugin/bukkit/SolarElo/31740)

  *Say goodbye to unfair PvP farming, win-trading, and complex setup headaches.*
</div>

---

### Why SolarElo?
SolarElo calculates, tracks, and manages PvP ELO with zero performance impact — fully compatible and optimized for **Paper**, **Leaf**, and **Folia**. This plugin provides a highly scalable and secure competitive experience, complete with dynamic ranking formulas, custom player bounty creation, robust anti-abuse algorithms, interactive GUIs, and seamless Discord integration.

> **WARNING — AI-Assisted Development**
>
> This plugin's codebase has been developed, refactored, and optimized with the assistance of advanced AI coding agents. Please test updates in a development or staging environment before deploying to production.

---

## 🌟 Main Feature Showcase

| | |
| :--- | :--- |
| ![Main GUI](https://cdn.modrinth.com/data/cached_images/6271845ecabf9719668a33c3998ae6e87a8fa222.png) | **Advanced PvP Menu** <br><br> Intuitive GUI system powered by Triumph-GUI that makes PvP tracking simple, with quick controls, clear ELO metrics, and easy access to all features. |
| **Global Leaderboard & Stats** <br><br> View top players asynchronously using player skulls, with multi-sorting modes (Top Elo, Top Kills, Top Streak, Top Bounty, Top Loss). Inspect detailed PvP profiles by clicking player heads. | ![Leaderboard](https://cdn.modrinth.com/data/cached_images/f7e84b5e46f148585d4f92364b13f70acc28b555.png) |
| ![Bounty Board](https://cdn.modrinth.com/data/cached_images/f83b1876ec5844f49eea69c848a6adce7a8d7d04.png) | **Custom Player Bounty Creation (`/bounty`)** <br><br> Open `/bounty` to select any online target, adjust bounty amounts using `+`/`-` buttons or type custom amounts in chat. Defeating a target immediately awards the full bounty Elo! |
| **Tiered Rank Rewards** <br><br> View rank requirements, progression milestones, and claimable bracket rewards all inside a visual tiered dashboard with automatic rank promotion commands. | ![Rank Rewards](https://cdn.modrinth.com/data/cached_images/46d3ff5726eec406959089780f002275cd18421d.png) |
| ![Player Settings](https://cdn.modrinth.com/data/cached_images/11c41ab1a8dea56af89681896dd153f7d8452090.png) | **Player Settings GUI** <br><br> Personalized control panel for players. Toggle PvP notifications, customize sound effects, configure visual particles, and manage personal PvP preferences directly. |
| **Admin Player List** <br><br> Easily browse online and offline players. Teleport to target players, view active bounty counts, and access individual profiles with a single click. | ![Admin Player List](https://cdn.modrinth.com/data/cached_images/e2b8f599c7a11f86881cbd2027920f8fff53d265.png) |
| ![Admin Player Editor](https://cdn.modrinth.com/data/cached_images/16acea286a4c80ee47cdbb9f6380756376439c48.png) | **Admin Player Editor** <br><br> Modify ELO balances, reset K/D stats, lock or unlock accounts, and execute immediate ELO adjustments in real-time. |

---

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

### 🤖 Discord & Season Automation
Sync server seasons and PvP activity automatically to keep the competition fresh:
* **Rich Discord Webhooks (`discord.yml`)**: Dispatch gorgeous Rich Embeds for player kills, rankups, or dethroning the Top 1 player.
* **Season Resets (`season.yml`)**: Program seasonal resets that soft-reset ELO (compressing scores to balance new and veteran players) and execute custom console commands to reward rank brackets.

---

## ⚡ Placeholders

SolarElo provides full native **PlaceholderAPI** support:

| Placeholder | Description | Example |
| :--- | :--- | :--- |
| `%solarelo_elo%` | Current ELO score | `1250` |
| `%solarelo_rank%` | Current rank name | `Gold III` |
| `%solarelo_rank_prefix%` | Current rank prefix | `&#ffaa00[Gold III]` |
| `%solarelo_bounty%` | Current bounty placed on player | `500` |
| `%solarelo_kills%` | Total PvP kills | `142` |
| `%solarelo_deaths%` | Total PvP deaths | `38` |
| `%solarelo_kd%` | Kill/Death ratio | `3.74` |
| `%solarelo_winrate%` | Win percentage | `78.9%` |
| `%solarelo_streak%` | Current killstreak | `7` |
| `%solarelo_best_streak%` | All-time highest killstreak | `15` |
| `%solarelo_top_1_name%` | Name of #1 ELO player | `Alex` |
| `%solarelo_top_1_elo%` | ELO score of #1 player | `2450` |

---

## 💻 Commands & Permissions

| Command | Description | Permission | Default |
| :--- | :--- | :--- | :--- |
| `/elo` | Open the main ELO menu GUI | *None* | Players |
| `/elo <player>` | View another player's PvP stats | *None* | Players |
| `/topelo` | Open the global ELO leaderboard GUI | *None* | Players |
| `/bounty` | Open the custom ELO bounty creation GUI | *None* | Players |
| `/stats` | View your personal PvP statistics | *None* | Players |
| `/eloadmin` | Open the admin settings panel GUI | `solarelo.admin` | OP |
| `/eloadmin search <player>` | Open the admin player search & edit editor | `solarelo.admin` | OP |
| `/eloadmin set/add/remove <player> <val>` | Directly modify player ELO scores | `solarelo.admin` | OP |
| `/eloadmin lock/unlock <player>` | Lock/unlock player ELO states | `solarelo.admin` | OP |
| `/eloadmin reset <player>` | Reset all PvP stats for a player | `solarelo.admin` | OP |
| `/eloadmin season reset` | Force-reset season and trigger rewards | `solarelo.admin` | OP |
| `/eloadmin reload` | Reload all YAML configs instantly | `solarelo.admin` | OP |

---

## 📊 Statistics

[![bStats](https://bstats.org/signatures/bukkit/SolarElo.svg)](https://bstats.org/plugin/bukkit/SolarElo/31740)
