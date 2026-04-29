package com.andyanh.cotienaddon.init;

import com.andyanh.cotienaddon.CoTienAddon;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CoTienBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, CoTienAddon.MODID);

    // Block trang trí cho hiệu ứng Băng Phong Trận
    public static final DeferredHolder<Block, Block> QUAN_TAI_BANG =
            BLOCKS.register("quan_tai_bang", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .strength(-1f, 3600000f)
                            .noLootTable()
                            .sound(SoundType.GLASS)
            ));

    // Tiên Đài — waystation block, đặt xuống tự đăng ký địa điểm trong Định Tiên Du
    public static final DeferredHolder<Block, Block> TIEN_DAI =
            BLOCKS.register("tien_dai", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .strength(3.0f, 6.0f)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.AMETHYST)
                            .lightLevel(state -> 8)
            ));

    // Khối Tiên Nguyên — tài nguyên hiếm trong Phúc Địa, drop Tiên Nguyên khi đào
    public static final DeferredHolder<Block, DropExperienceBlock> KHOI_TIEN_NGUYEN =
            BLOCKS.register("khoi_tien_nguyen", () -> new DropExperienceBlock(
                    UniformInt.of(3, 7),
                    BlockBehaviour.Properties.of()
                            .strength(4.0f, 4.0f)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.AMETHYST)
                            .lightLevel(state -> 6)
            ));
}
