package com.andyanh.cotienaddon.client;

import com.andyanh.cotienaddon.CoTienAddon;
import com.andyanh.cotienaddon.data.CoTienData;
import com.andyanh.cotienaddon.network.SetTonHieuPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

public class TienTonScreen extends Screen {

    private static final ResourceLocation BG =
            ResourceLocation.fromNamespaceAndPath(CoTienAddon.MODID, "textures/gui/tien_ton_bg.png");
    private static final ResourceLocation BTN_TEX =
            ResourceLocation.fromNamespaceAndPath(CoTienAddon.MODID, "textures/gui/button_normal.png");

    private static final int W = 256, H = 220;

    private static final int[][] COLORS = {
        {0xFFD700}, {0xFF4444}, {0x55FFFF}, {0xAA55FF},
        {0x55FF55}, {0xFF9900}, {0xFF88CC}, {0xFFFFFF}
    };
    private static final String[] COLOR_NAMES = {"Vàng","Đỏ","Xanh","Tím","Lá","Cam","Hồng","Trắng"};

    private final CoTienData data;
    private EditBox nameInput;
    private int selectedColor;

    public TienTonScreen(CoTienData data) {
        super(Component.literal("✦ Danh Hiệu Tôn ✦"));
        this.data = data;
        this.selectedColor = data.tonHieuColor;
    }

    @Override
    protected void init() {
        super.init();
        int left = (width - W) / 2;
        int top  = (height - H) / 2;

        // Name input
        nameInput = new EditBox(font, left + 12, top + 72, W - 24, 16,
                Component.literal("Tên danh hiệu..."));
        nameInput.setMaxLength(20);
        nameInput.setValue(data.tonHieuName);
        nameInput.setHint(Component.literal("§7Nhập tên danh hiệu..."));
        addRenderableWidget(nameInput);

        // Color picker — 8 ô, mỗi ô 12px, canh giữa
        int totalW = COLORS.length * 13;
        int cpX = left + (W - totalW) / 2;
        int cpY = top + 104;
        for (int i = 0; i < COLORS.length; i++) {
            final int rgb = COLORS[i][0];
            addRenderableWidget(new Button(cpX + i * 13, cpY, 12, 16,
                    Component.literal(""), b -> selectedColor = rgb,
                    supplier -> supplier.get()) {
                @Override
                public void renderWidget(GuiGraphics g, int mx, int my, float dt) {
                    int border = selectedColor == rgb ? 0xFFFFFFFF : 0xFF444444;
                    g.fill(getX()-1, getY()-1, getX()+width+1, getY()+height+1, border);
                    g.fill(getX(), getY(), getX()+width, getY()+height, rgb | 0xFF000000);
                }
            });
        }

        // Nút Xác nhận
        addRenderableWidget(makeBtn(
                Component.literal("§a✦ Xác nhận"),
                left + W / 2 - 95, top + 135, 90, 18,
                b -> {
                    String name = nameInput.getValue().trim();
                    PacketDistributor.sendToServer(new SetTonHieuPacket(name, selectedColor, !name.isEmpty()));
                    onClose();
                }));

        // Nút Xóa danh hiệu
        addRenderableWidget(makeBtn(
                Component.literal("§7✗ Xóa danh hiệu"),
                left + W / 2 + 5, top + 135, 90, 18,
                b -> {
                    PacketDistributor.sendToServer(new SetTonHieuPacket("", 0xFFD700, false));
                    onClose();
                }));

        // Nút Quay lại
        addRenderableWidget(makeBtn(
                Component.literal("§7← Quay lại"),
                left + 8, top + H - 24, 70, 16,
                b -> Minecraft.getInstance().setScreen(new KhongKhieuScreen(data))));
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {}

    @Override
    public void render(GuiGraphics g, int mx, int my, float delta) {
        int left = (width - W) / 2;
        int top  = (height - H) / 2;

        g.blit(BG, left, top, 0, 0, W, H, 256, 256);

        int cx = left + W / 2;
        double daode = data.daode;
        boolean isTien = daode >= 0;

        // Title
        g.drawCenteredString(font,
                Component.literal("✦ DANH HIỆU TÔN ✦").withStyle(s -> s.withColor(0xFFD700)),
                cx, top + 18, 0xFFD700);

        // Loại Tôn
        Component typeComp = isTien
                ? Component.literal("☯ TIÊN TÔN").withStyle(s -> s.withColor(0x55FFFF))
                : Component.literal("☠ MA TÔN").withStyle(s -> s.withColor(0xFF4444));
        g.drawCenteredString(font, typeComp, cx, top + 32, 0xFFFFFF);

        // Đạo đức
        String daodeStr = String.format("Đạo Đức: %+.0f", daode);
        g.drawCenteredString(font, Component.literal("§7" + daodeStr), cx, top + 44, 0xAAAAAA);

        // Divider
        g.fill(left + 12, top + 56, left + W - 12, top + 57, 0xFF555555);

        // Label nhập tên
        g.drawString(font, Component.literal("§eNhập tên danh hiệu:"), left + 12, top + 62, 0xFFFFFF);

        // Label màu sắc
        g.drawString(font, Component.literal("§eMàu sắc:"), left + 12, top + 95, 0xFFFFFF);

        // Preview
        String name = nameInput != null ? nameInput.getValue().trim() : data.tonHieuName;
        if (!name.isEmpty()) {
            String typeName = isTien ? "Tiên Tôn" : "Ma Tôn";
            Component preview = Component.literal(name + " " + typeName)
                    .withStyle(s -> s.withColor(TextColor.fromRgb(selectedColor)));
            g.drawString(font, Component.literal("§7Xem trước: ").append(preview),
                    left + 12, top + 158, 0xFFFFFF);
        }

        // Hiện tên màu đang chọn
        for (int i = 0; i < COLORS.length; i++) {
            if (COLORS[i][0] == selectedColor) {
                g.drawCenteredString(font,
                        Component.literal("§7Màu: " + COLOR_NAMES[i]),
                        cx, top + 125, 0xAAAAAA);
                break;
            }
        }

        // Thông báo nếu không đủ điều kiện
        var gv = Minecraft.getInstance().player.getData(net.guzhenren.network.GuzhenrenModVariables.PLAYER_VARIABLES);
        boolean eligible = (gv.zhuanshu >= 9) || (gv.zhuanshu >= 8 && gv.jieduan >= 4);
        if (!eligible) {
            g.drawCenteredString(font,
                    Component.literal("§c⚠ Cần Bát Chuyển Đỉnh Phong trở lên"),
                    cx, top + 180, 0xFF5555);
        }

        super.render(g, mx, my, delta);
    }

    private Button makeBtn(Component label, int x, int y, int w, int h, Button.OnPress onPress) {
        return new Button(x, y, w, h, label, onPress, supplier -> supplier.get()) {
            @Override
            public void renderWidget(GuiGraphics g, int mx, int my, float delta) {
                g.blit(BTN_TEX, getX(), getY(), 0, 0, width, height, 120, 20);
                int col = isHovered ? 0xFFFFA0 : 0xFFFFFF;
                g.drawCenteredString(Minecraft.getInstance().font, getMessage(),
                        getX() + width / 2, getY() + (height - 8) / 2, col);
            }
        };
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
