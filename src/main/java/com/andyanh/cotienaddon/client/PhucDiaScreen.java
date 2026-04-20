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

    private final CoTienData data;
    private int activeTab = 0; // 0=Tổng quan, 1=Quản lý Khách, 2=Hệ sinh thái

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

        // Tab buttons
        addRenderableWidget(Button.builder(Component.translatable("gui.cotienaddon.phuc_dia.tab.overview"), b -> switchTab(0))
                .bounds(left + 5, top + 5, 85, 18).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.cotienaddon.phuc_dia.tab.members"), b -> switchTab(1))
                .bounds(left + 95, top + 5, 85, 18).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.cotienaddon.phuc_dia.tab.ecosystem"), b -> switchTab(2))
                .bounds(left + 185, top + 5, 85, 18).build());

        buildTab(left, top);
    }

    private void switchTab(int tab) {
        activeTab = tab;
        clearWidgets();
        init();
    }

    private void buildTab(int left, int top) {
        int contentTop = top + 30;
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

        if (inPhucDia) {
            addRenderableWidget(Button.builder(
                    Component.translatable("gui.cotienaddon.phuc_dia.btn.exit"), b -> {
                        PacketDistributor.sendToServer(new TeleportPhucDiaPacket(false));
                        onClose();
                    }).bounds(left + W / 2 - 50, top + 150, 100, 20).build());
        } else {
            if (data.thangTienPhase >= 4) {
                addRenderableWidget(Button.builder(
                        Component.translatable("gui.cotienaddon.phuc_dia.btn.enter"), b -> {
                            PacketDistributor.sendToServer(new TeleportPhucDiaPacket(true));
                            onClose();
                        }).bounds(left + W / 2 - 50, top + 150, 100, 20).build());
            }
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
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g, mx, my, pt);

        int left = (width - W) / 2;
        int top = (height - H) / 2;

        // Background texture
        g.blit(BG, left, top, 0, 0, W, H, 256, 256);
        g.drawString(font, title, left + 5, top - 10, 0xFFD700);

        int contentTop = top + 30;

        if (activeTab == 0) renderOverviewContent(g, left, contentTop);
        else if (activeTab == 1) renderMembersContent(g, left, contentTop);

        super.render(g, mx, my, pt);
    }

    private void renderOverviewContent(GuiGraphics g, int left, int top) {
        int col = 0xFFFFFF;
        g.drawString(font, Component.translatable("gui.cotienaddon.phuc_dia.grade",
                gradeKey(data.phucDiaGrade)), left + 10, top + 5, 0xFFD700);
        g.drawString(font, Component.translatable("gui.cotienaddon.phuc_dia.tien_nguyen",
                String.format("%.1f", data.tienNguyen)), left + 10, top + 20, col);
        g.drawString(font, Component.translatable("gui.cotienaddon.phuc_dia.thien_khi",
                String.format("%.1f", data.thienKhi)), left + 10, top + 35, 0x55FFFF);
        g.drawString(font, Component.translatable("gui.cotienaddon.phuc_dia.dia_khi",
                String.format("%.1f", data.diaKhi)), left + 10, top + 50, 0x55FF55);
        g.drawString(font, Component.translatable("gui.cotienaddon.phuc_dia.slot",
                data.phucDiaSlot), left + 10, top + 65, 0xAAAAAA);
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
