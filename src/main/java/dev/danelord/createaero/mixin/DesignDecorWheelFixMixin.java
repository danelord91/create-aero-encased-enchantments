package dev.danelord.createaero.mixin;

import com.simibubi.create.content.kinetics.crusher.CrushingWheelBlock;
import dev.danelord.createaero.DesignDecorCompatibility;
import io.github.cotrin8672.cem.content.block.crusher.EnchantableCrushingWheelBlockEntity;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** CEM's final wheel entity assumes its own block class in this otherwise reusable method. */
@Mixin(value = EnchantableCrushingWheelBlockEntity.class, remap = false)
public abstract class DesignDecorWheelFixMixin {
    @Inject(method = "fixControllers", at = @At("HEAD"), cancellable = true)
    private void aero$dndFixControllers(CallbackInfo ci) {
        var self = (EnchantableCrushingWheelBlockEntity) (Object) this;
        if (!DesignDecorCompatibility.isWheel(self.getBlockState().getBlock())) return;
        if (self.hasLevel()) for (Direction direction : Direction.values()) {
            ((CrushingWheelBlock) self.getBlockState().getBlock()).updateControllers(self.getBlockState(), self.getLevel(), self.getBlockPos(), direction);
        }
        ci.cancel();
    }
}
