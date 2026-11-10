package dev.danelord.createaero.mixin.client;

import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ActorVisual;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.content.contraptions.actors.plough.PloughMovementBehaviour;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.danelord.createaero.Compatibility;
import io.github.cotrin8672.cem.content.block.plough.EnchantablePloughMovementBehaviour;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Keep Encased's original actor head renderer; CEM handles the server-side logic. */
@Mixin(value = EnchantablePloughMovementBehaviour.class, remap = false)
public abstract class EncasedPloughActorRenderMixin {
    @Unique private final PloughMovementBehaviour aero$originalRenderer = new PloughMovementBehaviour();

    @Inject(method = "renderInContraption", at = @At("HEAD"), cancellable = true)
    private void aero$render(MovementContext movement, VirtualRenderWorld world, ContraptionMatrices matrices,
                             MultiBufferSource buffers, CallbackInfo ci) {
        if (Compatibility.isEncasedPlough(movement.state.getBlock())) {
            aero$originalRenderer.renderInContraption(movement, world, matrices, buffers);
            ci.cancel();
        }
    }
}
