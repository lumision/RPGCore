package rpgcore.skills;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGEvents;
import rpgcore.player.RPlayer;
import rpgcore.skillinventory.SkillInventory;

public class HolyBolt extends RPGSkill
{
	public final static String skillName = "Holy Bolt";
	public final static int castDelay = 10;
	public final static ClassType classType = ClassType.PRIEST;
	public HolyBolt(RPlayer caster)
	{
		super(skillName, caster, castDelay, calculateDamage(caster.getSkillLevel(skillName)), classType);
	}
	
	public HolyBolt()
	{
		super(skillName, null, castDelay, 0, classType);
	}
	
	@Override
	public void insantiate(RPlayer rp)
	{
		new HolyBolt(rp);
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
		return CakeLibrary.addLore(CakeLibrary.renameItem(unlocked ? new ItemStack(175, 1) : SkillInventory.locked.clone(), 
				"&fHoly Bolt"),
				"&7Skill Level: " + (unlocked ? level : 0),
				"&7Damage: " + (int) (calculateDamage(level) * 100) + "%",
				"&7Interval: 0.5s",
				"&f",
				"&8&oShoots a beam of holy energy.",
				"&7Class: " + classType.getClassName());
	}
	
	public static double calculateDamage(int level)
	{
		return 0.4D + (level / 20.0D);
	}
	
	@Override
	public void activate()
	{
		ArrayList<LivingEntity> hit = new ArrayList<LivingEntity>();
		Vector vector = player.getLocation().getDirection().normalize().multiply(0.5D);
		int multiplier = 0;
        player.getWorld().playSound(player.getEyeLocation(), Sound.BLOCK_ANVIL_LAND, 0.1F, 1.0F);
		while (multiplier < 20)
		{
			multiplier++;
			Location point = player.getEyeLocation().add(vector.clone().multiply(multiplier));
			if (!CakeLibrary.getPassableBlocks().contains(point.getBlock().getType()))
				break;
			RPGEvents.scheduleRunnable(new RPGEvents.FireworkTrail(point, 0, 1), multiplier);
			RPGEvents.scheduleRunnable(new RPGEvents.PlaySoundEffect(point, Sound.BLOCK_GLASS_BREAK, 0.1F, 1.25F), multiplier);
			RPGEvents.scheduleRunnable(new RPGEvents.AOEDetectionAttackWithBlockBreakEffect(hit, point, 1.25D, getUnvariedDamage(), player, 20), multiplier);
		}
	}
}
