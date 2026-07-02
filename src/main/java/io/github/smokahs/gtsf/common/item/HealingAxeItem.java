package io.github.smokahs.gtsf.common.item;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.util.ModelUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import io.github.smokahs.gtsf.StarFoundry;
import io.github.smokahs.gtsf.client.model.item.CosmicToolBakedModel;
import io.github.smokahs.gtsf.client.model.item.PrimordialShaderItem;
import io.github.smokahs.gtsf.client.shader.PrimordialWindowShaders;
import io.github.smokahs.gtsf.common.data.GTSFToolTiers;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

public class HealingAxeItem extends AxeItem implements PrimordialShaderItem {
    private static final Tier HEALING_AXE_TIER = new Tier() {

        @Override
        public int getUses() {
            return Tiers.NETHERITE.getUses();
        }
        @Override
        public float getSpeed() {
            return 8.0F;
        }
        @Override
        public float getAttackDamageBonus() {
            return Tiers.NETHERITE.getAttackDamageBonus();
        }
        @Override
        public int getLevel() {
            return GTSFToolTiers.getGodforgedTier().getLevel();
        }
        @Override
        public int getEnchantmentValue() {
            return Tiers.NETHERITE.getEnchantmentValue();
        }
        @Override
        public net.minecraft.world.item.crafting.Ingredient getRepairIngredient() {
            return Tiers.NETHERITE.getRepairIngredient();
        }
    };
    private static final float HEAL_AMOUNT = 4.0F;
    private static final float UNDEAD_DAMAGE = 16.0F;
    private static final float HIT_EXHAUSTION = 10.0F;
    private static final int REGEN_TICK_INTERVAL = 20;
    private static final float REGEN_SATURATION_MODIFIER = 0.25F;
    private static final String INFINITY_SYMBOL = "∞";
    private static final String OMEGA_SYMBOL = "Ω";
    private static final ResourceLocation HANDLE_TEXTURE = StarFoundry.id("item/primordial_healing_axe_handle");
    private static final ResourceLocation HEAD_TEXTURE = StarFoundry.id("item/primordial_healing_axe_head");
    private static final int HEAD_OVERLAY_COLOR = 0x4DFFFFFF;
    private static final Method START_CONVERTING = ObfuscationReflectionHelper.findMethod(
            ZombieVillager.class, "m_34383_", UUID.class, int.class);
    public HealingAxeItem(Properties properties) {
        super(HEALING_AXE_TIER, 5.0F, -2.5F, properties);
        if (GTCEu.isClientSide()) {
            initClient();
        }
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (!player.level().isClientSide() && entity instanceof LivingEntity target) {
            if (target.getMobType() == MobType.UNDEAD) {
                target.hurt(player.damageSources().playerAttack(player), UNDEAD_DAMAGE);
                player.causeFoodExhaustion(HIT_EXHAUSTION);
            } else {
                target.heal(HEAL_AMOUNT);
                player.causeFoodExhaustion(HIT_EXHAUSTION);
                spawnHealParticles(target);
            }
        }
        return true;
    }
    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target,
                                                  InteractionHand hand) {
        if (target instanceof ZombieVillager zombieVillager) {
            if (!player.level().isClientSide()) {
                try {
                    START_CONVERTING.invoke(zombieVillager, player.getUUID(), 0);
                } catch (ReflectiveOperationException e) {
                    StarFoundry.LOGGER.error("failed to cure zombie villager", e);
                }
                spawnHealParticles(target);
            }
            return InteractionResult.sidedSuccess(player.level().isClientSide());
        }
        return InteractionResult.PASS;
    }
    private static void spawnHealParticles(LivingEntity target) {
        if (target.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.INSTANT_EFFECT,
                    target.getX(), target.getY() + target.getBbHeight() / 2.0, target.getZ(),
                    12, 0.5, 0.5, 0.5, 0.0);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide() && isSelected && entity instanceof Player player) {
            if (level.getGameTime() % REGEN_TICK_INTERVAL == 0) {
                player.getFoodData().eat(1, REGEN_SATURATION_MODIFIER);
            }
        }
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }
    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        tooltipComponents.add(Component.translatable("item.gtceu.tool.tooltip.general_uses", INFINITY_SYMBOL));
        tooltipComponents.add(Component.translatable("item.gtceu.tool.tooltip.mining_speed", "8"));
        tooltipComponents.add(Component.literal("Harvest Level: ").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(OMEGA_SYMBOL).withStyle(ChatFormatting.WHITE))
                .append(Component.literal(" (").withStyle(ChatFormatting.WHITE))
                .append(Component.translatable("item.gtsf.tool.harvest_level.godforged")
                        .withStyle(ChatFormatting.WHITE))
                .append(Component.literal(")").withStyle(ChatFormatting.WHITE)));
        tooltipComponents.add(Component.empty());
        tooltipComponents.add(Component.literal("Passive: ").withStyle(ChatFormatting.GOLD)
                .append(Component.literal("Restores hunger and stamina when held in either hand")
                        .withStyle(ChatFormatting.WHITE)));
        tooltipComponents.add(Component.empty());
        tooltipComponents.add(Component.literal("Left Click: ").withStyle(ChatFormatting.GREEN)
                .append(Component.literal("Heals living creatures on hit").withStyle(ChatFormatting.WHITE)));
        tooltipComponents.add(Component.literal("Right Click: ").withStyle(ChatFormatting.LIGHT_PURPLE)
                .append(Component.literal("Cures Zombie Villagers").withStyle(ChatFormatting.WHITE)));
    }

    private void initClient() {
        ModelUtils.registerBakeEventListener(false, event -> {
            ResourceLocation itemId = StarFoundry.id("primordial_healing_axe");
            wrapModel(event, itemId.withPrefix("item/"));
            wrapModel(event, new ModelResourceLocation(itemId, "inventory"));
            PrimordialWindowShaders.updateCosmicUvs();
        });
    }
    private static void wrapModel(ModelEvent.ModifyBakingResult event, ResourceLocation modelLocation) {
        BakedModel model = event.getModels().get(modelLocation);
        if (model != null && !(model instanceof CosmicToolBakedModel)) {
            event.getModels().put(modelLocation,
                    new CosmicToolBakedModel(model, HANDLE_TEXTURE, HEAD_TEXTURE, false, HEAD_OVERLAY_COLOR));
        }
    }
}