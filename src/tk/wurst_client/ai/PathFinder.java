/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public class PathFinder
{
	private BlockPos start;
	private BlockPos goal;
	private PathPoint currentPoint;
	private HashMap<BlockPos, Float> costMap = new HashMap<>();
	private PriorityQueue<PathPoint> queue =
		new PriorityQueue<>((PathPoint o1, PathPoint o2) -> {
			float d = o1.getPriority() - o2.getPriority();
			if(d > 0)
				return 1;
			else if(d < 0)
				return -1;
			else
				return 0;
		});
	
	public PathFinder(BlockPos goal)
	{
		this(new BlockPos(Minecraft.getMinecraft().thePlayer), goal);
	}
	
	public PathFinder(BlockPos start, BlockPos goal)
	{
		this.start = start;
		this.goal = goal;
		queue.add(new PathPoint(start, null, 0, getDistance(start)));
	}
	
	@Deprecated
	public boolean find()
	{
		return process(2000000);
	}
	
	public boolean process(int limit)
	{
		for(int i = 0; i < limit && !queue.isEmpty(); i++)
		{
			// get next point
			currentPoint = queue.poll();
			
			// check if goal is reached
			// TODO: custom condition for reaching goal
			if(currentPoint.getPos().equals(goal))
				return true;
			
			// add neighbors to queue
			for(BlockPos nextPos : getNeighbors(currentPoint.getPos()))
			{
				float newTotalCost = currentPoint.getTotalCost()
					+ PathUtils.getCost(currentPoint, nextPos);
				
				// check if there is a better way to get here
				if(costMap.containsKey(nextPos)
					&& costMap.get(nextPos) <= newTotalCost)
					continue;
				
				// get next movement direction
				BlockPos pos = currentPoint.getPos();
				Vec3i nextMove = nextPos.subtract(currentPoint.getPos());
				
				// vertical
				if(nextMove.getY() != 0)
				{
					// up: no further checks required
					
					// down: check fall damage
					if(nextMove.getY() < 0 && !PathUtils.canFlyAt(pos)
						&& !PathUtils.canFallBelow(currentPoint))
						continue;
					
					// horizontal
				}else
				{
					// check if flying, walking or jumping
					BlockPos prevPos = currentPoint.getPrevious() == null ? null
						: currentPoint.getPrevious().getPos();
					BlockPos down = pos.down();
					if(!PathUtils.canFlyAt(pos) && !PathUtils.canBeSolid(down)
						&& !down.equals(prevPos))
						continue;
				}
				
				// add this point to queue and cost map
				costMap.put(nextPos, newTotalCost);
				queue.add(new PathPoint(nextPos, currentPoint, newTotalCost,
					newTotalCost + getDistance(nextPos)));
			}
		}
		return false;
	}
	
	private ArrayList<BlockPos> getNeighbors(BlockPos pos)
	{
		ArrayList<BlockPos> neighbors = new ArrayList<BlockPos>();
		
		// abort if too far away
		if(Math.abs(start.getX() - pos.getX()) > 256
			|| Math.abs(start.getZ() - pos.getZ()) > 256)
			return neighbors;
		
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
		boolean onGround = PathUtils.canBeSolid(down);
		
		// player can move sideways if flying, standing on the ground, jumping
		// (one block above ground), or in a block that allows sideways movement
		// (ladder, web, etc.)
		if(flying || onGround || PathUtils.canBeSolid(down.down())
			|| PathUtils.canMoveSidewaysInMidair(pos)
			|| PathUtils.canClimbUpAt(pos.down()))
		{
			// north
			boolean basicCheckNorth = PathUtils.canGoThrough(north)
				&& PathUtils.canGoThrough(north.up());
			if(basicCheckNorth
				&& (flying || PathUtils.canGoThrough(north.down())
					|| PathUtils.canSafelyStandOn(north.down())))
				neighbors.add(north);
			
			// east
			boolean basicCheckEast = PathUtils.canGoThrough(east)
				&& PathUtils.canGoThrough(east.up());
			if(basicCheckEast && (flying || PathUtils.canGoThrough(east.down())
				|| PathUtils.canSafelyStandOn(east.down())))
				neighbors.add(east);
			
			// south
			boolean basicCheckSouth = PathUtils.canGoThrough(south)
				&& PathUtils.canGoThrough(south.up());
			if(basicCheckSouth
				&& (flying || PathUtils.canGoThrough(south.down())
					|| PathUtils.canSafelyStandOn(south.down())))
				neighbors.add(south);
			
			// west
			boolean basicCheckWest = PathUtils.canGoThrough(west)
				&& PathUtils.canGoThrough(west.up());
			if(basicCheckWest && (flying || PathUtils.canGoThrough(west.down())
				|| PathUtils.canSafelyStandOn(west.down())))
				neighbors.add(west);
			
			// north-east
			if(basicCheckNorth && basicCheckEast
				&& PathUtils.canGoThrough(northEast)
				&& PathUtils.canGoThrough(northEast.up())
				&& (flying || PathUtils.canGoThrough(northEast.down())
					|| PathUtils.canSafelyStandOn(northEast.down())))
				neighbors.add(northEast);
			
			// south-east
			if(basicCheckSouth && basicCheckEast
				&& PathUtils.canGoThrough(southEast)
				&& PathUtils.canGoThrough(southEast.up())
				&& (flying || PathUtils.canGoThrough(southEast.down())
					|| PathUtils.canSafelyStandOn(southEast.down())))
				neighbors.add(southEast);
			
			// south-west
			if(basicCheckSouth && basicCheckWest
				&& PathUtils.canGoThrough(southWest)
				&& PathUtils.canGoThrough(southWest.up())
				&& (flying || PathUtils.canGoThrough(southWest.down())
					|| PathUtils.canSafelyStandOn(southWest.down())))
				neighbors.add(southWest);
			
			// north-west
			if(basicCheckNorth && basicCheckWest
				&& PathUtils.canGoThrough(northWest)
				&& PathUtils.canGoThrough(northWest.up())
				&& (flying || PathUtils.canGoThrough(northWest.down())
					|| PathUtils.canSafelyStandOn(northWest.down())))
				neighbors.add(northWest);
		}
		
		// up
		if(pos.getY() < 256 && PathUtils.canGoThrough(up.up())
			&& (flying || onGround || PathUtils.canClimbUpAt(pos)))
			neighbors.add(up);
		
		// down
		if(pos.getY() > 0 && PathUtils.canGoThrough(down))
			neighbors.add(down);
		
		return neighbors;
	}
	
	private float getDistance(BlockPos pos)
	{
		float dx = Math.abs(pos.getX() - goal.getX());
		float dy = Math.abs(pos.getY() - goal.getY());
		float dz = Math.abs(pos.getZ() - goal.getZ());
		return 1.001F
			* ((dx + dy + dz) - 0.5857864376269049F * Math.min(dx, dz));
	}
	
	public PathPoint getCurrentPoint()
	{
		return currentPoint;
	}
	
	public Set<BlockPos> getProcessedBlocks()
	{
		return costMap.keySet();
	}
	
	public PathPoint[] getQueuedPoints()
	{
		return queue.toArray(new PathPoint[queue.size()]);
	}
	
	public ArrayList<BlockPos> formatPath()
	{
		ArrayList<BlockPos> path = new ArrayList<BlockPos>();
		PathPoint point = currentPoint;
		while(point != null)
		{
			path.add(point.getPos());
			point = point.getPrevious();
		}
		Collections.reverse(path);
		// for(int i = path.size() - 1; i > 1; i--)
		// if(path.get(i).getX() == path.get(i - 2).getX()
		// && path.get(i).getY() == path.get(i - 2).getY()
		// || path.get(i).getX() == path.get(i - 2).getX()
		// && path.get(i).getZ() == path.get(i - 2).getZ()
		// || path.get(i).getY() == path.get(i - 2).getY()
		// && path.get(i).getZ() == path.get(i - 2).getZ())
		// path.remove(i - 1);
		return path;
	}
}
