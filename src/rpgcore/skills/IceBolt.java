package rpgcore.skills;

import java.util.ArrayList;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGEvents;
import rpgcore.player.RPlayer;

public class IceBolt extends RPGSkill
{
	public final static String skillName = "Ice Bolt";
	public final static boolean passiveSkill = false;
	public final static int skillTier = 1;
	public final static int castDelay = 10;
	public final static ClassType classType = ClassType.MAGE;
	public final static float damage = 1.6F;
	public final static int debuffLevel = 1;
	public final static int debuffLength = 10 * 20;
	public IceBolt(RPlayer caster)
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
		rp.skillCasts.add(new IceBolt(rp));
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.SNOW_BALL, 1), 
				"&bIce Bolt"),
				"&7Damage: " + (int) (damage * 100.0F) + "%",
				"&7Interval: " + (castDelay / 20.0F) + "s",
				"&f",
				"&7Debuff:",
				"&7 * Slow " + CakeLibrary.convertToRoman(debuffLevel + 1),
				"&7 * Duration: " + (debuffLength / 20) + "s",
				"&f",
				"&8&oShoots a bolt of ice energy;",
				"&8&oslows down anything it hits.",
				"&f",
				"&7Skill Tier: " + RPGSkill.skillTierNames[skillTier],
				"&7Class: " + classType.getClassName());
	}
	
	public void activate()
	{
		new IceBoltE(this);
	}
	
	public static class IceBoltE extends SkillEffect
	{
		Location origin;
		Vector vector;
		ArrayList<LivingEntity> hit;
		public IceBoltE(RPGSkill skill)
		{
			super(skill);
			
			origin = skill.player.getEyeLocation();
			vector = skill.player.getLocation().getDirection().normalize().multiply(0.75F).clone();
			hit = new ArrayList<LivingEntity>();
			
			skill.player.getWorld().playSound(skill.player.getEyeLocation(), Sound.BLOCK_ANVIL_LAND, 0.05F, 1.0F);
		}

		@Override
		public boolean tick() 
		{
			if (tick < 24)
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
						new RPGEvents.PlayEffect(Effect.STEP_SOUND, entity, 79).run();
						CakeLibrary.addPotionEffectIfBetterOrEquivalent(entity, new PotionEffect(PotionEffectType.SLOW, debuffLength, debuffLevel));
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
