# 🛠️ SolarElo - Changelog & DevLog (v1.1-R08)

### 📋 Release Notes / Changelog:
- Fixed `IllegalStateException: Thread failed main thread check: Cannot init menu async` on Canvas/Folia [#1.1-R08](https://github.com/OmhVN/SolarElo/commit/892cadf)
- Migrated all ActionBars, Hover Stats, and Click Events from legacy BungeeCord Chat API to 100% Paper Adventure API [#1.1-R08](https://github.com/OmhVN/SolarElo/commit/b7231f6)
- Integrated high-performance Caffeine Cache (`expireAfterWrite 10s`) for zero-latency rank lookup [#1.1-R08](https://github.com/OmhVN/SolarElo/commit/4e4a568)
- Integrated Triumph-GUI library (`dev.triumphteam:triumph-gui`) with automated package relocation [#1.1-R08](https://github.com/OmhVN/SolarElo/commit/7fd9d2a)
- Fixed asynchronous `victim.getLocation()` thread safety violation during PvP Anti-Farm verification [#1.1-R07-Fix](https://github.com/OmhVN/SolarElo/commit/8fb281b)
- Fixed startup check sequence to run integrity signature check before displaying the ASCII banner [#1.1-R07-Fix](https://github.com/OmhVN/SolarElo/commit/24e12cf)
- Cleaned up 100% redundant comments and optimized build dependencies across all source files [#1.1-R08](https://github.com/OmhVN/SolarElo/commit/dc2fce5)

---

### 💬 Discord Announcement Format:
```markdown
**DevLog Plugins #134 28 / 7 / 2026**
• **SolarElo-1.1-R08 (Canvas / Folia Thread-Safety & Library Upgrade)**

- Fixed `IllegalStateException: Cannot init menu async` on Canvas/Folia [#1.1-R08](https://github.com/OmhVN/SolarElo)
- Migrated all ActionBars & Hover Stats from BungeeCord to 100% Paper Adventure API [#1.1-R08](https://github.com/OmhVN/SolarElo)
- Integrated high-performance Caffeine Cache (`10s TTL`) for zero-latency rank lookup [#1.1-R08](https://github.com/OmhVN/SolarElo)
- Integrated Triumph-GUI library (`dev.triumphteam:triumph-gui`) with automated relocation [#1.1-R08](https://github.com/OmhVN/SolarElo)
- Fixed async `victim.getLocation()` thread safety during Anti-Farm check [#1.1-R07-Fix](https://github.com/OmhVN/SolarElo)
- Cleaned up 100% redundant comments across all source files [#1.1-R08](https://github.com/OmhVN/SolarElo)

@• KHÁCH HÀNG (#) • - Mua Tại: # 🎫 | ticket
```
