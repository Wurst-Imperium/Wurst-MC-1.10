package tk.wurst_client.mods;

import tk.wurst_client.mods.Mod.Bypasses;
import tk.wurst_client.mods.Mod.Category;
import tk.wurst_client.mods.Mod.Info;
import tk.wurst_client.navigator.settings.CheckboxSetting;
import tk.wurst_client.navigator.settings.SliderSetting;
import tk.wurst_client.navigator.settings.SliderSetting.ValueDisplay;

@Info(category = Category.RENDER,
	description = "Changes the world wheather and time in the client side.",
	name = "Weather",
	help = "Mods/Weather")
@Bypasses(ghostMode = true)
public class WeatherMod extends Mod
{
	public CheckboxSetting disableRain = new CheckboxSetting("Disable Rain", false);
	public CheckboxSetting changeTime = new CheckboxSetting("Change World Time", false)
	{
		@Override
		public void update()
		{
			if(!isChecked())
			{
				time.lockToValue(time.getValue());
			}
			else
			{
				time.unlock();
			}
		};
	};
	public SliderSetting time = new SliderSetting("Time", 12000, 0, 24000, 1,
		ValueDisplay.INTEGER);
	
	public CheckboxSetting changeMoonPhase = new CheckboxSetting("Change MoonPhase", false)
	{
		@Override
		public void update()
		{
			if(!isChecked())
			{
				moonPhase.lockToValue(moonPhase.getValue());
			}
			else
			{
				moonPhase.unlock();
			}
		};
	};
	public SliderSetting moonPhase = new SliderSetting("Moon Phase", 0, 0, 8, 1,
		ValueDisplay.INTEGER);
	
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
