"""
Tao 14 placeholder skin Dia Linh (64x64, Minecraft 1.8+ UV layout dung).
"""
from PIL import Image, ImageDraw
import os

OUTPUT_DIR = r"src\main\resources\assets\cotienaddon\textures\entity\dia_linh"
os.makedirs(OUTPUT_DIR, exist_ok=True)

ROBE_COLORS = [
    (0,  140,  80),
    (0,  100, 160),
    (120, 60, 160),
    (180,  90,   0),
    (40,  160, 120),
    (200,  50,  50),
    (0,   60, 120),
    (80,  160,  60),
    (160, 120,   0),
    (100,  40, 100),
    (40,  120, 160),
    (200, 130,   0),
    (60,   40, 140),
    (0,   100,  60),
]

def dark(c, f=0.65): return tuple(int(v*f) for v in c)
def rgba(c, a=255): return (*c, a)

def make_skin(robe):
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    R = rgba(robe)
    D = rgba(dark(robe))          # shadow / sides
    B = rgba(dark(robe, 0.45))    # back
    SKIN  = (220, 180, 140, 255)
    HAIR  = (230, 230, 230, 255)
    EYE   = (0, 240, 120, 255)
    GOLD  = (210, 170, 30, 255)
    GDARK = (150, 110, 10, 255)

    # ============ HEAD (8x8 face area) ============
    # UV: right=0-7,8-15 | front=8-15,8-15 | left=16-23,8-15 | back=24-31,8-15
    # top=8-15,0-7 | bottom=16-23,0-7

    # All head sides: skin tone
    d.rectangle([0, 8, 31, 15], fill=SKIN)    # all 4 sides
    d.rectangle([8, 0, 23, 7], fill=SKIN)     # top + bottom

    # Hair: top row of front + left/right sides
    d.rectangle([8, 8, 15, 9], fill=HAIR)     # hair top of front
    d.rectangle([0, 8, 7, 9], fill=HAIR)      # right side top
    d.rectangle([16, 8, 23, 9], fill=HAIR)    # left side top
    d.rectangle([24, 8, 31, 9], fill=HAIR)    # back top
    d.rectangle([8, 0, 15, 7], fill=HAIR)     # top of head

    # Eyes (front face only, 2 pixels wide, 2 down from top)
    d.rectangle([9, 11, 10, 12], fill=EYE)    # left eye
    d.rectangle([13, 11, 14, 12], fill=EYE)   # right eye

    # HEAD OVERLAY (hat/helmet): front=40-47,8-15
    hat_c = (*tuple(min(v+50,255) for v in robe), 200)
    d.rectangle([32, 8, 63, 15], fill=(*robe, 180))   # all overlay sides
    d.rectangle([40, 8, 63, 8], fill=GOLD)             # gold trim top row

    # ============ BODY ============
    # right=16-19,20-31 | front=20-27,20-31 | left=28-31,20-31 | back=32-39,20-31
    # top=20-27,16-19 | bottom=28-35,16-19

    d.rectangle([16, 16, 39, 31], fill=D)      # all sides dark first
    d.rectangle([20, 20, 27, 31], fill=R)      # front = main color
    d.rectangle([20, 16, 27, 19], fill=R)      # top
    # Gold trim neckline
    d.rectangle([20, 20, 27, 20], fill=GOLD)
    # Vertical center stripe (rune)
    d.rectangle([23, 20, 24, 31], fill=GOLD)

    # ============ RIGHT ARM (4 wide) ============
    # right=40-43,20-31 | front=44-47,20-31 | left=48-51,20-31 | back=52-55,20-31
    d.rectangle([40, 20, 55, 31], fill=D)
    d.rectangle([44, 20, 47, 31], fill=R)      # front
    d.rectangle([44, 20, 47, 20], fill=GOLD)   # shoulder trim

    # ============ LEFT ARM (4 wide, new format) ============
    # right=32-35,52-63 | front=36-39,52-63 | left=40-43,52-63 | back=44-47,52-63
    # top=36-39,48-51 | bottom=40-43,48-51
    d.rectangle([32, 52, 47, 63], fill=D)
    d.rectangle([36, 52, 39, 63], fill=R)      # front
    d.rectangle([36, 52, 39, 52], fill=GOLD)   # shoulder trim

    # ============ RIGHT LEG (4 wide) ============
    # right=0-3,20-31 | front=4-7,20-31 | left=8-11,20-31 | back=12-15,20-31
    d.rectangle([0, 20, 15, 31], fill=D)
    d.rectangle([4, 20, 7, 31], fill=R)        # front
    # Trim at top (waist)
    d.rectangle([4, 20, 7, 20], fill=GOLD)

    # ============ LEFT LEG (new format) ============
    # right=16-19,52-63 | front=20-23,52-63 | left=24-27,52-63 | back=28-31,52-63
    # top=20-23,48-51 | bottom=24-27,48-51
    d.rectangle([16, 52, 31, 63], fill=D)
    d.rectangle([20, 52, 23, 63], fill=R)      # front
    d.rectangle([20, 52, 23, 52], fill=GOLD)

    return img

for i, color in enumerate(ROBE_COLORS):
    skin = make_skin(color)
    path = os.path.join(OUTPUT_DIR, f"skin_{i}.png")
    skin.save(path)

print(f"Done! 14 skins saved to {OUTPUT_DIR}")
