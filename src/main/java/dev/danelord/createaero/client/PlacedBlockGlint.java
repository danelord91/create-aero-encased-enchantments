package dev.danelord.createaero.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import dev.danelord.createaero.Compatibility;
import io.github.cotrin8672.cem.client.CustomRenderType;
import io.github.cotrin8672.cem.config.CemConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Add only the baked casing overlay. Original Encased BER/Flywheel parts still own animation. */
public final class PlacedBlockGlint {
    private static final RandomSource RANDOM = RandomSource.create();
    private PlacedBlockGlint() {}

    public static void render(BlockEntity be, PoseStack pose, MultiBufferSource buffers) {
        // World dispatch only; avoid touching config/render state for unrelated entities.
        if (be.getLevel() == null || !be.getLevel().isClientSide
                || !Compatibility.shouldGlint(be, true)) return;
        if (!Compatibility.shouldGlint(be, CemConfig.Companion.getCONFIG().getRenderGlint().get())) return;
        pose.pushPose();
        try {
            var glint = new SheetedDecalTextureGenerator(
                    buffers.getBuffer(CustomRenderType.getGLINT()), pose.last(), 0.007125F);
            // Use the actual Encased state/model data, never an ordinary Create or CEM model.
            // CEM's render type supplies its polygon offset; no expanded cube or duplicate solid pass.
            Minecraft.getInstance().getBlockRenderer().renderBatched(
                    be.getBlockState(), be.getBlockPos(), be.getLevel(), pose, glint,
                    true, RANDOM, be.getModelData(), null);
        } finally {
            pose.popPose();
        }
    }
}
