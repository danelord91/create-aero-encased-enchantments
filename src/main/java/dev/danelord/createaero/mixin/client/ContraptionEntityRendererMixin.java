package dev.danelord.createaero.mixin.client;

import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.render.ContraptionEntityRenderer;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;
import dev.danelord.createaero.client.ContraptionBlockGlint;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ContraptionEntityRenderer.class, remap = false)
public abstract class ContraptionEntityRendererMixin {
    // Called unconditionally in Create 6.0.10, AFTER structure/BER rendering and BEFORE matrices.clear().
    // Using this common path avoids a second solid model and leaves actor geometry/animation untouched.
    @Inject(method = "renderActors(Lnet/minecraft/world/level/Level;Lcom/simibubi/create/foundation/virtualWorld/VirtualRenderWorld;Lcom/simibubi/create/content/contraptions/Contraption;Lcom/simibubi/create/content/contraptions/render/ContraptionMatrices;Lnet/minecraft/client/renderer/MultiBufferSource;)V", at = @At("HEAD"))
    private static void aero$carriedCasingGlint(Level world, VirtualRenderWorld renderWorld,
            Contraption contraption, ContraptionMatrices matrices, MultiBufferSource buffers, CallbackInfo ci) {
        ContraptionBlockGlint.render(contraption, matrices, buffers);
    }
}
