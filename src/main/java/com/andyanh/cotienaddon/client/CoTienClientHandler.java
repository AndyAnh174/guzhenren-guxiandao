package com.andyanh.cotienaddon.client;

import com.andyanh.cotienaddon.CoTienAddon;
import com.andyanh.cotienaddon.data.CoTienData;
import com.andyanh.cotienaddon.network.OpenKhongKhieuPacket;
import com.andyanh.cotienaddon.network.OpenPhucDiaPacket;
import com.andyanh.cotienaddon.network.SyncCoTienPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = CoTienAddon.MODID, value = Dist.CLIENT)
public class CoTienClientHandler {

    public static final KeyMapping KEY_KHONG_KHIEU = new KeyMapping(
            "key.cotienaddon.open_khong_khieu",
            GLFW.GLFW_KEY_K,
            "key.categories.cotienaddon"
    );

    public static final KeyMapping KEY_PHUC_DIA = new KeyMapping(
            "key.cotienaddon.open_phuc_dia",
            GLFW.GLFW_KEY_P,
            "key.categories.cotienaddon"
    );

    // Đăng ký keybind (MOD bus)
    @EventBusSubscriber(modid = CoTienAddon.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static class ModEvents {
        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(KEY_KHONG_KHIEU);
            event.register(KEY_PHUC_DIA);
        }
    }

    // Xử lý keybind press (GAME bus)
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (KEY_KHONG_KHIEU.consumeClick()) {
            openingPhucDia = false;
            PacketDistributor.sendToServer(new OpenKhongKhieuPacket());
        }
        if (KEY_PHUC_DIA.consumeClick()) {
            openingPhucDia = true;
            PacketDistributor.sendToServer(new OpenPhucDiaPacket());
        }
    }

    // Nhận SyncCoTienPacket từ server → mở screen phù hợp
    // Convention: nếu screen hiện tại là null → mở KhongKhieuScreen
    // Nếu đang mở PhucDiaScreen (do nhấn P) thì refresh với data mới
    private static boolean openingPhucDia = false;
    private static CoTienData cachedData = null;

    public static CoTienData getCachedData() { return cachedData; }

    public static void openPhucDia() {
        openingPhucDia = true;
        PacketDistributor.sendToServer(new OpenPhucDiaPacket());
    }

    public static void handleSyncPacket(SyncCoTienPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            cachedData = pkt.toData();
            if (openingPhucDia) {
                openingPhucDia = false;
                mc.setScreen(new PhucDiaScreen(cachedData));
            } else if (mc.screen instanceof DinhTienDuScreen dts) {
                // Refresh saved locations list when server syncs back after save/delete
                dts.refreshData(cachedData);
            } else {
                mc.setScreen(new KhongKhieuScreen(cachedData));
            }
        });
    }
}
