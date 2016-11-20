/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.mods;

import java.util.ArrayList;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockLadder;
import net.minecraft.util.math.BlockPos;
import tk.wurst_client.ai.PathFinder;
import tk.wurst_client.events.listeners.UpdateListener;
import tk.wurst_client.mods.Mod.Bypasses;
import tk.wurst_client.mods.Mod.Info;
import tk.wurst_client.navigator.NavigatorItem;
import tk.wurst_client.utils.BlockUtils;

@Info(
	description = "This mod shouldn't be here, we will remove it eventually.\n"
		+ "Please use the .goto command instead.",
	name = "GoTo")
@Bypasses
public class GoToCmdMod extends Mod implements UpdateListener
{
	private static ArrayList<BlockPos> path;
	private int index;
	private PathFinder pathFinder;
	
	@Override
	public NavigatorItem[] getSeeAlso()
	{
		return new NavigatorItem[]{wurst.commands.goToCmd};
	}
	
	@Override
	public String getRenderName()
	{
		if(pathFinder != null)
		{
			BlockPos goal = pathFinder.getGoal();
			return "Go to " + goal.getX() + " " + goal.getY() + " "
				+ goal.getZ();
		}else
			return "GoTo";
	}
	
	@Override
	public void onEnable()
	{
		if(pathFinder == null)
		{
			wurst.chat.error("This mod shouldn't exist. Use .goto instead.");
			setEnabled(false);
			return;
		}
		
		index = 0;
		path = pathFinder.formatPath();
		wurst.events.add(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		if(pathFinder == null || path == null)
		{
			setEnabled(false);
			return;
		}
		BlockPos currentPos = new BlockPos(mc.thePlayer);
		BlockPos nextPos = path.get(index);
		float dist = BlockUtils.getPlayerBlockDistance(nextPos);
		float hDist = BlockUtils.getHorizontalPlayerBlockDistance(nextPos);
		double vDist = Math.abs(mc.thePlayer.posY - nextPos.getY());
		mc.gameSettings.keyBindForward.pressed = false;
		mc.gameSettings.keyBindBack.pressed = false;
		mc.gameSettings.keyBindRight.pressed = false;
		mc.gameSettings.keyBindLeft.pressed = false;
		mc.gameSettings.keyBindJump.pressed = false;
		mc.gameSettings.keyBindSneak.pressed = false;
		mc.thePlayer.rotationPitch = 10;
		BlockUtils.faceBlockClientHorizontally(nextPos);
		
		if(mc.thePlayer.isInWater() && currentPos.getY() <= nextPos.getY())
			mc.gameSettings.keyBindJump.pressed = true;
		
		if(hDist > 0.25)
			mc.gameSettings.keyBindForward.pressed = true;
		if(vDist > 0.75)
			if(pathFinder.canFlyAt(currentPos))
			{
				if(currentPos.getY() > nextPos.getY())
					mc.gameSettings.keyBindSneak.pressed = true;
				else
					mc.gameSettings.keyBindJump.pressed = true;
			}else if(pathFinder.canClimbUpAt(currentPos)
				&& currentPos.getY() < nextPos.getY())
			{
				if(mc.theWorld.getBlockState(currentPos)
					.getBlock() instanceof BlockLadder)
				{
					BlockUtils.faceBlockClientHorizontally(
						currentPos.offset(mc.theWorld.getBlockState(currentPos)
							.getValue(BlockHorizontal.FACING).getOpposite()));
					mc.gameSettings.keyBindForward.pressed = true;
				}else
				{
					BlockPos[] neighbors = new BlockPos[]{
						currentPos.add(0, 0, -1), currentPos.add(0, 0, 1),
						currentPos.add(1, 0, 0), currentPos.add(-1, 0, 0)};
					for(BlockPos neigbor : neighbors)
					{
						if(!pathFinder.canBeSolid(neigbor))
							continue;
						BlockUtils.faceBlockClientHorizontally(neigbor);
						mc.gameSettings.keyBindForward.pressed = true;
						break;
					}
				}
			}
		
		if(dist < 1)
			index++;
		if(index >= path.size())
			setEnabled(false);
	}
	
	@Override
	public void onDisable()
	{
		wurst.events.remove(UpdateListener.class, this);
		pathFinder = null;
		path = null;
		mc.gameSettings.keyBindForward.pressed = false;
		mc.gameSettings.keyBindBack.pressed = false;
		mc.gameSettings.keyBindRight.pressed = false;
		mc.gameSettings.keyBindLeft.pressed = false;
		mc.gameSettings.keyBindJump.pressed = false;
		mc.gameSettings.keyBindSneak.pressed = false;
	}
	
	public void setPathFinder(PathFinder pathFinder)
	{
		this.pathFinder = pathFinder;
	}
}
