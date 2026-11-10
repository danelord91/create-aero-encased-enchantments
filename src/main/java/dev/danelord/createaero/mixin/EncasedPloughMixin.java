package dev.danelord.createaero.mixin;

import com.simibubi.create.content.contraptions.actors.plough.PloughBlock;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.api.schematic.requirement.SpecialBlockItemRequirement;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import dev.danelord.createaero.AeroEnchantCompat;
import dev.danelord.createaero.Compatibility;
import io.github.cotrin8672.cem.content.block.plough.EnchantablePloughBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.jetbrains.annotations.Nullable;

@Mixin(value = PloughBlock.class, remap = false)
public abstract class EncasedPloughMixin implements EntityBlock, SpecialBlockItemRequirement {
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (Compatibility.isEncasedPlough(state.getBlock()))
            return new EnchantablePloughBlockEntity(AeroEnchantCompat.PLOUGH_ENTITY, pos, state);
        if ((Object) this instanceof IBE<?> ibe) return ibe.getBlockEntityType().create(pos, state);
        return null;
    }
    @Override public ItemRequirement getRequiredItems(BlockState state, @Nullable BlockEntity be) {
        ItemStack stack = Compatibility.copyEnchantments(new ItemStack(state.getBlock()), state, be);
        if (!Compatibility.isEncasedPlough(state.getBlock()))
            return new ItemRequirement(ItemRequirement.ItemUseType.CONSUME, stack);
        return new ItemRequirement(new ItemRequirement.StrictNbtStackRequirement(stack, ItemRequirement.ItemUseType.CONSUME));
    }
}
