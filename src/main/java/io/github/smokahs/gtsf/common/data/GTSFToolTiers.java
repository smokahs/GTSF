package io.github.smokahs.gtsf.common.data;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.TierSortingRegistry;

import io.github.smokahs.gtsf.StarFoundry;

import java.util.ArrayList;
import java.util.List;

public class GTSFToolTiers {

    public static final TagKey<Block> NEEDS_GODFORGED_TOOL = TagKey.create(Registries.BLOCK,
            StarFoundry.id("needs_godforged_tool"));

    private static Tier GODFORGED;

    public static void init() {
        var godforged = StarFoundry.id("godforged");

        List<Object> after = new ArrayList<>();
        after.add(new ResourceLocation("netherite"));
        var neutronium = new ResourceLocation("gtceu", "neutronium");
        boolean hasNeutronium = TierSortingRegistry.getSortedTiers().stream()
                .anyMatch(t -> neutronium.equals(TierSortingRegistry.getName(t)));
        if (hasNeutronium) {
            after.add(neutronium);
        }

        GODFORGED = TierSortingRegistry.registerTier(
                new ForgeTier(7, Integer.MAX_VALUE, 256.0F, 120.0F, 33, NEEDS_GODFORGED_TOOL,
                        () -> Ingredient.of(ChemicalHelper.getTag(TagPrefix.ingot, GTMaterials.Neutronium))),
                godforged,
                after,
                List.of());
    }

    public static Tier getGodforgedTier() {
        return GODFORGED;
    }
}
