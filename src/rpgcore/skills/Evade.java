package rpgcore.skills;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import rpgcore.main.CakeAPI;
import rpgcore.player.RPlayer;
import rpgcore.skillinventory.SkillInventory;

public class Evade
{
	public final static String skillName = "Evade";
	public static ItemStack getSkillItem(RPlayer player)
	{
		int level = player.getSkillLevel(skillName);
		boolean unlocked = level > 0;
		level += unlocked ? 0 : 1;
		return CakeAPI.addLore(CakeAPI.renameItem(unlocked ? new ItemStack(Material.FEATHER, 1) : SkillInventory.locked.clone(), 
				"&8Evade"),
				"&7Skill Level: " + (unlocked ? level : 0),
				"&7Passive Skill:",
				"&7 * Evasion Time: " + (0.2D + (level / 50.0D)) + "s",
				"&f",
				"&8&oCompletely nullifies an attack if",
				"&8&oyou sneak within the Evasion Time",
				"&8&owindow given.",
				"&7Class: Thief");
	}
}
