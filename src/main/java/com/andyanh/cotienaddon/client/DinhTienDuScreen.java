package com.andyanh.cotienaddon.client;

import com.andyanh.cotienaddon.data.CoTienData;
import com.andyanh.cotienaddon.network.SaveLocationPacket;
import com.andyanh.cotienaddon.network.TeleportDinhTienDuPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class DinhTienDuScreen extends Screen {

    // Left panel (coord input) — 160px wide
    // Right panel (saved locations) — 120px wide
    // Total width: 300px

    private static final int PANEL_W = 300;
    private static final int PANEL_H = 160;

    private EditBox xField, yField, zField, nameField;
    private CoTienData clientData;

    public DinhTienDuScreen() {
        super(Component.literal("☯ Định Tiên Du"));
        // Grab cached client data from CoTienClientHandler
        this.clientData = CoTienClientHandler.getCachedData();
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int cy = height / 2;

        int left = cx - PANEL_W / 2 + 8;
        int top  = cy - PANEL_H / 2 + 22;

        // === LEFT: Coordinate input ===
        xField = addRenderableWidget(new EditBox(font, left, top, 80, 16, Component.literal("X")));
        xField.setHint(Component.literal("X"));
        xField.setFilter(s -> s.isEmpty() || s.matches("-?[0-9]*\\.?[0-9]*"));

        yField = addRenderableWidget(new EditBox(font, left, top + 22, 80, 16, Component.literal("Y")));
        yField.setHint(Component.literal("Y"));
        yField.setFilter(s -> s.isEmpty() || s.matches("-?[0-9]*\\.?[0-9]*"));

        zField = addRenderableWidget(new EditBox(font, left, top + 44, 80, 16, Component.literal("Z")));
        zField.setHint(Component.literal("Z"));
        zField.setFilter(s -> s.isEmpty() || s.matches("-?[0-9]*\\.?[0-9]*"));

        // Name field for saving
        nameField = addRenderableWidget(new EditBox(font, left, top + 72, 80, 16, Component.literal("Tên vị trí")));
        nameField.setHint(Component.literal("Tên vị trí..."));
        nameField.setMaxLength(20);

        // Pre-fill player coords
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            xField.setValue(String.valueOf((int) mc.player.getX()));
            yField.setValue(String.valueOf((int) mc.player.getY()));
            zField.setValue(String.valueOf((int) mc.player.getZ()));
        }

        // Teleport button
        addRenderableWidget(Button.builder(Component.literal("✦ Dịch Chuyển"), btn -> confirmTeleport())
                .bounds(left, top + 96, 80, 18).build());

        // Save location button
        addRenderableWidget(Button.builder(Component.literal("+ Lưu"), btn -> saveCurrentCoords())
                .bounds(left, top + 118, 38, 16).build());

        // === RIGHT: Saved locations list ===
        rebuildLocationButtons(cx, top);

        setInitialFocus(xField);
    }

    private void rebuildLocationButtons(int cx, int top) {
        int rx = cx + 8; // right panel start
        List<CoTienData.SavedLocation> locs = clientData != null ? clientData.savedLocations : List.of();

        for (int i = 0; i < locs.size(); i++) {
            CoTienData.SavedLocation loc = locs.get(i);
            final int idx = i;
            final CoTienData.SavedLocation l = loc;
            int y = top + i * 22;

            // Dimension short label
            String dimShort = dimShortName(loc.dimensionId);
            // Location name button (click → teleport trực tiếp)
            addRenderableWidget(Button.builder(
                    Component.literal("§f" + loc.name + " §8[" + dimShort + "] §7(" + (int)loc.x + "," + (int)loc.y + "," + (int)loc.z + ")"),
                    btn -> teleportToSaved(l))
                    .bounds(rx, y, 100, 16).build());

            // Delete button
            addRenderableWidget(Button.builder(Component.literal("§c✕"), btn -> deleteLocation(idx))
                    .bounds(rx + 103, y, 14, 16).build());
        }

        // Slot count indicator is drawn in render()
    }

    private void fillCoords(CoTienData.SavedLocation loc) {
        xField.setValue(String.valueOf((int) loc.x));
        yField.setValue(String.valueOf((int) loc.y));
        zField.setValue(String.valueOf((int) loc.z));
    }

    private void teleportToSaved(CoTienData.SavedLocation loc) {
        String dim = loc.dimensionId != null ? loc.dimensionId : "minecraft:overworld";
        PacketDistributor.sendToServer(new TeleportDinhTienDuPacket(loc.x, loc.y, loc.z, dim));
        onClose();
    }

    private void confirmTeleport() {
        try {
            double x = Double.parseDouble(xField.getValue());
            double y = Double.parseDouble(yField.getValue());
            double z = Double.parseDouble(zField.getValue());
            // Teleport về dimension hiện tại của player
            String dim = net.minecraft.client.Minecraft.getInstance().player != null
                    ? net.minecraft.client.Minecraft.getInstance().player.level()
                            .dimension().location().toString()
                    : "minecraft:overworld";
            PacketDistributor.sendToServer(new TeleportDinhTienDuPacket(x, y, z, dim));
            onClose();
        } catch (NumberFormatException e) {
            xField.setTextColor(0xFF5555);
            yField.setTextColor(0xFF5555);
            zField.setTextColor(0xFF5555);
        }
    }

    private void saveCurrentCoords() {
        try {
            double x = Double.parseDouble(xField.getValue());
            double y = Double.parseDouble(yField.getValue());
            double z = Double.parseDouble(zField.getValue());
            String name = nameField.getValue().trim();
            if (name.isEmpty()) name = (int)x + "," + (int)y + "," + (int)z;

            int currentSize = clientData != null ? clientData.savedLocations.size() : 0;
            if (currentSize >= CoTienData.MAX_SAVED_LOCATIONS) return;

            // Gửi "" → server dùng dimension hiện tại
            PacketDistributor.sendToServer(new SaveLocationPacket(
                    SaveLocationPacket.ACTION_SAVE, name, x, y, z, -1, ""));
            nameField.setValue("");
        } catch (NumberFormatException ignored) {}
    }

    private void deleteLocation(int idx) {
        PacketDistributor.sendToServer(new SaveLocationPacket(
                SaveLocationPacket.ACTION_DELETE, "", 0, 0, 0, idx, ""));
        if (clientData != null && idx < clientData.savedLocations.size()) {
            clientData.savedLocations.remove(idx);
        }
        rebuildScreen();
    }

    private static String dimShortName(String dimId) {
        if (dimId == null) return "OW";
        return switch (dimId) {
            case "minecraft:overworld"   -> "OW";
            case "minecraft:the_nether"  -> "NT";
            case "minecraft:the_end"     -> "END";
            default -> dimId.contains(":") ? dimId.substring(dimId.indexOf(':') + 1, Math.min(dimId.length(), dimId.indexOf(':') + 4)).toUpperCase() : dimId.substring(0, Math.min(3, dimId.length())).toUpperCase();
        };
    }

    private void rebuildScreen() {
        clearWidgets();
        init();
    }

    public void refreshData(CoTienData newData) {
        this.clientData = newData;
        rebuildScreen();
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);

        int cx = width / 2;
        int cy = height / 2;
        int px = cx - PANEL_W / 2;
        int py = cy - PANEL_H / 2;

        // Outer panel
        g.fill(px, py, px + PANEL_W, py + PANEL_H, 0xCC000000);
        g.renderOutline(px, py, PANEL_W, PANEL_H, 0xFF44AAFF);

        // Divider between left/right panels
        int divX = cx + 4;
        g.fill(divX, py + 2, divX + 1, py + PANEL_H - 2, 0xFF224466);

        // Title
        g.drawCenteredString(font, "☯ Định Tiên Du Cổ", cx, py + 6, 0xFFD700);

        // Left panel labels
        int top = cy - PANEL_H / 2 + 22;
        int left = px + 8;
        g.drawString(font, "§bX:", left - 2, top + 3, 0xAAAAAA);
        g.drawString(font, "§bY:", left - 2, top + 25, 0xAAAAAA);
        g.drawString(font, "§bZ:", left - 2, top + 47, 0xAAAAAA);
        g.drawString(font, "§7Tên:", left - 2, top + 75, 0xAAAAAA);

        // Cost reminder
        g.drawString(font, "§7-25 hạn  -1 Tiên Nguyên", left - 2, top + 100, 0x888888);

        // Right panel header
        int rx = cx + 8;
        int slots = clientData != null ? clientData.savedLocations.size() : 0;
        g.drawString(font, "§eTọa Độ Đã Lưu §7(" + slots + "/" + CoTienData.MAX_SAVED_LOCATIONS + ")", rx, py + 8, 0xFFD700);
        if (slots == 0) {
            g.drawString(font, "§7(Chưa có tọa độ nào)", rx, top + 4, 0x666666);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 257 || keyCode == 335) { // Enter
            if (nameField.isFocused()) {
                saveCurrentCoords();
            } else {
                confirmTeleport();
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
