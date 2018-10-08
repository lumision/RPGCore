package rpgcore.entities.mobs;

import org.bukkit.Material;
import org.bukkit.entity.Monster;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import rpgcore.item.RItem;
import rpgcore.main.RPGCore;

public class SageSkeleton extends RPGMonster
{
	public static double maxHealth = 900.0D;
	public static String name = "§9Sage Skeleton §7Lv. 30";

	public SageSkeleton(Monster entity)
	{
		super(entity, false);
		entity.setMaxHealth(maxHealth);
		entity.setHealth(maxHealth);
		entity.setCustomName(name);
		entity.setCustomNameVisible(true);

		entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
		entity.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0));

		RItem rBoots = RPGCore.getItemFromDatabase("SageSkeletonBoots");
		RItem rChestplate = RPGCore.getItemFromDatabase("SageSkeletonRobe");
		RItem rLeggings = RPGCore.getItemFromDatabase("SageSkeletonPants");
		RItem rHelmet = RPGCore.getItemFromDatabase("SageSkeletonHood");
		RItem rHand = RPGCore.getItemFromDatabase("SageSkeletonSceptre");
		
		ItemStack boots = rBoots != null ? rBoots.createItem() : new ItemStack(Material.LEATHER_BOOTS);
		ItemStack chestplate = rChestplate != null ? rChestplate.createItem() : new ItemStack(Material.LEATHER_CHESTPLATE);
		ItemStack leggings = rLeggings != null ? rChestplate.createItem() : new ItemStack(Material.LEATHER_LEGGINGS);
		ItemStack helmet = rHelmet != null ? rChestplate.createItem() : new ItemStack(Material.LEATHER_HELMET);
		ItemStack hand = rHand != null ? rChestplate.createItem() : new ItemStack(Material.STICK);

		EntityEquipment eq = entity.getEquipment();
		eq.setItemInMainHand(hand);
		eq.setItemInMainHandDropChance(0);
		eq.setHelmet(helmet);
		eq.setHelmetDropChance(0);
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
		if (i <= 6)
			castArcaneBeam(8, 16);
		return false;
	}
}
