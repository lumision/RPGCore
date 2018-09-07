package rpgcore.skills;

import org.bukkit.inventory.ItemStack;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;

public class Wisdom extends RPGSkill
{
	public final static String skillName = "Wisdom";
	public final static int skillTier = 1;
	public final static int castDelay = 0;
	public final static ClassType classType = ClassType.MAGE;
	public final static float magicDamagePercentageAdd = 0.2F;
	
	public Wisdom()
	{
		super(skillName, null, castDelay, 0, classType, skillTier);
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(6, 1, (short) 5), 
				"&eWisdom"),
				"&7Passive Skill:",
				"&7 * Magic Damage: +" + (int) (magicDamagePercentageAdd * 100.0F) + "%",
				"&f",
				"&8&oIncreased knowledge in",
				"&8&ochanneling magic power.",
				"&f",
				"&7Skill Tier: " + CakeLibrary.convertToRoman(skillTier),
				"&7Class: " + classType.getClassName());
	}
}
