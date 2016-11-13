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
				return (int)(o1.getPriority() - o2.getPriority());
			}
		});
		queue.add(new PathPoint(start, null, 0, 0));
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
				if(!PathUtils.isSafe(next))
					continue;
				
				float newTotalCost = lastPoint.getTotalCost()
					+ PathUtils.getCost(lastPoint.getPos(), next);
				
				if(!processed.containsKey(next)
					|| processed.get(next).getTotalCost() > newTotalCost)
					queue.add(new PathPoint(next, lastPoint, newTotalCost,
						newTotalCost + getDistance(next, goal)));
			}
		}
		System.out.println("Processed " + processed.size() + " nodes");
		return foundPath;
	}
	
	private int getDistance(BlockPos a, BlockPos b)
	{
		return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY())
			+ Math.abs(a.getZ() - b.getZ());
	}
	
	public PathPoint getRawPath()
	{
		return lastPoint;
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
		for(int i = path.size() - 1; i > 1; i--)
			if(path.get(i).getX() == path.get(i - 2).getX()
				&& path.get(i).getY() == path.get(i - 2).getY()
				|| path.get(i).getX() == path.get(i - 2).getX()
					&& path.get(i).getZ() == path.get(i - 2).getZ()
				|| path.get(i).getY() == path.get(i - 2).getY()
					&& path.get(i).getZ() == path.get(i - 2).getZ())
				path.remove(i - 1);
		return path;
	}
}
