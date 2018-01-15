package rpgcore.entites.bosses;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.entity.Monster;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import rpgcore.entities.mobs.CasterEntity;
import rpgcore.main.CakeAPI;

public class Astrea extends CasterEntity
{
	public static double maxHealth = 500000.0D;
	public static String name = CakeAPI.recodeColorCodes("&6&lA&e&ls&6&lt&e&lr&6&le&e&la&7 Lv. ??");
	public Random rand = new Random();

	public Astrea(Monster m)
	{
		super(m);
		this.reachDistance = 32.0D;
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
	public void tick()
	{
		super.tick();
		if (isDead())
			return;
		if (castDelay > 0 || target == null)
			return;
		int r = rand.nextInt(10) + 1;
	}
}
