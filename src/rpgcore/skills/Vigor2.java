package rpgcore.skills;

import org.bukkit.inventory.ItemStack;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;

public class Vigor2 extends RPGSkill
{
	public final static String skillName = "Vigor II";
	public final static boolean passiveSkill = true;
	public final static int skillTier = 3;
	public final static int castDelay = 0;
	public final static ClassType classType = ClassType.ALL;
	public final static int recoverySpeedAdd = 20;
	
	public Vigor2()
	{
		super(skillName, null, passiveSkill, castDelay, 0, classType, skillTier);
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(351, 1, (short) 13), 
				"&dVigor II"),
				"&7Passive Skill:",
				"&7 * Recovery Speed: +" + recoverySpeedAdd + "%",
				"&f",
				"&8&oIncreased capacity to",
				"&8&oregenerate wounds.",
				"&f",
				"&7Skill Tier: " + RPGSkill.skillTierNames[skillTier],
				"&7Class: " + classType.getClassName());
	}
}
