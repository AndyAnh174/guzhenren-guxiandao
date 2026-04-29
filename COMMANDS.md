# Danh sách lệnh — CoTien Addon

> Tất cả lệnh đều cần quyền op (permission level 2).

---

## `/cotien debug` — Lệnh debug (test tính năng)

### Thăng Tiên

| Lệnh | Tác dụng |
|------|----------|
| `/cotien debug ascend` | Bắt đầu thăng tiên (phase 1 → 2 tự động sau 3 giây, bypass điều kiện) |
| `/cotien debug complete` | Force hoàn thành thăng tiên (phase 3 → 4, lên Cổ Tiên ngay) |
| `/cotien debug reset` | Reset về phase 0, xóa Thiên Khí / Địa Khí |

### Nhân Khí & Phúc Địa

| Lệnh | Tác dụng |
|------|----------|
| `/cotien debug setnk <số>` | Set Nhân Khí (vd: `setnk 1000` = Trung đẳng, `setnk 10000` = Thượng đẳng, `setnk 100000` = Siêu đẳng) |
| `/cotien debug setgrade <1-4>` | Đổi grade Phúc Địa (1=Hạ, 2=Trung, 3=Thượng, 4=Siêu). Phải vào lại Phúc Địa để biome thay đổi |
| `/cotien debug settn <số>` | Set Tiên Nguyên trực tiếp (vd: `settn 500` để test nâng cấp) |

### Thông tin

| Lệnh | Tác dụng |
|------|----------|
| `/cotien debug status` | In toàn bộ CoTienData: phase, grade, slot, Nhân Khí, Thiên Khí, Địa Khí |

### Thiên Kiếp / Địa Tai (test trong Phúc Địa)

| Lệnh | Tác dụng |
|------|----------|
| `/cotien debug kiep` | Set ThienKhi=600 → Thiên Kiếp kích hoạt tick tiếp (sét + Phantom) |
| `/cotien debug ditai` | Set DiaKhi=600 → Địa Tai kích hoạt tick tiếp (Ravager + địa chấn) |

> Cần đứng **trong Phúc Địa** để event trigger. Threshold mặc định = 500, giảm xuống khi nâng **Phòng Hộ**.

### Đạo Ngân (liupai_*dao)

| Lệnh | Tác dụng |
|------|----------|
| `/cotien debug setdao <tên_đạo> <số>` | Set điểm cho 1 Đạo cụ thể (vd: `setdao tiendao 100000`) |
| `/cotien debug listdao` | Xem tổng quan Đạo Ngân: số đạo đạt ≥100k, đạo cao nhất |

**Ngưỡng quan trọng**: 100,000 = Chuẩn Vô thượng Đại Tông Sư → cần **≥2 đạo** để thăng tiên.

**Tên đạo hỗ trợ** (chọn 1 trong các alias):

| Tên lệnh | Đạo | Tên lệnh | Đạo |
|----------|-----|----------|-----|
| `tiendao` / `tiandao` | Thiên Đạo | `thodao` / `tudao` | Thổ Đạo |
| `thudao2` / `shidao` | Thời Đạo | `kiemdao` / `jiandao` | Kiếm Đạo |
| `leidao` | Lôi Đạo | `hanhdao` / `xingdao` | Hành Đạo |
| `thuidao` / `shuidao` | Thủy Đạo | `viemdao` / `yandao` | Viêm Đạo |
| `mocdao` / `mudao` | Mộc Đạo | `phihanhhdao` | Phi Hành Đạo |
| `guangdao` | Quang Đạo | `andao` | Ám Đạo |
| `kimdao` / `jindao` | Kim Đạo | `nhandao` / `rendao` | Nhân Đạo |
| `hundao` | Hồn Đạo | `vundao` / `yundao` | Vân Đạo |
| `huyetdao` / `xuedao` | Huyết Đạo | `dandao` | Đan Đạo |
| `docdao` / `dudao` | Độc Đạo | `mongdao` / `mengdao` | Mộng Đạo |
| `daodao` | Đạo Đạo | `cotdao` / `gudao` | Cốt Đạo |
| `nudao` | Nộ Đạo | `tamdao` / `xindao` | Tâm Đạo |
| `hoadao` / `huadao` | Hoa Đạo | `nguvetdao` / `yuedao` | Nguyệt Đạo |
| `bienhoadao` | Biến Hóa Đạo | `thaudao` / `toudao` | Thấu Đạo |
| `hoandao` / `huandao` | Hoán Đạo | `trudao` / `zhoudao` | Trụ Đạo |
| `lucdao` / `lidao` | Lực Đạo | `anhdao` / `yingdao` | Ảnh Đạo |
| `lucdao2` / `lvdao` | Lục Đạo | `amdao` / `yindao` | Âm Đạo |
| `kimdao2` / `jindao2` | Kim Đạo 2 | `hudao` / `xudao` | Hư Đạo |
| `tridao` / `zhidao` | Trí Đạo | `trandao` / `zhendao` | Trấn Đạo |
| `khidao` / `qidao` | Khí Đạo | `bangdao` / `bingdao` | Băng Đạo |
| `bangxuedao` | Băng Tuyết Đạo | `phonddao` / `fengdao` | Phong Đạo |
| `luyndao` / `liandao` | Luyện Đạo | `vundao2` / `yundao2` | Vân Đạo 2 |

```bash
# Set 2 đạo đủ điều kiện thăng tiên
/cotien debug setdao tiendao 100000
/cotien debug setdao thodao 100000
/cotien debug listdao    # kiểm tra: "Số đạo ≥100k: 2/44 ✓ Đủ thăng tiên"
```

---

### Trấn Vũ Cổ (test skill)

| Lệnh | Tác dụng |
|------|----------|
| `/cotien debug seal` | Tự áp Băng Phong Trận lên bản thân — đứng yên 10 giây |
| `/cotien debug unseal` | Giải phóng khỏi Băng Phong Trận ngay lập tức |

### Địa Linh Quest (test)

| Lệnh | Tác dụng |
|------|----------|
| `/cotien debug questcomplete` | Hoàn thành nhiệm vụ Địa Linh hiện tại ngay (Shift+Click Địa Linh để nhận thưởng) |
| `/cotien debug questreset` | Reset toàn bộ quest + bond Địa Linh về trạng thái ban đầu |

---

## `/cotien setkongqiao <số>` — Set số Khiếu (Aptitude Grade)

**Aptitude grade trong màn hình Khai Khiếu (Hi Vọng Cổ) dựa vào `kongqiao`:**

| Giá trị | Hiển thị | Ghi chú |
|---------|---------|---------|
| 1 hoặc 4 | Ất Đẳng | B-grade |
| **3** | **Giáp Đẳng — ✦ Thập Tuyệt Thiên Tử ✦** | **Cao nhất, texture đặc biệt** |
| 5–7 | Bính Đẳng | C-grade |
| 8–10 | Đinh Đẳng | D-grade |

> ⚠ `tizhi` (thể chất) và `kongqiao` (aptitude) là 2 biến RIÊNG BIỆT.  
> Khai Khiếu screen dùng `kongqiao`, Stats screen dùng `tizhi`.

```bash
/cotien setkongqiao 3   # Giáp Đẳng → Thập Tuyệt Thiên Tử
/cotien settizhi 2      # Cổ Nguyệt Âm Hoang Thể (set SAU khi confirm Khiếu)
/cotien setqiyun 100    # Hồng Vận Tề Thiên (set SAU khi confirm Khiếu)
```

> ⚠ Khi xác nhận Không Khiếu, game sẽ **override lại `tizhi` và `qiyun`** về random.  
> Phải chạy lại `/cotien settizhi` và `/cotien setqiyun` SAU KHI bấm xác nhận.

---

## `/cotien setdaode <số>` — Set Đạo Đức

**Tác dụng của Đạo Đức:**
- Tăng khi giết yêu quái/quái vật (evil mobs) → `+5 × zhuanshu của target`
- Giảm khi giết NPC tu sĩ lành (neutral cultivator NPCs) → `-50 × zhuanshu của target`
- Ảnh hưởng đến reputation và quest với NPC trong mod

| Giá trị | Cấp độ |
|---------|--------|
| ≤ -100.000 | Táng tận thiên lương (tệ nhất) |
| -100.000 → -10.001 | Thập ác bất xá |
| -10.000 → -1.001 | Ác quán mãn doanh |
| -1.000 → -101 | Thâm độc độc ác |
| -100 → -1 | Tâm mật thủ lạt |
| 0 | Không |
| 1 → 99 | Trợ nhân vi lạc |
| 100 → 999 | Tích thiện thành đức |
| 1.000 → 9.999 | Quang minh lỗi lạc |
| 10.000 → 99.999 | Đức cao vọng trọng |
| ≥ 100.000 | Hoạt Phật tại thế (tốt nhất) |

```bash
/cotien setdaode 100000    # Hoạt Phật tại thế
/cotien setdaode -100000   # Táng tận thiên lương
/cotien setdaode 0         # Reset về Không
```

---

## `/cotien setqiyun <số>` — Set Khí Vận

**Tác dụng của Khí Vận:**
- Ảnh hưởng trực tiếp đến **tỉ lệ drop đồ** từ ký sinh thể (shiti), cây thuốc, luyện đan...
- Công thức: `xác suất thực tế = base_rate + qiyun × 0.01`
- Mỗi +1 qiyun = +1% tỉ lệ drop, mỗi -1 qiyun = -1% tỉ lệ drop
- Range thực dụng: -100 đến +100

| Giá trị | Cấp độ |
|---------|--------|
| < -100 | ⟨Hắc Quan Tử Vận⟩ (max xui) |
| -100 → -80 | Hắc Quan Tử Vận |
| -79 → -60 | Mệnh đồ đa suyễn |
| -59 → -40 | Thời quai vận kiển |
| -39 → -20 | Họa tại đán tịch |
| -19 → -1 | Họa bất đơn hành |
| 0 | Không |
| 1 → 20 | Thời lai vận chuyển |
| 21 → 40 | Thiên tùy nhân nguyện |
| 41 → 60 | Thời vận hanh thông |
| 61 → 80 | Cát tinh cao chiếu |
| 81 → 100 | Hồng Vận Tề Thiên |
| > 100 | ⟨Hồng Vận Tề Thiên⟩ (max hên) |

```bash
/cotien setqiyun 100    # Hồng Vận Tề Thiên (drop đồ tốt nhất)
/cotien setqiyun -100   # Hắc Quan Tử Vận (drop đồ tệ nhất)
/cotien setqiyun 0      # Reset về Không
```

---

## `/cotien settizhi <0-15>` — Set Thể Chất

| Lệnh | Tác dụng |
|------|----------|
| `/cotien tizhi` | Xem danh sách tất cả 15 thể chất kèm số ID |
| `/cotien settizhi 0` | Xóa thể chất (Không) |
| `/cotien settizhi <1-15>` | Set thể chất theo số |

### Danh sách Thể Chất (tizhi)

| ID | Tên | ID | Tên |
|----|-----|----|-----|
| 1 | Thái Nhật Dương Mãng Thể | 9 | Hậu Thổ Nguyên Ương Thể |
| 2 | Cổ Nguyệt Âm Hoang Thể | 10 | Vũ Trụ Đại Diễn Thể |
| 3 | Bắc Minh Băng Phách Thể | 11 | Chí Tôn Tiên Thai Thể |
| 4 | Sâm Hải Luân Hồi Thể | 12 | Thuần Mộng Cầu Chân Thể |
| 5 | Viêm Hoàng Lôi Trạch Thể | 13 | Khí Vận Chi Tử |
| 6 | Vạn Kim Diệu Hoa Thể | 14 | Thiên Ngoại Chi Ma |
| 7 | Đại Lực Chân Võ Thể | 15 | Chính Đạo Thiện Đức Thân |
| 8 | Tiêu Dao Trí Tâm Thể | 0 | Không (xóa) |

---

## `/cotien dialinhname <tên>` — Đặt tên Địa Linh

> Yêu cầu: đã hoàn thành chuỗi **Nhận Chủ** (bond quest). Phải đứng trong Phúc Địa.

```
/cotien dialinhname Thanh Long Vệ
```

---

## Keybind

| Phím | Tác dụng |
|------|----------|
| `K` | Mở GUI Tiên Khiếu (Không Khiếu Screen) — xem Nhân Khí, thăng tiên |
| `P` | Mở GUI Phúc Địa — tổng quan, quản lý khách, nâng cấp |

---

## Luồng test chuẩn (từ đầu đến Cổ Tiên)

```bash
# 1. Set Nhân Khí (chọn grade muốn test)
/cotien debug setnk 1000      # Trung đẳng
/cotien debug setnk 10000     # Thượng đẳng
/cotien debug setnk 100000    # Siêu đẳng

# 2. Lên Cổ Tiên ngay
/cotien debug complete

# 3. Set Tiên Nguyên để test upgrade
/cotien debug settn 500

# 4. Nhấn P → Vào Phúc Địa → Nâng Cấp

# 5. Đổi grade để xem biome khác nhau
/cotien debug setgrade 4
# Nhấn P → Vào Phúc Địa (biome Bamboo Jungle cho Siêu đẳng)
```

---

## Grade Phúc Địa & Biome

| Grade | Nhân Khí tối thiểu | Biome | Tiên Nguyên/giây |
|-------|-------------------|-------|-----------------|
| 1 — Hạ đẳng | < 1.000 | Cherry Grove 🌸 | 0.01/s |
| 2 — Trung đẳng | 1.000 | Flower Forest 🌺 | 0.02/s |
| 3 — Thượng đẳng | 10.000 | Old Growth Spruce Taiga 🌲 | 0.035/s |
| 4 — Siêu đẳng | 100.000 | Bamboo Jungle 🎋 | 0.05/s |

> Rate trên là base rate. Có thể tăng bằng nâng cấp **Năng suất** (×0.5 mỗi cấp, tối đa ×3.5 ở cấp 5).

---

## Nâng cấp Phúc Địa (qua GUI)

Nhấn P → Tổng quan → **⚙ Nâng Cấp Phúc Địa**

| Nâng cấp | Base cost | Nhân | Tối đa | Tác dụng |
|----------|-----------|------|--------|----------|
| Cấp Bậc Phúc Địa | 100 TN | ×2 | Cấp 10 | Mở rộng bán kính vùng |
| Năng Suất Tiên Nguyên | 100 TN | ×3 | Cấp 5 | +50% rate/cấp |
| Tốc Độ Thời Gian | 300 TN | ×3 | Cấp 5 | Tăng randomTickSpeed (cây/thảo dược lớn nhanh hơn) |
| Phòng Hộ Thiên Kiếp | 500 TN | ×2 | Cấp 5 | Giảm sát thương Thiên Kiếp |
| Khai Mở Linh Mạch | 800 TN | ×3 | Cấp 5 | **Phase 2**: ×1.5→×3.5 tốc độ hấp thụ Thiên/Địa Khí khi thiền. **Phase 4 trong Phúc Địa**: tích lũy thụ động 0.25→1.25 Khí/s |

---

## Items Không Đạo

| Item | Cách lấy | Tác dụng |
|------|----------|----------|
| `cotienaddon:dinh_tien_du` | `/give @s cotienaddon:dinh_tien_du` | Cổ dịch chuyển tọa độ, lưu 5 vị trí |
| `cotienaddon:tien_nguyen` | `/give @s cotienaddon:tien_nguyen` | Tiên Nguyên (nhiên liệu cho Định Tiên Du + nâng cấp Phúc Địa) |
| `cotienaddon:tran_vu` | `/give @s cotienaddon:tran_vu` | Trấn Vũ Cổ — triển khai Băng Phong Trận (phong ấn 16 block) |
