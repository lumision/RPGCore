package rpgcore.entities.minibosses;

import java.util.ArrayList;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import net.minecraft.server.v1_12_R1.EnumParticle;
import rpgcore.entities.mobs.RPGMonster;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGCore;
import rpgcore.main.RPGEvents;

public class QueenSpider extends RPGMonster
{
	public static double maxHealth = 1200.0D;
	public static String name = CakeLibrary.recodeColorCodes("&d&lQueen Spider&7 Lv. 26");
	
	public static final int arcaneBeamDamage = 4;
	public static final int arcaneBeamDelay = 16;

	public static final int slowDelay = 16;
	
	public static final int minionSpawnDelay = 24;

	public QueenSpider(Monster m)
	{
		super(m, true);
		entity.setRemoveWhenFarAway(false);
		entity.setMaxHealth(maxHealth);
		entity.setHealth(maxHealth);
		entity.setCustomName(name);
		entity.setCustomNameVisible(true);

		entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
		entity.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0));
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
			return true;
		}
		
		if (castDelay > 0 || target == null)
			return false;
		

		int r = rand.nextInt(10) + 1;
		if (r <= 7)
			castArcaneBeam();
		else if (r <= 8)
			castSlow();
		else
			castMinionSpawn();
		return false;
	}

	public void castSlow()
	{
		castDelay = slowDelay;
		for (Player p: CakeLibrary.getNearbyPlayers(entity.getLocation(), 32))
		{
			new RPGEvents.PlayEffect(Effect.STEP_SOUND, p, 30).run();
			p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 10 * 20, 2));
			RPGCore.msgNoTag(p, "&cYou have been slowed by " + name + "&c.");
		}
	}

	public void castMinionSpawn()
	{
		castDelay = minionSpawnDelay;
		for (int i = 0; i < 4 + rand.nextInt(4); i++)
		{
			Entity e = entity.getWorld().spawnEntity(entity.getLocation().add(rand.nextInt(5) - rand.nextInt(5), 2, rand.nextInt(5) - rand.nextInt(5)), EntityType.CAVE_SPIDER);
			new RPGEvents.ParticleEffect(EnumParticle.FLAME, e, 0.5F, 16).run();
		}
	}

	public void castArcaneBeam()
	{
		castDelay = arcaneBeamDelay;
		
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
			RPGEvents.scheduleRunnable(new RPGEvents.AOEDetectionAttackWithBlockBreakEffect(hit, point, 1.25D, arcaneBeamDamage, entity, 20), multiplier);
		}
	}
}
