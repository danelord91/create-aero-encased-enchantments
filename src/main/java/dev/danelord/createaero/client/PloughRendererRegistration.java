package dev.danelord.createaero.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.danelord.createaero.AeroEnchantCompat;
import io.github.cotrin8672.cem.content.block.plough.EnchantablePloughBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Plough has a baked Encased model; an empty BER lets the shared glint pass run. */
@EventBusSubscriber(modid = AeroEnchantCompat.ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class PloughRendererRegistration {
    private PloughRendererRegistration() {}
    @SubscribeEvent public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(AeroEnchantCompat.PLOUGH_ENTITY, EmptyRenderer::new);
    }
    public static final class EmptyRenderer implements BlockEntityRenderer<EnchantablePloughBlockEntity> {
        public EmptyRenderer(BlockEntityRendererProvider.Context context) {}
        @Override public void render(EnchantablePloughBlockEntity be, float partialTick, PoseStack pose,
                                     MultiBufferSource buffers, int light, int overlay) {}
    }
}
