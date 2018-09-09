package rpgcore.skills;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;

public class Evade extends RPGSkill
{
	public final static String skillName = "Evade";
	public final static boolean passiveSkill = true;
	public final static int skillTier = 2;
	public final static int castDelay = 0;
	public final static ClassType classType = ClassType.ASSASSIN;
	
	public Evade()
	{
		super(skillName, null, passiveSkill, castDelay, 0, classType, skillTier);
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.FEATHER, 1), 
				"&8Evade"),
				"&7Passive Skill:",
				"&7 * Evasion Time: 0.4s",
				"&f",
				"&8&oCompletely nullifies an attack if",
				"&8&oyou sneak within the Evasion Time",
				"&8&owindow given.",
				"&f",
				"&7Skill Tier: " + RPGSkill.skillTierNames[skillTier],
				"&7Class: " + classType.getClassName());
	}
}
