# ☀️ SolarElo — Advanced PvP ELO & Rank Solution

[![Minecraft 1.21+](https://img.shields.io/badge/Minecraft-1.21%2B-brightgreen.svg)](https://papermc.io)
[![Paper & Folia Ready](https://img.shields.io/badge/Server-Paper%20%7C%20Leaf%20%7C%20Folia-blue.svg)](https://papermc.io)
[![Built with Triumph-GUI](https://img.shields.io/badge/GUI-Triumph--GUI-orange.svg)](https://github.com/TriumphTeam/triumph-gui)
[![Database](https://img.shields.io/badge/Database-H2%20%7C%20MySQL%20%7C%20SQLite-yellow.svg)](https://github.com/brettwooldridge/HikariCP)
[![bStats](https://bstats.org/signatures/bukkit/SolarElo.svg)](https://bstats.org/plugin/bukkit/SolarElo/31740)

Say goodbye to unfair PvP farming, win-trading, and clunky configuration. **SolarElo** is a modern, high-performance **PvP ELO, Rank & Bounty Management System** built for Paper, Leaf, and Folia 1.21+ servers.

---

## ✨ Feature Showcase

![SolarElo Main Banner](https://cdn.modrinth.com/data/cached_images/6271845ecabf9719668a33c3998ae6e87a8fa222.png)

### 🎯 Custom Player Bounty Creation System (`/bounty`)
![Bounty System](https://cdn.modrinth.com/data/cached_images/f83b1876ec5844f49eea69c848a6adce7a8d7d04.png)
- **Interactive GUI & Chat Input**: Open `/bounty` to select any online target, adjust Elo bounty with `+`/`-` buttons or type exact amounts in chat via anvil/sign/dialog input helpers.
- **ESC Exit & Clean UX**: Effortlessly navigate menus or cancel creation using ESC with zero leftover inventory bugs.
- **Instant Claiming**: Defeating a player with an active bounty instantly awards the full placed Elo bounty to the killer, accompanied by server-wide broadcast announcements!

### 📊 Global Leaderboard & Stats GUI (`/topelo` & `/stats`)
![Global Leaderboard](https://cdn.modrinth.com/data/cached_images/f7e84b5e46f148585d4f92364b13f70acc28b555.png)
- **Multi-Sort Modes**: Sort rankings asynchronously by **Top ELO**, **Top Kills**, **Top Streak**, **Top Bounty**, or **Top Loss**.
- **Player Skulls**: Real-time async player head rendering without main-thread server lag.
- **Detailed Player Stats**: Click any player head in the leaderboard to inspect their full PvP breakdown (K/D ratio, Winrate, Current Rank, Best Streak, and Active Bounty).

### 🏆 Tiered Ranks & Rewards (`/elo`)
![Tiered Ranks](https://cdn.modrinth.com/data/cached_images/46d3ff5726eec406959089780f002275cd18421d.png)
- **Custom Rank Progression**: Define custom ranks in `rank.yml` with custom thresholds, prefixes, and hex color tags.
- **Claimable Rank Rewards**: Interactive GUI panel displaying rank requirements, milestone badges, and claimable reward kits or console commands.
- **Rank Change Effects**: Automatic title announcements, sound effects, particle bursts, and console command execution upon rank updates.

### ⚙️ Player Settings GUI
![Player Settings](https://cdn.modrinth.com/data/cached_images/11c41ab1a8dea56af89681896dd153f7d8452090.png)
- Customize personal sound preferences, toggle visual particle effects, and adjust PvP chat alert notifications.

### 🛡️ Admin Management & Player Editor (`/eloadmin`)
![Admin Player List](https://cdn.modrinth.com/data/cached_images/e2b8f599c7a11f86881cbd2027920f8fff53d265.png)
![Admin Player Editor](https://cdn.modrinth.com/data/cached_images/16acea286a4c80ee47cdbb9f6380756376439c48.png)
- Search players, edit ELO values in real-time, lock/unlock player accounts, and manage seasons effortlessly.

### 🛡️ Smart Anti-Farm & Anti-Exploit Security
Prevent alt-farming, win-trading, and stat inflating with 5 layers of protection:
- **IP & Subnet Detection**: Blocks ELO changes when players kill alts on the same IP or `/24` subnet.
- **AFK & Movement Check**: Cancels ELO gain if the victim has been motionless or AFK before dying.
- **Spawn Camping Protection**: Restricts ELO gains within configurable radius of spawn or recent respawn timers.
- **Diminished Returns**: Repeated kills on the same player yield lower ELO gains over rolling hours.
- **ELO Difference Caps**: Prevents high-ranked veterans from farming new unranked players.

### 🤖 Rich Discord Webhooks & Season Automation
- **Discord Integration**: Send formatted Discord embeds for PvP kills, rank promotions, top 1 dethroning, and bounty claims.
- **Automated Season Resets**: Program seasonal resets that soft-compress ELO scores towards baselines, rewarding top brackets with automated console rewards.

---

## 💻 Commands & Permissions

| Command | Permission | Description |
| :--- | :--- | :--- |
| `/elo` | `solarelo.use` | Open the main SolarElo GUI |
| `/elo <player>` | `solarelo.use` | Inspect another player's PvP profile |
| `/topelo` | `solarelo.use` | Open the Global Leaderboard GUI |
| `/bounty` | `solarelo.use` | Open the Custom Bounty Creation GUI |
| `/stats` | `solarelo.use` | View your personal PvP statistics |
| `/eloadmin` | `solarelo.admin` | Open the Admin Control Panel GUI |
| `/eloadmin search <player>` | `solarelo.admin` | Search & edit player ELO live |
| `/eloadmin set <player> <elo>` | `solarelo.admin` | Set a player's exact ELO score |
| `/eloadmin add <player> <elo>` | `solarelo.admin` | Add ELO to a player |
| `/eloadmin remove <player> <elo>`| `solarelo.admin` | Deduct ELO from a player |
| `/eloadmin lock/unlock <player>` | `solarelo.admin` | Lock/unlock ELO gain for a player |
| `/eloadmin reset <player>` | `solarelo.admin` | Reset all stats for a player |
| `/eloadmin season reset` | `solarelo.admin` | Trigger a manual Season Reset |
| `/eloadmin reload` | `solarelo.admin` | Reload all YAML configs |

---

## 📈 PlaceholderAPI List

| Placeholder | Description | Example Output |
| :--- | :--- | :--- |
| `%solarelo_elo%` | Current ELO score | `1250` |
| `%solarelo_rank%` | Current rank name | `Gold III` |
| `%solarelo_rank_prefix%` | Current rank prefix | `&#ffaa00[Gold III]` |
| `%solarelo_bounty%` | Current bounty on player | `500` |
| `%solarelo_kills%` | Total PvP kills | `142` |
| `%solarelo_deaths%` | Total PvP deaths | `38` |
| `%solarelo_kd%` | Kill/Death ratio | `3.74` |
| `%solarelo_winrate%` | Win percentage | `78.9%` |
| `%solarelo_streak%` | Current killstreak | `7` |
| `%solarelo_best_streak%` | Highest historical killstreak | `15` |
| `%solarelo_top_1_name%` | Name of #1 ELO player | `Alex` |
| `%solarelo_top_1_elo%` | ELO score of #1 player | `2450` |

---

## 📜 SpigotMC / BuiltByBit BBCode Format (Copy-Paste Ready)

Copy code bên dưới dán trực tiếp vào khung mô tả (BBCode Editor) của SpigotMC / BuiltByBit:

```text
[CENTER][IMG]https://cdn.modrinth.com/data/cached_images/6271845ecabf9719668a33c3998ae6e87a8fa222.png[/IMG][/CENTER]

[CENTER][SIZE=6][B]☀️ SolarElo[/B][/SIZE]
[SIZE=4]Advanced PvP ELO, Rank & Bounty Solution for Minecraft Servers[/SIZE][/CENTER]

[HR][/HR]

[CENTER]
Say goodbye to unfair PvP farming, win-trading, and complex setup headaches.
SolarElo calculates, tracks, and manages PvP ELO with [B]zero performance impact[/B] —
fully compatible and optimized for [B]Paper[/B], [B]Leaf[/B], and [B]Folia 1.21+[/B].
[/CENTER]

[HR][/HR]

[CENTER][SIZE=5][B]✨ Feature Showcase[/B][/SIZE][/CENTER]

[CENTER][IMG]https://cdn.modrinth.com/data/cached_images/f83b1876ec5844f49eea69c848a6adce7a8d7d04.png[/IMG][/CENTER]
[B]🎯 Custom Bounty Creation System (/bounty)[/B]
Open /bounty to choose target players, adjust bounty amounts using quick + / - buttons, or type custom numbers via chat input. Defeating a target immediately claims their accumulated bounty Elo!

[CENTER][IMG]https://cdn.modrinth.com/data/cached_images/f7e84b5e46f148585d4f92364b13f70acc28b555.png[/IMG][/CENTER]
[B]📊 Global Leaderboard & Profiles (/topelo & /stats)[/B]
Asynchronous player head rendering with multi-sorting modes (Top Elo, Top Kills, Top Streak, Top Bounty, Top Loss). Click player heads to view detailed PvP statistics.

[CENTER][IMG]https://cdn.modrinth.com/data/cached_images/46d3ff5726eec406959089780f002275cd18421d.png[/IMG][/CENTER]
[B]🏆 Tiered Rank Progression & Rewards[/B]
Visual rank dashboard displaying requirements, progression status, and claimable rank rewards with custom console command execution on rank promotion.

[CENTER][IMG]https://cdn.modrinth.com/data/cached_images/11c41ab1a8dea56af89681896dd153f7d8452090.png[/IMG][/CENTER]
[B]⚙️ Player Settings GUI[/B]
Customize personal sound preferences, toggle visual particle effects, and adjust PvP chat alert notifications.

[CENTER][IMG]https://cdn.modrinth.com/data/cached_images/e2b8f599c7a11f86881cbd2027920f8fff53d265.png[/IMG][/CENTER]
[CENTER][IMG]https://cdn.modrinth.com/data/cached_images/16acea286a4c80ee47cdbb9f6380756376439c48.png[/IMG][/CENTER]
[B]🛡️ Admin Management & Player Editor (/eloadmin)[/B]
Search players, edit ELO values in real-time, lock/unlock player accounts, and manage seasons effortlessly.

[HR][/HR]

[CENTER][SIZE=5][B]🛡️ Intelligent Anti-Farm & Security[/B][/SIZE]
Integrated security algorithms: IP/Subnet checks, AFK movement checks, spawn camping protection, kill diminishing returns, and max Elo difference caps.[/CENTER]

[HR][/HR]

[CENTER][SIZE=5][B]📊 PlaceholderAPI Support[/B][/SIZE][/CENTER]

[LIST]
[*][ICODE]%solarelo_elo%[/ICODE] — Current ELO score
[*][ICODE]%solarelo_rank%[/ICODE] — Current rank name
[*][ICODE]%solarelo_rank_prefix%[/ICODE] — Current rank prefix
[*][ICODE]%solarelo_bounty%[/ICODE] — Active bounty on player
[*][ICODE]%solarelo_kills%[/ICODE] — Total PvP kills
[*][ICODE]%solarelo_deaths%[/ICODE] — Total PvP deaths
[*][ICODE]%solarelo_kd%[/ICODE] — Kill/Death ratio
[*][ICODE]%solarelo_winrate%[/ICODE] — Win percentage
[*][ICODE]%solarelo_streak%[/ICODE] — Current killstreak
[*][ICODE]%solarelo_best_streak%[/ICODE] — Historical best killstreak
[/LIST]

[HR][/HR]

[CENTER][SIZE=5][B]💻 Key Commands[/B][/SIZE][/CENTER]

[LIST]
[*][ICODE]/elo[/ICODE] — Main PvP Menu GUI
[*][ICODE]/topelo[/ICODE] — Global Leaderboard GUI
[*][ICODE]/bounty[/ICODE] — Custom Bounty Creation GUI
[*][ICODE]/stats[/ICODE] — Personal Stats Profile
[*][ICODE]/eloadmin[/ICODE] — Admin Management Panel (solarelo.admin)
[/LIST]

[HR][/HR]

[CENTER][URL=https://bstats.org/plugin/bukkit/SolarElo/31740][IMG]https://bstats.org/signatures/bukkit/SolarElo.svg[/IMG][/URL][/CENTER]
```
