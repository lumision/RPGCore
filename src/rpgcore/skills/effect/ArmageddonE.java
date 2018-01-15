package rpgcore.skills.effect;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import rpgcore.external.InstantFirework;
import rpgcore.main.CakeAPI;
import rpgcore.main.RPGEvents;
import rpgcore.player.RPlayer;
import rpgcore.skills.Armageddon;

public class ArmageddonE
{
	public static ArrayList<ArmageddonE> effects = new ArrayList<ArmageddonE>();
	public static ArrayList<ArmageddonE> remove = new ArrayList<ArmageddonE>();
	public static FireworkEffect fe = FireworkEffect.builder().with(Type.BURST).withColor(Color.WHITE).withColor(Color.GRAY).build();
	public ArrayList<LivingEntity> hit = new ArrayList<LivingEntity>();
	public Armageddon skill;
	public Location origin;
	public int tick;
	public ArrayList<Location> offset = new ArrayList<Location>();
	public Random rand = new Random();

	private ArmageddonE(Armageddon skill)
	{
		this.skill = skill;
		this.origin = skill.player.getLocation().clone().add(0, 3, 0);
		ArrayList<LivingEntity> nearby = CakeAPI.getNearbyLivingEntities(skill.player.getLocation(), 16);
		for (LivingEntity e: nearby)
		{
			if (hit.size() > 8)
				break;
			if (e instanceof Player)
				continue;
			if (hit.contains(e))
				continue;
			hit.add(e);
			offset.add(new Location(skill.player.getWorld(), rand.nextInt(5) - 2, rand.nextInt(3) + 4, rand.nextInt(5) - 2));
		}
	}

	public void tick()
	{
		
		if (tick <= 40 && tick % 2 == 0)
			new RPGEvents.PlaySoundEffect(skill.player, Sound.BLOCK_ANVIL_LAND, 0.1F, 0.5F + (tick / 40F)).run();
		for (int index = 0; index < hit.size(); index++)
		{
			LivingEntity e = hit.get(index);
			if (tick == 41)
			{
				for (int i = 0; i < 8; i++)
				{
					Vector movement = e.getLocation().add(offset.get(index)).subtract(e.getLocation()).toVector().multiply(i / 8D);
					Location point = e.getLocation().add(movement);
					new RPGEvents.FireworkTrail(point, 0.2F, 4).run();
				}
			} else if (tick == 43) {
				int damage = RPlayer.varyDamage(skill.getUnvariedDamage());
				new RPGEvents.ApplyDamage(skill.player, e, damage).run();
				new RPGEvents.PlayLightningEffect(e).run();
				new InstantFirework(fe, e.getLocation());
			} else if (tick % 2 == 0) {
				Vector movement = e.getLocation().add(offset.get(index)).subtract(origin).toVector().multiply(tick / 40D);
				Location point = origin.clone().add(movement);
				new RPGEvents.FireworkTrail(point, 0.1F, 1).run();
			}
		}


		tick++;
		if (tick > 43)
			remove.add(this);
	}

	public static void newEffect(Armageddon skill)
	{
		effects.add(new ArmageddonE(skill));
	}

	public static void globalTick()
	{
		for (ArmageddonE e: effects)
			e.tick();
		effects.removeAll(ArmageddonE.remove);
		remove.clear();
	}
}
