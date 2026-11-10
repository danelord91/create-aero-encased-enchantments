package dev.danelord.createaero.mixin;

import dev.danelord.createaero.Compatibility;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Placement applies item components before setPlacedBy, even when a Create superclass overrides it. */
@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin {
    @Inject(method = "applyComponentsFromItemStack", at = @At("TAIL"))
    private void aero$applyEnchantments(ItemStack stack, CallbackInfo ci) {
        BlockEntity be = (BlockEntity) (Object) this;
        if (be.hasLevel()) Compatibility.placed(be.getLevel(), be.getBlockPos(), be.getBlockState(), stack);
    }
}
