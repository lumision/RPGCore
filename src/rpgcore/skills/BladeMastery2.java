package rpgcore.skills;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;

public class BladeMastery2 extends RPGSkill
{
	public final static String skillName = "Blade Mastery II";
	public final static boolean passiveSkill = true;
	public final static int skillTier = 2;
	public final static int castDelay = 0;
	public final static ClassType classType = ClassType.ASSASSIN;
	public final static int bruteDamageAdd = 5;
	public final static float attackSpeedMultiplier = 1.1F;
	public final static int cooldownReductionAdd = 10;
	
	public BladeMastery2()
	{
		super(skillName, passiveSkill, castDelay, 0, classType, skillTier);
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.IRON_SWORD), 
				"&4Blade Mastery II"),
				"&7Passive Skill:",
				"&7 * Brute Damage: +" + bruteDamageAdd,
				"&7 * Attack Speed: +" + CakeLibrary.convertMultiplierToAddedPercentage(attackSpeedMultiplier) + "%",
				"&7 * Cooldowns: -" + cooldownReductionAdd + "%",
				"&f",
				"&8&oMastery in blade-handling",
				"&8&oincreases your overall",
				"&8&oattack speed and damage.",
				"&f",
				"&7Skill Tier: " + RPGSkill.skillTierNames[skillTier],
				"&7Class: " + classType.getClassName());
	}
}
