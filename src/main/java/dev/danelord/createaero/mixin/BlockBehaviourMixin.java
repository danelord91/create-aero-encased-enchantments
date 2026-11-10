package dev.danelord.createaero.mixin;

import dev.danelord.createaero.Compatibility;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.class)
public abstract class BlockBehaviourMixin {
    @Inject(method = "getDrops", at = @At("RETURN"))
    private void aero$drops(BlockState state, LootParams.Builder params, CallbackInfoReturnable<List<ItemStack>> cir) {
        if (!Compatibility.supports(state.getBlock())) return;
        var be = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        for (ItemStack stack : cir.getReturnValue()) Compatibility.copyEnchantments(stack, state, be);
    }
}
