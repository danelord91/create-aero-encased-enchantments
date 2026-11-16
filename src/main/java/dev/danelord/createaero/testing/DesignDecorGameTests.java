package dev.danelord.createaero.testing;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.crusher.CrushingWheelBlock;
import com.simibubi.create.content.kinetics.crusher.CrushingWheelControllerBlock;
import com.simibubi.create.content.kinetics.crusher.CrushingWheelControllerBlockEntity;
import com.simibubi.create.content.kinetics.millstone.MillstoneBlockEntity;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import dev.danelord.createaero.Compatibility;
import io.github.cotrin8672.cem.content.block.EnchantableBlockEntity;
import io.github.cotrin8672.cem.content.block.crusher.EnchantableCrushingWheelBlockEntity;
import io.github.cotrin8672.cem.content.block.crusher.EnchantableCrushingWheelControllerBlockEntity;
import io.github.cotrin8672.cem.content.block.millstone.EnchantableMillstoneBlockEntity;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("aero_enchant_compat")
@PrefixGameTestTemplate(false)
public final class DesignDecorGameTests {
    private static final BlockPos POS = new BlockPos(2, 3, 2);
    private static List<Block> variants() {
        return BuiltInRegistries.BLOCK.stream().filter(b -> BuiltInRegistries.BLOCK.getKey(b).getNamespace().equals("dndecor"))
                .filter(b -> b.getClass().getName().equals("dev.lopyluna.dndecor.content.blocks.MillstoneTypeBlock")
                        || b.getClass().getName().equals("dev.lopyluna.dndecor.content.blocks.CrushingWheelTypeBlock")).toList();
    }
    private static boolean absent(GameTestHelper h) {
        if (ModList.get().isLoaded("dndecor")) return false;
        h.assertTrue(variants().isEmpty(), "Absent optional DnD must have no variants");
        h.assertTrue(!Compatibility.supports(AllBlocks.MILLSTONE.get()) && !Compatibility.supports(AllBlocks.CRUSHING_WHEEL.get()), "Optional absence must not broaden Create support");
        System.out.println("AERO_DND_ABSENT baseline branch exercised");
        h.succeed(); return true;
    }
    private static void enchant(GameTestHelper h, BlockEntity be, int value) {
        var mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        mutable.set(h.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.EFFICIENCY), value);
        ((EnchantableBlockEntity) be).setEnchantment(mutable.toImmutable());
    }
    private static void initialize(BlockEntity be) {
        ((SmartBlockEntity) be).initialize();
        ((io.github.cotrin8672.cem.mixin.SmartBlockEntityMixin) be).setInitialized(true);
        if (be instanceof KineticBlockEntity kinetic) {
            kinetic.updateSpeed = false;
            ((io.github.cotrin8672.cem.mixin.KineticBlockEntityMixin) kinetic).setValidationCountdown(100000);
        }
    }
    private static final class Captured extends com.simibubi.create.content.contraptions.Contraption {
        @Override public com.simibubi.create.api.contraption.ContraptionType getType() { return com.simibubi.create.AllContraptionTypes.PISTON.value(); }
        @Override public boolean canBeStabilized(Direction direction, BlockPos pos) { return false; }
        @Override public boolean assemble(net.minecraft.world.level.Level world, BlockPos pos) {
            anchor = pos; bounds = new net.minecraft.world.phys.AABB(BlockPos.ZERO);
            addBlock(world, pos, capture(world, pos)); getStorage().initialize(); return true;
        }
    }
    @GameTest(template = "empty")
    public static void exactScopeAndFactories(GameTestHelper h) {
        if (absent(h)) return;
        var variants = variants();
        h.assertTrue(variants.size() == 44, "44 exact registered functional variants expected");
        int mills = 0, wheels = 0;
        for (Block block : variants) {
            h.assertTrue(Compatibility.supports(block), "Missing functional support " + BuiltInRegistries.BLOCK.getKey(block));
            var state = block.defaultBlockState(); var type = ((IBE<?>) block).getBlockEntityType();
            var be = type.create(BlockPos.ZERO, state);
            if (block instanceof CrushingWheelBlock) { wheels++; h.assertTrue(be instanceof EnchantableCrushingWheelBlockEntity, "CEM wheel factory required"); }
            else { mills++; h.assertTrue(be instanceof EnchantableMillstoneBlockEntity, "CEM mill factory required"); }
            h.assertTrue(be.getType() == type && be.getBlockState() == state && type.isValid(state), "Original registry type and state identity");
            h.assertTrue(Compatibility.create(AllBlockEntityTypes.MILLSTONE.get(), BlockPos.ZERO, state) == null, "Foreign factory must not be hijacked");
        }
        h.assertTrue(mills == 22 && wheels == 22, "22 + 22, not a silently reduced matrix");
        h.assertTrue(!(AllBlockEntityTypes.MILLSTONE.get().create(BlockPos.ZERO, AllBlocks.MILLSTONE.getDefaultState()) instanceof EnchantableBlockEntity), "Ordinary Create mill unchanged");
        h.assertTrue(!(AllBlockEntityTypes.CRUSHING_WHEEL.get().create(BlockPos.ZERO, AllBlocks.CRUSHING_WHEEL.getDefaultState()) instanceof EnchantableBlockEntity), "Ordinary Create wheel unchanged");
        h.assertTrue(!(AllBlockEntityTypes.CRUSHING_WHEEL_CONTROLLER.get().create(BlockPos.ZERO, AllBlocks.CRUSHING_WHEEL_CONTROLLER.getDefaultState()) instanceof EnchantableBlockEntity), "No global controller factory replacement");
        System.out.println("AERO_DND_SCOPE mills=" + mills + " wheels=" + wheels); h.succeed();
    }
    @GameTest(template = "empty")
    public static void allVariantActualAnvilsAndUnsupportedNegatives(GameTestHelper h) {
        if (absent(h)) return;
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        var registry = h.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        int count = 0;
        for (Block block : variants()) {
            for (var key : List.of(Enchantments.EFFICIENCY, Enchantments.FORTUNE, Enchantments.SILK_TOUCH, Enchantments.UNBREAKING)) {
                var enchant = registry.getOrThrow(key);
                var menu = new AnvilMenu(1, player.getInventory(), ContainerLevelAccess.create(h.getLevel(), h.absolutePos(POS)));
                var book = new ItemStack(Items.ENCHANTED_BOOK); var stored = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
                stored.set(enchant, 1); book.set(DataComponents.STORED_ENCHANTMENTS, stored.toImmutable());
                var machine = new ItemStack(block); menu.getSlot(0).set(machine); menu.getSlot(1).set(book); menu.createResult();
                boolean expected = key.equals(Enchantments.EFFICIENCY);
                h.assertTrue(enchant.value().isSupportedItem(machine) == expected, "Meaningful eligibility only " + block + " " + key);
                var output = menu.getSlot(2).getItem();
                h.assertTrue(expected ? output.is(block.asItem()) && output.getEnchantmentLevel(enchant) == 1 : output.isEmpty(), "Actual anvil result " + block + " " + key);
            } count++;
        }
        int excluded = 0;
        for (Block block : BuiltInRegistries.BLOCK.stream().filter(b -> BuiltInRegistries.BLOCK.getKey(b).getNamespace().equals("dndecor") && !variants().contains(b)).toList()) {
            h.assertTrue(!Compatibility.supports(block), "Decor/large fans/cogs not machinery support " + block);
            h.assertTrue(!registry.getOrThrow(Enchantments.EFFICIENCY).value().isSupportedItem(new ItemStack(block)), "Decor not tagged for Efficiency " + block); excluded++;
            if (block.asItem() != Items.AIR) {
                var menu = new AnvilMenu(1, player.getInventory(), ContainerLevelAccess.create(h.getLevel(), h.absolutePos(POS)));
                var book = new ItemStack(Items.ENCHANTED_BOOK); var stored = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
                stored.set(registry.getOrThrow(Enchantments.EFFICIENCY), 1);
                book.set(DataComponents.STORED_ENCHANTMENTS, stored.toImmutable());
                menu.getSlot(0).set(new ItemStack(block)); menu.getSlot(1).set(book); menu.createResult();
                h.assertTrue(menu.getSlot(2).getItem().isEmpty(), "Actual anvil rejects decorative block " + block);
            }
        }
        h.assertTrue(count == 44 && excluded > 0, "All variants and decor negatives");
        System.out.println("AERO_DND_ANVIL variants=" + count + " booksPerVariant=4 excludedBlocks=" + excluded); h.succeed();
    }
    @GameTest(template = "empty")
    public static void allVariantPlacementPersistenceRecoveryCapture(GameTestHelper h) {
        if (absent(h)) return;
        var player = h.makeMockPlayer(GameType.SURVIVAL); var pos = h.absolutePos(POS);
        var efficiency = h.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.EFFICIENCY);
        int count = 0;
        for (Block block : variants()) {
            player.getInventory().clearContent(); h.setBlock(POS, Blocks.AIR); h.setBlock(POS.below(), Blocks.STONE);
            var stack = new ItemStack(block); h.assertTrue(stack.isEnchantable(), "Enchantable item " + block); stack.enchant(efficiency, 3);
            var expected = stack.get(DataComponents.ENCHANTMENTS); player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            var hit = new BlockHitResult(Vec3.atCenterOf(pos.below()).add(0, .5, 0), Direction.UP, pos.below(), false);
            var result = ((BlockItem) stack.getItem()).place(new BlockPlaceContext(new UseOnContext(h.getLevel(), player, InteractionHand.MAIN_HAND, stack, hit)));
            h.assertTrue(result.consumesAction() && h.getBlockState(POS).is(block), "Actual placement retains ID " + block);
            var state = h.getBlockState(POS); var be = h.getBlockEntity(POS); var type = ((IBE<?>) block).getBlockEntityType();
            h.assertTrue(be.getType() == type && ((EnchantableBlockEntity) be).getEnchantmentLevel(efficiency) == 3, "Original entity type/enchantment " + block);
            var restored = BlockEntity.loadStatic(pos, state, be.saveWithFullMetadata(h.getLevel().registryAccess()), h.getLevel().registryAccess());
            h.assertTrue(restored != null && restored.getClass() == be.getClass() && restored.getType() == type && ((EnchantableBlockEntity) restored).getEnchantmentLevel(efficiency) == 3, "Actual NBT factory reload " + block);
            var drops = Block.getDrops(state, h.getLevel(), pos, be);
            h.assertTrue(drops.size() == 1 && drops.getFirst().is(block.asItem()) && expected.equals(drops.getFirst().get(DataComponents.ENCHANTMENTS)), "Exact enchanted drops " + block);
            var picked = block.getCloneItemStack(state, hit, h.getLevel(), pos, player);
            h.assertTrue(picked.is(block.asItem()) && expected.equals(picked.get(DataComponents.ENCHANTMENTS)), "Pick identity and enchantments " + block);
            h.assertTrue(!Compatibility.shouldGlint(be, false) && Compatibility.shouldGlint(be, true) && Compatibility.shouldGlintCarried(state, be, true), "Additive glint policy, not pixels " + block);
            var captured = new Captured(); captured.assemble(h.getLevel(), pos); var info = captured.getBlocks().get(BlockPos.ZERO);
            h.assertTrue(info.state().is(block) && info.nbt().contains("Enchantments"), "Real Create capture " + block);
            captured.removeBlocksFromWorld(h.getLevel(), BlockPos.ZERO);
            captured.addBlocksToWorld(h.getLevel(), new com.simibubi.create.content.contraptions.StructureTransform(pos, 0, 0, 0));
            be = h.getBlockEntity(POS); state = h.getBlockState(POS);
            h.assertTrue(state.is(block) && be.getType() == type && ((EnchantableBlockEntity) be).getEnchantmentLevel(efficiency) == 3, "Capture/remove/disassembly " + block);
            player.setItemInHand(InteractionHand.MAIN_HAND, com.simibubi.create.AllItems.WRENCH.asStack());
            var wrench = ((com.simibubi.create.content.equipment.wrench.IWrenchable) block).onSneakWrenched(state,
                    new UseOnContext(h.getLevel(), player, InteractionHand.MAIN_HAND, player.getMainHandItem(), new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false)));
            h.assertTrue(wrench.consumesAction() && h.getBlockState(POS).isAir(), "Actual sneak wrench " + block);
            var returned = player.getInventory().items.stream().filter(s -> s.is(block.asItem())).toList();
            h.assertTrue(returned.stream().mapToInt(ItemStack::getCount).sum() == 1 && expected.equals(returned.getFirst().get(DataComponents.ENCHANTMENTS)), "Exactly one enchanted recovery " + block);
            var replacement = returned.getFirst().copy(); player.setItemInHand(InteractionHand.MAIN_HAND, replacement);
            ((BlockItem) replacement.getItem()).place(new BlockPlaceContext(new UseOnContext(h.getLevel(), player, InteractionHand.MAIN_HAND, replacement, hit)));
            h.assertTrue(h.getBlockState(POS).is(block) && ((EnchantableBlockEntity) h.getBlockEntity(POS)).getEnchantmentLevel(efficiency) == 3, "Actual replacement " + block);
            h.setBlock(POS, Blocks.AIR); h.setBlock(POS, block);
            h.assertTrue(!Compatibility.shouldGlint(h.getBlockEntity(POS), true), "Plain glint excluded " + block);
            var plain = Block.getDrops(h.getBlockState(POS), h.getLevel(), pos, h.getBlockEntity(POS));
            h.assertTrue(plain.size() == 1 && plain.getFirst().is(block.asItem()) && !plain.getFirst().isEnchanted(), "Plain identity/no duplication " + block); count++;
        }
        h.assertTrue(count == 44, "Full recovery matrix"); System.out.println("AERO_DND_RECOVERY variants=" + count); h.succeed();
    }
    private static int mill(GameTestHelper h, Block block, int efficiency) {
        h.setBlock(POS, Blocks.AIR); h.setBlock(POS, block); var mill = (MillstoneBlockEntity) h.getBlockEntity(POS);
        initialize(mill); if (mill instanceof EnchantableBlockEntity) enchant(h, mill, efficiency);
        mill.setSpeed(64); mill.inputInv.setStackInSlot(0, new ItemStack(Items.WHEAT));
        int calls = 0; while (mill.outputInv.getStackInSlot(0).isEmpty() && calls < 500) { mill.tick(); calls++; }
        h.assertTrue(mill.inputInv.getStackInSlot(0).isEmpty() && mill.outputInv.getStackInSlot(0).is(com.simibubi.create.AllItems.WHEAT_FLOUR.get()), "Real milling wheat -> flour " + block);
        return calls;
    }
    @GameTest(template = "empty")
    public static void allMillstonesCompleteFasterRealRecipes(GameTestHelper h) {
        if (absent(h)) return;
        int count = 0;
        for (Block block : variants().stream().filter(b -> !(b instanceof CrushingWheelBlock)).toList()) {
            int plain = mill(h, block, 0), enchanted = mill(h, block, 5);
            h.assertTrue(plain > enchanted && enchanted > 0, "Efficiency accelerates completed recipe " + block + " " + plain + "/" + enchanted); count++;
        }
        int ordinary = mill(h, AllBlocks.MILLSTONE.get(), 0);
        int cemPlain = mill(h, io.github.cotrin8672.cem.registry.BlockRegistration.getENCHANTABLE_MILLSTONE().get(), 0);
        int cemEnchanted = mill(h, io.github.cotrin8672.cem.registry.BlockRegistration.getENCHANTABLE_MILLSTONE().get(), 5);
        h.assertTrue(count == 22 && ordinary == cemPlain && cemPlain > cemEnchanted, "Ordinary/CEM controls and 22 recipes");
        System.out.println("AERO_DND_MILL_RECIPES variants=" + count + " plain=" + cemPlain + " efficiency5=" + cemEnchanted + " ordinary=" + ordinary); h.succeed();
    }
    private static CrushingWheelControllerBlockEntity pair(GameTestHelper h, Block first, Block second, int left, int right) {
        for (var p : List.of(POS, POS.east(), POS.east(2))) h.setBlock(p, Blocks.AIR);
        h.setBlock(POS, first.defaultBlockState().setValue(CrushingWheelBlock.AXIS, Direction.Axis.Z));
        h.setBlock(POS.east(2), second.defaultBlockState().setValue(CrushingWheelBlock.AXIS, Direction.Axis.Z));
        var a = (KineticBlockEntity) h.getBlockEntity(POS); var b = (KineticBlockEntity) h.getBlockEntity(POS.east(2));
        initialize(a); initialize(b); if (a instanceof EnchantableBlockEntity) enchant(h, a, left); if (b instanceof EnchantableBlockEntity) enchant(h, b, right);
        a.setSpeed(64); b.setSpeed(-64);
        ((CrushingWheelBlock) first).updateControllers(a.getBlockState(), h.getLevel(), h.absolutePos(POS), Direction.EAST);
        ((CrushingWheelBlock) second).updateControllers(b.getBlockState(), h.getLevel(), h.absolutePos(POS.east(2)), Direction.WEST);
        var state = h.getBlockState(POS.east());
        h.assertTrue(state.getBlock() instanceof CrushingWheelControllerBlock && state.getValue(CrushingWheelControllerBlock.VALID), "Real opposed wheels form valid controller " + first + "/" + second);
        var controller = (CrushingWheelControllerBlockEntity) h.getBlockEntity(POS.east());
        initialize(controller); controller.tick();
        controller = (CrushingWheelControllerBlockEntity) h.getBlockEntity(POS.east()); initialize(controller);
        return controller;
    }
    @GameTest(template = "empty")
    public static void controllerReloadWheelChangesAndScopedFix(GameTestHelper h) {
        if (absent(h)) return;
        var wheels = variants().stream().filter(b -> b instanceof CrushingWheelBlock).toList();
        var efficiency = h.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.EFFICIENCY);
        for (Block wheel : wheels) {
            var controller = pair(h, wheel, wheel, 2, 3);
            var originalState = controller.getBlockState(); var originalType = controller.getType();
            h.assertTrue(originalState.is(AllBlocks.CRUSHING_WHEEL_CONTROLLER.get()) && originalType == AllBlockEntityTypes.CRUSHING_WHEEL_CONTROLLER.get(), "Controller block/type may not be swapped globally");
            ((EnchantableCrushingWheelBlockEntity) h.getBlockEntity(POS)).fixControllers();
            controller = (CrushingWheelControllerBlockEntity) h.getBlockEntity(POS.east());
            h.assertTrue(((EnchantableBlockEntity) controller).getEnchantmentLevel(efficiency) == 5, "Scoped CEM fixControllers cannot cast DnD to CEM block");
            controller.inventory.setStackInSlot(0, new ItemStack(Items.COBBLESTONE, 2));
            controller.inventory.recipeDuration = 87; controller.inventory.remainingTime = 43;
            var nbt = controller.saveWithFullMetadata(h.getLevel().registryAccess());
            var reloaded = (CrushingWheelControllerBlockEntity) BlockEntity.loadStatic(controller.getBlockPos(), controller.getBlockState(), nbt, h.getLevel().registryAccess());
            h.assertTrue(reloaded.getClass() == CrushingWheelControllerBlockEntity.class, "Original factory remains ordinary on reload");
            h.getLevel().removeBlockEntity(reloaded.getBlockPos()); h.getLevel().setBlockEntity(reloaded); reloaded.tick();
            controller = (CrushingWheelControllerBlockEntity) h.getBlockEntity(POS.east());
            h.assertTrue(controller instanceof EnchantableCrushingWheelControllerBlockEntity && controller.getType() == originalType && controller.getBlockState() == originalState, "Reload selects scoped CEM controller without changing identity");
            h.assertTrue(controller.inventory.getStackInSlot(0).is(Items.COBBLESTONE) && controller.inventory.getStackInSlot(0).getCount() == 2 && controller.inventory.remainingTime == 43 && controller.inventory.recipeDuration == 87, "Occupied inventory/timing survive factory reload and upgrade");
            enchant(h, h.getBlockEntity(POS), 0); enchant(h, h.getBlockEntity(POS.east(2)), 1); controller.tick();
            h.assertTrue(((EnchantableBlockEntity) controller).getEnchantmentLevel(efficiency) == 1, "Removed/replaced enchantments recomputed, never stale");
            ((KineticBlockEntity) h.getBlockEntity(POS)).setSpeed(0);
            ((CrushingWheelBlock) wheel).updateControllers(h.getBlockState(POS), h.getLevel(), h.absolutePos(POS), Direction.EAST);
            controller = (CrushingWheelControllerBlockEntity) h.getBlockEntity(POS.east());
            h.assertTrue(controller.getClass() == CrushingWheelControllerBlockEntity.class && controller.crushingspeed == 0, "Stopped invalid controller must not retain active CEM processing");
        }
        var ordinary = pair(h, AllBlocks.CRUSHING_WHEEL.get(), AllBlocks.CRUSHING_WHEEL.get(), 0, 0);
        h.assertTrue(ordinary.getClass() == CrushingWheelControllerBlockEntity.class, "Ordinary Create pair untouched after DnD pairs");
        System.out.println("AERO_DND_CONTROLLER_LIFECYCLE variants=" + wheels.size() + " occupied-reload/fixControllers/enchantment-removal/invalid/plain passed"); h.succeed();
    }

    private static int crush(GameTestHelper h, Block first, Block second, int left, int right) {
        var controller = pair(h, first, second, left, right);
        boolean dnd = BuiltInRegistries.BLOCK.getKey(first).getNamespace().equals("dndecor") || BuiltInRegistries.BLOCK.getKey(second).getNamespace().equals("dndecor");
        if (dnd) h.assertTrue(controller instanceof EnchantableCrushingWheelControllerBlockEntity, "DnD must run actual CEM controller not tag-only integration");
        if (controller instanceof EnchantableBlockEntity enchanted) {
            var efficiency = h.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.EFFICIENCY);
            h.assertTrue(enchanted.getEnchantmentLevel(efficiency) == left + right, "Controller sums wheel Efficiency like CEM");
        }
        var input = new ItemEntity(h.getLevel(), h.absolutePos(POS.east()).getX()+.5, h.absolutePos(POS.east()).getY()+.5, h.absolutePos(POS.east()).getZ()+.5, new ItemStack(Items.COBBLESTONE, 2));
        h.getLevel().addFreshEntity(input);
        ((CrushingWheelControllerBlock) controller.getBlockState().getBlock()).checkEntityForProcessing(h.getLevel(), controller.getBlockPos(), input);
        h.assertTrue(controller.isOccupied(), "Real item intake");
        int calls = 0; while (controller.isOccupied() && calls < 1000) { controller.tick(); calls++; }
        h.assertTrue(!controller.isOccupied() && !input.isAlive(), "Controller consumed input and completed cycle");
        var area = new net.minecraft.world.phys.AABB(controller.getBlockPos()).inflate(2);
        var output = h.getLevel().getEntitiesOfClass(ItemEntity.class, area).stream().filter(e -> e.getItem().is(Items.GRAVEL)).mapToInt(e -> e.getItem().getCount()).sum();
        h.assertTrue(output == 2, "Actual cobblestone -> exactly 2 gravel, got " + output);
        h.killAllEntities(); return calls;
    }
    @GameTest(template = "empty")
    public static void allPairedWheelsCompleteFasterRealRecipes(GameTestHelper h) {
        if (absent(h)) return;
        int count = 0;
        var wheels = variants().stream().filter(b -> b instanceof CrushingWheelBlock).toList();
        for (Block block : wheels) {
            int plain = crush(h, block, block, 0, 0), enchanted = crush(h, block, block, 5, 5);
            h.assertTrue(plain > enchanted && enchanted > 0, "Efficiency accelerates real paired crushing " + block + " " + plain + "/" + enchanted); count++;
        }
        int mixedPlain = crush(h, wheels.getFirst(), wheels.getLast(), 0, 0), mixed = crush(h, wheels.getFirst(), wheels.getLast(), 5, 0);
        int vanillaMixed = crush(h, wheels.getFirst(), AllBlocks.CRUSHING_WHEEL.get(), 5, 0);
        int cemMixed = crush(h, wheels.getFirst(), io.github.cotrin8672.cem.registry.BlockRegistration.getENCHANTABLE_CRUSHING_WHEEL().get(), 5, 5);
        int ordinary = crush(h, AllBlocks.CRUSHING_WHEEL.get(), AllBlocks.CRUSHING_WHEEL.get(), 0, 0);
        int cemPlain = crush(h, io.github.cotrin8672.cem.registry.BlockRegistration.getENCHANTABLE_CRUSHING_WHEEL().get(), io.github.cotrin8672.cem.registry.BlockRegistration.getENCHANTABLE_CRUSHING_WHEEL().get(), 0, 0);
        int cemEnchanted = crush(h, io.github.cotrin8672.cem.registry.BlockRegistration.getENCHANTABLE_CRUSHING_WHEEL().get(), io.github.cotrin8672.cem.registry.BlockRegistration.getENCHANTABLE_CRUSHING_WHEEL().get(), 5, 5);
        h.assertTrue(count == 22 && mixedPlain > mixed && ordinary == cemPlain && cemPlain > cemEnchanted && vanillaMixed < ordinary && cemMixed == cemEnchanted, "22 variants, mixed, one-wheel, Create/CEM controls");
        System.out.println("AERO_DND_CRUSH_RECIPES variants=" + count + " ordinary=" + ordinary + " cemPlain=" + cemPlain + " cem10=" + cemEnchanted + " mixed0=" + mixedPlain + " mixed5=" + mixed + " vanillaMixed=" + vanillaMixed); h.succeed();
    }
}
