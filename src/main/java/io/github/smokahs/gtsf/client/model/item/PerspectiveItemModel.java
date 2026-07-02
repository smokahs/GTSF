package io.github.smokahs.gtsf.client.model.item;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import com.mojang.blaze3d.vertex.PoseStack;

public interface PerspectiveItemModel extends BakedModel {

    void renderItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                    MultiBufferSource buffer, int packedLight, int packedOverlay);

    @Override
    default boolean isCustomRenderer() {
        return true;
    }
}
