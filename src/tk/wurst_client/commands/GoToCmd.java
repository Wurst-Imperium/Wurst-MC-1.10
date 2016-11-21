/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.commands;

import java.util.ArrayList;

import org.lwjgl.input.Keyboard;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockLadder;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.math.BlockPos;
import tk.wurst_client.ai.PathFinder;
import tk.wurst_client.commands.Cmd.Info;
import tk.wurst_client.events.listeners.UpdateListener;
import tk.wurst_client.utils.BlockUtils;
import tk.wurst_client.utils.EntityUtils.TargetSettings;

@Info(description = "Walks or flies you to a specific location.",
	name = "goto",
	syntax = {"<x> <y> <z>", "<entity>"},
	help = "Commands/goto")
public class GoToCmd extends Cmd implements UpdateListener
{
	private PathFinder pathFinder;
	private ArrayList<BlockPos> path;
	private boolean enabled;
	private int index;
	
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
	public void execute(String[] args) throws Error
	{
		// disable if enabled
		if(enabled)
		{
			disable();
			
			if(args.length == 0)
				return;
		}
		
		// set PathFinder
		path = null;
		index = 0;
		int[] goal = argsToPos(targetSettings, args);
		pathFinder = new PathFinder(new BlockPos(goal[0], goal[1], goal[2]));
		
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
		
		// disable manual controls
		mc.gameSettings.keyBindForward.pressed = false;
		mc.gameSettings.keyBindBack.pressed = false;
		mc.gameSettings.keyBindRight.pressed = false;
		mc.gameSettings.keyBindLeft.pressed = false;
		mc.gameSettings.keyBindJump.pressed = false;
		mc.gameSettings.keyBindSneak.pressed = false;
		mc.thePlayer.rotationPitch = 10;
		mc.thePlayer.setSprinting(false);
		
		// get positions
		BlockPos pos = new BlockPos(mc.thePlayer);
		BlockPos nextPos = path.get(index);
		
		// check if player moved off the path
		if(index > 0)
		{
			BlockPos prevPos = path.get(index - 1);
			if((pos.getX() != prevPos.getX() && pos.getX() != nextPos.getX())
				|| (pos.getY() != prevPos.getY()
					&& pos.getY() != nextPos.getY())
				|| (pos.getZ() != prevPos.getZ()
					&& pos.getZ() != nextPos.getZ()))
			{
				wurst.chat.error("Player moved off the path.");
				disable();
				return;
			}
		}
		
		// move
		if(!pos.equals(nextPos))
		{
			BlockUtils.faceBlockClientHorizontally(nextPos);
			
			// horizontal movement
			if(pos.getX() != nextPos.getX() || pos.getZ() != nextPos.getZ())
			{
				mc.gameSettings.keyBindForward.pressed = true;
				
				// vertical movement
			}else if(pos.getY() != nextPos.getY())
			{
				mc.thePlayer.motionX = 0;
				mc.thePlayer.motionZ = 0;
				
				// flying
				if(pathFinder.flying)
				{
					if(pos.getY() < nextPos.getY())
						mc.gameSettings.keyBindJump.pressed = true;
					else
						mc.gameSettings.keyBindSneak.pressed = true;
					
					// not flying
				}else
				{
					// go up
					if(pos.getY() < nextPos.getY())
					{
						// climb up
						// TODO: vines and spider
						if(mc.theWorld.getBlockState(pos)
							.getBlock() instanceof BlockLadder)
						{
							BlockUtils.faceBlockClientHorizontally(
								pos.offset(mc.theWorld.getBlockState(pos)
									.getValue(BlockHorizontal.FACING)
									.getOpposite()));
							mc.gameSettings.keyBindForward.pressed = true;
							
							// jump up
						}else
						{
							mc.gameSettings.keyBindJump.pressed = true;
							
							// directional jump
							if(index < path.size() - 1)
							{
								BlockUtils.faceBlockClientHorizontally(
									path.get(index + 1));
								mc.gameSettings.keyBindForward.pressed = true;
							}
						}
						
						// go down
					}else
					{
						// walk off the edge
						if(mc.thePlayer.onGround)
							mc.gameSettings.keyBindForward.pressed = true;
					}
				}
			}
		}else
			index++;
		
		// disable when done
		if(index >= path.size())
			disable();
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
}
