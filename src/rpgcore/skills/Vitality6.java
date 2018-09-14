package rpgcore.skills;

import org.bukkit.inventory.ItemStack;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;

public class Vitality6 extends RPGSkill
{
	public final static String skillName = "Vitality VI";
	public final static boolean passiveSkill = true;
	public final static int skillTier = 6;
	public final static int castDelay = 0;
	public final static ClassType classType = ClassType.ALL;
	public final static int maxHealthAdd = 1;
	
	public Vitality6()
	{
		super(skillName, null, passiveSkill, castDelay, 0, classType, skillTier);
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(351, 1, (short) 1), 
				"&cVitality VI"),
				"&7Passive Skill:",
				"&7 * Max Health: +" + maxHealthAdd + " Hearts",
				"&f",
				"&8&oIncreased fortitude to",
				"&8&oreceiving damage.",
				"&f",
				"&7Skill Tier: " + RPGSkill.skillTierNames[skillTier],
				"&7Class: " + classType.getClassName());
	}
}
