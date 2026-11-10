package dev.danelord.createaero;

import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.kinetics.drill.DrillMovementBehaviour;
import fr.iglee42.createcasing.blocks.customs.CustomDrillBlock;
import io.github.cotrin8672.cem.content.block.drill.EnchantableDrillMovementBehaviour;
import dev.danelord.createaero.mixin.SimpleRegistryAccessor;
import com.simibubi.create.content.contraptions.actors.plough.PloughMovementBehaviour;
import io.github.cotrin8672.cem.content.block.plough.EnchantablePloughMovementBehaviour;
import io.github.cotrin8672.cem.content.block.plough.EnchantablePloughBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.RegisterEvent;
import fr.iglee42.createcasing.blocks.customs.CustomRollerBlock;
import com.simibubi.create.content.contraptions.actors.roller.RollerMovementBehaviour;
import io.github.cotrin8672.cem.content.block.roller.EnchantableRollerMovementBehaviour;
import fr.iglee42.createcasing.blocks.customs.CustomHarvesterBlock;
import com.simibubi.create.content.contraptions.actors.harvester.HarvesterMovementBehaviour;
import io.github.cotrin8672.cem.content.block.harvester.EnchantableHarvesterMovementBehaviour;
import fr.iglee42.createcasing.blocks.customs.CustomSawBlock;
import com.simibubi.create.content.kinetics.saw.SawMovementBehaviour;
import io.github.cotrin8672.cem.content.block.saw.EnchantableSawMovementBehaviour;

@Mod(AeroEnchantCompat.ID)
public final class AeroEnchantCompat {
    public static final String ID = "aero_enchant_compat";
    public static BlockEntityType<EnchantablePloughBlockEntity> PLOUGH_ENTITY;

    public AeroEnchantCompat(IEventBus bus) {
        bus.addListener(this::setup);
        bus.addListener(this::registerEntities);
    }

    private void registerEntities(RegisterEvent event) {
        event.register(Registries.BLOCK_ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(ID, "encased_plough"), () -> {
            var blocks = BuiltInRegistries.BLOCK.stream().filter(Compatibility::isEncasedPlough)
                    .toArray(net.minecraft.world.level.block.Block[]::new);
            if (blocks.length != 9) throw new IllegalStateException("Expected nine Encased ploughs; got " + blocks.length);
            PLOUGH_ENTITY = BlockEntityType.Builder.of((pos, state) -> new EnchantablePloughBlockEntity(PLOUGH_ENTITY, pos, state), blocks).build(null);
            return PLOUGH_ENTITY;
        });
    }

    private void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            var registry = MovementBehaviour.REGISTRY;
            var registrations = ((SimpleRegistryAccessor) registry).aero$registrations();
            synchronized (registry) {
                BuiltInRegistries.BLOCK.stream().filter(block -> block instanceof CustomDrillBlock)
                    .filter(block -> BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals("createcasing"))
                    .forEach(block -> {
                        Object previous = registrations.get(block);
                        if (previous != null && previous.getClass() == DrillMovementBehaviour.class)
                            registrations.put(block, new EnchantableDrillMovementBehaviour());
                    });
                BuiltInRegistries.BLOCK.stream().filter(block -> block instanceof CustomSawBlock)
                    .filter(block -> BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals("createcasing"))
                    .forEach(block -> {
                        Object previous = registrations.get(block);
                        if (previous != null && previous.getClass() == SawMovementBehaviour.class)
                            registrations.put(block, new EnchantableSawMovementBehaviour());
                    });
                BuiltInRegistries.BLOCK.stream().filter(block -> block instanceof CustomHarvesterBlock)
                    .filter(block -> BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals("createcasing"))
                    .forEach(block -> {
                        Object previous = registrations.get(block);
                        if (previous != null && previous.getClass() == HarvesterMovementBehaviour.class)
                            registrations.put(block, new EnchantableHarvesterMovementBehaviour());
                    });
                BuiltInRegistries.BLOCK.stream().filter(block -> block instanceof CustomRollerBlock)
                    .filter(block -> BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals("createcasing"))
                    .forEach(block -> {
                        Object previous = registrations.get(block);
                        if (previous != null && previous.getClass() == RollerMovementBehaviour.class)
                            registrations.put(block, new EnchantableRollerMovementBehaviour());
                    });
                BuiltInRegistries.BLOCK.stream().filter(Compatibility::isEncasedPlough).forEach(block -> {
                    Object previous = registrations.get(block);
                    if (previous != null && previous.getClass() == PloughMovementBehaviour.class)
                        registrations.put(block, new EnchantablePloughMovementBehaviour());
                });
                registry.invalidate();
            }
        });
    }
}
