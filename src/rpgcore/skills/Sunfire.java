package rpgcore.skills;

import java.util.ArrayList;

import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.external.InstantFirework;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGEvents;
import rpgcore.player.RPlayer;

public class Sunfire extends RPGSkill
{
	public final static String skillName = "Sunfire";
	public final static boolean passiveSkill = false;
	public final static int skillTier = 4;
	public final static int castDelay = 100;
	public final static ClassType classType = ClassType.MAGE;
	public final static float damage = 24.8F;
	public final static int radius = 16;
	public final static int cooldown = 60;
	public Sunfire(RPlayer caster)
	{
		super(skillName, caster, passiveSkill, castDelay, damage, classType, skillTier);
	}

	@Override
	public void instantiate(RPlayer rp)
	{
		for (RPGSkill skill: rp.skillCasts)
			if (skill.skillName.equals(skillName))
			{
				skill.casterDamage = rp.getDamageOfClass();
				skill.caster.lastSkill = skillName;
				skill.caster.castDelays.put(skillName, (int) (castDelay * skill.caster.getStats().attackSpeedMultiplier));
				skill.caster.globalCastDelay = 1;
				skill.activate();
				return;
			}
		rp.skillCasts.add(new Sunfire(rp));
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.MAGMA_CREAM, 1), 
				"&eS&cu&fn&ef&ci&fr&ee"),
				"&7Damage/Projectile: " + (int) (damage * 100) + "%",
				"&7Radius: " + radius + " blocks",
				"&7Cooldown: 60s",
				"&f",
				"&8&oUnleashes a barrage of arcane",
				"&8&oprojectiles unto the target.",
				"&f",
				"&7Skill Tier: " + RPGSkill.skillTierNames[skillTier],
				"&7Class: " + classType.getClassName());
	}

	@Override
	public void activate()
	{
		new SunfireE(this);
	}

	public static class SunfireE extends SkillEffect
	{
		public static FireworkEffect fe = FireworkEffect.builder().with(Type.BURST).withColor(Color.WHITE).withColor(Color.RED).withColor(Color.YELLOW).build();
		public ArrayList<LivingEntity> hit = new ArrayList<LivingEntity>();
		public Location origin;
		public ArrayList<Location> offset = new ArrayList<Location>();

		public SunfireE(Sunfire skill)
		{
			super(skill);
			ArrayList<LivingEntity> nearby = CakeLibrary.getNearbyLivingEntities(skill.player.getLocation(), Sunfire.radius);
			this.origin = skill.player.getLocation().clone().add(0, 5, 0);
			for (LivingEntity e: nearby)
			{
				if (hit.size() > 16)
					break;
				if (e instanceof Player)
					continue;
				if (hit.contains(e))
					continue;
				hit.add(e);
				offset.add(new Location(skill.player.getWorld(), rand.nextInt(5) - 2, rand.nextInt(3) + 8, rand.nextInt(5) - 2));
			}
			if (hit.size() <= 0)
			{
				skill.player.playSound(skill.player.getLocation(), Sound.ENTITY_EGG_THROW, 0.2F, 0.5F);
				return;
			}
			skill.applyCooldown(cooldown);
		}

		@Override
		public boolean tick()
		{
			if (tick <= 60 && tick % 2 == 0)
			{
				new RPGEvents.PlaySoundEffect(skill.player, Sound.BLOCK_ANVIL_LAND, 0.2F, 0.5F + (tick / 60F)).run();
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
					damage = skill.getUnvariedDamage();
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
						new RPGEvents.ApplyDamage(skill.player, e, damage).run();
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
