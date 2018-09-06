package rpgcore.skillinventory2;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import rpgcore.main.CakeLibrary;
import rpgcore.player.RPlayer;
import rpgcore.skills.RPGSkill;

public class SkillInventory2 
{

	public static Inventory getSkillInventory(RPlayer player, int tier)
	{
		int size = 18;
		Inventory inv = Bukkit.createInventory(null, size, CakeLibrary.recodeColorCodes("&9Learnt Skills"));

		int index = 0;
		for (RPGSkill skill: RPGSkill.skillList)
		{
			if (skill.skillTier == tier && skill.classType.equals(player.currentClass) && player.skills.contains(skill.skillName))
			{
				inv.setItem(index, skill.getSkillItem());
				index++;
			}
		}

		inv.setItem(9 + 3, getPrevTierItem(player, tier));
		inv.setItem(9 + 5, getNextTierItem(player, tier));
		inv.setItem(9 + 4, getCurrentTierItem(tier));

		return inv;
	}

	static ItemStack getCurrentTierItem(int tier)
	{
		return CakeLibrary.renameItem(new ItemStack(Material.ENCHANTED_BOOK, tier), "&eSkillbook Tier: " + CakeLibrary.convertToRoman(tier));
	}

	static ItemStack getPrevTierItem(RPlayer player, int tier)
	{
		int[] amounts = new int[tier - 1];
		boolean cont = tier > 1;
		for (String skill: player.skills)
		{
			RPGSkill s = RPGSkill.getSkill(skill);
			if (s.skillTier >= tier)
				continue;
			amounts[s.skillTier - 1]++;
		}
		ItemStack item = CakeLibrary.renameItem(new ItemStack(Material.BOOK), (cont ? "&6&n" : "&8") 
				+ "<-- " + (cont ? "(" + CakeLibrary.convertToRoman(tier - 1) + ") " : "") + "Previous Tier");
		ArrayList<String> lore = new ArrayList<String>();
		for (int i = 0; i < amounts.length; i++)
		{
			if (amounts[i] == 0)
				continue;
			lore.add(CakeLibrary.recodeColorCodes("&7&o" + CakeLibrary.convertToRoman(i + 1) + " &8&o-&7&o " + amounts[i]));
		}
		return CakeLibrary.setItemLore(item, lore);
	}

	static ItemStack getNextTierItem(RPlayer player, int tier)
	{
		int[] amounts = new int[10 - tier];
		boolean cont = tier < 10;
		for (String skill: player.skills)
		{
			RPGSkill s = RPGSkill.getSkill(skill);
			if (s.skillTier <= tier)
				continue;
			amounts[s.skillTier - 1 - tier]++;
		}
		ItemStack item = CakeLibrary.renameItem(new ItemStack(Material.BOOK), (cont ? "&6&n" : "&8") 
				+ "Next Tier " + (cont ? "(" + CakeLibrary.convertToRoman(tier + 1) + ") " : "") + "-->");
		ArrayList<String> lore = new ArrayList<String>();
		for (int i = 0; i < amounts.length; i++)
		{
			if (amounts[i] == 0)
				continue;
			lore.add(CakeLibrary.recodeColorCodes("&7&o" + CakeLibrary.convertToRoman(i + 1 + tier) + " &8&o-&7&o " + amounts[i]));
		}
		return CakeLibrary.setItemLore(item, lore);
	}
}
