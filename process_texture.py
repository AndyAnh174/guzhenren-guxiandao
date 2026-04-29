"""
Universal texture processor — xóa phông + resize 16x16
Usage: python process_texture.py <input> <output> [tolerance]
  input:     ảnh gốc (512-2048px)
  output:    đường dẫn output (thường là textures/item/xxx.png)
  tolerance: 0-100, mặc định 50 (tăng nếu nền không đồng màu)

Ví dụ:
  python process_texture.py raw/tran_vu.png src/main/resources/assets/cotienaddon/textures/item/tran_vu.png
  python process_texture.py raw/tran_vu.png src/main/resources/assets/cotienaddon/textures/item/tran_vu.png 40
"""
import sys
from PIL import Image

if len(sys.argv) < 3:
    print(__doc__)
    sys.exit(1)

INPUT     = sys.argv[1]
OUTPUT    = sys.argv[2]
TOLERANCE = int(sys.argv[3]) if len(sys.argv) > 3 else 50

def color_diff(c1, c2):
    return max(abs(int(a) - int(b)) for a, b in zip(c1[:3], c2[:3]))

img = Image.open(INPUT).convert("RGBA")
pixels = img.load()
w, h = img.size
print(f"Input: {INPUT}  ({w}x{h})")

# 1. Sample bg — lấy điểm gần cạnh trái giữa ảnh (ít bị sai nhất)
bg_color = pixels[10, h // 2][:3]
print(f"BG color sampled: {bg_color}  tolerance={TOLERANCE}")

# 2. Flood fill từ 4 cạnh
visited = set()
stack = [(x, y) for x in range(w) for y in [0, h-1]] + \
        [(x, y) for y in range(h) for x in [0, w-1]]
while stack:
    x, y = stack.pop()
    if (x, y) in visited or not (0 <= x < w and 0 <= y < h):
        continue
    px = pixels[x, y]
    visited.add((x, y))
    if px[3] == 0 or color_diff(px, bg_color) <= TOLERANCE:
        pixels[x, y] = (0, 0, 0, 0)
        stack += [(x+1,y),(x-1,y),(x,y+1),(x,y-1)]

# 3. Crop sát nội dung + pad về hình vuông
bbox = img.getbbox()
if bbox is None:
    print("ERROR: Toàn bộ ảnh bị xóa! Giảm tolerance xuống.")
    sys.exit(1)
cropped = img.crop(bbox)
cw, ch = cropped.size
side = max(cw, ch)
padded = Image.new("RGBA", (side, side), (0, 0, 0, 0))
padded.paste(cropped, ((side-cw)//2, (side-ch)//2))

# 4. Resize LANCZOS → 16x16
resized = padded.resize((16, 16), Image.LANCZOS)

# 5. Hard alpha threshold — tránh viền mờ do LANCZOS blend
data = resized.getdata()
resized.putdata([(r, g, b, 0 if a < 100 else 255) for r, g, b, a in data])

resized.save(OUTPUT)
transparent = sum(1 for r, g, b, a in resized.getdata() if a == 0)
print(f"Saved: {OUTPUT}  ({transparent}/256 pixels transparent)")
