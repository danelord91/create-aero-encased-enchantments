package dev.danelord.createaero.testing;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.fan.processing.AllFanProcessingTypes;
import com.simibubi.create.content.kinetics.press.PressingBehaviour;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import dev.danelord.createaero.AeroEnchantCompat;
import dev.danelord.createaero.Compatibility;
import fr.iglee42.createcasing.registries.EncasedBlockEntities;
import io.github.cotrin8672.cem.content.block.EnchantableBlockEntity;
import io.github.cotrin8672.cem.content.block.fan.EnchantableEncasedFanBlockEntity;
import io.github.cotrin8672.cem.content.block.fan.EnchantableFanProcessing;
import io.github.cotrin8672.cem.content.block.mixer.EnchantableMechanicalMixerBlockEntity;
import io.github.cotrin8672.cem.content.block.press.EnchantableMechanicalPressBlockEntity;
import io.github.cotrin8672.cem.content.block.spout.EnchantableSpoutBlockEntity;
import java.lang.reflect.Field;
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
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(AeroEnchantCompat.ID)
@PrefixGameTestTemplate(false)
public final class CompatGameTests {
    private static final BlockPos POS = new BlockPos(2, 3, 2);
    private static Block block(String id) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("createcasing", id));
    }
    private static void level(GameTestHelper h, BlockEntity be, int efficiency) {
        var builder = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        builder.set(h.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.EFFICIENCY), efficiency);
        ((EnchantableBlockEntity) be).setEnchantment(builder.toImmutable());
    }
    private static BlockEntity entity(GameTestHelper h, String id, int efficiency) {
        h.setBlock(POS, Blocks.AIR);
        h.setBlock(POS, block(id));
        BlockEntity be = h.getBlockEntity(POS);
        level(h, be, efficiency);
        ((SmartBlockEntity) be).initialize();
        ((io.github.cotrin8672.cem.mixin.SmartBlockEntityMixin) be).setInitialized(true);
        if (be instanceof KineticBlockEntity kinetic) {
            kinetic.setSpeed(64);
            kinetic.updateSpeed = false;
            ((io.github.cotrin8672.cem.mixin.KineticBlockEntityMixin) kinetic).setValidationCountdown(1000);
        }
        return be;
    }

    private static com.simibubi.create.content.contraptions.behaviour.MovementContext actorContext(GameTestHelper h, BlockEntity be) {
        var info = new net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo(BlockPos.ZERO, be.getBlockState(), be.saveWithoutMetadata(h.getLevel().registryAccess()));
        var contraption = new com.simibubi.create.content.contraptions.piston.PistonContraption();
        contraption.getStorage().initialize();
        var context = new com.simibubi.create.content.contraptions.behaviour.MovementContext(h.getLevel(), info, contraption);
        context.position = Vec3.atCenterOf(h.absolutePos(POS));
        context.motion = new Vec3(0.1, 0, 0);
        context.relativeMotion = context.motion;
        return context;
    }

    private static final class CapturedContraption extends com.simibubi.create.content.contraptions.Contraption {
        @Override public com.simibubi.create.api.contraption.ContraptionType getType() { return com.simibubi.create.AllContraptionTypes.PISTON.value(); }
        @Override public boolean canBeStabilized(Direction direction, BlockPos pos) { return false; }
        @Override public boolean assemble(net.minecraft.world.level.Level world, BlockPos pos) {
            anchor = pos;
            bounds = new net.minecraft.world.phys.AABB(BlockPos.ZERO);
            addBlock(world, pos, capture(world, pos));
            getStorage().initialize();
            return true;
        }
    }

    private static float sawDuration(GameTestHelper h, int efficiency) {
        h.setBlock(POS, Blocks.AIR);
        h.setBlock(POS, block("brass_mechanical_saw").defaultBlockState().setValue(com.simibubi.create.content.kinetics.saw.SawBlock.FACING, Direction.UP));
        var saw = (io.github.cotrin8672.cem.content.block.saw.EnchantableSawBlockEntity) h.getBlockEntity(POS);
        saw.initialize();
        level(h, saw, efficiency);
        saw.setSpeed(64);
        saw.updateSpeed = false;
        saw.inventory.setStackInSlot(0, new ItemStack(Items.OAK_LOG));
        saw.start(saw.inventory.getStackInSlot(0));
        h.assertTrue(saw.inventory.recipeDuration > 0, "saw must start real cutting recipe");
        return saw.inventory.recipeDuration;
    }

    @GameTest(template = "empty")
    public static void sawActuallyStartsAndAcceleratesRealRecipe(GameTestHelper h) {
        float plain = sawDuration(h, 0), enchanted = sawDuration(h, 5);
        h.assertTrue(enchanted > 0 && enchanted < plain, "saw efficiency must reduce cutting duration: " + plain + " / " + enchanted);
        System.out.println("AERO_EVIDENCE saw recipe duration plain=" + plain + " efficiency5=" + enchanted);
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void realAnvilAppliesOnlySupportedBooks(GameTestHelper h) {
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        var registry = h.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var efficiency = registry.getOrThrow(Enchantments.EFFICIENCY);
        var fortune = registry.getOrThrow(Enchantments.FORTUNE);
        var silk = registry.getOrThrow(Enchantments.SILK_TOUCH);
        int count = 0;
        for (Block b : BuiltInRegistries.BLOCK.stream().filter(Compatibility::supports).toList()) {
            boolean loot = b instanceof fr.iglee42.createcasing.blocks.customs.CustomDrillBlock || b instanceof fr.iglee42.createcasing.blocks.customs.CustomSawBlock || b instanceof fr.iglee42.createcasing.blocks.customs.CustomHarvesterBlock || Compatibility.isEncasedPlough(b);
            var enchants = loot ? java.util.List.of(fortune, silk) : java.util.List.of(efficiency);
            for (var enchant : enchants) {
                var menu = new net.minecraft.world.inventory.AnvilMenu(1, player.getInventory(), net.minecraft.world.inventory.ContainerLevelAccess.create(h.getLevel(), h.absolutePos(POS)));
                var book = new ItemStack(Items.ENCHANTED_BOOK);
                var stored = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
                stored.set(enchant, 1);
                book.set(DataComponents.STORED_ENCHANTMENTS, stored.toImmutable());
                menu.getSlot(0).set(new ItemStack(b));
                menu.getSlot(1).set(book);
                menu.createResult();
                var result = menu.getSlot(2).getItem();
                h.assertTrue(result.is(b.asItem()) && result.getEnchantmentLevel(enchant) == 1,
                        "Anvil must apply " + enchant.unwrapKey().orElseThrow().location() + " to " + BuiltInRegistries.BLOCK.getKey(b));
            }
            count++;
        }
        h.assertTrue(count == 75, "all variant anvils tested");
        System.out.println("AERO_EVIDENCE anvil applications for " + count + " variants");
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void capturedActorsDisassembleWithEnchantments(GameTestHelper h) {
        var ids = BuiltInRegistries.BLOCK.stream().filter(b -> b instanceof fr.iglee42.createcasing.blocks.customs.CustomDrillBlock || b instanceof fr.iglee42.createcasing.blocks.customs.CustomSawBlock || b instanceof fr.iglee42.createcasing.blocks.customs.CustomHarvesterBlock || b instanceof fr.iglee42.createcasing.blocks.customs.CustomRollerBlock || Compatibility.isEncasedPlough(b)).map(b -> BuiltInRegistries.BLOCK.getKey(b).getPath()).toList();
        h.assertTrue(ids.size() == 45, "Expected all 45 built-in actor variants");
        var efficiency = h.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.EFFICIENCY);
        var position = h.absolutePos(POS);
        for (String id : ids) {
            h.setBlock(POS, Blocks.AIR);
            for (Direction side : Direction.Plane.HORIZONTAL) h.setBlock(POS.relative(side), Blocks.STONE);
            h.setBlock(POS, block(id));
            var be = h.getBlockEntity(POS);
            level(h, be, 5);
            var contraption = new CapturedContraption();
            h.assertTrue(contraption.assemble(h.getLevel(), position), "fixture capture succeeds: " + id);
            h.assertTrue(contraption.getBlocks().size() == 1 && contraption.getActors().size() == 1, "exact actor captured: " + id);
            var info = contraption.getBlocks().get(BlockPos.ZERO);
            h.assertTrue(info.state().getBlock() == block(id) && info.nbt() != null && info.nbt().contains("Enchantments"), "captured state and enchantments: " + id);
            contraption.removeBlocksFromWorld(h.getLevel(), BlockPos.ZERO);
            h.assertTrue(h.getBlockState(POS).isAir(), "captured machine removed: " + id);
            contraption.addBlocksToWorld(h.getLevel(), new com.simibubi.create.content.contraptions.StructureTransform(position, 0, 0, 0));
            h.assertTrue(h.getBlockState(POS).getBlock() == block(id), "disassembly retains casing: " + id);
            h.assertTrue(((EnchantableBlockEntity) h.getBlockEntity(POS)).getEnchantmentLevel(efficiency) == 5, "disassembly retains Efficiency: " + id);
        }
        System.out.println("AERO_EVIDENCE real Create capture/remove/disassemble 45 actor variants");
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void rollerLootMatchesOriginalWithoutDuplication(GameTestHelper h) {
        h.setBlock(POS, block("brass_mechanical_roller"));
        var be = h.getBlockEntity(POS);
        var addon = (dev.danelord.createaero.mixin.ActorDestroyAccess) com.simibubi.create.api.behaviour.movement.MovementBehaviour.REGISTRY.get(block("brass_mechanical_roller"));
        var original = (dev.danelord.createaero.mixin.OriginalRollerDestroyAccess) new com.simibubi.create.content.contraptions.actors.roller.RollerMovementBehaviour();
        var target = POS.east();
        var area = new net.minecraft.world.phys.AABB(Vec3.atCenterOf(h.absolutePos(POS)), Vec3.atCenterOf(h.absolutePos(POS))).inflate(2);
        for (int seed = 0; seed < 32; seed++) {
            int[] counts = new int[3];
            for (int mode = 0; mode < 3; mode++) {
                h.killAllEntities();
                h.setBlock(target, Blocks.DIRT);
                level(h, be, mode == 2 ? 5 : 0);
                var context = actorContext(h, be);
                h.getLevel().random.setSeed(seed);
                if (mode == 0) original.aero$originalDestroy(context, h.absolutePos(target));
                else addon.aero$destroy(context, h.absolutePos(target));
                h.assertTrue(h.getBlockState(target).isAir(), "Roller must remove the block");
                counts[mode] = h.getLevel().getEntitiesOfClass(ItemEntity.class, area).stream()
                        .filter(e -> e.getItem().is(Items.DIRT)).mapToInt(e -> e.getItem().getCount()).sum();
                h.assertTrue(counts[mode] <= 1, "Roller may not duplicate a dirt block");
            }
            h.assertTrue(counts[0] == counts[1] && counts[0] == counts[2], "Roller loot changed vs original for seed " + seed + ": " + java.util.Arrays.toString(counts));
        }
        System.out.println("AERO_EVIDENCE roller loot 32 paired seeds original/plain/efficiency5, no duplicate");
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void harvesterFortuneChangesActualCropLoot(GameTestHelper h) {
        h.setBlock(POS, block("brass_mechanical_harvester"));
        var be = h.getBlockEntity(POS);
        var actor = com.simibubi.create.api.behaviour.movement.MovementBehaviour.REGISTRY.get(block("brass_mechanical_harvester"));
        var fortune = h.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE);
        var target = POS.east();
        var area = new net.minecraft.world.phys.AABB(Vec3.atCenterOf(h.absolutePos(POS)), Vec3.atCenterOf(h.absolutePos(POS))).inflate(2);
        int[] totals = new int[2];
        for (int seed = 0; seed < 32; seed++) {
            for (int mode = 0; mode < 2; mode++) {
                h.killAllEntities();
                h.setBlock(target.below(), Blocks.FARMLAND);
                h.setBlock(target, Blocks.WHEAT.defaultBlockState().setValue(net.minecraft.world.level.block.CropBlock.AGE, 7));
                var ench = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
                if (mode == 1) ench.set(fortune, 3);
                ((EnchantableBlockEntity) be).setEnchantment(ench.toImmutable());
                var context = actorContext(h, be);
                h.getLevel().random.setSeed(seed);
                actor.visitNewPosition(context, h.absolutePos(target));
                h.assertTrue(h.getBlockState(target).is(Blocks.WHEAT), "harvester must replant mature crop");
                totals[mode] += h.getLevel().getEntitiesOfClass(ItemEntity.class, area).stream()
                        .filter(e -> e.getItem().is(Items.WHEAT_SEEDS)).mapToInt(e -> e.getItem().getCount()).sum();
            }
        }
        h.assertTrue(totals[1] > totals[0], "Fortune must increase real crop seed loot: " + java.util.Arrays.toString(totals));
        System.out.println("AERO_EVIDENCE harvester seeds plain=" + totals[0] + " fortune3=" + totals[1]);
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void movingDrillSilkTouchDropsOre(GameTestHelper h) {
        h.setBlock(POS, block("brass_mechanical_drill"));
        var be = h.getBlockEntity(POS);
        var silk = h.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH);
        var ench = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        ench.set(silk, 1);
        ((EnchantableBlockEntity) be).setEnchantment(ench.toImmutable());
        var target = POS.east();
        h.setBlock(target, Blocks.DIAMOND_ORE);
        var actor = (dev.danelord.createaero.mixin.ActorDestroyAccess) com.simibubi.create.api.behaviour.movement.MovementBehaviour.REGISTRY.get(block("brass_mechanical_drill"));
        actor.aero$destroy(actorContext(h, be), h.absolutePos(target));
        h.assertTrue(h.getBlockState(target).isAir(), "Actor must actually break the ore");
        var area = new net.minecraft.world.phys.AABB(Vec3.atCenterOf(h.absolutePos(POS)), Vec3.atCenterOf(h.absolutePos(POS))).inflate(2);
        var items = h.getLevel().getEntitiesOfClass(ItemEntity.class, area);
        int ore = items.stream().filter(e -> e.getItem().is(Blocks.DIAMOND_ORE.asItem())).mapToInt(e -> e.getItem().getCount()).sum();
        int diamonds = items.stream().filter(e -> e.getItem().is(Items.DIAMOND)).mapToInt(e -> e.getItem().getCount()).sum();
        h.assertTrue(ore == 1 && diamonds == 0, "Silk Touch must yield exactly one ore and no diamonds; got ore=" + ore + " diamonds=" + diamonds);
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void ploughDoesNotChangeVanillaOrCemBlockEntities(GameTestHelper h) {
        var vanilla = AllBlocks.MECHANICAL_PLOUGH.getDefaultState();
        h.assertTrue(!vanilla.hasBlockEntity(), "Original Create plough must remain without a block entity");
        var cem = io.github.cotrin8672.cem.registry.BlockRegistration.getENCHANTABLE_MECHANICAL_PLOUGH().getDefaultState();
        h.assertTrue(cem.hasBlockEntity(), "CEM plough must retain its block entity");
        var entity = ((net.minecraft.world.level.block.EntityBlock) cem.getBlock()).newBlockEntity(BlockPos.ZERO, cem);
        h.assertTrue(entity instanceof io.github.cotrin8672.cem.content.block.plough.EnchantablePloughBlockEntity, "CEM plough entity must still instantiate");
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void movingActorReadsSerializedEfficiency(GameTestHelper h) throws Exception {
        String[] ids = {"brass_mechanical_drill", "brass_mechanical_saw", "brass_mechanical_plough", "brass_mechanical_roller"};
        for (String id : ids) {
            h.setBlock(POS, Blocks.AIR);
            for (Direction side : Direction.Plane.HORIZONTAL) h.setBlock(POS.relative(side), Blocks.STONE);
            h.setBlock(POS, block(id));
            var be = h.getBlockEntity(POS);
            var actor = com.simibubi.create.api.behaviour.movement.MovementBehaviour.REGISTRY.get(block(id));
            var speed = (dev.danelord.createaero.mixin.ActorSpeedAccess) actor;
            float[] measured = new float[2];
            for (int i = 0; i < 2; i++) {
                level(h, be, i == 0 ? 0 : 5);
                var nbt = be.saveWithoutMetadata(h.getLevel().registryAccess());
                var info = new net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo(BlockPos.ZERO, be.getBlockState(), nbt);
                var context = new com.simibubi.create.content.contraptions.behaviour.MovementContext(h.getLevel(), info, null);
                context.motion = new Vec3(0.1, 0, 0);
                context.relativeMotion = context.motion;
                measured[i] = speed.aero$breakingSpeed(context);
            }
            h.assertTrue(measured[0] > 0 && measured[1] > measured[0], "Serialized Efficiency must increase moving actor speed for " + id + " values=" + java.util.Arrays.toString(measured));
            System.out.println("AERO_EVIDENCE actor speed " + id + " plain=" + measured[0] + " efficiency5=" + measured[1]);
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void expandedPloughNeedsPersistedEntityAndActor(GameTestHelper h) {
        var machine = block("brass_mechanical_plough");
        h.setBlock(POS.north(), Blocks.STONE);
        h.setBlock(POS, machine);
        var be = h.getBlockEntity(POS);
        h.assertTrue(be instanceof io.github.cotrin8672.cem.content.block.plough.EnchantablePloughBlockEntity,
                "Encased plough needs persisted enchantments without block conversion");
        h.assertTrue(com.simibubi.create.api.behaviour.movement.MovementBehaviour.REGISTRY.get(machine)
                instanceof io.github.cotrin8672.cem.content.block.plough.EnchantablePloughMovementBehaviour,
                "Encased plough requires enchanted actor behavior");
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void expandedRollerMustUseEnchantedEntityAndActor(GameTestHelper h) {
        var machine = block("brass_mechanical_roller");
        var be = EncasedBlockEntities.MECHANICAL_ROLLER.get().create(BlockPos.ZERO, machine.defaultBlockState());
        h.assertTrue(be instanceof io.github.cotrin8672.cem.content.block.roller.EnchantableRollerBlockEntity,
                "Encased roller requires CEM's enchantable entity");
        h.assertTrue(com.simibubi.create.api.behaviour.movement.MovementBehaviour.REGISTRY.get(machine)
                instanceof io.github.cotrin8672.cem.content.block.roller.EnchantableRollerMovementBehaviour,
                "Encased roller requires enchanted actor behavior");
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void expandedHarvesterMustUseEnchantedEntityAndActor(GameTestHelper h) {
        var machine = block("brass_mechanical_harvester");
        var be = EncasedBlockEntities.HARVESTER.get().create(BlockPos.ZERO, machine.defaultBlockState());
        h.assertTrue(be instanceof io.github.cotrin8672.cem.content.block.harvester.EnchantableHarvesterBlockEntity,
                "Encased harvester requires CEM's real enchantable entity");
        h.assertTrue(com.simibubi.create.api.behaviour.movement.MovementBehaviour.REGISTRY.get(machine)
                instanceof io.github.cotrin8672.cem.content.block.harvester.EnchantableHarvesterMovementBehaviour,
                "Encased harvester requires enchanted actor behavior");
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void expandedSawMustUseEnchantedEntityAndActor(GameTestHelper h) {
        var saw = block("brass_mechanical_saw");
        var be = EncasedBlockEntities.SAW.get().create(BlockPos.ZERO, saw.defaultBlockState());
        h.assertTrue(be instanceof io.github.cotrin8672.cem.content.block.saw.EnchantableSawBlockEntity,
                "Encased saw requires CEM's real processing entity");
        var actor = com.simibubi.create.api.behaviour.movement.MovementBehaviour.REGISTRY.get(saw);
        h.assertTrue(actor instanceof io.github.cotrin8672.cem.content.block.saw.EnchantableSawMovementBehaviour,
                "Encased saw requires enchanted contraption behavior");
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void expandedDrillMustUseEnchantedEntityAndActor(GameTestHelper h) {
        var drill = block("brass_mechanical_drill");
        var be = EncasedBlockEntities.DRILL.get().create(BlockPos.ZERO, drill.defaultBlockState());
        h.assertTrue(be instanceof io.github.cotrin8672.cem.content.block.drill.EnchantableDrillBlockEntity,
                "Encased drill requires CEM's real processing entity");
        var actor = com.simibubi.create.api.behaviour.movement.MovementBehaviour.REGISTRY.get(drill);
        h.assertTrue(actor instanceof io.github.cotrin8672.cem.content.block.drill.EnchantableDrillMovementBehaviour,
                "Encased drill requires enchanted contraption behavior");
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void encasedFactoryUsesRealCemProcessing(GameTestHelper h) {
        var state = block("brass_press").defaultBlockState();
        var be = EncasedBlockEntities.PRESS.get().create(BlockPos.ZERO, state);
        h.assertTrue(be instanceof EnchantableBlockEntity, "Encased press must run real CEM enchantable entity, not tag-only support");
        h.assertTrue(be.getType() == EncasedBlockEntities.PRESS.get(), "Encased entity identity must be preserved");
        h.assertTrue(be.getBlockState().equals(state), "Exact Encased state must be preserved");
        h.assertTrue(!(AllBlockEntityTypes.MECHANICAL_PRESS.get().create(BlockPos.ZERO, AllBlocks.MECHANICAL_PRESS.getDefaultState()) instanceof EnchantableBlockEntity), "Vanilla Create factories must be untouched");
        h.assertTrue(!Compatibility.supports(AllBlocks.MECHANICAL_ROLLER.get()), "Vanilla Create roller must be untouched");
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void everySupportedVariantPlacementSaveDropsAndPick(GameTestHelper h) {
        List<Block> blocks = BuiltInRegistries.BLOCK.stream().filter(Compatibility::supports).toList();
        h.assertTrue(blocks.size() == 75, "Expected 75 built-in supported variants; got " + blocks.size());
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        var pos = h.absolutePos(POS);
        var efficiency = h.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.EFFICIENCY);
        for (Block b : blocks) {
            player.getInventory().clearContent();
            String id = BuiltInRegistries.BLOCK.getKey(b).toString();
            h.setBlock(POS, Blocks.AIR);
            h.setBlock(POS.below(), Blocks.STONE);
            for (Direction side : Direction.Plane.HORIZONTAL) h.setBlock(POS.relative(side), Blocks.STONE);
            var stack = new ItemStack(b);
            h.assertTrue(stack.isEnchantable(), "Enchanting table eligibility: " + id);
            var selected = b instanceof fr.iglee42.createcasing.blocks.customs.CustomHarvesterBlock
                    ? h.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE) : efficiency;
            h.assertTrue(selected.value().isSupportedItem(stack), "Meaningful enchantment eligibility: " + id);
            if (b instanceof fr.iglee42.createcasing.blocks.customs.CustomHarvesterBlock)
                h.assertTrue(!efficiency.value().isSupportedItem(stack), "Harvester must not offer ineffective Efficiency: " + id);
            stack.enchant(selected, 3);
            var expectedEnchantments = stack.get(DataComponents.ENCHANTMENTS);
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            if (b instanceof fr.iglee42.createcasing.blocks.customs.CustomRollerBlock) h.setBlock(POS.below(), Blocks.AIR);
            var hit = b instanceof fr.iglee42.createcasing.blocks.customs.CustomRollerBlock
                    ? new BlockHitResult(Vec3.atCenterOf(pos.north()).add(0, 0, 0.5), Direction.SOUTH, pos.north(), false)
                    : new BlockHitResult(Vec3.atCenterOf(pos.below()).add(0, 0.5, 0), Direction.UP, pos.below(), false);
            var result = ((BlockItem) stack.getItem()).place(new BlockPlaceContext(new UseOnContext(h.getLevel(), player, InteractionHand.MAIN_HAND, stack, hit)));
            h.assertTrue(result.consumesAction(), "Actual BlockItem placement must succeed: " + id);
            var state = h.getBlockState(POS);
            h.assertTrue(state.getBlock() == b, "Placement may not convert casing: " + id);
            var be = h.getBlockEntity(POS);
            h.assertTrue(be.getType() == (Compatibility.isEncasedPlough(b) ? AeroEnchantCompat.PLOUGH_ENTITY : ((IBE<?>) b).getBlockEntityType()), "Entity type must remain correct: " + id);
            h.assertTrue(((EnchantableBlockEntity) be).getEnchantmentLevel(selected) == 3, "Placement must transfer enchantments: " + id);
            var saved = be.saveWithFullMetadata(h.getLevel().registryAccess());
            var restored = BlockEntity.loadStatic(pos, state, saved, h.getLevel().registryAccess());
            h.assertTrue(restored != null && restored.getClass() == be.getClass(), "NBT reload class: " + id);
            h.assertTrue(restored.getType() == be.getType(), "NBT reload Encased registry ID: " + id);
            h.assertTrue(((EnchantableBlockEntity) restored).getEnchantmentLevel(selected) == 3, "NBT enchantment persistence: " + id);
            var drops = Block.getDrops(state, h.getLevel(), pos, be);
            h.assertTrue(drops.size() == 1 && drops.getFirst().is(b.asItem()), "Drops preserve Encased item: " + id);
            h.assertTrue(expectedEnchantments.equals(drops.getFirst().get(DataComponents.ENCHANTMENTS)), "Drops preserve enchantments: " + id);
            var picked = b.getCloneItemStack(state, hit, h.getLevel(), pos, player);
            h.assertTrue(picked.is(b.asItem()) && expectedEnchantments.equals(picked.get(DataComponents.ENCHANTMENTS)), "Pick-block preserves Encased enchanted item: " + id);
            h.assertTrue(b instanceof com.simibubi.create.api.schematic.requirement.SpecialBlockItemRequirement, "Strict schematic item requirement: " + id);
            player.setItemInHand(InteractionHand.MAIN_HAND, com.simibubi.create.AllItems.WRENCH.asStack());
            var wrenchResult = ((com.simibubi.create.content.equipment.wrench.IWrenchable) b).onSneakWrenched(state,
                    new UseOnContext(h.getLevel(), player, InteractionHand.MAIN_HAND, player.getMainHandItem(), new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false)));
            h.assertTrue(wrenchResult.consumesAction() && h.getBlockState(POS).isAir(), "Actual wrench removal: " + id);
            h.assertTrue(player.getInventory().items.stream().anyMatch(s -> s.is(b.asItem()) && expectedEnchantments.equals(s.get(DataComponents.ENCHANTMENTS))), "Wrench returns Encased enchanted item: " + id);
            h.setBlock(POS, b);
            var plainDrops = Block.getDrops(h.getBlockState(POS), h.getLevel(), pos, h.getBlockEntity(POS));
            h.assertTrue(plainDrops.size() == 1 && plainDrops.getFirst().is(b.asItem()) && !plainDrops.getFirst().isEnchanted(), "Unenchanted machines remain unenchanted: " + id);
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void placedGlintPolicyAllVariants(GameTestHelper h) throws ReflectiveOperationException {
        // Reflection allows the pre-fix RED run to reach the actual GameTest server.
        var gate = Compatibility.class.getMethod("shouldGlint", BlockEntity.class, boolean.class);
        int variants = 0;
        for (Block b : BuiltInRegistries.BLOCK.stream().filter(Compatibility::supports).toList()) {
            h.setBlock(POS, Blocks.AIR);
            h.setBlock(POS, b);
            var be = h.getBlockEntity(POS);
            h.assertTrue(!(boolean) gate.invoke(null, be, true), "Ordinary block cannot glint: " + b);
            level(h, be, 5);
            h.assertTrue((boolean) gate.invoke(null, be, true), "Enchanted supported block must glint: " + b);
            h.assertTrue(!(boolean) gate.invoke(null, be, false), "CEM renderGlint=false must disable overlay: " + b);
            var saved = be.saveWithFullMetadata(h.getLevel().registryAccess());
            var restored = BlockEntity.loadStatic(be.getBlockPos(), be.getBlockState(), saved, h.getLevel().registryAccess());
            h.assertTrue((boolean) gate.invoke(null, restored, true), "Reloaded enchantments must drive glint: " + b);
            ((EnchantableBlockEntity) be).setEnchantment(ItemEnchantments.EMPTY);
            h.assertTrue(!(boolean) gate.invoke(null, be, true), "Removing enchantments must stop glint: " + b);
            variants++;
        }
        var ordinary = AllBlockEntityTypes.MECHANICAL_PRESS.get().create(BlockPos.ZERO, AllBlocks.MECHANICAL_PRESS.getDefaultState());
        h.assertTrue(!(boolean) gate.invoke(null, ordinary, true), "Ordinary Create machine excluded");
        var cemPress = new EnchantableMechanicalPressBlockEntity(AllBlockEntityTypes.MECHANICAL_PRESS.get(), BlockPos.ZERO, AllBlocks.MECHANICAL_PRESS.getDefaultState());
        level(h, cemPress, 5);
        h.assertTrue(!(boolean) gate.invoke(null, cemPress, true), "Foreign CEM instances excluded even when enchanted");
        var unsupported = AllBlockEntityTypes.MECHANICAL_ROLLER.get().create(BlockPos.ZERO, AllBlocks.MECHANICAL_ROLLER.getDefaultState());
        h.assertTrue(!(boolean) gate.invoke(null, unsupported, true), "Unsupported Encased machine excluded");
        h.assertTrue(!(boolean) gate.invoke(null, null, true), "Null excluded");
        h.assertTrue(variants == 75, "All supported variants exercised");
        System.out.println("AERO_EVIDENCE glint policy variants=" + variants + " plain/enchanted/config-off/reload/removed/foreign/null passed (not GPU rendering)");
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void carriedGlintSelectionAllVariants(GameTestHelper h) throws ReflectiveOperationException {
        // Reflection deliberately permits a production-JAR RED run before adding the gate.
        var gate = Compatibility.class.getMethod("shouldGlintCarried", net.minecraft.world.level.block.state.BlockState.class, BlockEntity.class, boolean.class);
        int variants = 0;
        for (Block b : BuiltInRegistries.BLOCK.stream().filter(Compatibility::supports).toList()) {
            h.setBlock(POS, Blocks.AIR);
            for (Direction side : Direction.Plane.HORIZONTAL) h.setBlock(POS.relative(side), Blocks.STONE);
            h.setBlock(POS, b);
            var be = h.getBlockEntity(POS);
            var captured = new CapturedContraption();
            captured.assemble(h.getLevel(), h.absolutePos(POS));
            var plain = captured.getBlocks().get(BlockPos.ZERO);
            var plainBe = ((net.minecraft.world.level.block.EntityBlock) b).newBlockEntity(BlockPos.ZERO, plain.state());
            plainBe.handleUpdateTag(plain.nbt(), h.getLevel().registryAccess());
            h.assertTrue(!(boolean) gate.invoke(null, plain.state(), plainBe, true), "Unenchanted carried block excluded: " + b);
            level(h, be, 5);
            captured = new CapturedContraption();
            captured.assemble(h.getLevel(), h.absolutePos(POS));
            var info = captured.getBlocks().get(BlockPos.ZERO);
            // Same newBlockEntity/handleUpdateTag path as ClientContraption.readBlockEntity (non-legacy).
            var carried = ((net.minecraft.world.level.block.EntityBlock) b).newBlockEntity(BlockPos.ZERO, info.state());
            carried.handleUpdateTag(info.nbt(), h.getLevel().registryAccess());
            h.assertTrue((boolean) gate.invoke(null, info.state(), carried, true), "Actual carried enchantments select casing: " + b);
            h.assertTrue(!(boolean) gate.invoke(null, info.state(), carried, false), "Config off excludes carried glint: " + b);
            h.assertTrue(!(boolean) gate.invoke(null, Blocks.AIR.defaultBlockState(), carried, true), "Portal-hidden/foreign state excluded: " + b);
            h.assertTrue(!(boolean) gate.invoke(null, info.state(), null, true), "Missing carried BE excluded: " + b);
            ((EnchantableBlockEntity) carried).setEnchantment(ItemEnchantments.EMPTY);
            h.assertTrue(!(boolean) gate.invoke(null, info.state(), carried, true), "Removed carried enchantments excluded: " + b);
            variants++;
        }
        var foreign = new EnchantableMechanicalPressBlockEntity(AllBlockEntityTypes.MECHANICAL_PRESS.get(), BlockPos.ZERO, AllBlocks.MECHANICAL_PRESS.getDefaultState());
        level(h, foreign, 5);
        h.assertTrue(!(boolean) gate.invoke(null, foreign.getBlockState(), foreign, true), "Ordinary Create/CEM untouched");
        h.assertTrue(!(boolean) gate.invoke(null, null, foreign, true), "Missing carried state excluded");
        h.assertTrue(variants == 75, "All 75 supported casings selected from Create capture NBT");
        System.out.println("AERO_EVIDENCE carried glint selection variants=" + variants + " actual Create capture/newBlockEntity/updateTag plain/enchanted/config-off/hidden/missing/removed/foreign passed (not GPU rendering)");
        h.succeed();
    }

    private static int pressCycle(GameTestHelper h, int efficiency) {
        var press = (EnchantableMechanicalPressBlockEntity) entity(h, "brass_press", efficiency);
        Vec3 itemPos = Vec3.atCenterOf(h.absolutePos(POS.below()));
        var item = new ItemEntity(h.getLevel(), itemPos.x, itemPos.y, itemPos.z, new ItemStack(Items.IRON_INGOT));
        item.setOnGround(true);
        h.getLevel().addFreshEntity(item);
        press.pressingBehaviour.start(PressingBehaviour.Mode.WORLD);
        int ticks = 0;
        while (press.pressingBehaviour.running && ticks < 300) { press.pressingBehaviour.tick(); ticks++; }
        h.assertTrue(press.pressingBehaviour.finished, "Press must finish a real behavior cycle");
        h.assertTrue(!item.isAlive() || !item.getItem().is(Items.IRON_INGOT), "Press cycle must consume the ingot");
        var ironSheet = com.simibubi.create.AllItems.IRON_SHEET.get();
        h.assertTrue(!h.getLevel().getEntitiesOfClass(ItemEntity.class, new net.minecraft.world.phys.AABB(itemPos, itemPos).inflate(1), e -> e.getItem().is(ironSheet)).isEmpty(), "Press cycle must produce an iron sheet");
        h.killAllEntities();
        return ticks;
    }
    @GameTest(template = "empty")
    public static void efficiencyActuallyAcceleratesPressCycle(GameTestHelper h) {
        int plain = pressCycle(h, 0), enchanted = pressCycle(h, 5);
        h.assertTrue(enchanted < plain, "Enchanted press faster: " + enchanted + " vs " + plain);
        System.out.println("AERO_EVIDENCE press cycle ticks plain=" + plain + " efficiency5=" + enchanted);
        h.succeed();
    }

    private static int mixerTime(GameTestHelper h, int efficiency) throws ReflectiveOperationException {
        var mixer = (EnchantableMechanicalMixerBlockEntity) entity(h, "brass_mixer", efficiency);
        var recipe = h.getLevel().getRecipeManager().byKey(ResourceLocation.fromNamespaceAndPath("create", "mixing/brass_ingot")).orElseThrow().value();
        Field recipeField = com.simibubi.create.content.processing.basin.BasinOperatingBlockEntity.class.getDeclaredField("currentRecipe");
        recipeField.setAccessible(true);
        recipeField.set(mixer, recipe);
        mixer.running = true; mixer.runningTicks = 20; mixer.processingTicks = -1;
        mixer.tick();
        return mixer.processingTicks;
    }
    @GameTest(template = "empty")
    public static void efficiencyActuallyAcceleratesMixerTimer(GameTestHelper h) throws ReflectiveOperationException {
        int plain = mixerTime(h, 0), enchanted = mixerTime(h, 5);
        h.assertTrue(enchanted > 0 && enchanted < plain, "Mixer processing timer faster: " + enchanted + " vs " + plain);
        System.out.println("AERO_EVIDENCE mixer timer plain=" + plain + " efficiency5=" + enchanted);
        h.succeed();
    }

    private static int fanTime(GameTestHelper h, int efficiency) throws ReflectiveOperationException {
        var fan = (EnchantableEncasedFanBlockEntity) entity(h, "brass_encased_fan", efficiency);
        fan.tick();
        Field field = fan.airCurrent.getClass().getDeclaredField("fanProcessing");
        field.setAccessible(true);
        var processor = (EnchantableFanProcessing) field.get(fan.airCurrent);
        var item = new ItemEntity(h.getLevel(), 0, 0, 0, new ItemStack(Items.BEEF));
        int ticks = 0;
        while (item.getItem().is(Items.BEEF) && ticks < 500) { processor.applyProcessing(item, AllFanProcessingTypes.SMOKING); ticks++; }
        h.assertTrue(item.getItem().is(Items.COOKED_BEEF), "Fan's bound CEM processor must actually cook beef");
        return ticks;
    }
    @GameTest(template = "empty")
    public static void efficiencyActuallyAcceleratesFanProcessing(GameTestHelper h) throws ReflectiveOperationException {
        int plain = fanTime(h, 0), enchanted = fanTime(h, 5);
        h.assertTrue(enchanted < plain, "Fan processing faster: " + enchanted + " vs " + plain);
        System.out.println("AERO_EVIDENCE fan processor calls plain=" + plain + " efficiency5=" + enchanted);
        h.succeed();
    }
    @GameTest(template = "empty")
    public static void spoutActuallyFillsBottleThroughEncasedFluidCapability(GameTestHelper h) throws ReflectiveOperationException {
        var spout = (EnchantableSpoutBlockEntity) entity(h, "andesite_spout", 5);
        var cap = h.getLevel().getCapability(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK,
                h.absolutePos(POS), Direction.UP);
        h.assertTrue(cap != null, "Encased fluid capability must remain registered");
        h.assertTrue(cap.fill(new net.neoforged.neoforge.fluids.FluidStack(net.minecraft.world.level.material.Fluids.WATER, 1000),
                net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE) == 1000, "Spout accepts water");
        var outputs = new java.util.ArrayList<ItemStack>();
        var handler = new com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour(spout, (maxDistance, processing) -> {}) {
            @Override
            public void handleProcessingOnItem(com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack input, TransportedResult result) {
                result.getOutputs().forEach(output -> outputs.add(output.stack));
            }
        };
        var input = new com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack(new ItemStack(Items.GLASS_BOTTLE));
        var heldMethod = EnchantableSpoutBlockEntity.class.getDeclaredMethod("whenItemHeld", input.getClass(), com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour.class);
        heldMethod.setAccessible(true);
        heldMethod.invoke(spout, input, handler);
        h.assertTrue(spout.processingTicks == 10, "Enchanted spout starts its 10-tick cycle");
        int calls = 0;
        while (spout.processingTicks > spout.getExecutionTick() && calls++ < 30) spout.tick();
        heldMethod.invoke(spout, input, handler);
        h.assertTrue(outputs.size() == 1 && outputs.getFirst().is(Items.POTION), "Actual spout filling must output a water bottle");
        h.assertTrue(cap.getFluidInTank(0).getAmount() < 1000 && input.stack.isEmpty(), "Filling consumes water and glass bottle");
        System.out.println("AERO_EVIDENCE spout actual bottle output=" + outputs.getFirst() + " remainingWater=" + cap.getFluidInTank(0).getAmount());
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void efficiencyChangesActualSpoutExecutionTimer(GameTestHelper h) {
        var plain = (EnchantableSpoutBlockEntity) entity(h, "andesite_spout", 0);
        int base = plain.getEnchantedFillingTime();
        var enchanted = (EnchantableSpoutBlockEntity) entity(h, "andesite_spout", 5);
        h.assertTrue(enchanted.getEnchantedFillingTime() < base && enchanted.getExecutionTick() < plain.getExecutionTick(), "CEM spout filling and execution timers must accelerate");
        enchanted.tick();
        System.out.println("AERO_EVIDENCE spout filling timer plain=" + base + " efficiency5=" + enchanted.getEnchantedFillingTime());
        h.succeed();
    }
}
