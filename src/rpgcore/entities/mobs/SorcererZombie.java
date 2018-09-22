package rpgcore.entities.mobs;

import java.util.ArrayList;
import java.util.concurrent.Callable;

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
import rpgcore.skills.IceBolt;
import rpgcore.skills.PoisonBolt;

public class SorcererZombie extends RPGMonster
{
	public static double maxHealth = 400.0D;
	public static String name = "§eSorcerer Zombie §7Lv. 21";
	
	public static final int arcaneBoltDamage = 4;
	public static final int arcaneBoltDelay = 24;
	
	public static final int iceBoltDamage = 5;
	public static final int iceBoltDelay = 24;
	
	public static final int poisonBoltDamage = 5;
	public static final int poisonBoltDelay = 24;

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
			castArcaneBolt();
		else if (i <= 7)
			castIceBolt();
		else
			castPoisonBolt();
		return false;
	}

	public void castArcaneBolt()
	{
		castDelay = arcaneBoltDelay;
		
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
			RPGEvents.scheduleRunnable(new RPGEvents.AOEDetectionAttackWithBlockBreakEffect(hit, point, 1.25D, arcaneBoltDamage, entity, 20), multiplier);
		}
	}

	public void castIceBolt()
	{
		castDelay = iceBoltDelay;
		
		ArrayList<LivingEntity> hit = new ArrayList<LivingEntity>();
		Vector vector = entity.getLocation().getDirection().normalize().multiply(0.5D);
		int multiplier = 0;
		entity.getWorld().playSound(entity.getEyeLocation(), Sound.BLOCK_ANVIL_LAND, 0.05F, 1.0F);
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
			RPGEvents.scheduleRunnable(new RPGEvents.PlaySoundEffect(point, Sound.BLOCK_GLASS_BREAK, 0.05F, 1.25F), multiplier);
			RPGEvents.scheduleRunnable(new RPGEvents.AOEDetectionCustom(hit, point, 1.25D, entity, new Callable<Void>()
			{
				@Override
				public Void call() throws Exception {
					LivingEntity e = RPGEvents.customHit;
					new RPGEvents.ApplyDamage(entity, e, iceBoltDamage).run();
					new RPGEvents.PlayEffect(Effect.STEP_SOUND, e, 79).run();
					CakeLibrary.addPotionEffectIfBetterOrEquivalent(e, new PotionEffect(PotionEffectType.SLOW, 
							IceBolt.debuffLength, IceBolt.debuffLevel));
					return null;
				}

			}), multiplier);
		}
	}

	public void castPoisonBolt()
	{
		castDelay = poisonBoltDelay;
		
		ArrayList<LivingEntity> hit = new ArrayList<LivingEntity>();
		Vector vector = entity.getLocation().getDirection().normalize().multiply(0.5D);
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
			RPGEvents.scheduleRunnable(new RPGEvents.PlaySoundEffect(point, Sound.BLOCK_SLIME_BREAK, 0.1F, 1.25F), multiplier);
			RPGEvents.scheduleRunnable(new RPGEvents.AOEDetectionCustom(hit, point, 1.25D, entity, new Callable<Void>()
			{
				@Override
				public Void call() throws Exception {
					LivingEntity e = RPGEvents.customHit;
					new RPGEvents.ApplyDamage(entity, e, poisonBoltDamage).run();
					new RPGEvents.PlayEffect(Effect.STEP_SOUND, e, 165).run();
					new RPGEvents.DamageOverTime(PoisonBolt.debuffLength, 20, 1, entity, e);
					return null;
				}

			}), multiplier);
		}
	}
}
