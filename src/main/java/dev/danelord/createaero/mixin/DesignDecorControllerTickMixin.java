package dev.danelord.createaero.mixin;

import com.simibubi.create.content.kinetics.crusher.CrushingWheelControllerBlockEntity;
import dev.danelord.createaero.DesignDecorCompatibility;
import io.github.cotrin8672.cem.content.block.crusher.EnchantableCrushingWheelControllerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Original controller factory stays unchanged. Re-evaluate after reload and wheel changes. */
@Mixin(value = {CrushingWheelControllerBlockEntity.class, EnchantableCrushingWheelControllerBlockEntity.class}, remap = false)
public abstract class DesignDecorControllerTickMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void aero$dndController(CallbackInfo ci) {
        var self = (CrushingWheelControllerBlockEntity) (Object) this;
        if (DesignDecorCompatibility.refresh(self) != self) ci.cancel();
    }
}
