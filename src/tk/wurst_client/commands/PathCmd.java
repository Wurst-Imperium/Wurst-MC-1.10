/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.commands;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import tk.wurst_client.ai.PathFinder;
import tk.wurst_client.ai.PathPoint;
import tk.wurst_client.commands.Cmd.Info;
import tk.wurst_client.events.listeners.RenderListener;
import tk.wurst_client.utils.EntityUtils.TargetSettings;

@Info(
	description = "Shows the shortest path to a specific point. Useful for labyrinths and caves.",
	name = "path",
	syntax = {"<x> <y> <z>", "<entity>"},
	help = "Commands/path")
public class PathCmd extends Cmd implements RenderListener
{
	private PathPoint path;
	private boolean enabled;
	
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
		path = null;
		if(enabled)
		{
			wurst.events.remove(RenderListener.class, this);
			enabled = false;
			return;
		}
		int[] posArray = argsToPos(targetSettings, args);
		final BlockPos pos =
			new BlockPos(posArray[0], posArray[1], posArray[2]);
		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				System.out.println("Finding path");
				long startTime = System.nanoTime();
				PathFinder pathFinder = new PathFinder(pos);
				if(pathFinder.find())
				{
					path = pathFinder.getRawPath();
					enabled = true;
					wurst.events.add(RenderListener.class, PathCmd.this);
				}else
					wurst.chat.error("Could not find a path.");
				System.out.println("Done after "
					+ (System.nanoTime() - startTime) / 1e6 + "ms");
			}
		});
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
	}
	
	@Override
	public void onRender()
	{
		PathPoint currentPoint = path;
		while(currentPoint != null && currentPoint.getPrevious() != null)
		{
			PathPoint prevPoint = currentPoint.getPrevious();
			
			double x = currentPoint.getPos().getX() + 0.5
				- Minecraft.getMinecraft().getRenderManager().renderPosX;
			double y = currentPoint.getPos().getY() + 0.5
				- Minecraft.getMinecraft().getRenderManager().renderPosY;
			double z = currentPoint.getPos().getZ() + 0.5
				- Minecraft.getMinecraft().getRenderManager().renderPosZ;
			
			double prevX = prevPoint.getPos().getX() + 0.5
				- Minecraft.getMinecraft().getRenderManager().renderPosX;
			double prevY = prevPoint.getPos().getY() + 0.5
				- Minecraft.getMinecraft().getRenderManager().renderPosY;
			double prevZ = prevPoint.getPos().getZ() + 0.5
				- Minecraft.getMinecraft().getRenderManager().renderPosZ;
			
			glBlendFunc(770, 771);
			glEnable(GL_BLEND);
			glEnable(GL_LINE_SMOOTH);
			glLineWidth(2.0F);
			glDisable(GL11.GL_TEXTURE_2D);
			glDisable(GL_DEPTH_TEST);
			glDisable(GL_CULL_FACE);
			glDepthMask(false);
			
			glColor4f(0F, 1F, 0F, 0.75F);
			glBegin(GL_LINES);
			{
				glVertex3d(prevX, prevY, prevZ);
				glVertex3d(x, y, z);
			}
			glEnd();
			
			glPushMatrix();
			glTranslated(x, y, z);
			glScaled(1D / 16D, 1D / 16D, 1D / 16D);
			glRotated(Math.toDegrees(Math.atan2(y - prevY, prevZ - z)) + 90, 1,
				0, 0);
			glRotated(
				Math.toDegrees(Math.atan2(x - prevX,
					Math.sqrt(
						Math.pow(prevY - y, 2) + Math.pow(prevZ - z, 2)))),
				0, 0, 1);
			glBegin(GL_LINES);
			{
				glVertex3d(0, 2, 1);
				glVertex3d(-1, 2, 0);
				
				glVertex3d(-1, 2, 0);
				glVertex3d(0, 2, -1);
				
				glVertex3d(0, 2, -1);
				glVertex3d(1, 2, 0);
				
				glVertex3d(1, 2, 0);
				glVertex3d(0, 2, 1);
				
				glVertex3d(1, 2, 0);
				glVertex3d(-1, 2, 0);
				
				glVertex3d(0, 2, 1);
				glVertex3d(0, 2, -1);
				
				glVertex3d(0, 0, 0);
				glVertex3d(1, 2, 0);
				
				glVertex3d(0, 0, 0);
				glVertex3d(-1, 2, 0);
				
				glVertex3d(0, 0, 0);
				glVertex3d(0, 2, -1);
				
				glVertex3d(0, 0, 0);
				glVertex3d(0, 2, 1);
			}
			glEnd();
			glPopMatrix();
			
			glEnable(GL11.GL_TEXTURE_2D);
			glEnable(GL_DEPTH_TEST);
			glDepthMask(true);
			glDisable(GL_BLEND);
			
			currentPoint = prevPoint;
		}
	}
}
