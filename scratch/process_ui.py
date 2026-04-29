import os
from PIL import Image, ImageOps
import numpy as np

def process_frame(img_path, output_path, target_size=(180, 240)):
    # 1. Open and convert to RGBA
    img = Image.open(img_path).convert("RGBA")
    
    # 2. Hard crop to content (alpha > 0)
    np_img = np.array(img)
    alpha = np_img[:, :, 3]
    coords = np.argwhere(alpha > 0)
    if coords.size > 0:
        y_min, x_min = coords.min(axis=0)
        y_max, x_max = coords.max(axis=0)
        img = img.crop((x_min, y_min, x_max + 1, y_max + 1))
    
    # 3. Resize to exact target size
    img = img.resize(target_size, Image.Resampling.LANCZOS)
    
    # 4. Hard alpha threshold
    pixels = np.array(img)
    mask = pixels[:, :, 3] > 128
    pixels[:, :, 3] = 0
    pixels[mask, 3] = 255
    
    final_img = Image.fromarray(pixels)
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    final_img.save(output_path)
    print(f"Saved frame to {output_path}")

def process_button(img_path, output_path):
    img = Image.open(img_path).convert("RGBA")
    
    # Create 128x64 sheet
    sheet = Image.new("RGBA", (128, 64), (0, 0, 0, 0))
    
    # State 1 (Normal): 120x24 (centered in 128x32 top half)
    btn_normal = img.resize((120, 24), Image.Resampling.LANCZOS)
    
    # State 2 (Hover): 120x24 (centered in 128x32 bottom half)
    btn_hover = btn_normal.point(lambda p: min(255, int(p * 1.3))) # Brighter
    
    # Paste centered
    sheet.paste(btn_normal, (4, 4), btn_normal)
    sheet.paste(btn_hover, (4, 36), btn_hover)
    
    # Hard alpha threshold
    pixels = np.array(sheet)
    mask = pixels[:, :, 3] > 128
    pixels[:, :, 3] = 0
    pixels[mask, 3] = 255
    
    final_img = Image.fromarray(pixels)
    final_img.save(output_path)
    print(f"Saved button to {output_path}")

if __name__ == "__main__":
    from PIL import Image
    # Resampling is needed
    
    # Long frame
    process_frame(r"C:\Users\ADMIN\.gemini\antigravity\brain\539a2e00-f1fe-479a-a18c-538b86ce0ae6\sect_gui_frame_long_1777295329189.png", 
                  r"C:\moddev\guxiandao\src\main\resources\assets\cotienaddon\textures\gui\sect_screen.png",
                  (180, 240))
    
    # Button
    process_button(r"C:\Users\ADMIN\.gemini\antigravity\brain\539a2e00-f1fe-479a-a18c-538b86ce0ae6\sect_gui_button_1777295346035.png",
                   r"C:\moddev\guxiandao\src\main\resources\assets\cotienaddon\textures\gui\sect_button.png")
