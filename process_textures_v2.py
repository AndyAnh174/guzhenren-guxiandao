from PIL import Image
import os

def color_diff(c1, c2):
    return max(abs(int(a) - int(b)) for a, b in zip(c1[:3], c2[:3]))

def process_image(input_path, output_paths, tolerance=60):
    if not os.path.exists(input_path):
        print(f"Error: File {input_path} not found.")
        return False

    print(f"Processing: {input_path}")
    img = Image.open(input_path).convert("RGBA")
    pixels = img.load()
    w, h = img.size

    # 1. Sample background color (avoid corners, take a few samples and average)
    # Taking a few samples around the edges to be sure
    samples = [pixels[10, h//2], pixels[w-11, h//2], pixels[w//2, 10], pixels[w//2, h-11]]
    avg_bg = [sum(s[i] for s in samples)//4 for i in range(3)]

    # 2. Flood fill from 4 edges
    visited = set()
    stack = [(x, y) for x in range(w) for y in [0, h-1]] + [(x, y) for y in range(h) for x in [0, w-1]]
    
    while stack:
        x, y = stack.pop()
        if (x, y) in visited or not (0 <= x < w and 0 <= y < h):
            continue
        px = pixels[x, y]
        visited.add((x, y))
        if px[3] == 0 or color_diff(px, avg_bg) <= tolerance:
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

    # Save to all output paths
    for out in output_paths:
        os.makedirs(os.path.dirname(out), exist_ok=True)
        resized.save(out)
        print(f"Saved to: {out}")
    
    return True

if __name__ == "__main__":
    tasks = [
        ("UI-texture/raw/tran-vu-tien-co-raw.png.png", ["src/main/resources/assets/cotienaddon/textures/item/tran_vu.png"]),
        ("UI-texture/raw/quan_tai_bang_ice_raw.png.png", [
            "src/main/resources/assets/cotienaddon/textures/item/quan_tai_bang_ice.png",
            "src/main/resources/assets/cotienaddon/textures/gui/quan_tai_bang_ice.png"
        ]),
        ("UI-texture/raw/quan_tai_bang_rune_raw.png.png", [
            "src/main/resources/assets/cotienaddon/textures/item/quan_tai_bang_rune.png",
            "src/main/resources/assets/cotienaddon/textures/gui/quan_tai_bang_rune.png"
        ]),
    ]

    for inp, outs in tasks:
        success = process_image(inp, outs)
        if not success:
            print(f"Trying with tolerance=30 for {inp}...")
            process_image(inp, outs, tolerance=30)
