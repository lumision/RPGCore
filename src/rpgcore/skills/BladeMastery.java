package rpgcore.skills;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;

public class BladeMastery extends RPGSkill
{
	public final static String skillName = "Blade Mastery";
	public final static int skillTier = 1;
	public final static int castDelay = 0;
	public final static ClassType classType = ClassType.ASSASSIN;
	
	public BladeMastery()
	{
		super(skillName, null, castDelay, 0, classType, skillTier);
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.IRON_SWORD), 
				"&4Blade Mastery"),
				"&7Passive Skill:",
				"&7 * Brute Damage: +10",
				"&7 * Attack Speed: +10" + "%",
				"&7 * Cooldowns: -10%",
				"&f",
				"&8&oIncreases your mastery in blades,",
				"&8&oboosting your attack speeds and",
				"&8&oreducing all your cooldowns.",
				"&f",
				"&7Skill Tier: " + CakeLibrary.convertToRoman(skillTier),
				"&7Class: " + classType.getClassName());
	}
}
