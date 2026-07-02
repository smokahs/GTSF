package io.github.smokahs.gtsf.mixin.client;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.smokahs.gtsf.client.model.item.PerspectiveItemModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    @Inject(
            method = "render",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", ordinal = 0),
            cancellable = true)
    private void gtsf$renderPerspectiveItem(ItemStack stack, ItemDisplayContext displayContext, boolean leftHand,
                                            PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                            int packedOverlay, BakedModel model, CallbackInfo ci) {
        if (!(model instanceof PerspectiveItemModel perspectiveModel)) {
            return;
        }

        poseStack.pushPose();
        BakedModel transformedModel = perspectiveModel.applyTransform(displayContext, poseStack, leftHand);
        if (transformedModel instanceof PerspectiveItemModel transformedPerspectiveModel) {
            poseStack.translate(-0.5D, -0.5D, -0.5D);
            transformedPerspectiveModel.renderItem(stack, displayContext, poseStack, buffer, packedLight,
                    packedOverlay);
        }
        poseStack.popPose();
        ci.cancel();
    }
}
