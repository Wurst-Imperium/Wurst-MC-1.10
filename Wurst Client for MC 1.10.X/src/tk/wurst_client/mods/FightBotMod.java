/*
 * Copyright © 2014 - 2016 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.mods;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;
import tk.wurst_client.events.listeners.UpdateListener;
import tk.wurst_client.mods.Mod.Bypasses;
import tk.wurst_client.mods.Mod.Category;
import tk.wurst_client.mods.Mod.Info;
import tk.wurst_client.navigator.NavigatorItem;
import tk.wurst_client.navigator.settings.CheckboxSetting;
import tk.wurst_client.navigator.settings.SliderSetting;
import tk.wurst_client.navigator.settings.SliderSetting.ValueDisplay;
import tk.wurst_client.special.YesCheatSpf.BypassLevel;
import tk.wurst_client.utils.EntityUtils;

@Info(category = Category.COMBAT,
	description = "A bot that automatically fights for you.\n"
		+ "It walks around and kills everything.\n" + "Good for MobArena.",
	name = "FightBot",
	tags = "fight bot",
	help = "Mods/FightBot")
@Bypasses(ghostMode = false)
public class FightBotMod extends Mod implements UpdateListener
{
	public CheckboxSetting useKillaura =
		new CheckboxSetting("Use Killaura settings", true)
		{
			@Override
			public void update()
			{
				if(isChecked())
				{
					KillauraMod killaura = wurst.mods.killauraMod;
					useCooldown.lock(killaura.useCooldown.isChecked());
					speed.lockToValue(killaura.speed.getValue());
					range.lockToValue(killaura.range.getValue());
				}else
				{
					useCooldown.unlock();
					speed.unlock();
					range.unlock();
				}
			};
		};
	public CheckboxSetting useCooldown =
		new CheckboxSetting("Use Attack Cooldown as Speed", true)
		{
			@Override
			public void update()
			{
				speed.setDisabled(isChecked());
			};
		};
	public SliderSetting speed =
		new SliderSetting("Speed", 20, 2, 20, 0.1, ValueDisplay.DECIMAL);
	public SliderSetting range =
		new SliderSetting("Range", 6, 1, 6, 0.05, ValueDisplay.DECIMAL);
	public SliderSetting distance =
		new SliderSetting("Distance", 3, 1, 6, 0.05, ValueDisplay.DECIMAL);
	
	@Override
	public void initSettings()
	{
		settings.add(useKillaura);
		settings.add(useCooldown);
		settings.add(speed);
		settings.add(range);
		settings.add(distance);
	}
	
	@Override
	public NavigatorItem[] getSeeAlso()
	{
		return new NavigatorItem[]{wurst.mods.killauraMod,
			wurst.special.targetSpf, wurst.special.yesCheatSpf};
	}
	
	@Override
	public void onEnable()
	{
		wurst.events.add(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		// set target
		EntityLivingBase target =
			EntityUtils.getClosestEntity(true, 360, false);
		if(target == null)
			return;
		
		// face target
		EntityUtils.faceEntityClient(target);
		
		// walk to target
		if(mc.thePlayer.getDistanceToEntity(target) > distance.getValueF())
			mc.gameSettings.keyBindForward.pressed = true;
		else
			mc.gameSettings.keyBindForward.pressed = false;
		
		// jump
		if(mc.thePlayer.isCollidedHorizontally && mc.thePlayer.onGround)
			mc.thePlayer.jump();
		
		// swim
		if(mc.thePlayer.isInWater() && mc.thePlayer.posY < target.posY)
			mc.thePlayer.motionY += 0.04;
		
		// attack target
		updateMS();
		if((useCooldown.isChecked()
			? mc.thePlayer.getCooledAttackStrength(0F) >= 1F
			: hasTimePassedS(speed.getValueF()))
			&& mc.thePlayer.getDistanceToEntity(target) <= range.getValueF())
		{
			if(wurst.mods.autoSwordMod.isActive())
				AutoSwordMod.setSlot();
			wurst.mods.criticalsMod.doCritical();
			wurst.mods.blockHitMod.doBlock();
			if(EntityUtils.getDistanceFromMouse(target) > 55)
				EntityUtils.faceEntityClient(target);
			else
			{
				EntityUtils.faceEntityClient(target);
				mc.playerController.attackEntity(mc.thePlayer, target);
				mc.thePlayer.swingArm(EnumHand.MAIN_HAND);
			}
			updateLastMS();
		}
	}
	
	@Override
	public void onDisable()
	{
		wurst.events.remove(UpdateListener.class, this);
		mc.gameSettings.keyBindForward.pressed = false;
	}
	
	@Override
	public void onYesCheatUpdate(BypassLevel bypassLevel)
	{
		switch(bypassLevel)
		{
			default:
			case OFF:
			case MINEPLEX_ANTICHEAT:
				speed.unlock();
				range.unlock();
				distance.unlock();
				break;
			case ANTICHEAT:
			case OLDER_NCP:
			case LATEST_NCP:
			case GHOST_MODE:
				speed.lockToMax(12);
				range.lockToMax(4.25);
				distance.lockToMax(4.25);
				break;
		}
	}
}
