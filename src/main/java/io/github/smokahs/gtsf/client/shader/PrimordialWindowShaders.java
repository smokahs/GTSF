package io.github.smokahs.gtsf.client.shader;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.event.RegisterShadersEvent;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import io.github.smokahs.gtsf.StarFoundry;

public final class PrimordialWindowShaders {

    public static final int SYMBOL_COUNT = 11;
    public static final ResourceLocation BACKGROUND_TEXTURE = StarFoundry
            .id("textures/misc/primordial_window/background.png");

    public static final ResourceLocation[] COSMIC_SPRITE_LOCATIONS = new ResourceLocation[SYMBOL_COUNT];
    static {
        for (int i = 0; i < SYMBOL_COUNT - 1; i++) {
            COSMIC_SPRITE_LOCATIONS[i] = StarFoundry.id("misc/primordial_window/cosmic" + i);
        }
        COSMIC_SPRITE_LOCATIONS[SYMBOL_COUNT - 1] = StarFoundry.id("misc/primordial_window/cosmic_black_hole");
    }

    public static ShaderInstance PRIMORDIAL_WINDOW_SHADER;
    public static Uniform time;
    public static Uniform yaw;
    public static Uniform pitch;
    public static Uniform externalScale;
    public static Uniform opacity;
    public static Uniform cosmicuvs;

    private PrimordialWindowShaders() {}

    public static void bindBackgroundTexture() {
        if (PRIMORDIAL_WINDOW_SHADER == null) {
            return;
        }
        AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(BACKGROUND_TEXTURE);
        PRIMORDIAL_WINDOW_SHADER.setSampler("Sampler1", texture.getId());
    }

    public static void updateCosmicUvs() {
        if (PRIMORDIAL_WINDOW_SHADER == null || cosmicuvs == null) {
            return;
        }
        var atlasFn = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS);
        float[] uvData = new float[SYMBOL_COUNT * 4];
        for (int i = 0; i < SYMBOL_COUNT; i++) {
            TextureAtlasSprite sprite = atlasFn.apply(COSMIC_SPRITE_LOCATIONS[i]);
            uvData[i * 4 + 0] = sprite.getU0();
            uvData[i * 4 + 1] = sprite.getV0();
            uvData[i * 4 + 2] = sprite.getU1();
            uvData[i * 4 + 3] = sprite.getV1();
        }
        cosmicuvs.set(uvData);
    }

    public static void updateForCurrentFrame() {
        if (PRIMORDIAL_WINDOW_SHADER == null || time == null || yaw == null || pitch == null ||
                externalScale == null || opacity == null) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        float yawValue = 0.0F;
        float pitchValue = 0.0F;
        if (minecraft.player != null) {
            yawValue = (float) (minecraft.player.getYRot() * (Math.PI / 180.0));
            pitchValue = (float) (-minecraft.player.getXRot() * (Math.PI / 180.0));
        }

        long frameTime = minecraft.level != null ? minecraft.level.getGameTime() : System.currentTimeMillis() / 50L;
        float scale = minecraft.screen != null ? 100.0F : 1.0F;

        time.set(frameTime % Integer.MAX_VALUE);
        yaw.set(yawValue);
        pitch.set(pitchValue);
        externalScale.set(scale);
        opacity.set(1.0F);
    }

    public static void onRegisterShaders(RegisterShadersEvent event) {
        try {
            event.registerShader(new ShaderInstance(event.getResourceProvider(), StarFoundry.id("primordial_window"),
                    DefaultVertexFormat.BLOCK), shader -> {
                        PRIMORDIAL_WINDOW_SHADER = shader;
                        time = shader.getUniform("time");
                        yaw = shader.getUniform("yaw");
                        pitch = shader.getUniform("pitch");
                        externalScale = shader.getUniform("externalScale");
                        opacity = shader.getUniform("opacity");
                        cosmicuvs = shader.getUniform("cosmicuvs");
                    });
        } catch (Exception exception) {
            StarFoundry.LOGGER.error("Failed to register primordial window shader", exception);
        }
    }
}
