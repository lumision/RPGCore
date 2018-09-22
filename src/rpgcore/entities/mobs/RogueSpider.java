package rpgcore.entities.mobs;

import java.util.ArrayList;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGEvents;

public class RogueSpider extends RPGMonster
{
	public static double maxHealth = 450.0D;
	public static String name = "§3Rogue Spider §7Lv. 20";
	
	public static final int dashDelay = 8;

	public static final int shadowStabDamage = 3;
	public static final int shadowStabDelay = 16;

	public static final int kunaiDamage = 6;
	public static final int kunaiDelay = 24;

	public RogueSpider(Monster entity)
	{
		super(entity, false);
		entity.setMaxHealth(maxHealth);
		entity.setHealth(maxHealth);
		entity.setCustomName(name);
		entity.setCustomNameVisible(true);

		entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 4));
		entity.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 2));
		entity.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0));
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
		if (i <= 3 && target.getLocation().distanceSquared(entity.getLocation()) > 25)
			castDash();
		else if (i <= 7 && target.getLocation().distanceSquared(entity.getLocation()) <= 25)
			castShadowStab();
		else
			castKunai();
		return false;
	}

	public void castDash()
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
		castDelay = dashDelay;
	}
	
	public void castShadowStab()
	{
		castDelay = shadowStabDelay;
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
				RPGEvents.scheduleRunnable(new RPGEvents.ApplyDamage(entity, e, shadowStabDamage), 0);
				RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, e, 20), 0);
			}
		}
	}

	public void castKunai()
	{
		castDelay = kunaiDelay;
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
			RPGEvents.scheduleRunnable(new RPGEvents.AOEDetectionAttackWithBlockBreakEffect(hit, point, 0.75D, kunaiDamage, entity, 20), multiplier / 3);
		}
	}
}
