package rpgcore.skills;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import rpgcore.main.CakeAPI;
import rpgcore.player.RPlayer;
import rpgcore.skillinventory.SkillInventory;

public class LightFeet
{
	public final static String skillName = "Light Feet";
	public static ItemStack getSkillItem(RPlayer player)
	{
		int level = player.getSkillLevel(skillName);
		boolean unlocked = level > 0;
		level += unlocked ? 0 : 1;
		int jump = getJumpLevel(level);
		return CakeAPI.addLore(CakeAPI.renameItem(unlocked ? new ItemStack(Material.FEATHER) : SkillInventory.locked.clone(), 
				"&eLight Feet"),
				"&7Skill Level: " + (unlocked ? level : 0),
				"&7Passive Skill:",
				"&7 * +Swiftness " + CakeAPI.convertToRoman(getSwiftnessLevel(level) + 1),
				jump != -1 ? "&7 * +Jump " + CakeAPI.convertToRoman(getJumpLevel(level) + 1) : "",
				"&f",
				"&8&oIncreases your overall speed.",
				"&7Class: Thief");
	}
	
	public static int getSwiftnessLevel(int level)
	{
		return level < 5 ? 0 : level < 10 ? 1 : 2;
	}
	
	public static int getJumpLevel(int level)
	{
		return level < 5 ? -1 : level < 10 ? 0 : 1;
	}
}
