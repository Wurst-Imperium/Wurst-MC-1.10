package net.wurstclient.mods;

import net.wurstclient.mods.Mod.Bypasses;
import net.wurstclient.mods.Mod.Info;
import net.wurstclient.navigator.settings.CheckboxSetting;
import net.wurstclient.navigator.settings.SliderSetting;
import net.wurstclient.navigator.settings.SliderSetting.ValueDisplay;

@Info(
	description = "Allows you to alter the client-side weather, time and moon phase.",
	name = "NoWeather")
@Bypasses
public class NoWeatherMod extends Mod
{
	public CheckboxSetting disableRain =
		new CheckboxSetting("Disable Rain", true);
	
	public CheckboxSetting changeTime =
		new CheckboxSetting("Change World Time", false)
		{
			@Override
			public void update()
			{
				if(!isChecked())
					time.setDisabled(true);
				else
					time.setDisabled(false);
			};
		};
	public SliderSetting time =
		new SliderSetting("Time", 6000, 0, 23900, 100, ValueDisplay.INTEGER);
	
	public CheckboxSetting changeMoonPhase =
		new CheckboxSetting("Change Moon Phase", false)
		{
			@Override
			public void update()
			{
				if(!isChecked())
					moonPhase.setDisabled(true);
				else
					moonPhase.setDisabled(false);
			};
		};
	public SliderSetting moonPhase =
		new SliderSetting("Moon Phase", 0, 0, 7, 1, ValueDisplay.INTEGER);
	
	@Override
	public void initSettings()
	{
		settings.add(disableRain);
		settings.add(changeTime);
		settings.add(time);
		settings.add(changeMoonPhase);
		settings.add(moonPhase);
	}
}
