package com.andyanh.cotienaddon.client;
 
import com.andyanh.cotienaddon.CoTienAddon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
 
public class SectButton extends AbstractButton {
    private static final ResourceLocation BTN_TEX = ResourceLocation.fromNamespaceAndPath(CoTienAddon.MODID, "textures/gui/sect_button.png");
    private final OnPress onPress;
 
    public SectButton(int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message);
        this.onPress = onPress;
    }
 
    @Override
    public void onPress() {
        this.onPress.onPress(this);
    }
 
    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int v = this.isHoveredOrFocused() ? 32 : 0;
        // Drawing 120x24 slice with 4px offset from 128x64 sheet
        guiGraphics.blit(BTN_TEX, this.getX(), this.getY(), 4, v + 4, this.width, this.height, 128, 64);
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, 0xFFFFFF);
    }
 
    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }
 
    public interface OnPress {
        void onPress(SectButton button);
    }
}
