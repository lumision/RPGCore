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

public class PowerSlash extends RPGSkill
{
	public final static String skillName = "Power Slash";
	public final static boolean passiveSkill = false;
	public final static int skillTier = 1;
	public final static int castDelay = 20;
	public final static ClassType classType = ClassType.WARRIOR;
	public final static float damage = 1.4F;
	public PowerSlash(RPlayer caster)
	{
		super(skillName, caster, passiveSkill, castDelay, damage, classType, skillTier);
	}
	
	public PowerSlash()
	{
		super(skillName, null, passiveSkill, castDelay, 0, classType, skillTier);
	}
	
	@Override
	public void insantiate(RPlayer rp)
	{
		new PowerSlash(rp);
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.IRON_SWORD, 1), 
				"&fPower Slash"),
				"&7Damage: " + (int) (damage * 100) + "%",
				"&7Cooldown: 4s",
				"&f",
				"&8&oSends a short pierce forward.",
				"&f",
				"&7Skill Tier: " + RPGSkill.skillTierNames[skillTier],
				"&7Class: " + classType.getClassName());
	}

	@Override
	public void activate()
	{
		ArrayList<LivingEntity> hit = new ArrayList<LivingEntity>();
		Vector vector = player.getLocation().getDirection().normalize();
		int multiplier = 1;
        player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_GHAST_SHOOT, 0.1F, 1.5F);
		while (multiplier < 7)
		{
			multiplier++;
			Location point = player.getEyeLocation().add(vector.clone().multiply(multiplier));
			if (!CakeLibrary.getPassableBlocks().contains(point.getBlock().getType()))
				break;
			RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, point, 20), multiplier);
			RPGEvents.scheduleRunnable(new RPGEvents.AOEDetectionAttackWithBlockBreakEffect(hit, point, 1.25D, getUnvariedDamage(), player, 20), multiplier);
		}
	}
}
