package com.andyanh.cotienaddon.command;

import com.andyanh.cotienaddon.data.CoTienData;
import com.andyanh.cotienaddon.init.CoTienAttachments;
import com.andyanh.cotienaddon.system.ThangTienManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import static com.andyanh.cotienaddon.CoTienAddon.MODID;

@EventBusSubscriber(modid = MODID)
public class CoTienCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("cotien")
            .requires(src -> src.hasPermission(2))
            .then(Commands.literal("debug")
                .then(Commands.literal("ascend")
                    .executes(ctx -> {
                        ServerPlayer sp = ctx.getSource().getPlayerOrException();
                        CoTienData data = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());
                        if (data.thangTienPhase != 0) {
                            ctx.getSource().sendFailure(Component.literal("Đang trong quá trình thăng tiên (phase=" + data.thangTienPhase + ")"));
                            return 0;
                        }
                        data.thangTienPhase = 1;
                        data.phucDiaGrade = Math.max(1, data.calcPhucDiaGrade());
                        sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
                        ctx.getSource().sendSuccess(() -> Component.literal("§a[Debug] Bắt đầu Thăng Tiên — Phase 1 (Phá Toái Không Khiếu)"), false);

                        net.guzhenren.GuzhenrenMod.queueServerWork(60, () -> {
                            CoTienData d2 = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());
                            if (d2.thangTienPhase == 1) {
                                d2.thangTienPhase = 2;
                                d2.thienKhi = d2.calcNhanKhi() * 0.5;
                                d2.diaKhi   = d2.calcNhanKhi() * 0.5;
                                sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), d2);
                                sp.sendSystemMessage(Component.literal("§b[Debug] Phase 2 — Nạp Khí (đã pre-fill đủ Khí)"));
                            }
                        });
                        return 1;
                    }))
                .then(Commands.literal("complete")
                    .executes(ctx -> {
                        ServerPlayer sp = ctx.getSource().getPlayerOrException();
                        CoTienData data = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());
                        data.thangTienPhase = 3;
                        sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
                        ThangTienManager.completeAscension(sp);
                        ctx.getSource().sendSuccess(() -> Component.literal("§a[Debug] Force complete ascension"), false);
                        return 1;
                    }))
                .then(Commands.literal("reset")
                    .executes(ctx -> {
                        ServerPlayer sp = ctx.getSource().getPlayerOrException();
                        CoTienData data = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());
                        data.thangTienPhase = 0;
                        data.thienKhi = 0;
                        data.diaKhi = 0;
                        sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
                        ctx.getSource().sendSuccess(() -> Component.literal("§e[Debug] Reset thangTienPhase = 0"), false);
                        return 1;
                    }))
                .then(Commands.literal("setnk")
                    .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                        .executes(ctx -> {
                            ServerPlayer sp = ctx.getSource().getPlayerOrException();
                            double amount = DoubleArgumentType.getDouble(ctx, "amount");
                            CoTienData data = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());
                            // Distribute amount into tier5 slots for simplicity
                            data.guUsed_tier5 = amount / 81.0;
                            data.nhanKhi = data.calcNhanKhi();
                            sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                "§a[Debug] Nhân Khí = " + String.format("%.1f", data.calcNhanKhi())
                                + " (grade=" + data.calcPhucDiaGrade() + ")"), false);
                            return 1;
                        })))
                .then(Commands.literal("status")
                    .executes(ctx -> {
                        ServerPlayer sp = ctx.getSource().getPlayerOrException();
                        CoTienData data = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());
                        ctx.getSource().sendSuccess(() -> Component.literal(
                            "§e[CoTien] phase=" + data.thangTienPhase
                            + " grade=" + data.phucDiaGrade
                            + " slot=" + data.phucDiaSlot
                            + "\n§eNhanKhi=" + String.format("%.0f", data.calcNhanKhi())
                            + " ThienKhi=" + String.format("%.1f", data.thienKhi)
                            + " DiaKhi=" + String.format("%.1f", data.diaKhi)
                        ), false);
                        return 1;
                    }))
            )
        );
    }
}
