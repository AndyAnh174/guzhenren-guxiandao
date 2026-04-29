package com.andyanh.cotienaddon.client;

import com.andyanh.cotienaddon.entity.ThachNhanEntity;
import com.andyanh.cotienaddon.network.ThachNhanActionPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

public class ThachNhanUpgradeScreen extends Screen {

    private final ThachNhanEntity entity;
    private static final int W = 200, H = 180;

    public ThachNhanUpgradeScreen(ThachNhanEntity entity) {
        super(Component.literal("Thạch Nhân — Nâng Cấp"));
        this.entity = entity;
    }

    @Override
    protected void init() {
        int left = (width - W) / 2;
        int top  = (height - H) / 2;

        // Upgrade HP
        addRenderableWidget(Button.builder(Component.literal("Nâng HP"), b -> upgrade(ThachNhanActionPacket.ACTION_HP))
                .bounds(left + 10, top + 60, 80, 18).build());
        // Upgrade ATK
        addRenderableWidget(Button.builder(Component.literal("Nâng Công"), b -> upgrade(ThachNhanActionPacket.ACTION_ATK))
                .bounds(left + 10, top + 85, 80, 18).build());
        // Upgrade SPD
        addRenderableWidget(Button.builder(Component.literal("Nâng Tốc Đào"), b -> upgrade(ThachNhanActionPacket.ACTION_SPD))
                .bounds(left + 10, top + 110, 80, 18).build());
        // Dismiss
        addRenderableWidget(Button.builder(Component.literal("§cGiải Tán"), b -> upgrade(ThachNhanActionPacket.ACTION_DISMISS))
                .bounds(left + W - 90, top + H - 25, 80, 18).build());
    }

    private void upgrade(int action) {
        PacketDistributor.sendToServer(new ThachNhanActionPacket(entity.getId(), action));
        onClose();
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float delta) {
        renderBackground(g, mx, my, delta);
        int left = (width - W) / 2;
        int top  = (height - H) / 2;

        // Background
        g.fill(left, top, left + W, top + H, 0xCC111122);
        g.renderOutline(left, top, W, H, 0xFF8866AA);

        // Title
        g.drawCenteredString(font, "§6⚒ Thạch Nhân — Nâng Cấp", left + W/2, top + 8, 0xFFFFFF);
        g.drawString(font, "§7Cấp bậc: §f" + entity.getSkinLevel() + "/5", left + 10, top + 26, 0xFFFFFF);

        // Stats
        int hp = entity.getHpLevel(), atk = entity.getAtkLevel(), spd = entity.getSpdLevel();
        g.drawString(font, "§c❤ HP: §f" + (int)entity.getMaxHp() + " §7(cấp " + hp + "/4) — §eTốn §f"
                + costStr(hp) + " TN", left + 10, top + 48, 0xFFFFFF);
        g.drawString(font, "§c⚔ Công: §f" + (int)entity.getAttackDmg() + " §7(cấp " + atk + "/4) — §eTốn §f"
                + costStr(atk) + " TN", left + 10, top + 73, 0xFFFFFF);
        g.drawString(font, "§b⛏ Tốc đào: §f" + entity.getMiningSpeed() + " tick §7(cấp " + spd + "/4) — §eTốn §f"
                + costStr(spd) + " TN", left + 10, top + 98, 0xFFFFFF);

        // Inventory summary
        g.drawString(font, "§aKho Thạch Nhân:", left + 10, top + 130, 0xFFFFFF);
        int count = 0;
        for (int i = 0; i < entity.getInventory().getContainerSize(); i++) {
            if (!entity.getInventory().getItem(i).isEmpty()) count++;
        }
        g.drawString(font, count + "/9 slot đang dùng", left + 10, top + 142, 0xAAAAAA);

        super.render(g, mx, my, delta);
    }

    private String costStr(int lvl) {
        if (lvl >= 4) return "§7MAX";
        return String.valueOf(ThachNhanEntity.getUpgradeCost(lvl));
    }

    @Override public boolean isPauseScreen() { return false; }
}
