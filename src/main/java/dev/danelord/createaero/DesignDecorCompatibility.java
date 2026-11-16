package dev.danelord.createaero;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.crusher.CrushingWheelBlock;
import com.simibubi.create.content.kinetics.crusher.CrushingWheelBlockEntity;
import com.simibubi.create.content.kinetics.crusher.CrushingWheelControllerBlock;
import com.simibubi.create.content.kinetics.crusher.CrushingWheelControllerBlockEntity;
import com.simibubi.create.content.kinetics.millstone.MillstoneBlock;
import io.github.cotrin8672.cem.content.block.EnchantableBlockEntity;
import io.github.cotrin8672.cem.content.block.crusher.EnchantableCrushingWheelControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Optional integration: no symbolic references, class loading, or reflection into DnD. */
public final class DesignDecorCompatibility {
    private DesignDecorCompatibility() {}

    private static boolean exact(Block block, String name) {
        return BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals("dndecor")
                && block.getClass().getName().equals("dev.lopyluna.dndecor.content.blocks." + name);
    }
    public static boolean isMillstone(Block block) { return block instanceof MillstoneBlock && exact(block, "MillstoneTypeBlock"); }
    public static boolean isWheel(Block block) { return block instanceof CrushingWheelBlock && exact(block, "CrushingWheelTypeBlock"); }

    private record Pair(CrushingWheelBlockEntity first, CrushingWheelBlockEntity second) {}

    /** Never select an unrelated neighboring wheel or an invalid/stopped pair. */
    private static Pair pair(CrushingWheelControllerBlockEntity controller) {
        if (!controller.hasLevel() || !controller.getBlockState().getValue(CrushingWheelControllerBlock.VALID)) return null;
        var level = controller.getLevel(); var pos = controller.getBlockPos();
        var facingAxis = controller.getBlockState().getValue(CrushingWheelControllerBlock.FACING).getAxis();
        for (Direction direction : new Direction[]{Direction.EAST, Direction.UP, Direction.SOUTH}) {
            if (direction.getAxis() == facingAxis) continue;
            var first = level.getBlockEntity(pos.relative(direction));
            var second = level.getBlockEntity(pos.relative(direction.getOpposite()));
            if (!(first instanceof CrushingWheelBlockEntity a) || !(second instanceof CrushingWheelBlockEntity b)) continue;
            var aState = a.getBlockState(); var bState = b.getBlockState();
            if (!isWheel(aState.getBlock()) && !isWheel(bState.getBlock())) continue;
            if (!(aState.getBlock() instanceof CrushingWheelBlock) || !(bState.getBlock() instanceof CrushingWheelBlock)) continue;
            var axis = aState.getValue(CrushingWheelBlock.AXIS);
            if (axis == direction.getAxis() || axis == facingAxis || axis != bState.getValue(CrushingWheelBlock.AXIS)) continue;
            if (a.getSpeed() == 0 || a.getSpeed() != -b.getSpeed()) continue;
            return new Pair(a, b);
        }
        return null;
    }

    /** Upgrade only this active DnD controller entity, never its block or registry type.
     * Copy complete upstream NBT (including occupied inventory/UUID) before replacing.
     * The factory remains ordinary Create; tick reselects after chunk NBT loading.
     */
    public static CrushingWheelControllerBlockEntity refresh(CrushingWheelControllerBlockEntity current) {
        if (!current.hasLevel() || current.getLevel().isClientSide) return current;
        var pair = pair(current); var level = current.getLevel();
        boolean managed = current.getType() == AllBlockEntityTypes.CRUSHING_WHEEL_CONTROLLER.get()
                && current.getBlockState().is(AllBlocks.CRUSHING_WHEEL_CONTROLLER.get());
        CrushingWheelControllerBlockEntity selected = current;
        if (managed && (pair != null) != (current instanceof EnchantableCrushingWheelControllerBlockEntity)) {
            selected = pair == null
                    ? new CrushingWheelControllerBlockEntity(current.getType(), current.getBlockPos(), current.getBlockState())
                    : new EnchantableCrushingWheelControllerBlockEntity(current.getType(), current.getBlockPos(), current.getBlockState());
            selected.loadWithComponents(current.saveWithoutMetadata(level.registryAccess()), level.registryAccess());
            level.removeBlockEntity(current.getBlockPos());
            level.setBlockEntity(selected);
            selected.setChanged(); selected.sendData();
        }
        if (pair != null && selected instanceof EnchantableBlockEntity enchanted) {
            var efficiency = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.EFFICIENCY);
            int first = pair.first() instanceof EnchantableBlockEntity a ? a.getEnchantmentLevel(efficiency) : 0;
            int second = pair.second() instanceof EnchantableBlockEntity b ? b.getEnchantmentLevel(efficiency) : 0;
            var mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            mutable.set(efficiency, first + second); // CEM's actual rule: add levels; not max or average.
            var next = mutable.toImmutable();
            if (!next.equals(enchanted.getEnchantments())) { enchanted.setEnchantment(next); selected.setChanged(); selected.sendData(); }
            selected.crushingspeed = Math.abs(pair.first().getSpeed() / 50f);
        }
        return selected;
    }

    public static void refreshAt(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof CrushingWheelControllerBlockEntity controller) refresh(controller);
    }
}
