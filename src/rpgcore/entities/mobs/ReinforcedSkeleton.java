package rpgcore.entities.mobs;

import org.bukkit.entity.Monster;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ReinforcedSkeleton extends RPGMonster
{
	public static double maxHealth = 50.0D;
	public static String name = "Reinforced Skeleton §7Lv. 6";
	
	public ReinforcedSkeleton(Monster entity)
	{
		super(entity, false);
		entity.setMaxHealth(maxHealth);
		entity.setHealth(maxHealth);
		entity.setCustomName(name);
		entity.setCustomNameVisible(true);
		
		entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2));
	}
}
