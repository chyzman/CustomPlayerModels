package com.tom.cpm.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.block.BlockState;
import net.minecraft.block.SkullBlock;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.SkullBlockEntityModel;
import net.minecraft.client.render.block.entity.SkullBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import com.mojang.authlib.GameProfile;

import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.client.RefHolder;

@Mixin(SkullBlockEntityRenderer.class)
public abstract class SkullBlockEntityRendererMixin {

	@Shadow private @Final Map<SkullBlock.SkullType, SkullBlockEntityModel> MODELS;

	@Inject(at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/render/block/entity/SkullBlockEntityRenderer;"
					+ "getRenderLayer(Lnet/minecraft/block/SkullBlock$SkullType;Lcom/mojang/authlib/GameProfile;)"
					+ "Lnet/minecraft/client/render/RenderLayer;"
			),
			method = "render(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformation$Mode;"
					+ "Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V",
					locals = LocalCapture.CAPTURE_FAILHARD)
	public void onRender(SkullBlockEntity skullBlockEntity, float f, MatrixStack matrixStack, VertexConsumerProvider buffer, int i, int arg5, CallbackInfo ci, float g, BlockState blockState, boolean bl, Direction direction, float h, SkullBlock.SkullType skullType, SkullBlockEntityModel model) {
		RefHolder.CPM_MODELS = MODELS;
		GameProfile gameProfile = skullBlockEntity.getOwner();
		if(skullType == SkullBlock.Type.PLAYER) {
			CustomPlayerModelsClient.INSTANCE.renderSkull(model, gameProfile, buffer);
		}
	}

	@Redirect(at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/render/RenderLayer;getEntityTranslucent("
					+ "Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;"
			),
			method = "getRenderLayer(Lnet/minecraft/block/SkullBlock$SkullType;Lcom/mojang/authlib/GameProfile;)"
					+ "Lnet/minecraft/client/render/RenderLayer;")
	private static RenderLayer onGetRenderType(Identifier resLoc, SkullBlock.SkullType skullType, GameProfile gameProfileIn) {
		SkullBlockEntityModel model = RefHolder.CPM_MODELS.get(skullType);
		RefHolder.CPM_MODELS = null;
		CallbackInfoReturnable<Identifier> cbi = new CallbackInfoReturnable<>(null, true, resLoc);
		CustomPlayerModelsClient.mc.getPlayerRenderManager().bindSkin(model, cbi);
		return RenderLayer.getEntityTranslucent(cbi.getReturnValue());
	}
}