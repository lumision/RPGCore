package rpgcore.kits;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import rpgcore.item.RItem;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGCore;
import rpgcore.player.RPlayer;

public class RPGKit
{
	public static ArrayList<RPGKit> kits = new ArrayList<RPGKit>();
	public static final File kitsFolder = new File("plugins/RPGCore/kits");

	public String kitName;
	public ArrayList<RItem> items;
	public int intervalDays, intervalHours, intervalMinutes;
	Inventory kitInventory;

	public static void globalCheck()
	{
		for (Player p: Bukkit.getOnlinePlayers())
			checkForPlayer(p, false);
		KitHistory.writeData();
	}
	
	public static void checkForPlayer(Player p, boolean save)
	{
		RPlayer rp = RPGCore.playerManager.getRPlayer(p.getUniqueId());
		if (rp == null)
			return;
		for (RPGKit kit: kits)
			if (p.hasPermission("rpgcore.kit." + kit.kitName) && !p.hasPermission("rpgcore.kitbypass"))
			{
				KitHistory history = KitHistory.getKitHistory(kit, p.getUniqueId());
				if (history == null)
				{
					KitHistory.kitHistories.add(new KitHistory(kit, p.getUniqueId(), new TimeTrack()));
					kit.sendToPlayer(rp);
				}
				else if (history.canReuse())
				{
					history.updateTimeTrack();
					kit.sendToPlayer(rp);
				}
			}
		if (save)
			KitHistory.writeData();
	}

	public static RPGKit getKit(String kitName)
	{
		for (RPGKit kit: kits)
			if (kit.kitName.equalsIgnoreCase(kitName))
				return kit;
		return null;
	}

	public static RPGKit createKit(String kitName, int intervalDays, int intervalHours, int intervalMinutes)
	{
		RPGKit kit = new RPGKit(kitName, intervalDays, intervalHours, intervalMinutes);
		kit.writeData();
		kits.add(kit);
		return kit;
	}

	private RPGKit(String kitName, int intervalDays, int intervalHours, int intervalMinutes)
	{
		this.kitName = kitName;
		this.intervalDays = intervalDays;
		this.intervalHours = intervalHours;
		this.intervalMinutes = intervalMinutes;
		this.items = new ArrayList<RItem>();
	}

	public void sendToPlayer(RPlayer rp)
	{
		for (RItem item: items)
		{
			rp.mailbox.items.add(item);
			/**
			rp.mailbox.items.add(new RItem(CakeLibrary.addLore(item.createItem(), "&a&a&a", 
					"&a&a&a&5Received from \"&d" + kitName + "&5\" kit", 
					"&a&a&a&5Interval:&d" + (intervalDays > 0 ? (" " + intervalDays + "day" + (intervalDays == 1 ? " " : "s ")) : "") 
					+ (intervalHours > 0 ? (" " + intervalHours + " hour" + (intervalHours == 1 ? "" : "s")) : "") 
					+ (intervalMinutes > 0 ? (" " + intervalMinutes + " minute" + (intervalMinutes == 1 ? "" : "s")) : ""))));
			 */
		}
		rp.setInvButtons();
		rp.mailbox.updateMailboxInventory();
		RPGCore.playerManager.writeData(rp);
	}

	public Inventory getKitInventory()
	{
		if (kitInventory != null)
			return kitInventory;
		kitInventory = Bukkit.createInventory(null, 27, CakeLibrary.recodeColorCodes("&5Kit - " + kitName));
		int index = 0;
		for (RItem key: items)
		{
			ItemStack item = key.createItem();
			kitInventory.setItem(index++, item);
		}
		return kitInventory;
	}

	public File getKitFolder()
	{
		return new File(kitsFolder.getPath() + "/" + kitName + "-" + intervalDays + "-" + intervalHours + "-" + intervalMinutes);
	}

	public void delete()
	{
		kits.remove(this);
		getKitFolder().delete();
	}

	public void writeData()
	{
		File folder = getKitFolder();
		if (folder.exists())
			folder.delete();
		folder.mkdirs();
		if (items.size() > 0)
			for (int i = 0; i < items.size(); i++)
				items.get(i).saveToFile(new File(folder.getPath() + "/" + i + ".yml"));
	}

	public static void readData()
	{
		kits.clear();
		RPGKit kit;
		if (kitsFolder.exists())
			for (File folder : kitsFolder.listFiles())
			{
				try
				{
					ArrayList<RItem> items = new ArrayList<RItem>();
					for (File item: folder.listFiles())
						if (item.getName().endsWith(".yml"))
							items.add(RItem.readFromFile(item));
					String[] split = folder.getName().split("-");
					kit = new RPGKit(split[0], Integer.valueOf(split[1]), Integer.valueOf(split[2]), Integer.valueOf(split[3]));
					kit.items = items;
					kits.add(kit);
				} catch (Exception e) 
				{
					RPGCore.msgConsole("&4Error reading kit folder: " + folder.getName());
					e.printStackTrace();
				}
			}
		KitHistory.readData();
	}

	public static class TimeTrack
	{
		public int year, dayOfYear, hour, minute;

		public TimeTrack()
		{
			Calendar c = Calendar.getInstance();
			year = c.get(Calendar.YEAR);
			dayOfYear = c.get(Calendar.DAY_OF_YEAR);
			hour = c.get(Calendar.HOUR_OF_DAY);
			minute = c.get(Calendar.MINUTE);
		}

		public TimeTrack(int year, int dayOfYear, int hour, int minute)
		{
			this.year = year;
			this.dayOfYear = dayOfYear;
			this.hour = hour;
			this.minute = minute;
			fixOffsets();
		}

		public void fixOffsets()
		{
			while (minute >= 60)
			{
				minute -= 60;
				hour++;
			}
			while (hour >= 24)
			{
				hour -= 24;
				dayOfYear++;
			}
			while (dayOfYear > 365)
			{
				dayOfYear -= 365;
				year++;
			}
		}

		public TimeTrack getClonedOffset(int years, int days, int hours, int minutes)
		{
			return new TimeTrack(this.year + years, this.dayOfYear + days, this.hour + hours, this.minute + minutes);
		}

		public boolean hasPassed(TimeTrack other)
		{
			if (this.year > other.year)
				return true;
			if (this.dayOfYear > other.dayOfYear)
				return true;
			if (this.dayOfYear == other.dayOfYear)
			{
				if (this.hour > other.hour)
					return true;
				else if (this.hour == other.hour && this.minute > other.minute)
					return true;
				else
					return false;
			}
			return false;
		}
	}

	public static class KitHistory
	{
		public static ArrayList<KitHistory> kitHistories = new ArrayList<KitHistory>();
		public RPGKit kit;
		public UUID player;
		private TimeTrack timeTrack;
		private TimeTrack timeTrackOffset;

		public static final File kitHistoryFile = new File("plugins/RPGCore/kit-histories.yml");

		public static KitHistory getKitHistory(RPGKit kit, UUID player)
		{
			for (KitHistory history: kitHistories)
				if (history.player.equals(player) && history.kit == kit)
					return history;
			return null;
		}

		public KitHistory(RPGKit kit, UUID player, TimeTrack timeTrack)
		{
			this.kit = kit;
			this.player = player;
			this.timeTrack = timeTrack;
		}

		private TimeTrack getTimeTrackOffset()
		{
			return timeTrackOffset != null ? timeTrackOffset :
				(timeTrackOffset = timeTrack.getClonedOffset(0, kit.intervalDays, kit.intervalHours, kit.intervalMinutes));
		}

		public void updateTimeTrack()
		{
			timeTrack = new TimeTrack();
			timeTrackOffset = null;
		}

		public boolean canReuse()
		{
			return new TimeTrack().hasPassed(getTimeTrackOffset());
		}

		public void check()
		{
			RPlayer rp = RPGCore.playerManager.getRPlayer(player);
			if (rp == null)
				return;
			if (rp.getPlayer() == null)
				return;
			if (rp.getPlayer().hasPermission("rpgcore.kitbypass"))
				return;
			if (!canReuse())
				return;
			updateTimeTrack();
			kit.sendToPlayer(rp);
		}

		public static void readData()
		{
			kitHistories.clear();
			String[] split;
			for (String line: CakeLibrary.readFile(kitHistoryFile))
			{
				try
				{
					split = line.split("::");
					kitHistories.add(new KitHistory(RPGKit.getKit(split[0]), UUID.fromString(split[1]), 
							new TimeTrack(Integer.parseInt(split[2]), Integer.parseInt(split[3]), 
									Integer.parseInt(split[4]), Integer.parseInt(split[5]))));
				} catch (Exception e)
				{
					RPGCore.msgConsole("Error reading kitHistory data line: &c" + line);
					e.printStackTrace();
				}
			}
		}

		public static void writeData()
		{
			ArrayList<String> lines = new ArrayList<String>();
			for (KitHistory history: kitHistories)
				lines.add(history.kit.kitName + "::" + history.player + "::" + 
						history.timeTrack.year + "::" + history.timeTrack.dayOfYear + "::" + history.timeTrack.hour + "::" + history.timeTrack.minute);
			CakeLibrary.writeFile(lines, kitHistoryFile);
		}
	}
}
