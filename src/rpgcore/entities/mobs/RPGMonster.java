package rpgcore.entities.mobs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.Callable;

import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGEvents;
import rpgcore.skills.IceBolt;
import rpgcore.skills.PoisonBolt;

public abstract class RPGMonster 
{
	public static ArrayList<RPGMonster> entities = new ArrayList<RPGMonster>();

	public boolean isBoss;
	public int reachDistance;
	public int reachDistanceLoseSquared;
	public Monster entity;
	public Player target;
	public int castDelay;
	public double aliveTicks;
	public boolean bound;
	public static Random rand = new Random();
	public static HashMap<String, Class<? extends RPGMonster>> mobList = new HashMap<String, Class<? extends RPGMonster>>();
	
	public RPGMonster(Monster entity, boolean isBoss)
	{
		this.entity = entity;
		this.isBoss = isBoss;
		this.reachDistance = 16;
		this.reachDistanceLoseSquared = isBoss ? 32 * 32 : 16 * 16;
		entities.add(this);
	}

	public static RPGMonster getRPGMob(int entityID)
	{
		for (RPGMonster ce: entities)
			if (ce.entity.getEntityId() == entityID)
				return ce;
		return null;
	}

	public boolean isDead()
	{
		if (entity == null)
			return true;
		if (entity.isDead())
			return true;
		if (entity.getHealth() <= 0)
			return true;
		return false;
	}

	public boolean tick()
	{
		if (isDead())
			return true;
		if (bound)
		{
			this.castDelay = 1;
			return false;
		}
		if (aliveTicks % 10 == 0)
			findTarget();
		if (this.castDelay > 0)
			this.castDelay--;
		this.aliveTicks++;
		if (target != null)
		{
			if (target.getHealth() <= 0 || target.isDead())
			{
				target = null;
				return false;
			}
			if (target.getWorld() != entity.getWorld())
			{
				target = null;
				return false;
			}
			if (target.getLocation().distanceSquared(entity.getLocation()) > reachDistanceLoseSquared)
			{
				target = null;
				return false;
			}
			if (!target.getGameMode().equals(GameMode.SURVIVAL))
			{
				target = null;
				return false;
			}
			entity.setTarget(target);
		}
		return false;
	}

	public void findTarget()
	{
		if (target != null)
			return;
		ArrayList<Player> players = CakeLibrary.getNearbyPlayers(entity.getLocation(), reachDistance);
		if (players.size() < 1)
			return;
		for (Player player: players)
		{
			if (player.getGameMode().equals(GameMode.SURVIVAL) && player.getHealth() > 0 && !player.isDead())
			{
				target = player;
				entity.setTarget(player);
				return;
			}
		}
	}
	
	//Skills

	public void castArcaneBeam(int damage, int castDelay)
	{
		ArrayList<LivingEntity> hit = new ArrayList<LivingEntity>();
		Vector vector = entity.getLocation().getDirection().normalize().multiply(0.75D);
		int multiplier = 0;
        entity.getWorld().playSound(entity.getEyeLocation(), Sound.BLOCK_ANVIL_LAND, 0.05F, 1.0F);
		while (multiplier < 20)
		{
			multiplier++;
			Location point = entity.getEyeLocation().add(vector.clone().multiply(multiplier));
			if (!CakeLibrary.getPassableBlocks().contains(point.getBlock().getType()))
			{
				RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, point, point.getBlock().getTypeId()), multiplier);
				break;
			}
			RPGEvents.scheduleRunnable(new RPGEvents.FireworkTrail(point, 0.1F, 3), multiplier);
			RPGEvents.scheduleRunnable(new RPGEvents.PlaySoundEffect(point, Sound.BLOCK_GLASS_BREAK, 0.05F, 1.25F), multiplier);
			RPGEvents.scheduleRunnable(new RPGEvents.AOEDetectionAttackWithBlockBreakEffect(hit, point, 1.25D, damage, entity, 20), multiplier);
		}
		this.castDelay = castDelay;
	}

	public void castIceBolt(int damage, int castDelay)
	{
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
					new RPGEvents.ApplyDamage(entity, e, damage).run();
					new RPGEvents.PlayEffect(Effect.STEP_SOUND, e, 79).run();
					CakeLibrary.addPotionEffectIfBetterOrEquivalent(e, new PotionEffect(PotionEffectType.SLOW, 
							IceBolt.debuffLength, IceBolt.debuffLevel));
					return null;
				}

			}), multiplier);
		}
		this.castDelay = castDelay;
	}

	public void castPoisonBolt(int damage, int castDelay)
	{
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
					new RPGEvents.ApplyDamage(entity, e, damage).run();
					new RPGEvents.PlayEffect(Effect.STEP_SOUND, e, 165).run();
					new RPGEvents.DamageOverTime(PoisonBolt.debuffLength, 20, 1, entity, e);
					return null;
				}

			}), multiplier);
		}
		this.castDelay = castDelay;
	}

	public void castPowerPierce(int damage, int castDelay)
	{
		if (target.getLocation().distanceSquared(entity.getLocation()) > 25)
			return;
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
			RPGEvents.scheduleRunnable(new RPGEvents.AOEDetectionAttackWithBlockBreakEffect(hit, point, 1.25D, damage, entity, 20), multiplier);
		}
		this.castDelay = castDelay;
	}
	
	public void castPowerSlash(int damage, int castDelay)
	{
		if (target.getLocation().distanceSquared(entity.getLocation()) > 25)
			return;
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
		
		Location startPoint = startPointCenter.clone().add(slashDirection.clone().multiply(-7));
		
		multiplier = 1;
		int delay = 0;
        entity.getWorld().playSound(entity.getEyeLocation(), Sound.ENTITY_GHAST_SHOOT, 0.1F, 1.5F);
		while (multiplier < 7 * 2)
		{
			multiplier++;
			delay = multiplier / 2;
			Location point = startPoint.clone().add(slashDirection.clone().multiply(multiplier));
			RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, point, 20), delay);
			if (multiplier % 2 == 0)
			RPGEvents.scheduleRunnable(new RPGEvents.AOEDetectionAttackWithBlockBreakEffect(hit, point, 1.25D, damage, entity, 20), delay);
		}
		this.castDelay = castDelay;
	}

	public void castArcaneBolt(int damage, int castDelay)
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
			RPGEvents.scheduleRunnable(new RPGEvents.AOEDetectionAttackWithBlockBreakEffect(hit, point, 1.25D, damage, entity, 20), multiplier);
		}
		this.castDelay = castDelay;
	}

	public void castDash(int castDelay)
	{
		Location b = null;
		int length = 8;
			b = entity.getTargetBlock(CakeLibrary.getPassableBlocks(), length).getLocation();
		if (b == null)
			return;
		Location b1 = b.clone().add(0, 1, 0);
		if (!CakeLibrary.getPassableBlocks().contains(b1.getBlock().getType()))
			return;
		int yDiff = 0;
		for (int y = b.getBlockY(); y > 0; y--)
		{
			b.setY(y);
			yDiff++;
			if (!CakeLibrary.getPassableBlocks().contains(b.getBlock().getType()))
				break;
		}
		if (yDiff > 5)
			return;
		Location start = entity.getLocation();
		Location teleport = b.clone().add(0.5D, 1, 0.5D);
		teleport.setYaw(start.getYaw());
		teleport.setPitch(start.getPitch());
		teleport.getWorld().playEffect(teleport, Effect.STEP_SOUND, 20);
		teleport.getWorld().playEffect(teleport.clone().add(0, 1, 0), Effect.STEP_SOUND, 20);
		teleport.getWorld().playEffect(start, Effect.STEP_SOUND, 20);
		teleport.getWorld().playEffect(start.clone().add(0, 1, 0), Effect.STEP_SOUND, 20);
		entity.teleport(teleport);
		this.castDelay = castDelay;
	}
	
	public void castShadowStab(int damage, int castDelay)
	{
		ArrayList<LivingEntity> hit = new ArrayList<LivingEntity>();
		Vector vector = entity.getLocation().getDirection().normalize().multiply(0.5D);
		int multiplier = 0;
        entity.getWorld().playSound(entity.getEyeLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.2F, 1.0F);
		while (multiplier < 10)
		{
			multiplier++;
			Location point = entity.getEyeLocation().add(vector.clone().multiply(multiplier));
			if (!CakeLibrary.getPassableBlocks().contains(point.getBlock().getType()))
				break;
			RPGEvents.scheduleRunnable(new RPGEvents.FireworkTrail(point, 0, 1), 0);
			RPGEvents.scheduleRunnable(new RPGEvents.PlaySoundEffect(point, Sound.BLOCK_GLASS_BREAK, 0.05F, 1.25F), 0);
			for (LivingEntity e: CakeLibrary.getNearbyLivingEntities(point, 0.75D))
			{
				if (e instanceof Monster)
					continue;
				if (hit.contains(e))
					continue;
				hit.add(e);
				RPGEvents.scheduleRunnable(new RPGEvents.ApplyDamage(entity, e, damage), 0);
				RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, e, 20), 0);
			}
		}
		this.castDelay = castDelay;
	}

	public void castKunai(int damage, int castDelay)
	{
		ArrayList<LivingEntity> hit = new ArrayList<LivingEntity>();
		Vector vector = entity.getLocation().getDirection().normalize().multiply(0.5D);
		int multiplier = 0;
        entity.getWorld().playSound(entity.getEyeLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.2F, 1.0F);
		while (multiplier < 24)
		{
			multiplier++;
			Location point = entity.getEyeLocation().add(vector.clone().multiply(multiplier));
			if (!CakeLibrary.getPassableBlocks().contains(point.getBlock().getType()))
			{
				RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, point, point.getBlock().getTypeId()), multiplier);
				break;
			}
			RPGEvents.scheduleRunnable(new RPGEvents.FireworkTrail(point, 0, 1), multiplier / 3);
			RPGEvents.scheduleRunnable(new RPGEvents.PlaySoundEffect(point, Sound.BLOCK_GLASS_BREAK, 0.1F, 1.25F), multiplier/ 3);
			RPGEvents.scheduleRunnable(new RPGEvents.AOEDetectionAttackWithBlockBreakEffect(hit, point, 0.75D, damage, entity, 20), multiplier / 3);
		}
		this.castDelay = castDelay;
	}
}
