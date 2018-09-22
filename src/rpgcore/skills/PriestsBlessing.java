package rpgcore.skills;

import org.bukkit.inventory.ItemStack;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;

public class PriestsBlessing extends RPGSkill
{
	public final static String skillName = "Priest's Blessing";
	public final static boolean passiveSkill = true;
	public final static int skillTier = 1;
	public final static int castDelay = 0;
	public final static ClassType classType = ClassType.PRIEST;
	public final static float xpMultiplier = 1.1F;
	
	public PriestsBlessing()
	{
		super(skillName, null, passiveSkill, castDelay, 0, classType, skillTier);
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(6, 1, (short) 5), 
				"&ePriest's Blessing"),
				"&7Passive Skill:",
				"&7 * Combat XP: +" + CakeLibrary.convertMultiplierToAddedPercentage(xpMultiplier) + "%",
				"&7 * Party Buff",
				"&f",
				"&8&oBlesses you and anyone in your",
				"&8&oparty with a small XP boost.",
				"&f",
				"&7Skill Tier: " + RPGSkill.skillTierNames[skillTier],
				"&7Class: " + classType.getClassName());
	}
}
