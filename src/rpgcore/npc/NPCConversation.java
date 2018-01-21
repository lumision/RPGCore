package rpgcore.npc;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGEvents;
import rpgcore.npc.ConversationData.ConversationPart;
import rpgcore.npc.ConversationData.ConversationPartType;
import rpgcore.player.RPlayer;
import rpgcore.shop.Shop;
import rpgcore.shop.ShopManager;

public class NPCConversation
{
	public RPlayer player;
	public ConversationData conversationData;
	public ConversationPart part;
	public Inventory conversationUI;
	public boolean closed;
	public int lastClickedSlot = 4;
	public static ArrayList<NPCConversation> conversations = new ArrayList<NPCConversation>();
	public static ItemStack right = CakeLibrary.renameItem(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15), "&7<---");
	public static ItemStack left = CakeLibrary.renameItem(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15), "&7--->");
	public static ItemStack centre = CakeLibrary.renameItem(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15), "&7---");
	public NPCConversation(RPlayer player, ConversationData conversationData)
	{
		this.player = player;
		this.conversationData = conversationData;
		this.part = conversationData.master;
		conversations.add(this);
	}

	public Inventory getConversationUI()
	{
		Inventory inv = Bukkit.createInventory(null, 9, CakeLibrary.recodeColorCodes("&0Conversation - " + CakeLibrary.removeColorColorCodes(conversationData.npcName)));
		this.conversationUI = inv;
		updateUI();
		return inv;
	}

	public int getDecisionSlot(int index)
	{
		/**
		if (index % 2 == 0)
			return 3 - (index / 2);
		else
			return 5 + (index / 2);
		 */
		return lastClickedSlot >= 4 ? lastClickedSlot - 1 - index : lastClickedSlot + 1 + index;
	}

	public void updateUI()
	{
		if (part.string.toLowerCase().startsWith("@shop: "))
		{
			Shop shop = ShopManager.getShopWithDB(part.string.split(": ")[1]);
			RPGEvents.scheduleRunnable(new RPGEvents.InventoryOpen(player.getPlayer(), shop.getShopInventory()), 1);
			return;
		}

		for (int i = 0; i < lastClickedSlot; i++)
			conversationUI.setItem(i, left);
		for (int i = lastClickedSlot + 1; i < 9; i++)
			conversationUI.setItem(i, right);

		ItemStack npc = new ItemStack(Material.SIGN);
		String[] quotes = part.string.split("###");

		String name = CakeLibrary.recodeColorCodes("&7\"&f" + quotes[0] + "&7\"&f");
		ArrayList<String> lines = new ArrayList<String>();
		if (quotes.length > 1)
		{
			String[] split1 = quotes[0].split("##");
			if (split1.length > 1)
			{
				name = CakeLibrary.recodeColorCodes("&7\"&f" + split1[0]);
				for (int i = 1; i < split1.length; i++)
				{
					String s = "&f" + split1[i];
					if (i == 0)
						s = "&7\"" + s;
					if (i == split1.length - 1)
						s += "&7\"&f";
					lines.add(CakeLibrary.recodeColorCodes(s));
				}
			}
			for (int index = 1; index < quotes.length; index++)
			{
				lines.add(CakeLibrary.recodeColorCodes("&f "));	
				String quote = quotes[index];
				String[] split = quote.split("##");
				for (int i = 0; i < split.length; i++)
				{
					String s = "&f" + split[i];
					if (i == 0)
						s = "&7\"" + s;
					if (i == split.length - 1)
						s += "&7\"&f";
					lines.add(CakeLibrary.recodeColorCodes(s));
				}
			}
		} else
		{
			String[] split1 = quotes[0].split("##");
			if (split1.length > 1)
			{
				name = CakeLibrary.recodeColorCodes("&7\"&f" + split1[0]);
				for (int i = 1; i < split1.length; i++)
				{
					String s = "&f" + split1[i];
					if (i == 0)
						s = "&7\"" + s;
					if (i == split1.length - 1)
						s += "&7\"&f";
					lines.add(CakeLibrary.recodeColorCodes(s));
				}
			}
		}
		
		String suffix = CakeLibrary.recodeColorCodes("&c --> Exit <--");
		if (part.next.size() > 0)
				suffix = part.next.get(0).type == ConversationPartType.PLAYER ? 
						lastClickedSlot >= 4 ? CakeLibrary.recodeColorCodes("&e <-- Choose <-- ") : 
							CakeLibrary.recodeColorCodes("&e --> Choose --> ") : 
								CakeLibrary.recodeColorCodes("&a --> Next -->");
		
		lines.add(CakeLibrary.recodeColorCodes("&f "));
		lines.add(suffix);

		if (part.next.size() > 1)
		{
			for (int i = 0; i < part.next.size(); i++)
			{
				ConversationPart n = part.next.get(i);
				ItemStack decision = new ItemStack(Material.PAPER);
				decision = CakeLibrary.renameItem(decision, "&6\"&e" + n.string + "&6\"&e");
				conversationUI.setItem(getDecisionSlot(i), decision);
			}
		}

		npc = CakeLibrary.renameItem(npc, name);
		if (lines.size() > 0)
			npc = CakeLibrary.setItemLore(npc, lines);
		conversationUI.setItem(lastClickedSlot, npc);
	}
}