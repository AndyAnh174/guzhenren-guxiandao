package com.andyanh.cotienaddon.client;

import com.andyanh.cotienaddon.CoTienAddon;
import com.andyanh.cotienaddon.data.CoTienData;
import com.andyanh.cotienaddon.network.ManageMemberPacket;
import com.andyanh.cotienaddon.network.TeleportPhucDiaPacket;
import com.andyanh.cotienaddon.network.UpdatePermissionPacket;
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
            ResourceLocation.fromNamespaceAndPath(CoTienAddon.MODID, "textures/gui/phuc_dia_bg.png");
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
        addRenderableWidget(makeTabButton(Component.translatable("gui.cotienaddon.phuc_dia.tab.overview"),  0, left + 8,   top + 24, b -> switchTab(0)));
        addRenderableWidget(makeTabButton(Component.translatable("gui.cotienaddon.phuc_dia.tab.members"),   1, left + 90,  top + 24, b -> switchTab(1)));
        addRenderableWidget(makeTabButton(Component.translatable("gui.cotienaddon.phuc_dia.tab.ecosystem"), 2, left + 172, top + 24, b -> switchTab(2)));

        buildTab(left, top);
    }

    private void switchTab(int tab) {
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
        boolean inPhucDia = dimKey.equals("cotienaddon:phuc_dia");

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

            // Permission toggles (5 bits)
            int perms = data.permissions.getOrDefault(memberUUID, 0);
            int[] bits = {CoTienData.PERM_BUILD, CoTienData.PERM_CONTAINERS,
                    CoTienData.PERM_COMBAT, CoTienData.PERM_CORE, CoTienData.PERM_MANAGE};
            String[] icons = {"⛏", "📦", "⚔", "✦", "👑"};

            for (int b = 0; b < bits.length; b++) {
                final int bit = bits[b];
                boolean on = (perms & bit) != 0;
                int btnX = left + 5 + b * 24;
                addRenderableWidget(Button.builder(Component.literal(icons[b] + (on ? "✓" : "✗")), btn -> {
                    boolean newVal = (data.permissions.getOrDefault(memberUUID, 0) & bit) == 0;
                    PacketDistributor.sendToServer(new UpdatePermissionPacket(memberUUID, bit, newVal));
                    data.setPermission(java.util.UUID.fromString(memberUUID), bit, newVal);
                }).bounds(btnX, rowY, 22, 18).build());
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
        // Placeholder: weather / mob spawn controls (Phase 5 chi tiết hơn)
        addRenderableWidget(Button.builder(Component.translatable("gui.cotienaddon.phuc_dia.ecosystem.coming_soon"),
                b -> {}).bounds(left + W / 2 - 60, top + 80, 120, 20).build()
        ).active = false;
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
                String display = uuid.length() > 18 ? uuid.substring(0, 18) + "..." : uuid;
                g.drawString(font, display, left + 125, top + 25 + (i - memberScrollOffset) * rowH + 3, 0xFFFFFF);
            }
        }
    }

    private String gradeKey(int grade) {
        return "gui.cotienaddon.grade." + grade;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
