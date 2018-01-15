package rpgcore.entities.mobs;

import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ReinforcedSkeleton 
{
	public static double maxHealth = 50.0D;
	public static String name = "Reinforced Skeleton §7Lv. 6";
	
	public ReinforcedSkeleton(LivingEntity entity)
	{
		entity.setMaxHealth(maxHealth);
		entity.setHealth(maxHealth);
		entity.setCustomName(name);
		entity.setCustomNameVisible(true);
		
		entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2));
	}
}
