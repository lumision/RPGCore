package rpgcore.item;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import rpgcore.item.BonusStat.BonusStatCrystal;
import rpgcore.item.BonusStat.BonusStatType;
import rpgcore.main.CakeLibrary;

public class RItem
{
	public String databaseName;

	// BASE STATS
	public int levelRequirement, magicDamage, bruteDamage, cooldownReduction, critChance, critDamage, damageReduction;
	public double attackSpeed;

	//BONUS STATS
	public BonusStat bonusStat;

	//INTRINSIC STATS
	public double lifeDrain, lifeDrainChance,
	iceDamage, slowChance, slowLevel, slowDuration,
	fireDamage, burnChance, burnDPS, burnDuration,
	lightningDamage, stunChance, stunDuration,
	poisonChance, poisonDPS, poisonDuration;

	//ITEM
	public ArrayList<String> itemLore; //All lore lines excluding stat and misc ones
	public ItemStack itemVanilla;

	//MISC
	public String owner;
	public boolean isWeapon; //Used for equip-checking

	//MISC1
	public static Random rand = new Random();
	public static final String statColor = CakeLibrary.recodeColorCodes("&6");
	public static final String statColorSecondary = CakeLibrary.recodeColorCodes("&e");

	public RItem(ItemStack is)
	{
		setItemStats(is);
	}

	public RItem(ItemStack is, String databaseName)
	{
		this.databaseName = databaseName;
		setItemStats(is);
	}

	public void cleanItemStats()
	{
		levelRequirement = magicDamage = bruteDamage = cooldownReduction = critChance = critDamage = damageReduction = 0;
		attackSpeed = 0.0D;
		lifeDrain = lifeDrainChance = 
				iceDamage = slowChance = slowLevel = slowDuration = 
				fireDamage = burnChance = burnDPS = burnDuration = 
				lightningDamage = stunChance = stunDuration =
				poisonChance = poisonDPS = poisonDuration = 0.0D;
		bonusStat = null;
		itemLore = null;
		itemVanilla = null;
		owner = null;
		isWeapon = false;
	}

	public void setItemStats(ItemStack is)
	{
		if (CakeLibrary.isItemStackNull(is))
			return;
		bonusStat = BonusStat.getItemStats(is);

		ArrayList<String> lore = CakeLibrary.getItemLore(is);
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
			line = CakeLibrary.removeColorCodes(line);
			if (line.startsWith("  Lv. Requirement: "))
			{
				try
				{
					levelRequirement = Integer.parseInt(line.split(": +")[1]);
					loreRemove.add(line1);
				} catch (Exception e) {}
			} else if (line.startsWith("  Magic Damage: +"))
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
			} else if (line.startsWith("  Dmg Received: -"))
			{
				try
				{
					String percentage = line.split(": -")[1];
					percentage = percentage.substring(0, percentage.length() - 1);
					damageReduction = Integer.parseInt(percentage);
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
				continue;
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
		if (levelRequirement != 0)
			lore.add(CakeLibrary.recodeColorCodes(statColor + "  Lv. Requirement:" + statColorSecondary + " " + levelRequirement));
		if (magicDamage != 0)
			lore.add(CakeLibrary.recodeColorCodes(statColor + "  Magic Damage:" + statColorSecondary + " +" + magicDamage));
		if (bruteDamage != 0)
			lore.add(CakeLibrary.recodeColorCodes(statColor + "  Brute Damage:" + statColorSecondary + " +" + bruteDamage));
		if (attackSpeed != 0)
			lore.add(CakeLibrary.recodeColorCodes(statColor + "  Attack Speed:" + statColorSecondary +" x" + attackSpeed));
		if (critChance != 0)
			lore.add(CakeLibrary.recodeColorCodes(statColor + "  Crit Chance:" + statColorSecondary + " +" + critChance + "%"));
		if (critDamage != 0)
			lore.add(CakeLibrary.recodeColorCodes(statColor + "  Crit Damage:" + statColorSecondary + " +" + critDamage + "%"));
		if (cooldownReduction != 0)
			lore.add(CakeLibrary.recodeColorCodes(statColor + "  Cooldowns:" + statColorSecondary + " -" + cooldownReduction + "%"));
		if (damageReduction != 0)
			lore.add(CakeLibrary.recodeColorCodes(statColor + "  Dmg Received:" + statColorSecondary + " -" + damageReduction + "%"));

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
				lore.add(CakeLibrary.recodeColorCodes("&fOwner: " + owner));

		is = CakeLibrary.setItemLore(is, lore);
		return is;
	}

	public void saveItemToFile(String fileName)
	{
		File file = new File("plugins/RPGCore/items/" + fileName + ".yml");
		ArrayList<String> lines = new ArrayList<String>();
		lines.add("id: " + itemVanilla.getTypeId());
		lines.add("durability: " + itemVanilla.getDurability());
		lines.add("amount: " + itemVanilla.getAmount());

		ItemMeta im = itemVanilla.getItemMeta();
		if (im != null)
		{
			lines.add("");
			String name = im.getDisplayName();
			if (name != null)
				lines.add("name: " + name);
			List<String> lore = im.getLore();
			if (lore != null)
			{
				lines.add("lore: ");
				for (String s: lore)
					lines.add(" " + (CakeLibrary.removeColorCodes(s).length() <= 0 ? s + " " : s));
			}
		}

		ArrayList<Enchantment> enchs = new ArrayList<Enchantment>();
		ArrayList<Integer> levels = new ArrayList<Integer>();
		enchs.addAll(itemVanilla.getEnchantments().keySet());
		levels.addAll(itemVanilla.getEnchantments().values());

		if (enchs.size() > 0)
		{
			lines.add("");
			lines.add("enchantments: ");
			for (int i = 0; i < enchs.size(); i++)
				lines.add(" " + enchs.get(i).getId() + ": " + levels.get(i));
		}
		CakeLibrary.writeFile(lines, file);
	}

	public int applyIntrinsicEffects(Player user, LivingEntity victim, double damage)
	{
		if (iceDamage > 0)
			damage *= iceDamage;
		if (fireDamage > 0)
			damage *= fireDamage;
		if (lightningDamage > 0)
			damage *= lightningDamage;

		if (rand.nextDouble() > lifeDrainChance)
		{

		}
		if (rand.nextDouble() > slowChance)
		{
			CakeLibrary.addPotionEffectIfBetterOrEquivalent(victim, new PotionEffect(PotionEffectType.SLOW, (int) slowDuration, (int) slowLevel));
		}
		if (rand.nextDouble() > burnChance)
		{

		}
		if (rand.nextDouble() > stunChance)
		{
			CakeLibrary.addPotionEffectIfBetterOrEquivalent(victim, new PotionEffect(PotionEffectType.SLOW, (int) stunDuration, 32767));
		}
		if (rand.nextDouble() > poisonChance)
		{

		}


		return (int) damage;
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
		ArrayList<String> lore = CakeLibrary.getItemLore(is);
		for (String l: lore)
		{
			if (!l.contains("§"))
				continue;
			l = CakeLibrary.removeColorCodes(l);
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
		ArrayList<String> lore = CakeLibrary.getItemLore(is);
		for (String l: lore)
		{
			if (!l.contains("§"))
				continue;
			l = CakeLibrary.removeColorCodes(l);
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
		ArrayList<String> lore = CakeLibrary.getItemLore(is);
		for (String l: lore)
		{
			if (!l.contains("§"))
				continue;
			l = CakeLibrary.removeColorCodes(l);
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
		if (CakeLibrary.isItemStackNull(is))
			return 0;
		int dmg = 0;
		ArrayList<String> lore = CakeLibrary.getItemLore(is);
		for (String l: lore)
		{
			if (!l.contains("§"))
				continue;
			l = CakeLibrary.removeColorCodes(l);
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
		if (CakeLibrary.isItemStackNull(is))
			return 0;
		int dmg = 0;
		ArrayList<String> lore = CakeLibrary.getItemLore(is);
		for (String l: lore)
		{
			if (!l.contains("§"))
				continue;
			l = CakeLibrary.removeColorCodes(l);
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
		ArrayList<String> lore = CakeLibrary.getItemLore(is);
		for (String l: lore)
		{
			if (!l.contains("§"))
				continue;
			l = CakeLibrary.removeColorCodes(l);

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
