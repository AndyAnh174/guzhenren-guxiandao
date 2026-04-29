package com.andyanh.cotienaddon.client;

import com.andyanh.cotienaddon.CoTienAddon;
import com.andyanh.cotienaddon.data.CoTienData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = CoTienAddon.MODID, value = Dist.CLIENT)
public class CoTienHudOverlay {

    @SubscribeEvent
    public static void onRenderHud(RenderGuiLayerEvent.Post event) {
        if (!event.getName().equals(VanillaGuiLayers.PLAYER_HEALTH)) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        GuiGraphics g = event.getGuiGraphics();
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();

        // Hiển thị "PHONG ẤN" ở giữa màn hình khi bị seal
        var pd = mc.player.getPersistentData();
        if (pd.contains("tran_vu_sealed")) {
            int ticks = pd.getInt("tran_vu_sealed");
            int seconds = (ticks / 20) + 1;
            String sealText = "❄ BĂNG PHONG TRẬN — " + seconds + "s ❄";
            int sx = (sw - mc.font.width(sealText)) / 2;
            int sy = sh / 2 - 30;
            // Background semi-transparent
            g.fill(sx - 4, sy - 2, sx + mc.font.width(sealText) + 4, sy + 10, 0x88001133);
            g.drawString(mc.font, sealText, sx, sy, 0x55EEFF, true);
        }

        CoTienData data = CoTienClientHandler.getCachedData();
        if (data == null || data.thangTienPhase < 4) return;

        // Hiển thị "✦ Cổ Tiên ✦" góc trên phải
        String text = "✦ Cổ Tiên ✦";
        int x = sw - mc.font.width(text) - 4;
        int y = 32;
        g.drawString(mc.font, text, x, y, 0xFFD700, true);

        // Tiên Nguyên
        String tn = "☯ " + String.format("%.1f", data.tienNguyen) + " Tiên Nguyên";
        g.drawString(mc.font, tn, sw - mc.font.width(tn) - 4, y + 10, 0xAAFFFF, true);
    }
}
