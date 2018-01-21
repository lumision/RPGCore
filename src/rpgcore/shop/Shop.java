package rpgcore.shop;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Shop 
{
	public String dbName;
	public String shopName;
	public ArrayList<ShopItem> shopItems;
	private int shopSize = -1;
	public Shop(String shopName, ArrayList<ShopItem> shopItems)
	{
		this.shopName = shopName;
		this.shopItems = shopItems;
	}
	
	public int getShopSize()
	{
		if (shopSize > 0)
			return shopSize;
		int highest = 0;
		for (ShopItem si: shopItems)
			if (si.slot > highest)
				highest = si.slot;
		for (shopSize = 0; shopSize <= highest; shopSize += 9);
		return shopSize;
	}
	
	public Inventory getShopInventory()
	{
		Inventory inv = Bukkit.createInventory(null, getShopSize(), shopName);
		for (ShopItem si: shopItems)
			inv.setItem(si.slot, si.getItemWithCost());
		ShopManager.openShops.add(inv);
		return inv;
	}
	
	public ItemStack getRawItem(int slot)
	{
		for (ShopItem si: shopItems)
			if (si.slot == slot)
				return si.item.createItem();
		return null;
	}
	
	public ShopItem getShopItem(int slot)
	{
		for (ShopItem si: shopItems)
			if (si.slot == slot)
				return si;
		return null;
	}
}
