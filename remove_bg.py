"""
Remove background của phuc_dia_bg.png
Flood fill từ cạnh, dùng max per-channel diff để không bị lọc sai.
"""
from PIL import Image

INPUT  = "phuc_dia_bg.png"
OUTPUT = "phuc_dia_bg_nobg.png"
TOLERANCE = 60  # max diff mỗi channel

def color_diff(c1, c2):
    # Max per-channel (không dùng sum — tránh lọc oan màu sáng)
    return max(abs(int(a) - int(b)) for a, b in zip(c1[:3], c2[:3]))

img = Image.open(INPUT).convert("RGBA")
pixels = img.load()
w, h = img.size

# Sample màu nền từ điểm chắc chắn là nền (gần cạnh trái, giữa)
bg_color = pixels[10, h // 2][:3]
print(f"bg_color: {bg_color}")

# Flood fill từ tất cả các cạnh
visited = set()
stack = []
for x in range(0, w, 1):
    stack += [(x, 0), (x, h-1)]
for y in range(0, h, 1):
    stack += [(0, y), (w-1, y)]

while stack:
    x, y = stack.pop()
    if (x, y) in visited:
        continue
    if x < 0 or x >= w or y < 0 or y >= h:
        continue
    px = pixels[x, y]
    if px[3] == 0:
        visited.add((x, y))
        stack += [(x+1,y),(x-1,y),(x,y+1),(x,y-1)]
        continue
    if color_diff(px, bg_color) > TOLERANCE:
        continue
    visited.add((x, y))
    pixels[x, y] = (0, 0, 0, 0)
    stack += [(x+1,y),(x-1,y),(x,y+1),(x,y-1)]

img.save(OUTPUT)
print(f"Saved: {OUTPUT}  ({w}x{h})")
