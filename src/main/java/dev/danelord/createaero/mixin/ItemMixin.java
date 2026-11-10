package dev.danelord.createaero.mixin;

import dev.danelord.createaero.Compatibility;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class ItemMixin {
    @Inject(method = "getEnchantmentValue", at = @At("HEAD"), cancellable = true)
    private void aero$value(CallbackInfoReturnable<Integer> cir) {
        if ((Object) this instanceof BlockItem item && Compatibility.supports(item.getBlock()))
            cir.setReturnValue(14);
    }

    @Inject(method = "isEnchantable", at = @At("HEAD"), cancellable = true)
    private void aero$enchantable(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() instanceof BlockItem item && Compatibility.supports(item.getBlock()))
            cir.setReturnValue(stack.getCount() == 1 && !stack.isEnchanted());
    }
}
