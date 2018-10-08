package rpgcore.item;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import rpgcore.buff.Buff;
import rpgcore.buff.Stats;
import rpgcore.item.BonusStat.BonusStatCrystal;
import rpgcore.item.BonusStat.BonusStatType;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGCore;

public class RItem
{
	public String databaseName;
	public File file;

	// BASE STATS
	public int levelRequirement, magicDamage, bruteDamage, cooldownReduction, critChance, critDamage, damageReduction, recoverySpeed;
	public double attackSpeed, xpMultiplier;

	//BONUS STATS
	public BonusStat bonusStat;

	//ENHANCEMENT
	private int tier;
	public int addedMagicDamage, addedBruteDamage;

	//INTRINSIC STATS
	public double lifeDrain, lifeDrainChance,
	iceDamage, slowChance, slowLevel, slowDuration,
	fireDamage, burnChance, burnDPS, burnDuration,
	lightningDamage, stunChance, stunDuration,
	poisonChance, poisonDPS, poisonDuration;

	//FOOD BUFF
	public boolean consumable;
	public int satiate, buffDuration, consumableCooldown;
	public int magicDamageAdd, bruteDamageAdd, damageReductionAdd, cooldownReductionAdd, recoverySpeedAdd, critChanceAdd, critDamageAdd;
	public float magicDamageMultiplier, bruteDamageMultiplier, attackSpeedMultiplier, xpMultiplierFood;

	//ITEM
	public ArrayList<String> itemLore; //All lore lines excluding stat and misc ones
	public ItemStack itemVanilla;
	public boolean accessory;

	//MISC
	public String owner;
	public boolean isWeapon; //Used for equip-checking
	public String headTexture;
	public int dropRoll;

	//MISC1
	static Random rand = new Random();
	static final String statColor = CakeLibrary.recodeColorCodes("&6");
	static final String statColorSecondary = CakeLibrary.recodeColorCodes("&e");

	public static final String[] tiers1 = {
			CakeLibrary.recodeColorCodes("&a [&2I&a]"),
			CakeLibrary.recodeColorCodes("&b [&3II&b]"),
			CakeLibrary.recodeColorCodes("&c [&4III&c]"),
			CakeLibrary.recodeColorCodes("&d [&5IV&d]"),
			CakeLibrary.recodeColorCodes("&e [&6V&e]")
	};

	public static final String[] tiers2 = {
			CakeLibrary.recodeColorCodes("&6  [ &e✮&6 ]"),
			CakeLibrary.recodeColorCodes("&6  [ &e✮✮&6 ]"),
			CakeLibrary.recodeColorCodes("&6  [ &e✮✮✮&6 ]"),
			CakeLibrary.recodeColorCodes("&6  [ &e✮✮✮✮&6 ]"),
			CakeLibrary.recodeColorCodes("&6  [ &e✮✮✮✮✮&6 ]")
	};

	public static final String[] tiers = {
			CakeLibrary.recodeColorCodes("&e ✮"),
			CakeLibrary.recodeColorCodes("&e ✮✮"),
			CakeLibrary.recodeColorCodes("&e ✮✮✮"),
			CakeLibrary.recodeColorCodes("&e ✮✮✮✮"),
			CakeLibrary.recodeColorCodes("&e ✮✮✮✮✮")
	};

	static final boolean tierLore = false;

	static final float tierStatMultiplier = 1.3F;

	public RItem(ItemStack is)
	{
		setItemStats(is);
	}

	public RItem(ItemStack is, String databaseName)
	{
		this.databaseName = databaseName;
		setItemStats(is);
	}

	public Buff getBuff()
	{
		String buffName = CakeLibrary.getItemName(itemVanilla);
		Stats bs = Stats.getBuffStats(buffName);
		if (bs == null)
			bs = Stats.createStats(buffName, itemVanilla).setAttackSpeedMultiplier(attackSpeedMultiplier).setBruteDamageAdd(bruteDamageAdd)
			.setBruteDamageMultiplier(bruteDamageMultiplier).setCooldownReductionAdd(cooldownReductionAdd).setCritChanceAdd(critChanceAdd)
			.setCritDamageAdd(critDamageAdd).setDamageReductionAdd(damageReductionAdd).setMagicDamageAdd(magicDamageAdd).setMagicDamageMultiplier(magicDamageMultiplier)
			.setXPMultiplier(xpMultiplierFood).setBuffDuration(buffDuration);
		return Buff.createBuff(bs);
	}
	
	public boolean compareBase(RItem other)
	{
		return (new RItem(createBaseItem())).compare(new RItem(other.createBaseItem()));
	}
	
	public static boolean compare(ItemStack item1, ItemStack item2)
	{
		if (!item1.getType().equals(item2.getType()))
			return false;

		if (item1.getDurability() != item2.getDurability())
			return false;

		ItemMeta im = item1.getItemMeta();
		ItemMeta imOther = item2.getItemMeta();

		if ((im.getDisplayName() == null) != (imOther.getDisplayName() == null))
			return false;
		if (im.getDisplayName() != null && imOther.getDisplayName() != null && !im.getDisplayName().equals(imOther.getDisplayName()))
			return false;

		if ((im.getLore() == null) != (imOther.getLore() == null))
			return false;
		if (im.getLore() != null && imOther.getLore() != null)
		{
			if (im.getLore().size() != imOther.getLore().size())
				return false;

			for (int i = 0; i < im.getLore().size(); i++)
				if (!CakeLibrary.removeColorCodes(im.getLore().get(i).replaceAll(" ", ""))
						.equals(CakeLibrary.removeColorCodes(imOther.getLore().get(i).replaceAll(" ", ""))))
					return false;
		}
		return true;
	}

	public boolean compare(RItem other)
	{
		return compare(itemVanilla, other.itemVanilla);
	}

	public void setItemStats(ItemStack is)
	{
		if (CakeLibrary.isItemStackNull(is))
			return;

		bonusStat = BonusStat.getItemStats(is);

		String name = CakeLibrary.getItemName(is);
		boolean nameChange = false;
		if (!tierLore)
		{
			for (int i = 0; i < tiers.length; i++)
				if (name.endsWith(tiers[i]))
				{
					tier = i + 1;
					name = name.substring(0, name.length() - tiers[i].length());
					nameChange = true;
				}
		}

		ArrayList<String> lore = CakeLibrary.getItemLore(is);
		ArrayList<Integer> loreRemove = new ArrayList<Integer>();
		boolean bonusStatLines = false;
		for (int i = 0; i < lore.size(); i++)
		{
			String line = lore.get(i);
			String line1 = line;
			if (!line.contains("§"))
				continue;
			line = CakeLibrary.removeColorCodes(line);
			if (tierLore)
				for (int i1 = 0; i1 < tiers.length; i1++)
					if (line1.equals(tiers[i1]))
					{
						tier = i1 + 1;
						loreRemove.add(i);
					}
			if (line.equals("Accessory"))
			{
				try
				{
					accessory = true;
					loreRemove.add(i);
				} catch (Exception e) {}
			} else if (line.startsWith("  Magic Damage: +"))
			{
				try
				{
					String[] numbers = line.split(": +")[1].split(" ");
					magicDamage = Integer.parseInt(numbers[0]);
					if (numbers.length > 1)
					{
						addedMagicDamage = Integer.parseInt(numbers[1].substring(2, numbers[1].length() - 1));
						magicDamage -= addedMagicDamage;
					}
					loreRemove.add(i);
				} catch (Exception e) {}
			} else if (line.startsWith("  Brute Damage: +"))
			{
				try
				{
					String[] numbers = line.split(": +")[1].split(" ");
					bruteDamage = Integer.parseInt(numbers[0]);
					if (numbers.length > 1)
					{
						addedBruteDamage = Integer.parseInt(numbers[1].substring(2, numbers[1].length() - 1));
						bruteDamage -= addedBruteDamage;
					}
					loreRemove.add(i);
				} catch (Exception e) {}
			} else if (line.startsWith("  Attack Speed: x"))
			{
				try
				{
					attackSpeed = Double.parseDouble(line.split(": x")[1]);
					loreRemove.add(i);
				} catch (Exception e) {}
			} else if (line.startsWith("  Combat XP: x"))
			{
				try
				{
					xpMultiplier = Double.parseDouble(line.split(": x")[1]);
					loreRemove.add(i);
				} catch (Exception e) {}
			} else if (line.startsWith("  Crit Chance: +"))
			{
				try
				{
					String percentage = line.split(": +")[1];
					percentage = percentage.substring(0, percentage.length() - 1);
					critChance = Integer.parseInt(percentage);
					loreRemove.add(i);
				} catch (Exception e) {}
			} else if (line.startsWith("  Crit Damage: +"))
			{
				try
				{
					String percentage = line.split(": +")[1];
					percentage = percentage.substring(0, percentage.length() - 1);
					critDamage = Integer.parseInt(percentage);
					loreRemove.add(i);
				} catch (Exception e) {}
			} else if (line.startsWith("  Recovery Speed: +"))
			{
				try
				{
					String percentage = line.split(": +")[1];
					percentage = percentage.substring(0, percentage.length() - 1);
					recoverySpeed = Integer.parseInt(percentage);
					loreRemove.add(i);
				} catch (Exception e) {}
			} else if (line.startsWith("  Damage Reduction: +"))
			{
				try
				{
					String percentage = line.split(": +")[1];
					percentage = percentage.substring(0, percentage.length() - 1);
					damageReduction = Integer.parseInt(percentage);
					loreRemove.add(i);
				} catch (Exception e) {}
			} else if (line.startsWith("  Cooldown Reduction: "))
			{
				try
				{
					String percentage = line.split(": +")[1];
					percentage = percentage.substring(0, percentage.length() - 1);
					cooldownReduction = Integer.parseInt(percentage);
					loreRemove.add(i);
				} catch (Exception e) {}
			} else if (line.startsWith("Buff:"))
			{
				try
				{
					if (i > 0)
						loreRemove.add(-i + 1);
					loreRemove.add(-i);
				} catch (Exception e) {}
			} else if (line.equals(" * Consumable"))
			{
				consumable = true;
				loreRemove.add(-i);
			} else if (line.startsWith(" * Satiate: -"))
			{
				try
				{
					String num = line.split(": -")[1];
					num = num.substring(0, num.length() - 7);
					satiate = (int) (Float.parseFloat(num) * 2);
					loreRemove.add(-i);
				} catch (Exception e) {}
			} else if (line.startsWith(" * Buff Duration: "))
			{
				try
				{
					String num = line.split(": ")[1];
					buffDuration = 0;
					for (String s: num.split(" "))
					{
						if (s.endsWith("s"))
							buffDuration += Integer.valueOf(s.substring(0, s.length() - 1)) * 20;
						else if (s.endsWith("m"))
							buffDuration += Integer.valueOf(s.substring(0, s.length() - 1)) * 20 * 60;
					}
					loreRemove.add(-i);
				} catch (Exception e) {}
			} else if (line.startsWith("Consumable Cooldown: "))
			{
				try
				{
					String num = line.split(": ")[1];
					consumableCooldown = 0;
					for (String s: num.split(" "))
					{
						if (s.endsWith("s"))
							consumableCooldown += Integer.valueOf(s.substring(0, s.length() - 1)) * 20;
						else if (s.endsWith("m"))
							consumableCooldown += Integer.valueOf(s.substring(0, s.length() - 1)) * 20 * 60;
					}
					loreRemove.add(-i + 1);
					loreRemove.add(-i);
				} catch (Exception e) {}
			} else if (line.startsWith(" * Magic Damage: +"))
			{
				try
				{
					String num = line.split(": +")[1];
					if (line.endsWith("%"))
						magicDamageMultiplier = CakeLibrary.convertAddedPercentageToMultiplier(
								Integer.parseInt(num.substring(0, num.length() - 1)));
					else
						magicDamageAdd = Integer.parseInt(num);
					loreRemove.add(-i);
				} catch (Exception e) {}
			} else if (line.startsWith(" * Brute Damage: +"))
			{
				try
				{
					String num = line.split(": +")[1];
					if (line.endsWith("%"))
						bruteDamageMultiplier = CakeLibrary.convertAddedPercentageToMultiplier(
								Integer.parseInt(num.substring(0, num.length() - 1)));
					else
						bruteDamageAdd = Integer.parseInt(num);

					loreRemove.add(-i);
				} catch (Exception e) {}
			} else if (line.startsWith(" * Attack Speed: +"))
			{
				try
				{
					String num = line.split(": +")[1];
					attackSpeedMultiplier = CakeLibrary.convertAddedPercentageToMultiplier(
							Integer.parseInt(num.substring(0, num.length() - 1)));
					loreRemove.add(-i);
				} catch (Exception e) {}
			} else if (line.startsWith(" * Crit Chance: +"))
			{
				try
				{
					String num = line.split(": +")[1];
					critChanceAdd = Integer.parseInt(num.substring(0, num.length() - 1));
					loreRemove.add(-i);
				} catch (Exception e) {}
			} else if (line.startsWith(" * Crit Damage: +"))
			{
				try
				{
					String num = line.split(": +")[1];
					critDamageAdd = Integer.parseInt(num.substring(0, num.length() - 1));
					loreRemove.add(-i);
				} catch (Exception e) {}
			} else if (line.startsWith(" * Damage Reduction: +"))
			{
				try
				{
					String num = line.split(": +")[1];
					damageReductionAdd = Integer.parseInt(num.substring(0, num.length() - 1));
					loreRemove.add(-i);
				} catch (Exception e) {}
			} else if (line.startsWith(" * Cooldown Reduction: +"))
			{
				try
				{
					String num = line.split(": +")[1];
					cooldownReductionAdd = Integer.parseInt(num.substring(0, num.length() - 1));
					loreRemove.add(-i);
				} catch (Exception e) {}
			} else if (line.startsWith(" * Recovery Speed: +"))
			{
				try
				{
					String num = line.split(": +")[1];
					recoverySpeedAdd = Integer.parseInt(num.substring(0, num.length() - 1));
					loreRemove.add(-i);
				} catch (Exception e) {}
			} else if (line.startsWith(" * Combat XP: +"))
			{
				try
				{
					String num = line.split(": +")[1];
					xpMultiplierFood = CakeLibrary.convertAddedPercentageToMultiplier(
							Integer.parseInt(num.substring(0, num.length() - 1)));
					loreRemove.add(-i);
				} catch (Exception e) {}
			} else if (line.startsWith("  --- Tier "))
			{
				bonusStatLines = true;
				loreRemove.add(i - 1);
				loreRemove.add(i);
			} else if (line.equals("  ------------"))
			{
				bonusStatLines = false;
				loreRemove.add(i);
			} else if (bonusStatLines)
			{
				loreRemove.add(i);
			} else if (line.startsWith("Owner: "))
			{
				owner = line.split(": ")[1];
				loreRemove.add(i);
			} else if (line.equals(""))
				continue;
		}
		for (int i = loreRemove.size() - 1; i >= 0; i--)
		{
			int index = loreRemove.get(i);
			if (index < 0)
			{
				if (consumable)
					lore.remove(-index);
			} else
				lore.remove(index);
		}

		itemLore = lore;
		itemVanilla = nameChange ? CakeLibrary.renameItem(is.clone(), name) : is.clone(); 

		if ((headTexture == null || headTexture.length() == 0) 
				&& itemVanilla.getType().equals(Material.SKULL_ITEM) 
				&& itemVanilla.getDurability() == (short) 3)
		{
			SkullMeta headMeta = (SkullMeta) itemVanilla.getItemMeta();
			Field profileField = null;
			try {
				profileField = headMeta.getClass().getDeclaredField("profile");
				profileField.setAccessible(true);
				GameProfile profile = (GameProfile) profileField.get(headMeta);
				Property property = profile.getProperties().get("textures").iterator().next();
				headTexture = property.getValue();
			} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e1) {}
		}
	}

	public ItemStack createItem()
	{
		ItemStack is = itemVanilla.clone();
		ArrayList<String> lore = new ArrayList<String>();

		//ENHANCEMENT
		if (tier > 0)
		{
			if (!tierLore)
				is = CakeLibrary.renameItem(is, CakeLibrary.getItemName(itemVanilla) + tiers[tier - 1]);
			else
				lore.add(tiers[tier - 1]);
		}

		if (accessory)
			lore.add(CakeLibrary.recodeColorCodes("&eAccessory"));

		//BASE STATS
		if (!consumable)
		{
			if (levelRequirement != 0)
				lore.add(CakeLibrary.recodeColorCodes(statColor + "  Lv. Requirement:" + statColorSecondary + " " + levelRequirement));
			if (magicDamage != 0)
				lore.add(CakeLibrary.recodeColorCodes(statColor + "  Magic Damage:" + statColorSecondary + " +" + (magicDamage + addedMagicDamage))
						+ (addedMagicDamage > 0 ? " (+" + addedMagicDamage + ")" : ""));
			if (bruteDamage != 0)
				lore.add(CakeLibrary.recodeColorCodes(statColor + "  Brute Damage:" + statColorSecondary + " +" + (bruteDamage + addedBruteDamage))
						+ (addedBruteDamage > 0 ? " (+" + addedBruteDamage + ")" : ""));
			if (damageReduction != 0)
				lore.add(CakeLibrary.recodeColorCodes(statColor + "  Damage Reduction:" + statColorSecondary + " +" + damageReduction + "%"));
			if (cooldownReduction != 0)
				lore.add(CakeLibrary.recodeColorCodes(statColor + "  Cooldown Reduction:" + statColorSecondary + " +" + cooldownReduction + "%"));
			if (critChance != 0)
				lore.add(CakeLibrary.recodeColorCodes(statColor + "  Crit Chance:" + statColorSecondary + " +" + critChance + "%"));
			if (critDamage != 0)
				lore.add(CakeLibrary.recodeColorCodes(statColor + "  Crit Damage:" + statColorSecondary + " +" + critDamage + "%"));
			if (attackSpeed != 0)
				lore.add(CakeLibrary.recodeColorCodes(statColor + "  Attack Speed:" + statColorSecondary +" x" + attackSpeed));
			if (recoverySpeed != 0)
				lore.add(CakeLibrary.recodeColorCodes(statColor + "  Recovery Speed:" + statColorSecondary + " +" + recoverySpeed + "%"));
			if (xpMultiplier != 0)
				lore.add(CakeLibrary.recodeColorCodes(statColor + "  Combat XP:" + statColorSecondary +" x" + xpMultiplier));
		} else
		{
			lore.add(CakeLibrary.recodeColorCodes("&7 * Consumable"));
			if (satiate != 0)
				lore.add(CakeLibrary.recodeColorCodes("&7 * Satiate: -" + (satiate / 2.0F) + " hunger"));
			if (buffDuration != 0)
			{
				lore.add(CakeLibrary.recodeColorCodes("&f"));
				lore.add(CakeLibrary.recodeColorCodes("&7Buff:"));
			}
			if (magicDamageAdd != 0)
				lore.add(CakeLibrary.recodeColorCodes("&7 * Magic Damage: +" + magicDamageAdd));
			if (magicDamageMultiplier != 0)
				lore.add(CakeLibrary.recodeColorCodes("&7 * Magic Damage: +" + CakeLibrary.convertMultiplierToAddedPercentage(magicDamageMultiplier) + "%"));
			if (bruteDamageAdd != 0)
				lore.add(CakeLibrary.recodeColorCodes("&7 * Brute Damage: +" + bruteDamageAdd));
			if (bruteDamageMultiplier != 0)
				lore.add(CakeLibrary.recodeColorCodes("&7 * Brute Damage: +" + CakeLibrary.convertMultiplierToAddedPercentage(bruteDamageMultiplier) + "%"));
			if (damageReductionAdd != 0)
				lore.add(CakeLibrary.recodeColorCodes("&7 * Damage Reduction: +" + damageReductionAdd + "%"));
			if (cooldownReductionAdd != 0)
				lore.add(CakeLibrary.recodeColorCodes("&7 * Cooldown Reduction: +" + cooldownReductionAdd + "%"));
			if (critChanceAdd != 0)
				lore.add(CakeLibrary.recodeColorCodes("&7 * Crit Chance: +" + critChanceAdd + "%"));
			if (critDamageAdd != 0)
				lore.add(CakeLibrary.recodeColorCodes("&7 * Crit Damage: +" + critDamageAdd + "%"));
			if (attackSpeedMultiplier != 0)
				lore.add(CakeLibrary.recodeColorCodes("&7 * Attack Speed: +" + CakeLibrary.convertMultiplierToAddedPercentage(attackSpeedMultiplier) + "%"));
			if (recoverySpeedAdd != 0)
				lore.add(CakeLibrary.recodeColorCodes("&7 * Recovery Speed: +" + recoverySpeedAdd + "%"));
			if (xpMultiplierFood != 0)
				lore.add(CakeLibrary.recodeColorCodes("&7 * Combat XP: +" + CakeLibrary.convertMultiplierToAddedPercentage(xpMultiplierFood) + "%"));
			if (buffDuration != 0)
				lore.add(CakeLibrary.recodeColorCodes("&7 * Buff Duration: " + CakeLibrary.convertTimeToString(buffDuration / 20)));
			if (consumableCooldown != 0)
			{
				lore.add(CakeLibrary.recodeColorCodes("&f"));
				lore.add(CakeLibrary.recodeColorCodes("&7Consumable Cooldown: " + CakeLibrary.convertTimeToString(consumableCooldown / 20)));
			}
		}

		//BONUS STATS
		if (bonusStat != null)
		{
			lore.add(CakeLibrary.recodeColorCodes("&f"));
			String color = BonusStat.getTierColor(bonusStat.tier);
			lore.add(color + "  --- Tier " + bonusStat.tier + " ---");
			for (int i = 0; i < bonusStat.statLines.size(); i++)
				lore.add(color + bonusStat.statLines.get(i).getDescriptionWithValueRoll(bonusStat.tier, bonusStat.statLower.get(i)));
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

	public ItemStack createBaseItem()
	{
		ItemStack is = itemVanilla.clone();
		ArrayList<String> lore = new ArrayList<String>();

		if (accessory)
			lore.add(CakeLibrary.recodeColorCodes("&eAccessory"));

		//BASE STATS
		if (!consumable)
		{
			if (levelRequirement != 0)
				lore.add(CakeLibrary.recodeColorCodes(statColor + "  Lv. Requirement:" + statColorSecondary + " " + levelRequirement));
			if (magicDamage != 0)
				lore.add(CakeLibrary.recodeColorCodes(statColor + "  Magic Damage:" + statColorSecondary + " +" + magicDamage));
			if (bruteDamage != 0)
				lore.add(CakeLibrary.recodeColorCodes(statColor + "  Brute Damage:" + statColorSecondary + " +" + bruteDamage));
			if (damageReduction != 0)
				lore.add(CakeLibrary.recodeColorCodes(statColor + "  Damage Reduction:" + statColorSecondary + " +" + damageReduction + "%"));
			if (cooldownReduction != 0)
				lore.add(CakeLibrary.recodeColorCodes(statColor + "  Cooldown Reduction:" + statColorSecondary + " +" + cooldownReduction + "%"));
			if (critChance != 0)
				lore.add(CakeLibrary.recodeColorCodes(statColor + "  Crit Chance:" + statColorSecondary + " +" + critChance + "%"));
			if (critDamage != 0)
				lore.add(CakeLibrary.recodeColorCodes(statColor + "  Crit Damage:" + statColorSecondary + " +" + critDamage + "%"));
			if (attackSpeed != 0)
				lore.add(CakeLibrary.recodeColorCodes(statColor + "  Attack Speed:" + statColorSecondary +" x" + attackSpeed));
			if (recoverySpeed != 0)
				lore.add(CakeLibrary.recodeColorCodes(statColor + "  Recovery Speed:" + statColorSecondary + " +" + recoverySpeed + "%"));
			if (xpMultiplier != 0)
				lore.add(CakeLibrary.recodeColorCodes(statColor + "  Combat XP:" + statColorSecondary +" x" + xpMultiplier));
		} else
		{
			lore.add(CakeLibrary.recodeColorCodes("&7 * Consumable"));
			if (satiate != 0)
				lore.add(CakeLibrary.recodeColorCodes("&7 * Satiate: -" + (satiate / 2.0F) + " hunger"));
			if (buffDuration != 0)
			{
				lore.add(CakeLibrary.recodeColorCodes("&f"));
				lore.add(CakeLibrary.recodeColorCodes("&7Buff:"));
			}
			if (magicDamageAdd != 0)
				lore.add(CakeLibrary.recodeColorCodes("&7 * Magic Damage: +" + magicDamageAdd));
			if (magicDamageMultiplier != 0)
				lore.add(CakeLibrary.recodeColorCodes("&7 * Magic Damage: +" + CakeLibrary.convertMultiplierToAddedPercentage(magicDamageMultiplier) + "%"));
			if (bruteDamageAdd != 0)
				lore.add(CakeLibrary.recodeColorCodes("&7 * Brute Damage: +" + bruteDamageAdd));
			if (bruteDamageMultiplier != 0)
				lore.add(CakeLibrary.recodeColorCodes("&7 * Brute Damage: +" + CakeLibrary.convertMultiplierToAddedPercentage(bruteDamageMultiplier) + "%"));
			if (attackSpeedMultiplier != 0)
				lore.add(CakeLibrary.recodeColorCodes("&7 * Attack Speed: +" + CakeLibrary.convertMultiplierToAddedPercentage(attackSpeedMultiplier) + "%"));
			if (critChanceAdd != 0)
				lore.add(CakeLibrary.recodeColorCodes("&7 * Crit Chance: +" + critChanceAdd + "%"));
			if (critDamageAdd != 0)
				lore.add(CakeLibrary.recodeColorCodes("&7 * Crit Damage: +" + critDamageAdd + "%"));
			if (recoverySpeedAdd != 0)
				lore.add(CakeLibrary.recodeColorCodes("&7 * Recovery Speed: +" + recoverySpeedAdd + "%"));
			if (damageReductionAdd != 0)
				lore.add(CakeLibrary.recodeColorCodes("&7 * Damage Reduction: +" + damageReductionAdd + "%"));
			if (cooldownReductionAdd != 0)
				lore.add(CakeLibrary.recodeColorCodes("&7 * Cooldown Reduction: +" + cooldownReductionAdd + "%"));
			if (xpMultiplierFood != 0)
				lore.add(CakeLibrary.recodeColorCodes("&7 * Combat XP: +" + CakeLibrary.convertMultiplierToAddedPercentage(xpMultiplierFood) + "%"));
			if (buffDuration != 0)
				lore.add(CakeLibrary.recodeColorCodes("&7 * Buff Duration: " + CakeLibrary.convertTimeToString(buffDuration / 20)));
			if (consumableCooldown != 0)
			{
				lore.add(CakeLibrary.recodeColorCodes("&f"));
				lore.add(CakeLibrary.recodeColorCodes("&7Consumable Cooldown: " + CakeLibrary.convertTimeToString(consumableCooldown / 20)));
			}
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

	public int getTier()
	{
		return tier;
	}

	public void setTier(int num)
	{
		tier = num;
		
		float m = magicDamage;
		float b = bruteDamage;
		
		for (int i = 0; i < num; i++)
		{
			m *= tierStatMultiplier;
			b *= tierStatMultiplier;
		}
		
		addedMagicDamage = (int) m - magicDamage;
		addedBruteDamage = (int) b - bruteDamage;
	}

	public void saveToFile(String fileName)
	{
		saveToFile(new File("plugins/RPGCore/items/" + fileName + ".yml"));
	}

	public void saveToFile(File file)
	{
		ArrayList<String> lines = new ArrayList<String>();
		lines.add("id: " + itemVanilla.getTypeId());
		lines.add("durability: " + itemVanilla.getDurability());
		lines.add("amount: " + itemVanilla.getAmount());
		if (tier > 0)
			lines.add("tier: " + tier);
		if (dropRoll > 0)
			lines.add("dropRoll: " + dropRoll);
		if (headTexture != null && headTexture.length() > 0)
			lines.add("headTexture: " + headTexture);

		ItemMeta im = itemVanilla.getItemMeta();
		if (im != null)
		{
			try
			{
				if (im.spigot().isUnbreakable())
					lines.add("unbreakable: " + true);
			} catch (Exception e) {}
			lines.add("");
			String name = im.getDisplayName();
			if (name != null)
				lines.add("name: " + name);
			List<String> lore = im.getLore();
			if (lore != null)
			{
				lines.add("lore: ");
				for (String s: lore)
				{
					if (CakeLibrary.removeColorCodes(s).equals(" "))
						s = s.replaceAll(" ", "");
					lines.add(" " + s);
				}
			}
			if (im instanceof LeatherArmorMeta)
			{
				Color c = ((LeatherArmorMeta) im).getColor();
				lines.add("leatherArmorColor: " + c.getRed() + ", " + c.getGreen() + ", " + c.getBlue());
			}
			if (im instanceof BookMeta)
			{
				BookMeta bm = (BookMeta) im;
				lines.add("bookAuthor: " + bm.getAuthor());
				lines.add("bookTitle: " + bm.getTitle());
				lines.add("bookPages: ");
				for (String page: bm.getPages())
					lines.add(" " + page);
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

	public static RItem readFromFile(File file)
	{
		try
		{
			int id = 0;
			int amount = 0;
			short durability = 0;
			boolean unbreakable = false;
			int tier = 0;
			int dropRoll = 0;
			int red = -1;
			int green = -1;
			int blue = -1;
			String headTexture = null;
			List<String> bookPages = new ArrayList<String>();
			String bookTitle = null;
			String bookAuthor = null;

			String name = null;
			ArrayList<String> lore = new ArrayList<String>();

			ArrayList<Enchantment> enchs = new ArrayList<Enchantment>();
			ArrayList<Integer> levels = new ArrayList<Integer>();

			ArrayList<String> lines = CakeLibrary.readFile(file);
			String header = "";
			for (String line: lines)
			{
				String[] split = line.split(": ");
				if (line.startsWith(" "))
				{
					split[0] = split[0].substring(1);
					line = line.substring(1);
					if (header.equals("lore: "))
					{
						if (CakeLibrary.removeColorCodes(line).equals(" "))
							line = line.replaceAll(" ", "");
						lore.add(line);
					}
					else if (header.equals("enchantments: "))
					{
						enchs.add(Enchantment.getById(Integer.valueOf(split[0])));
						levels.add(Integer.valueOf(split[1]));
					} else if (header.equals("bookPages: "))
						bookPages.add(line);
				} else
				{
					header = line;
					if (line.startsWith("id: "))
						id = Integer.valueOf(split[1]);
					else if (line.startsWith("amount: "))
						amount = Integer.valueOf(split[1]);
					else if (line.startsWith("durability: "))
						durability = Short.valueOf(split[1]);
					else if (line.startsWith("unbreakable: "))
						unbreakable = Boolean.valueOf(split[1]);
					else if (line.startsWith("tier: "))
						tier = Integer.valueOf(split[1]);
					else if (line.startsWith("dropRoll: "))
						dropRoll = Integer.valueOf(split[1]);
					else if (line.startsWith("headTexture: "))
						headTexture = split[1];
					else if (line.startsWith("name: "))
						name = split[1];
					else if (line.startsWith("leatherArmorColor: "))
					{
						String[] args = split[1].split(", ");
						red = Integer.valueOf(args[0]);
						green = Integer.valueOf(args[1]);
						blue = Integer.valueOf(args[2]);
					}
					else if (line.startsWith("bookTitle: "))
						bookTitle = split[1];
					else if (line.startsWith("bookAuthor: "))
						bookAuthor = split[1];
				}
			}

			ItemStack item;

			if (headTexture != null && headTexture.length() > 0)
				item = CakeLibrary.getSkullWithTexture(headTexture);
			else
				item = new ItemStack(id);

			item.setAmount(amount);
			item.setDurability(durability);

			ItemMeta im = item.getItemMeta();
			if (name != null)
				im.setDisplayName(name);
			if (lore.size() > 0)
				im.setLore(lore);
			if (unbreakable)
				im.spigot().setUnbreakable(unbreakable);
			item.setItemMeta(im);
			
			if (red != -1 && green != -1 && blue != -1)
			{
				LeatherArmorMeta lim = (LeatherArmorMeta) im;
				lim.setColor(Color.fromRGB(red, green, blue));
				item.setItemMeta(lim);
			}
			
			if (bookPages.size() > 0)
			{
				BookMeta bm = (BookMeta) im;
				bm.setPages(bookPages);
				bm.setAuthor(bookAuthor);
				bm.setTitle(bookTitle);
				item.setItemMeta(bm);
			}

			for (int i = 0; i < enchs.size(); i++)
				item.addUnsafeEnchantment(enchs.get(i), levels.get(i));

			RItem ri = new RItem(item, file.getName().substring(0, file.getName().length() - 4));
			ri.file = file;
			ri.setTier(tier);
			ri.headTexture = headTexture;
			ri.dropRoll = dropRoll;
			return ri;
		} catch (Exception e) {
			RPGCore.msgConsole("&4Unable to read item file: " + file.getName());
			e.printStackTrace();
		}
		return null;
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
			if (tier < 3)
				return false;
			if (bonusStat != null)
				return false;
			ArrayList<BonusStatType> list = new ArrayList<BonusStatType>();
			ArrayList<Boolean> listLower = new ArrayList<Boolean>();
			list.add(BonusStatType.rollRandomStat());
			listLower.add(BonusStat.rand.nextBoolean());
			bonusStat = new BonusStat(1, list, listLower);
			return true;
		}
		case ALL_LINES_REROLL:
		{
			if (bonusStat == null)
				return false;
			bonusStat.statLower.clear();
			for (int i = 0; i < bonusStat.statLines.size(); i++)
			{
				bonusStat.statLines.set(i, BonusStatType.rollRandomStat());
				bonusStat.statLower.add(BonusStat.rand.nextBoolean());
			}
			return true;
		}
		case LINE_AMOUNT_ADDER:
		{
			if (tier - 3 - bonusStat.statLines.size() < 0)
				return false;
			if (bonusStat == null)
				return false;
			bonusStat.statLines.add(BonusStatType.rollRandomStat());
			bonusStat.statLower.add(BonusStat.rand.nextBoolean());
			return true;
		}
		case TIER_REROLL:
		{
			if (bonusStat == null)
				return false;
			if (bonusStat.tier >= 3)
				return false;
			if (rand.nextInt(BonusStat.tierIncreaseRoll) != 0)
				return true;
			bonusStat.tier++;
			return true;
		}
		case TIER_REROLL_GREATER:
		{
			if (bonusStat == null)
				return false;
			if (bonusStat.tier >= 5)
				return false;
			if (rand.nextInt(BonusStat.tierIncreaseRollGreater) != 0)
				return true;
			bonusStat.tier++;
			return true;
		}
		}
		return false;
	}
}
