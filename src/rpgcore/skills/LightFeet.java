package rpgcore.skills;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;

public class LightFeet extends RPGSkill
{
	public final static String skillName = "Light Feet";
	public final static int skillTier = 2;
	public final static int castDelay = 0;
	public final static ClassType classType = ClassType.ASSASSIN;
	public final static int swiftness = 1;
	public final static int jump = 0;
	
	public LightFeet()
	{
		super(skillName, null, castDelay, 0, classType, skillTier);
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.FEATHER), 
				"&eLight Feet"),
				"&7Passive Skill:",
				"&7 * +Swiftness " + CakeLibrary.convertToRoman(swiftness + 1),
				jump != -1 ? "&7 * +Jump " + CakeLibrary.convertToRoman(jump + 1) : "",
				"&f",
				"&8&oIncreases your overall speed.",
				"&f",
				"&7Skill Tier: " + CakeLibrary.convertToRoman(skillTier),
				"&7Class: " + classType.getClassName());
	}
}
