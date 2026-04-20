"""
Remove background của phuc_dia_bg.png
Dùng flood fill từ 4 góc để xóa vùng nền ngoài panel.
"""
from PIL import Image
import sys

INPUT  = "phuc_dia_bg.png"
OUTPUT = "phuc_dia_bg_nobg.png"
TOLERANCE = 60   # tăng nếu còn sót nền, giảm nếu bị ăn vào panel

def color_diff(c1, c2):
    return sum(abs(a - b) for a, b in zip(c1[:3], c2[:3]))

def flood_fill_transparent(img, seed_points, tolerance):
    pixels = img.load()
    w, h = img.size
    # Sample màu nền từ nhiều điểm rồi lấy trung bình
    sample_pts = [(2, 2), (w-3, 2), (2, h-3), (w-3, h-3), (w//2, 2)]
    valid = [pixels[x, y][:3] for x,y in sample_pts if pixels[x,y][3] > 0]
    if valid:
        bg_color = tuple(sum(c[i] for c in valid)//len(valid) for i in range(3))
    else:
        bg_color = pixels[0, 0][:3]
    print(f"Background color sampled: {bg_color}")

    visited = set()
    stack = list(seed_points)

    while stack:
        x, y = stack.pop()
        if (x, y) in visited:
            continue
        if x < 0 or x >= w or y < 0 or y >= h:
            continue
        px = pixels[x, y]
        if color_diff(px, bg_color) > tolerance:
            continue
        visited.add((x, y))
        pixels[x, y] = (0, 0, 0, 0)  # transparent
        stack += [(x+1,y),(x-1,y),(x,y+1),(x,y-1)]

    return img

img = Image.open(INPUT).convert("RGBA")
w, h = img.size

# Seed từ 4 góc + các cạnh
seeds = []
for i in range(0, w, 4):
    seeds += [(i, 0), (i, h-1)]
for i in range(0, h, 4):
    seeds += [(0, i), (w-1, i)]

result = flood_fill_transparent(img, seeds, TOLERANCE)
result.save(OUTPUT)
print(f"Saved: {OUTPUT}  ({w}x{h})")
