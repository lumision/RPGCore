package rpgcore.entities.mobs;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Monster;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SorcererZombie extends RPGMonster
{
	public static double maxHealth = 400.0D;
	public static String name = "§3Sorcerer Zombie §7Lv. 21";
	
	public SorcererZombie(Monster entity)
	{
		super(entity, false);
		entity.setMaxHealth(maxHealth);
		entity.setHealth(maxHealth);
		entity.setCustomName(name);
		entity.setCustomNameVisible(true);

		entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
		entity.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0));

		ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
		ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
		ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
		ItemStack hand = new ItemStack(Material.STICK);
		hand.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);

		EntityEquipment eq = entity.getEquipment();
		eq.setItemInMainHand(hand);
		eq.setItemInMainHandDropChance(0);
		eq.setChestplate(chestplate);
		eq.setChestplateDropChance(0);
		eq.setLeggings(leggings);
		eq.setLeggingsDropChance(0);
		eq.setBoots(boots);
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
		if (i <= 4)
			castArcaneBolt(4, 24);
		else if (i <= 7)
			castIceBolt(5, 24);
		else
			castPoisonBolt(5, 24);
		return false;
	}
}
