package rpgcore.skills;

import java.util.ArrayList;

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

public class QuickSlash1 extends RPGSkill
{
	public final static String skillName = "Quick Slash I";
	public final static boolean passiveSkill = false;
	public final static int skillTier = 1;
	public final static int castDelay = 5;
	public final static ClassType classType = ClassType.ASSASSIN;
	public final static float damage = 2.6F;
	public final static int cooldown = 2;
	public final static int size = 7;
	public QuickSlash1(RPlayer caster)
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
		rp.skillCasts.add(new QuickSlash1(rp));
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.IRON_SWORD, 1), 
				"&cQuick Slash I"),
				"&7Damage: " + (int) (damage * 100) + "%",
				"&7Cooldown: " + cooldown + "s",
				"&f",
				"&8&oUnleash a hasty slash",
				"&f",
				"&7Skill Tier: " + RPGSkill.skillTierNames[skillTier],
				"&7Class: " + classType.getClassName());
	}

	@Override
	public void activate()
	{
		super.applyCooldown(2);
		ArrayList<LivingEntity> hit = new ArrayList<LivingEntity>();
		
		Location horizon = player.getLocation();
		horizon.setYaw(horizon.getYaw() - 100F);
		horizon.setPitch(25F);
		
		Vector slashDirection = horizon.getDirection().normalize().multiply(0.5D);
		Vector direction = player.getLocation().getDirection().normalize();
		Location startPointCenter = player.getEyeLocation();
		
		int multiplier = 1;
		while (multiplier < 5)
		{
			startPointCenter = startPointCenter.add(direction);
			if (CakeLibrary.getNearbyLivingEntitiesExcludePlayers(startPointCenter, 1.0D).size() > 0)
				break;
			multiplier++;
		}
		
		Location startPoint = startPointCenter.clone().add(slashDirection.clone().multiply(-size));
		
		multiplier = 1;
        player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.4F, 0.8F);
		while (multiplier < size * 2)
		{
			multiplier++;
			Location point = startPoint.clone().add(slashDirection.clone().multiply(multiplier));
			new RPGEvents.FireworkTrail(point, 0, 1).run();
			if (multiplier % 2 == 0)
			new RPGEvents.AOEDetectionAttackWithBlockBreakEffect(hit, point, 1.25D, getUnvariedDamage(), player, 20).run();
		}
	}
}
