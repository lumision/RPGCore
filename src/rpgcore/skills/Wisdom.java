package rpgcore.skills;

import org.bukkit.inventory.ItemStack;

import rpgcore.main.CakeLibrary;
import rpgcore.player.RPlayer;
import rpgcore.skillinventory.SkillInventory;

public class Wisdom
{
	public final static String skillName = "Wisdom";
	public static ItemStack getSkillItem(RPlayer player)
	{
		int level = player.getSkillLevel(skillName);
		boolean unlocked = level > 0;
		level += unlocked ? 0 : 1;
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(6, 1, (short) 5), 
				"&eWisdom"),
				"&7Skill Level: " + (unlocked ? level : 0),
				"&7Passive Skill:",
				"&7 * Magic Damage: +" + level,
				"&f",
				"&8&oIncreases knowledge in channeling",
				"&8&omagic which adds to magic power.",
				"&7Class: Mage");
	}
}
