from PIL import Image
import os

def color_diff(c1, c2):
    return max(abs(int(a) - int(b)) for a, b in zip(c1[:3], c2[:3]))

def process_image(input_path, output_path, tolerance=50):
    if not os.path.exists(input_path):
        print(f"Error: File {input_path} not found.")
        return False

    print(f"Processing: {input_path} -> {output_path}")
    img = Image.open(input_path).convert("RGBA")
    pixels = img.load()
    w, h = img.size

    # 1. Sample background color (at [10, h//2])
    bg_color = pixels[10, h // 2][:3]

    # 2. Flood fill from 4 edges
    visited = set()
    stack = ([(x, y) for x in range(w) for y in [0, h-1]] + 
               [(x, y) for y in range(h) for x in [0, w-1]])
    
    while stack:
        x, y = stack.pop()
        if (x, y) in visited or not (0 <= x < w and 0 <= y < h):
            continue
        px = pixels[x, y]
        visited.add((x, y))
        if px[3] == 0 or color_diff(px, bg_color) <= tolerance:
            pixels[x, y] = (0, 0, 0, 0)
            stack += [(x+1, y), (x-1, y), (x, y+1), (x, y-1)]

    # 3. Crop to content + pad to square
    bbox = img.getbbox()
    if not bbox:
        print(f"Error: No content found in {input_path}")
        return False
        
    cropped = img.crop(bbox)
    cw, ch = cropped.size
    side = max(cw, ch)
    padded = Image.new("RGBA", (side, side), (0, 0, 0, 0))
    padded.paste(cropped, ((side-cw)//2, (side-ch)//2))

    # 4. Resize LANCZOS to 16x16
    resized = padded.resize((16, 16), Image.LANCZOS)

    # 5. Hard alpha threshold
    data = resized.getdata()
    resized.putdata([(r, g, b, 0 if a < 100 else 255) for r, g, b, a in data])

    # Save
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    resized.save(output_path)
    print(f"Successfully saved: {output_path}")
    return True

if __name__ == "__main__":
    tasks = [
        ("UI-texture/raw/tran-vu-tien-co-raw.png.png", "src/main/resources/assets/cotienaddon/textures/item/tran_vu.png"),
        ("UI-texture/raw/quan_tai_bang_ice_raw.png.png", "src/main/resources/assets/cotienaddon/textures/block/quan_tai_bang_ice.png"),
        ("UI-texture/raw/quan_tai_bang_rune_raw.png.png", "src/main/resources/assets/cotienaddon/textures/block/quan_tai_bang_rune.png"),
    ]

    for inp, out in tasks:
        success = process_image(inp, out)
        if not success:
            print(f"Trying with tolerance=30 for {inp}...")
            process_image(inp, out, tolerance=30)
