package rpgcore.item;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGCore;
import rpgcore.player.RPlayer;

public class GlobalGift 
{
	public static final File giftsFile = new File("plugins/RPGCore/global-gifts.yml");
	public static ArrayList<GlobalGift> gifts = new ArrayList<GlobalGift>();
	public static int highestGiftIndex = 0;

	public int expireDay, expireYear;
	public int giftIndex;
	public RItem item;
	private GlobalGift(RItem item, int giftIndex, int expireDay, int expireYear)
	{
		this.item = item;
		this.expireDay = expireDay;
		this.expireYear = expireYear;
		this.giftIndex = giftIndex;
	}

	public static void createGlobalGift(RItem item, int expireDay, int expireYear)
	{
		gifts.add(new GlobalGift(item, highestGiftIndex + 1, expireDay, expireYear));
		highestGiftIndex++;
	}

	public static void createGlobalGift(RItem item, int giftIndex, int expireDay, int expireYear)
	{
		if (highestGiftIndex < giftIndex)
			highestGiftIndex = giftIndex;
		gifts.add(new GlobalGift(item, giftIndex, expireDay, expireYear));
	}

	public static GlobalGift getGiftByIndex(int index)
	{
		for (GlobalGift gift: gifts)
			if (gift.giftIndex == index)
				return gift;
		return null;
	}
	
	public static void checkExpiries()
	{
		boolean save = false;
		Calendar calendar = Calendar.getInstance();
		for (int i = 0; i < gifts.size(); i++)
		{
			GlobalGift gift = gifts.get(i);
			if (calendar.get(Calendar.DAY_OF_YEAR) >= gift.expireDay && calendar.get(Calendar.YEAR) >= gift.expireYear)
			{
				save = true;
				gifts.remove(i);
				i--;
			}
		}
		if (save)
			writeData();
	}

	public static void checkForPlayer(RPlayer rp)
	{
		boolean change = false;
		while (rp.globalGiftIndex < highestGiftIndex)
		{
			change = true;
			rp.globalGiftIndex++;
			GlobalGift gift = getGiftByIndex(rp.globalGiftIndex);
			if (gift != null && gift.expireDay != 0)
				rp.mailbox.items.add(gift.item);
		}
		if (change)
		{
			rp.mailbox.updateMailboxInventory();
			RPGCore.playerManager.writeData(rp);
		}
	}

	public static void writeData()
	{
		ArrayList<String> lines = new ArrayList<String>();
		for (GlobalGift gift: gifts)
			lines.add(gift.item.databaseName + ", " + gift.giftIndex + ", " + gift.expireDay + ", " + gift.expireYear);
		CakeLibrary.writeFile(lines, giftsFile);
	}

	public static void readData()
	{
		if (!giftsFile.exists())
		return;
		gifts.clear();
		for (String line: CakeLibrary.readFile(giftsFile))
		{
			try
			{
				String[] split = line.split(", ");
				createGlobalGift(RPGCore.getItemFromDatabase(split[0]), Integer.valueOf(split[1]), Integer.valueOf(split[2]), Integer.valueOf(split[3]));
			} catch (Exception e)
			{
				RPGCore.msgConsole("Error reading gift file line: " + line);
				e.printStackTrace();
			}
		}
	}
}
