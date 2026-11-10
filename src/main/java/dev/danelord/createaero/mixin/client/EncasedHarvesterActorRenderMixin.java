package dev.danelord.createaero.mixin.client;

import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ActorVisual;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.content.contraptions.actors.harvester.HarvesterMovementBehaviour;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import fr.iglee42.createcasing.blocks.customs.CustomHarvesterBlock;
import io.github.cotrin8672.cem.content.block.harvester.EnchantableHarvesterMovementBehaviour;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Keep Encased's original actor head renderer; CEM handles the server-side logic. */
@Mixin(value = EnchantableHarvesterMovementBehaviour.class, remap = false)
public abstract class EncasedHarvesterActorRenderMixin {
    @Unique private final HarvesterMovementBehaviour aero$originalRenderer = new HarvesterMovementBehaviour();

    @Inject(method = "createVisual", at = @At("HEAD"), cancellable = true)
    private void aero$visual(VisualizationContext ctx, VirtualRenderWorld world, MovementContext movement,
                             CallbackInfoReturnable<ActorVisual> cir) {
        if (movement.state.getBlock() instanceof CustomHarvesterBlock)
            cir.setReturnValue(aero$originalRenderer.createVisual(ctx, world, movement));
    }

    @Inject(method = "renderInContraption", at = @At("HEAD"), cancellable = true)
    private void aero$render(MovementContext movement, VirtualRenderWorld world, ContraptionMatrices matrices,
                             MultiBufferSource buffers, CallbackInfo ci) {
        if (movement.state.getBlock() instanceof CustomHarvesterBlock) {
            aero$originalRenderer.renderInContraption(movement, world, matrices, buffers);
            ci.cancel();
        }
    }
}
