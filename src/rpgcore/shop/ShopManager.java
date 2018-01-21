package rpgcore.shop;

import java.io.File;
import java.util.ArrayList;

import org.bukkit.inventory.Inventory;

import rpgcore.item.RItem;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGCore;

public class ShopManager 
{
	public static ArrayList<Inventory> openShops = new ArrayList<Inventory>();
	public static ArrayList<Shop> shopDatabase = new ArrayList<Shop>();
	public static File shopsFile = new File("plugins/RPGCore/shops");

	public ShopManager()
	{

	}
	
	public static Shop getShopWithDB(String dbName)
	{
		for (Shop shop: shopDatabase)
			if (shop.dbName.equalsIgnoreCase(dbName))
				return shop;
		return null;
	}

	public static void readShopDatabase()
	{
		shopDatabase.clear();
		for (File file: shopsFile.listFiles())
		{
			String shopName = null;
			ArrayList<ShopItem> shopItems = new ArrayList<ShopItem>();
			try
			{
				ArrayList<String> lines = CakeLibrary.readFile(file);
				String header = "";
				for (String s: lines)
				{
					if (s.startsWith(" "))
					{
						s = s.substring(1);
						if (header.equals("items:"))
						{
							String[] split = s.split(", ");
							RItem ri = RPGCore.instance.getItemFromDatabase(split[0]);
							if (ri == null)
							{
								RPGCore.msgConsole("&4Error while reading shop \"&c" + file.getName() + "&4\" - \"&c" + split[0] + "&4\" is not an item!");
								continue;
							}
							int cost = Integer.parseInt(split[1]);
							int slot = Integer.parseInt(split[2]);
							ShopItem si = new ShopItem(ri, cost, slot);
							shopItems.add(si);
						}
					} else
					{
						String[] split = s.split(": ");
						if (s.startsWith("shopname: "))
						{
							shopName = CakeLibrary.recodeColorCodes(split[1]);
							continue;
						}
						header = s;
					}
				}
			} catch (Exception e) {
				RPGCore.msgConsole("&4Unable to read shop file: " + file.getName());
			}
			Shop shop = new Shop(shopName, shopItems);
			shop.dbName = file.getName().substring(0, file.getName().length() - 4);
			shopDatabase.add(shop);
		}
	}
}
