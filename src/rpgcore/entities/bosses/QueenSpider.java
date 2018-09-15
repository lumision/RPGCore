package rpgcore.entities.bosses;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
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
	public static double maxHealth = 1000.0D;
	public static String name = CakeLibrary.recodeColorCodes("&d&lQueen Spider&7 Lv. 12");
	public BossBar bar;

	public QueenSpider(Monster m)
	{
		super(m);
		this.reachDistance = 32.0D;
		entity.setRemoveWhenFarAway(false);
		entity.setMaxHealth(maxHealth);
		entity.setHealth(maxHealth);
		entity.setCustomName(name);
		entity.setCustomNameVisible(true);

		entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 5));
		entity.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0));

		bar = Bukkit.createBossBar(name, BarColor.RED, BarStyle.SOLID, BarFlag.DARKEN_SKY);
	}

	@Override
	public void tick()
	{
		super.tick();

		if (isDead())
		{
			bar.removeAll();
			return;
		}
		if (castDelay > 0 || target == null)
			return;
		bar.setProgress(entity.getHealth() / maxHealth);
		for (Player p: Bukkit.getOnlinePlayers())
			if (p.getLocation().distanceSquared(entity.getLocation()) < Math.pow(32, 2))
				bar.addPlayer(p);
			else
				bar.removePlayer(p);

		int r = random.nextInt(10) + 1;
		if (r <= 7)
		{
			castArcaneBolt();
			castDelay = 30;
		} else if (r <= 8)
		{
			castSlow();
			castDelay = 30;
		} else
		{
			castMinionSpawn();
			castDelay = 30;
		}
	}

	public void castSlow()
	{
		for (Player p: CakeLibrary.getNearbyPlayers(entity.getLocation(), 32))
		{
			new RPGEvents.PlayEffect(Effect.STEP_SOUND, p, 30).run();
			p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 10 * 20, 2));
			RPGCore.msgNoTag(p, "&cYou have been slowed by " + name + "&c.");
		}
	}

	public void castMinionSpawn()
	{
		for (int i = 0; i < 4 + random.nextInt(4); i++)
		{
			Entity e = entity.getWorld().spawnEntity(entity.getLocation().add(random.nextInt(5) - random.nextInt(5), 2, random.nextInt(5) - random.nextInt(5)), EntityType.CAVE_SPIDER);
			new RPGEvents.ParticleEffect(EnumParticle.FLAME, e, 1.0F, 10);
		}
	}

	public void castArcaneBolt()
	{
		ArrayList<LivingEntity> hit = new ArrayList<LivingEntity>();
		Vector vector = entity.getLocation().getDirection().normalize().multiply(0.5F);
		int multiplier = 0;
		entity.getWorld().playSound(entity.getEyeLocation(), Sound.BLOCK_ANVIL_LAND, 0.1F, 1.0F);
		while (multiplier < 32)
		{
			multiplier++;
			Location point = entity.getEyeLocation().add(vector.clone().multiply(multiplier));
			if (!CakeLibrary.getPassableBlocks().contains(point.getBlock().getType()))
			{
				RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, point, point.getBlock().getTypeId()), multiplier);
				break;
			}
			RPGEvents.scheduleRunnable(new RPGEvents.FireworkTrail(point, 0, 1), multiplier / 2);
			RPGEvents.scheduleRunnable(new RPGEvents.PlaySoundEffect(point, Sound.BLOCK_GLASS_BREAK, 0.1F, 1.25F), multiplier / 2);
			RPGEvents.scheduleRunnable(new RPGEvents.AOEDetectionAttackWithBlockBreakEffect(hit, point, 1.25D, 3, entity, 20), multiplier / 2);
		}
	}
}
