package io.github.smokahs.gtsf.common.item.tool;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.item.tool.GTToolItem;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.IGTToolDefinition;
import com.gregtechceu.gtceu.api.item.tool.MaterialToolTier;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.item.tool.aoe.AoESymmetrical;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import io.github.smokahs.gtsf.client.model.item.PrimordialShaderItem;
import io.github.smokahs.gtsf.client.model.item.PrimordialToolItemClient;
import io.github.smokahs.gtsf.common.data.GTSFToolTiers;
import io.github.smokahs.gtsf.config.GTSFConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class PrimordialToolItem extends GTToolItem implements PrimordialShaderItem {

    public static final int GODFORGED_HARVEST_LEVEL = 7;
    public static final String MINING_MODE_KEY = "PrimordialMiningMode";
    private static final String INFINITY_SYMBOL = "∞";
    private static final String OMEGA_SYMBOL = "Ω";
    private static final float INSTA_MINING_SPEED = 1_000_000.0F;
    private static final float PRECISION_MINING_SPEED = 8.0F;
    private final Set<GTToolType> primordialToolClasses;
    private final Set<String> primordialToolClassNames;

    public PrimordialToolItem(GTToolType toolType, MaterialToolTier tier, Material material,
                              IGTToolDefinition definition,
                              Properties properties, Set<GTToolType> toolClasses, Set<String> toolClassNames) {
        super(toolType, tier, material, definition, properties);
        this.primordialToolClasses = Collections.unmodifiableSet(new LinkedHashSet<>(toolClasses));
        this.primordialToolClassNames = Collections.unmodifiableSet(new LinkedHashSet<>(toolClassNames));
        if (GTCEu.isClientSide()) {
            PrimordialToolItemClient.init(this, toolType);
        }
    }

    @Override
    public String getDescriptionId() {
        return this.getOrCreateDescriptionId();
    }

    @Override
    public Component getDescription() {
        return Component.translatable(getDescriptionId());
    }

    @Override
    public Component getName(@NotNull ItemStack stack) {
        return getDescription();
    }

    @Override
    public ItemStack get() {
        ItemStack stack = super.get();
        stack.getOrCreateTag().putBoolean(ToolHelper.UNBREAKABLE_KEY, true);
        applyRelocateMinedBlocks(stack);
        return stack;
    }

    private static void applyRelocateMinedBlocks(ItemStack stack) {
        ToolHelper.getBehaviorsTag(stack).putBoolean(ToolHelper.RELOCATE_MINED_BLOCKS_KEY,
                GTSFConfig.get().tools.relocateMinedBlocks);
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
        applyRelocateMinedBlocks(stack);
        return super.onBlockStartBreak(stack, pos, player);
    }

    @Override
    public Tier getTier() {
        return GTSFToolTiers.getGodforgedTier();
    }

    @Override
    public int getTotalHarvestLevel(ItemStack stack) {
        return GODFORGED_HARVEST_LEVEL;
    }

    @Override
    public float getTotalAttackDamage(ItemStack stack) {
        return 0.0F;
    }

    @Override
    public float getTotalAttackSpeed(ItemStack stack) {
        return 0.0F;
    }

    @Override
    public float getTotalToolSpeed(ItemStack stack) {
        return getMiningMode(stack).toolSpeed;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (!isGregTechBlock(state)) {
            return 1.0F;
        }
        return super.getDestroySpeed(stack, state);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return isGregTechBlock(state) && super.isCorrectToolForDrops(stack, state);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return false;
    }

    @Override
    public Set<GTToolType> getToolClasses(ItemStack stack) {
        return primordialToolClasses;
    }

    @Override
    public Set<String> getToolClassNames(ItemStack stack) {
        return primordialToolClassNames;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip,
                                TooltipFlag flag) {
        IGTToolDefinition toolStats = getToolStats();

        tooltip.add(Component.translatable("item.gtceu.tool.tooltip.general_uses", INFINITY_SYMBOL));

        if (toolStats.isSuitableForAttacking(stack)) {
            tooltip.add(Component.translatable("item.gtceu.tool.tooltip.attack_damage",
                    FormattingUtil.formatNumbers(2 + getTotalAttackDamage(stack))));
            tooltip.add(Component.translatable("item.gtceu.tool.tooltip.attack_speed",
                    FormattingUtil.formatNumbers(4 + getTotalAttackSpeed(stack))));
        }

        if (toolStats.isSuitableForBlockBreak(stack)) {
            MiningMode miningMode = getMiningMode(stack);
            tooltip.add(Component.translatable("item.gtceu.tool.tooltip.mining_speed",
                    miningMode == MiningMode.INSTA ? INFINITY_SYMBOL :
                            FormattingUtil.formatNumbers(getTotalToolSpeed(stack))));
            tooltip.add(Component.translatable("item.gtceu.tool.tooltip.harvest_level_extra", OMEGA_SYMBOL,
                    Component.translatable("item.gtsf.tool.harvest_level.godforged")));
            tooltip.add(Component.translatable("item.gtsf.tool.tooltip.mining_mode", miningMode.getDisplayName())
                    .withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("item.gtsf.tool.tooltip.mode_switch_hint")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }

        boolean addedBehaviorNewLine = false;
        AoESymmetrical aoeDefinition = ToolHelper.getAoEDefinition(stack);

        if (!aoeDefinition.isZero()) {
            addedBehaviorNewLine = tooltip.add(CommonComponents.EMPTY);
            tooltip.add(Component.translatable("item.gtceu.tool.behavior.aoe_mining",
                    aoeDefinition.column * 2 + 1, aoeDefinition.row * 2 + 1, aoeDefinition.layer + 1));
        }

        CompoundTag behaviorsTag = ToolHelper.getBehaviorsTag(stack);
        if (behaviorsTag.getBoolean(ToolHelper.RELOCATE_MINED_BLOCKS_KEY)) {
            if (!addedBehaviorNewLine) {
                addedBehaviorNewLine = true;
                tooltip.add(CommonComponents.EMPTY);
            }
            tooltip.add(Component.translatable("item.gtceu.tool.behavior.relocate_mining"));
        }

        if (!addedBehaviorNewLine && !toolStats.getBehaviors().isEmpty()) {
            tooltip.add(CommonComponents.EMPTY);
        }
        toolStats.getBehaviors().forEach(behavior -> behavior.addInformation(stack, world, tooltip, flag));

        String uniqueTooltip = getToolType().getUnlocalizedName() + ".tooltip";
        if (Language.getInstance().has(uniqueTooltip)) {
            tooltip.add(CommonComponents.EMPTY);
            tooltip.add(Component.translatable(uniqueTooltip));
        }

        tooltip.add(CommonComponents.EMPTY);

        var defaultEnchants = getDefaultEnchantments(stack);
        if (!defaultEnchants.isEmpty()) {
            tooltip.add(Component.translatable("item.gtceu.tool.tooltip.default_enchantments"));
            for (var entry : defaultEnchants.entrySet()) {
                Enchantment enchant = entry.getKey();
                if (enchant == null) continue;

                tooltip.add(enchant.getFullname(entry.getValue()));
            }
        }

        tooltip.add(CommonComponents.EMPTY);

        tooltip.add(Component.translatable("item.gtceu.tool.usable_as",
                getToolClassNames(stack).stream()
                        .filter(s -> Language.getInstance().has("gtceu.tool.class." + s))
                        .map(s -> Component.translatable("gtceu.tool.class." + s))
                        .collect(Component::empty, FormattingUtil::combineComponents,
                                FormattingUtil::combineComponents)));
    }

    public MiningMode getMiningMode(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(MINING_MODE_KEY)) {
            tag.putByte(MINING_MODE_KEY, (byte) MiningMode.PRECISION.ordinal());
            return MiningMode.PRECISION;
        }
        int modeIndex = Math.floorMod(tag.getByte(MINING_MODE_KEY), MiningMode.VALUES.length);
        return MiningMode.VALUES[modeIndex];
    }

    public MiningMode cycleMiningMode(ItemStack stack, int toggles) {
        MiningMode current = getMiningMode(stack);
        if ((toggles & 1) == 0) {
            return current;
        }
        MiningMode next = current == MiningMode.INSTA ? MiningMode.PRECISION : MiningMode.INSTA;
        stack.getOrCreateTag().putByte(MINING_MODE_KEY, (byte) next.ordinal());
        return next;
    }

    private static boolean isGregTechBlock(BlockState state) {
        return GTCEu.MOD_ID.equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()).getNamespace());
    }

    public enum MiningMode {

        INSTA("item.gtsf.tool.mode.insta_mine", INSTA_MINING_SPEED, ChatFormatting.LIGHT_PURPLE),
        PRECISION("item.gtsf.tool.mode.precision_mine", PRECISION_MINING_SPEED, ChatFormatting.AQUA);

        public static final MiningMode[] VALUES = values();

        private final String langKey;
        private final float toolSpeed;
        private final ChatFormatting color;

        MiningMode(String langKey, float toolSpeed, ChatFormatting color) {
            this.langKey = langKey;
            this.toolSpeed = toolSpeed;
            this.color = color;
        }

        public Component getDisplayName() {
            return Component.translatable(langKey).withStyle(color);
        }
    }
}
