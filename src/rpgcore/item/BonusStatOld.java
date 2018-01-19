package rpgcore.item;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import rpgcore.main.CakeLibrary;

public class BonusStatOld
{
	public static Random rand = new Random();
	public static enum BonusStatType
	{
		MAGIC_DAMAGE_PERCENTAGE_1("+::% Magic Damage ", 3.0D, 2),
		MAGIC_DAMAGE_PERCENTAGE_2("+::% Magic Damage  ", 2.0D, 2),
		BRUTE_DAMAGE_PERCENTAGE_1("+::% Brute Damage ", 3.0D, 2),
		BRUTE_DAMAGE_PERCENTAGE_2("+::% Brute Damage  ", 2.0D, 2),
		BOSS_DAMAGE_PERCENTAGE_1("+::% Boss Damage ", 5.0D, 2),
		BOSS_DAMAGE_PERCENTAGE_2("+::% Boss Damage  ", 3.3D, 2),
		CAST_SPEED_PERCENTAGE_1("+::% Attack Speed ", 3.0D, 2),
		CAST_SPEED_PERCENTAGE_2("+::% Attack Speed  ", 2.0D, 2),
		
		MAGIC_DAMAGE_1("+:: Magic Damage ", 4.0D, 3),
		MAGIC_DAMAGE_2("+:: Magic Damage  ", 3.0D, 3),
		BRUTE_DAMAGE_1("+:: Brute Damage ", 4.0D, 3),
		BRUTE_DAMAGE_2("+:: Brute Damage  ", 3.0D, 3),
		CRIT_CHANCE_1("+::% Crit Chance ", 2.0D, 3),
		CRIT_CHANCE_2("+::% Crit Chance  ", 1.32D, 3),
		CRIT_DAMAGE_1("+::% Crit Damage ", 8.0D, 3),
		CRIT_DAMAGE_2("+::% Crit Damage  ", 5.28D, 3);

		private String description;
		private double multiplier;
		private int rollSpace;
		
		/**
		 * Constructs a BonusStatType enum.
		 * @param description - The bonus stat description as shown in the lore. All "::"s are replaced with the value.
		 * @param multiplier - This value multiplied by the tier gives the bonus stat value.
		 *                     i.e if you want this stat to give "+10% Something" at tier 5, then the 'multiplier' value would be (10 / 5) == '2'.
		 * @param rollSpace - Dictates how much space the bonus stat takes up when rolling for a random one. 
		 *                    In other words, controls how often it appears when a player rolls for a random stat.
		 */
		private BonusStatType(String description, double multiplier, int rollSpace)
		{
			this.description = "  " + description;
			this.multiplier = multiplier;
			this.rollSpace = rollSpace;
		}

		public String getDescription()
		{
			return description;
		}

		public double getMultiplier()
		{
			return multiplier;
		}

		public String getDescriptionWithValue(int tier)
		{
			return description.replaceAll("::", (int) Math.round(tier * multiplier) + "");
		}

		public int getRollSpace()
		{
			return rollSpace;
		}

		public static BonusStatType rollRandomStat()
		{
			ArrayList<BonusStatType> stats = new ArrayList<BonusStatType>();
			for (BonusStatType stat: BonusStatType.values())
				for (int i = 0; i < stat.getRollSpace(); i++)
					stats.add(stat);
			return stats.get(rand.nextInt(stats.size()));
		}
	}

	public static enum BonusStatCrystal
	{
		ALL_LINES_REROLL(CakeLibrary.editNameAndLore(new ItemStack(Material.END_CRYSTAL), "&a&nAlchemy Crystal", "&aRerolls all bonus stat lines.", "&b", "&7&oHold and click to use.")),
		TIER_REROLL(CakeLibrary.editNameAndLore(new ItemStack(Material.END_CRYSTAL), "&e&nPassion Crystal", "&eIncreases the tier of a bonus", "&estat at a small chance.", "&eMax Tier: 5", "&b", "&7&oHold and click to use.")),
		LINE_AMOUNT_REROLL(CakeLibrary.editNameAndLore(new ItemStack(Material.END_CRYSTAL), "&b&nWisdom Crystal", "&bAdds a random bonus stat line.", "&bMax Lines: 3", "&b", "&7&oHold and click to use.")),
		STAT_ADDER(CakeLibrary.editNameAndLore(new ItemStack(Material.END_CRYSTAL), "&d&nSpirit Crystal", "&dAdds a bonus stat to an item.", "&b", "&7&oHold and click to use."));

		private String itemName;
		private ItemStack item;
		private BonusStatCrystal(ItemStack item)
		{
			this.itemName = CakeLibrary.removeColorCodes(CakeLibrary.getItemName(item));
			this.item = item;
		}

		public String getItemName()
		{
			return itemName;
		}

		public ItemStack getItemStack()
		{
			return item.clone();
		}
		
		public Inventory getCrystalInventory(int amount)
		{
			return getCrystalInventory(this, amount);
		}

		public static Inventory getCrystalInventory(BonusStatCrystal type, int amount)
		{
			ItemStack crystal = type.getItemStack();
			crystal.setAmount(amount);
			Inventory inv = Bukkit.createInventory(null, 9, CakeLibrary.getItemName(crystal));
			ItemStack sign = CakeLibrary.renameItem(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15), "&7Place the item in the middle.");
			for (int i = 1; i <= 8; i++)
				if (i != 4)
					inv.setItem(i, sign.clone());
			inv.setItem(0, crystal);
			return inv;
		}
	}

	public int tier;
	public ArrayList<BonusStatType> statLines;
	public BonusStatOld(int tier, ArrayList<BonusStatType> statTypeList)
	{
		this.tier = tier;
		this.statLines = statTypeList;
	}

	public static BonusStatOld getItemStats(ItemStack is)
	{
		ArrayList<BonusStatType> stats = new ArrayList<BonusStatType>();
		int tier = 0;
		if (is == null)
			return null;
		ArrayList<String> lore = CakeLibrary.getItemLore(is);
		if (lore.size() <= 0)
			return null;
		for (String line: lore)
		{
			if (!CakeLibrary.hasColor(line))
				continue;
			line = CakeLibrary.removeColorCodes(line);
			if (tier == 0 && line.startsWith("  --- Tier "))
			{
				try
				{
					tier = Integer.parseInt(line.replaceAll("[^\\d]", ""));
				} catch (Exception e) {}
			}
			for (BonusStatType type: BonusStatType.values())
			{
				if (!type.getDescription().replaceAll("::", "").equals(line.replaceAll("\\d", "")))
					continue;
				stats.add(type);
			}
		}
		if (stats.size() == 0)
			return null;
		return new BonusStatOld(tier, stats);
	}

	public static String getTierColor(int tier)
	{
		String color = "&4";
		switch(tier)
		{
		case 1:
			color = "&a";
			break;
		case 2:
			color = "&b";
			break;
		case 3:
			color = "&c";
			break;
		case 4:
			color = "&d";
			break;
		case 5:
			color = "&e";
			break;
		}
		return CakeLibrary.recodeColorCodes(color);
	}
}
