package net.prizowo.vectorientation.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.FallingBlockRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.phys.Vec3;
import net.prizowo.vectorientation.main.Vectorientation;

@Mixin(value = FallingBlockRenderer.class, priority = 1100)
public class FallingBlockRendererMixin {
	@Inject(
			at = @At(
					value = "INVOKE",
					target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(DDD)V",
					ordinal = 0
					),
			method = "render(Lnet/minecraft/world/entity/item/FallingBlockEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"
			)
	public void addRotation(FallingBlockEntity fallingBlockEntity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, CallbackInfo ci) {
		Vec3 velD = fallingBlockEntity.getDeltaMovement();
		Vector3f vel = new Vector3f((float) velD.x(), (float) velD.y(), (float) velD.z());
		float y = vel.y();
		boolean moving = !fallingBlockEntity.onGround();
		if(moving) {
			y -= 0.04D * 0.04D;
			y *= 0.98D;
		}
		vel.y = y;
		boolean blacklisted = Vectorientation.BLACKLIST.contains(BuiltInRegistries.BLOCK.getKey(fallingBlockEntity.getBlockState().getBlock()));
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
		poseStack.translate(0.0D, 0.5D, 0.0D);
		poseStack.scale(1/speed, speed, 1/speed);
		poseStack.mulPose(rot);
		poseStack.translate(0.0D, -0.5D, 0.0D);
	}
}
