package rpgcore.item;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGCore;
import rpgcore.main.RPGEvents;

public class EnhancementInventory 
{
	Inventory inventory;

	public static final ItemStack slotBlack = CakeLibrary.renameItem(new ItemStack(160, 1, (short) 15), "&f");
	public static final ItemStack slotGray = CakeLibrary.renameItem(new ItemStack(160, 1, (short) 7), "&f");
	public static final ItemStack slotItem = CakeLibrary.editNameAndLore(new ItemStack(160, 1, (short) 8), 
			"&7&oPlace a copy of the item", 
			"&7&oyou want to enhance here.");
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

	static final String inventoryName = CakeLibrary.recodeColorCodes("&1Equipment Enhancement");

	public static final int maxState = 20;

	static final Random rand = new Random();

	public EnhancementInventory()
	{

	}

	public Inventory getInventory()
	{
		if (inventory != null)
			return inventory;
		inventory = Bukkit.createInventory(null, 27, inventoryName);
		setEnhancementLayout();
		return inventory;
	}

	void setEnhancementLayout()
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
				getInventory().setItem(i, slotEnhance.clone());
			else if (i == 1)
				getInventory().setItem(i, slotGray.clone());
			else if (i == 7)
				getInventory().setItem(i, slotGray.clone());
			else if (i >= 11 && i <= 15)
				getInventory().setItem(i, slotGray.clone());
			else if (i == 19)
				getInventory().setItem(i, slotGray.clone());
			else if (i == 25)
				getInventory().setItem(i, slotGray.clone());
			else if (i == 9 || i == 17)
				getInventory().setItem(i, slotGray.clone());
			else if (i == 10 || i == 16)
				getInventory().setItem(i, slotItem.clone());
			else
				getInventory().setItem(i, slotBlack);
		}
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
				bar);
	}

	public static void updateInventory(Inventory inv, int state)
	{
		HumanEntity he = null;
		if (inv.getViewers().size() > 0)
			he = inv.getViewers().get(0);
		if (he != null)
			new RPGEvents.PlaySoundEffect(he, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.2F, (Float.valueOf(state) / Float.valueOf(maxState) / 2.0F) + 0.5F).run();
		if (state == maxState)
		{
			if (rand.nextInt(6) <= 3) //success
			{
				RItem ri = new RItem(inv.getItem(10));
				ri.setTier(ri.getTier() + 1);
				inv.setItem(13, ri.createItem());
				inv.setItem(10, slotItem.clone());
				inv.setItem(16, slotItem.clone());
				if (he != null)
				{
					new RPGEvents.PlayEffect(Effect.STEP_SOUND, he, 20).run();
					new RPGEvents.PlaySoundEffect(he, Sound.ENTITY_PLAYER_LEVELUP, 0.2F + (ri.getTier() / 10.0F), 0.8F + (ri.getTier() / 10.0F)).run();
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
