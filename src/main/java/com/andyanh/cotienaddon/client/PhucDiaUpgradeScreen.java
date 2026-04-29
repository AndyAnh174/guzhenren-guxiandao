package com.andyanh.cotienaddon.client;

import com.andyanh.cotienaddon.CoTienAddon;
import com.andyanh.cotienaddon.data.CoTienData;
import com.andyanh.cotienaddon.network.UpgradePhucDiaPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

public class PhucDiaUpgradeScreen extends Screen {

    private static final ResourceLocation BG =
            ResourceLocation.fromNamespaceAndPath(CoTienAddon.MODID, "textures/gui/phuc_dia_bg.png");
    private static final ResourceLocation BTN_TEX =
            ResourceLocation.fromNamespaceAndPath(CoTienAddon.MODID, "textures/gui/button_normal.png");
    private static final ResourceLocation ROW_STRIPE =
            ResourceLocation.fromNamespaceAndPath(CoTienAddon.MODID, "textures/gui/row_stripe.png");

    private static final int W = 320;
    private static final int H = 240;

    private final CoTienData data;

    private record UpgradeEntry(String name, String desc, int level, int maxLevel,
                                double cost, int type, int color) {}

    public PhucDiaUpgradeScreen(CoTienData data) {
        super(Component.literal("Nâng Cấp Phúc Địa"));
        this.data = data;
    }

    @Override
    protected void init() {
        int left = (width - W) / 2;
        int top  = (height - H) / 2;

        UpgradeEntry[] upgrades = {
            new UpgradeEntry("☯ Cấp Bậc Phúc Địa",
                    "Mở rộng không gian, tăng grade",
                    data.phucDiaLevel, 10, data.getPhucDiaLevelUpCost(),
                    UpgradePhucDiaPacket.TYPE_LEVEL, 0xFFD700),
            new UpgradeEntry("⚗ Năng Suất Tiên Nguyên",
                    "Tăng tốc độ sản xuất Tiên Nguyên (×0.5 mỗi cấp)",
                    data.productionLevel, 5, data.getProductionUpgradeCost(),
                    UpgradePhucDiaPacket.TYPE_PRODUCTION, 0xAAFFFF),
            new UpgradeEntry("⏳ Tốc Độ Thời Gian",
                    "Tăng randomTickSpeed — cây, thảo dược lớn nhanh hơn",
                    data.timeLevel, 5, data.getTimeUpgradeCost(),
                    UpgradePhucDiaPacket.TYPE_TIME, 0xFFFF55),
            new UpgradeEntry("🛡 Phòng Hộ Thiên Kiếp",
                    "Giảm sát thương Thiên Kiếp khi xảy ra",
                    data.defenseLevel, 5, data.getDefenseUpgradeCost(),
                    UpgradePhucDiaPacket.TYPE_DEFENSE, 0xFF9944),
            new UpgradeEntry("✦ Khai Mở Linh Mạch",
                    "Tăng tốc hấp thụ Thiên/Địa Khí khi thiền trong Phúc Địa",
                    data.lingmaiLevel, 5, data.getLingmaiUpgradeCost(),
                    UpgradePhucDiaPacket.TYPE_LINGMAI, 0xDD88FF),
            new UpgradeEntry("⚒ Mở Slot Thạch Nhân",
                    "Cho phép sở hữu thêm 1 Thạch Nhân (vô hạn)",
                    data.thachnhanSlots, Integer.MAX_VALUE, data.getThachnhanSlotCost(),
                    UpgradePhucDiaPacket.TYPE_THACH_NHAN_SLOT, 0x888888),
            new UpgradeEntry("☯ Kho Địa Linh",
                    "Mở rộng kho nhận đồ từ Thạch Nhân (27→54 slot)",
                    data.dialinhStorageLevel, 3, data.getDialinhStorageCost(),
                    UpgradePhucDiaPacket.TYPE_DIALINHSTORAGE, 0x44DDAA),
        };

        int startY = top + 38;
        int rowH = 30;

        for (int i = 0; i < upgrades.length; i++) {
            UpgradeEntry u = upgrades[i];
            int rowY = startY + i * rowH;
            boolean maxed = u.maxLevel() != Integer.MAX_VALUE && u.level() >= u.maxLevel();
            boolean canAfford = !maxed && data.tienNguyen >= u.cost();
            final int type = u.type();

            String btnText = maxed ? "§7TỐI ĐA" :
                    (canAfford ? "§a▲ " : "§c✗ ") + (int)u.cost() + " TN";

            addRenderableWidget(new Button(left + W - 90, rowY + 9, 82, 16,
                    Component.literal(btnText), btn -> {
                        if (!maxed && canAfford) {
                            PacketDistributor.sendToServer(new UpgradePhucDiaPacket(type));
                            onClose();
                        }
                    }, supplier -> supplier.get()) {
                @Override
                public void renderWidget(GuiGraphics g, int mx, int my, float dt) {
                    g.blit(BTN_TEX, getX(), getY(), 0, 0, width, height, 120, 20);
                    int c = isHovered && isActive() ? 0xFFFFA0 : 0xFFFFFF;
                    g.drawCenteredString(Minecraft.getInstance().font, getMessage(),
                            getX() + width / 2, getY() + 4, c);
                }
            });
        }

        // Nút Quay lại
        addRenderableWidget(new Button(left + W / 2 - 40, top + H - 22, 80, 16,
                Component.literal("§7← Quay lại"), btn -> {
                    Minecraft.getInstance().setScreen(new PhucDiaScreen(data));
                }, supplier -> supplier.get()) {
            @Override
            public void renderWidget(GuiGraphics g, int mx, int my, float dt) {
                g.blit(BTN_TEX, getX(), getY(), 0, 0, width, height, 120, 20);
                g.drawCenteredString(Minecraft.getInstance().font, getMessage(),
                        getX() + width / 2, getY() + 4, 0xAAAAAA);
            }
        });
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {}

    @Override
    public void render(GuiGraphics g, int mx, int my, float dt) {
        int left = (width - W) / 2;
        int top  = (height - H) / 2;

        g.fill(left, top, left + W, top + H, 0xEE000011);
        g.renderOutline(left, top, W, H, 0xFFD700);
        g.drawCenteredString(font, "✦ Nâng Cấp Phúc Địa ✦", left + W / 2, top + 8, 0xFFD700);
        g.drawString(font, "§7Tiên Nguyên: §f" + String.format("%.2f", data.tienNguyen),
                left + 8, top + 22, 0xAAAAAA);

        // Vẽ các row
        UpgradeEntry[] upgrades = {
            new UpgradeEntry("☯ Cấp Bậc Phúc Địa",
                    "Mở rộng không gian, tăng grade",
                    data.phucDiaLevel, 10, data.getPhucDiaLevelUpCost(),
                    UpgradePhucDiaPacket.TYPE_LEVEL, 0xFFD700),
            new UpgradeEntry("⚗ Năng Suất Tiên Nguyên",
                    "Tăng tốc độ sản xuất Tiên Nguyên",
                    data.productionLevel, 5, data.getProductionUpgradeCost(),
                    UpgradePhucDiaPacket.TYPE_PRODUCTION, 0xAAFFFF),
            new UpgradeEntry("⏳ Tốc Độ Thời Gian",
                    "randomTickSpeed — cây & thảo dược lớn nhanh hơn",
                    data.timeLevel, 5, data.getTimeUpgradeCost(),
                    UpgradePhucDiaPacket.TYPE_TIME, 0xFFFF55),
            new UpgradeEntry("🛡 Phòng Hộ Thiên Kiếp",
                    "Giảm sát thương Thiên Kiếp",
                    data.defenseLevel, 5, data.getDefenseUpgradeCost(),
                    UpgradePhucDiaPacket.TYPE_DEFENSE, 0xFF9944),
            new UpgradeEntry("✦ Khai Mở Linh Mạch",
                    "Tăng hấp thụ Thiên/Địa Khí khi thiền",
                    data.lingmaiLevel, 5, data.getLingmaiUpgradeCost(),
                    UpgradePhucDiaPacket.TYPE_LINGMAI, 0xDD88FF),
            new UpgradeEntry("⚒ Mở Slot Thạch Nhân",
                    "Cho phép sở hữu thêm 1 Thạch Nhân (vô hạn)",
                    data.thachnhanSlots, Integer.MAX_VALUE, data.getThachnhanSlotCost(),
                    UpgradePhucDiaPacket.TYPE_THACH_NHAN_SLOT, 0x888888),
            new UpgradeEntry("☯ Kho Địa Linh",
                    "Mở rộng kho nhận đồ từ Thạch Nhân (27→54 slot)",
                    data.dialinhStorageLevel, 3, data.getDialinhStorageCost(),
                    UpgradePhucDiaPacket.TYPE_DIALINHSTORAGE, 0x44DDAA),
        };

        int startY = top + 38;
        int rowH = 30;
        for (int i = 0; i < upgrades.length; i++) {
            UpgradeEntry u = upgrades[i];
            int rowY = startY + i * rowH;

            g.blit(ROW_STRIPE, left + 6, rowY + 2, 0, 0, 236, 14, 236, 14);

            // Tên + level
            String lvlText = u.maxLevel() == Integer.MAX_VALUE ? String.valueOf(u.level()) : u.level() + "/" + u.maxLevel();
            g.drawString(font, "§f" + u.name(), left + 10, rowY + 4, u.color());
            g.drawString(font, "§7Cấp: §e" + lvlText, left + 10, rowY + 16, 0xAAAAAA);
            g.drawString(font, "§8" + u.desc(), left + 10, rowY + 26, 0x666666);

            // Progress bar (nhỏ)
            int barW = 120;
            int filled = u.maxLevel() > 0 ? (int)((double)u.level() / u.maxLevel() * barW) : 0;
            g.fill(left + W - 92, rowY + 3, left + W - 92 + barW, rowY + 8, 0xFF333333);
            if (filled > 0) g.fill(left + W - 92, rowY + 3, left + W - 92 + filled, rowY + 8, u.color() | 0xFF000000);
        }

        super.render(g, mx, my, dt);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
