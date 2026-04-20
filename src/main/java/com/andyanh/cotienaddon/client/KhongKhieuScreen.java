package com.andyanh.cotienaddon.client;

import com.andyanh.cotienaddon.CoTienAddon;
import com.andyanh.cotienaddon.data.CoTienData;
import com.andyanh.cotienaddon.network.ThangTienRequestPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

public class KhongKhieuScreen extends Screen {

    private static final ResourceLocation BG =
            ResourceLocation.fromNamespaceAndPath(CoTienAddon.MODID, "textures/gui/phuc_dia_bg.png");

    private static final int W = 256;
    private static final int H = 220;

    private final CoTienData data;

    public KhongKhieuScreen(CoTienData data) {
        super(Component.translatable("gui.cotienaddon.khong_khieu.title"));
        this.data = data;
    }

    @Override
    protected void init() {
        super.init();
        int left = (width - W) / 2;
        int top  = (height - H) / 2;

        boolean canAscend = data.thangTienPhase == 0;
        Button dotPhaButton = Button.builder(
                Component.translatable("gui.cotienaddon.dot_pha"),
                btn -> { PacketDistributor.sendToServer(new ThangTienRequestPacket()); onClose(); }
        ).bounds(left + W / 2 - 60, top + H - 40, 120, 20).build();
        dotPhaButton.active = canAscend;
        addRenderableWidget(dotPhaButton);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float delta) {
        renderBackground(g, mx, my, delta);

        int left = (width - W) / 2;
        int top  = (height - H) / 2;

        // Background texture (256x256 panel)
        g.blit(BG, left, top, 0, 0, W, H, 256, 256);

        int cx   = left + W / 2;
        int lineY = top + 30;

        // Title
        g.drawCenteredString(font, title, cx, lineY, 0xFFD700);
        lineY += 18;

        // Nhân Khí
        g.drawString(font,
                Component.translatable("gui.cotienaddon.nhan_khi",
                        String.format("%.0f", data.calcNhanKhi())),
                left + 20, lineY, 0xE0E0E0);
        lineY += 13;

        // Grade dự kiến
        String gradeKey = "gui.cotienaddon.grade." + data.calcPhucDiaGrade();
        g.drawString(font,
                Component.translatable("gui.cotienaddon.phuc_dia_grade",
                        Component.translatable(gradeKey)),
                left + 20, lineY, 0xE0E0E0);
        lineY += 13;

        // Gu usage
        g.drawString(font,
                Component.literal(String.format("T1=%.0f  T2=%.0f  T3=%.0f  T4=%.0f  T5=%.0f",
                        data.guUsed_tier1, data.guUsed_tier2, data.guUsed_tier3,
                        data.guUsed_tier4, data.guUsed_tier5)),
                left + 20, lineY, 0xAAAAAA);
        lineY += 18;

        // Phase status
        g.drawCenteredString(font,
                Component.translatable("gui.cotienaddon.phase." + data.thangTienPhase),
                cx, lineY, 0x55FF55);
        lineY += 16;

        // Warning
        if (data.thangTienPhase == 0) {
            g.drawCenteredString(font,
                    Component.translatable("gui.cotienaddon.warning_irreversible"),
                    cx, lineY, 0xFF5555);
        }

        super.render(g, mx, my, delta);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
