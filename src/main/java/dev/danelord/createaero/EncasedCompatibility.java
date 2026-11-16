package dev.danelord.createaero;

import fr.iglee42.createcasing.blocks.customs.*;
import fr.iglee42.createcasing.blocks.fluids.CustomSpoutBlock;
import io.github.cotrin8672.cem.content.block.roller.EnchantableRollerBlockEntity;
import io.github.cotrin8672.cem.content.block.harvester.EnchantableHarvesterBlockEntity;
import io.github.cotrin8672.cem.content.block.saw.EnchantableSawBlockEntity;
import io.github.cotrin8672.cem.content.block.drill.EnchantableDrillBlockEntity;
import io.github.cotrin8672.cem.content.block.fan.EnchantableEncasedFanBlockEntity;
import io.github.cotrin8672.cem.content.block.mixer.EnchantableMechanicalMixerBlockEntity;
import io.github.cotrin8672.cem.content.block.press.EnchantableMechanicalPressBlockEntity;
import io.github.cotrin8672.cem.content.block.spout.EnchantableSpoutBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Loaded only when Create Encased is present. */
final class EncasedCompatibility {
    private EncasedCompatibility() {}
    static boolean supports(Block block) {
        return block instanceof CustomPressBlock || block instanceof CustomMixerBlock
                || block instanceof CustomEncasedFanBlock || block instanceof CustomSpoutBlock
                || block instanceof CustomDrillBlock || block instanceof CustomSawBlock
                || block instanceof CustomHarvesterBlock || block instanceof CustomRollerBlock
                || Compatibility.isEncasedPlough(block);
    }
    static BlockEntity create(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        Block block = state.getBlock();
        if (block instanceof CustomRollerBlock) return new EnchantableRollerBlockEntity(type, pos, state);
        if (block instanceof CustomHarvesterBlock) return new EnchantableHarvesterBlockEntity(type, pos, state);
        if (block instanceof CustomSawBlock) return new EnchantableSawBlockEntity(type, pos, state);
        if (block instanceof CustomDrillBlock) return new EnchantableDrillBlockEntity(type, pos, state);
        if (block instanceof CustomPressBlock) return new EnchantableMechanicalPressBlockEntity(type, pos, state);
        if (block instanceof CustomMixerBlock) return new EnchantableMechanicalMixerBlockEntity(type, pos, state);
        if (block instanceof CustomEncasedFanBlock) return new EnchantableEncasedFanBlockEntity(type, pos, state);
        if (block instanceof CustomSpoutBlock) return new EnchantableSpoutBlockEntity(type, pos, state);
        return null;
    }
}
