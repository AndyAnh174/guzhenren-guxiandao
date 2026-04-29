package com.andyanh.cotienaddon.client;

import com.andyanh.cotienaddon.CoTienAddon;
import com.andyanh.cotienaddon.network.SectNetwork;
import com.andyanh.cotienaddon.system.SectSavedData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SectScreen extends Screen {
    private static final ResourceLocation BG =
            ResourceLocation.fromNamespaceAndPath(CoTienAddon.MODID, "textures/gui/sect_gui.png");
    private static final int W = 256, H = 256;

    private int left, top;
    private final SectSavedData.Sect sect;
    private boolean inviteMode = false;
    private EditBox inviteInput;

    // Kick button positions (built each render cycle when in leader mode)
    private final List<KickEntry> kickEntries = new ArrayList<>();

    private record KickEntry(UUID uuid, int bx, int by) {}

    public SectScreen(SectSavedData.Sect sect) {
        super(Component.literal("Tông Môn"));
        this.sect = sect;
    }

    @Override
    protected void init() {
        left = (width - W) / 2;
        top  = (height - H) / 2;

        if (sect == null) {
            // === Chưa có tông môn ===
            addRenderableWidget(new SectButton(
                    left + (W - 120) / 2, top + 190, 120, 24,
                    Component.literal("✦ Thành Lập"),
                    b -> Minecraft.getInstance().setScreen(new CreateSectScreen())));

        } else {
            boolean isLeader = isLeader();

            if (inviteMode) {
                // === Invite mode ===
                inviteInput = new EditBox(font, left + 44, top + 148, W - 88, 18,
                        Component.literal("Tên player..."));
                inviteInput.setMaxLength(32);
                inviteInput.setHint(Component.literal("§7Nhập tên người chơi..."));
                addRenderableWidget(inviteInput);

                addRenderableWidget(new SectButton(left + (W - 120) / 2 - 32, top + 175, 74, 20,
                        Component.literal("§aGửi Mời"),
                        b -> {
                            String name = inviteInput.getValue().trim();
                            if (!name.isEmpty()) {
                                PacketDistributor.sendToServer(new SectNetwork.SectActionPacket(
                                        SectNetwork.SectActionPacket.ActionType.INVITE.ordinal(), name));
                            }
                            inviteMode = false;
                            rebuildWidgets();
                        }));

                addRenderableWidget(new SectButton(left + (W - 120) / 2 + 46, top + 175, 74, 20,
                        Component.literal("§7Hủy"),
                        b -> { inviteMode = false; rebuildWidgets(); }));

            } else {
                // === Normal mode ===
                // Hàng nút dưới cùng
                int btnY = top + 207;
                addRenderableWidget(new SectButton(
                        left + 18, btnY, 90, 20,
                        Component.literal("Rời Tông Môn"),
                        b -> {
                            PacketDistributor.sendToServer(new SectNetwork.SectActionPacket(
                                    SectNetwork.SectActionPacket.ActionType.LEAVE.ordinal(), ""));
                            onClose();
                        }));

                if (isLeader) {
                    // Mời (giữa)
                    addRenderableWidget(new SectButton(
                            left + 114, btnY, 60, 20,
                            Component.literal("Mời"),
                            b -> { inviteMode = true; rebuildWidgets(); }));

                    // Đặt Home (hàng trên, không đè chữ)
                    addRenderableWidget(new SectButton(
                            left + 180, btnY, 58, 20,
                            Component.literal("Home"),
                            b -> PacketDistributor.sendToServer(new SectNetwork.SectActionPacket(
                                    SectNetwork.SectActionPacket.ActionType.SET_HOME.ordinal(), ""))));

                    // Kick buttons next to each member (built dynamically)
                    kickEntries.clear();
                    List<UUID> memberList = new ArrayList<>(sect.members);
                    int ky = top + 86;
                    for (UUID m : memberList) {
                        if (m.equals(sect.leader)) { ky += 12; continue; }
                        final UUID targetUUID = m;
                        final int finalKy = ky;
                        addRenderableWidget(new SectButton(left + W - 50, finalKy - 2, 24, 12,
                                Component.literal("§cX"),
                                b -> PacketDistributor.sendToServer(new SectNetwork.SectActionPacket(
                                        SectNetwork.SectActionPacket.ActionType.KICK.ordinal(),
                                        targetUUID.toString()))));
                        ky += 12;
                        if (ky > top + 158) break;
                    }
                }
            }
        }
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {}

    @Override
    public void renderBackground(GuiGraphics g, int mx, int my, float pt) {}

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.blit(BG, left, top, 0, 0, W, H, W, H);
        int cx = left + W / 2;

        if (sect == null) {
            g.drawCenteredString(font, Component.literal("§6§l✦ TÔNG MÔN ✦"), cx, top + 45, 0xFFD700);
            g.drawCenteredString(font, Component.literal("§7Bạn chưa thuộc Tông Môn nào"), cx, top + 80, 0xAAAAAA);
            g.drawCenteredString(font, Component.literal("§7Chi phí: §e100,000 §7Nguyên Thạch"), cx, top + 100, 0xAAAAAA);
            g.drawCenteredString(font, Component.literal("§7(trong Nguyên Lão Cổ)"), cx, top + 114, 0x888888);

        } else {
            boolean isImmortal = sect.type == SectSavedData.SectType.IMMORTAL;
            boolean isLeader = isLeader();

            // Tên + loại
            g.drawCenteredString(font, Component.literal("§6§l" + sect.name), cx, top + 38, 0xFFD700);
            g.drawCenteredString(font,
                    isImmortal ? Component.literal("§b[ Cổ Tiên Tông ]") : Component.literal("§f[ Phàm Nhân Tông ]"),
                    cx, top + 52, 0xFFFFFF);

            g.fill(left + 28, top + 65, left + W - 28, top + 66, 0x88555555);

            // Thành viên
            g.drawString(font, Component.literal("§eThành viên (" + sect.members.size() + "):"),
                    left + 32, top + 72, 0xFFFFFF);

            List<UUID> memberList = new ArrayList<>(sect.members);
            int my2 = top + 86;
            for (UUID m : memberList) {
                String displayName = sect.memberNames.getOrDefault(m, m.toString().substring(0, 8));
                boolean mLeader = m.equals(sect.leader);
                String label = (mLeader ? "§6★ " : "§f• ") + displayName + (mLeader ? " §6[Trưởng]" : "");
                g.drawString(font, Component.literal(label), left + 36, my2, 0xFFFFFF);
                my2 += 12;
                if (my2 > top + 160) { g.drawString(font, Component.literal("§8..."), left + 36, my2, 0x888888); break; }
            }

            g.fill(left + 28, top + 168, left + W - 28, top + 169, 0x88555555);

            if (!inviteMode) {
                // Buff info
                int buff = isImmortal ? 4 : 2;
                g.drawString(font, Component.literal("§aHiệu ứng:"), left + 32, top + 172, 0x55FF55);
                g.drawString(font, Component.literal("§7● Ôn dưỡng: §a+" + buff), left + 36, top + 183, 0xAAAAAA);
                if (sect.homePos != null) {
                    g.drawString(font, Component.literal(
                            "§7● Home: §f" + sect.homePos.toShortString()), left + 36, top + 195, 0xAAAAAA);
                } else if (isLeader) {
                    g.drawString(font, Component.literal("§7● Home: §8Chưa đặt (nhấn [Home])"),
                            left + 36, top + 195, 0x888888);
                }
            } else {
                // Invite mode header
                g.drawCenteredString(font, Component.literal("§e✉ Mời thành viên mới"), cx, top + 136, 0xFFD700);
            }
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    private boolean isLeader() {
        if (sect == null) return false;
        var player = Minecraft.getInstance().player;
        return player != null && player.getUUID().equals(sect.leader);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
