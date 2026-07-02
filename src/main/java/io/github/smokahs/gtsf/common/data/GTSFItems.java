package io.github.smokahs.gtsf.common.data;

import com.gregtechceu.gtceu.common.data.GTCreativeModeTabs;

import net.minecraft.world.item.Rarity;
import net.minecraftforge.client.model.generators.ModelFile;

import com.tterrag.registrate.util.entry.ItemEntry;
import io.github.smokahs.gtsf.StarFoundry;
import io.github.smokahs.gtsf.common.item.HealingAxeItem;
import io.github.smokahs.gtsf.config.GTSFConfig;

public final class GTSFItems {

    public static ItemEntry<HealingAxeItem> PRIMORDIAL_HEALING_AXE;

    private GTSFItems() {}

    public static void init() {
        if (!GTSFConfig.get().tools.primordialHealingAxe) {
            StarFoundry.LOGGER.info("Skipping registration of primordial_healing_axe (disabled in gtsf.yaml)");
            return;
        }
        StarFoundry.REGISTRATE.creativeModeTab(() -> GTCreativeModeTabs.TOOL);
        PRIMORDIAL_HEALING_AXE = StarFoundry.REGISTRATE
                .item("primordial_healing_axe", HealingAxeItem::new)
                .lang("Primordial Healing Axe")
                .properties(p -> p.stacksTo(1).rarity(Rarity.COMMON).fireResistant())
                .model((ctx, prov) -> prov.getBuilder("primordial_healing_axe")
                        .parent(new ModelFile.UncheckedModelFile("item/handheld"))
                        .texture("layer0", prov.modLoc("item/primordial_healing_axe_handle"))
                        .texture("layer1", prov.modLoc("item/primordial_healing_axe_head")))
                .register();
    }
}
