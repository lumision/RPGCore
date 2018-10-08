package rpgcore.skills;

import java.util.ArrayList;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGEvents;
import rpgcore.player.RPlayer;

public class HolyBolt extends RPGSkill
{
	public final static String skillName = "Holy Bolt";
	public final static boolean passiveSkill = false;
	public final static int skillTier = 1;
	public final static int castDelay = 10;
	public final static ClassType classType = ClassType.PRIEST;
	public final static float damage = 1.2F;
	public HolyBolt(RPlayer caster)
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
		rp.skillCasts.add(new HolyBolt(rp));
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(175, 1), 
				"&fHoly Bolt"),
				"&7Damage: " + (int) (damage * 100) + "%",
				"&7Interval: " + (castDelay / 20.0F) + "s",
				"&f",
				"&8&oShoots a beam of holy energy.",
				"&f",
				"&7Skill Tier: " + RPGSkill.skillTierNames[skillTier],
				"&7Class: " + classType.getClassName());
	}
	
	@Override
	public void activate()
	{
		ArrayList<LivingEntity> hit = new ArrayList<LivingEntity>();
		Vector vector = player.getLocation().getDirection().normalize().multiply(0.75D);
		int multiplier = 0;
        player.getWorld().playSound(player.getEyeLocation(), Sound.BLOCK_ANVIL_LAND, 0.05F, 1.0F);
		while (multiplier < 20)
		{
			multiplier++;
			Location point = player.getEyeLocation().add(vector.clone().multiply(multiplier));
			if (!CakeLibrary.getPassableBlocks().contains(point.getBlock().getType()))
			{
				RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, point, point.getBlock().getTypeId()), multiplier);
				break;
			}
			RPGEvents.scheduleRunnable(new RPGEvents.FireworkTrail(point, 0, 1), multiplier);
			RPGEvents.scheduleRunnable(new RPGEvents.PlaySoundEffect(point, Sound.BLOCK_GLASS_BREAK, 0.05F, 1.25F), multiplier);
			RPGEvents.scheduleRunnable(new RPGEvents.AOEDetectionAttackWithBlockBreakEffect(hit, point, 1.25D, getUnvariedDamage(), player, 20), multiplier);
		}
	}
}
