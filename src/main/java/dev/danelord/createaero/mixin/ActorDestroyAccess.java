package dev.danelord.createaero.mixin;

import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import io.github.cotrin8672.cem.content.block.drill.EnchantableDrillMovementBehaviour;
import io.github.cotrin8672.cem.content.block.saw.EnchantableSawMovementBehaviour;
import io.github.cotrin8672.cem.content.block.plough.EnchantablePloughMovementBehaviour;
import io.github.cotrin8672.cem.content.block.roller.EnchantableRollerMovementBehaviour;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = {EnchantableDrillMovementBehaviour.class, EnchantableSawMovementBehaviour.class,
        EnchantablePloughMovementBehaviour.class, EnchantableRollerMovementBehaviour.class}, remap = false)
public interface ActorDestroyAccess {
    @Invoker("destroyBlock") void aero$destroy(MovementContext context, BlockPos pos);
}
