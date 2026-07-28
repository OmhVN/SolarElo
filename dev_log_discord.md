# 🛠️ SolarElo - Discord DevLog Announcement
**Phiên bản mới: 1.1-R08 (Canvas / Folia Region Thread-Safety Fix & Library Optimization)**

Sao chép khối văn bản bên dưới để gửi thẳng lên kênh thông báo cập nhật của Discord:

```markdown
**DevLog Plugins #134 28 / 7 / 2026**
• **SolarElo-1.1-R08 (Canvas / Folia Region Thread-Safety Fix & Library Optimization)**

> [#] Sửa lỗi tương thích Canvas & Folia (Thread Safety Fix):
> * **Khắc phục triệt để exception `Thread failed main thread check: Cannot init menu async`** khi mở GUI hoặc thao tác trên inventory người chơi ở môi trường Canvas / Folia.
> * **Chuyển đổi toàn bộ tác vụ GUI sang Entity Scheduler**: Tự động chuyển đổi các lệnh mở giao diện (`openMainMenu`, `openLeaderboard`, `openSettings`, `openEloAdmin`, `openStats`, v.v.), cập nhật inventory và phản hồi chat prompt sang chạy chuẩn xác trên Region Thread của từng người chơi (`runForEntity`).
> * Đảm bảo tính tương thích đồng bộ 100% trên cả Spigot, Paper và Folia/Canvas đa luồng phân vùng.
>
> [#] Tối ưu hóa thư viện & làm sạch mã nguồn:
> * Loại bỏ 100% các chú thích (comments) dư thừa trong toàn bộ mã nguồn Java.
> * Bổ sung thư viện lưu trữ bộ nhớ đệm hiệu năng cao **Caffeine** (`com.github.ben-manes.caffeine:caffeine`) vào `build.gradle` với cơ chế relocation package tự động.

@• KHÁCH HÀNG (#) • - Mua Tại: # 🎫 | ticket
```
