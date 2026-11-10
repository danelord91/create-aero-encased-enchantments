package dev.danelord.createaero.mixin;

import com.simibubi.create.content.contraptions.actors.plough.PloughBlock;
import com.simibubi.create.foundation.block.IBE;
import dev.danelord.createaero.Compatibility;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class PloughStateMixin {
    @Inject(method = "hasBlockEntity", at = @At("HEAD"), cancellable = true)
    private void aero$guardVanillaPlough(CallbackInfoReturnable<Boolean> cir) {
        var state = (BlockBehaviour.BlockStateBase) (Object) this;
        var block = state.getBlock();
        if (block instanceof PloughBlock && !(block instanceof IBE<?>) && !Compatibility.isEncasedPlough(block))
            cir.setReturnValue(false);
    }
}
