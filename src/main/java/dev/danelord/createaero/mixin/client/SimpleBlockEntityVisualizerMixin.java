package dev.danelord.createaero.mixin.client;

import dev.danelord.createaero.Compatibility;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import io.github.cotrin8672.cem.content.block.EnchantableBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Keep a BER overlay pass without removing or replacing any Flywheel visual. */
@Mixin(value = SimpleBlockEntityVisualizer.class, remap = false)
public abstract class SimpleBlockEntityVisualizerMixin {
    @Inject(method = "skipVanillaRender", at = @At("RETURN"), cancellable = true)
    private void aero$keepOverlayPass(BlockEntity be, CallbackInfoReturnable<Boolean> cir) {
        // Encased fans normally suppress their BER under Flywheel. The original fan BER
        // already skips animated geometry when visualized, so retaining its dispatch adds
        // only our casing overlay. Keep the pass for plain/config-off supported entities
        // too: toggling config or receiving enchantment packets must not require remeshing.
        // This runs during chunk compilation as well, so never read client config here.
        if (be instanceof EnchantableBlockEntity && Compatibility.supports(be.getBlockState().getBlock()))
            cir.setReturnValue(false);
    }
}
