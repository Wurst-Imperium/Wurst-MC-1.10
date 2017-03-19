/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.wurstclient.features.mods;

import java.util.ArrayList;

import net.minecraft.block.BlockFenceGate;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.BlockPos;
import net.wurstclient.events.listeners.UpdateListener;
import net.wurstclient.features.mods.Mod.Bypasses;
import net.wurstclient.features.mods.Mod.Info;
import net.wurstclient.features.special_features.YesCheatSpf.BypassLevel;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;

@Info(description = "Allows you to step up full blocks.",
	name = "Step",
	help = "Mods/Step")
@Bypasses
public class StepMod extends Mod implements UpdateListener
{
	public SliderSetting height =
		new SliderSetting("Height", 1, 1, 100, 1, ValueDisplay.INTEGER);
	
	@Override
	public void initSettings()
	{
		settings.add(height);
	}
	
	@Override
	public void onEnable()
	{
		wurst.events.add(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		if(wurst.special.yesCheatSpf.getBypassLevel()
			.ordinal() >= BypassLevel.ANTICHEAT.ordinal())
		{
			mc.thePlayer.stepHeight = 0.5F;
			if(mc.thePlayer.onGround && !mc.thePlayer.isOnLadder()
				&& (mc.thePlayer.movementInput.moveForward != 0.0F
					|| mc.thePlayer.movementInput.moveStrafe != 0.0F)
				&& canStep() && !mc.thePlayer.movementInput.jump
				&& mc.thePlayer.isCollidedHorizontally)
			{
				mc.getConnection()
					.sendPacket(new CPacketPlayer.Position(mc.thePlayer.posX,
						mc.thePlayer.posY + 0.42D, mc.thePlayer.posZ,
						mc.thePlayer.onGround));
				mc.getConnection()
					.sendPacket(new CPacketPlayer.Position(mc.thePlayer.posX,
						mc.thePlayer.posY + 0.753D, mc.thePlayer.posZ,
						mc.thePlayer.onGround));
				mc.thePlayer.setPosition(mc.thePlayer.posX,
					mc.thePlayer.posY + 1D, mc.thePlayer.posZ);
			}
		}else
			mc.thePlayer.stepHeight = height.getValueF();
	}
	
	@Override
	public void onDisable()
	{
		wurst.events.remove(UpdateListener.class, this);
		mc.thePlayer.stepHeight = 0.5F;
	}
	
	@Override
	public void onYesCheatUpdate(BypassLevel bypassLevel)
	{
		switch(bypassLevel)
		{
			default:
			case OFF:
			case MINEPLEX_ANTICHEAT:
			height.unlock();
			break;
			case ANTICHEAT:
			case OLDER_NCP:
			case LATEST_NCP:
			case GHOST_MODE:
			height.lockToValue(1);
			break;
		}
	}
	
	@SuppressWarnings("deprecation")
	private boolean canStep()
	{
		ArrayList<BlockPos> collisionBlocks = new ArrayList<>();
		
		EntityPlayerSP player = mc.thePlayer;
		BlockPos pos1 =
			new BlockPos(player.getEntityBoundingBox().minX - 0.001D,
				player.getEntityBoundingBox().minY - 0.001D,
				player.getEntityBoundingBox().minZ - 0.001D);
		BlockPos pos2 =
			new BlockPos(player.getEntityBoundingBox().maxX + 0.001D,
				player.getEntityBoundingBox().maxY + 0.001D,
				player.getEntityBoundingBox().maxZ + 0.001D);
		
		if(player.worldObj.isAreaLoaded(pos1, pos2))
			for(int x = pos1.getX(); x <= pos2.getX(); x++)
				for(int y = pos1.getY(); y <= pos2.getY(); y++)
					for(int z = pos1.getZ(); z <= pos2.getZ(); z++)
						if(y > player.posY - 1.0D && y <= player.posY)
							collisionBlocks.add(new BlockPos(x, y, z));
						
		BlockPos belowPlayerPos =
			new BlockPos(player.posX, player.posY - 1.0D, player.posZ);
		for(BlockPos collisionBlock : collisionBlocks)
			if(!(player.worldObj.getBlockState(collisionBlock.add(0, 1, 0))
				.getBlock() instanceof BlockFenceGate))
				if(player.worldObj.getBlockState(collisionBlock.add(0, 1, 0))
					.getBlock().getCollisionBoundingBox(
						mc.theWorld.getBlockState(collisionBlock), mc.theWorld,
						belowPlayerPos) != null)
					return false;
				
		return true;
	}
}
