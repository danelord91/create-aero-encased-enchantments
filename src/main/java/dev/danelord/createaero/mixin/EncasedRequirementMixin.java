package dev.danelord.createaero.mixin;

import com.simibubi.create.api.schematic.requirement.SpecialBlockItemRequirement;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import dev.danelord.createaero.Compatibility;
import fr.iglee42.createcasing.blocks.customs.CustomEncasedFanBlock;
import fr.iglee42.createcasing.blocks.customs.CustomMixerBlock;
import fr.iglee42.createcasing.blocks.customs.CustomPressBlock;
import fr.iglee42.createcasing.blocks.customs.CustomDrillBlock;
import fr.iglee42.createcasing.blocks.customs.CustomSawBlock;
import fr.iglee42.createcasing.blocks.customs.CustomHarvesterBlock;
import fr.iglee42.createcasing.blocks.customs.CustomRollerBlock;
import fr.iglee42.createcasing.blocks.fluids.CustomSpoutBlock;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = {CustomPressBlock.class, CustomMixerBlock.class, CustomEncasedFanBlock.class, CustomSpoutBlock.class, CustomDrillBlock.class, CustomSawBlock.class, CustomHarvesterBlock.class, CustomRollerBlock.class}, remap = false)
public abstract class EncasedRequirementMixin implements SpecialBlockItemRequirement {
    @Override
    public ItemRequirement getRequiredItems(BlockState state, BlockEntity be) {
        ItemStack stack = Compatibility.copyEnchantments(new ItemStack(state.getBlock()), state, be);
        return new ItemRequirement(new ItemRequirement.StrictNbtStackRequirement(stack, ItemRequirement.ItemUseType.CONSUME));
    }
}
