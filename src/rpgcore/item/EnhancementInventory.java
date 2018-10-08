package rpgcore.item;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGCore;
import rpgcore.main.RPGEvents;

public class EnhancementInventory 
{
	public static final ItemStack slotBlack = CakeLibrary.renameItem(new ItemStack(160, 1, (short) 15), "&f");
	public static final ItemStack slotGray = CakeLibrary.renameItem(new ItemStack(160, 1, (short) 7), "&f");
	public static final ItemStack slotItem = CakeLibrary.editNameAndLore(new ItemStack(160, 1, (short) 8), 
			"&7&oPlace a copy of the item", 
			"&7&oyou want to enhance here.");
	public static final ItemStack slotInstruct = CakeLibrary.editNameAndLore(new ItemStack(Material.PAPER), 
			"&7&nAttempt Enhancement", 
			"&f",
			"&fPlace two copies of",
			"&fthe same item on the", 
			"&fleft and right slots.");
	public static final ItemStack slotEnhance = CakeLibrary.editNameAndLore(new ItemStack(Material.PAPER), 
			"&e&nAttempt Enhancement", 
			"&f",
			"&4If enhancement fails,",
			"&4the item to the right", 
			"&4will be destroyed.");
	public static final ItemStack slotFail = CakeLibrary.editNameAndLore(new ItemStack(Material.PAPER), 
			"&cEnhancement Failed...", 
			"&f",
			"&4If enhancement fails,",
			"&4the item to the right", 
			"&4will be destroyed.");
	public static final ItemStack slotWarn = CakeLibrary.editNameAndLore(new ItemStack(Material.PAPER), 
			"&4&nWarning", 
			"&f",
			"&cWhen enhancing items with bonus",
			"&cstats, the &4item to the left&c will",
			"&chave its bonus stats transferred",
			"&cto the result.",
			"&f",
			"&cShould the enhancement &4fail&c,",
			"&cthe &4item to the right&c will be", 
			"&cdestroyed regardless.", 
			"&f",
			"&7Click again to proceed");
	public static final ItemStack slotWarn1 = CakeLibrary.editNameAndLore(new ItemStack(Material.PAPER), 
			"&4&nWarning", 
			"&f",
			"&cWhen enhancing items with bonus",
			"&cstats, the &4item to the left&c will",
			"&chave its bonus stats transferred",
			"&cto the result.",
			"&f",
			"&cShould the enhancement &4fail&c,",
			"&cthe &4item to the right&c will be", 
			"&cdestroyed regardless.", 
			"&f",
			"&4&nClick again to proceed");

	static final String inventoryName = CakeLibrary.recodeColorCodes("&1Equipment Enhancement");

	public static final int maxState = 10;

	static final Random rand = new Random();

	public static final int[] tierEnhanceRate = { 100, 70, 50, 40, 30 };

	public static Inventory getNewInventory()
	{
		Inventory inv = Bukkit.createInventory(null, 27, inventoryName);
		return setEnhancementLayout(inv);
	}

	static Inventory setEnhancementLayout(Inventory inv)
	{
		for (int i = 0; i < 27; i++)
		{
			/*
			if (i == 13)
				getInventory().setItem(i, slotEnhance.clone());
			else if (i >= 0 && i <= 2)
				getInventory().setItem(i, slotGray.clone());
			else if (i >= 6 && i <= 8)
				getInventory().setItem(i, slotGray.clone());
			else if (i >= 11 && i <= 15)
				getInventory().setItem(i, slotGray.clone());
			else if (i >= 18 && i <= 20)
				getInventory().setItem(i, slotGray.clone());
			else if (i >= 24 && i <= 26)
				getInventory().setItem(i, slotGray.clone());
			else if (i == 9 || i == 17)
				getInventory().setItem(i, slotGray.clone());
			else if (i == 10 || i == 16)
				getInventory().setItem(i, slotItem.clone());
			else
				getInventory().setItem(i, slotBlack);
			 */

			if (i == 13)
				inv.setItem(i, slotInstruct.clone());
			else if (i == 1)
				inv.setItem(i, slotGray.clone());
			else if (i == 7)
				inv.setItem(i, slotGray.clone());
			else if (i >= 11 && i <= 15)
				inv.setItem(i, slotGray.clone());
			else if (i == 19)
				inv.setItem(i, slotGray.clone());
			else if (i == 25)
				inv.setItem(i, slotGray.clone());
			else if (i == 9 || i == 17)
				inv.setItem(i, slotGray.clone());
			else if (i == 10 || i == 16)
				inv.setItem(i, slotItem.clone());
			else
				inv.setItem(i, slotBlack);
		}
		return inv;
	}

	public static ItemStack getSlotEnhance(int state)
	{
		int dots = state;
		while (dots > 6)
			dots -= 6;
		String sDots = ".";
		while (dots > 1)
		{
			sDots += ".";
			dots--;
		}

		int fill = (int) (state / (maxState / 20.0F)) + 1;
		String bar = "&7[";
		for (int i = 0; i < fill; i++)
			bar += "&6&ki&e&k!";
		bar += "&7";
		for (int i = fill; i < 20; i++)
			bar += " ";
		bar += "]";

		return CakeLibrary.editNameAndLore(new ItemStack(Material.PAPER, (int) Math.max(1, state / maxState * 100.0F)), 
				"&eEnhancing" + sDots,
				bar,
				"&f",
				"&7&oClose inventory to cancel");
	}

	public static void updateMiddleItem(Inventory inv)
	{
		ItemStack middle = inv.getItem(13);
		ItemStack left = inv.getItem(10);
		ItemStack right = inv.getItem(16);
		if (CakeLibrary.isItemStackNull(middle))
		{
			inv.setItem(13, (isItemPartOfLayout(left) || isItemPartOfLayout(right)) ? slotInstruct.clone() : slotEnhance.clone());
			return;
		}
		if (isItemPartOfLayout(middle))
		{
			inv.setItem(13, (isItemPartOfLayout(left) && isItemPartOfLayout(right)) ? slotInstruct.clone() : slotEnhance.clone());
			return;
		}
	}

	public static boolean isItemPartOfLayout(ItemStack is)
	{
		if (CakeLibrary.isItemStackNull(is))
			return false;
		String itemName = CakeLibrary.getItemName(is);
		if (itemName.startsWith(CakeLibrary.getItemName(getSlotEnhance(0))))
			return true;
		if (itemName.equals(CakeLibrary.getItemName(slotInstruct)))
			return true;
		if (itemName.equals(CakeLibrary.getItemName(slotWarn)))
			return true;
		if (itemName.equals(CakeLibrary.getItemName(slotEnhance)))
			return true;
		if (itemName.equals(CakeLibrary.getItemName(slotFail)))
			return true;
		if (itemName.equals(CakeLibrary.getItemName(slotItem)))
			return true;
		if (itemName.equals(CakeLibrary.getItemName(slotBlack)))
			return true;
		if (itemName.equals(CakeLibrary.getItemName(slotGray)))
			return true;
		return false;
	}

	public static void updateInventory(Inventory inv, int state)
	{
		HumanEntity he = null;
		if (inv.getViewers().size() == 0)
			return;
		he = inv.getViewers().get(0);
		if (he != null)
			if (he instanceof Player)
				((Player) he).playSound(he.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.2F, (Float.valueOf(state) / Float.valueOf(maxState) / 2.0F) + 0.5F);
		if (state == maxState)
		{
			RItem ri1 = new RItem(inv.getItem(10));
			if (rand.nextInt(100) + 1 <= tierEnhanceRate[ri1.getTier()]) //success
			{
				ri1.setTier(ri1.getTier() + 1);
				inv.setItem(13, ri1.createItem());
				inv.setItem(10, slotItem.clone());
				inv.setItem(16, slotItem.clone());
				if (he != null)
				{
					new RPGEvents.PlayEffect(Effect.STEP_SOUND, he, 20).run();
					new RPGEvents.PlaySoundEffect(he, Sound.ENTITY_PLAYER_LEVELUP, 0.2F + (ri1.getTier() / 10.0F), 0.8F + (ri1.getTier() / 10.0F)).run();
					RPGCore.msg(he, "&2Enhancement success!");
				}
			} else //failure
			{
				inv.setItem(13, slotFail.clone());
				inv.setItem(16, slotItem.clone());
				if (he != null)
				{
					he = inv.getViewers().get(0);
					new RPGEvents.PlayEffect(Effect.STEP_SOUND, he, 173).run();
					RPGCore.msg(he, "&cEnhancement failure...");
				}
			}
			return;
		}
		inv.setItem(13, getSlotEnhance(state));
	}
}
