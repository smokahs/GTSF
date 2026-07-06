package io.github.smokahs.gtsf.client.model.item;

import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.client.util.ModelUtils;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import io.github.smokahs.gtsf.StarFoundry;
import io.github.smokahs.gtsf.client.shader.PrimordialWindowShaders;
import io.github.smokahs.gtsf.common.data.GTSFPrimordialTools;
import io.github.smokahs.gtsf.common.item.tool.PrimordialToolItem;
import io.github.smokahs.gtsf.config.GTSFConfig;

import java.util.List;

public final class PrimordialToolItemClient {

    private static final int HEAD_OVERLAY_COLOR = 0x99000000;

    private PrimordialToolItemClient() {}

    public static void init(PrimordialToolItem item, GTToolType toolType) {
        ResourceLocation maskTexture = toolType.modelLocation;

        ModelUtils.registerBakeEventListener(false, event -> {
            if (!GTSFConfig.get().tools.general.primordialShader) {
                return;
            }
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);

            if (GTSFPrimordialTools.isAvaritiaStyled(itemId.getPath())) {
                ResourceLocation handle = StarFoundry.id("item/" + itemId.getPath() + "_handle");
                ResourceLocation head = StarFoundry.id("item/" + itemId.getPath() + "_head");
                wrapCosmic(event, itemId.withPrefix("item/"), handle, head);
                wrapCosmic(event, new ModelResourceLocation(itemId, "inventory"), handle, head);
            } else {
                List<ResourceLocation> masks = List.of(maskTexture);
                wrapWindow(event, itemId.withPrefix("item/"), masks);
                wrapWindow(event, new ModelResourceLocation(itemId, "inventory"), masks);
                wrapWindow(event, toolType.modelLocation, masks);
            }

            PrimordialWindowShaders.updateCosmicUvs();
        });
    }

    private static void wrapWindow(net.minecraftforge.client.event.ModelEvent.ModifyBakingResult event,
                                   ResourceLocation modelLocation, List<ResourceLocation> masks) {
        BakedModel model = event.getModels().get(modelLocation);
        if (model == null || model instanceof PrimordialWindowBakedModel) {
            return;
        }

        event.getModels().put(modelLocation, new PrimordialWindowBakedModel(model, masks));
    }

    private static void wrapCosmic(net.minecraftforge.client.event.ModelEvent.ModifyBakingResult event,
                                   ResourceLocation modelLocation, ResourceLocation handle, ResourceLocation head) {
        BakedModel model = event.getModels().get(modelLocation);
        if (model == null || model instanceof CosmicToolBakedModel) {
            return;
        }

        event.getModels().put(modelLocation, new CosmicToolBakedModel(model, handle, head, true, HEAD_OVERLAY_COLOR));
    }
}
