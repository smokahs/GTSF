package io.github.smokahs.gtsf.client.model.item;

import com.gregtechceu.gtceu.client.model.BaseBakedModel;
import com.gregtechceu.gtceu.client.util.GTQuadTransformers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
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
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.IQuadTransformer;
import net.minecraftforge.client.model.data.ModelData;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.smokahs.gtsf.client.shader.PrimordialWindowRenderTypes;
import io.github.smokahs.gtsf.client.shader.PrimordialWindowShaders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class CosmicToolBakedModel extends BaseBakedModel implements PerspectiveItemModel {

    private static final ItemModelGenerator ITEM_MODEL_GENERATOR = new ItemModelGenerator();
    private static final FaceBakery FACE_BAKERY = new FaceBakery();
    private static final IQuadTransformer COSMIC_OFFSET = GTQuadTransformers.offset(0.0010F);
    private static final IQuadTransformer HEAD_OVERLAY_OFFSET = GTQuadTransformers.offset(0.0015F);
    private final BakedModel wrapped;
    private final ResourceLocation handleTexture;
    private final ResourceLocation headTexture;
    private final boolean solidHead;
    private final int headOverlayColor;
    private List<BakedQuad> staticQuads;

    public CosmicToolBakedModel(BakedModel wrapped, ResourceLocation handleTexture, ResourceLocation headTexture,
                                boolean solidHead, int headOverlayColor) {
        this.wrapped = wrapped;
        this.handleTexture = handleTexture;
        this.headTexture = headTexture;
        this.solidHead = solidHead;
        this.headOverlayColor = headOverlayColor;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                             @NotNull RandomSource rand, @NotNull ModelData data,
                                             @Nullable RenderType renderType) {
        if (side != null) {
            return List.of();
        }
        return getStaticQuads();
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
    public @NotNull BakedModel applyTransform(@NotNull ItemDisplayContext displayContext,
                                              @NotNull PoseStack poseStack, boolean applyLeftHandTransform) {
        wrapped.applyTransform(displayContext, poseStack, applyLeftHandTransform);
        return this;
    }

    @Override
    public void renderItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                           MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Minecraft mc = Minecraft.getInstance();
        TextureAtlasSprite handleSprite = mc.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(handleTexture);
        TextureAtlasSprite headSprite = mc.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(headTexture);

        renderSprite(handleSprite, stack, poseStack, buffer, packedLight, packedOverlay,
                Sheets.cutoutBlockSheet(), null, null);
        if (buffer instanceof MultiBufferSource.BufferSource bs) {
            bs.endBatch();
        }
        if (solidHead) {

            renderHeadBacking(headSprite, stack, poseStack, buffer, packedOverlay);
            if (buffer instanceof MultiBufferSource.BufferSource bs) {
                bs.endBatch();
            }
        }
        renderCosmic(displayContext, poseStack, buffer, stack, packedLight, packedOverlay, headSprite);
        if (buffer instanceof MultiBufferSource.BufferSource bs) {
            bs.endBatch();
        }
        renderSprite(headSprite, stack, poseStack, buffer, packedLight, packedOverlay,
                Sheets.translucentItemSheet(), headOverlayColor, HEAD_OVERLAY_OFFSET);
    }

    private static void renderSprite(TextureAtlasSprite sprite, ItemStack stack, PoseStack poseStack,
                                     MultiBufferSource buffer, int packedLight, int packedOverlay,
                                     RenderType renderType, @Nullable Integer argbColor,
                                     @Nullable IQuadTransformer transformer) {
        List<BakedQuad> quads = bakeItem(sprite, false);
        if (argbColor != null || transformer != null) {
            List<BakedQuad> transformed = new ArrayList<>(quads.size());
            for (BakedQuad quad : quads) {
                BakedQuad working = quad;
                if (argbColor != null) {
                    working = GTQuadTransformers.setColor(working, argbColor, true);
                }
                if (transformer != null) {
                    working = GTQuadTransformers.copy(working);
                    transformer.processInPlace(working);
                }
                transformed.add(working);
            }
            quads = transformed;
        }
        VertexConsumer consumer = ItemRenderer.getFoilBufferDirect(buffer, renderType, true, stack.hasFoil());
        Minecraft.getInstance().getItemRenderer().renderQuadList(poseStack, consumer, quads, stack,
                packedLight, packedOverlay);
    }

    private List<BakedQuad> getStaticQuads() {
        if (staticQuads == null) {
            Minecraft mc = Minecraft.getInstance();
            TextureAtlasSprite handleSprite = mc.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(handleTexture);
            TextureAtlasSprite headSprite = mc.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(headTexture);
            List<BakedQuad> quads = new ArrayList<>();

            quads.addAll(bakeItem(handleSprite, false));
            quads.addAll(bakeItem(headSprite, false));
            staticQuads = List.copyOf(quads);
        }
        return staticQuads;
    }

    private void renderCosmic(ItemDisplayContext displayContext, PoseStack poseStack,
                              MultiBufferSource buffer, ItemStack stack, int packedLight, int packedOverlay,
                              TextureAtlasSprite headSprite) {
        if (PrimordialWindowShaders.PRIMORDIAL_WINDOW_SHADER == null ||
                PrimordialWindowShaders.time == null || PrimordialWindowShaders.yaw == null ||
                PrimordialWindowShaders.pitch == null || PrimordialWindowShaders.externalScale == null ||
                PrimordialWindowShaders.opacity == null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        float yaw = 0.0F;
        float pitch = 0.0F;
        float scale = displayContext == ItemDisplayContext.GUI ? 100.0F : 1.0F;
        if (displayContext != ItemDisplayContext.GUI && mc.player != null) {
            yaw = (float) (mc.player.getYRot() * (Math.PI / 180.0));
            pitch = (float) (-mc.player.getXRot() * (Math.PI / 180.0));
        }

        long frameTime = mc.level != null ? mc.level.getGameTime() : System.currentTimeMillis() / 50L;
        PrimordialWindowShaders.time.set(frameTime % Integer.MAX_VALUE);
        PrimordialWindowShaders.yaw.set(yaw);
        PrimordialWindowShaders.pitch.set(pitch);
        PrimordialWindowShaders.externalScale.set(scale);
        PrimordialWindowShaders.opacity.set(1.0F);
        VertexConsumer consumer = buffer.getBuffer(PrimordialWindowRenderTypes.PRIMORDIAL_WINDOW);
        List<BakedQuad> cosmicQuads = new ArrayList<>(bakeItem(headSprite));
        cosmicQuads.replaceAll(quad -> {
            BakedQuad shifted = GTQuadTransformers.copy(quad);
            COSMIC_OFFSET.processInPlace(shifted);
            return shifted;
        });
        mc.getItemRenderer().renderQuadList(poseStack, consumer, cosmicQuads, stack,
                packedLight, packedOverlay);
        if (buffer instanceof MultiBufferSource.BufferSource bufferSource) {
            bufferSource.endBatch(PrimordialWindowRenderTypes.PRIMORDIAL_WINDOW);
        }
    }

    private static void renderHeadBacking(TextureAtlasSprite headSprite, ItemStack stack, PoseStack poseStack,
                                          MultiBufferSource buffer, int packedOverlay) {
        List<BakedQuad> quads = bakeItem(headSprite, false);
        VertexConsumer consumer = ItemRenderer.getFoilBufferDirect(buffer, Sheets.cutoutBlockSheet(), true,
                stack.hasFoil());
        Minecraft.getInstance().getItemRenderer().renderQuadList(poseStack, consumer, quads, stack,
                LightTexture.FULL_BRIGHT, packedOverlay);
    }

    private static List<BakedQuad> bakeItem(TextureAtlasSprite sprite) {
        return bakeItem(sprite, true);
    }

    private static List<BakedQuad> bakeItem(TextureAtlasSprite sprite, boolean shade) {
        List<BakedQuad> quads = new ArrayList<>();
        List<BlockElement> elements = ITEM_MODEL_GENERATOR.processFrames(-1, "layer0", sprite.contents());
        for (BlockElement element : elements) {
            for (Map.Entry<Direction, BlockElementFace> entry : element.faces.entrySet()) {
                quads.add(FACE_BAKERY.bakeQuad(element.from, element.to, entry.getValue(), sprite,
                        entry.getKey(), BlockModelRotation.X0_Y0, element.rotation, element.shade && shade,
                        new ResourceLocation("gtsf:dynamic")));
            }
        }
        return quads;
    }
}
