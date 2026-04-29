package com.andyanh.cotienaddon.client;

import com.andyanh.cotienaddon.CoTienAddon;
import com.andyanh.cotienaddon.data.CoTienData;
import com.andyanh.cotienaddon.network.CallDialinhPacket;
import com.andyanh.cotienaddon.network.EcosystemPacket;
import com.andyanh.cotienaddon.network.ManageMemberPacket;
import com.andyanh.cotienaddon.network.TeleportPhucDiaPacket;
import com.andyanh.cotienaddon.network.UpdatePermissionPacket;
import com.andyanh.cotienaddon.network.WithdrawTienNguyenPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class PhucDiaScreen extends Screen {

    private static final ResourceLocation BG =
            ResourceLocation.fromNamespaceAndPath(CoTienAddon.MODID, "textures/gui/main_panel.png");
    private static final ResourceLocation BTN_TEX =
            ResourceLocation.fromNamespaceAndPath(CoTienAddon.MODID, "textures/gui/button_normal.png");
    private static final ResourceLocation TAB_ACTIVE =
            ResourceLocation.fromNamespaceAndPath(CoTienAddon.MODID, "textures/gui/tab_active.png");
    private static final ResourceLocation TAB_INACTIVE =
            ResourceLocation.fromNamespaceAndPath(CoTienAddon.MODID, "textures/gui/tab_inactive.png");
    private static final ResourceLocation ROW_STRIPE =
            ResourceLocation.fromNamespaceAndPath(CoTienAddon.MODID, "textures/gui/row_stripe.png");

    private final CoTienData data;
    private int activeTab = 0;
    private int ecoSubView = 0;

    private static final int W = 256;
    private static final int H = 220;

    // Tab 2 state
    private EditBox inviteBox;
    private int memberScrollOffset = 0;

    public PhucDiaScreen(CoTienData data) {
        super(Component.translatable("gui.cotienaddon.phuc_dia.title"));
        this.data = data;
    }

    @Override
    protected void init() {
        int left = (width - W) / 2;
        int top = (height - H) / 2;

        // Tab buttons — textured, active/inactive texture swaps each frame
        addRenderableWidget(makeTabButton(Component.translatable("gui.cotienaddon.phuc_dia.tab.overview"),  0, left + 14,   top + 24, b -> switchTab(0)));
        addRenderableWidget(makeTabButton(Component.translatable("gui.cotienaddon.phuc_dia.tab.members"),   1, left + 90,  top + 24, b -> switchTab(1)));
        addRenderableWidget(makeTabButton(Component.translatable("gui.cotienaddon.phuc_dia.tab.ecosystem"), 2, left + 166, top + 24, b -> switchTab(2)));

        buildTab(left, top);
    }

    private void switchTab(int tab) {
        if (activeTab != tab) ecoSubView = 0;
        activeTab = tab;
        clearWidgets();
        init();
    }

    private Button makeTabButton(Component label, int tabIdx, int x, int y, Button.OnPress onPress) {
        return new Button(x, y, 76, 14, label, onPress, supplier -> supplier.get()) {
            @Override
            public void renderWidget(GuiGraphics g, int mx, int my, float delta) {
                ResourceLocation tex = (activeTab == tabIdx) ? TAB_ACTIVE : TAB_INACTIVE;
                g.blit(tex, getX(), getY(), 0, 0, 76, 14, 76, 14);
                int color = (activeTab == tabIdx) ? 0xFFFFFF : 0xAAAAAA;
                g.drawCenteredString(Minecraft.getInstance().font, getMessage(),
                        getX() + 38, getY() + 3, color);
            }
        };
    }

    private Button makeTexturedButton(Component label, int x, int y, int w, int h, Button.OnPress onPress) {
        return new Button(x, y, w, h, label, onPress, supplier -> supplier.get()) {
            @Override
            public void renderWidget(GuiGraphics g, int mx, int my, float delta) {
                g.blit(BTN_TEX, getX(), getY(), 0, 0, width, height, 120, 20);
                int color = isHovered ? 0xFFFFA0 : 0xFFFFFF;
                g.drawCenteredString(Minecraft.getInstance().font, getMessage(),
                        getX() + width / 2, getY() + (height - 8) / 2, color);
            }
        };
    }

    private void buildTab(int left, int top) {
        int contentTop = top + 42;
        switch (activeTab) {
            case 0 -> buildOverviewTab(left, contentTop);
            case 1 -> buildMembersTab(left, contentTop);
            case 2 -> buildEcosystemTab(left, contentTop);
        }
    }

    // --- Tab 0: Tổng quan ---
    private void buildOverviewTab(int left, int top) {
        // Nút Enter/Exit Phúc Địa
        String dimKey = Minecraft.getInstance().level != null
                ? Minecraft.getInstance().level.dimension().location().toString()
                : "";
        boolean inPhucDia = dimKey.startsWith("cotienaddon:phuc_dia");

        // Nút mở panel Nâng Cấp
        addRenderableWidget(makeTexturedButton(
                Component.literal("§e⚙ Nâng Cấp Phúc Địa"),
                left + W / 2 - 70, top + 90, 140, 18,
                b -> Minecraft.getInstance().setScreen(new PhucDiaUpgradeScreen(data))));

        // Nút Gọi Địa Linh / Đến Địa Linh (chỉ hiện khi trong Phúc Địa)
        if (inPhucDia) {
            addRenderableWidget(makeTexturedButton(
                    Component.literal("§d☯ Gọi Địa Linh"),
                    left + W / 2 - 70, top + 110, 66, 16,
                    b -> { PacketDistributor.sendToServer(new CallDialinhPacket(true)); onClose(); }));
            addRenderableWidget(makeTexturedButton(
                    Component.literal("§d→ Đến Địa Linh"),
                    left + W / 2 + 2, top + 110, 66, 16,
                    b -> { PacketDistributor.sendToServer(new CallDialinhPacket(false)); onClose(); }));
        }

        // Nút rút Tiên Nguyên (luôn hiện nếu có)
        if (data.tienNguyen >= 1.0) {
            int tnAmount = (int) data.tienNguyen;
            addRenderableWidget(makeTexturedButton(
                    Component.literal("§b↓ Rút " + tnAmount + " Tiên Nguyên"),
                    left + W / 2 - 70, top + 128, 140, 16,
                    b -> {
                        PacketDistributor.sendToServer(new WithdrawTienNguyenPacket(tnAmount));
                        onClose();
                    }));
        }

        int btnY = top + 150;
        if (inPhucDia) {
            addRenderableWidget(makeTexturedButton(
                    Component.translatable("gui.cotienaddon.phuc_dia.btn.exit"),
                    left + W / 2 - 60, btnY, 120, 20,
                    b -> { PacketDistributor.sendToServer(new TeleportPhucDiaPacket(false)); onClose(); }));
        } else if (data.thangTienPhase >= 4) {
            addRenderableWidget(makeTexturedButton(
                    Component.translatable("gui.cotienaddon.phuc_dia.btn.enter"),
                    left + W / 2 - 60, btnY, 120, 20,
                    b -> { PacketDistributor.sendToServer(new TeleportPhucDiaPacket(true)); onClose(); }));
        }
    }

    // --- Tab 1: Quản lý Khách ---
    private void buildMembersTab(int left, int top) {
        // Invite box
        inviteBox = new EditBox(font, left + 5, top, W - 90, 18,
                Component.translatable("gui.cotienaddon.phuc_dia.invite_hint"));
        inviteBox.setMaxLength(40);
        addRenderableWidget(inviteBox);

        // Nút Mời
        addRenderableWidget(Button.builder(Component.translatable("gui.cotienaddon.phuc_dia.btn.invite"), b -> {
            String name = inviteBox.getValue().trim();
            if (!name.isEmpty()) {
                PacketDistributor.sendToServer(new ManageMemberPacket(ManageMemberPacket.ACTION_INVITE, name));
                inviteBox.setValue("");
            }
        }).bounds(left + W - 80, top, 75, 18).build());

        // Danh sách member với scroll
        List<String> wl = data.whitelist;
        int rowH = 22;
        int maxVisible = 5;
        for (int i = memberScrollOffset; i < Math.min(wl.size(), memberScrollOffset + maxVisible); i++) {
            final String memberUUID = wl.get(i);
            int rowY = top + 25 + (i - memberScrollOffset) * rowH;

            // Permission toggles (5 bits) — nhãn viết tắt rõ ràng
            int perms = data.permissions.getOrDefault(memberUUID, 0);
            int[] bits = {CoTienData.PERM_BUILD, CoTienData.PERM_CONTAINERS,
                    CoTienData.PERM_COMBAT, CoTienData.PERM_CORE, CoTienData.PERM_MANAGE};
            String[] labels = {"XD", "HM", "CT", "CC", "QL"};
            String[] tooltips = {"Xây Dựng", "Hòm Đồ", "Chiến Đấu", "Cốt Lõi", "Quản Lý"};

            for (int b = 0; b < bits.length; b++) {
                final int bit = bits[b];
                boolean on = (perms & bit) != 0;
                int btnX = left + 5 + b * 24;
                String label = (on ? "§a" : "§c") + labels[b];
                addRenderableWidget(Button.builder(Component.literal(label), btn -> {
                    boolean newVal = (data.permissions.getOrDefault(memberUUID, 0) & bit) == 0;
                    PacketDistributor.sendToServer(new UpdatePermissionPacket(memberUUID, bit, newVal));
                    data.setPermission(java.util.UUID.fromString(memberUUID), bit, newVal);
                    switchTab(1);
                }).bounds(btnX, rowY, 22, 18).tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal(tooltips[b]))).build());
            }

            // Nút Kick
            addRenderableWidget(Button.builder(Component.translatable("gui.cotienaddon.phuc_dia.btn.kick"), b2 -> {
                PacketDistributor.sendToServer(new ManageMemberPacket(ManageMemberPacket.ACTION_KICK, memberUUID));
                data.whitelist.remove(memberUUID);
                switchTab(1);
            }).bounds(left + W - 45, rowY, 40, 18).build());
        }

        // Scroll buttons
        if (memberScrollOffset > 0) {
            addRenderableWidget(Button.builder(Component.literal("▲"), b -> {
                memberScrollOffset = Math.max(0, memberScrollOffset - 1);
                switchTab(1);
            }).bounds(left + W - 15, top + 25, 12, 12).build());
        }
        if (memberScrollOffset + maxVisible < wl.size()) {
            addRenderableWidget(Button.builder(Component.literal("▼"), b -> {
                memberScrollOffset++;
                switchTab(1);
            }).bounds(left + W - 15, top + 25 + maxVisible * rowH - 14, 12, 12).build());
        }
    }

    // --- Tab 2: Hệ sinh thái ---
    private void buildEcosystemTab(int left, int top) {
        if (ecoSubView == 1) {
            buildCreatureListSubView(left, top);
            return;
        } else if (ecoSubView == 2) {
            buildThachNhanTab(left, top);
            return;
        } else if (ecoSubView == 3) {
            buildDialinhSubView(left, top);
            return;
        }
        int y = top + 10;
        int btnW = 110;

        // Toggle: Cố định Ban Ngày
        boolean fixedDay = data.ecoFixedDay;
        addRenderableWidget(makeTexturedButton(
                Component.literal((fixedDay ? "§a✔ " : "§7✘ ") + "Cố định Ban Ngày"),
                left + W / 2 - btnW / 2, y, btnW, 16,
                b -> PacketDistributor.sendToServer(new EcosystemPacket(EcosystemPacket.FIXED_DAY, !fixedDay))));
        y += 22;

        // Toggle: Cho phép Mưa
        boolean allowRain = data.ecoAllowRain;
        addRenderableWidget(makeTexturedButton(
                Component.literal((allowRain ? "§a✔ " : "§7✘ ") + "Cho phép Mưa"),
                left + W / 2 - btnW / 2, y, btnW, 16,
                b -> PacketDistributor.sendToServer(new EcosystemPacket(EcosystemPacket.ALLOW_RAIN, !allowRain))));
        y += 22;

        // Toggle: Sinh vật Hòa Bình
        boolean peaceMobs = data.ecoPeacefulMobs;
        addRenderableWidget(makeTexturedButton(
                Component.literal((peaceMobs ? "§a✔ " : "§7✘ ") + "Sinh vật Hòa Bình"),
                left + W / 2 - btnW / 2, y, btnW, 16,
                b -> PacketDistributor.sendToServer(new EcosystemPacket(EcosystemPacket.PEACEFUL_MOBS, !peaceMobs))));
        y += 22;

        // Toggle: Quái Cổ Chân Nhân
        boolean guzhenrenMobs = data.ecoGuzhenrenMobs;
        addRenderableWidget(makeTexturedButton(
                Component.literal((guzhenrenMobs ? "§a✔ " : "§7✘ ") + "Quái Cổ Chân Nhân"),
                left + W / 2 - btnW / 2, y, btnW, 16,
                b -> PacketDistributor.sendToServer(new EcosystemPacket(EcosystemPacket.GUZHENREN_MOBS, !guzhenrenMobs))));
        y += 30;

        // Thông tin khu vực
        int grade = data.phucDiaGrade;
        String[] gradeNames = {"", "Hạ đẳng", "Trung đẳng", "Thượng đẳng", "Siêu đẳng"};
        String oreInfo = grade >= 4 ? "§aQuặng hiếm + Thiên Kiếp hardcore"
                       : grade >= 3 ? "§eQuặng hiếm tự sinh" : "§7—";
        // "Sinh vật ở phúc địa" button
        addRenderableWidget(makeTexturedButton(
                Component.literal("§a🐾 Sinh vật ở Phúc Địa"),
                left + W / 2 - btnW / 2, y, btnW, 16,
                b -> { ecoSubView = 1; clearWidgets(); init(); }));
    }
    
    private void buildCreatureListSubView(int left, int top) {
        addRenderableWidget(makeTexturedButton(
                Component.literal("§7← Trở lại"),
                left + 10, top - 10, 60, 16,
                b -> { ecoSubView = 0; clearWidgets(); init(); }));

        addRenderableWidget(makeTexturedButton(
                Component.literal("§8⚒ Thạch Nhân"),
                left + W / 2 - 60, top + 20, 120, 20,
                b -> { ecoSubView = 2; clearWidgets(); init(); }));

        addRenderableWidget(makeTexturedButton(
                Component.literal("§2☯ Địa Linh"),
                left + W / 2 - 60, top + 45, 120, 20,
                b -> { ecoSubView = 3; clearWidgets(); init(); }));
    }

    private void buildDialinhSubView(int left, int top) {
        addRenderableWidget(makeTexturedButton(
                Component.literal("§7← Trở lại"),
                left + 10, top - 10, 60, 16,
                b -> { ecoSubView = 1; clearWidgets(); init(); }));

        // Nâng cấp Sức Mạnh (DMG)
        double dmgCost = data.getDialinhSkillCost(data.dialinhSkillDamage);
        boolean canDmg = data.dialinhSkillDamage < 10 && data.tienNguyen >= dmgCost;
        String dmgLabel = data.dialinhSkillDamage >= 10
                ? "§c⚔ DMG MAX"
                : (canDmg ? "§c⚔ DMG Lv" + (data.dialinhSkillDamage + 1) + " (" + (int)dmgCost + " TN)"
                          : "§8⚔ DMG (" + (int)dmgCost + " TN)");
        addRenderableWidget(makeTexturedButton(
                Component.literal(dmgLabel),
                left + W / 2 - 105, top + 15, 100, 18,
                b -> {
                    PacketDistributor.sendToServer(
                        new com.andyanh.cotienaddon.network.UpgradePhucDiaPacket(
                            com.andyanh.cotienaddon.network.UpgradePhucDiaPacket.TYPE_DIALINH_SKILL_DMG));
                    onClose();
                }));

        // Nâng cấp Sinh Lực (HP)
        double hpCost = data.getDialinhSkillCost(data.dialinhSkillHp);
        boolean canHp = data.dialinhSkillHp < 10 && data.tienNguyen >= hpCost;
        String hpLabel = data.dialinhSkillHp >= 10
                ? "§a❤ HP MAX"
                : (canHp ? "§a❤ HP Lv" + (data.dialinhSkillHp + 1) + " (" + (int)hpCost + " TN)"
                          : "§8❤ HP (" + (int)hpCost + " TN)");
        addRenderableWidget(makeTexturedButton(
                Component.literal(hpLabel),
                left + W / 2 + 5, top + 15, 100, 18,
                b -> {
                    PacketDistributor.sendToServer(
                        new com.andyanh.cotienaddon.network.UpgradePhucDiaPacket(
                            com.andyanh.cotienaddon.network.UpgradePhucDiaPacket.TYPE_DIALINH_SKILL_HP));
                    onClose();
                }));

        // Nâng cấp Kho (Storage)
        int storeLvl = data.dialinhStorageLevel;
        if (storeLvl < 3) {
            double storeCost = data.getDialinhStorageCost();
            boolean canStore = data.tienNguyen >= storeCost;
            int nextSlots = 27 + 9 * (storeLvl + 1);
            addRenderableWidget(makeTexturedButton(
                    Component.literal(canStore
                        ? "§e📦 Kho " + nextSlots + " (" + (int)storeCost + " TN)"
                        : "§8📦 Kho (" + (int)storeCost + " TN)"),
                    left + W / 2 - 60, top + 40, 120, 18,
                    b -> {
                        PacketDistributor.sendToServer(
                            new com.andyanh.cotienaddon.network.UpgradePhucDiaPacket(
                                com.andyanh.cotienaddon.network.UpgradePhucDiaPacket.TYPE_DIALINHSTORAGE));
                        onClose();
                    }));
        }
    }

    private void renderDialinhContent(GuiGraphics g, int left, int top) {
        g.drawCenteredString(font, "§2☯ Quản Lý Địa Linh ☯", left + W / 2, top - 5, 0x55FF55);
        int y = top + 70;
        double hp = 100000.0 + data.phucDiaLevel * 10000.0 + data.dialinhSkillHp * 50000.0;
        double dmg = 40.0 + data.phucDiaLevel * 3.0 + data.productionLevel * 2.0 + data.dialinhSkillDamage * 20.0;
        int slots = 27 + 9 * data.dialinhStorageLevel;
        g.drawString(font, "§c⚔ Sức Mạnh: §f" + String.format("%.0f", dmg) + " §7(Lv" + data.dialinhSkillDamage + "/10)", left + 14, y, 0xFFAAAA);
        g.drawString(font, "§a❤ Sinh Lực: §f" + String.format("%.0f", hp) + " §7(Lv" + data.dialinhSkillHp + "/10)", left + 14, y + 14, 0xAAFFAA);
        g.drawString(font, "§e📦 Kho Đồ: §f" + slots + " ô §7(Lv" + data.dialinhStorageLevel + "/3)", left + 14, y + 28, 0xFFFFAA);
        if (!data.dialinhBondComplete) {
            g.drawString(font, "§c⚠ Cần hoàn thành Nhiệm Vụ Thân Thiết!", left + 14, y + 48, 0xFF5555);
            g.drawString(font, "§7  Shift+Click vào Địa Linh để bắt đầu.", left + 14, y + 60, 0x888888);
        } else {
            g.drawString(font, "§d✦ Đã nhận chủ — có thể nâng cấp!", left + 14, y + 48, 0xDD88FF);
        }
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {}

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        int left = (width - W) / 2;
        int top = (height - H) / 2;

        g.blit(BG, left, top, 0, 0, W, H, 256, 256);
        g.drawCenteredString(font, title, left + W / 2, top + 8, 0xFFD700);

        int contentTop = top + 42;

        if (activeTab == 0) renderOverviewContent(g, left, contentTop);
        else if (activeTab == 1) renderMembersContent(g, left, contentTop);
        else if (activeTab == 2) {
             if (ecoSubView == 0) {
                 int grade = data.phucDiaGrade;
                 String[] gradeNames = {"", "Hạ đẳng", "Trung đẳng", "Thượng đẳng", "Siêu đẳng"};
                 String oreInfo = grade >= 4 ? "§aQuặng hiếm + Thiên Kiếp hardcore"
                                : grade >= 3 ? "§eQuặng hiếm tự sinh" : "§7—";
                 g.drawCenteredString(font, "§7Grade " + (grade>0?gradeNames[grade]:"?") + ": " + oreInfo, left + W / 2, contentTop + 140, 0xFFFFFF);
             } else if (ecoSubView == 1) {
                 g.drawCenteredString(font, "§a🐾 Sinh vật ở Phúc Địa", left + W / 2, contentTop - 5, 0xFFD700);
             } else if (ecoSubView == 2) {
                 renderThachNhanContent(g, left, contentTop);
             } else if (ecoSubView == 3) {
                 renderDialinhContent(g, left, contentTop);
             }
        }

        super.render(g, mx, my, pt);
    }

    private void renderRow(GuiGraphics g, int left, int y, Component text, int color) {
        g.blit(ROW_STRIPE, left + 10, y - 1, 0, 0, 236, 14, 236, 14);
        g.drawString(font, text, left + 14, y + 2, color);
    }

    private void renderOverviewContent(GuiGraphics g, int left, int top) {
        int rowSpacing = 16;
        int y = top + 4;
        renderRow(g, left, y, Component.translatable("gui.cotienaddon.phuc_dia.grade",
                Component.translatable(gradeKey(data.phucDiaGrade))), 0xFFD700);
        y += rowSpacing;
        renderRow(g, left, y, Component.translatable("gui.cotienaddon.phuc_dia.tien_nguyen",
                String.format("%.1f", data.tienNguyen)), 0xFFFFFF);
        y += rowSpacing;
        renderRow(g, left, y, Component.translatable("gui.cotienaddon.phuc_dia.thien_khi",
                String.format("%.1f", data.thienKhi)), 0x55FFFF);
        y += rowSpacing;
        renderRow(g, left, y, Component.translatable("gui.cotienaddon.phuc_dia.dia_khi",
                String.format("%.1f", data.diaKhi)), 0x55FF55);
        y += rowSpacing;
        renderRow(g, left, y, Component.translatable("gui.cotienaddon.phuc_dia.slot",
                data.phucDiaSlot), 0xAAAAAA);
    }

    private void renderMembersContent(GuiGraphics g, int left, int top) {
        List<String> wl = data.whitelist;
        if (wl.isEmpty()) {
            g.drawString(font, Component.translatable("gui.cotienaddon.phuc_dia.no_members"),
                    left + 10, top + 25, 0xAAAAAA);
        } else {
            int rowH = 22;
            int maxVisible = 5;
            for (int i = memberScrollOffset; i < Math.min(wl.size(), memberScrollOffset + maxVisible); i++) {
                String uuid = wl.get(i);
                String name = data.memberNames.getOrDefault(uuid, uuid.substring(0, Math.min(12, uuid.length())) + "..");
                g.drawString(font, name, left + 125, top + 25 + (i - memberScrollOffset) * rowH + 3, 0xFFFFFF);
            }
        }
    }

    // --- Tab 3: Thạch Nhân ---
    private void buildThachNhanTab(int left, int top) {
        addRenderableWidget(makeTexturedButton(
                Component.literal("§7← Trở lại"),
                left + 10, top - 10, 60, 16,
                b -> { ecoSubView = 1; clearWidgets(); init(); }));

        boolean canBuy = data.tienNguyen >= 80;
        addRenderableWidget(makeTexturedButton(
                Component.literal(canBuy ? "§a+ Mua (80 TN)" : "§cThiếu TN (80)"),
                left + W / 2 - 100, top + 15, 95, 18,
                b -> {
                    net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                            new com.andyanh.cotienaddon.network.UpgradePhucDiaPacket(
                                    com.andyanh.cotienaddon.network.UpgradePhucDiaPacket.TYPE_BUY_THACH_NHAN));
                    onClose();
                }));

        double slotCost = data.getThachnhanSlotCost();
        boolean canSlot = data.tienNguyen >= slotCost;
        addRenderableWidget(makeTexturedButton(
                Component.literal(canSlot ? "§a+ Mở Slot (" + (int)slotCost + ")" : "§c✗ Slot (" + (int)slotCost + ")"),
                left + W / 2 + 5, top + 15, 95, 18,
                b -> {
                    net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                            new com.andyanh.cotienaddon.network.UpgradePhucDiaPacket(
                                    com.andyanh.cotienaddon.network.UpgradePhucDiaPacket.TYPE_THACH_NHAN_SLOT));
                    onClose();
                }));

        addRenderableWidget(makeTexturedButton(
                Component.literal("§d☯ Gọi tất cả về đây"),
                left + W / 2 - 100, top + 40, 95, 18,
                b -> {
                    net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                            new com.andyanh.cotienaddon.network.UpgradePhucDiaPacket(
                                    com.andyanh.cotienaddon.network.UpgradePhucDiaPacket.TYPE_CALL_THACH_NHAN));
                    onClose();
                }));

        addRenderableWidget(makeTexturedButton(
                Component.literal("§b→ Đến chỗ Thạch Nhân"),
                left + W / 2 + 5, top + 40, 95, 18,
                b -> {
                    net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                            new com.andyanh.cotienaddon.network.UpgradePhucDiaPacket(
                                    com.andyanh.cotienaddon.network.UpgradePhucDiaPacket.TYPE_TP_THACH_NHAN));
                    onClose();
                }));
    }

    private void renderThachNhanContent(GuiGraphics g, int left, int top) {
        int textY = top + 75;
        g.drawString(font, "§8⚒ Quản Lý Thạch Nhân", left + 5, top - 10, 0x888888);
        g.drawString(font, "§7Slot: §f" + data.thachnhanSlots + " §7| §eTN: §f" + String.format("%.1f", data.tienNguyen),
                left + 5, textY, 0xAAAAAA);
        g.drawString(font, "§7Right-click Thạch Nhân để nâng cấp", left + 5, textY + 12, 0x666666);
        g.drawString(font, "§7Shift+click để xem kho đồ", left + 5, textY + 24, 0x666666);
        g.drawString(font, "§8Giá mua slot tăng gấp đôi mỗi lần", left + 5, textY + 36, 0x555555);
    }

    private String gradeKey(int grade) {
        return "gui.cotienaddon.grade." + grade;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
