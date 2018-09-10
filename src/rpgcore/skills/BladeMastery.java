package rpgcore.skills;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;

public class BladeMastery extends RPGSkill
{
	public final static String skillName = "Blade Mastery";
	public final static boolean passiveSkill = true;
	public final static int skillTier = 1;
	public final static int castDelay = 0;
	public final static ClassType classType = ClassType.ASSASSIN;
	public final static float bruteDamageMultiplier = 0.2F;
	public final static float attackSpeedMultiplier = 0.2F;
	public final static int cooldownReductionAdd = 10;
	
	public BladeMastery()
	{
		super(skillName, null, passiveSkill, castDelay, 0, classType, skillTier);
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.IRON_SWORD), 
				"&4Blade Mastery"),
				"&7Passive Skill:",
				"&7 * Brute Damage: +" + bruteDamageMultiplier + "%",
				"&7 * Attack Speed: +" + (int) (attackSpeedMultiplier * 100.0F) + "%",
				"&7 * Cooldowns: -" + cooldownReductionAdd + "%",
				"&f",
				"&8&oIncreases your mastery in blades,",
				"&8&oboosting your attack speeds and",
				"&8&oreducing all your cooldowns.",
				"&f",
				"&7Skill Tier: " + RPGSkill.skillTierNames[skillTier],
				"&7Class: " + classType.getClassName());
	}
}
