package dev.danelord.createaero.mixin;

import dev.danelord.createaero.Compatibility;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntityType.class)
public abstract class BlockEntityTypeMixin {
    @Inject(method = "create", at = @At("HEAD"), cancellable = true)
    private void aero$create(BlockPos pos, BlockState state, CallbackInfoReturnable<BlockEntity> cir) {
        BlockEntity be = Compatibility.create((BlockEntityType<?>) (Object) this, pos, state);
        if (be != null) cir.setReturnValue(be);
    }
}
