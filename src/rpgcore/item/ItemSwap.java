
package rpgcore.item;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGCore;

public class ItemSwap 
{
	public static ArrayList<ItemSwap> itemSwaps = new ArrayList<ItemSwap>();
	public static final File itemSwapsFile = new File("plugins/RPGCore/ItemSwaps.yml");
	public static int itemSwapVersion = 0;

	public RItem from;
	public RItem to;
	private ItemSwap(RItem from, RItem to)
	{
		this.from = from;
		this.to = to;
	}
	
	public static void addItemSwap(RItem from, RItem to)
	{
		itemSwaps.add(new ItemSwap(from, to));
		itemSwapVersion++;
		writeData();
	}
	
	public static class HolderVersion
	{
		public static ArrayList<HolderVersion> holderVersions = new ArrayList<HolderVersion>();
		
		public UUID player;
		public Location block;
		public int version;
		
		public HolderVersion(Location block, int version)
		{
			this.player = null;
			this.block = block;
			this.version = version;
		}
		
		public HolderVersion(UUID player, int version)
		{
			this.block = null;
			this.player = player;
			this.version = version;
		}
		
		public boolean upToDate()
		{
			return version == itemSwapVersion;
		}
		
		public static boolean updateVersion(Inventory inv)
		{
			UUID player = inv.getHolder() instanceof Player ? ((Player) inv.getHolder()).getUniqueId() : null;
			Location block = inv.getHolder() instanceof BlockState ? ((BlockState) inv.getHolder()).getLocation() : null;
			for (HolderVersion hv: holderVersions)
			{
				if (hv.player != null && player != null && player.equals(hv.player))
				{
						boolean r = hv.upToDate();
						hv.version = itemSwapVersion;
						ItemSwap.writeData();
						return r;
				} else if (hv.block != null && block != null
						&& hv.block.getBlockX() == block.getBlockX() 
								&& hv.block.getBlockY() == block.getBlockY()
								&& hv.block.getBlockZ() == block.getBlockZ())
				{
						boolean r = hv.upToDate();
						hv.version = itemSwapVersion;
						ItemSwap.writeData();
						return r;
				}
			}
			if (player != null)
				holderVersions.add(new HolderVersion(player, itemSwapVersion));
			else if (block != null)
				holderVersions.add(new HolderVersion(block, itemSwapVersion));
			ItemSwap.writeData();
			return false;
		}
	}

	public static void checkInventory(Inventory inv)
	{
		if (HolderVersion.updateVersion(inv))
			return;
		for (int i10 = 0; i10 < itemSwaps.size(); i10++)
		{
			ItemSwap swap = itemSwaps.get(i10);
			for (int i = 0; i < inv.getSize(); i++)
			{
				ItemStack item = inv.getItem(i);
				if (CakeLibrary.isItemStackNull(item))
					continue;
				if (i10 == 0)
				{
					ItemMeta im = item.getItemMeta();
					List<String> lore = im.getLore();
					if (lore != null)
					{
						for (int i1 = 0; i1 < lore.size(); i1++)
						{
							String s = lore.get(i1);
							if (CakeLibrary.removeColorCodes(s).equals(" "))
								lore.set(i1, s.replaceAll(" ", ""));
						}
						im.setLore(lore);
					}
					item.setItemMeta(im);
				}
				if (RItem.compare(item, swap.from.itemVanilla))
				{
					ItemStack change = swap.to.createItem();
					change.setAmount(item.getAmount());
					inv.setItem(i, change);
				} else if (i10 == 0)
					inv.setItem(i, item);
			}
		}
	}

	public static void readData()
	{
		itemSwaps.clear();
		RItem from = null;
		RItem to = null;
		for (String line: CakeLibrary.readFile(itemSwapsFile))
		{
			try
			{
				if (line.startsWith("itemSwapVersion: "))
				{
					itemSwapVersion = Integer.valueOf(line.split(": ")[1]);
					continue;
				} else if (line.equals("itemSwaps:") || line.equals("holderVersions:"))
					continue;
				else if (line.startsWith(" "))
				{
					line = line.substring(1);
					if (line.contains(": "))
					{
						String[] split = line.split(": ");
						String[] args = split[0].split(", ");
						if (args.length == 1)
							HolderVersion.holderVersions.add(new HolderVersion(UUID.fromString(args[0]), Integer.valueOf(split[1])));
						else
							HolderVersion.holderVersions.add(new HolderVersion(
									new Location(Bukkit.getWorld(args[0]), 
											Integer.valueOf(args[1]), Integer.valueOf(args[2]), Integer.valueOf(args[3])), 
									Integer.valueOf(split[1])));
					} else
					{
						String[] split = line.split(", ");
						from = RPGCore.getItemFromDatabase(split[0]);
						to = RPGCore.getItemFromDatabase(split[1]);
						if (from != null && to != null)
							itemSwaps.add(new ItemSwap(from, to));
					}
				}
			} catch (Exception e) 
			{
				RPGCore.msgConsole("Error reading ItemSwap line: " + line);
				e.printStackTrace();
			}
			from = to = null;
		}
	}

	public static void writeData()
	{
		ArrayList<String> lines = new ArrayList<String>();
		lines.add("itemSwapVersion: " + itemSwapVersion);
		lines.add("itemSwaps:");
		for (ItemSwap swap: itemSwaps)
			lines.add(" " + swap.from.databaseName + ", " + swap.to.databaseName);
		lines.add("holderVersions:");
		for (HolderVersion hv: HolderVersion.holderVersions)
		{
			try
			{
				lines.add(" " + (hv.player != null ? hv.player : 
					hv.block.getWorld().getName() + ", " + hv.block.getBlockX() + ", " + hv.block.getBlockY() + ", " + hv.block.getBlockZ())
						+ ": " + hv.version);
			} catch (Exception e) {}
		}
		CakeLibrary.writeFile(lines, itemSwapsFile);
	}
}
