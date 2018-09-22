package rpgcore.skillinventory2;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;
import rpgcore.player.RPlayer;
import rpgcore.skills.RPGSkill;

public class SkillInventory2 
{

	public static Inventory getSkillInventory(RPlayer player, int tier)
	{
		int size = 27;
		Inventory inv = Bukkit.createInventory(null, size, CakeLibrary.recodeColorCodes("&9Learnt Skills"));

		int passiveSkillIndex = 0;
		int activeSkillIndex = 0;
		int passiveSkillIndexAll = 8;
		int activeSkillIndexAll = 8;
		for (RPGSkill skill: RPGSkill.skillList)
			if (skill.skillTier == tier 
			&& (skill.classType.equals(player.currentClass) || skill.classType.equals(ClassType.ALL)) 
			&& player.skills.contains(skill.skillName))
				inv.setItem(skill.passiveSkill ? 
						(skill.classType.equals(ClassType.ALL) ? 
						passiveSkillIndexAll-- : passiveSkillIndex++) : 
							(skill.classType.equals(ClassType.ALL) ? 
									9 + activeSkillIndexAll-- : 
										9 + activeSkillIndex++), skill.getSkillItem());

		inv.setItem(18 + 3, getPrevTierItem(player, tier));
		inv.setItem(18 + 5, getNextTierItem(player, tier));
		inv.setItem(18 + 4, getCurrentTierItem(tier));

		return inv;
	}

	static ItemStack getCurrentTierItem(int tier)
	{
		return CakeLibrary.renameItem(new ItemStack(Material.ENCHANTED_BOOK, tier == 11 ? 16 : tier == 12 ? 24 : tier == 13 ? 32 : tier), "&eSkillbook Tier: " + RPGSkill.skillTierNames[tier]);
	}

	static ItemStack getPrevTierItem(RPlayer player, int tier)
	{
		int[] amounts = new int[tier - 1];
		boolean cont = tier > 1;
		for (String skill: player.skills)
		{
			RPGSkill s = RPGSkill.getSkill(skill);
			if (!s.classType.equals(player.currentClass) && !s.classType.equals(ClassType.ALL))
				continue;
			if (s.skillTier >= tier)
				continue;
			amounts[s.skillTier - 1]++;
		}
		ItemStack item = CakeLibrary.renameItem(new ItemStack(Material.BOOK), 
				CakeLibrary.insertColorFormatCode((cont ? "&6" : "&8") 
						+ "<-- " + (cont ? "(" + RPGSkill.skillTierNames[tier - 1] + "&6) " : "") + "Previous Tier", cont ? "&n" : ""));

		ArrayList<String> lore = new ArrayList<String>();
		for (int i = 0; i < amounts.length; i++)
		{
			if (amounts[i] == 0)
				continue;
			lore.add(CakeLibrary.recodeColorCodes("&7" + RPGSkill.skillTierNames[i + 1] + " &8-&7 " + amounts[i]));
		}
		return CakeLibrary.setItemLore(item, lore);
	}

	static ItemStack getNextTierItem(RPlayer player, int tier)
	{
		int[] amounts = new int[RPGSkill.skillTierNames.length - 1 - tier];
		boolean cont = tier < RPGSkill.skillTierNames.length - 1;
		for (String skill: player.skills)
		{
			RPGSkill s = RPGSkill.getSkill(skill);
			if (!s.classType.equals(player.currentClass) && !s.classType.equals(ClassType.ALL))
				continue;
			if (s.skillTier <= tier)
				continue;
			amounts[s.skillTier - 1 - tier]++;
		}
		ArrayList<String> lore = new ArrayList<String>();
		for (int i = 0; i < amounts.length; i++)
		{
			if (amounts[i] == 0)
				continue;
			lore.add(CakeLibrary.recodeColorCodes("&7" + RPGSkill.skillTierNames[i + tier + 1] + " &8-&7 " + amounts[i]));
		}
		if (lore.size() == 0)
			cont = false;
		ItemStack item = CakeLibrary.renameItem(new ItemStack(Material.BOOK), 
				CakeLibrary.insertColorFormatCode(
						(cont ? "&6" : "&8") + "Next Tier " + (cont ? "(" + RPGSkill.skillTierNames[tier + 1] + "&6) " : "") + "-->"
						, cont ? "&n" : ""));
		return CakeLibrary.setItemLore(item, lore);
	}

	public static ItemStack getClassIcon(ClassType classType)
	{
		switch (classType)
		{
		case WARRIOR:
			return CakeLibrary.renameItem(new ItemStack(Material.IRON_SWORD), "&fClass: " + getClassColor(classType) + "Warrior");
		case MAGE:
			return CakeLibrary.renameItem(new ItemStack(Material.STICK), "&fClass: " + getClassColor(classType) + "Mage");
		case PRIEST:
			return CakeLibrary.renameItem(new ItemStack(38, 1, (short) 3), "&fClass: " + getClassColor(classType) + "Priest");
		case ASSASSIN:
			return CakeLibrary.renameItem(new ItemStack(Material.NETHER_STAR), "&fClass: " + getClassColor(classType) + "Dualist");
		}
		return null;
	}

	public static String getClassColor(ClassType classType)
	{
		switch (classType)
		{
		case WARRIOR:
			return "§a";
		case MAGE:
			return "§b";
		case PRIEST:
			return "§e";
		case ASSASSIN:
			return "§c";
		}
		return "";
	}
}
