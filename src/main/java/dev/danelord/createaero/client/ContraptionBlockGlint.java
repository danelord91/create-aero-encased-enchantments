package dev.danelord.createaero.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.foundation.utility.worldWrappers.WrappedBlockAndTintGetter;
import dev.danelord.createaero.Compatibility;
import io.github.cotrin8672.cem.client.CustomRenderType;
import io.github.cotrin8672.cem.config.CemConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

/** Additive baked-casing pass for BOTH Flywheel structure rendering and vanilla fallback.
 * Create retains the entity renderer in both modes (CreateEntityBuilder.visual defaults renderNormally=true).
 * Do not replace CEM's structure model or broaden its placement/substitution mapping.
 * Original Encased actors/visuals still own animated parts; this does not add head/shaft glint.
 */
public final class ContraptionBlockGlint {
    private static final RandomSource RANDOM = RandomSource.create();
    private ContraptionBlockGlint() {}

    public static void render(Contraption contraption, ContraptionMatrices matrices, MultiBufferSource buffers) {
        if (!CemConfig.Companion.getCONFIG().getRenderGlint().get()) return;
        var client = contraption.getOrCreateClientContraptionLazy();
        var blocks = client.getRenderedBlocks();
        // Match Create's portal-aware state lookup, retaining all neighbours for correct face culling.
        var modelWorld = new WrappedBlockAndTintGetter(client.getRenderLevel()) {
            @Override public BlockState getBlockState(BlockPos pos) { return blocks.lookup().apply(pos); }
        };
        PoseStack pose = matrices.getModelViewProjection();
        for (BlockPos pos : blocks.positions()) {
            BlockState state = blocks.lookup().apply(pos);
            var be = client.getBlockEntity(pos);
            if (!Compatibility.shouldGlintCarried(state, be, true)) continue;
            pose.pushPose();
            try {
                pose.translate(pos.getX(), pos.getY(), pos.getZ());
                var glint = new SheetedDecalTextureGenerator(
                        buffers.getBuffer(CustomRenderType.getGLINT()), pose.last(), 0.007125F);
                Minecraft.getInstance().getBlockRenderer().renderBatched(
                        state, pos, modelWorld, pose, glint, true, RANDOM, be.getModelData(), null);
            } finally {
                pose.popPose();
            }
        }
    }
}
