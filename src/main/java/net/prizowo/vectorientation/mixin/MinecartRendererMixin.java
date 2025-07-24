package net.prizowo.vectorientation.mixin;

import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.tags.BlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.prizowo.vectorientation.main.Vectorientation;

@Mixin(MinecartRenderer.class)
public class MinecartRendererMixin<T extends AbstractMinecart> {
    @Inject(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/vehicle/AbstractMinecart;getDisplayBlockState()Lnet/minecraft/world/level/block/state/BlockState;"
            ),
            method = "render(Lnet/minecraft/world/entity/vehicle/AbstractMinecart;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"
    )
    public void addRotation(T minecart, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, CallbackInfo info) {
        if(!Vectorientation.MINECARTS) return;
        if(isOnRail(minecart)) return;
        Vec3 velD = minecart.getDeltaMovement();
        Vector3f vel = new Vector3f((float) velD.x(), (float) velD.y(), (float) velD.z());
        if(minecart.onGround() || vel.length() < .03f) return;
        float y = vel.y();
        if(Math.abs(y) > 0.01f) {
            y -= .04D * partialTicks;
            y *= .98D;
        }
        vel.y = y;
        boolean xMajor = Math.abs(vel.x()) > Math.abs(vel.z());
        float angle = (float) (Math.asin(vel.normalize().y));
        Vector3f axis = new Vector3f(0,1,0).cross(xMajor ? new Vector3f(0,0,1) : new Vector3f(1,0,0));
        Quaternionf rot = new Quaternionf();
        if(axis.length() > .01f){
            axis.normalize();
            rot = new Quaternionf(new AxisAngle4f(-angle, axis));
        }
        boolean translate = minecart.isVehicle();
        if (translate) poseStack.translate(0.0D, 0.5D, 0.0D);
        poseStack.mulPose(rot);
        if (translate) poseStack.translate(-0.0D, -0.5D, -0.0D);
    }

    private boolean isOnRail(T minecart){
        int posX = Mth.floor(minecart.getX());
        int posY = Mth.floor(minecart.getY());
        int posZ = Mth.floor(minecart.getZ());
        if (minecart.level().getBlockState(new BlockPos(posX, posY - 1, posZ)).is(BlockTags.RAILS)) {
            --posZ;
        }
        BlockState blockState = minecart.level().getBlockState(new BlockPos(posX, posY, posZ));
        if (BaseRailBlock.isRail(blockState)) {
            return true;
        }
        return false;
    }
}
