package com.andyanh.cotienaddon.item;

import com.andyanh.cotienaddon.CoTienAddon;
import com.andyanh.cotienaddon.data.CoTienData;
import com.andyanh.cotienaddon.init.CoTienAttachments;
import com.andyanh.cotienaddon.network.AnnexPhucDiaPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/**
 * Cô Hồn Tiên Địa Khế — drop khi chủ Phúc Địa chết ngoài Kiếp/Tai.
 * Lưu trữ thông tin Phúc Địa cũ. Cổ Tiên khác dùng để annex.
 */
public class OrphanedNodeItem extends Item {

    public OrphanedNodeItem() {
        super(new Item.Properties().stacksTo(1));
    }

    /** Tạo stack OrphanedNode từ CoTienData của chủ vừa chết */
    public static ItemStack create(CoTienData data, String victimName, String victimUUID) {
        ItemStack stack = new ItemStack(
                net.minecraft.core.registries.BuiltInRegistries.ITEM
                        .get(net.minecraft.resources.ResourceLocation.parse("cotienaddon:orphaned_node")));
        CompoundTag tag = new CompoundTag();
        tag.putString("victimUUID",  victimUUID);
        tag.putString("victimName",  victimName);
        tag.putInt   ("phucDiaSlot", data.phucDiaSlot);
        tag.putInt   ("phucDiaGrade", data.phucDiaGrade);
        tag.putInt   ("phucDiaLevel", data.phucDiaLevel);
        tag.putDouble("tienNguyen",  data.tienNguyen * 0.5); // 50% tài nguyên chuyển giao
        tag.putDouble("nhanKhi",     data.calcNhanKhi());
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.of(tag));
        return stack;
    }

    public static CompoundTag getData(ItemStack stack) {
        var cd = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        return cd != null ? cd.copyTag() : new CompoundTag();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResultHolder.pass(player.getItemInHand(hand));
        if (level.isClientSide) return InteractionResultHolder.success(player.getItemInHand(hand));

        ItemStack stack = player.getItemInHand(hand);
        CompoundTag tag = getData(stack);
        if (tag.isEmpty()) return InteractionResultHolder.fail(stack);

        if (!(player instanceof ServerPlayer sp)) return InteractionResultHolder.fail(stack);

        CoTienData data = sp.getData(CoTienAttachments.CO_TIEN_DATA.get());
        if (data.thangTienPhase < 4) {
            sp.sendSystemMessage(Component.literal("§c[Tiên Địa Khế] Chỉ Cổ Tiên mới có thể nuốt Phúc Địa!"));
            return InteractionResultHolder.fail(stack);
        }

        // Gửi packet annex
        PacketDistributor.sendToServer(new AnnexPhucDiaPacket(tag));
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> lines, TooltipFlag flag) {
        CompoundTag tag = getData(stack);
        if (tag.isEmpty()) {
            lines.add(Component.literal("§cDữ liệu bị hỏng!").withStyle(ChatFormatting.RED));
            return;
        }
        String[] grades = {"","Hạ đẳng","Trung đẳng","Thượng đẳng","Siêu đẳng"};
        int grade = Math.max(0, Math.min(4, tag.getInt("phucDiaGrade")));
        lines.add(Component.literal("§6Cô Hồn Tiên Địa Khế").withStyle(ChatFormatting.GOLD));
        lines.add(Component.literal("§7Chủ cũ: §f" + tag.getString("victimName")));
        lines.add(Component.literal("§7Phúc Địa: §e" + (grade>0?grades[grade]:"?") + " Cấp " + tag.getInt("phucDiaLevel")));
        lines.add(Component.literal("§7Tiên Nguyên kèm theo: §b" + String.format("%.0f", tag.getDouble("tienNguyen"))));
        lines.add(Component.literal("§7Nhân Khí: §e" + String.format("%.0f", tag.getDouble("nhanKhi"))));
        lines.add(Component.literal(""));
        lines.add(Component.literal("§6[Dùng] §7để Annex Phúc Địa này").withStyle(ChatFormatting.YELLOW));
        lines.add(Component.literal("§cYêu cầu: Nhân Khí ≥ §f" + String.format("%.0f", tag.getDouble("nhanKhi"))
                + " §c+ §f" + (grade * 5) + " §cĐạo Ngân").withStyle(ChatFormatting.RED));
    }
}
