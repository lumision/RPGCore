package rpgcore.skills;

import org.bukkit.inventory.ItemStack;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;

public class MagicMastery2 extends RPGSkill
{
	public final static String skillName = "Magic Mastery II";
	public final static boolean passiveSkill = true;
	public final static int skillTier = 2;
	public final static int castDelay = 0;
	public final static ClassType classType = ClassType.MAGE;
	public final static int magicDamageAdd = 5;
	
	public MagicMastery2()
	{
		super(skillName, passiveSkill, castDelay, 0, classType, skillTier);
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(6, 1, (short) 2), 
				"&bMagic Mastery II"),
				"&7Passive Skill:",
				"&7 * Magic Damage: +" + magicDamageAdd,
				"&f",
				"&8&oYour mastery in arcane control",
				"&8&oincreases your damage output.",
				"&f",
				"&7Skill Tier: " + RPGSkill.skillTierNames[skillTier],
				"&7Class: " + classType.getClassName());
	}
}
