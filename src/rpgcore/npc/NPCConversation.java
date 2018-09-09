package rpgcore.npc;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.chat.TextComponent;
import rpgcore.item.RItem;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGCore;
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
	public static boolean useChat = false;
	public static ArrayList<NPCConversation> conversations = new ArrayList<NPCConversation>();
	public static ItemStack right = CakeLibrary.renameItem(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15), "&7<---");
	public static ItemStack left = CakeLibrary.renameItem(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15), "&7--->");
	public static ItemStack centre = CakeLibrary.renameItem(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15), "&7---");
	public NPCConversation(RPlayer player, ConversationData conversationData)
	{
		this.player = player;
		this.conversationData = conversationData;
		for (ConversationPart cp: conversationData.masters)
		{
			if (cp == null)
				continue;
			if (cp.flagKey == null)
				continue;
			String value = player.npcFlags.get(cp.flagKey);
			if (value != null && value.equals(cp.flagValue))
				part = cp;
		}
		if (part == null)
			part = conversationData.masters.get(0);
		conversations.add(this);
	}

	public Inventory getConversationUI()
	{
		Inventory inv = Bukkit.createInventory(null, 9, CakeLibrary.recodeColorCodes("&0Conversation - " + CakeLibrary.removeColorCodes(conversationData.npcName)));
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

	public int checkCommands()
	{
		if (part == null)
			return -1;

		Player p = player.getPlayer();

		if (part.string.toLowerCase().startsWith("@exit"))
		{
			RPGEvents.scheduleRunnable(new RPGEvents.InventoryClose(p), 1);
			return -1;
		} else if (part.string.toLowerCase().startsWith("@shop: "))
		{
			Shop shop = ShopManager.getShopWithDB(part.string.split(": ")[1]);
			RPGEvents.scheduleRunnable(new RPGEvents.InventoryOpen(p, shop.getShopInventory()), 1);
			if (part.next.size() <= 0)
				part = null;
			else
				part = part.next.get(0);
			return -1;
		} else if (part.string.toLowerCase().startsWith("@giveitem: "))
		{
			String[] vars = part.string.split(": ");
			RItem item = RPGCore.getItemFromDatabase(vars[1]);
			if (item == null)
			{
				RPGCore.msgConsole("&4Error while executing @giveitem in " + conversationData.npcName + "'s conversation data: &c" + vars[1] + " &4is not an existing RItem.");
				return -1;
			}
			if (!CakeLibrary.playerHasVacantSlots(p))
			{
				RPGCore.msg(p, "Please clear up an inventory slot to receive an item!");
				return -1;
			}
			p.getInventory().addItem(item.createItem());
			if (part.next.size() <= 0)
				part = null;
			else
				part = part.next.get(0);
			return 0;
		} else if (part.string.toLowerCase().startsWith("@setflag: "))
		{
			String[] vars = part.string.split(": ");
			String[] vars1 = vars[1].split(", ");
			player.npcFlags.put(vars1[0], vars1[1]);
			if (part.next.size() <= 0)
				part = null;
			else
				part = part.next.get(0);
			RPGCore.playerManager.writeData(player);
			return 0;
		} else if (part.string.toLowerCase().startsWith("@delflag: "))
		{
			String[] vars = part.string.split(": ");
			player.npcFlags.remove(vars[1]);
			if (part.next.size() <= 0)
				part = null;
			else
				part = part.next.get(0);
			RPGCore.playerManager.writeData(player);
			return 0;
		}
		return 1;
	}

	public void updateUI()
	{
		if (useChat)
		{
			String[] quotes = part.string.split("###");
			TextComponent msg = new TextComponent("�f<" + conversationData.npcName + "�f> ");
			for (int i = 0; i < quotes.length; i++)
			{
				String quote = quotes[i];
				String[] lines = quote.split("##");
				for (int i1 = 0; i1 < lines.length; i1++)
				{
					String line = lines[i1];
					if (i != 0 && i1 != 0)
						msg.addExtra("\n");
					msg.addExtra(line);
				}
			}
			player.getPlayer().spigot().sendMessage(msg);
			return;
		}

		int check = checkCommands();
		while (check == 0)
			check = checkCommands();
		if (check == -1)
			return;

		Player p = player.getPlayer();
		if (part == null)
		{
			RPGEvents.scheduleRunnable(new RPGEvents.InventoryClose(p), 1);
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
		if (part.next.size() > 0 && part.next.get(0).type == ConversationPartType.PLAYER)
			suffix = lastClickedSlot >= 4 ? CakeLibrary.recodeColorCodes("&e <-- Choose <-- ") : CakeLibrary.recodeColorCodes("&e --> Choose --> ");
			else if (part.next.size() > 0)
			{
				boolean more = false;
				ConversationPart next = part.next.get(0);
				try
				{
					while (next.string.startsWith("@setflag") || 
							next.string.startsWith("@delflag") || 
							next.string.startsWith("@giveitem") || 
							next.string.startsWith("@exit"))
						next = next.next.get(0);
					more = true;
				} catch (Exception e) {}
				if (more)
					suffix = CakeLibrary.recodeColorCodes("&a --> Next -->");
			}

		lines.add(CakeLibrary.recodeColorCodes("&f "));
		lines.add(suffix);

		if (part.next.size() > 1)
		{
			for (int i = 0; i < part.next.size(); i++)
			{
				ArrayList<String> lines1 = new ArrayList<String>();
				ConversationPart n = part.next.get(i);
				ItemStack decision = new ItemStack(Material.PAPER);

				String[] quotes1 = n.string.split("###");

				String name1 = CakeLibrary.recodeColorCodes("&6\"&e" + quotes1[0] + "&6\"&e");
				if (quotes1.length > 1)
				{
					String[] split1 = quotes1[0].split("##");
					if (split1.length > 1)
					{
						name1 = CakeLibrary.recodeColorCodes("&6\"&e" + split1[0]);
						for (int i2 = 1; i2 < split1.length; i2++)
						{
							String s = "&e" + split1[i2];
							if (i2 == 0)
								s = "&6\"" + s;
							if (i2 == split1.length - 1)
								s += "&6\"&e";
							lines1.add(CakeLibrary.recodeColorCodes(s));
						}
					}
					for (int index = 1; index < quotes1.length; index++)
					{
						lines1.add(CakeLibrary.recodeColorCodes("&e "));	
						String quote = quotes1[index];
						String[] split = quote.split("##");
						for (int i2 = 0; i2 < split.length; i2++)
						{
							String s = "&e" + split[i2];
							if (i2 == 0)
								s = "&6\"" + s;
							if (i2 == split.length - 1)
								s += "&6\"&e";
							lines1.add(CakeLibrary.recodeColorCodes(s));
						}
					}
				} else
				{
					String[] split1 = quotes1[0].split("##");
					if (split1.length > 1)
					{
						name1 = CakeLibrary.recodeColorCodes("&6\"&e" + split1[0]);
						for (int i2 = 1; i2 < split1.length; i2++)
						{
							String s = "&e" + split1[i2];
							if (i2 == 0)
								s = "&6\"" + s;
							if (i2 == split1.length - 1)
								s += "&6\"&e";
							lines1.add(CakeLibrary.recodeColorCodes(s));
						}
					}
				}

				decision = CakeLibrary.renameItem(decision, name1);
				if (lines1.size() > 0)
					decision = CakeLibrary.setItemLore(decision, lines1);
				conversationUI.setItem(getDecisionSlot(i), decision);
			}
		}

		npc = CakeLibrary.renameItem(npc, name);
		if (lines.size() > 0)
			npc = CakeLibrary.setItemLore(npc, lines);
		conversationUI.setItem(lastClickedSlot, npc);
	}
}