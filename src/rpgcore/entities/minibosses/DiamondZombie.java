package rpgcore.entities.minibosses;

import java.util.ArrayList;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import net.minecraft.server.v1_12_R1.EnumParticle;
import rpgcore.entities.mobs.RPGMonster;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGCore;
import rpgcore.main.RPGEvents;

public class DiamondZombie extends RPGMonster
{
	public static double maxHealth = 1500.0D;
	public static final String name = CakeLibrary.recodeColorCodes("&b&lDiamond Zombie&7 Lv. 24");

	public static final int powerPierceDamage = 4;
	public static final int powerPierceDelay = 16;
	
	public static final int powerSlashDamage = 8;
	public static final int powerSlashDelay = 24;
	public final static int powerSlashSize = 7;
	
	public static final int minionSpawnDelay = 20;

	public DiamondZombie(Monster m)
	{
		super(m, true);
		entity.setRemoveWhenFarAway(false);
		entity.setMaxHealth(maxHealth);
		entity.setHealth(maxHealth);
		entity.setCustomName(name);
		entity.setCustomNameVisible(true);

		entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 3));
		entity.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0));
		
		EntityEquipment ee = entity.getEquipment();
		ee.setHelmet(new ItemStack(Material.DIAMOND_HELMET));
		ee.setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
		ee.setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
		ee.setBoots(new ItemStack(Material.DIAMOND_BOOTS));
		ee.setHelmetDropChance(0);
		ee.setChestplateDropChance(0);
		ee.setLeggingsDropChance(0);
		ee.setBootsDropChance(0);
		ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
		sword.addEnchantment(Enchantment.DAMAGE_ALL, 1);
		ee.setItemInMainHand(sword);
		ee.setItemInMainHandDropChance(0);
	}

	@Override
	public boolean tick()
	{
		super.tick();

		if (isDead())
			return true;
		
		if (!entity.getWorld().getName().equals(RPGCore.areaInstanceWorld) && (
				entity.getWorld().getTime() >= 22000 || entity.getWorld().getTime() <= 14000))
		{
			entity.remove();
			return false;
		}
		
		if (castDelay > 0 || target == null)
			return false;
		

		int r = rand.nextInt(10) + 1;
		if (r <= 6)
			castPowerPierce();
		else if (r <= 9)
			castPowerSlash();
		else
			castMinionSpawn();
		return false;
	}

	public void castMinionSpawn()
	{
		castDelay = minionSpawnDelay;
		
		for (int i = 0; i < 4 + rand.nextInt(4); i++)
		{
			Entity e = entity.getWorld().spawnEntity(entity.getLocation().add(rand.nextInt(5) - rand.nextInt(5), 2, rand.nextInt(5) - rand.nextInt(5)), EntityType.ZOMBIE);
			new RPGEvents.ParticleEffect(EnumParticle.FLAME, e, 0.5F, 16).run();
		}
	}

	public void castPowerPierce()
	{
		castDelay = powerPierceDelay;
		
		ArrayList<LivingEntity> hit = new ArrayList<LivingEntity>();
		Vector vector = entity.getLocation().getDirection().normalize();
		int multiplier = 1;
        entity.getWorld().playSound(entity.getEyeLocation(), Sound.ENTITY_GHAST_SHOOT, 0.1F, 1.5F);
		while (multiplier < 7)
		{
			multiplier++;
			Location point = entity.getEyeLocation().add(vector.clone().multiply(multiplier));
			if (!CakeLibrary.getPassableBlocks().contains(point.getBlock().getType()))
				break;
			RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, point, 57), multiplier);
			RPGEvents.scheduleRunnable(new RPGEvents.AOEDetectionAttackWithBlockBreakEffect(hit, point, 1.25D, powerPierceDamage, entity, 169), multiplier);
		}
	}
	
	public void castPowerSlash()
	{
		castDelay = powerSlashDelay;
		
		ArrayList<LivingEntity> hit = new ArrayList<LivingEntity>();
		
		Location horizon = entity.getLocation();
		horizon.setYaw(horizon.getYaw() + 90F);
		if (horizon.getYaw() > 180)
			horizon.setYaw(-180 + (horizon.getYaw() - 180));
		horizon.setPitch(-25F);
		
		Vector slashDirection = horizon.getDirection().normalize().multiply(-1);
		Vector direction = entity.getLocation().getDirection().normalize();
		Location startPointCenter = entity.getEyeLocation();
		
		int multiplier = 1;
		while (multiplier < 6)
		{
			startPointCenter = startPointCenter.add(direction);
			if (CakeLibrary.getNearbyLivingEntitiesExcludePlayers(startPointCenter, 1.0D).size() > 0)
				break;
			multiplier++;
		}
		
		Location startPoint = startPointCenter.clone().add(slashDirection.clone().multiply(-powerSlashSize));
		
		multiplier = 1;
		int delay = 0;
        entity.getWorld().playSound(entity.getEyeLocation(), Sound.ENTITY_GHAST_SHOOT, 0.1F, 1.5F);
		while (multiplier < powerSlashSize * 2)
		{
			multiplier++;
			delay = multiplier / 2;
			Location point = startPoint.clone().add(slashDirection.clone().multiply(multiplier));
			RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, point, 57), delay);
			if (multiplier % 2 == 0)
			RPGEvents.scheduleRunnable(new RPGEvents.AOEDetectionAttackWithBlockBreakEffect(hit, point, 1.25D, powerSlashDamage, entity, 20), delay);
		}
	}
}
