package com.andyanh.cotienaddon.item;

import com.andyanh.cotienaddon.CoTienAddon;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
// Minecraft imported only in @OnlyIn method below

import java.util.List;
import net.minecraft.nbt.CompoundTag;

@EventBusSubscriber(modid = CoTienAddon.MODID)
public class ThangThanhVanTienGuItem extends Item {
    public static final int MAX_DURABILITY = 1000;
    
    public ThangThanhVanTienGuItem() {
        super(new Item.Properties().stacksTo(1).durability(MAX_DURABILITY).rarity(net.minecraft.world.item.Rarity.EPIC));
    }

    public static boolean isActive(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            return customData.copyTag().getBoolean("Active");
        }
        return false;
    }

    public static void setActive(ItemStack stack, boolean active) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putBoolean("Active", active));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slot, boolean selected) {
        if (!level.isClientSide && entity instanceof ServerPlayer sp) {
            if (isActive(stack)) {
                if (sp.tickCount % 20 == 0) {
                    int currentDmg = stack.getDamageValue();
                    
                    // Auto-consume Tiên Nguyên if empty or about to be empty
                    if (currentDmg >= MAX_DURABILITY - 1) {
                        boolean foundTienNguyen = false;
                        for (int i = 0; i < sp.getInventory().getContainerSize(); i++) {
                            ItemStack invStack = sp.getInventory().getItem(i);
                            if (isTienNguyen(invStack)) {
                                invStack.shrink(1);
                                // 1 Tiên Nguyên = 500 durability
                                currentDmg = Math.max(0, currentDmg - 500);
                                stack.setDamageValue(currentDmg);
                                sp.sendSystemMessage(Component.literal("§b✦ Thăng Thanh Vân Tiên Cổ đã tự động tiêu thụ 1 Tiên Nguyên (hồi 500 điểm)."));
                                foundTienNguyen = true;
                                break;
                            }
                        }
                        if (!foundTienNguyen) {
                            setActive(stack, false);
                            sp.sendSystemMessage(Component.literal("§c[Thăng Thanh Vân] Tiên Cổ đã cạn kiệt năng lượng! Tự động thu hồi thanh vân."));
                            return;
                        }
                    }

                    stack.setDamageValue(currentDmg + 1);

                    // Track usage for Dao marks + Tiên Nguyên cost
                    CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                    net.minecraft.nbt.CompoundTag tag = customData.copyTag();
                    int usageCount = tag.getInt("UsageCounter") + 1;
                    if (usageCount >= 100) {
                        usageCount = 0;
                        // Add 0.05 Vân Đạo marks
                        try {
                            Class<?> clazz = Class.forName("net.guzhenren.network.GuzhenrenModVariables");
                            Object vars = sp.getData((net.neoforged.neoforge.attachment.AttachmentType<?>) clazz.getField("PLAYER_VARIABLES").get(null));
                            java.lang.reflect.Field yunDaoField = clazz.getField("liupai_yundao");
                            double currentYunDao = yunDaoField.getDouble(vars);
                            yunDaoField.setDouble(vars, currentYunDao + 0.05);
                            java.lang.reflect.Method syncMethod = clazz.getMethod("markSyncDirty");
                            syncMethod.invoke(vars);
                            sp.sendSystemMessage(Component.literal("§e✦ Vân Đạo +0.05"));
                        } catch (Exception e) {
                            CoTienAddon.LOGGER.error("Failed to add Vân Đạo marks: " + e.getMessage());
                        }

                        // Accumulate Tiên Nguyên cost: 0.2 per 100 uses, consume 1 item when >= 1.0
                        float tnDebt = tag.getFloat("TienNguyenDebt") + 0.2f;
                        if (tnDebt >= 1.0f) {
                            // Try to consume 1 Tiên Nguyên
                            for (int i = 0; i < sp.getInventory().getContainerSize(); i++) {
                                ItemStack invStack = sp.getInventory().getItem(i);
                                if (isTienNguyen(invStack)) {
                                    invStack.shrink(1);
                                    tnDebt -= 1.0f;
                                    sp.sendSystemMessage(Component.literal("§6✦ Tiêu hao 1 Tiên Nguyên (chi phí vận hành)"));
                                    break;
                                }
                            }
                        }
                        tag.putFloat("TienNguyenDebt", tnDebt);
                    }
                    tag.putInt("UsageCounter", usageCount);
                    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                }
            }
        }
    }

    private boolean isTienNguyen(ItemStack stack) {
        if (stack.isEmpty()) return false;
        ResourceLocation id = stack.getItemHolder().unwrapKey()
                .map(k -> k.location()).orElse(null);
        return id != null && id.getNamespace().equals("cotienaddon") && id.getPath().equals("tien_nguyen");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResultHolder.pass(player.getItemInHand(hand));

        ItemStack stack = player.getItemInHand(hand);
        ItemStack offhand = player.getOffhandItem();

        // Feed mechanic
        ResourceLocation offhandId = offhand.getItemHolder().unwrapKey().map(k -> k.location()).orElse(null);
        if (offhandId != null && offhandId.getNamespace().equals("minecraft") && 
            (offhandId.getPath().equals("dragon_breath") || offhandId.getPath().equals("ender_eye"))) {
            if (!level.isClientSide) {
                int heal = offhandId.getPath().equals("dragon_breath") ? 1000 : 100;
                int currentDmg = stack.getDamageValue();
                if (currentDmg == 0) {
                    player.sendSystemMessage(Component.literal("§e[Thăng Thanh Vân] Cổ đã no, không cần ăn thêm!"));
                } else {
                    int repaired = Math.min(heal, currentDmg);
                    stack.setDamageValue(currentDmg - repaired);
                    if (!player.isCreative()) {
                        offhand.shrink(1);
                    }
                    double pct = (double)(MAX_DURABILITY - stack.getDamageValue()) / MAX_DURABILITY * 100;
                    player.sendSystemMessage(Component.literal(String.format("§b[Thăng Thanh Vân] Đã cho ăn! §7(Độ no: %.1f%%)", pct)));
                }
            }
            return InteractionResultHolder.success(stack);
        }

        // Toggle mechanic
        if (!level.isClientSide) {
            if (stack.getDamageValue() >= MAX_DURABILITY) {
                player.sendSystemMessage(Component.literal("§c[Thăng Thanh Vân] Tiên Cổ đang đói, không thể kích hoạt!"));
                return InteractionResultHolder.success(stack);
            }
            
            boolean state = !isActive(stack);
            setActive(stack, state);
            if (state) {
                player.sendSystemMessage(Component.literal("§a[Thăng Thanh Vân] Khởi động! Mây xanh bích ngọc hội tụ."));
            } else {
                player.sendSystemMessage(Component.literal("§e[Thăng Thanh Vân] Hủy kích hoạt."));
            }
        }

        return InteractionResultHolder.success(stack);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.isCreative() || player.isSpectator()) return;

        boolean hasActive = false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof ThangThanhVanTienGuItem && isActive(stack)) {
                hasActive = true;
                break;
            }
        }

        if (hasActive) {
            if (!player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                player.onUpdateAbilities();
            }
            
            if (player.level().isClientSide && player.getAbilities().flying) {
                clientFlightTick(player);
            }
        } else {
            // Remove flight if they don't have it active
            if (player.getAbilities().mayfly) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void clientFlightTick(Player player) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.player != player) return;
        
        // Tốc độ xé gió thăng giáng
        if (mc.options.keyJump.isDown()) {
            player.setDeltaMovement(player.getDeltaMovement().x, 2.0, player.getDeltaMovement().z);
        } else if (mc.options.keyShift.isDown()) {
            player.setDeltaMovement(player.getDeltaMovement().x, -2.0, player.getDeltaMovement().z);
        }
        
        // Particles: Mây xanh bích ngọc
        player.level().addParticle(net.minecraft.core.particles.ParticleTypes.WARPED_SPORE,
            player.getRandomX(0.8), player.getY() + 0.1, player.getRandomZ(0.8),
            0, 0.05, 0);
            
        if (player.tickCount % 3 == 0) {
            player.level().addParticle(net.minecraft.core.particles.ParticleTypes.CLOUD,
                player.getRandomX(0.5), player.getY() - 0.1, player.getRandomZ(0.5),
                0, -0.05, 0);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        boolean isActive = tag.getBoolean("Active");
        int remaining = MAX_DURABILITY - stack.getDamageValue();
        tooltip.add(Component.literal(String.format("§b■ Năng lượng: %d/%d", remaining, MAX_DURABILITY)));
        tooltip.add(Component.literal("§aTrạng thái: " + (isActive ? "§a[ĐANG KÍCH HOẠT]" : "§c[ĐÃ TẮT]")));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§aChuyển số: §fLục Chuyển - Cổ Tiên"));
        tooltip.add(Component.literal("§aLưu phái: §fVân Đạo"));
        tooltip.add(Component.literal("§dCơ chế: Triệt tiêu trọng lực. Nhấn Space để bắn lên, Shift để hạ xuống với tốc độ xé gió."));
        tooltip.add(Component.literal("§6Đạo ngân: §eMỗi 100 điểm sử dụng +0.05 Vân Đạo"));
        tooltip.add(Component.literal("§aThức ăn: §fHơi Thở Rồng (+1000), Mắt Ender (+100)"));
        tooltip.add(Component.literal("§6Tự động: §fTiêu thụ 1 Tiên Nguyên trong túi để hồi 500 điểm khi sắp hết."));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0f * (MAX_DURABILITY - stack.getDamageValue()) / MAX_DURABILITY);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float f = (float)(MAX_DURABILITY - stack.getDamageValue()) / MAX_DURABILITY;
        int r = (int)(255 * (1 - f));
        int g = (int)(200 * f);
        int b = (int)(255 * f);
        return (r << 16) | (g << 8) | b;
    }
}
