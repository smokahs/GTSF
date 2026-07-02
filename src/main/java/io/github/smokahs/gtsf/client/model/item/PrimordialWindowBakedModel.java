package io.github.smokahs.gtsf.client.model.item;

import com.gregtechceu.gtceu.client.model.BaseBakedModel;
import com.gregtechceu.gtceu.client.util.GTQuadTransformers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.model.IQuadTransformer;
import net.minecraftforge.client.model.data.ModelData;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.smokahs.gtsf.StarFoundry;
import io.github.smokahs.gtsf.client.shader.PrimordialWindowRenderTypes;
import io.github.smokahs.gtsf.client.shader.PrimordialWindowShaders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class PrimordialWindowBakedModel extends BaseBakedModel implements PerspectiveItemModel {

    private static final ItemModelGenerator ITEM_MODEL_GENERATOR = new ItemModelGenerator();
    private static final FaceBakery FACE_BAKERY = new FaceBakery();
    private static final IQuadTransformer WINDOW_OFFSET = GTQuadTransformers.offset(0.0010F);
    private static boolean loggedRenderPath;
    private static boolean loggedShaderMissing;

    private final BakedModel wrapped;
    private final List<ResourceLocation> maskTextures;

    public PrimordialWindowBakedModel(BakedModel wrapped, List<ResourceLocation> maskTextures) {
        this.wrapped = wrapped;
        this.maskTextures = List.copyOf(maskTextures);
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable net.minecraft.world.level.block.state.BlockState state,
                                             @Nullable Direction side, @NotNull RandomSource rand,
                                             @NotNull ModelData data, @Nullable RenderType renderType) {
        return wrapped.getQuads(state, side, rand, data, renderType);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return wrapped.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return wrapped.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return wrapped.usesBlockLight();
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleIcon() {
        return wrapped.getParticleIcon();
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
        return wrapped.getParticleIcon(data);
    }

    @Override
    public @NotNull ItemOverrides getOverrides() {
        return wrapped.getOverrides();
    }

    @Override
    public @NotNull BakedModel applyTransform(@NotNull ItemDisplayContext displayContext, @NotNull PoseStack poseStack,
                                              boolean applyLeftHandTransform) {
        wrapped.applyTransform(displayContext, poseStack, applyLeftHandTransform);
        return this;
    }

    @Override
    public void renderItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                           MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Minecraft minecraft = Minecraft.getInstance();
        renderWrapped(stack, poseStack, buffer, packedLight, packedOverlay, true);

        if (!(stack.getItem() instanceof PrimordialShaderItem)) {
            return;
        }

        if (displayContext != ItemDisplayContext.GUI && isShaderPackActive()) {
            renderShaderpackFallback(stack, poseStack, buffer, packedOverlay);
            return;
        }

        if (buffer instanceof MultiBufferSource.BufferSource bufferSource) {
            bufferSource.endBatch();
        }

        if (PrimordialWindowShaders.PRIMORDIAL_WINDOW_SHADER == null || PrimordialWindowShaders.time == null ||
                PrimordialWindowShaders.yaw == null || PrimordialWindowShaders.pitch == null ||
                PrimordialWindowShaders.externalScale == null || PrimordialWindowShaders.opacity == null) {
            if (!loggedShaderMissing) {
                StarFoundry.LOGGER
                        .warn("primordial window shader uniforms were missing while rendering primordial tool");
                loggedShaderMissing = true;
            }
            return;
        }

        if (!loggedRenderPath) {
            StarFoundry.LOGGER.info("primordial window render path hit for primordial tool");
            loggedRenderPath = true;
        }

        float yaw = 0.0F;
        float pitch = 0.0F;
        float scale = displayContext == ItemDisplayContext.GUI ? 100.0F : 1.0F;
        if (displayContext != ItemDisplayContext.GUI && minecraft.player != null) {
            yaw = (float) (minecraft.player.getYRot() * (Math.PI / 180.0));
            pitch = (float) (-minecraft.player.getXRot() * (Math.PI / 180.0));
        }

        long frameTime = minecraft.level != null ? minecraft.level.getGameTime() : System.currentTimeMillis() / 50L;
        PrimordialWindowShaders.time.set(frameTime % Integer.MAX_VALUE);
        PrimordialWindowShaders.yaw.set(yaw);
        PrimordialWindowShaders.pitch.set(pitch);
        PrimordialWindowShaders.externalScale.set(scale);
        PrimordialWindowShaders.opacity.set(1.0F);

        MultiBufferSource.BufferSource immediate = buffer instanceof MultiBufferSource.BufferSource bufferSource ?
                bufferSource : minecraft.renderBuffers().bufferSource();

        List<BakedQuad> windowQuads = new ArrayList<>(bakeItem(getSprites(maskTextures)));
        windowQuads.replaceAll(quad -> {
            BakedQuad shifted = GTQuadTransformers.copy(quad);
            WINDOW_OFFSET.processInPlace(shifted);
            return shifted;
        });

        VertexConsumer consumer = immediate.getBuffer(PrimordialWindowRenderTypes.PRIMORDIAL_WINDOW);
        minecraft.getItemRenderer().renderQuadList(poseStack, consumer, windowQuads, stack, packedLight, packedOverlay);

        immediate.endBatch(PrimordialWindowRenderTypes.PRIMORDIAL_WINDOW);
    }

    private static boolean irisLookupDone;
    private static Object irisApi;
    private static java.lang.reflect.Method irisShaderPackInUse;

    private static boolean isShaderPackActive() {
        if (!irisLookupDone) {
            irisLookupDone = true;
            for (String className : new String[] { "net.irisshaders.iris.api.v0.IrisApi",
                    "net.coderbot.iris.api.v0.IrisApi" }) {
                try {
                    Class<?> api = Class.forName(className);
                    irisApi = api.getMethod("getInstance").invoke(null);
                    irisShaderPackInUse = api.getMethod("isShaderPackInUse");
                    break;
                } catch (Throwable ignored) {
                    irisApi = null;
                    irisShaderPackInUse = null;
                }
            }
        }
        if (irisApi == null || irisShaderPackInUse == null) {
            return false;
        }
        try {
            return (boolean) irisShaderPackInUse.invoke(irisApi);
        } catch (Throwable ignored) {
            return false;
        }
    }

    private void renderShaderpackFallback(ItemStack stack, PoseStack poseStack, MultiBufferSource buffer,
                                          int packedOverlay) {
        Minecraft minecraft = Minecraft.getInstance();
        long gameTime = minecraft.level != null ? minecraft.level.getGameTime() : System.currentTimeMillis() / 50L;
        float t = (float) (gameTime % 100_000L) + minecraft.getFrameTime();
        int tint = cosmicTint(t);

        List<BakedQuad> tinted = new ArrayList<>();
        for (BakedQuad quad : bakeItem(getSprites(maskTextures))) {
            BakedQuad working = GTQuadTransformers.setColor(GTQuadTransformers.copy(quad), tint, true);
            WINDOW_OFFSET.processInPlace(working);
            tinted.add(working);
        }

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucentEmissive(InventoryMenu.BLOCK_ATLAS));
        minecraft.getItemRenderer().renderQuadList(poseStack, consumer, tinted, stack,
                LightTexture.FULL_BRIGHT, packedOverlay);
    }

    private static int cosmicTint(float t) {
        float hue = 0.72F + 0.10F * Mth.sin(t * 0.04F);
        float brightness = 0.80F + 0.20F * (0.5F + 0.5F * Mth.sin(t * 0.10F));
        int rgb = Mth.hsvToRgb(hue, 0.55F, brightness);
        return 0xE6000000 | (rgb & 0xFFFFFF);
    }

    private void renderWrapped(ItemStack stack, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                               int packedOverlay, boolean fabulous) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        BakedModel resolvedModel = wrapped.getOverrides().resolve(wrapped, stack, level, minecraft.player, 0);
        ItemRenderer itemRenderer = minecraft.getItemRenderer();

        for (BakedModel renderPass : resolvedModel.getRenderPasses(stack, fabulous)) {
            for (RenderType renderType : renderPass.getRenderTypes(stack, fabulous)) {
                VertexConsumer consumer = fabulous ?
                        ItemRenderer.getFoilBufferDirect(buffer, renderType, true, stack.hasFoil()) :
                        ItemRenderer.getFoilBuffer(buffer, renderType, true, stack.hasFoil());
                itemRenderer.renderModelLists(renderPass, stack, packedLight, packedOverlay, poseStack, consumer);
            }
        }
    }

    private static List<TextureAtlasSprite> getSprites(List<ResourceLocation> maskTextures) {
        Minecraft minecraft = Minecraft.getInstance();
        List<TextureAtlasSprite> sprites = new ArrayList<>(maskTextures.size());
        for (ResourceLocation maskTexture : maskTextures) {
            sprites.add(minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(maskTexture));
        }
        return sprites;
    }

    private static List<BakedQuad> bakeItem(List<TextureAtlasSprite> sprites) {
        List<BakedQuad> quads = new ArrayList<>();
        int index = 0;
        for (TextureAtlasSprite sprite : sprites) {
            List<BlockElement> elements = ITEM_MODEL_GENERATOR.processFrames(index, "layer" + index, sprite.contents());
            for (BlockElement element : elements) {
                for (Map.Entry<Direction, BlockElementFace> entry : element.faces.entrySet()) {
                    quads.add(FACE_BAKERY.bakeQuad(element.from, element.to, entry.getValue(), sprite, entry.getKey(),
                            BlockModelRotation.X0_Y0, element.rotation, element.shade,
                            new ResourceLocation("gtsf:dynamic")));
                }
            }
            index++;
        }
        return quads;
    }
}
