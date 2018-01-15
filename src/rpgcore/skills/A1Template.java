package rpgcore.skills;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeAPI;
import rpgcore.player.RPlayer;
import rpgcore.skillinventory.SkillInventory;

public class A1Template extends RPGSkill
{
	public final static String skillName = ""; //TODO
	public final static int castDelay = 0; //TODO
	public final static ClassType classType = ClassType.WARRIOR; //TODO
	public A1Template(RPlayer caster)
	{
		super(skillName, caster, castDelay, calculateDamage(caster.getSkillLevel(skillName)), classType);
	}
	
	public A1Template()
	{
		super(skillName, null, castDelay, 0, classType);
	}
	
	@Override
	public void insantiate(RPlayer rp)
	{
		new A1Template(rp); //TODO
	}
	
	@Override 
	public ItemStack instanceGetSkillItem(RPlayer player)
	{
		return getSkillItem(player);
	}

	public static ItemStack getSkillItem(RPlayer player) //TODO
	{
		int level = player.getSkillLevel(skillName);
		boolean unlocked = level > 0;
		level += unlocked ? 0 : 1;
		return CakeAPI.addLore(CakeAPI.renameItem(unlocked ? new ItemStack(Material.BRICK, 1) : SkillInventory.locked.clone(), 
				"&f"),
				"&7Skill Level: " + (unlocked ? level : 0),
				"&7Damage: " + (int) (calculateDamage(level) * 100) + "%",
				"&7Interval: s",
				"&f",
				"&8&oSkill description.",
				"&7Class: " + classType.getClassName());
	}

	public static double calculateDamage(int level)
	{
		return 1.0D + (level / 1.0D); //TODO
	}

	@Override
	public void activate() //TODO
	{
	}
}
