package rpgcore.skills;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import rpgcore.main.CakeLibrary;
import rpgcore.player.RPlayer;
import rpgcore.skillinventory.SkillInventory;

public class BladeMastery
{
	public final static String skillName = "Blade Mastery";
	public static ItemStack getSkillItem(RPlayer player)
	{
		int level = player.getSkillLevel(skillName);
		boolean unlocked = level > 0;
		level += unlocked ? 0 : 1;
		return CakeLibrary.addLore(CakeLibrary.renameItem(unlocked ? new ItemStack(Material.IRON_SWORD) : SkillInventory.locked.clone(), 
				"&4Blade Mastery"),
				"&7Skill Level: " + (unlocked ? level : 0),
				"&7Passive Skill:",
				"&7 * Brute Damage: +" + level,
				"&7 * Attack Speed: +" + level + "%",
				"&7 * Cooldowns: -" + level + "%",
				"&f",
				"&8&oIncreases your mastery in blades,",
				"&8&oboosting your attack speeds and",
				"&8&oreducing all your cooldowns.",
				"&7Class: Thief");
	}
}
