/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.wurstclient.features.commands;

import java.util.ArrayList;

import org.lwjgl.input.Keyboard;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockLadder;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.math.BlockPos;
import net.wurstclient.ai.PathFinder;
import net.wurstclient.compatibility.WMinecraft;
import net.wurstclient.events.listeners.UpdateListener;
import net.wurstclient.utils.BlockUtils;
import net.wurstclient.utils.EntityUtils.TargetSettings;

@Cmd.Info(description = "Walks or flies you to a specific location.",
	name = "goto",
	syntax = {"<x> <y> <z>", "<entity>", "-path"},
	help = "Commands/goto")
public final class GoToCmd extends Cmd implements UpdateListener
{
	private PathFinder pathFinder;
	private ArrayList<BlockPos> path;
	private boolean enabled;
	private int index;
	private boolean stopped;
	
	private TargetSettings targetSettings = new TargetSettings()
	{
		@Override
		public boolean targetFriends()
		{
			return true;
		}
		
		@Override
		public boolean targetBehindWalls()
		{
			return true;
		};
	};
	
	@Override
	public void execute(String[] args) throws CmdError
	{
		// disable if enabled
		if(enabled)
		{
			disable();
			
			if(args.length == 0)
				return;
		}
		
		// resets
		path = null;
		index = 0;
		stopped = false;
		
		// set PathFinder
		if(args.length == 1 && args[0].equals("-path"))
		{
			BlockPos goal = wurst.commands.pathCmd.getLastGoal();
			if(goal != null)
				pathFinder = new PathFinder(goal);
			else
				error("No previous position on .path.");
		}else
		{
			int[] goal = argsToPos(targetSettings, args);
			pathFinder =
				new PathFinder(new BlockPos(goal[0], goal[1], goal[2]));
		}
		
		// start
		enabled = true;
		wurst.events.add(UpdateListener.class, this);
		System.out.println("Finding path...");
	}
	
	@Override
	public void onUpdate()
	{
		// find path
		if(path == null)
		{
			if(!pathFinder.process(1024))
				return;
			
			path = pathFinder.formatPath();
			System.out.println("Done");
		}
		
		// get positions
		BlockPos pos = new BlockPos(WMinecraft.getPlayer());
		BlockPos nextPos = path.get(index);
		
		// update index
		if(pos.equals(nextPos))
		{
			index++;
			
			if(index < path.size())
			{
				// stop when changing directions
				if(pathFinder.creativeFlying && index >= 2)
				{
					BlockPos prevPos = path.get(index - 1);
					if(!path.get(index).subtract(prevPos)
						.equals(prevPos.subtract(path.get(index - 2))))
						if(!stopped)
						{
							WMinecraft.getPlayer().motionX /= Math.max(
								Math.abs(WMinecraft.getPlayer().motionX) * 50,
								1);
							WMinecraft.getPlayer().motionY /= Math.max(
								Math.abs(WMinecraft.getPlayer().motionY) * 50,
								1);
							WMinecraft.getPlayer().motionZ /= Math.max(
								Math.abs(WMinecraft.getPlayer().motionZ) * 50,
								1);
							stopped = true;
						}
				}
				
				// disable when done
			}else
			{
				if(pathFinder.creativeFlying)
				{
					WMinecraft.getPlayer().motionX /= Math
						.max(Math.abs(WMinecraft.getPlayer().motionX) * 50, 1);
					WMinecraft.getPlayer().motionY /= Math
						.max(Math.abs(WMinecraft.getPlayer().motionY) * 50, 1);
					WMinecraft.getPlayer().motionZ /= Math
						.max(Math.abs(WMinecraft.getPlayer().motionZ) * 50, 1);
				}
				
				disable();
			}
			
			return;
		}
		
		stopped = false;
		
		// disable manual controls
		mc.gameSettings.keyBindForward.pressed = false;
		mc.gameSettings.keyBindBack.pressed = false;
		mc.gameSettings.keyBindRight.pressed = false;
		mc.gameSettings.keyBindLeft.pressed = false;
		mc.gameSettings.keyBindJump.pressed = false;
		mc.gameSettings.keyBindSneak.pressed = false;
		WMinecraft.getPlayer().rotationPitch = 10;
		WMinecraft.getPlayer().setSprinting(false);
		WMinecraft.getPlayer().capabilities.isFlying =
			pathFinder.creativeFlying;
		
		// check if player moved off the path
		if(index > 0)
		{
			BlockPos prevPos = path.get(index - 1);
			if(pos.getX() != prevPos.getX() && pos.getX() != nextPos.getX()
				|| pos.getY() != prevPos.getY() && pos.getY() != nextPos.getY()
				|| pos.getZ() != prevPos.getZ() && pos.getZ() != nextPos.getZ())
				System.err.println("Player moved off the path.");
		}
		
		// move
		BlockUtils.faceBlockClientHorizontally(nextPos);
		
		// horizontal movement
		if(pos.getX() != nextPos.getX() || pos.getZ() != nextPos.getZ())
			mc.gameSettings.keyBindForward.pressed = true;
		else if(pos.getY() != nextPos.getY())
			// flying
			if(pathFinder.flying)
			{
				if(pos.getY() < nextPos.getY())
					mc.gameSettings.keyBindJump.pressed = true;
				else
					mc.gameSettings.keyBindSneak.pressed = true;
				
				// not flying
			}else // go up
			if(pos.getY() < nextPos.getY())
			{
				// climb up
				// TODO: vines and spider
				if(WMinecraft.getWorld().getBlockState(pos)
					.getBlock() instanceof BlockLadder)
				{
					BlockUtils.faceBlockClientHorizontally(
						pos.offset(WMinecraft.getWorld().getBlockState(pos)
							.getValue(BlockHorizontal.FACING).getOpposite()));
					mc.gameSettings.keyBindForward.pressed = true;
					
					// jump up
				}else
				{
					mc.gameSettings.keyBindJump.pressed = true;
					
					// directional jump
					if(index < path.size() - 1)
					{
						BlockUtils
							.faceBlockClientHorizontally(path.get(index + 1));
						mc.gameSettings.keyBindForward.pressed = true;
					}
				}
				
				// go down
			}else // walk off the edge
			if(WMinecraft.getPlayer().onGround)
				mc.gameSettings.keyBindForward.pressed = true;
	}
	
	private void disable()
	{
		// get keys
		GameSettings gs = mc.gameSettings;
		KeyBinding[] keys = new KeyBinding[]{gs.keyBindForward, gs.keyBindJump,
			gs.keyBindSneak};
		
		// reset keys
		for(KeyBinding key : keys)
			key.pressed = Keyboard.isKeyDown(key.getKeyCode());
		
		wurst.events.remove(UpdateListener.class, this);
		enabled = false;
	}
	
	public boolean isActive()
	{
		return enabled;
	}
}
