package rpgcore.entities.mobs;

import java.util.ArrayList;

import org.bukkit.entity.Monster;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ReinforcedSkeleton extends RPGMonster
{
	public static double maxHealth = 50.0D;
	public static String name = "Reinforced Skeleton �7Lv. 6";
	
	public ReinforcedSkeleton(Monster entity)
	{
		super(entity);
		entity.setMaxHealth(maxHealth);
		entity.setHealth(maxHealth);
		entity.setCustomName(name);
		entity.setCustomNameVisible(true);
		
		entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2));
	}

	@Override
	public ArrayList<ItemStack> getDrops() {
		return null;
	}
}
