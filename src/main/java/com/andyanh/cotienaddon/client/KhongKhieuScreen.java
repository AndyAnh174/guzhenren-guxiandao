package com.andyanh.cotienaddon.client;

import com.andyanh.cotienaddon.CoTienAddon;
import com.andyanh.cotienaddon.data.CoTienData;
import com.andyanh.cotienaddon.network.DebugActionPacket;
import com.andyanh.cotienaddon.network.ThangTienRequestPacket;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

import java.io.InputStream;
import java.io.InputStreamReader;

public class KhongKhieuScreen extends Screen {

    private static final ResourceLocation BG =
            ResourceLocation.fromNamespaceAndPath(CoTienAddon.MODID, "textures/gui/khong_khieu_bg.png");
    private static final ResourceLocation BTN_TEX =
            ResourceLocation.fromNamespaceAndPath(CoTienAddon.MODID, "textures/gui/button_normal.png");
    private static final ResourceLocation ROW_STRIPE =
            ResourceLocation.fromNamespaceAndPath(CoTienAddon.MODID, "textures/gui/row_stripe.png");
    private static final ResourceLocation LAYOUT_RES =
            ResourceLocation.fromNamespaceAndPath(CoTienAddon.MODID, "gui/khong_khieu_layout.json");

    private final CoTienData data;
    private int W = 256, H = 220;  // default, overridden by layout JSON

    public KhongKhieuScreen(CoTienData data) {
        super(Component.translatable("gui.cotienaddon.khong_khieu.title"));
        this.data = data;
        loadLayout();
    }

    private JsonObject layout = null;

    private void loadLayout() {
        try {
            Minecraft mc = Minecraft.getInstance();
            var res = mc.getResourceManager().getResource(LAYOUT_RES);
            if (res.isPresent()) {
                try (InputStream is = res.get().open()) {
                    layout = JsonParser.parseReader(new InputStreamReader(is)).getAsJsonObject();
                    JsonObject panel = layout.getAsJsonObject("panel");
                    W = panel.get("width").getAsInt();
                    H = panel.get("height").getAsInt();
                }
            }
        } catch (Exception e) {
            CoTienAddon.LOGGER.warn("[CoTienAddon] Could not load khong_khieu_layout.json: {}", e.getMessage());
        }
    }

    @Override
    protected void init() {
        super.init();
        int left = (width - W) / 2;
        int top  = (height - H) / 2;

        if (layout != null && layout.has("buttons")) {
            for (var el : layout.getAsJsonArray("buttons")) {
                JsonObject btn = el.getAsJsonObject();
                String id = btn.get("id").getAsString();
                int bx = btn.has("x_offset_from_center")
                        ? left + W / 2 + btn.get("x_offset_from_center").getAsInt()
                        : left + btn.get("x_offset_from_left").getAsInt();
                int by = btn.has("y_offset_from_bottom")
                        ? top + H - btn.get("y_offset_from_bottom").getAsInt()
                        : top + btn.get("y_offset_from_top").getAsInt();
                int bw = btn.get("width").getAsInt();
                int bh = btn.get("height").getAsInt();
                String labelKey = btn.get("label_key").getAsString();

                if ("dot_pha".equals(id)) {
                    addRenderableWidget(makeTexturedButton(
                            Component.translatable(labelKey), bx, by, bw, bh,
                            b -> { PacketDistributor.sendToServer(new ThangTienRequestPacket()); onClose(); }));
                }
            }
        } else {
            addRenderableWidget(makeTexturedButton(
                    Component.translatable("gui.cotienaddon.dot_pha"),
                    left + W / 2 - 60, top + H - 40, 120, 20,
                    b -> { PacketDistributor.sendToServer(new ThangTienRequestPacket()); onClose(); }));
        }

        // === Button mở Tiên Tôn panel (chỉ khi đủ điều kiện: Bát Chuyển Đỉnh Phong hoặc Cửu Chuyển) ===
        var gv = Minecraft.getInstance().player.getData(net.guzhenren.network.GuzhenrenModVariables.PLAYER_VARIABLES);
        boolean eligible = (gv.zhuanshu >= 9) || (gv.zhuanshu >= 8 && gv.jieduan >= 4);
        if (eligible) {
            double daode = data.daode;
            String btnLabel = daode >= 0 ? "§b☯ Tiên Tôn" : "§c☠ Ma Tôn";
            addRenderableWidget(makeTexturedButton(
                    Component.literal(btnLabel),
                    left + W / 2 - 40, top + H - 62, 80, 18,
                    b -> Minecraft.getInstance().setScreen(new TienTonScreen(data))));
        }

        // Debug buttons — chỉ hiện trong creative/gamemode 1
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.isCreative()) {
            int dbx = left + W / 2;
            int dby = top + H - 16;
            addRenderableWidget(Button.builder(Component.literal("§7[▶]"),
                    b -> PacketDistributor.sendToServer(new DebugActionPacket(DebugActionPacket.ASCEND)))
                    .bounds(dbx - 54, dby, 34, 12).build());
            addRenderableWidget(Button.builder(Component.literal("§7[✓]"),
                    b -> { PacketDistributor.sendToServer(new DebugActionPacket(DebugActionPacket.COMPLETE)); onClose(); })
                    .bounds(dbx - 17, dby, 34, 12).build());
            addRenderableWidget(Button.builder(Component.literal("§7[↺]"),
                    b -> PacketDistributor.sendToServer(new DebugActionPacket(DebugActionPacket.RESET)))
                    .bounds(dbx + 20, dby, 34, 12).build());
        }
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {}

    @Override
    public void render(GuiGraphics g, int mx, int my, float delta) {
        // Không render dark overlay — texture hiện thẳng lên game
        int left = (width - W) / 2;
        int top  = (height - H) / 2;

        g.blit(BG, left, top, 0, 0, W, H, 256, 256);

        int cx = left + W / 2;

        if (layout != null && layout.has("labels")) {
            for (var el : layout.getAsJsonArray("labels")) {
                JsonObject lbl = el.getAsJsonObject();
                String id = lbl.get("id").getAsString();
                int lx = lbl.has("x_offset_from_center")
                        ? cx + lbl.get("x_offset_from_center").getAsInt()
                        : left + lbl.get("x_offset_from_left").getAsInt();
                int ly = top + lbl.get("y_offset_from_top").getAsInt();
                int color = (int) Long.parseLong(lbl.get("color").getAsString().replace("0x",""), 16);
                boolean centered = lbl.has("centered") && lbl.get("centered").getAsBoolean();
                boolean stripe = lbl.has("stripe") && lbl.get("stripe").getAsBoolean();
                Component text = getLabelText(id);
                if (text == null) continue;
                if (stripe) g.blit(ROW_STRIPE, left + 10, ly - 2, 0, 0, 236, 14, 236, 14);
                if (centered) g.drawCenteredString(font, text, lx, ly, color);
                else g.drawString(font, text, lx, ly, color);
            }
        } else {
            renderFallbackLabels(g, cx, left, top);
        }

        super.render(g, mx, my, delta);
    }

    private Component getLabelText(String id) {
        return switch (id) {
            case "title" -> title;
            case "nhan_khi" -> Component.translatable("gui.cotienaddon.nhan_khi",
                    String.format("%.0f", data.calcNhanKhi()));
            case "grade" -> Component.translatable("gui.cotienaddon.phuc_dia_grade",
                    Component.translatable("gui.cotienaddon.grade." + data.calcPhucDiaGrade()));
            case "gu_usage" -> Component.literal(
                    String.format("T1=%.0f  T2=%.0f  T3=%.0f  T4=%.0f  T5=%.0f",
                            data.guUsed_tier1, data.guUsed_tier2, data.guUsed_tier3,
                            data.guUsed_tier4, data.guUsed_tier5));
            case "phase" -> Component.translatable("gui.cotienaddon.phase." + data.thangTienPhase);
            case "condition" -> data.thangTienPhase == 0
                    ? Component.literal(data.canStartAscension()
                        ? "§a✔ Đủ điều kiện Thăng Tiên"
                        : "§c✘ Chưa đủ điều kiện") : null;
            case "warning" -> data.thangTienPhase == 0
                    ? Component.translatable("gui.cotienaddon.warning_irreversible") : null;
            default -> null;
        };
    }

    private void renderFallbackLabels(GuiGraphics g, int cx, int left, int top) {
        g.drawCenteredString(font, title, cx, top + 30, 0xFFD700);
        g.drawString(font, Component.translatable("gui.cotienaddon.nhan_khi",
                String.format("%.0f", data.calcNhanKhi())), left + 20, top + 48, 0xE0E0E0);
        g.drawString(font, Component.translatable("gui.cotienaddon.phuc_dia_grade",
                Component.translatable("gui.cotienaddon.grade." + data.calcPhucDiaGrade())),
                left + 20, top + 61, 0xE0E0E0);
        g.drawString(font, Component.literal(String.format("T1=%.0f  T2=%.0f  T3=%.0f  T4=%.0f  T5=%.0f",
                data.guUsed_tier1, data.guUsed_tier2, data.guUsed_tier3,
                data.guUsed_tier4, data.guUsed_tier5)), left + 20, top + 74, 0xAAAAAA);
        g.drawCenteredString(font,
                Component.translatable("gui.cotienaddon.phase." + data.thangTienPhase),
                cx, top + 92, 0x55FF55);
        if (data.thangTienPhase == 0)
            g.drawCenteredString(font,
                    Component.translatable("gui.cotienaddon.warning_irreversible"),
                    cx, top + 108, 0xFF5555);
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

    @Override
    public boolean isPauseScreen() { return false; }
}
