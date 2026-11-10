package dev.danelord.createaero.mixin;

import com.simibubi.create.content.contraptions.actors.roller.RollerMovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import fr.iglee42.createcasing.blocks.customs.CustomRollerBlock;
import io.github.cotrin8672.cem.content.block.roller.EnchantableRollerMovementBehaviour;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Roller has Efficiency only; preserve Create's original block breaking and drops. */
@Mixin(value = EnchantableRollerMovementBehaviour.class, remap = false)
public abstract class EncasedRollerLootMixin {
    @Unique private final RollerMovementBehaviour aero$original = new RollerMovementBehaviour();
    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    private void aero$originalLoot(MovementContext context, BlockPos pos, CallbackInfo ci) {
        if (context != null && context.state.getBlock() instanceof CustomRollerBlock) {
            ((OriginalRollerDestroyAccess) aero$original).aero$originalDestroy(context, pos);
            ci.cancel();
        }
    }
}
