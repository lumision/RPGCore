package rpgcore.player;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import rpgcore.item.RItem;
import rpgcore.main.CakeLibrary;

public class Mailbox 
{
	public RPlayer owner;
	public ArrayList<RItem> items;

	private Inventory mailboxInventory;
	public static final String mailboxDescriptionPrefix = "§a§a§a";
	static final String invTitle = CakeLibrary.recodeColorCodes("&2Mailbox");

	public Mailbox(RPlayer owner)
	{
		this.owner = owner;
		items = new ArrayList<RItem>();
	}

	public Inventory getMailboxInventory()
	{
		if (mailboxInventory != null)
			return mailboxInventory;
		mailboxInventory = Bukkit.createInventory(null, 27, invTitle);
		updateMailboxInventory();
		return mailboxInventory;
	}

	public void updateMailboxInventory()
	{
		if (mailboxInventory == null)
		{
			getMailboxInventory();
			return;
		}
		mailboxInventory.clear();
		if (items.size() > 0)
			for (int i = 0; i < Math.min(items.size(), mailboxInventory.getSize()); i++)
				mailboxInventory.setItem(i, items.get(i).createItem());
	}
	
	public static ItemStack removeMailDescription(ItemStack item)
	{
		ItemMeta im = item.getItemMeta();
		if (im.getLore() == null)
			return item;
		List<String> lore = im.getLore();
		for (int i = 0; i < lore.size(); i++)
		{
			String line = lore.get(i);
			if (line.startsWith(mailboxDescriptionPrefix))
			{
				lore.remove(i);
				i--;
			}
		}
		im.setLore(lore);
		item.setItemMeta(im);
		return item;
	}
}
