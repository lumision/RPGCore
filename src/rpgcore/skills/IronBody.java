package rpgcore.skills;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;

public class IronBody extends RPGSkill
{
	public final static String skillName = "Iron Body";
	public final static boolean passiveSkill = true;
	public final static int skillTier = 1;
	public final static int castDelay = 0;
	public final static ClassType classType = ClassType.WARRIOR;
	public final static int damageReductionAdd = 20;
	public IronBody()
	{
		super(skillName, null, passiveSkill, castDelay, 0, classType, skillTier);
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.IRON_CHESTPLATE, 1), 
				"&eIron Body"),
				"&7Passive Skill:",
				"&7 * Damage Reduction: +" + damageReductionAdd + "%",
				"&f",
				"&8&oFortifies your natural defense.",
				"&f",
				"&7Skill Tier: " + RPGSkill.skillTierNames[skillTier],
				"&7Class: " + classType.getClassName());
	}
}
