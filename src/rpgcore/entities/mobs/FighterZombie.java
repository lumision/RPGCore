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

public class FighterZombie extends RPGMonster
{
	public static double maxHealth = 600.0D;
	public static String name = "§cWarrior Zombie §7Lv. 22";

	public static final int powerPierceDamage = 5;
	public static final int powerPierceDelay = 16;
	
	public static final int powerSlashDamage = 7;
	public static final int powerSlashDelay = 24;
	public final static int powerSlashDelaySize = 7;

	public FighterZombie(Monster entity)
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
		eq.setHelmet(new ItemStack(Material.IRON_HELMET));
		eq.setHelmetDropChance(0);
		eq.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
		eq.setChestplateDropChance(0);
		eq.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
		eq.setLeggingsDropChance(0);
		eq.setBoots(new ItemStack(Material.IRON_BOOTS));
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
			castPowerPierce();
		else
			castPowerSlash();
		return false;
	}

	public void castPowerPierce()
	{
		if (target.getLocation().distanceSquared(entity.getLocation()) > 25)
			return;
		castDelay = powerPierceDelay;
		ArrayList<LivingEntity> hit = new ArrayList<LivingEntity>();
		Vector vector = entity.getLocation().getDirection().normalize();
		int multiplier = 1;
		entity.getWorld().playSound(entity.getEyeLocation(), Sound.ENTITY_GHAST_SHOOT, 0.2F, 1.5F);
		while (multiplier < 7)
		{
			multiplier++;
			Location point = entity.getEyeLocation().add(vector.clone().multiply(multiplier));
			if (!CakeLibrary.getPassableBlocks().contains(point.getBlock().getType()))
				break;
			RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, point, 20), multiplier);
			RPGEvents.scheduleRunnable(new RPGEvents.AOEDetectionAttackWithBlockBreakEffect(hit, point, 1.25D, 4, entity, 20), multiplier);
		}
	}
	
	public void castPowerSlash()
	{
		if (target.getLocation().distanceSquared(entity.getLocation()) > 25)
			return;
		castDelay = powerSlashDelay;
		ArrayList<LivingEntity> hit = new ArrayList<LivingEntity>();
		
		Location horizon = entity.getLocation();
		horizon.setYaw(horizon.getYaw() + 90F);
		horizon.setPitch(-25F);
		
		Vector slashDirection = horizon.getDirection().normalize().multiply(-1);
		Vector direction = entity.getLocation().getDirection().normalize();
		Location startPointCenter = entity.getEyeLocation();
		
		int multiplier = 1;
		while (multiplier < 6)
		{
			startPointCenter = startPointCenter.add(direction);
			if (CakeLibrary.getNearbyPlayers(startPointCenter, 1.0D).size() > 0)
				break;
			multiplier++;
		}
		
		Location startPoint = startPointCenter.clone().add(slashDirection.clone().multiply(-powerSlashDelaySize));
		
		multiplier = 1;
		int delay = 0;
        entity.getWorld().playSound(entity.getEyeLocation(), Sound.ENTITY_GHAST_SHOOT, 0.1F, 1.5F);
		while (multiplier < powerSlashDelaySize * 2)
		{
			multiplier++;
			delay = multiplier / 2;
			Location point = startPoint.clone().add(slashDirection.clone().multiply(multiplier));
			RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, point, 20), delay);
			if (multiplier % 2 == 0)
			RPGEvents.scheduleRunnable(new RPGEvents.AOEDetectionAttackWithBlockBreakEffect(hit, point, 1.25D, powerSlashDamage, entity, 20), delay);
		}
	}
}
