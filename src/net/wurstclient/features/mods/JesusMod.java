/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.wurstclient.features.mods;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.wurstclient.WurstClient;
import net.wurstclient.events.listeners.UpdateListener;
import net.wurstclient.features.mods.Mod.Bypasses;

@Mod.Info(
	description = "Allows you to walk on water.\n"
		+ "The real Jesus used this hack ~2000 years ago.\n"
		+ "Bypasses NoCheat+ if YesCheat+ is enabled.",
	name = "Jesus",
	help = "Mods/Jesus")
@Bypasses(ghostMode = false)
public class JesusMod extends Mod implements UpdateListener
{
	private int ticksOutOfWater = 10;
	public int time = 0;
	public final int delay = 4;
	
	@Override
	public void onEnable()
	{
		WurstClient.INSTANCE.events.add(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		if(!mc.gameSettings.keyBindSneak.pressed)
			if(mc.thePlayer.isInWater())
			{
				mc.thePlayer.motionY = 0.11;
				ticksOutOfWater = 0;
			}else
			{
				if(ticksOutOfWater == 0)
					mc.thePlayer.motionY = 0.30;
				else if(ticksOutOfWater == 1)
					mc.thePlayer.motionY = 0;
				
				ticksOutOfWater++;
			}
	}
	
	@Override
	public void onDisable()
	{
		WurstClient.INSTANCE.events.remove(UpdateListener.class, this);
	}
	
	@SuppressWarnings("deprecation")
	public boolean isOverWater()
	{
		final EntityPlayerSP thePlayer = mc.thePlayer;
		
		boolean isOnWater = false;
		boolean isOnSolid = false;
		
		for(final Object o : mc.theWorld.getCollisionBoxes(thePlayer,
			thePlayer.getEntityBoundingBox().offset(0, -1.0D, 0).expand(-0.001,
				0, -0.001)))
		{
			final AxisAlignedBB bbox = (AxisAlignedBB)o;
			final BlockPos blockPos =
				new BlockPos(bbox.maxX - (bbox.maxX - bbox.minX) / 2.0,
					bbox.maxY - (bbox.maxY - bbox.minY) / 2.0,
					bbox.maxZ - (bbox.maxZ - bbox.minZ) / 2.0);
			final Block block = mc.theWorld.getBlockState(blockPos).getBlock();
			if(block.getMaterial(null) == Material.WATER
				|| block.getMaterial(null) == Material.LAVA)
				isOnWater = true;
			else if(block.getMaterial(null) != Material.AIR)
				isOnSolid = true;
		}
		
		return isOnWater && !isOnSolid;
	}
	
	public boolean shouldBeSolid()
	{
		return isActive() && !(mc.thePlayer == null)
			&& !(mc.thePlayer.fallDistance > 3)
			&& !mc.gameSettings.keyBindSneak.pressed
			&& !mc.thePlayer.isInWater();
	}
}
