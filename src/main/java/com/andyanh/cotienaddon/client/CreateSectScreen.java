package com.andyanh.cotienaddon.client;

import com.andyanh.cotienaddon.CoTienAddon;
import com.andyanh.cotienaddon.network.SectNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

public class CreateSectScreen extends Screen {
    private static final ResourceLocation BG = ResourceLocation.fromNamespaceAndPath(CoTienAddon.MODID, "textures/gui/sect_gui.png");
    private static final int W = 256, H = 256;
    private int left, top;
    private EditBox nameInput;

    public CreateSectScreen() {
        super(Component.literal("Thành Lập Tông Môn"));
    }

    @Override
    protected void init() {
        this.left = (this.width - W) / 2;
        this.top  = (this.height - H) / 2;

        nameInput = new EditBox(font, left + 48, top + 120, W - 96, 20,
                Component.literal("Tên Tông Môn..."));
        nameInput.setMaxLength(20);
        nameInput.setHint(Component.literal("§7Nhập tên Tông Môn..."));
        addRenderableWidget(nameInput);

        addRenderableWidget(new SectButton(
                left + (W - 120) / 2, top + 160, 120, 24,
                Component.literal("§a✦ Xác Nhận"),
                b -> {
                    String name = nameInput.getValue().trim();
                    if (!name.isEmpty()) {
                        PacketDistributor.sendToServer(new SectNetwork.SectActionPacket(
                                SectNetwork.SectActionPacket.ActionType.CREATE.ordinal(), name));
                        onClose();
                    }
                }));
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {} // tắt nền mờ

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.blit(BG, left, top, 0, 0, W, H, W, H);

        int cx = left + W / 2;

        g.drawCenteredString(font,
                Component.literal("§6§l✦ THÀNH LẬP TÔNG MÔN ✦"), cx, top + 45, 0xFFD700);
        g.drawCenteredString(font,
                Component.literal("§7Tông Môn là nơi tập hợp tu tiên,"), cx, top + 68, 0xAAAAAA);
        g.drawCenteredString(font,
                Component.literal("§7cùng nhau luyện công và chiến đấu."), cx, top + 80, 0xAAAAAA);

        // Divider
        g.fill(left + 30, top + 98, left + W - 30, top + 99, 0xFF555555);

        g.drawString(font, Component.literal("§eTên Tông Môn:"), left + 48, top + 108, 0xFFFFFF);

        g.drawCenteredString(font,
                Component.literal("§7Chi phí: §e100,000 §7Nguyên Thạch"), cx, top + 196, 0xAAAAAA);

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
