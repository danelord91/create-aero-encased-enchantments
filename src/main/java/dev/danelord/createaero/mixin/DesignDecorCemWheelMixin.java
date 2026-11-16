package dev.danelord.createaero.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tterrag.registrate.util.entry.BlockEntry;
import dev.danelord.createaero.DesignDecorCompatibility;
import io.github.cotrin8672.cem.content.block.crusher.EnchantableCrushingWheelBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Recognize the other wheel without broadening CEM's controller checks. */
@Mixin(value = EnchantableCrushingWheelBlock.class, remap = false)
public abstract class DesignDecorCemWheelMixin {
    @WrapOperation(method = "updateControllers", at = @At(value = "INVOKE", target = "Lcom/tterrag/registrate/util/entry/BlockEntry;has(Lnet/minecraft/world/level/block/state/BlockState;)Z", ordinal = 2))
    private boolean aero$recognizeDecorWheel(BlockEntry<?> entry, BlockState state, Operation<Boolean> original) {
        return original.call(entry, state) || DesignDecorCompatibility.isWheel(state.getBlock());
    }
}
