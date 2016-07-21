/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.mods;

import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.world.GameType;

import tk.wurst_client.events.PacketOutputEvent;
import tk.wurst_client.events.listeners.UpdateListener;
import tk.wurst_client.events.listeners.PacketOutputListener;

import tk.wurst_client.mods.Mod.Bypasses;
import tk.wurst_client.mods.Mod.Category;

import tk.wurst_client.mods.Mod.Info;

@Info(category = Category.MOVEMENT,
	description = "Allows you walk through walls.",
	name = "NoWalls",
	help = "Mods/NoWalls")
@Bypasses
public class NoWallsMod extends Mod implements PacketOutputListener, UpdateListener
{	
	
	@Override
	public void onEnable()
	{
		if(wurst.mods.noClipMod.isEnabled())
			wurst.mods.noClipMod.setEnabled(false);
		wurst.events.add(PacketOutputListener.class, this);		
		wurst.events.add(UpdateListener.class, this);
	}
	
	@Override
	public void onSentPacket(PacketOutputEvent event)
	{
		Packet packet = event.getPacket();
		if(packet instanceof CPacketPlayer)
		{
			event.cancel();
		}
	}
	
	@Override
	public void onUpdate()
	{
		mc.thePlayer.noClip = true;
		mc.thePlayer.fallDistance = 0;
		mc.thePlayer.onGround = false;
		
		mc.thePlayer.capabilities.isFlying = false;
		mc.thePlayer.motionX = 0;
		mc.thePlayer.motionY = 0;
		mc.thePlayer.motionZ = 0;
		
		float speed = 0.2F;
		mc.thePlayer.jumpMovementFactor = speed;
		if(mc.gameSettings.keyBindJump.pressed)
			mc.thePlayer.motionY += speed;
		if(mc.gameSettings.keyBindSneak.pressed)
			mc.thePlayer.motionY -= speed;
	}

	
	@Override
	public void onDisable()
	{
		wurst.events.remove(PacketOutputListener.class, this);
		wurst.events.remove(UpdateListener.class, this);
		
		mc.thePlayer.connection.sendPacket(new CPacketPlayer.PositionRotation(
			mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY,
			mc.thePlayer.posZ, mc.thePlayer.cameraYaw, mc.thePlayer.cameraPitch,
			mc.thePlayer.onGround));
			
		mc.thePlayer.noClip = false;
	}
	
}
