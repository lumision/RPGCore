package rpgcore.skills.effect;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import net.minecraft.server.v1_12_R1.EnumParticle;
import rpgcore.external.InstantFirework;
import rpgcore.main.RPGEvents;
import rpgcore.player.RPlayer;
import rpgcore.skills.HellfireTerminus;

public class HellfireTerminusE
{
	public static ArrayList<HellfireTerminusE> effects = new ArrayList<HellfireTerminusE>();
	public static ArrayList<HellfireTerminusE> remove = new ArrayList<HellfireTerminusE>();
	public static FireworkEffect fe = FireworkEffect.builder().with(Type.BURST).withColor(Color.RED).withColor(Color.YELLOW).build();
	public ArrayList<LivingEntity> hit = new ArrayList<LivingEntity>();
	public HellfireTerminus skill;
	public int tick;
	public LivingEntity target;
	Location targetOrigin;
	Random rand = new Random();
	Location[] orbs = new Location[4];
	

	public HellfireTerminusE(HellfireTerminus skill, LivingEntity target)
	{
		this.skill = skill;
		this.target = target;
		targetOrigin = target.getLocation();
		for (int i = 0; i < orbs.length; i++)
			orbs[i] = target.getLocation().add(4 - rand.nextInt(9), 6 + i * 2, 4 - rand.nextInt(9));
		
		skill.applyCooldown(60 * 5);
		effects.add(this);
	}

	public void tick()
	{
		target.teleport(targetOrigin);
		for (Location orb: orbs)
		{
			new RPGEvents.ParticleEffect(EnumParticle.FLAME, orb, 0.4F, Math.min(tick, 32)).run();
	        orb.getWorld().playSound(orb, Sound.ENTITY_GHAST_SHOOT, 0.2F, Math.min(1.5F, 0.5F + (tick / 32.0F)));
			
			if (tick < 32)
				continue;
			Location line = orb.clone().subtract(target.getLocation());
			Vector vector = line.toVector().normalize().multiply(0.5D);
			int length = (int) (line.getX() / vector.getX());
			
			int multiplier = 0;
			while (multiplier < length)
			{
				multiplier++;
				Location point = orb.clone().add(vector.clone().multiply(-multiplier));
				new RPGEvents.ParticleEffect(EnumParticle.FLAME, point, 0.1F, 2).run();
			}
		}
		
		if (tick == 32)
	        new RPGEvents.PlayLightningEffect(target).run();
		
		int damage;
		float pitch;
		if (tick >= 32 && tick % 2 == 0)
		{
			damage = RPlayer.varyDamage(skill.getUnvariedDamage());
			new RPGEvents.ApplyDamage(skill.player, target, damage).run();
			pitch = 1.5F - ((tick - 32.0F) / 38.0F);
	        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_LIGHTNING_IMPACT, 0.2F, pitch);
	        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_LIGHTNING_THUNDER, 0.2F, pitch);
			new InstantFirework(fe, target.getLocation());
			if (rand.nextInt(10) == 0 && tick != 32)
		        new RPGEvents.PlayLightningEffect(target).run();
		}
		
		tick++;
		if (tick > 70)
			remove.add(this);
	}

	public static void globalTick()
	{
		for (HellfireTerminusE e: effects)
			e.tick();
		effects.removeAll(HellfireTerminusE.remove);
		remove.clear();
	}
}
