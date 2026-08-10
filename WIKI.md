# ☀️ SolarElo Wiki - Tài liệu chi tiết từ A-Z

Chào mừng bạn đến với tài liệu hướng dẫn sử dụng và cấu hình đầy đủ của **SolarElo** - Giải pháp quản lý điểm Elo, thứ hạng PvP hiệu năng cao cho máy chủ Minecraft (Hỗ trợ Paper, Folia và tích hợp Discord Webhooks).

---

## 📌 1. Tổng quan & Tính năng chính

**SolarElo** là một plugin quản lý điểm Elo và xếp hạng PvP hiệu năng cao, tối ưu hóa toàn diện cho Folia và Paper, tích hợp sẵn các tính năng cao cấp:

*   **Hệ thống Xếp hạng Elo đa dạng**:
    *   Hỗ trợ 4 chế độ tính điểm Elo:
        *   `FORMULA`: Tính theo công thức Elo chuẩn quốc tế với K-factor.
        *   `RANDOM`: Điểm nhận/mất ngẫu nhiên trong khoảng min-max cấu hình.
        *   `KD`: Điểm nhận/mất dựa vào tỷ lệ K/D của sát thủ và nạn nhân.
        *   `CUSTOM`: Điểm nhận/mất dựa trên công thức toán học tự định nghĩa.
    *   Giới hạn điểm Elo tối thiểu tùy chỉnh.
*   **Hệ thống Cấp bậc & Thăng hạng (Rank System)**:
    *   Tự động thăng cấp/hạ cấp khi Elo thay đổi.
    *   Chạy các lệnh tùy biến từ console khi người chơi lên hạng/xuống hạng (`rankup-commands`).
    *   Tùy chỉnh tên hiển thị (Display) và tiền tố (Prefix) cho từng rank riêng biệt.
*   **Hệ thống Chống farm & Khai thác (Anti-Farm)**:
    *   **IP / Subnet Check**: Ngăn chặn nhận Elo khi hạ gục tài khoản clone cùng địa chỉ IP hoặc cùng dải IP subnet `/24`.
    *   **Same Player Cooldown**: Đặt thời gian chờ tối thiểu giữa các lần giết cùng một người chơi để nhận Elo.
    *   **Repeat Kill Limit**: Tự động giảm một nửa điểm Elo nhận được (Diminished) nếu giết cùng một người chơi vượt quá giới hạn cho phép trong 1 giờ.
    *   **Activity Checks (AFK)**: Chặn nhận Elo nếu nạn nhân không di chuyển (no-move) hoặc không tấn công lại (no-attack) trong khoảng thời gian quy định. **Đặc biệt, yêu cầu di chuyển (`no-move-seconds`) chỉ áp dụng cho lần chết đầu tiên** (khi số lần chết = 0) giúp chống tài khoản clone vừa đăng nhập đứng yên farm điểm mà không ảnh hưởng tới PvP thông thường.
    *   **Spawn Camping Check**: Chặn nhận Elo nếu nạn nhân bị giết trong thời gian ngắn sau khi hồi sinh (Spawn) và trong phạm vi bảo vệ của điểm spawn.
    *   **Elo Difference Check**: Chặn nhận Elo nếu chênh lệch điểm Elo giữa Killer và Victim quá lớn (mặc định > 200). Chống tình trạng cao thủ đi săn/farm newbie để lấy điểm.
*   **Tin nhắn PvP Tương tác cao (Interactive Chat & Hover Stats)**:
    *   Tự động gửi thông báo khi một người chơi hạ gục người chơi khác.
    *   **Hover tooltip**: Di chuột vào tên người chơi để hiển thị chỉ số PvP chi tiết (Elo, Cấp bậc, K/D Ratio, Số mạng giết/chết, Chuỗi thắng hiện tại) cấu hình tại `messages.yml`.
    *   **Click action**: Nhấp chuột trực tiếp vào tên người chơi để tự động thực thi lệnh `/elo <tên>` mở giao diện xem thông tin **Stats GUI**.
*   **Hệ thống Mùa giải & Tự động trao thưởng (Seasonal ELO & Auto Rewards)**:
    *   Tổ chức thi đấu theo mùa thông qua lệnh quản lý `/eloadmin season reset`.
    *   **Soft-Reset ELO**: Elo của người chơi được nén lại theo công thức `1000 + (Elo hiện tại - 1000) * multiplier` để cân bằng giữa người chơi cũ và mới khi bước sang mùa giải mới.
    *   **Auto-Reward Commands**: Tự động thực thi các lệnh console phát thưởng (vật phẩm, VIP, lệnh Permission) cho TOP thứ hạng hoặc khoảng thứ hạng được định cấu hình.
*   **Phần thưởng Chuỗi hạ gục (Kill Streak)**:
    *   Thưởng thêm phần trăm Elo khi đang có chuỗi giết liên tiếp.
    *   Tự động gửi thông báo chuỗi hạ gục (Title/Subtitle) ở các mốc cấu hình (milestones).
    *   Phạt trừ thêm điểm Elo khi chết lúc đang giữ chuỗi hạ gục cao.
*   **Hệ thống Săn tiền thưởng Cao thủ (Elo Bounty System)**:
    *   Treo thưởng tự động khi người chơi đạt mốc chuỗi hạ gục (Streak Bounties) hoặc đứng TOP thứ hạng cao trên server (Rank Bounties như TOP 1, TOP 2, TOP 3).
    *   Hạ gục người chơi bị treo thưởng giúp nhận thêm Elo và tự động kích hoạt phần thưởng lệnh Console (ví dụ phát quà, cộng tiền).
    *   Hệ thống tự động phát thông báo toàn Server khi có người đạt mốc bị treo thưởng hoặc khi phần thưởng được tuyên bố thu về (claim).
*   **Hệ thống Hao hụt Elo do không hoạt động (Elo Decay System)**:
    *   Tự động kiểm tra và trừ một lượng Elo (mặc định là 20 Elo) cho các người chơi trong **TOP 10** nếu họ không có bất kỳ thay đổi Elo nào trong khoảng thời gian quy định (ví dụ: `24h`, `3d`, v.v.).
    *   Hỗ trợ cấu hình khoảng thời gian quét (`check-interval`) và thời gian không hoạt động tối đa (`inactive-threshold`) với các đơn vị: `m` (phút), `h` (giờ), `d` (ngày).
    *   Người chơi đang trực tuyến sẽ nhận được thông báo trực quan khi bị trừ điểm Elo do không hoạt động.

*   **Tích hợp Discord Webhook**:
    *   Tự động gửi thông báo dạng văn bản đẹp mắt về kênh Discord khi xảy ra các sự kiện: PvP Kill, Hạ gục TOP 1 Server, Lên hạng (Rank up).
*   **Hiệu ứng & Âm thanh tùy biến (Sounds & Particles)**:
    *   Phát âm thanh và hiệu ứng hạt (Particle) tương thích cho các sự kiện: Cộng điểm Elo (+), Trừ điểm Elo (-), Nhận bonus Elo, Lên hạng, Xuống hạng.
*   **Tích hợp bStats & PlaceholderAPI**:
    *   Tự động gửi thống kê bStats bảo mật.
    *   Hỗ trợ đầy đủ các placeholder cho bảng điểm (Scoreboard), Tablist, Chat, v.v.

---

## 🎮 2. Lệnh (Commands) & Phân quyền (Permissions)

| Lệnh | Mô tả | Quyền hạn (Permission) | Mặc định | Ghi chú |
| :--- | :--- | :--- | :--- | :--- |
| `/elo` | Mở giao diện GUI Menu Chính (Main Menu) | *Không yêu cầu* | `True` | Giao diện 3 hàng gồm: Săn thưởng (kiếm), Bảng xếp hạng (cờ), Phần thưởng (rương) |
| `/elo <player>` | Xem thông số Elo/PvP dạng văn bản của người chơi khác | *Không yêu cầu* | `True` | Xem Elo, Rank, Kills, Deaths, K/D, Streak, Best Streak |
| `/topelo` | Mở trực tiếp giao diện GUI Bảng xếp hạng Elo | *Không yêu cầu* | `True` | Giao diện 6 hàng xem bảng xếp hạng ELO của toàn server |
| `/eloadmin search <player>` | Tìm kiếm & mở giao diện Admin chi tiết của người chơi | `solarelo.admin` | `OP` | Hỗ trợ cả phím tắt `/eloadmin <player>` |
| `/eloadmin lock <player>` | Khóa Elo của người chơi (không cộng/trừ Elo và khóa tính năng săn thưởng) | `solarelo.admin` | `OP` | Elo không đổi khi PvP hoặc Decay |
| `/eloadmin unlock <player>` | Mở khóa Elo của người chơi | `solarelo.admin` | `OP` | Khôi phục trạng thái hoạt động bình thường |
| `/eloadmin reload` | Tải lại toàn bộ cấu hình plugin | `solarelo.admin` | `OP` | Nạp lại file `config.yml`, `messages.yml`, `rank.yml` |
| `/eloadmin set <player/*> <amount>` | Thiết lập điểm Elo cho một người chơi hoặc toàn server | `solarelo.admin` | `OP` | Sử dụng `*` để áp dụng cho tất cả người chơi |
| `/eloadmin add <player/*> <amount>` | Cộng điểm Elo cho một người chơi hoặc toàn server | `solarelo.admin` | `OP` | Sử dụng `*` để áp dụng cho tất cả người chơi |
| `/eloadmin remove <player/*> <amount>` | Trừ điểm Elo của một người chơi hoặc toàn server | `solarelo.admin` | `OP` | Sử dụng `*` để áp dụng cho tất cả người chơi |
| `/eloadmin reset <player/*>` | Reset toàn bộ thông số Elo/PvP về mặc định | `solarelo.admin` | `OP` | Đưa Elo về mặc định và reset K/D/Streak về 0 |
| `/eloadmin season reset` | Kết thúc mùa giải, phát thưởng và thực hiện soft-reset Elo | `solarelo.admin` | `OP` | Trao thưởng cho TOP và nén Elo theo hệ số cấu hình |
| `/bounty` | Mở giao diện GUI Nhận nhiệm vụ săn thưởng tiêu diệt người chơi | *Không yêu cầu* | `True` | Có thể chọn mục tiêu online hoặc hủy bỏ nhiệm vụ |

---

## 📊 3. Hệ thống Placeholders

SolarElo đăng ký định danh `%solarelo_*%` để các plugin khác (như Scoreboard, Tablist, Hologram, Chat) có thể lấy dữ liệu PvP:

| Placeholder | Mô tả | Ví dụ kết quả |
| :--- | :--- | :--- |
| `%solarelo_elo%` | Điểm Elo hiện tại của người chơi | `1250` |
| `%solarelo_kills%` | Tổng số mạng hạ gục (Kills) | `42` |
| `%solarelo_deaths%` | Tổng số lần chết (Deaths) | `18` |
| `%solarelo_kd%` | Tỉ lệ K/D ratio (lấy 2 chữ số thập phân) | `2.33` |
| `%solarelo_streak%` | Chuỗi hạ gục hiện tại (Kill Streak) | `5` |
| `%solarelo_best_streak%` | Chuỗi hạ gục tốt nhất từ trước đến nay | `12` |
| `%solarelo_rank%` | Tên hiển thị của thứ hạng hiện tại (kèm màu sắc) | `&aLT4` |
| `%solarelo_rank_prefix%` | Tiền tố Rank hiện tại | `&a[LT4]` |

---

## ⚙️ 4. Chi tiết các File Cấu hình (Configurations)

### 📂 File cấu hình chính: `config.yml`
Quản lý chế độ tính điểm Elo, cấu hình chống farm, thưởng Top 1, hiển thị ActionBar/Title, và các hiệu ứng âm thanh/hạt.

```yaml
# Database settings
database:
  type: SQLITE   # SQLITE hoặc MYSQL
  mysql:
    host: localhost
    port: 3306
    database: solarelo
    username: root
    password: password
    pool-size: 10

# Starting elo for new players
default-elo: 1000

# Chế độ tính điểm Elo: FORMULA (công thức Elo chuẩn), RANDOM (ngẫu nhiên), KD (dựa trên tỷ lệ K/D), CUSTOM (công thức tùy chỉnh)
scoring-mode: FORMULA

elo:
  # FORMULA mode: K-factor càng cao = thay đổi elo càng lớn
  k-factor: 32

  # RANDOM mode
  random:
    min-gain: 5
    max-gain: 25
    min-loss: 5
    max-loss: 25

  # KD mode: base * (victim_kd / killer_kd)
  kd:
    base: 20
    min-gain: 3
    max-gain: 50
    min-loss: 3
    max-loss: 50

  # CUSTOM mode: Công thức toán tự định nghĩa (Hỗ trợ +, -, *, /, ^, ( ))
  # Các biến: {killer_elo}, {victim_elo}, {killer_kd}, {victim_kd}, {killer_streak}, {victim_streak}, {k_factor}
  custom:
    formula-gain: "32 * (1 - 1 / (1 + 10^((victim_elo - killer_elo) / 400)))"
    formula-loss: "32 * (1 - (1 - 1 / (1 + 10^((victim_elo - killer_elo) / 400))))"

  # Elo tối thiểu (có thể âm nếu set < 0)
  minimum-elo: -500

# Kill Streak
kill-streak:
  enabled: true
  reset-on-death: true
  bonus-per-kill-percent: 10
  max-bonus-streak: 10
  death-streak-penalty: 5
  announce-streaks:
    - 3
    - 5
    - 10
    - 15
    - 20

# Thưởng đặc biệt khi hạ gục người chơi đứng TOP 1 Server
top-1-bonus:
  enabled: true
  extra-percent: 20
  extra-flat: 10

# Hiển thị Elo thay đổi trên màn hình
display:
  actionbar:
    enabled: true
    duration-ticks: 60
    kill-format: "&a+{gained} Elo &7| &fElo: &e{elo} &7| &fStreak: &c{streak}"
    death-format: "&c-{lost} Elo &7| &fElo: &e{elo}"
  title:
    enabled: true
    streak-title: "&c&l{streak} KILL STREAK!"
    streak-subtitle: "&e+{bonus}% Elo Bonus"
    rank-up-title: "&6&lRANK UP!"
    rank-up-subtitle: "&eBạn đã lên rank {rank}"
    rank-down-title: "&c&lRANK DOWN!"
    rank-down-subtitle: "&7Bạn đã xuống rank {rank}"
    fade-in: 5
    stay: 40
    fadeOut: 10

# Anti-farm settings (Chống farm)
anti-farm:
  enabled: true
  same-player-cooldown: 300
  repeat-kill-threshold: 3
  diminished-return-percent: 50

  # Ngăn nhận Elo khi chênh lệch Elo quá cao (Killer Elo - Victim Elo > 200)
  elo-difference:
    enabled: true
    max-difference: 200
 
  # IP / Subnet Check (Chống clone/alt accounts)
  ip-check:
    enabled: true
    prevent-same-ip: true
    prevent-same-subnet: true

  # Trạng thái hoạt động (AFK / Hoạt động / Spawn Camping Check)
  activity-check:
    enabled: true
    no-move-seconds: 15
    no-attack-seconds: 15
    spawn-camping:
      enabled: true
      protection-seconds: 10
      protection-radius: 15
      action: BLOCK # BLOCK (chặn Elo) hoặc DIMINISH (giảm 50% Elo)

# Elo decay settings (Hao hụt Elo cho người chơi không hoạt động)
# Chỉ áp dụng cho TOP 10 người chơi có Elo cao nhất
elo-decay:
  enabled: true
  
  # Khoảng thời gian chạy kiểm tra hao hụt Elo (Ví dụ: 30m, 1h, 12h, 24h, 1d, 7d)
  # Đơn vị hỗ trợ: m (phút), h (giờ), d (ngày)
  check-interval: "12h"
  
  # Thời gian không thay đổi Elo tối đa trước khi bị trừ Elo (Ví dụ: 12h, 24h, 1d, 3d)
  # inactive-threshold: "24h"
  
  # Số điểm Elo bị trừ mỗi lần
  decay-amount: 20

# Leaderboard settings
leaderboard:
  per-page: 10
  cache-refresh: 60


# Custom sounds & particle effects
effects:
  kill:
    sound: "ENTITY_EXPERIENCE_ORB_PICKUP"
    volume: 1.0
    pitch: 1.0
    particle: "VILLAGER_HAPPY"
    particle-count: 10
  death:
    sound: "ENTITY_PLAYER_HURT"
    volume: 1.0
    pitch: 0.8
    particle: "SMOKE"
    particle-count: 10
  plus:
    sound: "ENTITY_EXPERIENCE_ORB_PICKUP"
    volume: 1.0
    pitch: 1.0
    particle: "VILLAGER_HAPPY"
    particle-count: 10
  minus:
    sound: "ENTITY_PLAYER_HURT"
    volume: 1.0
    pitch: 0.8
    particle: "SMOKE"
    particle-count: 10
  rank-up:
    sound: "UI_TOAST_CHALLENGE_COMPLETE"
    volume: 1.0
    pitch: 1.0
    particle: "TOTEM_OF_UNDYING"
    particle-count: 30
  rank-down:
    sound: "ENTITY_WITHER_DEATH"
    volume: 0.5
    pitch: 0.8
    particle: "LARGE_SMOKE"
    particle-count: 15
  bonus:
    sound: "ENTITY_PLAYER_LEVELUP"
    volume: 1.0
    pitch: 1.2
    particle: "ENCHANTMENT_TABLE"
    particle-count: 20


# Elo Bounty System (Hệ thống Săn thưởng Elo)
bounty:
  enabled: true
  # Thưởng chuỗi hạ gục (Streak Bounties)
  streak-bounties:
    "5":
      reward-elo: 20
      commands:
        - "eco give {killer} 500"
    "10":
      reward-elo: 50
      commands:
        - "eco give {killer} 1000"
        - "give {killer} diamond 1"
  # Thưởng hạ gục TOP bảng xếp hạng (Rank Bounties)
  top-player-bounties:
    "1":
      reward-elo: 50
      commands:
        - "eco give {killer} 2000"
    "2":
      reward-elo: 30
      commands:
        - "eco give {killer} 1000"
    "3":
      reward-elo: 20
      commands:
        - "eco give {killer} 500"

config-version: "1.4.0"

```

---

### 📂 File cấu hình tích hợp Discord: `discord.yml`
Quản lý các thiết lập gửi thông báo trực tiếp lên kênh Discord thông qua Discord Webhooks. Hỗ trợ tùy chỉnh cấu trúc nội dung tin nhắn thường hoặc Embed chi tiết (thêm hình ảnh avatar, màu sắc tùy biến, tiêu đề, mô tả...).

```yaml
discord-webhook:
  enabled: false
  url: "https://discord.com/api/webhooks/..."
  events:
    kill:
      enabled: true
      use-embed: true
      format: "⚔️ **{killer}** ({killer_elo} Elo) has defeated **{victim}** ({victim_elo} Elo)!"
      embed:
        title: "⚔️ Player Defeated"
        description: "**{killer}** ({killer_elo} Elo) has defeated **{victim}** ({victim_elo} Elo)!"
        color: "#ff3c3c"
        footer: "SolarElo Webhook System"
        timestamp: true
        thumbnail: "https://minotar.net/avatar/{killer}/100.png"
    top-1-defeat:
      enabled: true
      use-embed: true
      format: "👑 **{killer}** has defeated the #1 ranked player **{victim}**!"
      embed:
        title: "👑 TOP 1 Defeated"
        description: "**{killer}** has defeated the #1 ranked player **{victim}**!"
        color: "#ffaa00"
        footer: "SolarElo Webhook System"
        timestamp: true
        thumbnail: "https://minotar.net/avatar/{killer}/100.png"
    rank-up:
      enabled: true
      use-embed: true
      format: "⚡ **{player}** has ranked up to **{rank}**!"
      embed:
        title: "⚡ Rank Up"
        description: "**{player}** has ranked up to **{rank}**!"
        color: "#00ffcc"
        footer: "SolarElo Webhook System"
        timestamp: true
        thumbnail: "https://minotar.net/avatar/{player}/100.png"
```

---

### 📂 File cấu hình Mùa giải: `season.yml`
Quản lý thiết lập mùa giải cho server. Hỗ trợ tự động chạy tác vụ khi đến ngày kết thúc cấu hình: thực hiện nén điểm Elo cũ (Soft-Reset) và thực thi chuỗi lệnh Console tự động để phát thưởng theo rank/thứ hạng tương ứng.

```yaml
season:
  # Thời gian kết thúc mùa giải (Định dạng: YYYY-MM-DD HH:mm:ss)
  end-date: "2026-07-01 00:00:00"

  # Soft-Reset settings
  soft-reset:
    enabled: true
    # Công thức: default-elo + (elo-hiện-tại - default-elo) * multiplier
    multiplier: 0.4
    # Reset stats khác (kills, deaths, streaks) về 0
    reset-stats: true
  
  # Auto rewards commands when ending season
  rewards:
    # Trao thưởng theo thứ hạng chính xác (TOP 1, TOP 2, TOP 3, v.v.)
    ranks:
      "1":
        - "broadcast #ffaa00★ MÙA GIẢI KẾT THÚC! #e0e0e0Người chơi #00ff3c{player} đạt TOP 1!"
      "2":
        - "broadcast #ffaa00★ MÙA GIẢI KẾT THÚC! #e0e0e0Người chơi #00ff3c{player} đạt TOP 2!"
    # Hoặc trao thưởng theo khoảng thứ hạng (ví dụ: TOP 4 đến 10)
    brackets:
      "4-10":
        - "give {player} gold_ingot 5"
```

---

### 📂 File cấu hình ngôn ngữ: `messages.yml`
Quản lý định dạng tin nhắn hiển thị trong game. Có thể tùy chỉnh màu sắc qua mã màu cũ (`&`) hoặc mã màu MiniMessage `<gradient>`.

```yaml
prefix: "<gradient:#ff8a00:#da1b60>SolarElo</gradient> #555555> #aaaaaa"

# ── PvP ───────────────────────────────
kill-gain: "#00ff3cKill #ffaa00{victim} #00ff3c+#ffaa00{gained} Elo #aaaaaa(Streak: #ff3c3c{streak}#aaaaaa) #555555| #ffffffTotal: #ffaa00{elo}"
death-loss: "#ff3c3cChết bởi #ffaa00{killer} #ff3c3c-#ffaa00{lost} Elo #555555| #ffffffTotal: #ffaa00{elo}"
streak-end: "#ff3c3cStreak #ffaa00{streak} #ff3c3ckill đã kết thúc!"
top-1-defeat: "#ffaa00[TOP 1 BONUS] Bạn đã nhận thêm #ffaa00{extra} Elo #ffaa00từ việc hạ gục người đứng đầu Server!"
# Định dạng chú thích khi di chuột vào tên người chơi trong chat (\n là xuống dòng)
hover-stats-format: "#ffaa00{player} #ffffffStats:\n#aaaaaaCấp bậc: &r{rank}\n#aaaaaaĐiểm Elo: #ffaa00{elo}\n#aaaaaaTỷ lệ K/D: #ffaa00{kd} #aaaaaa({kills}/{deaths})\n#aaaaaaChuỗi thắng: #ff3c3c{streak}"

# ── Anti-farm ─────────────────────────
anti-farm-cooldown: "#ff3c3cPhải chờ #ffaa00{seconds}s #ff3c3ctrước khi nhận Elo từ việc giết #ffaa00{player} #ff3c3clại."
anti-farm-diminished: "#ffaa00Elo giảm do farm #aaaaaa(giết lặp). Nhận được: #ffaa00{gained}"
anti-farm-ip: "#ff3c3cKhông thể nhận Elo do trùng IP hoặc cùng dải IP subnet."
anti-farm-afk: "#ff3c3cKhông thể nhận Elo do nạn nhân đang AFK hoặc không hoạt động."
anti-farm-spawn: "#ff3c3cKhông thể nhận Elo do nạn nhân vừa hồi sinh hoặc ở gần điểm spawn."
anti-farm-elo-difference: "#ff3c3cKhông thể nhận Elo do chênh lệch Elo giữa hai bên quá lớn ({difference} Elo)."

# ── Rank ──────────────────────────────
rank-up: "#ffaa00★ RANK UP ★ #ffaa00Bạn đã lên &r{rank}#ffaa00!"
rank-down: "#ff3c3c▼ RANK DOWN ▼ #ffaa00Bạn xuống &r{rank}#ffaa00."

# ── Commands ──────────────────────────
elo-info: "#ffaa00{player} #555555| #ffffffElo: #ffaa00{elo} #ffffff| Rank: {rank} #ffffff| K: #00ff3c{kills} #ffffff| D: #ff3c3c{deaths} #ffffff| K/D: #ffaa00{kd} #ffffff| Streak: #ff3c3c{streak}"
elo-info-own: "#ffaa00Stats của bạn #555555| #ffffffElo: #ffaa00{elo} #ffffff| Rank: {rank} #ffffff| K: #00ff3c{kills} #ffffff| D: #ff3c3c{deaths} #ffffff| K/D: #ffaa00{kd} #ffffff| Streak: #ff3c3c{streak}"

# ── Leaderboard ───────────────────────
top-header: "#555555&m         &r #ffaa00SolarElo #555555- #aaaaaaTop Players &r#555555&m         "
top-entry: "#555555{pos}. &r{rank} #ffffff{player} #555555- #ffaa00Elo: #ffffff{elo} #555555| Streak: #ff3c3c{streak}"
top-footer: "#555555&m         &r #aaaaaaTrang #ffaa00{page}#aaaaaa/#ffaa00{max_pages} &r#555555&m         "

# ── Leaderboard Details ───────────────
gui-detail-header: "#555555&m         &r #ffaa00Thống kê chi tiết &r#555555&m         "
gui-detail-format: "#aaaaaaNgười chơi: #ffaa00{player}\n#aaaaaaCấp bậc: &r{rank}\n#aaaaaaĐiểm Elo: #ffaa00{elo}\n#aaaaaaMạng giết: #00ff3c{kills}\n#aaaaaaSố lần chết: #ff3c3c{deaths}\n#aaaaaaTỷ lệ K/D: #ffaa00{kd}\n#aaaaaaChuỗi thắng: #ff3c3c{streak}\n#aaaaaaChuỗi thắng lớn nhất: #ffaa00{best_streak}"
gui-detail-footer: "#555555&m                                           "

# ── Admin ─────────────────────────────
admin-set: "&#00ff3cᴇʟᴏ ᴀᴅᴍɪɴ &8» &fĐã đặt ELO của &#ffffff{player} &fthành &#00ff3c{elo} ELO&f."
admin-add: "&#00ff3cᴇʟᴏ ᴀᴅᴍɪɴ &8» &fĐã cộng &#00ff3c+{amount} ELO &fcho &#ffffff{player}"
admin-remove: "&#00ff3cᴇʟᴏ ᴀᴅᴍɪɴ &8» &fĐã trừ &#ff3c3c-{amount} ELO &fcủa &#ffffff{player}"
admin-reset: "&#00ff3cᴇʟᴏ ᴀᴅᴍɪɴ &8» &fĐã reset ELO & Stats của &#ffffff{player} &fvề mặc định."

# ── Misc ──────────────────────────────
reload: "&#00ff3cᴇʟᴏ ᴀᴅᴍɪɴ &8» &fReload cấu hình plugin thành công!"
no-permission: "&#ff3c3cᴇʟᴏ ᴀᴅᴍɪɴ &8» &cBạn không có quyền thực hiện thao tác này."
player-not-found: "&#ff3c3cᴇʟᴏ ᴀᴅᴍɪɴ &8» &cKhông tìm thấy người chơi này."
season-resetting: "&#ffaa00ᴇʟᴏ ᴀᴅᴍɪɴ &8» &fĐang tiến hành kết thúc mùa giải và trao thưởng..."
season-reset-success: "&#00ff3cᴇʟᴏ ᴀᴅᴍɪɴ &8» &fMùa giải đã được reset thành công! Đã trao thưởng cho các người chơi hàng đầu và thực hiện soft-reset ELO."
elo-decay-notice: "#ff3c3cBạn bị trừ {amount} Elo do không hoạt động tích cực gần đây!"

# ── Bounty ────────────────────────────
bounty-broadcast-streak: "#ffaa00[SĂN THƯỞNG] #ffaa00{player} #ffffffđã đạt chuỗi #ff3c3c{streak} kills#ffffff! Treo thưởng: #00ff3c+{elo} Elo #ffffff+ phần quà!"
bounty-claim-streak: "#ffaa00[SĂN THƯỞNG] #ffaa00{killer} #ffffffđã chấm dứt chuỗi #ff3c3c{streak} #ffffffcủa #ffaa00{victim} #ffffffvà nhận thưởng (#00ff3c+{elo} Elo#ffffff)!"
bounty-claim-top: "#ffaa00[SĂN THƯỞNG] #ffaa00{killer} #ffffffđã tiêu diệt người chơi TOP #ffaa00{rank} #ffaa00({victim}) #ffffffvà nhận thưởng (#00ff3c+{elo} Elo#ffffff)!"
welcome-top-10: "#ffaa00[Chào Mừng] Huyền thoại #ffffff{player} #aaaaaa(Hạng #{rank} | {elo} Elo) #ffaa00đã tham gia máy chủ!"
welcome-top-3: "#ffaa00[Chào Mừng] Huyền thoại #ffffff{player} #aaaaaa(Hạng #{rank} | {elo} Elo) #ffaa00đã tham gia máy chủ!"
bounty-targeted: "#ffaa00[Nhiệm Vụ] Một thợ săn đã nhận khế ước truy nã bạn!"

# ── Elo Lock ──────────────────────────
elo-locked-by-admin: "&#ff3c3cElo của bạn đã bị khóa bởi Admin!"
elo-unlocked-by-admin: "&#00ff3cElo của bạn đã được mở khóa bởi Admin!"
admin-lock-success: "&#00ff3cᴇʟᴏ ᴀᴅᴍɪɴ &8» &fĐã khóa Elo của player &#ffffff{player} &fthành công."
admin-unlock-success: "&#00ff3cᴇʟᴏ ᴀᴅᴍɪɴ &8» &fĐã mở khóa Elo của player &#ffffff{player} &fthành công."
elo-locked-auto: "&#ff3c3cElo của bạn đã bị tự động khóa do đạt mức tối thiểu!"
bounty-locked-error: "&#ff3c3cElo của bạn đang bị khóa, không thể sử dụng tính năng săn thưởng!"

config-version: "1.4.0"
```


---

### 📂 File cấu hình cấp bậc: `rank.yml`
Quản lý cấu hình thứ hạng của người chơi dựa trên điểm Elo hiện có. Cấp bậc được sắp xếp tự động từ thấp đến cao dựa theo mốc điểm `min-elo`.

```yaml
ranks:
  lt5:
    display: "&7LT5"
    min-elo: 0
    prefix: "&7[LT5]"
    rankup-commands: []

  ht5:
    display: "&7HT5"
    min-elo: 1000
    prefix: "&7[HT5]"
    rankup-commands: []

  lt4:
    display: "&aLT4"
    min-elo: 1150
    prefix: "&a[LT4]"
    rankup-commands:
      - "broadcast &e{player} &ađã đột phá lên Rank &aLT4!"

  ht4:
    display: "&aHT4"
    min-elo: 1300
    prefix: "&a[HT4]"
    rankup-commands: []

  # Có thể thêm vô số hạng khác theo logic tăng dần điểm min-elo...
```

---

### 📂 File cấu hình nhiệm vụ: `gui/bounty.yml`
Quản lý giao diện nhận nhiệm vụ săn thưởng tiêu diệt người chơi khác.

```yaml
title: "#ff3c3cBounty Quests (Săn Tiền Thưởng)"
rows: 6

filler:
  enabled: true
  material: "GRAY_STAINED_GLASS_PANE"

reward-elo: 20
cooldown-seconds: 5400
cancel-cooldown-seconds: 300
commands:
  - "give {killer} diamond 2"
  - "broadcast #555555[#ffaa00SolarElo#555555] #00ff3c{killer} #ffffffđã hoàn thành nhiệm vụ và tiêu diệt #ff3c3c{victim}#ffffff!"

target-player-head:
  name: "#ff3c3c[Mục Tiêu] #ffffff{player}"
  lore:
    - "#aaaaaaElo: #ffaa00{elo}"
    - "#aaaaaaRank: &r{rank}"
    - ""
    - "#ffaa00Nhấp để nhận nhiệm vụ tiêu diệt mục tiêu này!"
    - "#ffaa00Thành công: #00ff3c+{reward_elo} Elo #ffffff+ phần thưởng."

active-bounty-item:
  name: "#00ff3c[Nhiệm Vụ Hoạt Động]"
  material: "PLAYER_HEAD"
  slot: 13
  lore:
    - "#aaaaaaMục tiêu: #ff3c3c{target}"
    - "#aaaaaaThưởng Elo: #00ff3c+{reward_elo}"
    - ""
    - "#ffffffHãy tìm và tiêu diệt #ffaa00{target} #ffffffđể nhận thưởng!"

cancel-bounty-item:
  name: "#ff3c3c[Hủy Nhiệm Vụ Hiện Tại]"
  material: "BARRIER"
  slot: 22
  lore:
    - "#ffffffNhấp để huỷ bỏ hợp đồng săn thưởng hiện tại."
    - "#ffffffBạn sẽ bị phạt #ff3c3c{cooldown} giây #ffffffchờ trước"
    - "#ffffffkhi có thể nhận nhiệm vụ mới."
  confirm_sound: error

cooldown-item:
  name: "#ff3c3c[Thời Gian Chờ Cooldown]"
  material: "CLOCK"
  slot: 22
  lore:
    - "#ffffffBạn đã hoàn thành hoặc hủy bỏ nhiệm vụ gần đây."
    - "#ffffffVui lòng chờ để nhận nhiệm vụ tiếp theo."
    - ""
    - "#ffffffThời gian còn lại: #ff3c3c{remaining} giây."

no-targets-item:
  name: "#ff3c3c[Không Có Mục Tiêu Hợp Lệ]"
  material: "BARRIER"
  slot: 22
  lore:
    - "#ffffffHiện tại không có người chơi online hợp lệ khác."
    - "#ffffff(Bạn không thể tự săn thưởng chính mình)"

config-version: "1.1.0"
```

---

### 📂 File cấu hình Menu Chính: `gui/main.yml`
Quản lý giao diện chính (Menu Tổng) hiển thị khi gõ lệnh `/elo`.

```yaml
# Elo Main Menu GUI Configuration (Menu Chính SolarElo)
title: "ᴇʟᴏ ᴍᴇɴᴜ"
rows: 3

filler:
  enabled: false
  material: "GRAY_STAINED_GLASS_PANE"

bounty-item:
  enabled: true
  name: "#ff3c3c⚔ ʙᴏᴜɴᴛʏ ⚔"
  material: "DIAMOND_SWORD"
  slot: 10
  lore:
    - "&fClick to view the Bounty list"
    - "&fand accept contracts to eliminate other players."
    - ""
    - "#ff3c3cClick to open!"
  customModelData: -1
  confirm_sound: click

leaderboard-item:
  enabled: true
  name: "#00BFFF★ ʟᴇᴀᴅᴇʀʙᴏᴀʀᴅ ★"
  material: "BLUE_BANNER"
  slot: 12
  lore:
    - "&fClick to view the ELO Leaderboard"
    - "&fand other players' statistics."
    - ""
    - "#00BFFFClick to open!"
  customModelData: -1
  confirm_sound: click

rewards-item:
  enabled: true
  name: "#00ff3c✪ ʀᴇᴡᴀʀᴅs ✪"
  material: "CHEST"
  slot: 14
  lore:
    - "&fClick to view rank breakthrough rewards"
    - "&fearned by reaching ELO milestones."
    - ""
    - "#00ff3cClick to open!"
  customModelData: -1
  confirm_sound: xp

settings-item:
  enabled: true
  name: "#ffaa00⚙ sᴇᴛᴛɪɴɢs ⚙"
  material: "COMPARATOR"
  slot: 16
  lore:
    - "&fClick to customize notifications,"
    - "&fwelcome effects, and titles."
    - ""
    - "#ffaa00Click to open!"
  customModelData: -1
  confirm_sound: click

ip-blocked-item:
  material: "RED_BANNER"
  slot: 13
  name: "#ff3c3c⚠ ᴀᴄᴄᴇꜱꜱ ᴅᴇɴɪᴇᴅ ⚠"
  lore:
    - "#aaaaaaDetected multiple accounts on the same IP!"
    - "#aaaaaaGUI access has been disabled."
    - ""
    - "#ff3c3cPlease disconnect other accounts to unlock."
  customModelData: -1

config-version: "1.2.0"
```

---

### 📂 File cấu hình xác nhận nhận nhiệm vụ: `gui/confirmation.yml`
Quản lý giao diện xác nhận trước khi người chơi chính thức nhận một hợp đồng săn thưởng.

```yaml
title: "#ff3c3c{target}"
rows: 3

filler:
  enabled: true
  material: "GRAY_STAINED_GLASS_PANE"

target-slot: 13

confirm-item:
  name: "#00ff3cᴄᴏɴꜰɪʀᴍ"
  material: "LIME_STAINED_GLASS_PANE"
  slots: [10, 11, 12]
  lore:
    - "#ffffffHãy chắc chắn bạn muốn nhận hợp đồng này."
    - ""
    - "#ffffffMục tiêu: #ff3c3c{target}"
    - "#ffffffNhấp vào đây để xác nhận."

cancel-item:
  name: "#ff3c3cɢᴏ ʙᴀᴄᴋ"
  material: "RED_STAINED_GLASS_PANE"
  slots: [14, 15, 16]
  lore:
    - "#ffffffHủy bỏ và quay lại"
    - "#ffffffdanh sách mục tiêu."

config-version: "1.0.0"
```

### 📂 File cấu hình Menu Cài Đặt: `gui/settings.yml`
Quản lý giao diện cài đặt cá nhân nơi người chơi có thể tự do bật/tắt các thông báo chat, hiệu ứng âm thanh chào mừng và hiển thị Title trên màn hình của họ.

```yaml
# Elo Settings Menu GUI Configuration (Menu Cài Đặt SolarElo)
title: "sᴇᴛᴛɪɴɢs"
rows: 3

filler:
  enabled: false
  material: "GRAY_STAINED_GLASS_PANE"

items:
  chat-notification:
    name: "#00BFFFᴄʜᴀᴛ ɴᴏᴛɪꜰɪᴄᴀᴛɪᴏɴ"
    material: "BOOK"
    slot: 11
    lore_on:
      - "&a✔ Enabled"
      - "&7Click to disable."
    lore_off:
      - "&c❌ Disabled"
      - "&7Click to enable."
    confirm_sound: click

  title-notification:
    name: "#ff3c3cᴛɪᴛʟᴇ ɴᴏᴛɪꜰɪᴄᴀᴛɪᴏɴ"
    material: "COMPASS"
    slot: 15
    lore_on:
      - "&a✔ Enabled"
      - "&7Click to disable."
    lore_off:
      - "&c❌ Disabled"
      - "&7Click to enable."
    confirm_sound: click

back-button:
  name: "#ff3c3cʙᴀᴄᴋ"
  material: "ARROW"
  slot: 22
  lore:
    - "&fClick to return to the main menu"
  confirm_sound: click
```

### 📂 File cấu hình Admin GUI: `gui/admin.yml`
Quản lý các giao diện dành riêng cho Admin để cộng, trừ, đặt điểm ELO, reset chỉ số người chơi, và xem lịch sử ELO/PvP của họ.

```yaml
admin-list:
  title: "ᴇʟᴏ ᴀᴅᴍɪɴ - {page}"

admin-detail:
  title: "ᴀᴅᴍɪɴ ᴅᴇᴛᴀɪʟs - {player}"

elo-history:
  title: "ᴇʟᴏ ʜɪsᴛᴏʀʏ - {player}"

kill-history:
  title: "ᴘᴠᴘ ʜɪsᴛᴏʀʏ - {player}"

# Navigation Buttons (Các nút điều hướng)
back:
  name: "#00BFFFʙᴀᴄᴋ"
  lore:
    - "&fClick to go to the previous page"
  material: "ARROW"
  slot: 45
  customModelData: -1

next:
  name: "#00BFFFɴᴇxᴛ"
  lore:
    - "&fClick to go to the next page"
  material: "ARROW"
  slot: 53
  customModelData: -1

refresh:
  name: "#00ff3cʀᴇꜰʀᴇsʜ"
  lore:
    - "&fClick to refresh the list"
  material: "FEATHER"
  slot: 49
  customModelData: -1

filter:
  name: "#00BFFFғɪʟᴛᴇʀ"
  lore:
    - "&fClick to toggle filter"
  material: "HOPPER"
  slot: 49
  customModelData: -1

# Actions (Các nút thao tác chi tiết)
add-elo:
  name: "#00ff3cᴀᴅᴅ ᴇʟᴏ"
  lore:
    - "&fClick to add ELO to this player"
  material: "EMERALD"
  slot: 28
  customModelData: -1

set-elo:
  name: "#ffaa00sᴇᴛ ᴇʟᴏ"
  lore:
    - "&fClick to set player's ELO"
  material: "NAME_TAG"
  slot: 29
  customModelData: -1

remove-elo:
  name: "#ff3c3cʀᴇᴍᴏᴠᴇ ᴇʟᴏ"
  lore:
    - "&fClick to deduct ELO from player"
  material: "REDSTONE"
  slot: 30
  customModelData: -1

back-to-list:
  name: "#aaaaaaʙᴀᴄᴋ"
  lore:
    - "&fClick to return to player list"
  material: "ARROW"
  slot: 31
  customModelData: -1

elo-history:
  name: "#00BFFFᴇʟᴏ ʜɪsᴛᴏʀʏ"
  lore:
    - "&fClick to view ELO change history"
  material: "BOOK"
  slot: 32
  customModelData: -1

pvp-history:
  name: "#00BFFFᴘᴠᴘ ʜɪsᴛᴏʀʏ"
  lore:
    - "&fClick to view PvP logs"
  material: "DIAMOND_SWORD"
  slot: 33
  customModelData: -1

reset-stats:
  name: "#ff3c3cʀᴇsᴇᴛ sᴛᴀᴛs"
  lore:
    - "&fClick to reset ELO and stats to default"
  material: "GUNPOWDER"
  slot: 34
  customModelData: -1

# ELO Change Reasons (Lý do thay đổi Elo hiển thị)
reasons:
  kill: "⚔ Hạ gục {player}"
  death: "☠ Bị hạ gục bởi {player}"
  admin-set: "⚙ Thay đổi bởi Admin ({elo} Elo)"
  admin-add: "✚ Cộng bởi Admin (+{amount} Elo)"
  admin-remove: "➖ Trừ bởi Admin (-{amount} Elo)"
  admin-reset: "⟳ Reset bởi Admin"
```

---

### 📂 File cấu hình Database: `database.yml`
Tách riêng hoàn toàn khỏi `config.yml` từ phiên bản 1.5. Quản lý kết nối CSDL.

```yaml
database:
  type: SQLITE   # SQLITE, MYSQL, or MARIADB
  mysql:
    host: localhost
    port: 3306
    database: solarelo
    username: root
    password: password
    pool-size: 10   # Số kết nối tối đa trong connection pool (HikariCP)

config-version: "1.5"
```

> **Lưu ý**: `MARIADB` là lựa chọn thứ ba ngoài `SQLITE` và `MYSQL`. Plugin sử dụng HikariCP cho connection pooling hiệu năng cao.

---

### 📂 File cấu hình Hiệu ứng: `effects.yml`
Tách riêng từ `config.yml` từ phiên bản 1.5. Quản lý toàn bộ âm thanh GUI và hiệu ứng Particle/Sound cho các sự kiện Elo.

```yaml
# ── GUI Sounds ────────────────────────────────────────────────────────
click:           # Âm thanh khi click nút GUI
  enabled: true
  sound: UI_BUTTON_CLICK
  volume: 1.0
  pitch: 1.0

xp:              # Âm thanh khi mở GUI phần thưởng
  enabled: true
  sound: ENTITY_EXPERIENCE_ORB_PICKUP
  volume: 1.0
  pitch: 1.0

error:           # Âm thanh khi có lỗi (ví dụ: hủy nhiệm vụ)
  enabled: true
  sound: ENTITY_VILLAGER_NO
  volume: 1.0
  pitch: 1.0

# ── ELO & Rank Effects (Sounds & Particles) ──────────────────────────
effects:
  kill:          # Khi hạ gục đối thủ và nhận Elo
    sound: "ENTITY_EXPERIENCE_ORB_PICKUP"
    volume: 1.0
    pitch: 1.0
    particle: "VILLAGER_HAPPY"
    particle-count: 10
  death:         # Khi bị hạ gục và mất Elo
    sound: "ENTITY_PLAYER_HURT"
    volume: 1.0
    pitch: 0.8
    particle: "SMOKE"
    particle-count: 10
  plus:          # Khi được cộng Elo từ admin
    sound: "ENTITY_EXPERIENCE_ORB_PICKUP"
    volume: 1.0
    pitch: 1.0
    particle: "VILLAGER_HAPPY"
    particle-count: 10
  minus:         # Khi bị trừ Elo từ admin
    sound: "ENTITY_PLAYER_HURT"
    volume: 1.0
    pitch: 0.8
    particle: "SMOKE"
    particle-count: 10
  rank-up:       # Khi lên hạng
    sound: "UI_TOAST_CHALLENGE_COMPLETE"
    volume: 1.0
    pitch: 1.0
    particle: "TOTEM_OF_UNDYING"
    particle-count: 30
  rank-down:     # Khi xuống hạng
    sound: "ENTITY_WITHER_DEATH"
    volume: 0.5
    pitch: 0.8
    particle: "LARGE_SMOKE"
    particle-count: 15
  bonus:         # Khi nhận thưởng Elo (bounty, TOP 1, v.v.)
    sound: "ENTITY_PLAYER_LEVELUP"
    volume: 1.0
    pitch: 1.2
    particle: "ENCHANTMENT_TABLE"
    particle-count: 20

config-version: "1.5"
```

---

### 📂 File cấu hình GUI Bảng xếp hạng: `gui/leaderboard.yml`

```yaml
enabled: true
title: "ʟᴇᴀᴅᴇʀʙᴏᴀʀᴅ - ᴘᴀɢᴇ {page}"

# Bố cục GUI (x = slot player head, b = back, n = next, r = refresh, f = filter, s = self-head)
gui-disposition:
  - "xxxxxxxxx"
  - "xxxxxxxxx"
  - "xxxxxxxxx"
  - "xxxxxxxxx"
  - "xxxxxxxxx"
  - "b##srf##n"

# Màu sắc số thứ hạng
rank-colors:
  top-5:   "#ffaa00"   # Vàng — TOP 5
  top-10:  "#55ffff"   # Xanh lam — TOP 10
  top-50:  "#00ff3c"   # Xanh lá — TOP 50
  top-100: "#aaaaaa"   # Xám — TOP 100
  default: "#ffffff"   # Trắng — ngoài TOP 100

# Đầu người chơi trong bảng xếp hạng
player-head:
  name: "{pos_color}#{pos} #ffffff{player}"
  lore:
    - "#aaaaaaRank: &r{rank}"
    - "#aaaaaaElo Points: #ffaa00{elo}"
    - ""
    - "#ffaa00Click to view detailed statistics!"

# Đầu người chơi tự xem chính mình (góc dưới)
self-player-head:
  enabled: true
  name: "#00BFFFsᴇʟꜰ #ffffff{player}"
  lore:
    - "&fCurrent Elo: #ffaa00{elo}"
    - "&fCurrent Rank: {pos_color}#{pos}"
    - "&fNext Rank: #55ffff{next_rank}"
    - "&fElo Needed: #ff3c3c{elo_needed}"
    - "&fSeason Ends: #00ff3c{season_time}"

# Bộ lọc (Filter): HIGH_TO_LOW | LOW_TO_HIGH | ONLINE_ONLY
filter:
  options:
    - "HIGH_TO_LOW"
    - "LOW_TO_HIGH"
    - "ONLINE_ONLY"
  option-names:
    HIGH_TO_LOW:  "High to Low (Top)"
    LOW_TO_HIGH:  "Low to High (Bottom)"
    ONLINE_ONLY:  "Online Only (Top ELO)"

config-version: "1.5"
```

---

### 📂 File cấu hình GUI Thống kê chi tiết: `gui/stats.yml`
Hiển thị khi click vào đầu người chơi trong bảng xếp hạng.

```yaml
enabled: true
title: "sᴛᴀᴛs - {player}"

# Bố cục GUI (h = head, r = rank, e = elo, k = kills, d = deaths, q = kd, s = streak, b = best_streak, a = back)
gui-disposition:
  - "####h####"
  - "#rekdqsb#"
  - "####a####"

# Các item thống kê (tùy chỉnh material, name, lore)
items:
  player-head:  { material: "PLAYER_HEAD"     }
  rank:         { material: "EMERALD"         }
  elo:          { material: "AMETHYST_SHARD"  }
  kills:        { material: "DIAMOND_SWORD"   }
  deaths:       { material: "SKELETON_SKULL"  }
  kd:           { material: "CLOCK"           }
  streak:       { material: "ZOMBIE_HEAD"     }
  best_streak:  { material: "GOLD_NUGGET"     }

config-version: "1.5"
```

---

### 📂 File cấu hình GUI Phần thưởng Rank: `gui/rewards.yml`
Hiển thị khi người chơi click nút **Rewards** trong Menu Chính.

```yaml
enabled: true
title: "ʀᴀɴᴋ ʀᴇᴡᴀʀᴅs"

# Bố cục GUI (x = rank item, a = back)
gui-disposition:
  - "#########"
  - "#xxxxxxx#"
  - "#xxxxxxx#"
  - "#xxxxxxx#"
  - "####a####"

rank-item:
  name: "{rank}"
  lore:
    - "#aaaaaaRequired Elo: #ffaa00{min_elo}"
    - "#aaaaaaPrefix: &r{prefix}"
    - ""
    - "#ffaa00Breakthrough rewards:"
    - "{rewards}"
    - ""
    - "{status}"
  reward-line-format: " #00ff3c• #ffffff{command}"
  no-rewards-format:  " #ff3c3c• No automatic rewards"
  status-unlocked: "#00ff3c✔ Unlocked"
  status-locked:   "#ff3c3c🔒 Locked (Needs {missing} more Elo)"

config-version: "1.5"
```

---

### 📂 File cấu hình GUI Hợp đồng đang nhận: `gui/active_quest.yml`
Hiển thị chi tiết nhiệm vụ bounty đang nhận khi click nút **Active Quest**.

```yaml
title: "ᴀᴄᴛɪᴠᴇ ᴄᴏɴᴛʀᴀᴄᴛ"

# Bố cục GUI (q = quest info, n = active-quest view, c = cancel, a = back)
gui-disposition:
  - "#########"
  - "##q#n#c##"
  - "####a####"

# Item thông tin mục tiêu đang bị săn
active-item:
  material: "PLAYER_HEAD"
  name: "#ff3c3cᴛᴀʀɢᴇᴛ #ffffff{target}"
  lore:
    - "#aaaaaaElo: #ffaa00{elo}"
    - "#aaaaaaRank: &r{rank}"
    - "#aaaaaaElo Reward: #00ff3c+{reward_elo}"
    - "#aaaaaaTime Remaining: #ffaa00{time_remaining}"
    - ""
    - "#ffffffFind and eliminate #ffaa00{target} #ffffffto get rewards!"
    - "#ffffffIf they log out, the contract is preserved."

config-version: "1.5"
```

---

### 📂 File cấu hình GUI Săn thưởng: `gui/bounty.yml`

```yaml
enabled: true
title: "ʙᴏᴜɴᴛʏ ǫᴜᴇsᴛs"

# Bố cục GUI (x = player head mục tiêu, b = back, n = next, a = active-quest, r = refresh, f = filter)
gui-disposition:
  - "xxxxxxxxx"
  - "xxxxxxxxx"
  - "xxxxxxxxx"
  - "xxxxxxxxx"
  - "xxxxxxxxx"
  - "b##arf##n"

# Elo tối thiểu để HIỆN trong danh sách mục tiêu
minimum-target-elo: 5000

# Elo tối thiểu để MỞ KHÓA tính năng Bounty
minimum-unlock-elo: 1200

# Hiển thị khi tính năng bị khóa (chưa đủ Elo)
locked-item:
  name: "#ff3c3cʙᴏᴜɴᴛʏ ʟᴏᴄᴋᴇᴅ"
  lore:
    - "#ffffffYou need at least #ffaa00{required} Elo #ffffffto unlock"
    - "#ffffffthe Bounty feature."
    - ""
    - "#aaaaaaYour current Elo: #ff3c3c{elo} Elo"

config-version: "1.5"
```

---

### 📂 File cấu hình Hệ thống Bounty: `features/bounty.yml`

```yaml
bounty:
  enabled: true
  # Thưởng khi hạ gục người đang có chuỗi thắng (Streak Bounties)
  streak-bounties:
    "5":                        # Mốc streak để bị treo thưởng
      reward-elo: 20
      commands:
        - "eco give {killer} 500"
    "10":
      reward-elo: 50
      commands:
        - "eco give {killer} 1000"
        - "give {killer} diamond 1"
  # Thưởng khi hạ gục người đứng TOP (Top Player Bounties)
  top-player-bounties:
    "1":                        # TOP 1
      reward-elo: 50
      commands:
        - "eco give {killer} 2000"
    "2":
      reward-elo: 30
      commands:
        - "eco give {killer} 1000"
    "3":
      reward-elo: 20
      commands:
        - "eco give {killer} 500"

# Cài đặt nhiệm vụ /bounty (Accepted Contracts)
bounty-quest:
  reward-elo: 20
  contract-duration-seconds: 5400   # Thời gian hợp đồng còn hiệu lực (1h30)
  cooldown-seconds: 5400             # Cooldown sau khi hoàn thành (1h30)
  cancel-cooldown-seconds: 300       # Cooldown khi hủy nhiệm vụ (5 phút)
  commands:
    - "give {killer} diamond 2"

config-version: "1.5"
```

---

### 📂 File cấu hình Mùa giải: `features/season.yml`

```yaml
season:
  enabled: true
  # Thời gian kết thúc mùa giải (Định dạng: YYYY-MM-DD HH:mm:ss)
  end-date: "2026-07-01 00:00:00"

  soft-reset:
    enabled: true
    # Công thức: default-elo + (elo-hiện-tại - default-elo) * multiplier
    multiplier: 0.4
    reset-stats: true   # Reset kills, deaths, streak về 0

  rewards:
    # Trao thưởng theo thứ hạng chính xác
    ranks:
      "1":
        - "give {player} diamond_block 1"
      "2":
        - "give {player} emerald_block 1"
      "3":
        - "give {player} iron_block 1"
    # Trao thưởng theo khoảng thứ hạng
    brackets:
      "4-10":
        - "give {player} gold_ingot 5"

config-version: "1.5"
```

---

## 💻 4b. Hệ thống GUI Layout (gui-disposition)

Tất cả GUI trong SolarElo đều hỗ trợ tùy chỉnh bố cục bằng khóa `gui-disposition`. Đây là một mảng chuỗi ký tự, mỗi ký tự tương ứng một loại item ở slot đó.

### Ký tự quy ước

| Ký tự | Ý nghĩa | GUI áp dụng |
| :---: | :--- | :--- |
| `x` | Slot player head / item danh sách | Leaderboard, Admin List, Bounty, History |
| `#` | Ô trống hoặc filler glass pane | Tất cả |
| `b` | Nút BACK (trang trước) | Leaderboard, Admin, Bounty, History |
| `n` | Nút NEXT (trang sau) | Leaderboard, Admin, Bounty, History |
| `r` | Nút REFRESH | Admin List, Bounty |
| `f` | Nút FILTER | Leaderboard, Bounty, Kill History |
| `s` | Self player head | Leaderboard |
| `h` | Player head chính (chi tiết) | Admin Detail, Stats |
| `a` | Nút ADD Elo / nút BACK về Main | Admin Detail, Stats, Rewards |
| `e` | Nút ELO History / Elo stats item | Admin Detail, Stats |
| `p` | Nút PvP History | Admin Detail |
| `q` | Nút SEARCH / Quest info | Admin List, Active Quest |
| `k` | Kills stats item | Stats |
| `d` | Deaths stats item | Stats |
| `s` | Set Elo / Streak item | Admin Detail, Stats |
| `r` | Rank item / Remove Elo | Rewards, Admin Detail |
| `b` | Best Streak item | Stats |
| `x` | Reset Stats (admin detail) | Admin Detail |

### Ví dụ tùy chỉnh

```yaml
# Admin List 6 hàng — hàng cuối: Back | [trống] | Refresh | [trống] | Search | [trống] | Next
admin-list:
  gui-disposition:
    - "xxxxxxxxx"
    - "xxxxxxxxx"
    - "xxxxxxxxx"
    - "xxxxxxxxx"
    - "xxxxxxxxx"
    - "b###r###n"    # ← hàng điều hướng

# Thêm nút Search (ký tự 'q') vào vị trí giữa
    - "b##qr##n"
```

> **Lưu ý**: Số hàng trong `gui-disposition` quyết định kích thước inventory (1–6 hàng). Mỗi hàng phải đúng 9 ký tự.

---

## 🔌 9. Developer API

SolarElo cung cấp API công khai cho developer tích hợp plugin ngoài.

### 9.1 Dependency (Maven / Gradle)

Thêm module `solarelo-api` vào dự án của bạn:

```groovy
// build.gradle
repositories {
    maven { url = 'https://jitpack.io' }
}

dependencies {
    compileOnly 'com.github.YourUser:SolarElo:1.1-R06-FIX:api'
}
```

### 9.2 Khởi tạo

```java
import dev.solar.solarelo.api.SolarEloAPI;
import dev.solar.solarelo.api.SolarEloProvider;

// Trong onEnable() của plugin bạn:
SolarEloAPI api = SolarEloProvider.getAPI();
```

### 9.3 Các phương thức API

```java
// ── Lấy dữ liệu (async) ──────────────────────────────
CompletableFuture<Integer> future = api.getElo(uuid);
future.thenAccept(elo -> /* xử lý */);

CompletableFuture<Boolean> locked = api.isLocked(uuid);

// ── Lấy dữ liệu (sync — chỉ dùng trên main thread) ──
int elo    = api.getEloSync(uuid);
boolean lo = api.isLockedSync(uuid);

// ── Thao tác Elo ──────────────────────────────────────
api.setElo(uuid, 1500);      // Đặt Elo
api.addElo(uuid, 100);       // Cộng Elo
api.removeElo(uuid, 50);     // Trừ Elo

// ── Rank ──────────────────────────────────────────────
String rankName   = api.getRankName(uuid);    // e.g. "&aLT4"
String rankPrefix = api.getRankPrefix(uuid);  // e.g. "&a[LT4]"

// ── Thống kê ──────────────────────────────────────────
int kills   = api.getKills(uuid);
int deaths  = api.getDeaths(uuid);
int streak  = api.getCurrentStreak(uuid);
int best    = api.getBestStreak(uuid);

// ── Khóa / Mở khóa ────────────────────────────────────
api.setLocked(uuid, true);   // Khóa Elo
api.setLocked(uuid, false);  // Mở khóa Elo

// ── Reset ──────────────────────────────────────────────
api.resetStats(uuid);        // Reset toàn bộ Elo & Stats
```

### 9.4 Lưu ý quan trọng

> **Thread safety**: Các phương thức `getElo()`, `isLocked()` trả về `CompletableFuture` và an toàn để gọi từ bất kỳ thread nào. Các phương thức `*Sync` chỉ nên gọi trên **main thread** hoặc sau khi đã cache dữ liệu.
>
> **Cache**: SolarElo dùng in-memory cache cho player đang online. Dữ liệu offline player sẽ được load từ database không đồng bộ.

---

## 🛠️ 5. Hướng dẫn thiết lập & Cài đặt

1.  **Cài đặt plugin**: Sao chép file `SolarElo-obfuscated.jar` vào thư mục `plugins/` của máy chủ.
2.  **Khởi chạy máy chủ**: Để plugin tự động giải nén các file cấu hình mặc định vào thư mục `plugins/SolarElo/`.
3.  **Tích hợp PlaceholderAPI**:
    *   Hãy cài đặt [PlaceholderAPI](https://extendedclip.com/placeholderapi/) để sử dụng được các placeholder của SolarElo trên Scoreboard, Tablist hay các plugin Chat khác.
4.  **Cấu hình Discord Webhook**:
    *   Tại máy chủ Discord của bạn, vào **Server Settings** -> **Integrations** -> **Webhooks** -> **New Webhook**.
    *   Sao chép liên kết Webhook vừa tạo dán vào trường `url` dưới phần `discord-webhook` ở `discord.yml`.
    *   Bật `enabled: true` và chọn các event bạn muốn đồng bộ.
5.  **Cơ chế chẩn đoán lỗi bằng Console Log**:
    *   Nếu trong quá trình thử nghiệm PvP bạn thấy Elo không tăng hoặc giảm, hãy kiểm tra màn hình console. SolarElo sẽ xuất chi tiết lý do chặn Elo bằng Log như:
        *   `[Anti-Farm] Bo qua cong/tru Elo cho Killer va Victim. Ly do: BLOCKED_IP` (Do bạn đang chạy thử hai nick chung máy tính/Localhost).
        *   `[Anti-Farm] Bo qua cong/tru Elo cho Killer va Victim. Ly do: BLOCKED_SPAWN` (Do nạn nhân vừa hồi sinh hoặc đang ở quá gần điểm spawn).
        *   `[Anti-Farm] Bo qua cong/tru Elo cho Killer va Victim. Ly do: BLOCKED_AFK` (Do tài khoản nạn nhân đứng yên không di chuyển/không đánh trả).
    *   Bạn có thể vô hiệu hóa tạm thời các cơ chế này trong `config.yml` bằng cách tắt `anti-farm.ip-check.enabled: false` hoặc `anti-farm.activity-check.enabled: false` để phục vụ công tác test.
6.  **Tải lại cấu hình**: Gõ `/eloadmin reload` trong game hoặc từ bảng điều khiển console để áp dụng cấu hình mới thay đổi tức thì mà không cần restart server.

---

## 🔍 6. Hệ thống tìm kiếm người chơi bằng Form (Admin Search)

SolarElo hỗ trợ hệ thống nhập liệu tìm kiếm **3 lớp tự động** dành cho Admin, đảm bảo hoạt động mượt mà trên mọi loại client — Java hiện đại, Java cũ, và Bedrock/PE qua Geyser.

### 6.1 Cách mở Search

**Từ GUI Admin List:**
1. Gõ `/eloadmin` để mở giao diện danh sách player.
2. Click vào nút **🧭 SEARCH** (COMPASS, slot mặc định 47) ở thanh điều hướng phía dưới.

**Từ lệnh chat:**
```
/eloadmin search <tên_người_chơi>
/eloadmin <tên_người_chơi>          ← phím tắt
```

---

### 6.2 Hệ thống 3 lớp nhập liệu (3-Tier Input)

Plugin tự động phát hiện loại client và chọn phương thức nhập phù hợp:

| Ưu tiên | Loại client | Phương thức |
| :---: | :--- | :--- |
| 1 | Java **1.21.6+** (protocol ≥ 769) | **Paper Dialog** — hộp thoại native với ô nhập tên, có nút Tìm kiếm / Hủy |
| 2 | **Bedrock/PE** qua Geyser + Floodgate | **Floodgate CustomForm** — form giao diện native của Bedrock với trường văn bản |
| 3 | Java cũ (< 1.21.6) hoặc không có Floodgate | **Chat Prompt** — nhắn vào chat, gõ `cancel` để hủy |

> **Lưu ý**: Plugin tự động dự phòng (fallback) xuống lớp tiếp theo nếu lớp trên không khả dụng. Không cần cấu hình thêm.

---

### 6.3 Cấu hình nút Search trong `gui/admin.yml`

Nút Search có thể tùy chỉnh đầy đủ qua file `gui/admin.yml`:

```yaml
# Nút tìm kiếm (Search Button) — slot mặc định: 47
search:
  name: "#ffaa00sᴇᴀʀᴄʜ"
  lore:
    - "&fClick to search a player by name"
  material: "COMPASS"
  slot: 47
  customModelData: -1
```

Để đặt nút Search vào một ô khác, chỉ cần thay `slot: 47` thành số slot mong muốn (0–53).

---

### 6.4 Tùy chỉnh thông báo Search trong `messages.yml`

```yaml
# ── Search / Form ─────────────────────
search-form-title: "Tìm kiếm người chơi"
search-form-label: "Nhập tên người chơi:"
search-form-placeholder: "Tên..."

search-dialog-title: "Tìm kiếm người chơi"
search-dialog-body:  "Nhập tên người chơi cần tìm:"
search-dialog-field: "Tên người chơi"

search-chat-prompt: "&#ffaa00ᴇʟᴏ ᴀᴅᴍɪɴ &8» &fNhập tên người chơi cần tìm &7(gõ &ccancel &7để hủy):"
```

---

### 6.5 Tích hợp Floodgate (Bedrock/PE)

SolarElo sử dụng **pure reflection** để giao tiếp với Floodgate — **không yêu cầu** Floodgate phải có mặt trên server để plugin hoạt động. Đây là soft-dependency hoàn toàn.

| Trạng thái | Hành vi |
| :--- | :--- |
| Floodgate **không có** | PE/Bedrock dùng Chat Prompt như Java cũ |
| Floodgate **có mặt** | PE/Bedrock thấy native Bedrock Form |
| Floodgate có nhưng **form lỗi** | Tự động fallback về Chat Prompt, ghi log cảnh báo |

**Cài đặt Floodgate** (tùy chọn, chỉ cần nếu muốn hỗ trợ Bedrock Form):
- Tải Geyser + Floodgate từ [https://geysermc.org](https://geysermc.org)
- Cài vào thư mục `plugins/`
- SolarElo tự phát hiện và kích hoạt hỗ trợ Form mà không cần cấu hình thêm

---

## 🎮 Bổ sung: Bảng Lệnh Đầy Đủ (Cập nhật)

| Lệnh | Mô tả | Quyền hạn | Mặc định |
| :--- | :--- | :--- | :--- |
| `/elo` | Mở GUI Menu Chính | *Không yêu cầu* | `True` |
| `/elo <player>` | Xem thông số Elo của người chơi khác | *Không yêu cầu* | `True` |
| `/topelo` | Mở GUI Bảng xếp hạng Elo | *Không yêu cầu* | `True` |
| `/bounty` | Mở GUI Nhận nhiệm vụ săn thưởng | *Không yêu cầu* | `True` |
| `/eloadmin` | Mở GUI Admin danh sách player | `solarelo.admin` | `OP` |
| `/eloadmin <player>` | Phím tắt mở trang Admin chi tiết player | `solarelo.admin` | `OP` |
| `/eloadmin search <player>` | Tìm kiếm & mở trang Admin chi tiết | `solarelo.admin` | `OP` |
| `/eloadmin set <player/*> <amount>` | Đặt điểm Elo | `solarelo.admin` | `OP` |
| `/eloadmin add <player/*> <amount>` | Cộng điểm Elo | `solarelo.admin` | `OP` |
| `/eloadmin remove <player/*> <amount>` | Trừ điểm Elo | `solarelo.admin` | `OP` |
| `/eloadmin reset <player/*>` | Reset toàn bộ thống kê | `solarelo.admin` | `OP` |
| `/eloadmin lock <player>` | Khóa Elo (không thay đổi khi PvP/Decay) | `solarelo.admin` | `OP` |
| `/eloadmin unlock <player>` | Mở khóa Elo | `solarelo.admin` | `OP` |
| `/eloadmin season reset` | Kết thúc mùa giải & phát thưởng | `solarelo.admin` | `OP` |
| `/eloadmin reload` | Tải lại toàn bộ cấu hình | `solarelo.admin` | `OP` |

> Sử dụng `*` thay cho `<player>` trong các lệnh `set`, `add`, `remove`, `reset` để áp dụng cho **tất cả** người chơi trong database.

---

## 🖥️ 7. Tương thích Client & Phiên bản

| Client | Phiên bản | Tính năng được hỗ trợ |
| :--- | :--- | :--- |
| Paper / Folia | 1.21.6+ | Tất cả tính năng, Dialog native text input |
| Paper / Folia | 1.21.x (< 1.21.6) | Tất cả tính năng, Chat Prompt thay Dialog |
| Bedrock/PE via Geyser | Mọi phiên bản | Tất cả GUI, Bedrock Form cho search (cần Floodgate) |
| Java qua ViaVersion | ≥ 1.8 | GUI hoạt động, Dialog chỉ có trên ≥ 1.21.6 |

> **Ghi chú Folia**: SolarElo được tối ưu đặc biệt cho Folia — toàn bộ task scheduling sử dụng `runForEntity()` và `runAsync()` tương thích hoàn toàn với mô hình thread của Folia.

---

## ❓ 8. Câu hỏi thường gặp (FAQ)

**Q: Tại sao Admin PE không thấy Form khi click Search?**
> A: Cần cài Floodgate (`plugins/Floodgate.jar`). Nếu chỉ có Geyser mà không có Floodgate thì plugin dùng Chat Prompt thay thế.

**Q: Tại sao Elo không thay đổi sau khi PvP?**
> A: Kiểm tra console log tìm tag `[Anti-Farm]`. Các lý do phổ biến: `BLOCKED_IP` (cùng IP), `BLOCKED_AFK` (nạn nhân AFK), `BLOCKED_SPAWN` (spawn camping), `BLOCKED_ELO_DIFF` (chênh lệch Elo quá lớn).

**Q: Làm sao để test mà không bị chặn bởi Anti-Farm?**
> A: Tạm thời tắt trong `config.yml`:
> ```yaml
> anti-farm:
>   enabled: false
>   ip-check:
>     enabled: false
>   activity-check:
>     enabled: false
> ```
> Nhớ bật lại sau khi test xong và dùng `/eloadmin reload`.

**Q: Plugin có hỗ trợ MySQL cho multi-server không?**
> A: Có. Cấu hình trong `config.yml` mục `database.type: MYSQL` và điền thông tin kết nối. Tất cả server dùng chung một database sẽ chia sẻ bảng xếp hạng toàn cầu.

**Q: Soft-Reset Elo hoạt động thế nào?**
> A: Công thức: `Elo mới = default-elo + (Elo hiện tại - default-elo) × multiplier`. Ví dụ: người chơi có 2000 Elo, default 1000, multiplier 0.4 → Elo mới = `1000 + (2000 - 1000) × 0.4 = 1400`. Cân bằng giữa cũ và mới mà không xóa hoàn toàn thành tích.
