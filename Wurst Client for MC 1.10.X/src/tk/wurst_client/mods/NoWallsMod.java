/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.mods;

import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import tk.wurst_client.events.PacketOutputEvent;
import tk.wurst_client.events.listeners.PacketOutputListener;
import tk.wurst_client.mods.Mod.Bypasses;
import tk.wurst_client.mods.Mod.Category;
import tk.wurst_client.mods.Mod.Info;

@Info(category = Category.MOVEMENT,
	description = "Allows you walk through walls.\n"
		+ "Not all servers support this.",
	name = "NoWalls",
	help = "Mods/NoWalls")
@Bypasses(ghostMode = false, latestNCP = false, olderNCP = false)
public class NoWallsMod extends Mod implements PacketOutputListener
{
	
	@Override
	public void onEnable()
	{
		wurst.events.add(PacketOutputListener.class, this);
		
		if(!wurst.mods.noClipMod.isEnabled())
			wurst.mods.noClipMod.setEnabled(true);
	}
	
	@Override
	public void onSentPacket(PacketOutputEvent event)
	{
		Packet packet = event.getPacket();
		if(packet instanceof CPacketPlayer)
			event.cancel();
	}
	
	@Override
	public void onDisable()
	{
		wurst.events.remove(PacketOutputListener.class, this);
		
		mc.thePlayer.connection.sendPacket(new CPacketPlayer.PositionRotation(
			mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY,
			mc.thePlayer.posZ, mc.thePlayer.cameraYaw, mc.thePlayer.cameraPitch,
			mc.thePlayer.onGround));
		
		wurst.mods.noClipMod.setEnabled(false);
	}
}
