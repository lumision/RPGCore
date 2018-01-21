package rpgcore.shop;

import org.bukkit.inventory.ItemStack;

import rpgcore.item.RItem;
import rpgcore.main.CakeLibrary;

public class ShopItem 
{
	public int cost, slot;
	public RItem item;
	public ShopItem(RItem item, int cost, int slot)
	{
		this.item = item;
		this.cost = cost;
		this.slot = slot;
	}
	
	public ItemStack getItemWithCost()
	{
		ItemStack is = item.createItem();
		//is = CakeLibrary.addLore(is, "&f ", "&6Cost: &e&n" + cost + " Gold");
		is = CakeLibrary.renameItem(is, CakeLibrary.getItemName(is) + "&f &6(&e" + cost + " Gold&6)");
		return is;
	}
}
