import os
from PIL import Image, ImageOps
import numpy as np

def process_item_icon(img_path, output_path):
    # 1. Open
    img = Image.open(img_path).convert("RGBA")
    
    # 2. Flood fill remove white background
    # Since the prompt said solid white, we look for white pixels
    pixels = np.array(img)
    
    # Simple threshold for white-ish to transparent
    # Distance from white
    dist = np.sum((pixels[:, :, :3] - [255, 255, 255]) ** 2, axis=2)
    # Make pixels very close to white transparent
    mask = dist < 2000
    pixels[mask, 3] = 0
    
    img = Image.fromarray(pixels)
    
    # 3. Hard crop
    alpha = pixels[:, :, 3]
    coords = np.argwhere(alpha > 0)
    if coords.size > 0:
        y_min, x_min = coords.min(axis=0)
        y_max, x_max = coords.max(axis=0)
        # Pad to make it square
        width = x_max - x_min + 1
        height = y_max - y_min + 1
        size = max(width, height)
        # Center in square
        pad_x = (size - width) // 2
        pad_y = (size - height) // 2
        
        square = Image.new("RGBA", (size, size), (0, 0, 0, 0))
        cropped = img.crop((x_min, y_min, x_max + 1, y_max + 1))
        square.paste(cropped, (pad_x, pad_y))
        img = square

    # 4. Resize to 16x16 LANCZOS
    img = img.resize((16, 16), Image.Resampling.LANCZOS)
    
    # 5. Hard alpha threshold
    pixels = np.array(img)
    mask = pixels[:, :, 3] > 100
    pixels[:, :, 3] = 0
    pixels[mask, 3] = 255
    
    final_img = Image.fromarray(pixels)
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    final_img.save(output_path)
    print(f"Saved processed item icon to {output_path}")

if __name__ == "__main__":
    import sys
    process_item_icon(sys.argv[1], sys.argv[2])
