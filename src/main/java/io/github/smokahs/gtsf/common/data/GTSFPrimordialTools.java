package io.github.smokahs.gtsf.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.common.data.GTCreativeModeTabs;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.model.generators.ModelFile;

import com.tterrag.registrate.util.entry.ItemEntry;
import io.github.smokahs.gtsf.StarFoundry;
import io.github.smokahs.gtsf.common.item.tool.PrimordialToolItem;
import io.github.smokahs.gtsf.config.GTSFConfig;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class GTSFPrimordialTools {

    private static final TagKey<Item> GTCEU_TOOLS = TagKey.create(Registries.ITEM,
            new ResourceLocation("gtceu", "tools"));

    private static final Map<GTToolType, ItemEntry<PrimordialToolItem>> PRIMORDIAL_TOOLS = new LinkedHashMap<>();
    private static final Map<String, ItemEntry<PrimordialToolItem>> PRIMORDIAL_TOOLS_BY_ID = new LinkedHashMap<>();

    private static final List<PrimordialToolDefinition> ACTIVE_DEFINITIONS = List.of(
            define("primordial_hard_hammer", "Primordial Hard Hammer", "item/tools/hammer", GTMaterials.Neutronium,
                    GTToolType.HARD_HAMMER),
            define("primordial_soft_mallet", "Primordial Soft Mallet", "item/tools/mallet", GTMaterials.Rubber,
                    GTToolType.SOFT_MALLET),
            define("primordial_wrench", "Primordial Wrench", "item/tools/wrench", GTMaterials.Neutronium,
                    GTToolType.WRENCH,
                    GTToolType.WRENCH_LV, GTToolType.WRENCH_HV, GTToolType.WRENCH_IV),
            define("primordial_crowbar", "Primordial Crowbar", "item/tools/crowbar", GTMaterials.Neutronium,
                    GTToolType.CROWBAR),
            define("primordial_screwdriver", "Primordial Screwdriver", "item/tools/screwdriver", GTMaterials.Neutronium,
                    GTToolType.SCREWDRIVER,
                    GTToolType.SCREWDRIVER_LV, GTToolType.SCREWDRIVER_HV, GTToolType.SCREWDRIVER_IV),
            define("primordial_mortar", "Primordial Mortar", "item/tools/mortar", GTMaterials.Neutronium,
                    GTToolType.MORTAR),
            define("primordial_wire_cutter", "Primordial Wire Cutters", "item/tools/wire_cutter",
                    GTMaterials.Neutronium,
                    GTToolType.WIRE_CUTTER, GTToolType.WIRE_CUTTER_LV, GTToolType.WIRE_CUTTER_HV,
                    GTToolType.WIRE_CUTTER_IV),
            define("primordial_plunger", "Primordial Plunger", "item/tools/plunger", GTMaterials.Rubber,
                    GTToolType.PLUNGER),
            define("primordial_saw", "Primordial Saw", "item/tools/saw", GTMaterials.Neutronium, GTToolType.SAW),
            define("primordial_file", "Primordial File", "item/tools/file", GTMaterials.Neutronium, GTToolType.FILE),
            define("primordial_knife", "Primordial Knife", "item/tools/knife", GTMaterials.Neutronium,
                    GTToolType.KNIFE));

    private static final Set<String> AVARITIA_STYLED_IDS = ACTIVE_DEFINITIONS.stream()
            .map(PrimordialToolDefinition::id)
            .collect(Collectors.toUnmodifiableSet());

    private GTSFPrimordialTools() {}

    public static boolean isAvaritiaStyled(String id) {
        return AVARITIA_STYLED_IDS.contains(id);
    }

    public static void generatePrimordialTools() {
        registerExtraLang();
        StarFoundry.REGISTRATE.creativeModeTab(() -> GTCreativeModeTabs.TOOL);
        for (var definition : ACTIVE_DEFINITIONS) {
            if (!isToolEnabled(definition.id())) {
                StarFoundry.LOGGER.info("Skipping registration of {} (disabled in gtsf.yaml)", definition.id());
                continue;
            }
            register(definition, true);
        }
    }

    private static boolean isToolEnabled(String id) {
        var items = GTSFConfig.get().tools.items;
        return switch (id) {
            case "primordial_hard_hammer" -> items.primordialHardHammer;
            case "primordial_soft_mallet" -> items.primordialSoftMallet;
            case "primordial_wrench" -> items.primordialWrench;
            case "primordial_crowbar" -> items.primordialCrowbar;
            case "primordial_screwdriver" -> items.primordialScrewdriver;
            case "primordial_mortar" -> items.primordialMortar;
            case "primordial_wire_cutter" -> items.primordialWireCutter;
            case "primordial_plunger" -> items.primordialPlunger;
            case "primordial_saw" -> items.primordialSaw;
            case "primordial_file" -> items.primordialFile;
            case "primordial_knife" -> items.primordialKnife;
            default -> true;
        };
    }

    public static Collection<ItemEntry<PrimordialToolItem>> getEntries() {
        return Collections.unmodifiableCollection(PRIMORDIAL_TOOLS_BY_ID.values());
    }

    public static Map<GTToolType, ItemEntry<PrimordialToolItem>> getEntriesByType() {
        return Collections.unmodifiableMap(PRIMORDIAL_TOOLS);
    }

    public static @Nullable ItemEntry<PrimordialToolItem> getEntry(GTToolType toolType) {
        return PRIMORDIAL_TOOLS.get(toolType);
    }

    public static ItemStack getStack(GTToolType toolType) {
        var entry = getEntry(toolType);
        return entry == null ? ItemStack.EMPTY : entry.get().get();
    }

    private static void registerExtraLang() {
        StarFoundry.REGISTRATE.addRawLang("item.gtsf.tool.harvest_level.godforged", "§5Godforged");
        StarFoundry.REGISTRATE.addRawLang("item.gtsf.tool.mode.insta_mine", "Instant Mining");
        StarFoundry.REGISTRATE.addRawLang("item.gtsf.tool.mode.precision_mine", "Precision Mining");
        StarFoundry.REGISTRATE.addRawLang("item.gtsf.tool.tooltip.mining_mode", "Mining Mode: %s");
        StarFoundry.REGISTRATE.addRawLang("item.gtsf.tool.tooltip.mode_switch_hint",
                "Crouch + Scroll to change mining mode");
        StarFoundry.REGISTRATE.addRawLang("config.gtsf.option.tools", "Tools");
        StarFoundry.REGISTRATE.addRawLang("config.gtsf.option.general", "General");
        StarFoundry.REGISTRATE.addRawLang("config.gtsf.option.items", "Items");
        StarFoundry.REGISTRATE.addRawLang("config.gtsf.option.relocateMinedBlocks", "Relocate Mined Blocks");
        StarFoundry.REGISTRATE.addRawLang("config.gtsf.option.precisionMiningSpeed", "Precision Mining Speed");
        StarFoundry.REGISTRATE.addRawLang("config.gtsf.option.primordialShader", "Primordial Shader");
        StarFoundry.REGISTRATE.addRawLang("config.gtsf.option.primordialHardHammer", "Primordial Hard Hammer");
        StarFoundry.REGISTRATE.addRawLang("config.gtsf.option.primordialSoftMallet", "Primordial Soft Mallet");
        StarFoundry.REGISTRATE.addRawLang("config.gtsf.option.primordialWrench", "Primordial Wrench");
        StarFoundry.REGISTRATE.addRawLang("config.gtsf.option.primordialCrowbar", "Primordial Crowbar");
        StarFoundry.REGISTRATE.addRawLang("config.gtsf.option.primordialScrewdriver", "Primordial Screwdriver");
        StarFoundry.REGISTRATE.addRawLang("config.gtsf.option.primordialMortar", "Primordial Mortar");
        StarFoundry.REGISTRATE.addRawLang("config.gtsf.option.primordialWireCutter", "Primordial Wire Cutters");
        StarFoundry.REGISTRATE.addRawLang("config.gtsf.option.primordialPlunger", "Primordial Plunger");
        StarFoundry.REGISTRATE.addRawLang("config.gtsf.option.primordialSaw", "Primordial Saw");
        StarFoundry.REGISTRATE.addRawLang("config.gtsf.option.primordialFile", "Primordial File");
        StarFoundry.REGISTRATE.addRawLang("config.gtsf.option.primordialKnife", "Primordial Knife");
        StarFoundry.REGISTRATE.addRawLang("config.gtsf.option.primordialHealingAxe", "Primordial Healing Axe");
    }

    private static void register(PrimordialToolDefinition definition, boolean visible) {
        var material = definition.material();
        var tier = material.getToolTier();
        var primordialToolClasses = new LinkedHashSet<GTToolType>();
        var primordialToolClassNames = new LinkedHashSet<String>();

        for (var toolType : definition.toolTypes()) {
            primordialToolClasses.addAll(toolType.toolClasses);
        }
        for (var toolType : primordialToolClasses) {
            primordialToolClassNames.addAll(toolType.toolClassNames);
        }

        var matchTags = new LinkedHashSet<TagKey<Item>>();
        for (var toolType : definition.toolTypes()) {
            matchTags.addAll(toolType.matchTags);
        }
        for (var toolType : primordialToolClasses) {
            matchTags.addAll(toolType.matchTags);
        }
        matchTags.add(GTCEU_TOOLS);
        @SuppressWarnings({ "unchecked", "rawtypes" })
        TagKey<Item>[] matchTagArray = matchTags.toArray(new TagKey[0]);

        ItemEntry<PrimordialToolItem> entry = StarFoundry.REGISTRATE
                .item(definition.id(),
                        p -> new PrimordialToolItem(definition.representative(), tier, material,
                                definition.representative().toolDefinition, p,
                                primordialToolClasses, primordialToolClassNames))
                .properties(p -> p.craftRemainder(Items.AIR))
                .lang(definition.lang())
                .color(() -> IGTTool::tintColor)
                .tag(matchTagArray)
                .model((ctx, prov) -> {
                    if (!visible) {
                        prov.withExistingParent(prov.name(ctx), modelLocation(definition.modelPath()));
                        return;
                    }
                    var builder = prov.getBuilder(definition.id())
                            .parent(new ModelFile.UncheckedModelFile("item/handheld"))
                            .texture("layer0", prov.modLoc("item/" + definition.id() + "_handle"))
                            .texture("layer1", prov.modLoc("item/" + definition.id() + "_head"));
                    if (definition.id().equals("primordial_saw")) {
                        builder.transforms()
                                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)
                                .rotation(0.0F, -90.0F, 55.0F).translation(0.0F, -1.0F, -3.25F)
                                .scale(0.85F, 0.85F, 0.85F).end()
                                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND)
                                .rotation(0.0F, -90.0F, -55.0F).translation(0.0F, -1.0F, -3.25F)
                                .scale(0.85F, 0.85F, 0.85F).end()
                                .end();
                    }
                })
                .register();

        if (visible) {
            PRIMORDIAL_TOOLS_BY_ID.put(definition.id(), entry);
            for (var toolType : definition.toolTypes()) {
                PRIMORDIAL_TOOLS.put(toolType, entry);
            }
        }
    }

    private static PrimordialToolDefinition define(String id, String lang, String modelPath, Material material,
                                                   GTToolType representative,
                                                   GTToolType... extraTypes) {
        var toolTypes = new java.util.ArrayList<GTToolType>();
        toolTypes.add(representative);
        Collections.addAll(toolTypes, extraTypes);
        return new PrimordialToolDefinition(id, lang, modelPath, material, representative, List.copyOf(toolTypes));
    }

    private static ResourceLocation modelLocation(String modelPath) {
        return modelPath.contains(":") ? new ResourceLocation(modelPath) : GTCEu.id(modelPath);
    }

    private record PrimordialToolDefinition(String id, String lang, String modelPath, Material material,
                                            GTToolType representative,
                                            List<GTToolType> toolTypes) {}
}
