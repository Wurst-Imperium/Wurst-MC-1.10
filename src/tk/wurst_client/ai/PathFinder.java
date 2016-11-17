/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.ai;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;

public class PathFinder
{
	private BlockPos start;
	private BlockPos goal;
	private PathPoint currentPoint;
	private HashMap<BlockPos, PathPoint> processed =
		new HashMap<BlockPos, PathPoint>();
	private PriorityQueue<PathPoint> queue =
		new PriorityQueue<PathPoint>(new Comparator<PathPoint>()
		{
			@Override
			public int compare(PathPoint o1, PathPoint o2)
			{
				float d = o1.getPriority() - o2.getPriority();
				if(d > 0)
					return 1;
				else if(d < 0)
					return -1;
				else
					return 0;
			}
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
			for(BlockPos next : currentPoint.getNeighbors())
			{
				float newTotalCost = currentPoint.getTotalCost()
					+ PathUtils.getCost(currentPoint, next);
				
				if(!processed.containsKey(next)
					|| processed.get(next).getTotalCost() > newTotalCost)
					queue.add(new PathPoint(next, currentPoint, newTotalCost,
						newTotalCost + getDistance(next)));
			}
			
			// mark point as processed
			processed.put(currentPoint.getPos(), currentPoint);
		}
		return false;
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
	
	public Collection<PathPoint> getProcessedPoints()
	{
		return processed.values();
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
