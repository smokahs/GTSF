package io.github.smokahs.gtsf.client.shader;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.lwjgl.opengl.GL13;

@OnlyIn(Dist.CLIENT)
public final class PrimordialWindowRenderTypes extends RenderType {

    private static final RenderStateShard.TextureStateShard BLOCK_ATLAS_TEXTURE = new RenderStateShard.TextureStateShard(
            InventoryMenu.BLOCK_ATLAS, false, false) {

        @Override
        public void setupRenderState() {
            super.setupRenderState();
            RenderSystem.activeTexture(GL13.GL_TEXTURE0);
            TextureAtlas atlas = Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS);
            RenderSystem.bindTexture(atlas.getId());
            PrimordialWindowShaders.bindBackgroundTexture();
            PrimordialWindowShaders.updateCosmicUvs();
        }
    };

    public static final RenderType PRIMORDIAL_WINDOW = RenderType.create(
            "gtsf_primordial_window",
            DefaultVertexFormat.BLOCK,
            VertexFormat.Mode.QUADS,
            2_097_152,
            true,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(
                            () -> PrimordialWindowShaders.PRIMORDIAL_WINDOW_SHADER))
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setTextureState(BLOCK_ATLAS_TEXTURE)
                    .createCompositeState(true));

    private PrimordialWindowRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize,
                                        boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState,
                                        Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }
}
