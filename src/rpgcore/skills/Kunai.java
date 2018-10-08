package rpgcore.skills;

import java.util.ArrayList;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGEvents;
import rpgcore.player.RPlayer;

public class Kunai extends RPGSkill
{
	public final static String skillName = "Kunai";
	public final static boolean passiveSkill = false;
	public final static int skillTier = 1;
	public final static int castDelay = 10;
	public final static ClassType classType = ClassType.ASSASSIN;
	public final static float damage = 2.4F;
	public Kunai(RPlayer caster)
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
		rp.skillCasts.add(new Kunai(rp));
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.GOLDEN_CARROT, 1), 
				"&eKunai"),
				"&7Damage: " + (int) (damage * 100) + "%",
				"&7Cooldown: 2s",
				"&f",
				"&8&oThrows a kunai knife forward.",
				"&f",
				"&7Skill Tier: " + RPGSkill.skillTierNames[skillTier],
				"&7Class: " + classType.getClassName());
	}

	@Override
	public void activate()
	{
		super.applyCooldown(2);
		new KunaiE(this);
	}
	
	public static class KunaiE extends SkillEffect
	{
		Location origin;
		Vector vector;
		ArrayList<LivingEntity> hit;
		public KunaiE(RPGSkill skill)
		{
			super(skill);
			
			origin = skill.player.getEyeLocation();
			vector = skill.player.getLocation().getDirection().normalize().multiply(0.75F).clone();
			hit = new ArrayList<LivingEntity>();

	        skill.player.getWorld().playSound(skill.player.getEyeLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.2F, 1.0F);
		}

		@Override
		public boolean tick() 
		{
			if (tick < 32)
			{
				tick++;
				Location point = origin.clone().add(vector.clone().multiply(tick));
				if (hit.size() > 0 || !CakeLibrary.getPassableBlocks().contains(point.getBlock().getType()))
				{
					new RPGEvents.PlayEffect(Effect.STEP_SOUND, point, point.getBlock().getTypeId()).run();
					return true;
				}
				new RPGEvents.FireworkTrail(point, 0, 1).run();
				new RPGEvents.PlaySoundEffect(point, Sound.BLOCK_GLASS_BREAK, 0.05F, 1.25F).run();
				ArrayList<LivingEntity> splash = CakeLibrary.getNearbyLivingEntitiesExcludePlayers(point, 1.25D);
				if (splash.size() > 0)
				{
					for (LivingEntity entity: splash)
					{
						new RPGEvents.ApplyDamage(skill.player, entity, RPlayer.varyDamage(skill.getUnvariedDamage())).run();
						new RPGEvents.PlayEffect(Effect.STEP_SOUND, entity, 20).run();
					}
					return true;
				}
			} else
				return true;
			tick++;
			return false;
		}
	}
	
}
