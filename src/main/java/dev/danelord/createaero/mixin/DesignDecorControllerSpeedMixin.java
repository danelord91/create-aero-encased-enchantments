package dev.danelord.createaero.mixin;

import com.simibubi.create.content.kinetics.crusher.CrushingWheelControllerBlock;
import dev.danelord.createaero.DesignDecorCompatibility;
import io.github.cotrin8672.cem.content.block.crusher.EnchantableCrushingWheelControllerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {CrushingWheelControllerBlock.class, EnchantableCrushingWheelControllerBlock.class}, remap = false)
public abstract class DesignDecorControllerSpeedMixin {
    @Inject(method = "updateSpeed", at = @At("TAIL"))
    private void aero$dndSpeed(BlockState state, LevelAccessor world, BlockPos pos, CallbackInfo ci) {
        if (world instanceof Level level) DesignDecorCompatibility.refreshAt(level, pos);
    }
}
