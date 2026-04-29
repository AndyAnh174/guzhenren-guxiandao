from PIL import Image
import os

src = r"C:\Users\ADMIN\.gemini\antigravity\brain\539a2e00-f1fe-479a-a18c-538b86ce0ae6\sect_gui_frame_1777294916647.png"
dst = r"C:\moddev\guxiandao\src\main\resources\assets\cotienaddon\textures\gui\sect_gui.png"

img = Image.open(src)
img = img.resize((256, 256), Image.LANCZOS)
img.save(dst)
print(f"Saved {dst}")
