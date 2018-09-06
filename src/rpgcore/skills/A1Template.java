package rpgcore.skills;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;
import rpgcore.player.RPlayer;

public class A1Template extends RPGSkill
{
	public final static String skillName = ""; //Change
	public final static int skillTier = 1; //Change
	public final static int castDelay = 0; //Change
	public final static ClassType classType = ClassType.WARRIOR; //Change
	public A1Template(RPlayer caster)
	{
		super(skillName, caster, castDelay, calculateDamage(10), classType, skillTier);
	}
	
	public A1Template()
	{
		super(skillName, null, castDelay, 0, classType, skillTier);
	}
	
	@Override
	public void insantiate(RPlayer rp)
	{
		new A1Template(rp); //Change
	}
	
	@Override 
	public ItemStack instanceGetSkillItem(RPlayer player)
	{
		return getSkillItem(player);
	}

	public static ItemStack getSkillItem(RPlayer player) //Change
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.BRICK, 1), 
				"&f"),
				"&7Class: " + classType.getClassName(),
				"&7Damage: 100%",
				"&7Interval: s",
				"&f",
				"&8&oSkill description.",
				"&7Class: " + classType.getClassName());
	}

	public static double calculateDamage(int level)
	{
		return 1.0D + (level / 1.0D); //Change
	}

	@Override
	public void activate() //Change
	{
	}
}
