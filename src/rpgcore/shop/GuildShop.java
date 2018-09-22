package rpgcore.shop;

import java.io.File;
import java.util.LinkedHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import rpgcore.item.RItem;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGCore;

public class GuildShop 
{
	public static String inventoryName = CakeLibrary.recodeColorCodes("&2Receptionist");
	public static String priceListInventoryName = CakeLibrary.recodeColorCodes("&2Item Prices - Page ");
	public static ItemStack slotSell = CakeLibrary.renameItem(new ItemStack(Material.GOLD_NUGGET, 1, (short) 0),
			"§a§nSell all items");
	public static ItemStack slotInfo = CakeLibrary.editNameAndLore(new ItemStack(Material.PAPER, 1, (short) 0),
			"§e§nInfo",
			"§7Left-click to transfer",
			"§7the entire stack.",
			"§f",
			"§7Right-click to add or",
			"§7subtract one by one.");

	public static final File pricesFolder = new File("plugins/RPGCore/item-prices");

	public static LinkedHashMap<RItem, Integer> itemPrices = new LinkedHashMap<RItem, Integer>();

	public static void readItemPrices()
	{
		pricesFolder.mkdirs();
		itemPrices.clear();
		for (File file: pricesFolder.listFiles())
		{
			try
			{
				itemPrices.put(RItem.readRItemFile(file), 
						Integer.valueOf(file.getName().substring(0, file.getName().length() - 4).split("_")[1]));
			} catch (Exception e)
			{
				RPGCore.msgConsole("Error reading item price file: " + file.getName());
				e.printStackTrace();
			}
		}
	}

	public static void saveItemPrices()
	{
		pricesFolder.mkdirs();
		for (File file: pricesFolder.listFiles())
			file.delete();

		for (RItem key: itemPrices.keySet())
		{
			key.saveItemToFile(new File(pricesFolder.getPath() + "/" 
					+ CakeLibrary.removeColorCodes(CakeLibrary.getItemName(key.itemVanilla)) + "_" + itemPrices.get(key) + ".yml"));
		}
	}

	public static int getItemPriceListPages()
	{
		int size = itemPrices.size();
		int pages = 1;
		while (size > 54)
		{
			size -= 54;
			pages++;
		}
		return pages;
	}

	public static Inventory getItemPriceList(int page)
	{
		Inventory inv = Bukkit.createInventory(null, 54, priceListInventoryName + (page + 1));

		RItem[] keySet = new RItem[itemPrices.size()];
		int i = 0;
		for (RItem key: itemPrices.keySet())
		{
			keySet[i] = key;
			i++;
		}

		int offset = page * 54;
		int max = Math.min(offset + 54, keySet.length);
		for (i = offset; i < max; i++)
			inv.setItem(i - offset, convertToPriced(keySet[i].createItem(), itemPrices.get(keySet[i])));

		return inv;
	}

	public static Inventory getCustomItemPriceList(int page)
	{
		Inventory inv = Bukkit.createInventory(null, 54, priceListInventoryName + (page + 1));

		RItem[] keySet = new RItem[itemPrices.size()];
		int i = 0;
		for (RItem key: itemPrices.keySet())
		{
			if (key.itemVanilla.getItemMeta().getDisplayName() != null)
			{
				keySet[i] = key;
				i++;
			}
		}

		int offset = page * 54;
		int max = Math.min(offset + 54, i);
		for (i = offset; i < max; i++)
			inv.setItem(i - offset, convertToPriced(keySet[i].createItem(), itemPrices.get(keySet[i])));

		return inv;
	}

	public static Inventory getVanillaItemPriceList(int page)
	{
		Inventory inv = Bukkit.createInventory(null, 54, priceListInventoryName + (page + 1));

		RItem[] keySet = new RItem[itemPrices.size()];
		int i = 0;
		for (RItem key: itemPrices.keySet())
		{
			if (key.itemVanilla.getItemMeta().getDisplayName() == null)
			{
				keySet[i] = key;
				i++;
			}
		}

		int offset = page * 54;
		int max = Math.min(offset + 54, i);
		for (i = offset; i < max; i++)
			inv.setItem(i - offset, convertToPriced(keySet[i].createItem(), itemPrices.get(keySet[i])));

		return inv;
	}
	
	public static boolean canBeSold(ItemStack is)
	{
		RItem ri = new RItem(is);
		RItem check = null;
		for (RItem key: itemPrices.keySet())
			if (key.compare(ri))
				check = key;
		return check != null;
	}

	public static Inventory getGuildShopInventory()
	{
		Inventory inv = Bukkit.createInventory(null, 27, inventoryName);
		inv.setItem(inv.getSize() - 1, slotSell.clone());
		inv.setItem(inv.getSize() - 2, slotInfo.clone());
		return inv;
	}

	public static ItemStack convertToPriced(ItemStack item)
	{
		RItem check = null;
		RItem ri = new RItem(item);
		for (RItem key: itemPrices.keySet())
			if (key.compare(ri))
				check = key;

		if (check == null)
			return null;

		return convertToPriced(item, itemPrices.get(check) * item.getAmount());
	}

	public static ItemStack convertToPriced(ItemStack item, int price)
	{
		ItemMeta im = item.getItemMeta();
		im.setDisplayName(
				CakeLibrary.recodeColorCodes("&f" + CakeLibrary.getItemName(item) + "&a (&2" + price + " Gold&a)"));
		item.setItemMeta(im);
		return item;
	}

	public static ItemStack convertToUnpriced(ItemStack item)
	{
		ItemMeta im = item.getItemMeta();
		if (im.getDisplayName() == null)
			return item;
		im.setDisplayName(im.getDisplayName().startsWith("§f§") ? im.getDisplayName().substring(2).split("§a \\(§2")[0] : null);
		item.setItemMeta(im);
		return item;
	}
	
	public static void updateTotalPrice(Inventory inv)
	{
		inv.setItem(inv.getSize() - 1, CakeLibrary.addLore(slotSell.clone(), "§a * §2" + calculateTotalInventoryPrice(inv) + " Gold"));
	}
	
	public static int calculateTotalInventoryPrice(Inventory inv)
	{
		int total = 0;
		for (int i = 0; i < inv.getSize() - 2; i++)
		{
			try
			{
				ItemStack item = inv.getItem(i);
				if (CakeLibrary.isItemStackNull(item))
					break;
				ItemMeta im = item.getItemMeta();
				if (im.getDisplayName() == null)
					continue;
				String s = im.getDisplayName().split("§a \\(§2")[1];
				total += Integer.parseInt(s.substring(0, s.length() - 8));
			} catch (Exception e) {}
		}
		return total;
	}
}
