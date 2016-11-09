/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.mods;

import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import tk.wurst_client.events.listeners.UpdateListener;
import tk.wurst_client.mods.Mod.Bypasses;
import tk.wurst_client.mods.Mod.Info;
import tk.wurst_client.utils.EntityUtils;
import tk.wurst_client.utils.EntityUtils.TargetSettings;

@Info(
	description = "Allows you to see the world as someone else.\n"
		+ "Use the .rv command to make it target a specific entity.",
	name = "RemoteView",
	tags = "remote view",
	help = "Mods/RemoteView")
@Bypasses
public class RemoteViewMod extends Mod implements UpdateListener
{
	private Entity entity = null;
	
	private double oldX;
	private double oldY;
	private double oldZ;
	private float oldYaw;
	private float oldPitch;
	private boolean wasInvisible;
	
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
	public void onEnable()
	{
		// find entity if not already set
		if(entity == null)
		{
			entity = EntityUtils.getClosestEntity(targetSettings);
			
			// check if entity was found
			if(entity == null)
			{
				wurst.chat.message("There is no nearby entity.");
				setEnabled(false);
				return;
			}
		}
		
		// save old data
		oldX = mc.thePlayer.posX;
		oldY = mc.thePlayer.posY;
		oldZ = mc.thePlayer.posZ;
		oldYaw = mc.thePlayer.rotationYaw;
		oldPitch = mc.thePlayer.rotationPitch;
		wasInvisible = entity.isInvisibleToPlayer(mc.thePlayer);
		
		// activate NoClip
		mc.thePlayer.noClip = true;
		
		// spawn fake player
		EntityOtherPlayerMP fakePlayer =
			new EntityOtherPlayerMP(mc.theWorld, mc.thePlayer.getGameProfile());
		fakePlayer.clonePlayer(mc.thePlayer, true);
		fakePlayer.copyLocationAndAnglesFrom(mc.thePlayer);
		fakePlayer.rotationYawHead = mc.thePlayer.rotationYawHead;
		mc.theWorld.addEntityToWorld(-69, fakePlayer);
		
		// success message
		wurst.chat.message("Now viewing " + entity.getName() + ".");
		
		// add listener
		wurst.events.add(UpdateListener.class, this);
	}
	
	public void onToggledByCommand(String viewName)
	{
		// set entity
		if(!isEnabled() && viewName != null && !viewName.isEmpty())
			entity = EntityUtils.getEntityWithName(viewName, targetSettings);
		
		// toggle RemoteView
		toggle();
	}
	
	@Override
	public void onUpdate()
	{
		// validate entity
		if(!EntityUtils.isCorrectEntity(entity, targetSettings))
		{
			setEnabled(false);
			return;
		}
		
		// update position, rotation, etc.
		mc.thePlayer.copyLocationAndAnglesFrom(entity);
		mc.thePlayer.motionX = 0;
		mc.thePlayer.motionY = 0;
		mc.thePlayer.motionZ = 0;
		
		// set entity invisible
		entity.setInvisible(true);
	}
	
	@Override
	public void onDisable()
	{
		// remove listener
		wurst.events.remove(UpdateListener.class, this);
		
		// reset entity
		if(entity != null)
		{
			wurst.chat.message("No longer viewing " + entity.getName() + ".");
			entity.setInvisible(wasInvisible);
			entity = null;
		}
		
		// reset player
		mc.thePlayer.noClip = false;
		mc.thePlayer.setPositionAndRotation(oldX, oldY, oldZ, oldYaw, oldPitch);
		
		// remove fake player
		mc.theWorld.removeEntityFromWorld(-69);
	}
}
