package rpgcore.entities.mobs;

import org.bukkit.entity.Slime;

import rpgcore.main.RPGCore;

public class WeakSlime
{
	public static double maxHealth = 20.0D;
	public static String name = "§aWeak Slime §7Lv. 3";

	public WeakSlime(Slime entity)
	{
		randomizeLevel();
		
		entity.setMaxHealth(maxHealth);
		entity.setHealth(maxHealth);
		entity.setCustomName(name);
		entity.setCustomNameVisible(true);
	}
	
	public void randomizeLevel()
	{
		int lv = 2 + RPGCore.rand.nextInt(3);
		maxHealth = 10D + (lv * 5D);
		name = "§aWeak Slime §7Lv. " + lv;
	}
}
