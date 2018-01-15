package rpgcore.entities.mobs;

import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ReinforcedZombie 
{
	public static double maxHealth = 70.0D;
	public static String name = "Reinforced Zombie §7Lv. 6";
	
	public ReinforcedZombie(LivingEntity entity)
	{
		entity.setMaxHealth(maxHealth);
		entity.setHealth(maxHealth);
		entity.setCustomName(name);
		entity.setCustomNameVisible(true);
		
		entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2));
	}
}
