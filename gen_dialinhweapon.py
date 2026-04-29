"""
Tạo texture 16x16 cho vũ khí Địa Linh:
- dia_linh_vu_khi.png: Ngọc Thổ Kiếm (jade earth sword)
- dia_linh_zhang.png: Địa Sát Trượng (earth destruction staff)
"""
from PIL import Image
import os

OUT = r"src\main\resources\assets\cotienaddon\textures\item"
os.makedirs(OUT, exist_ok=True)

TRANSPARENT = (0, 0, 0, 0)

def new(): return Image.new("RGBA", (16, 16), TRANSPARENT)
def px(img, x, y, c): img.putpixel((x, y), c)

# ============================================================
# 1. NGỌC THỔ KIẾM — jade glowing sword (Địa Linh melee)
# ============================================================
sword = new()

# Màu sắc
JADE_LIGHT  = (80,  210, 130, 255)
JADE_MID    = (40,  160,  90, 255)
JADE_DARK   = (20,   90,  50, 255)
GOLD        = (220, 180,  30, 255)
GOLD_DARK   = (160, 120,  10, 255)
WHITE_GLOW  = (200, 255, 220, 200)
HANDLE      = (100,  60,  20, 255)
HANDLE_D    = ( 60,  30,  10, 255)

# Lưỡi kiếm (từ (2,0) đến (7,7) chéo)
blade = [
    (7, 0, JADE_LIGHT),
    (6, 1, JADE_LIGHT),  (7, 1, WHITE_GLOW),
    (5, 2, JADE_MID),    (6, 2, JADE_LIGHT),
    (4, 3, JADE_MID),    (5, 3, JADE_LIGHT),
    (3, 4, JADE_DARK),   (4, 4, JADE_MID),
    (2, 5, JADE_DARK),   (3, 5, JADE_MID),
    (1, 6, JADE_DARK),   (2, 6, JADE_MID),
    (0, 7, JADE_DARK),   (1, 7, JADE_MID),
]
for x, y, c in blade:
    px(sword, x, y, c)

# Thanh chắn tay (guard) vàng tại y=7..8, x=3..9
for x in range(3, 10):
    px(sword, x, 7, GOLD)
    px(sword, x, 8, GOLD_DARK)

# Chuôi kiếm
for i, y in enumerate(range(9, 16)):
    c = HANDLE if i % 2 == 0 else HANDLE_D
    px(sword, 5, y, c)
    px(sword, 6, y, c)

# Ánh sáng phát ra (glow dot)
glow_pts = [(8, 0),(8,1),(9,0)]
for gx, gy in glow_pts:
    if 0<=gx<16 and 0<=gy<16:
        px(sword, gx, gy, WHITE_GLOW)

sword.save(os.path.join(OUT, "dia_linh_vu_khi.png"))
print("Saved dia_linh_vu_khi.png (Ngọc Thổ Kiếm)")

# ============================================================
# 2. ĐỊA SÁT TRƯỢNG — earth staff (ranged / visual)
# ============================================================
staff = new()

EARTH_BROWN = (140,  90,  30, 255)
EARTH_D     = ( 80,  50,  10, 255)
CRYSTAL_G   = ( 60, 220, 140, 255)
CRYSTAL_L   = (160, 255, 200, 220)
RUNE_GOLD   = (230, 190,  50, 255)

# Thân gậy
for y in range(2, 16):
    px(staff, 7, y, EARTH_BROWN)
    px(staff, 8, y, EARTH_D)

# Khắc rune (vàng)
for y in [4, 7, 10, 13]:
    px(staff, 7, y, RUNE_GOLD)

# Đầu pha lê
crystal = [
    (7,0,CRYSTAL_L),(8,0,CRYSTAL_L),
    (6,1,CRYSTAL_G),(7,1,CRYSTAL_L),(8,1,CRYSTAL_G),(9,1,CRYSTAL_G),
    (6,2,CRYSTAL_G),(9,2,CRYSTAL_G),
]
for x,y,c in crystal:
    px(staff, x, y, c)

# Ánh sáng tỏa
glow2 = [(5,0,(*CRYSTAL_L[:3],120)),(10,0,(*CRYSTAL_L[:3],120)),
         (7,0,(*CRYSTAL_L[:3],255)),(8,0,(*CRYSTAL_L[:3],255))]
for x,y,c in glow2:
    if 0<=x<16 and 0<=y<16:
        px(staff, x, y, c)

staff.save(os.path.join(OUT, "dia_linh_zhang.png"))
print("Saved dia_linh_zhang.png (Địa Sát Trượng)")
print(f"\nDone! Textures in {OUT}")
