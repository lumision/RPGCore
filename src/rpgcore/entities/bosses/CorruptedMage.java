package rpgcore.entities.bosses;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import net.minecraft.server.v1_12_R1.EnumParticle;
import rpgcore.entities.mobs.RPGMonster;
import rpgcore.entities.mobs.RPGMonsterSpawn;
import rpgcore.external.InstantFirework;
import rpgcore.item.RItem;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGCore;
import rpgcore.main.RPGEvents;
import rpgcore.main.RPGEvents.Bind;
import rpgcore.player.RPlayer;
import rpgcore.skills.RPGSkill.SkillEffect;

public class CorruptedMage extends RPGMonster
{
	public static double maxHealth = 70000.0D;
	public static String name = "§3Corrupted Mage §7Lv. 42";

	public static final int arcaneBeamDamage = 6;
	public static final int arcaneBeamDelay = 12;

	public static final int fireballDamage = 8;
	public static final int fireballDelay = 12;
	public static final int fireballDebuffLength = 3 * 20;

	public static final int arcaneStormDamage = 6;
	public static final int arcaneStormDelay = 24;
	public static final int hits = 24;

	public static final int sunfireDamage = 16;
	public static final int sunfireDelay = 72;
	public static final int sunfireBindTicks = 5 * 20;

	public static final int minionSpawnDelay = 24;

	public static final String skullName = "mageblue";

	public int phase = 0;
	public int phaseTicks = 0;
	public Location down;

	public CorruptedMage(Monster entity)
	{
		super(entity, true);
		entity.setMaxHealth(maxHealth);
		entity.setHealth(maxHealth);
		entity.setCustomName(name);
		entity.setCustomNameVisible(true);

		entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
		entity.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0));

		EntityEquipment eq = entity.getEquipment();

		if (RPGCore.heads.containsKey(skullName))
			eq.setHelmet(CakeLibrary.getSkullWithTexture(RPGCore.heads.get(skullName)));
		RItem chestplate = RPGCore.getItemFromDatabase("RobesOfCorruption");
		RItem leggings = RPGCore.getItemFromDatabase("PantsOfCorruption");
		RItem boots = RPGCore.getItemFromDatabase("BootsOfCorruption");
		RItem hand = RPGCore.getItemFromDatabase("RodOfCorruption");

		if (chestplate != null)
			eq.setChestplate(chestplate.createItem());
		if (leggings != null)
			eq.setLeggings(leggings.createItem());
		if (boots != null)
			eq.setBoots(boots.createItem());
		if (hand != null)
			eq.setItemInMainHand(hand.createItem());

		eq.setHelmetDropChance(0.0F);
		eq.setItemInMainHandDropChance(0);
		eq.setChestplateDropChance(0);
		eq.setLeggingsDropChance(0);
		eq.setBootsDropChance(0);
	}

	@Override
	public boolean tick()
	{
		super.tick();
		if (isDead())
			return true;
		switch (phase)
		{
		case 0:
			phaseTicks = 0;
			if (entity.getHealth() < entity.getMaxHealth() * 0.75D)
			{
				Bind.bindTarget(entity, 10 * 20, false);
				down = entity.getLocation();
				down.setPitch(75);
				phase++;
			}
			break;
		case 1:
			phaseTicks++;
			entity.teleport(down);
			new RPGEvents.ParticleEffect(EnumParticle.BLOCK_DUST, entity.getEyeLocation(), 0.4F, 16, 0, 152).run();
			if (phaseTicks > 10 * 20)
				phase++;
			break;
		case 2:
			phaseTicks = 0;
			if (entity.getHealth() < entity.getMaxHealth() * 0.25D)
			{
				Bind.bindTarget(entity, 10 * 20, false);
				down = entity.getLocation();
				down.setPitch(75);
				phase++;
			}
			break;
		case 3:
			phaseTicks++;
			entity.teleport(down);
			new RPGEvents.ParticleEffect(EnumParticle.BLOCK_DUST, entity.getEyeLocation(), 0.4F, 16, 0, 152).run();
			if (phaseTicks > 10 * 20)
				phase++;
			break;
		case 4:
			break;
		}
		if (castDelay > 0 || target == null)
			return false;

		int i = rand.nextInt(20) + 1;
		if (i <= 8)
			castArcaneBeam();
		else if (i <= 14)
			castFireball();
		else if (i <= 17)
			new MobArcaneStormE(this);
		else if (i <= 19)
			new MobSunfireE(this);
		else
			castMinionSpawn();
		return false;
	}

	public void castArcaneBeam()
	{
		castDelay = arcaneBeamDelay;

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
			RPGEvents.scheduleRunnable(new RPGEvents.FireworkTrail(point, 0.1F, 3), multiplier);
			RPGEvents.scheduleRunnable(new RPGEvents.PlaySoundEffect(point, Sound.BLOCK_GLASS_BREAK, 0.1F, 1.25F), multiplier);
			RPGEvents.scheduleRunnable(new RPGEvents.AOEDetectionAttackWithBlockBreakEffect(hit, point, 1.25D, arcaneBeamDamage, entity, 20), multiplier);
		}
	}

	public void castFireball()
	{
		castDelay = fireballDelay;
		ArrayList<LivingEntity> hit = new ArrayList<LivingEntity>();
		Vector vector = entity.getLocation().getDirection().normalize().multiply(0.75D);
		int multiplier = 0;
		entity.getWorld().playSound(entity.getEyeLocation(), Sound.ENTITY_GHAST_SHOOT, 0.2F, 0.8F);
		while (multiplier < 20)
		{
			multiplier++;
			Location point = entity.getEyeLocation().add(vector.clone().multiply(multiplier));
			if (!CakeLibrary.getPassableBlocks().contains(point.getBlock().getType()))
			{
				RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, point, point.getBlock().getTypeId()), multiplier);
				break;
			}
			RPGEvents.scheduleRunnable(new RPGEvents.ParticleEffect(EnumParticle.FLAME, point, 0.1F, 4), multiplier);
			RPGEvents.scheduleRunnable(new RPGEvents.PlaySoundEffect(point, Sound.BLOCK_FURNACE_FIRE_CRACKLE, 0.4F, 1.2F), multiplier);
			RPGEvents.scheduleRunnable(new RPGEvents.AOEDetectionCustom(hit, point, 1.25D, entity, new Callable<Void>()
			{
				@Override
				public Void call() throws Exception {
					LivingEntity e = RPGEvents.customHit;
					int damage = RPlayer.varyDamage(fireballDamage);
					new RPGEvents.ApplyDamage(entity, e, damage).run();
					new RPGEvents.PlayEffect(Effect.STEP_SOUND, e, 11).run();
					e.setFireTicks(fireballDebuffLength);
					return null;
				}

			}), multiplier);
		}
	}

	public static class MobArcaneStormE extends SkillEffect
	{
		public RPGMonster mob;
		public MobArcaneStormE(RPGMonster mob)
		{
			super(null);
			this.mob = mob;
		}
		
		@Override
		public boolean tick() 
		{
			if (mob.isDead())
				return true;
			if (mob.target == null)
				return true;
			try
			{
				mob.castDelay = arcaneStormDelay;
				Location l = mob.target.getLocation();
				if (tick % 2 == 0 && tick < 20)
					RPGEvents.scheduleRunnable(new RPGEvents.PlaySoundEffect(l, Sound.ENTITY_CREEPER_DEATH, 2.0F, 0.5F + (tick / 20.0F)), tick);
				else if (tick == 20)
				{
					for (int i = 0; i < hits; i++)
					{
						ArrayList<LivingEntity> hit = new ArrayList<LivingEntity>();
						Vector offset = new Vector(3 - rand.nextInt(7), 8 + rand.nextInt(3), 3 - rand.nextInt(7));
						Location start = l.clone().add(offset);
						Vector vector = new Vector(0, -1, 0).normalize().multiply(1.0F);
						int multiplier = 0;
						int delay = 0;
						while (multiplier < 16)
						{
							multiplier++;
							delay = multiplier + i + 20;
							Location point = start.clone().add(vector.clone().multiply(multiplier));
							if (!CakeLibrary.getPassableBlocks().contains(point.getBlock().getType()))
							{
								RPGEvents.scheduleRunnable(new RPGEvents.PlaySoundEffect(point, Sound.BLOCK_FIRE_EXTINGUISH, 0.1F, 1.0F), delay);
								RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, point, point.getBlock().getTypeId()), delay);
								break;
							}
							RPGEvents.scheduleRunnable(new RPGEvents.ParticleEffect(EnumParticle.CRIT_MAGIC, point, 0.1F, 3), delay);
							RPGEvents.scheduleRunnable(new RPGEvents.AOEDetectionAttackWithBlockBreakEffect(hit, point, 1.25D, arcaneStormDamage, mob.entity, 57), delay);
						}
					}
				}
			} catch (Exception e)
			{
				return true;
			}
			tick++;
			if (tick > 20)
				return true;
			return false;
		}
		
	}

	public void castMinionSpawn()
	{
		castDelay = minionSpawnDelay;

		for (int i = 0; i < 4 + rand.nextInt(4); i++)
		{
			int type = rand.nextInt(6);
			RPGMonsterSpawn spawn = RPGMonsterSpawn.getRPGMonsterSpawn(type <= 2 ? "ReinforcedSkeleton" : type <= 4 ? "MageZombie" : "SorcererZombie");
			spawn.spawnMonster(entity.getLocation().add(rand.nextInt(5) - rand.nextInt(5), 2, rand.nextInt(5) - rand.nextInt(5)));
		}
	}

	public static class MobSunfireE extends SkillEffect
	{
		public static FireworkEffect fe = FireworkEffect.builder().with(Type.BURST).withColor(Color.WHITE).withColor(Color.RED).withColor(Color.YELLOW).build();
		public ArrayList<LivingEntity> hit = new ArrayList<LivingEntity>();
		public Location origin;
		public Location bindLocation;
		public ArrayList<Location> offset = new ArrayList<Location>();
		public RPGMonster mob;

		public MobSunfireE(RPGMonster mob)
		{
			super(null);
			this.mob = mob;
			ArrayList<LivingEntity> nearby = CakeLibrary.getNearbyLivingEntities(mob.entity.getLocation(), 32);
			this.origin = mob.entity.getLocation().clone().add(0, 5, 0);
			for (LivingEntity e: nearby)
			{
				if (hit.size() > 16)
					break;
				if (e instanceof Monster)
					continue;
				if (hit.contains(e))
					continue;
				hit.add(e);
				offset.add(new Location(mob.entity.getWorld(), rand.nextInt(5) - 2, rand.nextInt(3) + 8, rand.nextInt(5) - 2));
			}
			if (hit.size() <= 0)
				return;
			Bind.bindTarget(mob.entity, sunfireDelay, false);
		}

		@Override
		public boolean tick()
		{
			if (mob.isDead())
				return true;
			if (mob.target == null)
				return true;
			if (tick <= 60 && tick % 2 == 0)
			{
				new RPGEvents.PlaySoundEffect(mob.entity, Sound.BLOCK_ANVIL_LAND, 0.2F, 0.5F + (tick / 60F)).run();
				new RPGEvents.FireworkTrail(origin, tick / 60.0F, tick).run();
			}
			else if (tick == 61)
			{
				int damage;
				for (int index = 0; index < hit.size(); index++)
				{
					LivingEntity e = hit.get(index);
					if (e.isDead() || e.getHealth() <= 0)
						continue;
					Location l = e.getLocation();
					damage = sunfireDamage;
					Location line = l.clone().subtract(origin);
					Vector vector = line.toVector().normalize().multiply(0.5D);
					int length = (int) (line.getX() / vector.getX());
					int multiplier = 0;
					boolean cancel = false;
					while (multiplier < length)
					{
						multiplier++;
						Location point = origin.clone().add(vector.clone().multiply(multiplier));
						if (!CakeLibrary.getPassableBlocks().contains(point.getBlock().getType()))
						{
							impact(point);
							new RPGEvents.PlayEffect(Effect.STEP_SOUND, point, point.getBlock().getTypeId()).run();
							cancel = true;
							break;
						}
						new RPGEvents.FireworkTrail(point, 0.1F, 3).run();
						//new RPGEvents.ParticleEffect(EnumParticle.FLAME, point, 0.1F, 3).run();
					}
					if (!cancel)
					{
						impact(l);
						new RPGEvents.ApplyDamage(mob.entity, e, damage).run();
						Bind.bindTarget(e, sunfireBindTicks, true);
					}
				}
			}


			tick++;
			if (tick > 61)
				return true;
			return false;
		}
		
		public void impact(Location l)
		{
			new InstantFirework(fe, l);
			l.getWorld().playSound(l, Sound.ENTITY_LIGHTNING_IMPACT, 0.2F, 1.0F + rand.nextFloat() / 4.0F);
			l.getWorld().playSound(l, Sound.ENTITY_LIGHTNING_THUNDER, 0.2F, 1.0F + rand.nextFloat() / 4.0F);
		}
	}
}
