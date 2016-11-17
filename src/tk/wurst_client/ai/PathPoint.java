/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.ai;

import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;

public class PathPoint
{
	private BlockPos pos;
	private PathPoint previous;
	private float priority;
	private float totalCost;
	
	public PathPoint(BlockPos pos, PathPoint previous, float totalCost,
		float priority)
	{
		this.pos = pos;
		this.previous = previous;
		this.totalCost = totalCost;
		this.priority = priority;
	}
	
	public ArrayList<BlockPos> getNeighbors()
	{
		// TODO: Use start pos instead
		BlockPos playerPos = new BlockPos(Minecraft.getMinecraft().thePlayer);
		ArrayList<BlockPos> neighbors = new ArrayList<BlockPos>();
		
		// abort if too far away
		if(Math.abs(playerPos.getX() - pos.getX()) > 256
			|| Math.abs(playerPos.getZ() - pos.getZ()) > 256)
			return neighbors;
		
		BlockPos prevPos = previous != null ? previous.getPos() : null;
		
		// get all neighbors
		BlockPos north = pos.north();
		BlockPos east = pos.east();
		BlockPos south = pos.south();
		BlockPos west = pos.west();
		
		BlockPos northEast = north.east();
		BlockPos southEast = south.east();
		BlockPos southWest = south.west();
		BlockPos northWest = north.west();
		
		BlockPos up = pos.up();
		BlockPos down = pos.down();
		
		// flying
		boolean flying = PathUtils.canFlyAt(pos);
		// walking
		boolean walking = PathUtils.canBeSolid(down);
		// jumping or climbing
		boolean movingVertically = down.equals(prevPos)
			|| (up.equals(prevPos) && PathUtils.canMoveSidewaysInMidair(pos));
		
		// anything but falling
		if(flying || walking || movingVertically)
		{
			// north
			boolean basicCheckNorth =
				!north.equals(prevPos) && PathUtils.canGoThrough(north)
					&& PathUtils.canGoThrough(north.up());
			if(basicCheckNorth && !northEast.equals(prevPos)
				&& !northWest.equals(prevPos)
				&& (flying
					|| (!movingVertically
						&& PathUtils.canGoThrough(north.down()))
					|| PathUtils.canSafelyStandOn(north.down())))
				neighbors.add(north);
			
			// east
			boolean basicCheckEast =
				!east.equals(prevPos) && PathUtils.canGoThrough(east)
					&& PathUtils.canGoThrough(east.up());
			if(basicCheckEast && !northEast.equals(prevPos)
				&& !southEast.equals(prevPos)
				&& (flying
					|| (!movingVertically
						&& PathUtils.canGoThrough(east.down()))
					|| PathUtils.canSafelyStandOn(east.down())))
				neighbors.add(east);
			
			// south
			boolean basicCheckSouth =
				!south.equals(prevPos) && PathUtils.canGoThrough(south)
					&& PathUtils.canGoThrough(south.up());
			if(basicCheckSouth && !southEast.equals(prevPos)
				&& !southWest.equals(prevPos)
				&& (flying
					|| (!movingVertically
						&& PathUtils.canGoThrough(south.down()))
					|| PathUtils.canSafelyStandOn(south.down())))
				neighbors.add(south);
			
			// west
			boolean basicCheckWest =
				!west.equals(prevPos) && PathUtils.canGoThrough(west)
					&& PathUtils.canGoThrough(west.up());
			if(basicCheckWest && !southWest.equals(prevPos)
				&& !northWest.equals(prevPos)
				&& (flying
					|| (!movingVertically
						&& PathUtils.canGoThrough(west.down()))
					|| PathUtils.canSafelyStandOn(west.down())))
				neighbors.add(west);
			
			// north-east
			if(basicCheckNorth && basicCheckEast && !northEast.equals(prevPos)
				&& PathUtils.canGoThrough(northEast)
				&& PathUtils.canGoThrough(northEast.up())
				&& (flying
					|| (!movingVertically
						&& PathUtils.canGoThrough(northEast.down()))
					|| PathUtils.canSafelyStandOn(northEast.down())))
				neighbors.add(northEast);
			
			// south-east
			if(basicCheckSouth && basicCheckEast && !southEast.equals(prevPos)
				&& PathUtils.canGoThrough(southEast)
				&& PathUtils.canGoThrough(southEast.up())
				&& (flying
					|| (!movingVertically
						&& PathUtils.canGoThrough(southEast.down()))
					|| PathUtils.canSafelyStandOn(southEast.down())))
				neighbors.add(southEast);
			
			// south-west
			if(basicCheckSouth && basicCheckWest && !southWest.equals(prevPos)
				&& PathUtils.canGoThrough(southWest)
				&& PathUtils.canGoThrough(southWest.up())
				&& (flying
					|| (!movingVertically
						&& PathUtils.canGoThrough(southWest.down()))
					|| PathUtils.canSafelyStandOn(southWest.down())))
				neighbors.add(southWest);
			
			// north-west
			if(basicCheckNorth && basicCheckWest && !northWest.equals(prevPos)
				&& PathUtils.canGoThrough(northWest)
				&& PathUtils.canGoThrough(northWest.up())
				&& (flying
					|| (!movingVertically
						&& PathUtils.canGoThrough(northWest.down()))
					|| PathUtils.canSafelyStandOn(northWest.down())))
				neighbors.add(northWest);
		}
		
		// up
		if(pos.getY() < 256 && !up.equals(prevPos)
			&& PathUtils.canGoThrough(up.up()) && (flying
				|| PathUtils.canBeSolid(down) || PathUtils.canClimbUpAt(pos)))
			neighbors.add(up);
		
		// down
		if(pos.getY() > 0 && !down.equals(prevPos)
			&& PathUtils.canGoThrough(down)
			&& (flying || PathUtils.canFallBelow(this)))
			neighbors.add(down);
		
		return neighbors;
	}
	
	public BlockPos getPos()
	{
		return pos;
	}
	
	public PathPoint getPrevious()
	{
		return previous;
	}
	
	public float getPriority()
	{
		return priority;
	}
	
	public float getTotalCost()
	{
		return totalCost;
	}
}
