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
	private BlockPos goal;
	private PriorityQueue<PathPoint> queue;
	private HashMap<BlockPos, PathPoint> processed =
		new HashMap<BlockPos, PathPoint>();
	private PathPoint lastPoint;
	
	public PathFinder(BlockPos goal)
	{
		this(new BlockPos(Minecraft.getMinecraft().thePlayer), goal);
	}
	
	public PathFinder(BlockPos start, BlockPos goal)
	{
		this.goal = goal;
		queue = new PriorityQueue<PathPoint>(new Comparator<PathPoint>()
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
		queue.add(new PathPoint(start, null, 0, getDistance(start, goal)));
	}
	
	public boolean find()
	{
		long startTime = System.currentTimeMillis();
		boolean foundPath = false;
		while(!queue.isEmpty())
		{
			lastPoint = queue.poll();
			processed.put(lastPoint.getPos(), lastPoint);
			if(lastPoint.getPos().equals(goal))
			{
				foundPath = true;
				break;
			}
			if(System.currentTimeMillis() - startTime > 10e3)
			{
				System.err
					.println("Path finding took more than 10s. Aborting!");
				break;
			}
			for(BlockPos next : lastPoint.getNeighbors())
			{
				float newTotalCost = lastPoint.getTotalCost()
					+ PathUtils.getCost(lastPoint, next);
				
				if(!processed.containsKey(next)
					|| processed.get(next).getTotalCost() > newTotalCost)
					queue.add(new PathPoint(next, lastPoint, newTotalCost,
						newTotalCost + getDistance(next, goal)));
			}
		}
		return foundPath;
	}
	
	private float getDistance(BlockPos a, BlockPos b)
	{
		float dx = Math.abs(a.getX() - b.getX());
		float dy = Math.abs(a.getY() - b.getY());
		float dz = Math.abs(a.getZ() - b.getZ());
		return 1.001F
			* ((dx + dy + dz) - 0.5857864376269049F * Math.min(dx, dz));
	}
	
	public PathPoint getRawPath()
	{
		return lastPoint;
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
		PathPoint point = lastPoint;
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
