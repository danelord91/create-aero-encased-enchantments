package dev.danelord.createaero.testing;

import com.simibubi.create.AllBlocks;
import dev.danelord.createaero.AeroEnchantCompat;
import dev.danelord.createaero.Compatibility;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(AeroEnchantCompat.ID)
@PrefixGameTestTemplate(false)
public final class OptionalModsGameTests {
    @GameTest(template = "empty")
    public static void optionalModPresenceAndBaseControl(GameTestHelper h) {
        boolean encased = ModList.get().isLoaded("createcasing");
        boolean decor = ModList.get().isLoaded("dndecor");
        if (!encased) {
            h.assertTrue(AeroEnchantCompat.PLOUGH_ENTITY == null, "No Encased plough entity when mod absent");
            h.assertTrue(BuiltInRegistries.BLOCK.stream().noneMatch(b -> BuiltInRegistries.BLOCK.getKey(b).getNamespace().equals("createcasing")), "Encased blocks must really be absent");
        }
        h.assertTrue(!Compatibility.supports(AllBlocks.MILLSTONE.get()), "Ordinary Create millstone remains unchanged");
        System.out.println("AERO_OPTIONAL_MODS encased=" + encased + " dnd=" + decor);
        h.succeed();
    }
}
