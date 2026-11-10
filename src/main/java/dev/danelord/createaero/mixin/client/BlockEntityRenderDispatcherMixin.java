package dev.danelord.createaero.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.danelord.createaero.client.PlacedBlockGlint;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** World renderer only: runs after the original BER and its vanilla validity/distance checks. */
@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherMixin {
    @Inject(method = "setupAndRender", at = @At("TAIL"))
    private static <T extends BlockEntity> void aero$placedGlint(BlockEntityRenderer<T> renderer,
            T be, float partialTick, PoseStack pose, MultiBufferSource buffers, CallbackInfo ci) {
        PlacedBlockGlint.render(be, pose, buffers);
    }
}
