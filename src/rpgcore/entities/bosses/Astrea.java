package rpgcore.entities.bosses;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.entity.Monster;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import rpgcore.entities.mobs.RPGMonster;
import rpgcore.main.CakeLibrary;

public class Astrea extends RPGMonster
{
	public static double maxHealth = 500000.0D;
	public static String name = CakeLibrary.recodeColorCodes("&6&lA&e&ls&6&lt&e&lr&6&le&e&la&7 Lv. ??");

	public Astrea(Monster m)
	{
		super(m, true);
		entity.setRemoveWhenFarAway(false);
		entity.setMaxHealth(maxHealth);
		entity.setHealth(maxHealth);
		entity.setCustomName(name);
		entity.setCustomNameVisible(true);

		entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 6));
		entity.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0));
		
		EntityEquipment eq = entity.getEquipment();
		eq.setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
		eq.setChestplateDropChance(0.0F);
		eq.setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
		eq.setLeggingsDropChance(0.0F);
		eq.setBoots(new ItemStack(Material.DIAMOND_BOOTS));
		eq.setBootsDropChance(0.0F);
	}

	@Override
	public boolean tick()
	{
		super.tick();
		if (isDead())
			return true;
		if (castDelay > 0 || target == null)
			return false;
		return false;
	}
	
	public ArrayList<ItemStack> getDrops()
	{
		ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
		return drops;
	}
}
