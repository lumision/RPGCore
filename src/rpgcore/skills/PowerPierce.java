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
import rpgcore.skillinventory.SkillInventory;

public class PowerPierce extends RPGSkill
{
	public final static String skillName = "Power Pierce";
	public final static int castDelay = 20;
	public final static ClassType classType = ClassType.WARRIOR;
	public PowerPierce(RPlayer caster)
	{
		super(skillName, caster, castDelay, calculateDamage(caster.getSkillLevel(skillName)), classType);
	}
	
	public PowerPierce()
	{
		super(skillName, null, castDelay, 0, classType);
	}
	
	@Override
	public void insantiate(RPlayer rp)
	{
		new PowerPierce(rp);
	}
	
	@Override 
	public ItemStack instanceGetSkillItem(RPlayer player)
	{
		return getSkillItem(player);
	}
	
	public static ItemStack getSkillItem(RPlayer player)
	{
		int level = player.getSkillLevel(skillName);
		boolean unlocked = level > 0;
		level += unlocked ? 0 : 1;
		return CakeLibrary.addLore(CakeLibrary.renameItem(unlocked ? new ItemStack(Material.IRON_SWORD, 1) : SkillInventory.locked.clone(), 
				"&fPower Pierce"),
				"&7Skill Level: " + (unlocked ? level : 0),
				"&7Damage: " + (int) (calculateDamage(level) * 100) + "%",
				"&7Interval: 0.5s",
				"&f",
				"&8&oSends a long pierce forward.",
				"&7Class: " + classType.getClassName());
	}
	
	public static double calculateDamage(int level)
	{
		return 0.5D + (level / 20.0D);
	}

	@Override
	public void activate()
	{
		ArrayList<LivingEntity> hit = new ArrayList<LivingEntity>();
		Vector vector = player.getLocation().getDirection().normalize();
		int multiplier = 1;
        player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_GHAST_SHOOT, 0.2F, 1.5F);
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
