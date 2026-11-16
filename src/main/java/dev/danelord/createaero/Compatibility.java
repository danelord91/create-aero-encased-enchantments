package dev.danelord.createaero;

import com.simibubi.create.foundation.block.IBE;

import io.github.cotrin8672.cem.content.block.roller.EnchantableRollerBlockEntity;
import io.github.cotrin8672.cem.content.block.harvester.EnchantableHarvesterBlockEntity;
import io.github.cotrin8672.cem.content.block.saw.EnchantableSawBlockEntity;
import io.github.cotrin8672.cem.content.block.drill.EnchantableDrillBlockEntity;
import net.neoforged.fml.ModList;
import io.github.cotrin8672.cem.content.block.EnchantableBlockEntity;
import io.github.cotrin8672.cem.content.block.fan.EnchantableEncasedFanBlockEntity;
import io.github.cotrin8672.cem.content.block.mixer.EnchantableMechanicalMixerBlockEntity;
import io.github.cotrin8672.cem.content.block.press.EnchantableMechanicalPressBlockEntity;
import io.github.cotrin8672.cem.content.block.spout.EnchantableSpoutBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** No block swaps, new registry IDs, replacement models, or copies of upstream processing code. */
public final class Compatibility {
    private Compatibility() {}

    public static boolean isEncasedPlough(Block block) {
        var id = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block);
        return block.getClass() == com.simibubi.create.content.contraptions.actors.plough.PloughBlock.class
                && id != null && id.getNamespace().equals("createcasing") && id.getPath().endsWith("_mechanical_plough");
    }

    public static boolean supports(Block block) {
        return DesignDecorCompatibility.isMillstone(block) || DesignDecorCompatibility.isWheel(block)
                || (ModList.get().isLoaded("createcasing") && EncasedCompatibility.supports(block));
    }

    /** Shared decision only: safe to exercise on a dedicated server, no rendering/config imports. */
    public static boolean shouldGlint(BlockEntity be, boolean renderGlint) {
        return renderGlint && be != null && supports(be.getBlockState().getBlock())
                && be instanceof EnchantableBlockEntity enchanted && !enchanted.getEnchantments().isEmpty();
    }

    /** Select the rendered carried state, not machine eligibility or a world block at its old position. */
    public static boolean shouldGlintCarried(BlockState carriedState, BlockEntity be, boolean renderGlint) {
        return carriedState != null && be != null
                && carriedState.getBlock() == be.getBlockState().getBlock()
                && shouldGlint(be, renderGlint);
    }

    public static BlockEntity create(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        Block block = state.getBlock();
        // Both checks are needed: do not hijack a foreign factory given an Encased state.
        if (!supports(block) || !(block instanceof IBE<?> ibe) || ibe.getBlockEntityType() != type)
            return null;
        if (DesignDecorCompatibility.isMillstone(block)) return new io.github.cotrin8672.cem.content.block.millstone.EnchantableMillstoneBlockEntity(type, pos, state);
        if (DesignDecorCompatibility.isWheel(block)) return new io.github.cotrin8672.cem.content.block.crusher.EnchantableCrushingWheelBlockEntity(type, pos, state);
        return ModList.get().isLoaded("createcasing") ? EncasedCompatibility.create(type, pos, state) : null;
    }

    public static void placed(Level level, BlockPos pos, BlockState state, ItemStack stack) {
        if (!supports(state.getBlock())) return;
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof EnchantableBlockEntity enchanted) {
            ItemEnchantments enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            enchanted.setEnchantment(enchantments);
            be.setComponents(DataComponentMap.builder().addAll(be.components())
                    .set(DataComponents.ENCHANTMENTS, enchantments).build());
            be.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static ItemStack copyEnchantments(ItemStack stack, BlockState state, BlockEntity be) {
        if (supports(state.getBlock()) && be instanceof EnchantableBlockEntity enchanted
                && stack.is(state.getBlock().asItem()) && !enchanted.getEnchantments().isEmpty()) {
            stack.set(DataComponents.ENCHANTMENTS, enchanted.getEnchantments());
        }
        return stack;
    }
}
