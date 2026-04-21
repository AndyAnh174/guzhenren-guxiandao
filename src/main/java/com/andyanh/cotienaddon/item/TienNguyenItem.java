package com.andyanh.cotienaddon.item;

import com.andyanh.cotienaddon.data.CoTienData;
import com.andyanh.cotienaddon.init.CoTienAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class TienNguyenItem extends Item {

    public static final double NGUYEN_PER_ITEM = 1.0;

    public TienNguyenItem() {
        super(new Item.Properties().stacksTo(64));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResultHolder.pass(player.getItemInHand(hand));

        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            CoTienData data = player.getData(CoTienAttachments.CO_TIEN_DATA.get());
            data.tienNguyen += NGUYEN_PER_ITEM;
            player.setData(CoTienAttachments.CO_TIEN_DATA.get(), data);

            player.sendSystemMessage(Component.literal(
                    String.format("§6✦ Nạp 1 Tiên Nguyên! Tổng: §e%.0f §6Tiên Nguyên", data.tienNguyen)));

            level.playSound(null, player.blockPosition(),
                    net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP,
                    net.minecraft.sounds.SoundSource.PLAYERS, 0.8f, 1.3f);

            if (!player.isCreative()) {
                stack.shrink(1);
            }
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§6Click phải để nạp §e" + (int)NGUYEN_PER_ITEM + " §6Tiên Nguyên vào người")
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("§7Tiên Nguyên được dùng để vận hành các Tiên Cổ")
                .withStyle(ChatFormatting.GRAY));
    }
}
