package com.andyanh.cotienaddon.command;

import com.andyanh.cotienaddon.data.CoTienData;
import com.andyanh.cotienaddon.entity.DiaSinhEntity;
import com.andyanh.cotienaddon.init.CoTienAttachments;
import com.andyanh.cotienaddon.system.ThangTienManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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

    public static String getDaoRankName(double v) {
        if (v <= 0)         return "§8Không";
        if (v <= 100)       return "§7Phổ Thông";
        if (v <= 500)       return "§fChuẩn Đại Sư";
        if (v <= 1_000)     return "§fĐại Sư";
        if (v <= 5_000)     return "§aChuẩn Tông Sư";
        if (v <= 10_000)    return "§aTông Sư";
        if (v <= 50_000)    return "§bChuẩn Đại Tông Sư";
        if (v <= 100_000)   return "§bĐại Tông Sư";
        if (v <= 500_000)   return "§eChuẩn Vô Thượng Đại Tông Sư";
        if (v <= 1_000_000) return "§6Vô Thượng Đại Tông Sư";
        return "§d✦ Vô Cực Đại Đạo Sư ✦";
    }

    private static boolean setDaoField(net.guzhenren.network.GuzhenrenModVariables.PlayerVariables gv, String name, double v) {
        switch (name) {
            case "hanhdao","xingdao"    -> gv.liupai_xingdao    = v;
            case "tiendao","tiandao"    -> gv.liupai_tiandao    = v;
            case "phonddao","fengdao"   -> gv.liupai_fengdao    = v;
            case "leidao"               -> gv.liupai_leidao     = v;
            case "thuidao","shuidao"    -> gv.liupai_shuidao    = v;
            case "viemdao","yandao"     -> gv.liupai_yandao     = v;
            case "mocdao","mudao"       -> gv.liupai_mudao      = v;
            case "thodao","tudao"       -> gv.liupai_tudao      = v;
            case "guangdao"             -> gv.liupai_guangdao   = v;
            case "andao"                -> gv.liupai_andao      = v;
            case "kiemdao","jiandao"    -> gv.liupai_jiandao    = v;
            case "luyndao","liandao"    -> gv.liupai_liandao    = v;
            case "hundao"               -> gv.liupai_hundao     = v;
            case "vundao","yundao"      -> gv.liupai_yundao     = v;
            case "vundao2","yundao2"    -> gv.liupai_yundao2    = v;
            case "bangxuedao","bingxuedao" -> gv.liupai_bingxuedao = v;
            case "kimdao","jindao"      -> gv.liupai_jindao     = v;
            case "nhandao","rendao"     -> gv.liupai_rendao     = v;
            case "tridao","zhidao"      -> gv.liupai_zhidao     = v;
            case "trandao","zhendao"    -> gv.liupai_zhendao    = v;
            case "khidao","qidao"       -> gv.liupai_qidao      = v;
            case "nudao"                -> gv.liupai_nudao      = v;
            case "lucdao","lidao"       -> gv.liupai_lidao      = v;
            case "anhdao","yingdao"     -> gv.liupai_yingdao    = v;
            case "hoadao","huadao"      -> gv.liupai_huadao     = v;
            case "nguvetdao","yuedao"   -> gv.liupai_yuedao     = v;
            case "huyetdao","xuedao"    -> gv.liupai_xuedao     = v;
            case "dandao"               -> gv.liupai_dandao     = v;
            case "bangdao","bingdao"    -> gv.liupai_bingdao    = v;
            case "hoandao","huandao"    -> gv.liupai_huandao    = v;
            case "docdao","dudao"       -> gv.liupai_dudao      = v;
            case "mongdao","mengdao"    -> gv.liupai_mengdao    = v;
            case "daodao"               -> gv.liupai_daodao     = v;
            case "cotdao","gudao"       -> gv.Liupai_gudao      = v;
            case "hudao","xudao"        -> gv.liupai_xudao      = v;
            case "phihanhhdao","feixingdao" -> gv.liupai_feixingdao = v;
            case "bienhoadao","bianhuadao" -> gv.liupai_bianhuadao = v;
            case "thaudao","toudao"     -> gv.liupai_toudao     = v;
            case "thudao2","shidao"     -> gv.liupai_shidao     = v;
            case "tamdao","xindao"      -> gv.liupai_xindao     = v;
            case "lucdao2","lvdao"      -> gv.liupai_lvdao      = v;
            case "amdao","yindao"       -> gv.liupai_yindao     = v;
            case "kimdao2","jindao2"    -> gv.liupai_jindao2    = v;
            case "trudao","zhoudao"     -> gv.liupai_zhoudao    = v;
            default -> { return false; }
        }
        return true;
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("cotien")
            .requires(src -> src.hasPermission(0)) // mọi player dùng được; sub-commands tự set permission riêng
            .then(Commands.literal("debug")
                .requires(src -> src.hasPermission(2)) // debug chỉ op
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
                .then(Commands.literal("settn")
                    .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                        .executes(ctx -> {
                            ServerPlayer sp = ctx.getSource().getPlayerOrException();
                            double amount = DoubleArgumentType.getDouble(ctx, "amount");
                            CoTienData data = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());
                            data.tienNguyen = amount;
                            sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                "§b[Debug] Tiên Nguyên = " + amount), false);
                            return 1;
                        })))
                .then(Commands.literal("setgrade")
                    .then(Commands.argument("grade", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1, 4))
                        .executes(ctx -> {
                            ServerPlayer sp = ctx.getSource().getPlayerOrException();
                            int grade = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(ctx, "grade");
                            CoTienData data = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());
                            data.phucDiaGrade = grade;
                            sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
                            String[] names = {"", "Hạ đẳng", "Trung đẳng", "Thượng đẳng", "Siêu đẳng"};
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                "§a[Debug] Phúc Địa = " + names[grade]), false);
                            return 1;
                        })))
                .then(Commands.literal("kiep")
                    .executes(ctx -> {
                        ServerPlayer sp = ctx.getSource().getPlayerOrException();
                        CoTienData data = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());
                        com.andyanh.cotienaddon.event.PhucDiaEventHandler.startThienKiep(sp, data);
                        ctx.getSource().sendSuccess(() -> Component.literal(
                            "§c[Debug] Thiên Kiếp bắt đầu — 3 wave, 90 giây!"), false);
                        return 1;
                    }))
                .then(Commands.literal("ditai")
                    .executes(ctx -> {
                        ServerPlayer sp = ctx.getSource().getPlayerOrException();
                        CoTienData data = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());
                        com.andyanh.cotienaddon.event.PhucDiaEventHandler.startDiaTai(sp, data);
                        ctx.getSource().sendSuccess(() -> Component.literal(
                            "§6[Debug] Địa Tai bắt đầu — 3 wave, 90 giây!"), false);
                        return 1;
                    }))
                .then(Commands.literal("questcomplete")
                    .executes(ctx -> {
                        ServerPlayer sp = ctx.getSource().getPlayerOrException();
                        CoTienData data = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());
                        if (data.dialinhQuestType < 0) {
                            ctx.getSource().sendFailure(Component.literal("Không có nhiệm vụ đang làm."));
                            return 0;
                        }
                        data.dialinhQuestProgress = data.dialinhQuestGoal;
                        sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
                        ctx.getSource().sendSuccess(() -> Component.literal(
                            "§a[Debug] Quest hoàn thành! Shift+Click Địa Linh để nhận thưởng."), false);
                        return 1;
                    }))
                .then(Commands.literal("questreset")
                    .executes(ctx -> {
                        ServerPlayer sp = ctx.getSource().getPlayerOrException();
                        CoTienData data = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());
                        data.dialinhQuestType = com.andyanh.cotienaddon.data.CoTienData.QUEST_NONE;
                        data.dialinhQuestProgress = 0; data.dialinhQuestGoal = 0; data.dialinhQuestRewardTN = 0;
                        data.dialinhBondQuestCount = 0; data.dialinhBondQuestsDone = 0;
                        data.dialinhBondComplete = false;
                        sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
                        ctx.getSource().sendSuccess(() -> Component.literal("§e[Debug] Reset toàn bộ quest Địa Linh."), false);
                        return 1;
                    }))
                .then(Commands.literal("spawnores")
                    .executes(ctx -> {
                        ServerPlayer sp = ctx.getSource().getPlayerOrException();
                        if (!(sp.level() instanceof net.minecraft.server.level.ServerLevel sl)) return 0;
                        CoTienData data = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());
                        if (!com.andyanh.cotienaddon.system.PhucDiaManager.isPhucDiaDimension(sl.dimension())) {
                            ctx.getSource().sendFailure(Component.literal("Phải đứng trong Phúc Địa!"));
                            return 0;
                        }
                        com.andyanh.cotienaddon.event.PhucDiaEventHandler.spawnNguyenThach(sl, sp, data);
                        ctx.getSource().sendSuccess(() -> Component.literal("§5[Debug] Spawn quặng Nguyên Thạch + Khối Tiên Nguyên!"), false);
                        return 1;
                    }))
                .then(Commands.literal("buythachnhan")
                    .executes(ctx -> {
                        ServerPlayer sp = ctx.getSource().getPlayerOrException();
                        if (!(sp.level() instanceof net.minecraft.server.level.ServerLevel sl)) return 0;
                        CoTienData data = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());
                        int cost = 80;
                        if (data.tienNguyen < cost) {
                            ctx.getSource().sendFailure(Component.literal("§cCần " + cost + " Tiên Nguyên! (đang có: " + (int)data.tienNguyen + ")"));
                            return 0;
                        }
                        // Giới hạn số lượng theo grade
                        int maxCount = data.phucDiaGrade;
                        var existing = sl.getEntitiesOfClass(com.andyanh.cotienaddon.entity.ThachNhanEntity.class,
                                sp.getBoundingBox().inflate(8192), e -> sp.getUUID().toString().equals(e.getOwnerUUID()));
                        if (existing.size() >= maxCount) {
                            ctx.getSource().sendFailure(Component.literal("§cĐã đạt giới hạn Thạch Nhân! (max " + maxCount + " theo grade " + data.phucDiaGrade + ")"));
                            return 0;
                        }
                        var tn = com.andyanh.cotienaddon.init.CoTienEntities.THACH_NHAN.get().create(sl);
                        if (tn == null) return 0;
                        tn.setOwnerUUID(sp.getUUID().toString());
                        tn.setPos(sp.getX() + 1.5, sp.getY(), sp.getZ() + 1.5);
                        tn.applyStats();
                        tn.getPersistentData().putBoolean("cotien_spawned", true);
                        tn.setCustomName(net.minecraft.network.chat.Component.literal("§8⚒ Thạch Nhân"));
                        tn.setCustomNameVisible(true);
                        sl.addFreshEntity(tn);
                        data.tienNguyen -= cost;
                        sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
                        ctx.getSource().sendSuccess(() -> Component.literal("§a⚒ Thạch Nhân được tạo! (-" + cost + " TN)"), false);
                        return 1;
                    }))
                .then(Commands.literal("xray")
                    .executes(ctx -> {
                        ServerPlayer sp = ctx.getSource().getPlayerOrException();
                        if (!(sp.level() instanceof net.minecraft.server.level.ServerLevel sl)) return 0;

                        int px = (int) sp.getX(), pz = (int) sp.getZ();
                        int radius = 60;

                        var modOre = net.guzhenren.init.GuzhenrenModBlocks.FANGKUAIYUANSHIYUANKUANG.get();
                        var khoiTN = com.andyanh.cotienaddon.init.CoTienBlocks.KHOI_TIEN_NGUYEN.get();

                        java.util.List<net.minecraft.core.BlockPos> nguyenThachList = new java.util.ArrayList<>();
                        java.util.List<net.minecraft.core.BlockPos> khoiList = new java.util.ArrayList<>();

                        for (int x = px - radius; x <= px + radius; x += 2) {
                            for (int z = pz - radius; z <= pz + radius; z += 2) {
                                for (int y = 1; y <= 50; y++) {
                                    var pos = new net.minecraft.core.BlockPos(x, y, z);
                                    var block = sl.getBlockState(pos).getBlock();
                                    if (block == modOre) nguyenThachList.add(pos);
                                    else if (block == khoiTN) khoiList.add(pos);
                                }
                            }
                        }

                        // Spawn particles tại vị trí quặng (hiện rõ qua đất)
                        var dustNT = new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(0.2f, 0.8f, 0.2f), 2.0f);
                        var dustKT = new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(1.0f, 0.9f, 0.0f), 2.5f);
                        for (var pos : nguyenThachList) {
                            sl.sendParticles(sp, dustNT, true, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, 5, 0.3, 0.3, 0.3, 0);
                        }
                        for (var pos : khoiList) {
                            sl.sendParticles(sp, dustKT, true, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, 8, 0.3, 0.3, 0.3, 0);
                        }

                        int ntCount = nguyenThachList.size();
                        int ktCount = khoiList.size();

                        ctx.getSource().sendSuccess(() -> Component.literal(
                            "§a[Xray] Quặng Nguyên Thạch: §f" + ntCount +
                            " §a| Khối Tiên Nguyên: §6" + ktCount +
                            "\n§7Particles đã bắn — xanh lá = NT, vàng = Tiên Nguyên"), false);

                        // In tọa độ gần nhất
                        if (!khoiList.isEmpty()) {
                            var nearest = khoiList.get(0);
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                "§6★ Khối TN gần nhất: §f" + nearest.getX() + " " + nearest.getY() + " " + nearest.getZ()), false);
                        }
                        if (!nguyenThachList.isEmpty()) {
                            var nearest = nguyenThachList.get(0);
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                "§a★ Nguyên Thạch gần nhất: §f" + nearest.getX() + " " + nearest.getY() + " " + nearest.getZ()), false);
                        }
                        return 1;
                    }))
                .then(Commands.literal("seal")
                    .executes(ctx -> {
                        ServerPlayer sp = ctx.getSource().getPlayerOrException();
                        sp.getPersistentData().putInt("tran_vu_sealed", 200);
                        ctx.getSource().sendSuccess(() -> Component.literal(
                            "§9☯ [Debug] Băng Phong Trận — bị phong ấn 10 giây!"), false);
                        return 1;
                    }))
                .then(Commands.literal("unseal")
                    .executes(ctx -> {
                        ServerPlayer sp = ctx.getSource().getPlayerOrException();
                        sp.getPersistentData().remove("tran_vu_sealed");
                        ctx.getSource().sendSuccess(() -> Component.literal(
                            "§b[Debug] Băng phong tan rã."), false);
                        return 1;
                    }))
                .then(Commands.literal("setdao")
                    .then(Commands.argument("dao", com.mojang.brigadier.arguments.StringArgumentType.word())
                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                            .executes(ctx -> {
                                ServerPlayer sp = ctx.getSource().getPlayerOrException();
                                String dao = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "dao").toLowerCase();
                                double amount = DoubleArgumentType.getDouble(ctx, "amount");
                                var gv = sp.getData(net.guzhenren.network.GuzhenrenModVariables.PLAYER_VARIABLES);
                                boolean found = setDaoField(gv, dao, amount);
                                if (!found) {
                                    ctx.getSource().sendFailure(Component.literal("§cĐạo không hợp lệ! Dùng: tiendao, thudao, hanhdao, phonddao, leidao, thuidao, viemdao, mocdao, thodao, guangdao, andao, kiemdao, luyndao, hundao, vundao, bangxuedao, kimdao, nhandao, tridao, trandao, khidao, nudao, lucdao, anhdao, hoadao, nguvetdao, huyetdao, dandao, bangdao, hoandao, docdao, mongdao, daodao, cotdao, hudao, phihanhhdao, bienhoadao, thaudao, thudao2, tamdao, lucdao2, amdao, kimdao2, trudao"));
                                    return 0;
                                }
                                gv.markSyncDirty();
                                var check = com.andyanh.cotienaddon.system.ThangTienManager.checkDaoNganCondition(gv);
                                final String daoF = dao; final double amtF = amount;
                                final String rankName = getDaoRankName(amount);
                                ctx.getSource().sendSuccess(() -> Component.literal(
                                    "§6✦ Đạo Ngân [" + daoF + "] = §f" + String.format("%.0f", amtF)
                                    + " → " + rankName
                                    + "\n§7Số đạo ≥100k: §e" + check.count() + "/44"
                                    + (check.count() >= 2 ? " §a✓ Đủ thăng tiên" : " §c✗ Chưa đủ")), false);
                                return 1;
                            }))))
                .then(Commands.literal("listdao")
                    .executes(ctx -> {
                        ServerPlayer sp = ctx.getSource().getPlayerOrException();
                        var gv = sp.getData(net.guzhenren.network.GuzhenrenModVariables.PLAYER_VARIABLES);
                        var check = com.andyanh.cotienaddon.system.ThangTienManager.checkDaoNganCondition(gv);

                        // Build top-5 dao list
                        record DaoEntry(String name, double value) {}
                        java.util.List<DaoEntry> all = java.util.List.of(
                            new DaoEntry("Thiên Đạo", gv.liupai_tiandao), new DaoEntry("Thổ Đạo", gv.liupai_tudao),
                            new DaoEntry("Hành Đạo", gv.liupai_xingdao), new DaoEntry("Phong Đạo", gv.liupai_fengdao),
                            new DaoEntry("Lôi Đạo", gv.liupai_leidao), new DaoEntry("Thủy Đạo", gv.liupai_shuidao),
                            new DaoEntry("Viêm Đạo", gv.liupai_yandao), new DaoEntry("Mộc Đạo", gv.liupai_mudao),
                            new DaoEntry("Quang Đạo", gv.liupai_guangdao), new DaoEntry("Ám Đạo", gv.liupai_andao),
                            new DaoEntry("Kiếm Đạo", gv.liupai_jiandao), new DaoEntry("Luyện Đạo", gv.liupai_liandao),
                            new DaoEntry("Hồn Đạo", gv.liupai_hundao), new DaoEntry("Vân Đạo", gv.liupai_yundao),
                            new DaoEntry("Huyết Đạo", gv.liupai_xuedao), new DaoEntry("Đan Đạo", gv.liupai_dandao),
                            new DaoEntry("Kim Đạo", gv.liupai_jindao), new DaoEntry("Nhân Đạo", gv.liupai_rendao),
                            new DaoEntry("Trí Đạo", gv.liupai_zhidao), new DaoEntry("Độc Đạo", gv.liupai_dudao),
                            new DaoEntry("Khí Đạo", gv.liupai_qidao), new DaoEntry("Lực Đạo", gv.liupai_lidao),
                            new DaoEntry("Hoa Đạo", gv.liupai_huadao), new DaoEntry("Hoán Đạo", gv.liupai_huandao)
                        );
                        var top5 = all.stream()
                            .filter(e -> e.value() > 0)
                            .sorted((a, b) -> Double.compare(b.value(), a.value()))
                            .limit(5)
                            .toList();

                        StringBuilder sb = new StringBuilder();
                        sb.append("§6═══ Đạo Ngân ═══\n");
                        sb.append("§7Đạt Đại Tông Sư (≥100k): §e").append(check.count()).append("/44");
                        sb.append(check.count() >= 2 ? " §a✓ Đủ thăng tiên\n" : " §c✗ Chưa đủ\n");
                        if (!top5.isEmpty()) {
                            sb.append("§7Top đạo:\n");
                            for (int i = 0; i < top5.size(); i++) {
                                var e = top5.get(i);
                                sb.append("§7  ").append(i + 1).append(". §f").append(e.name())
                                  .append(" §8(").append(String.format("%.0f", e.value())).append(") ")
                                  .append(getDaoRankName(e.value())).append("\n");
                            }
                        } else {
                            sb.append("§8Chưa có Đạo Ngân nào.\n");
                        }
                        final String msg = sb.toString().stripTrailing();
                        ctx.getSource().sendSuccess(() -> Component.literal(msg), false);
                        return 1;
                    }))
            )
            .then(Commands.literal("setkongqiao")
                .requires(src -> src.hasPermission(2))
                .then(Commands.argument("value", IntegerArgumentType.integer(0, 36))
                    .executes(ctx -> {
                        ServerPlayer sp = ctx.getSource().getPlayerOrException();
                        int v = IntegerArgumentType.getInteger(ctx, "value");
                        var vars = sp.getData(net.guzhenren.network.GuzhenrenModVariables.PLAYER_VARIABLES);
                        vars.kongqiao = v;
                        // Set zuida_zhenyuan theo grade (giống UikongqiaoquedingProcedure)
                        if (v == 3) {
                            vars.zuida_zhenyuan = 1000.0;  // Giáp Đẳng / Thập Tuyệt
                        } else if (v == 1 || v == 4) {
                            vars.zuida_zhenyuan = 800.0;   // Ất Đẳng
                        } else if (v >= 5 && v <= 7) {
                            vars.zuida_zhenyuan = 500.0;   // Bính Đẳng
                        } else if (v >= 8 && v <= 10) {
                            vars.zuida_zhenyuan = 300.0;   // Đinh Đẳng
                        }
                        vars.markSyncDirty();
                        String grade = switch (v) {
                            case 1, 4 -> "Ất Đẳng (zuida_zhenyuan=800)";
                            case 3    -> "Giáp Đẳng — ✦ THẬP TUYỆT THIÊN TỬ ✦ (zuida_zhenyuan=1000)";
                            default   -> v >= 5 && v <= 7 ? "Bính Đẳng (zuida_zhenyuan=500)" :
                                         v >= 8 && v <= 10 ? "Đinh Đẳng (zuida_zhenyuan=300)" : "Không/Đặc biệt";
                        };
                        final String g = grade;
                        ctx.getSource().sendSuccess(() -> Component.literal(
                            "§d✦ Kongqiao = §f" + v + " §7(" + g + ")"), false);
                        return 1;
                    })))
            .then(Commands.literal("setdaode")
                .requires(src -> src.hasPermission(2))
                .then(Commands.argument("value", DoubleArgumentType.doubleArg(-999999, 999999))
                    .executes(ctx -> {
                        ServerPlayer sp = ctx.getSource().getPlayerOrException();
                        double v = DoubleArgumentType.getDouble(ctx, "value");
                        var vars = sp.getData(net.guzhenren.network.GuzhenrenModVariables.PLAYER_VARIABLES);
                        vars.daode = v;
                        vars.markSyncDirty();
                        String label;
                        if      (v <= -100000) label = "Táng tận thiên lương";
                        else if (v <= -10000)  label = "Thập ác bất xá";
                        else if (v <= -1000)   label = "Ác quán mãn doanh";
                        else if (v <= -100)    label = "Thâm độc độc ác";
                        else if (v < 0)        label = "Tâm mật thủ lạt";
                        else if (v == 0)       label = "Không";
                        else if (v <= 99)      label = "Trợ nhân vi lạc";
                        else if (v <= 999)     label = "Tích thiện thành đức";
                        else if (v <= 9999)    label = "Quang minh lỗi lạc";
                        else if (v <= 99999)   label = "Đức cao vọng trọng";
                        else                   label = "Hoạt Phật tại thế";
                        final String l = label;
                        ctx.getSource().sendSuccess(() -> Component.literal(
                            "§e✦ Đạo Đức = §f" + v + " §7(" + l + ")"), false);
                        return 1;
                    })))
            .then(Commands.literal("setqiyun")
                .requires(src -> src.hasPermission(2))
                .then(Commands.argument("value", DoubleArgumentType.doubleArg(-200, 200))
                    .executes(ctx -> {
                        ServerPlayer sp = ctx.getSource().getPlayerOrException();
                        double v = DoubleArgumentType.getDouble(ctx, "value");
                        var vars = sp.getData(net.guzhenren.network.GuzhenrenModVariables.PLAYER_VARIABLES);
                        vars.qiyun = v;
                        // Set qiyun_shangxian = v để KE1Procedure không cap và không drift vượt quá
                        vars.qiyun_shangxian = v;
                        vars.markSyncDirty();
                        String label;
                        if      (v < -100) label = "<Hắc Quan Tử Vận>";
                        else if (v >= -100 && v <= -80) label = "Hắc Quan Tử Vận";
                        else if (v > -80  && v <= -60)  label = "Mệnh đồ đa suyễn";
                        else if (v > -60  && v <= -40)  label = "Thời quai vận kiển";
                        else if (v > -40  && v <= -20)  label = "Họa tại đán tịch";
                        else if (v > -20  && v < 0)     label = "Họa bất đơn hành";
                        else if (v == 0)                label = "Không";
                        else if (v > 0   && v <= 20)    label = "Thời lai vận chuyển";
                        else if (v > 20  && v <= 40)    label = "Thiên tùy nhân nguyện";
                        else if (v > 40  && v <= 60)    label = "Thời vận hanh thông";
                        else if (v > 60  && v <= 80)    label = "Cát tinh cao chiếu";
                        else if (v > 80  && v <= 100)   label = "Hồng Vận Tề Thiên";
                        else                            label = "<Hồng Vận Tề Thiên>";
                        final String l = label;
                        ctx.getSource().sendSuccess(() -> Component.literal(
                            "§b✦ Khí Vận = §f" + v + " §7(" + l + ")"), false);
                        return 1;
                    })))
            .then(Commands.literal("settizhi")
                .requires(src -> src.hasPermission(2))
                .then(Commands.argument("value", IntegerArgumentType.integer(0, 15))
                    .executes(ctx -> {
                        ServerPlayer sp = ctx.getSource().getPlayerOrException();
                        int v = IntegerArgumentType.getInteger(ctx, "value");
                        var vars = sp.getData(net.guzhenren.network.GuzhenrenModVariables.PLAYER_VARIABLES);
                        vars.tizhi = v;
                        vars.markSyncDirty();
                        String[] names = {
                            "Không (0)",
                            "1 — Thái Nhật Dương Mãng Thể",
                            "2 — Cổ Nguyệt Âm Hoang Thể",
                            "3 — Bắc Minh Băng Phách Thể",
                            "4 — Sâm Hải Luân Hồi Thể",
                            "5 — Viêm Hoàng Lôi Trạch Thể",
                            "6 — Vạn Kim Diệu Hoa Thể",
                            "7 — Đại Lực Chân Võ Thể",
                            "8 — Tiêu Dao Trí Tâm Thể",
                            "9 — Hậu Thổ Nguyên Ương Thể",
                            "10 — Vũ Trụ Đại Diễn Thể",
                            "11 — Chí Tôn Tiên Thai Thể",
                            "12 — Thuần Mộng Cầu Chân Thể",
                            "13 — Khí Vận Chi Tử",
                            "14 — Thiên Ngoại Chi Ma",
                            "15 — Chính Đạo Thiện Đức Thân"
                        };
                        final String name = v < names.length ? names[v] : "Không";
                        ctx.getSource().sendSuccess(() -> Component.literal(
                            "§d✦ Thể chất: §f" + name), false);
                        return 1;
                    })))
            .then(Commands.literal("tizhi")  // alias xem tất cả thể chất
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> Component.literal(
                        "§d═══ Danh sách Thập Tuyệt Thể ═══\n" +
                        "§f/cotien settizhi <0-15>\n" +
                        "§71 §fThái Nhật Dương Mãng Thể\n" +
                        "§72 §fCổ Nguyệt Âm Hoang Thể\n" +
                        "§73 §fBắc Minh Băng Phách Thể\n" +
                        "§74 §fSâm Hải Luân Hồi Thể\n" +
                        "§75 §fViêm Hoàng Lôi Trạch Thể\n" +
                        "§76 §fVạn Kim Diệu Hoa Thể\n" +
                        "§77 §fĐại Lực Chân Võ Thể\n" +
                        "§78 §fTiêu Dao Trí Tâm Thể\n" +
                        "§79 §fHậu Thổ Nguyên Ương Thể\n" +
                        "§710 §fVũ Trụ Đại Diễn Thể\n" +
                        "§711 §fChí Tôn Tiên Thai Thể\n" +
                        "§712 §fThuần Mộng Cầu Chân Thể\n" +
                        "§713 §fKhí Vận Chi Tử\n" +
                        "§714 §fThiên Ngoại Chi Ma\n" +
                        "§715 §fChính Đạo Thiện Đức Thân"
                    ), false);
                    return 1;
                }))
            .then(Commands.literal("dialinhname")
                .then(Commands.argument("name", StringArgumentType.greedyString())
                    .executes(ctx -> {
                        ServerPlayer sp = ctx.getSource().getPlayerOrException();
                        String name = StringArgumentType.getString(ctx, "name");
                        if (name.length() > 24) {
                            ctx.getSource().sendFailure(Component.literal("Tên quá dài (tối đa 24 ký tự)"));
                            return 0;
                        }
                        CoTienData data = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());
                        if (!data.dialinhBondComplete) {
                            ctx.getSource().sendFailure(Component.literal("§c[Địa Linh] Phải hoàn thành nhiệm vụ Nhận Chủ trước!"));
                            return 0;
                        }
                        // Tìm Địa Linh của player trong level hiện tại
                        if (!(sp.level() instanceof ServerLevel sl)) return 0;
                        var list = sl.getEntitiesOfClass(DiaSinhEntity.class,
                                sp.getBoundingBox().inflate(8192),
                                e -> sp.getUUID().toString().equals(e.getOwnerUUID()));
                        if (list.isEmpty()) {
                            ctx.getSource().sendFailure(Component.literal("Không tìm thấy Địa Linh trong Phúc Địa!"));
                            return 0;
                        }
                        data.dialinhCustomName = name;
                        sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
                        list.get(0).updateStatsFromOwner(data);
                        ctx.getSource().sendSuccess(() -> Component.literal(
                            "§a☯ Địa Linh nhận tên: §f" + name), false);
                        return 1;
                    })))
            .then(Commands.literal("tonhieu")
                .then(Commands.literal("set")
                    .then(Commands.argument("name", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            ServerPlayer sp = ctx.getSource().getPlayerOrException();
                            String name = StringArgumentType.getString(ctx, "name");
                            CoTienData data = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());
                            var gv = sp.getData(net.guzhenren.network.GuzhenrenModVariables.PLAYER_VARIABLES);
                            // Yêu cầu: Bát chuyển đỉnh phong (zhuanshu>=8, jieduan>=4) hoặc Cửu chuyển (zhuanshu>=9)
                            boolean eligible = (gv.zhuanshu >= 9) || (gv.zhuanshu >= 8 && gv.jieduan >= 4);
                            if (!eligible) {
                                ctx.getSource().sendFailure(Component.literal(
                                    "§c[Tiên Khiếu] Cần đạt Bát Chuyển Đỉnh Phong (zhuanshu≥8.0, jieduan≥4) mới có thể thiết lập Danh Hiệu Tôn!"
                                    + "\n§7  Hiện tại: zhuanshu=" + String.format("%.1f", gv.zhuanshu) + ", jieduan=" + String.format("%.0f", gv.jieduan)));
                                return 0;
                            }
                            if (name.length() > 20) {
                                ctx.getSource().sendFailure(Component.literal("§cTên quá dài! Tối đa 20 ký tự."));
                                return 0;
                            }
                            String type = gv.daode >= 0 ? "Tiên Tôn" : "Ma Tôn";
                            data.tonHieuName = name;
                            data.tonHieuEnabled = true;
                            sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
                            com.andyanh.cotienaddon.event.PhucDiaEventHandler.applyTonHieuNameplate(sp, data);
                            final String t = type;
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                "§a✦ Danh Hiệu Tôn: §f" + name + " " + t
                                + "\n§7  Đạo Đức: §e" + String.format("%.0f", gv.daode)
                                + " §7→ " + (gv.daode >= 0 ? "§bTiên" : "§cMa") + " Tôn"
                                + "\n§7  Dùng §f/cotien tonhieu color <màu> §7để đổi màu"), false);
                            return 1;
                        })))
                .then(Commands.literal("color")
                    .then(Commands.argument("color", StringArgumentType.word())
                        .executes(ctx -> {
                            ServerPlayer sp = ctx.getSource().getPlayerOrException();
                            String colorStr = StringArgumentType.getString(ctx, "color").toLowerCase();
                            CoTienData data = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());
                            if (!data.tonHieuEnabled) {
                                ctx.getSource().sendFailure(Component.literal("§cChưa thiết lập Danh Hiệu Tôn! Dùng /cotien tonhieu set <tên> trước."));
                                return 0;
                            }
                            int rgb = switch (colorStr) {
                                case "vang", "gold", "vàng"     -> 0xFFD700;
                                case "do", "red", "đỏ"          -> 0xFF4444;
                                case "xanh", "blue", "cyan"     -> 0x55FFFF;
                                case "tim", "purple", "tím"     -> 0xAA55FF;
                                case "trang", "white", "trắng"  -> 0xFFFFFF;
                                case "xanhla", "green"          -> 0x55FF55;
                                case "cam", "orange"            -> 0xFF9900;
                                case "hong", "pink", "hồng"     -> 0xFF88CC;
                                default -> {
                                    // Try parse hex #RRGGBB or RRGGBB
                                    try {
                                        String hex = colorStr.startsWith("#") ? colorStr.substring(1) : colorStr;
                                        yield Integer.parseUnsignedInt(hex, 16) & 0xFFFFFF;
                                    } catch (NumberFormatException e) {
                                        yield -1;
                                    }
                                }
                            };
                            if (rgb == -1) {
                                ctx.getSource().sendFailure(Component.literal(
                                    "§cMàu không hợp lệ! Dùng: vàng, đỏ, xanh, tím, trắng, cam, hồng hoặc mã hex #RRGGBB"));
                                return 0;
                            }
                            data.tonHieuColor = rgb;
                            sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
                            com.andyanh.cotienaddon.event.PhucDiaEventHandler.applyTonHieuNameplate(sp, data);
                            final int c = rgb;
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                "§a✦ Màu Danh Hiệu Tôn → §r" +
                                net.minecraft.network.chat.Component.literal("■ #" + String.format("%06X", c))
                                    .withStyle(s -> s.withColor(net.minecraft.network.chat.TextColor.fromRgb(c)))
                                    .getString()), false);
                            return 1;
                        })))
                .then(Commands.literal("reset")
                    .executes(ctx -> {
                        ServerPlayer sp = ctx.getSource().getPlayerOrException();
                        CoTienData data = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());
                        data.tonHieuEnabled = false;
                        data.tonHieuName = "";
                        sp.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);
                        // Restore Cổ Tiên base nameplate
                        sp.setCustomName(net.minecraft.network.chat.Component.literal(
                            "§b[Tiên Cổ] §7" + sp.getName().getString()));
                        ctx.getSource().sendSuccess(() -> Component.literal("§7✦ Đã xóa Danh Hiệu Tôn."), false);
                        return 1;
                    }))
                .then(Commands.literal("info")
                    .executes(ctx -> {
                        ServerPlayer sp = ctx.getSource().getPlayerOrException();
                        CoTienData data = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());
                        var gv = sp.getData(net.guzhenren.network.GuzhenrenModVariables.PLAYER_VARIABLES);
                        if (!data.tonHieuEnabled) {
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                "§7Chưa có Danh Hiệu Tôn. Dùng §f/cotien tonhieu set <tên>"), false);
                        } else {
                            String type = gv.daode >= 0 ? "Tiên Tôn" : "Ma Tôn";
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                "§e✦ Danh Hiệu: §f" + data.tonHieuName + " " + type
                                + "\n§7  Màu: §f#" + String.format("%06X", data.tonHieuColor)
                                + "\n§7  Đạo Đức: §e" + String.format("%.0f", gv.daode)), false);
                        }
                        return 1;
                    })))
            .then(Commands.literal("sect")
                .then(Commands.literal("accept")
                    .executes(ctx -> {
                        ServerPlayer sp = ctx.getSource().getPlayerOrException();
                        com.andyanh.cotienaddon.network.SectNetwork.SectActionPacket.handle(
                                new com.andyanh.cotienaddon.network.SectNetwork.SectActionPacket(
                                        com.andyanh.cotienaddon.network.SectNetwork.SectActionPacket.ActionType.ACCEPT.ordinal(), ""),
                                null);
                        // Gọi trực tiếp server-side
                        var sectData = com.andyanh.cotienaddon.system.SectSavedData.get(sp.level());
                        java.util.UUID sectId = com.andyanh.cotienaddon.system.SectSavedData.getPendingInvite(sp.getUUID());
                        if (sectId == null) {
                            ctx.getSource().sendFailure(Component.literal("§c[Tông Môn] Không có lời mời nào!"));
                            return 0;
                        }
                        if (sectData.getSectOfPlayer(sp.getUUID()) != null) {
                            ctx.getSource().sendFailure(Component.literal("§c[Tông Môn] Bạn đã thuộc tông môn rồi!"));
                            return 0;
                        }
                        sectData.addMember(sectId, sp.getUUID());
                        var joined = sectData.getSect(sectId);
                        if (joined != null) {
                            com.andyanh.cotienaddon.network.SectNetwork.SectActionPacket.populateNames(sp.server, joined);
                            for (java.util.UUID m : joined.members) {
                                var mp = sp.server.getPlayerList().getPlayer(m);
                                if (mp != null) mp.connection.send(new com.andyanh.cotienaddon.network.SectNetwork.SyncSectPacket(joined));
                            }
                            ctx.getSource().sendSuccess(() -> Component.literal("§a[Tông Môn] Đã gia nhập §6" + joined.name), false);
                        }
                        return 1;
                    }))
                .then(Commands.literal("deny")
                    .executes(ctx -> {
                        ServerPlayer sp = ctx.getSource().getPlayerOrException();
                        com.andyanh.cotienaddon.system.SectSavedData.getPendingInvite(sp.getUUID());
                        ctx.getSource().sendSuccess(() -> Component.literal("§7[Tông Môn] Đã từ chối lời mời."), false);
                        return 1;
                    })))
            .then(Commands.literal("acceptinvite")
                .requires(src -> src.hasPermission(0)) // mọi player đều dùng được
                .then(Commands.argument("ownerName", StringArgumentType.word())
                    .executes(ctx -> {
                        ServerPlayer sp = ctx.getSource().getPlayerOrException();
                        String ownerName = StringArgumentType.getString(ctx, "ownerName");
                        ServerPlayer owner = sp.server.getPlayerList().getPlayerByName(ownerName);
                        if (owner == null) {
                            ctx.getSource().sendFailure(Component.literal("§c" + ownerName + " không online!"));
                            return 0;
                        }
                        if (owner.getUUID().equals(sp.getUUID())) {
                            ctx.getSource().sendFailure(Component.literal("§cKhông thể vào Phúc Địa của chính mình bằng lệnh này!"));
                            return 0;
                        }
                        boolean ok = com.andyanh.cotienaddon.system.PhucDiaManager.teleportToOwnerPhucDia(sp, owner);
                        return ok ? 1 : 0;
                    })))
        );
    }
}
