package dev.danelord.createaero.mixin;

import com.simibubi.create.content.contraptions.actors.roller.RollerMovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = RollerMovementBehaviour.class, remap = false)
public interface OriginalRollerDestroyAccess {
    @Invoker("destroyBlock") void aero$originalDestroy(MovementContext context, BlockPos pos);
}
