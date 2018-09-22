package rpgcore.entities.mobs;

import java.util.ArrayList;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGEvents;

public class MageZombie extends RPGMonster
{
	public static double maxHealth = 90.0D;
	public static String name = "§cMage Zombie §7Lv. 9";

	public MageZombie(Monster entity)
	{
		super(entity, false);
		entity.setMaxHealth(maxHealth);
		entity.setHealth(maxHealth);
		entity.setCustomName(name);
		entity.setCustomNameVisible(true);

		entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
		entity.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0));
		
		ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
		ItemStack hand = new ItemStack(Material.STICK);
		hand.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
		
		EntityEquipment eq = entity.getEquipment();
		eq.setItemInMainHand(hand);
		eq.setItemInMainHandDropChance(0);
		eq.setHelmet(helmet);
		eq.setHelmetDropChance(0);
	}

	@Override
	public boolean tick()
	{
		super.tick();
		if (isDead())
			return true;
		if (castDelay > 0 || target == null)
			return false;
		castArcaneBolt();
		castDelay = 60;
		return false;
	}

	public void castArcaneBolt()
	{
		ArrayList<LivingEntity> hit = new ArrayList<LivingEntity>();
		Vector vector = entity.getLocation().getDirection().normalize().multiply(0.5F);
		int multiplier = 0;
		entity.getWorld().playSound(entity.getEyeLocation(), Sound.BLOCK_ANVIL_LAND, 0.1F, 1.0F);
		while (multiplier < 30)
		{
			multiplier++;
			Location point = entity.getEyeLocation().add(vector.clone().multiply(multiplier));
			if (!CakeLibrary.getPassableBlocks().contains(point.getBlock().getType()))
			{
				RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, point, point.getBlock().getTypeId()), multiplier);
				break;
			}
			RPGEvents.scheduleRunnable(new RPGEvents.FireworkTrail(point, 0, 1), multiplier);
			RPGEvents.scheduleRunnable(new RPGEvents.PlaySoundEffect(point, Sound.BLOCK_GLASS_BREAK, 0.1F, 1.25F), multiplier);
			RPGEvents.scheduleRunnable(new RPGEvents.AOEDetectionAttackWithBlockBreakEffect(hit, point, 1.25D, 4, entity, 20), multiplier);
		}
	}
}
