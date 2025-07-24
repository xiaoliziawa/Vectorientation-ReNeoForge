package net.prizowo.vectorientation.mixin;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.TntRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.phys.Vec3;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.prizowo.vectorientation.main.Vectorientation;

@Mixin(TntRenderer.class)
public class TntEntityRendererMixin {
    @Inject(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/TntMinecartRenderer;renderWhiteSolidBlock(Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;Lnet/minecraft/world/level/block/state/BlockState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IZ)V"
            ),
            method = "render(Lnet/minecraft/world/entity/item/PrimedTnt;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"
    )
    public void addRotation(PrimedTnt primedTnt, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, CallbackInfo ci) {
        if(!Vectorientation.TNT) return;
        Vec3 velD = primedTnt.getDeltaMovement();
        Vector3f vel = new Vector3f((float) velD.x(), (float) velD.y(), (float) velD.z());
        float y = vel.y();
        boolean moving = !primedTnt.onGround();
        if(moving) {
            y -= .04D * 0.04D;
            y *= .98D;
        }
        vel.y = y;
        boolean blacklisted = Vectorientation.BLACKLIST.contains(Vectorientation.TNT_ID);
        float speed = (!blacklisted && moving && Vectorientation.SQUETCH) ?
                (float) (Vectorientation.MIN_WARP + Vectorientation.WARP_FACTOR * vel.length())
                : 1.0f;
        float angle = (float) Math.acos(vel.normalize().y);
        Vector3f axis = new Vector3f(-1 * vel.z(), 0, vel.x());
        Quaternionf rot = new Quaternionf();
        if(axis.length() > .01f){
            axis.normalize();
            rot = new Quaternionf(new AxisAngle4f(-angle, axis));
        }
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(rot);
        poseStack.scale(1/speed, speed, 1/speed);
        poseStack.translate(-0.5D, -0.5D, -0.5D);
    }
}

