package rpgcore.item;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.inventory.ItemStack;

import rpgcore.item.BonusStat.BonusStatCrystal;
import rpgcore.item.BonusStat.BonusStatType;
import rpgcore.main.CakeAPI;

public class RItem
{
	// BASE STATS
	public int magicDamage, bruteDamage, cooldownReduction, critChance, critDamage;
	public double attackSpeed;

	//BONUS STATS
	public BonusStat bonusStat;

	//ITEM
	public ArrayList<String> itemLore; //all lore lines excluding stat and misc ones
	public ItemStack itemVanilla;

	//MISC
	public String owner;
	public boolean isWeapon; //used for equip-checking

	//MISC1
	public static Random rand = new Random();
	public static final String statColor = CakeAPI.recodeColorCodes("&6");

	public RItem(ItemStack is)
	{
		setItemStats(is);
	}
	
	public void cleanItemStats()
	{
		magicDamage = bruteDamage = cooldownReduction = critChance = critDamage = 0;
		attackSpeed = 0.0D;
		bonusStat = null;
		itemLore = null;
		itemVanilla = null;
		owner = null;
		isWeapon = false;
	}
	
	public void setItemStats(ItemStack is)
	{
		if (CakeAPI.isItemStackNull(is))
			return;
		bonusStat = BonusStat.getItemStats(is);

		ArrayList<String> lore = CakeAPI.getItemLore(is);
		ArrayList<String> loreRemove = new ArrayList<String>();
		boolean bonusStatLines = false;
		for (int i = 0; i < lore.size(); i++)
		{
			String line = lore.get(i);
			String line1 = line;
			if (line.equals(""))
			{
				loreRemove.add(line1);
				continue;
			}
			if (!line.contains("§"))
				continue;
			line = CakeAPI.removeColorCodes(line);
			if (line.startsWith("  Magic Damage: +"))
			{
				try
				{
					magicDamage = Integer.parseInt(line.split(": +")[1]);
					loreRemove.add(line1);
				} catch (Exception e) {}
			} else if (line.startsWith("  Brute Damage: +"))
			{
				try
				{
					bruteDamage = Integer.parseInt(line.split(": +")[1]);
					loreRemove.add(line1);
				} catch (Exception e) {}
			} else if (line.startsWith("  Attack Speed: x"))
			{
				try
				{
					attackSpeed = Double.parseDouble(line.split(": x")[1]);
					loreRemove.add(line1);
				} catch (Exception e) {}
			} else if (line.startsWith("  Crit Chance: +"))
			{
				try
				{
					String percentage = line.split(": +")[1];
					percentage = percentage.substring(0, percentage.length() - 1);
					critChance = Integer.parseInt(percentage);
					loreRemove.add(line1);
				} catch (Exception e) {}
			} else if (line.startsWith("  Crit Damage: +"))
			{
				try
				{
					String percentage = line.split(": +")[1];
					percentage = percentage.substring(0, percentage.length() - 1);
					critDamage = Integer.parseInt(percentage);
					loreRemove.add(line1);
				} catch (Exception e) {}
			} else if (line.startsWith("  Cooldowns: "))
			{
				try
				{
					String percentage = line.split(": -")[1];
					percentage = percentage.substring(0, percentage.length() - 1);
					cooldownReduction = Integer.parseInt(percentage);
					loreRemove.add(line1);
				} catch (Exception e) {}
			} else if (line.startsWith("  --- Tier "))
			{
				bonusStatLines = true;
				loreRemove.add(line1);
			} else if (line.equals("  ------------"))
			{
				bonusStatLines = false;
				loreRemove.add(line1);
			} else if (bonusStatLines)
			{
				loreRemove.add(line1);
			} else if (line.startsWith("Owner: "))
			{
				owner = line.split(": ")[1];
				loreRemove.add(line1);
			} else if (line.equals(""))
			{
				loreRemove.add(line1);
				continue;
			}


		}
		lore.removeAll(loreRemove);

		itemLore = lore;
		itemVanilla = is.clone();
	}

	public ItemStack createItem()
	{
		ItemStack is = itemVanilla.clone();
		ArrayList<String> lore = new ArrayList<String>();

		//BASE STATS
		if (magicDamage != 0)
			lore.add(CakeAPI.recodeColorCodes(statColor + "  Magic Damage: +" + magicDamage));
		if (bruteDamage != 0)
			lore.add(CakeAPI.recodeColorCodes(statColor + "  Brute Damage: +" + bruteDamage));
		if (attackSpeed != 0)
			lore.add(CakeAPI.recodeColorCodes(statColor + "  Attack Speed: x" + attackSpeed));
		if (critChance != 0)
			lore.add(CakeAPI.recodeColorCodes(statColor + "  Crit Chance: +" + critChance + "%"));
		if (critDamage != 0)
			lore.add(CakeAPI.recodeColorCodes(statColor + "  Crit Damage: +" + critDamage + "%"));
		if (cooldownReduction != 0)
			lore.add(CakeAPI.recodeColorCodes(statColor + "  Cooldowns: -" + cooldownReduction + "%"));

		//BONUS STATS
		if (bonusStat != null)
		{
			lore.add("");
			String color = BonusStat.getTierColor(bonusStat.tier);
			lore.add(color + "  --- Tier " + bonusStat.tier + " ---");
			for (BonusStatType type: bonusStat.statLines)
				lore.add(color + type.getDescriptionWithValueRoll(bonusStat.tier));
			lore.add(color + "  ------------");
		}

		//ITEM
		lore.addAll(itemLore);

		//MISC
		if (owner != null)
			if (!owner.equals(""))
				lore.add(CakeAPI.recodeColorCodes("&fOwner: " + owner));

		is = CakeAPI.setItemLore(is, lore);
		return is;
	}

	/**
	 * @return Whether requirements were met for the scroll to be used or not.
	 */
	public boolean applyCrystal(BonusStatCrystal crystalType)
	{
		switch (crystalType)
		{
		case STAT_ADDER:
		{
			if (bonusStat != null)
				return false;
			ArrayList<BonusStatType> list = new ArrayList<BonusStatType>();
			list.add(BonusStatType.rollRandomStat());
			list.add(BonusStatType.rollRandomStat());
			bonusStat = new BonusStat(1, list);
			return true;
		}
		case ALL_LINES_REROLL:
		{
			if (bonusStat == null)
				return false;
			for (int i = 0; i < bonusStat.statLines.size(); i++)
				bonusStat.statLines.set(i, BonusStatType.rollRandomStat());
			return true;
		}
		case LINE_AMOUNT_REROLL:
		{
			if (bonusStat == null)
				return false;
			if (bonusStat.statLines.size() >= 3)
				return false;
			bonusStat.statLines.add(BonusStatType.rollRandomStat());
			return true;
		}
		case TIER_REROLL:
		{
			if (bonusStat == null)
				return false;
			if (bonusStat.tier == 5)
				return false;
			int chance = 5;	
			if (rand.nextInt(chance) != 0)
				return true;
			bonusStat.tier++;
			return true;
		}
		}
		return false;
	}

	/**
	 * Returns cooldown reduction in percentage
	 */
	public static int getItemCooldownReduction(ItemStack is) //return in percentage
	{
		ArrayList<String> lore = CakeAPI.getItemLore(is);
		for (String l: lore)
		{
			if (!l.contains("§"))
				continue;
			l = CakeAPI.removeColorCodes(l);
			if (l.startsWith("  Cooldowns: -"))
			{
				try
				{
					String percentage = l.split(": -")[1];
					percentage = percentage.substring(0, percentage.length() - 1);
					return Integer.parseInt(percentage);
				} catch (Exception e) {}
			}
		}
		return 0;
	}


	/**
	 * Returns crit chance bonus in percentage
	 */
	public static int getItemCritChance(ItemStack is) //return in percentage
	{
		ArrayList<String> lore = CakeAPI.getItemLore(is);
		for (String l: lore)
		{
			if (!l.contains("§"))
				continue;
			l = CakeAPI.removeColorCodes(l);
			if (l.startsWith("  Crit Chance: +"))
			{
				try
				{
					String percentage = l.split(": +")[1];
					percentage = percentage.substring(0, percentage.length() - 1);
					return Integer.parseInt(percentage);
				} catch (Exception e) {}
			}
		}
		return 0;
	}


	/**
	 * Returns crit damage bonus in percentage
	 */
	public static int getItemCritDamage(ItemStack is) //return in percentage
	{
		ArrayList<String> lore = CakeAPI.getItemLore(is);
		for (String l: lore)
		{
			if (!l.contains("§"))
				continue;
			l = CakeAPI.removeColorCodes(l);
			if (l.startsWith("  Crit Damage: +"))
			{
				try
				{
					String percentage = l.split(": +")[1];
					percentage = percentage.substring(0, percentage.length() - 1);
					return Integer.parseInt(percentage);
				} catch (Exception e) {}
			}
		}
		return 0;
	}


	/**
	 * Returns cooldown reduction in percentage
	 */
	public static int getItemMagicDamage(ItemStack is)
	{
		if (CakeAPI.isItemStackNull(is))
			return 0;
		int dmg = 0;
		ArrayList<String> lore = CakeAPI.getItemLore(is);
		for (String l: lore)
		{
			if (!l.contains("§"))
				continue;
			l = CakeAPI.removeColorCodes(l);
			if (l.startsWith("  Magic Damage: +"))
			{
				try
				{
					dmg += Integer.parseInt(l.split(": +")[1]);
				} catch (Exception e) {}
			}
		}
		return dmg;
	}

	public static int getItemBruteDamage(ItemStack is)
	{
		if (CakeAPI.isItemStackNull(is))
			return 0;
		int dmg = 0;
		ArrayList<String> lore = CakeAPI.getItemLore(is);
		for (String l: lore)
		{
			if (!l.contains("§"))
				continue;
			l = CakeAPI.removeColorCodes(l);
			if (l.startsWith("  Brute Damage: +"))
			{
				try
				{
					dmg += Integer.parseInt(l.split(": +")[1]);
				} catch (Exception e) {}
			}
		}
		return dmg;
	}

	public static double getItemCastDelayMultiplier(ItemStack is)
	{
		double castDelay = 1.0D;
		ArrayList<String> lore = CakeAPI.getItemLore(is);
		for (String l: lore)
		{
			if (!l.contains("§"))
				continue;
			l = CakeAPI.removeColorCodes(l);
			
			if (l.startsWith("  Attack Speed: x"))
			{
				try
				{
					castDelay = (1 / Double.parseDouble(l.split(": x")[1]));
				} catch (Exception e) {}
			}
		}

		return castDelay;
	}
}
