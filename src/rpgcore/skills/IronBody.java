package rpgcore.skills;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import rpgcore.main.CakeLibrary;
import rpgcore.player.RPlayer;
import rpgcore.skillinventory.SkillInventory;

public class IronBody
{
	public final static String skillName = "Iron Body";
	public static ItemStack getSkillItem(RPlayer player)
	{
		int level = player.getSkillLevel(skillName);
		boolean unlocked = level > 0;
		level += unlocked ? 0 : 1;
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.IRON_CHESTPLATE, 1), 
				"&eIron Body"),
				"&7Skill Level: " + (unlocked ? level : 0),
				"&7Passive Skill:",
				"&7 * Damage received: -" + (level * 5) + "%",
				"&f",
				"&8&oFortifies your natural defense.",
				"&7Class: Warrior");
	}
}
