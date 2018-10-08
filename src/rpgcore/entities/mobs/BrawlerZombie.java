package rpgcore.entities.mobs;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Monster;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BrawlerZombie extends RPGMonster
{
	public static double maxHealth = 1300.0D;
	public static String name = "§4Brawler Zombie §7Lv. 30";

	public BrawlerZombie(Monster entity)
	{
		super(entity, false);
		entity.setMaxHealth(maxHealth);
		entity.setHealth(maxHealth);
		entity.setCustomName(name);
		entity.setCustomNameVisible(true);

		entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 3));
		entity.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0));
		ItemStack hand = new ItemStack(Material.IRON_SWORD);
		hand.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);

		EntityEquipment eq = entity.getEquipment();
		eq.setItemInMainHand(hand);
		eq.setItemInMainHandDropChance(0);
		eq.setHelmet(new ItemStack(Material.GOLD_HELMET));
		eq.setHelmetDropChance(0);
		eq.setChestplate(new ItemStack(Material.GOLD_CHESTPLATE));
		eq.setChestplateDropChance(0);
		eq.setLeggings(new ItemStack(Material.GOLD_LEGGINGS));
		eq.setLeggingsDropChance(0);
		eq.setBoots(new ItemStack(Material.GOLD_BOOTS));
		eq.setBootsDropChance(0);
	}

	@Override
	public boolean tick()
	{
		super.tick();
		if (isDead())
			return true;
		if (castDelay > 0 || target == null)
			return false;
		int i = rand.nextInt(10) + 1;
		if (i <= 6)
			castPowerPierce(6, 16);
		else
			castPowerSlash(9, 24);
		return false;
	}
}
