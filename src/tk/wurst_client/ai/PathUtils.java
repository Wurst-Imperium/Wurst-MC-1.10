/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.ai;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.util.math.BlockPos;
import tk.wurst_client.WurstClient;

public class PathUtils
{
	private static final PlayerCapabilities playerCaps =
		Minecraft.getMinecraft().thePlayer.capabilities;
	private static final WurstClient wurst = WurstClient.INSTANCE;
	private static final Minecraft mc = Minecraft.getMinecraft();
	
	public static boolean canGoThrough(BlockPos pos)
	{
		// check if solid
		Material material = getMaterial(pos);
		Block block = getBlock(pos);
		if(material.blocksMovement() && !(block instanceof BlockSign))
			return false;
		
		// check if trapped
		if(block instanceof BlockTripWire
			|| block instanceof BlockPressurePlate)
			return false;
		
		// check if safe
		if(!playerCaps.isCreativeMode
			&& (material == Material.LAVA || material == Material.FIRE))
			return false;
		
		return true;
	}
	
	public static boolean canFlyAt(BlockPos pos)
	{
		return playerCaps.isFlying || wurst.mods.flightMod.isActive()
			|| !wurst.mods.noSlowdownMod.isActive()
				&& getMaterial(pos) == Material.WATER;
	}
	
	public static boolean canBeSolid(BlockPos pos)
	{
		Material material = getMaterial(pos);
		return (material.blocksMovement()
			&& !(getBlock(pos) instanceof BlockSign))
			|| (wurst.mods.jesusMod.isActive()
				&& (material == Material.WATER || material == Material.LAVA));
	}
	
	public static boolean canClimbUpAt(BlockPos pos)
	{
		// check if this block works for climbing
		Block block = getBlock(pos);
		if(!wurst.mods.spiderMod.isActive() && !(block instanceof BlockLadder)
			&& !(block instanceof BlockVine))
			return false;
		
		// check if any adjacent block is solid
		if(!canBeSolid(pos.north()) && !canBeSolid(pos.east())
			&& !canBeSolid(pos.south()) && !canBeSolid(pos.west()))
			return false;
		
		return true;
	}
	
	public static boolean canFallBelow(PathPoint point)
	{
		// check fall damage
		if(!checkFallDamage(point))
			return false;
		
		// check if player can stand below or keep falling
		BlockPos down2 = point.getPos().down(2);
		if(!canGoThrough(down2) && !canSafelyStandOn(down2))
			return false;
		
		return true;
	}
	
	private static boolean checkFallDamage(PathPoint point)
	{
		// check if fall damage is off
		if(playerCaps.isCreativeMode || wurst.mods.noFallMod.isActive())
			return true;
		
		// check if fall does not end yet
		BlockPos pos = point.getPos();
		BlockPos down2 = pos.down(2);
		if(!getMaterial(down2).blocksMovement()
			|| getBlock(down2) instanceof BlockSign)
			return true;
		
		// check if fall ends with slime block
		if(getBlock(down2) instanceof BlockSlime)
			return true;
		
		// check current and previous points
		PathPoint prevPoint = point;
		for(int i = 0; i <= 3; i++)
		{
			// check if point does not exist
			if(prevPoint == null)
				return true;
			
			BlockPos prevPos = prevPoint.getPos();
			
			// check if point is not part of this fall
			// (meaning the fall is too short to cause damage)
			if(!pos.up(i).equals(prevPos))
				return true;
			
			// check if block resets fall damage
			Block prevBlock = getBlock(prevPos);
			if(prevBlock instanceof BlockLiquid
				|| prevBlock instanceof BlockLadder
				|| prevBlock instanceof BlockVine
				|| prevBlock instanceof BlockWeb)
				return true;
			
			prevPoint = prevPoint.getPrevious();
		}
		
		return false;
	}
	
	public static boolean canMoveSidewaysInMidair(BlockPos pos)
	{
		// check feet
		Block blockFeet = getBlock(pos);
		if(blockFeet instanceof BlockLiquid || blockFeet instanceof BlockLadder
			|| blockFeet instanceof BlockVine || blockFeet instanceof BlockWeb)
			return true;
		
		// check head
		Block blockHead = getBlock(pos.up());
		if(blockHead instanceof BlockLiquid || blockHead instanceof BlockWeb)
			return true;
		
		return false;
	}
	
	public static boolean canSafelyStandOn(BlockPos pos)
	{
		// check if solid
		Material material = getMaterial(pos);
		if(!canBeSolid(pos))
			return false;
		
		// check if safe
		if(!playerCaps.isCreativeMode && material == Material.CACTUS)
			return false;
		
		return true;
	}
	
	public static float getCost(PathPoint lastPoint, BlockPos next)
	{
		float cost = 1F;
		
		// diagonal movement
		if(lastPoint.getPos().getX() != next.getX()
			&& lastPoint.getPos().getZ() != next.getZ())
			cost *= 1.4142135623730951F;
		
		// liquids
		Material nextMaterial = getMaterial(next);
		if(nextMaterial == Material.WATER
			&& !wurst.mods.noSlowdownMod.isActive())
			cost *= 1.3164437838225804F;
		else if(nextMaterial == Material.LAVA)
			cost *= 4.539515393656079F;
		
		// soul sand
		if(!canFlyAt(next) && getBlock(next.down()) instanceof BlockSoulSand)
			cost *= 2.5F;
		
		return cost;
	}
	
	private static Material getMaterial(BlockPos pos)
	{
		return mc.theWorld.getBlockState(pos).getMaterial();
	}
	
	private static Block getBlock(BlockPos pos)
	{
		return mc.theWorld.getBlockState(pos).getBlock();
	}
}
