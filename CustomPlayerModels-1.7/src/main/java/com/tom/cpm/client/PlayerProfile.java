package com.tom.cpm.client;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;

import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.animation.VanillaPose;
import com.tom.cpm.shared.config.Player;

import cpw.mods.fml.common.ObfuscationReflectionHelper;

public class PlayerProfile extends Player {
	private final GameProfile profile;
	private VanillaPose pose;

	public static PlayerProfile create(Object object) {
		return new PlayerProfile((GameProfile) object);
	}

	private PlayerProfile(GameProfile profile) {
		this.profile = profile;
	}

	@SuppressWarnings("unchecked")
	@Override
	public BufferedImage getSkin() {
		if(MinecraftObjectHolder.DEBUGGING) {
			try (FileInputStream fin = new FileInputStream("skin_test.png")){
				return ImageIO.read(fin);
			} catch (IOException e) {
			}
		}
		Minecraft minecraft = Minecraft.getMinecraft();
		Map<Type, MinecraftProfileTexture> map = minecraft.func_152342_ad().func_152788_a(profile);

		if (map.containsKey(Type.SKIN)) {
			ITextureObject skin = minecraft.getTextureManager().getTexture(minecraft.func_152342_ad().func_152792_a(map.get(Type.SKIN), Type.SKIN));
			if (skin instanceof ThreadDownloadImageData) {
				ThreadDownloadImageData imagedata = (ThreadDownloadImageData) skin;
				return ObfuscationReflectionHelper.getPrivateValue(ThreadDownloadImageData.class, imagedata, "field_110560_d", "bufferedImage");
			} else {
				return null;
			}
		}
		return null;
	}

	@Override
	public int getSkinType() {
		return 1;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((profile == null) ? 0 : profile.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		PlayerProfile other = (PlayerProfile) obj;
		if (profile == null) {
			if (other.profile != null) return false;
		} else if (!profile.equals(other.profile)) return false;
		return true;
	}

	public ModelBiped getModel() {
		return ((RenderPlayer) RenderManager.instance.entityRenderMap.get(EntityPlayer.class)).modelBipedMain;
	}

	@Override
	public void loadSkin(Runnable onLoaded) {
		Minecraft.getMinecraft().func_152342_ad().func_152790_a(profile, new SkinManager.SkinAvailableCallback() {

			@Override
			public void func_152121_a(Type typeIn, ResourceLocation p_152121_2_) {
				switch (typeIn) {
				case SKIN:
					if(onLoaded != null)onLoaded.run();
					break;
				default:
					break;
				}
			}
		}, true);
		if(MinecraftObjectHolder.DEBUGGING && onLoaded != null)onLoaded.run();
	}

	@Override
	public UUID getUUID() {
		return profile.getId();
	}

	@Override
	public VanillaPose getPose() {
		return pose;
	}

	public void updateFromPlayer(EntityPlayer player) {
		if(player.isPlayerSleeping())pose = VanillaPose.SLEEPING;
		else if(player.isDead)pose = VanillaPose.DYING;
		else if(player.fallDistance > 4)pose = VanillaPose.FALLING;
		else if(player.isRiding())pose = VanillaPose.RIDING;
		else if(player.isSprinting())pose = VanillaPose.RUNNING;
		else if(player.isSneaking())pose = VanillaPose.SNEAKING;
		else if(player.distanceWalkedModified - player.prevDistanceWalkedModified > 0)pose = VanillaPose.WALKING;
		else pose = VanillaPose.STANDING;
	}

	@Override
	public int getEncodedGestureId() {
		return -1;
	}
}