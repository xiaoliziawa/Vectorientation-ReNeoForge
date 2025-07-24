package net.prizowo.vectorientation.mixin;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.PistonHeadRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import net.prizowo.vectorientation.main.Vectorientation;

@Mixin(PistonHeadRenderer.class)
public class PistonHeadRendererMixin {
    
    @Inject(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/blockentity/PistonHeadRenderer;renderBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;ZI)V"
            ),
            method = "render(Lnet/minecraft/world/level/block/piston/PistonMovingBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V"
    )
    public void addPistonRotation(PistonMovingBlockEntity pistonEntity, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, CallbackInfo ci) {
        if (!Vectorientation.PISTONS || !Vectorientation.SQUETCH) {
            return;
        }

        BlockState movedState = pistonEntity.getMovedState();
        boolean blacklisted = Vectorientation.BLACKLIST.contains(BuiltInRegistries.BLOCK.getKey(movedState.getBlock()));
        if (blacklisted) {
            return;
        }
        float progress = pistonEntity.getProgress(partialTicks);
        if (progress <= 0.0f || progress >= 1.0f) {
            return;
        }
        Vec3 direction = Vec3.atLowerCornerOf(pistonEntity.getMovementDirection().getNormal());
        float movementSpeed;
        movementSpeed = 4.0f * progress * (1.0f - progress);
        float stretchFactor = 1.0f + movementSpeed * 1.2f;
        float scaleX = 1.0f;
        float scaleY = 1.0f;
        float scaleZ = 1.0f;
        float compressFactor = 1.0f / (float)Math.sqrt(stretchFactor);
        if (Math.abs(direction.x()) > 0.5) {
            scaleX = stretchFactor;
            scaleY = compressFactor;
            scaleZ = compressFactor;
        } else if (Math.abs(direction.y()) > 0.5) {
            scaleX = compressFactor;
            scaleY = stretchFactor;
            scaleZ = compressFactor;
        } else if (Math.abs(direction.z()) > 0.5) {
            scaleX = compressFactor;
            scaleY = compressFactor;
            scaleZ = stretchFactor;
        }
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.scale(scaleX, scaleY, scaleZ);
        poseStack.translate(-0.5D, -0.5D, -0.5D);
    }
}
