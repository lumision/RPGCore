package rpgcore.player;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import rpgcore.item.RItem;
import rpgcore.main.CakeLibrary;

public class AccessoryInventory 
{
	public RPlayer owner;
	public RItem[] slots;
	private Inventory inventory;

	public static final ItemStack slotItem = CakeLibrary.renameItem(new ItemStack(160, 1, (short) 8), 
			"&7&oEmpty Slot");
	public static final ItemStack slotBlack = CakeLibrary.renameItem(new ItemStack(160, 1, (short) 15), "&f");
	static final String inventoryName = CakeLibrary.recodeColorCodes("&5Accessories");
	
	public AccessoryInventory(RPlayer owner)
	{
		this.owner = owner;
		this.slots = new RItem[3];
	}
	
	public AccessoryInventory(RPlayer rp, RItem... slots)
	{
		this.owner = rp;
		this.slots = slots;
	}
	
	public void updateInventory()
	{
		for (int i = 0; i < 27; i++)
		{
			if (i == 10 || i == 13 || i == 16)
			{
				int riSlot = (i - 10) / 3;
				if (slots[riSlot] != null)
					getInventory().setItem(i, slots[riSlot].createItem());
				else
					getInventory().setItem(i, slotItem);
			}
			else
				getInventory().setItem(i, slotBlack.clone());
		}
	}

	public Inventory getInventory()
	{
		if (inventory != null)
			return inventory;
		inventory = Bukkit.createInventory(null, 27, inventoryName);
		updateInventory();
		return inventory;
	}
}
