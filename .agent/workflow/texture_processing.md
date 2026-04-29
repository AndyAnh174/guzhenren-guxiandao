# Workflow: Xử lý Texture (xóa phông + resize 16×16)

## Quy trình đúng

Làm tại full resolution → crop → resize → threshold. KHÔNG resize trước rồi mới xóa phông.

```python
from PIL import Image

INPUT = "raw/item_name.png"    # ảnh gốc cao res (512–2048px)
OUTPUT = "src/main/resources/assets/cotienaddon/textures/item/item_name.png"
TOLERANCE = 50  # tăng nếu nền không đều màu

def color_diff(c1, c2):
    return max(abs(int(a) - int(b)) for a, b in zip(c1[:3], c2[:3]))

img = Image.open(INPUT).convert("RGBA")
pixels = img.load()
w, h = img.size

# 1. Sample màu nền — lấy điểm chắc chắn là nền, KHÔNG lấy góc
bg_color = pixels[10, h // 2][:3]

# 2. Flood fill từ 4 cạnh
visited = set()
stack = [(x, y) for x in range(w) for y in [0, h-1]] + \
        [(x, y) for y in range(h) for x in [0, w-1]]
while stack:
    x, y = stack.pop()
    if (x, y) in visited or not (0 <= x < w and 0 <= y < h): continue
    px = pixels[x, y]
    visited.add((x, y))
    if px[3] == 0 or color_diff(px, bg_color) <= TOLERANCE:
        pixels[x, y] = (0, 0, 0, 0)
        stack += [(x+1,y),(x-1,y),(x,y+1),(x,y-1)]

# 3. Crop sát nội dung + pad vuông
bbox = img.getbbox()
cropped = img.crop(bbox)
cw, ch = cropped.size
side = max(cw, ch)
padded = Image.new("RGBA", (side, side), (0, 0, 0, 0))
padded.paste(cropped, ((side-cw)//2, (side-ch)//2))

# 4. Resize LANCZOS → 16×16
resized = padded.resize((16, 16), Image.LANCZOS)

# 5. Hard alpha threshold — BẮT BUỘC, tránh viền mờ
data = resized.getdata()
resized.putdata([(r, g, b, 0 if a < 100 else 255) for r, g, b, a in data])

resized.save(OUTPUT)
print(f"Done: {OUTPUT}")
```

## Lỗi thường gặp

| Triệu chứng | Nguyên nhân | Fix |
|-------------|-------------|-----|
| Ảnh 16×16 bị mờ, vỡ pixel | Resize NEAREST từ ảnh lớn | Dùng LANCZOS |
| Viền trắng/xám mờ sau resize | LANCZOS tạo alpha trung gian 1–99 | Hard threshold `a < 100 → 0` |
| Xóa nhầm màu của vật thể | Tolerance quá cao hoặc sample sai điểm nền | Giảm tolerance; chọn điểm nền chắc chắn |
| Nền xám tưởng transparent | Viewer checkerboard ≠ alpha thực | Luôn `.convert("RGBA")` rồi kiểm tra |

## Đặt texture
- Item: `src/main/resources/assets/cotienaddon/textures/item/`
- GUI: `src/main/resources/assets/cotienaddon/textures/gui/`
- Format: PNG, 16×16, RGBA (có alpha channel)
