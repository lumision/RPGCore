package rpgcore.main;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import rpgcore.areas.Area;
import rpgcore.classes.ClassInventory;
import rpgcore.classes.RPGClass;
import rpgcore.entities.bosses.Astrea;
import rpgcore.entities.bosses.UndeadEmperor;
import rpgcore.entities.mobs.MageZombie;
import rpgcore.entities.mobs.RPGMonster;
import rpgcore.entities.mobs.ReinforcedSkeleton;
import rpgcore.entities.mobs.ReinforcedZombie;
import rpgcore.entities.mobs.WarriorZombie;
import rpgcore.item.BonusStat.BonusStatCrystal;
import rpgcore.item.RItem;
import rpgcore.npc.ConversationData;
import rpgcore.npc.CustomNPC;
import rpgcore.npc.NPCManager;
import rpgcore.party.PartyManager;
import rpgcore.party.RPGParty;
import rpgcore.player.RPlayer;
import rpgcore.player.RPlayerManager;
import rpgcore.previewchests.PreviewChestManager;
import rpgcore.recipes.RPGRecipe;
import rpgcore.shop.Shop;
import rpgcore.shop.ShopManager;
import rpgcore.sideclasses.RPGSideClass;
import rpgcore.skillinventory.SkillInventory;
import rpgcore.songs.RPGSong;
import rpgcore.songs.RSongManager;
import rpgcore.songs.RunningTrack;

public class RPGCore extends JavaPlugin
{
	public File pluginFolder = new File("plugins/RPGCore");
	public File itemsFolder = new File("plugins/RPGCore/items");
	public static ArrayList<RItem> itemDatabase = new ArrayList<RItem>();
	public static RPGEvents events;
	public static RPGListener listener;
	public static RPlayerManager playerManager;
	public static RSongManager songManager;
	public static PartyManager partyManager;
	public static PreviewChestManager previewChestManager;
	public static RPGCore instance;
	public static Random rand = new Random();
	public static Timer timer = new Timer();
	public static NPCManager npcManager;
	public static long serverAliveTicks;
	

	static final String[] helpGold = new String[] { 
			"&6===[&e /gold Help &6]===", 
	"&6/gold withdraw/w <amt>: &eWithdraws Gold into item form"};

	static final String[] helpGoldAdmin = new String[] { 
			"&6/gold add/a <amt> [player]: &eAdds Gold to a player",  
	"&6/gold remove/r <amt> [player]: &eRemoves Gold from a player"};

	static final String[] helpParty = new String[] { 
			"&5===[&d /party Help &5]===",  
			"&5/party info: &dGives info about the party you're in",
			"&5/party join <host>: &dJoins a party if you were invited",
			"&5/party leave: &dLeaves the party as a member",
			"&5/party create: &dCreates a party with you as the host",
			"&5/party invite <player>: &dInvites a player to your party (Host)",
			"&5/party kick <player>: &dKicks a player from your party (Host)",
			"&5/party sethost <player>: &dPasses host to a member (Host)",
			"&5/party disband: &dDisbands your party (Host)",
	"&dBegin a message with '\\' to chat in the party."};

	static final String[] helpItem = new String[] { 
			"&6===[&e /item Help &6]===",
			"&6/item tier <tier>",
			"&6/item lvrequirement <level>",
			"&6/item magicdamage <damage>",
			"&6/item brutedamage <damage>",
			"&6/item attackspeed <multiplier>",
			"&6/item critchance <percentage>",
			"&6/item critdamage <percentage>",
			"&6/item cooldowns <percentage>",
			"&6/item dmgreduction <percentage>",
	"&6/item unbreakable"};

	static final String[] helpArea = new String[] { 
			"&4===[&c /area Help &4]===",  
			"&4/area pos1/pos2: &cSets the pos1 or pos2 of a creating area",
			"&4/area create <name>: &cCreates an area",
			"&4/area list: &cLists all areas",
			"&4/area editable <areaName>: &cToggles the editability of an area",
	"&4/area bgm <areaName> <bgmName>: &cSets the BGM of an area"};

	public static void main(String[] args) {}

	@Override
	public void onEnable()
	{
		RPGCore.instance = this;
		pluginFolder.mkdirs();
		readItemDatabase();
		RPGClass.setXPTable();
		RPGSideClass.setXPTable();
		playerManager = new RPlayerManager(this);
		songManager = new RSongManager(this);
		partyManager = new PartyManager(this);
		previewChestManager = new PreviewChestManager(this);
		listener = new RPGListener(this);
		getServer().getPluginManager().registerEvents(listener, this);
		npcManager = new NPCManager(this);
		events = new RPGEvents(this);
		RPGEvents.stopped = false;
		ShopManager.readShopDatabase();
		ConversationData.readConversationData();
		RPGRecipe.readRecipeData();

		RPGEvents.scheduleRunnable(new RPGEvents.ConsoleCommand("gamerule sendCommandFeedback false"), 1);
		RPGEvents.scheduleRunnable(new RPGEvents.ConsoleCommand("gamerule mobGriefing false"), 1);
		RPGEvents.scheduleRunnable(new RPGEvents.ConsoleCommand("gamerule doFireTick false"), 1);
		RPGEvents.scheduleRunnable(new RPGEvents.ConsoleCommand("gamerule keepInventory true"), 1);
		RPGEvents.scheduleRunnable(new RPGEvents.Message(Bukkit.getConsoleSender(), "&cCakeCraft has finished launching."), 5);
	}

	@Override
	public void onDisable()
	{
		RPGEvents.stopped = true;
		npcManager.onDisable();
		playerManager.writeData();
		previewChestManager.writeData();
		for (RPGMonster ce: RPGMonster.entities)
			ce.entity.remove();
	}

	public static ItemStack getGoldItem(int amount)
	{
		ItemStack gold = new ItemStack(Material.GOLD_NUGGET);
		return CakeLibrary.renameItem(gold, "&6Gold &e(" + CakeLibrary.seperateNumberWithCommas(amount, false) + ")");
	}

	public void readItemFolder(File folder)
	{
		if (folder == null)
			return;
		if (folder.listFiles() == null)
			return;
		for (File file: folder.listFiles())
		{
			if (!file.getName().contains("."))
			{
				readItemFolder(file);
				continue;
			}
			try
			{
				int id = 0;
				int amount = 0;
				short durability = 0;
				boolean unbreakable = false;
				int tier = 0;

				String name = null;
				ArrayList<String> lore = new ArrayList<String>();

				ArrayList<Enchantment> enchs = new ArrayList<Enchantment>();
				ArrayList<Integer> levels = new ArrayList<Integer>();

				ArrayList<String> lines = CakeLibrary.readFile(file);
				String header = "";
				for (String line: lines)
				{
					String[] split = line.split(": ");
					if (line.startsWith(" "))
					{
						split[0] = split[0].substring(1);
						line = line.substring(1);
						if (header.equals("lore: "))
							lore.add(line);
						else if (header.equals("enchantments: "))
						{
							enchs.add(Enchantment.getById(Integer.valueOf(split[0])));
							levels.add(Integer.valueOf(split[1]));
						}
					} else
					{
						header = line;
						if (line.startsWith("id: "))
							id = Integer.valueOf(split[1]);
						if (line.startsWith("amount: "))
							amount = Integer.valueOf(split[1]);
						if (line.startsWith("durability: "))
							durability = Short.valueOf(split[1]);
						if (line.startsWith("unbreakable: "))
							unbreakable = Boolean.valueOf(split[1]);
						if (line.startsWith("tier: "))
							tier = Integer.valueOf(split[1]);

						if (line.startsWith("name: "))
							name = split[1];
					}
				}

				ItemStack item = new ItemStack(id, amount, durability);

				ItemMeta im = item.getItemMeta();
				if (name != null)
					im.setDisplayName(name);
				if (lore.size() > 0)
					im.setLore(lore);
				if (unbreakable)
					im.spigot().setUnbreakable(unbreakable);
				item.setItemMeta(im);

				for (int i = 0; i < enchs.size(); i++)
					item.addUnsafeEnchantment(enchs.get(i), levels.get(i));

				RItem ri = new RItem(item, file.getName().substring(0, file.getName().length() - 4));
				ri.setTier(tier);
				itemDatabase.add(ri);

			} catch (Exception e) {
				msgConsole("&4Unable to read item file: " + file.getName());
				e.printStackTrace();
			}
		}
	}

	public void readItemDatabase()
	{
		itemDatabase.clear();
		readItemFolder(itemsFolder);
	}

	public static RItem getItemFromDatabase(String databaseName)
	{
		for (RItem run: itemDatabase)
			if (run.databaseName.equalsIgnoreCase(databaseName))
				return run;
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String cmd, String[] args)
	{
		if (sender instanceof Player)
		{
			Player p = (Player) sender;
			if (CakeLibrary.hasColor(p.getOpenInventory().getTitle()))
			{
				msg(p, "You're currently in an inventory and commands cannot be executed.");
				return true;
			}
			RPlayer rp = playerManager.getRPlayer(p.getUniqueId());
			if (rp == null)
				return false;
			if (command.getName().equalsIgnoreCase("rr"))
			{
				if (!p.hasPermission("rpgcore.rr"))
				{
					msg(p, "You do not have permissions to do this.");
					return true;
				}
				readItemDatabase();
				ShopManager.readShopDatabase();
				songManager.readSongs();
				for (CustomNPC npc: npcManager.npcs)
					npc.conversationData = null;
				ConversationData.readConversationData();
				RPGRecipe.readRecipeData();
				msg(p, "Reloaded RPGCore v" + getDescription().getVersion());
				return true;
			}
			if (command.getName().equalsIgnoreCase("area"))
			{
				if (args.length == 0)
				{
					msgNoTag(p, helpArea);
					return true;
				}
				if (args[0].equalsIgnoreCase("pos1"))
				{
					rp.pos1 = p.getLocation();
					msg(p, "Position 1 set.");
					return true;
				}
				if (args[0].equalsIgnoreCase("pos2"))
				{
					rp.pos2 = p.getLocation();
					msg(p, "Position 2 set.");
					return true;
				}
				if (args[0].equalsIgnoreCase("create"))
				{
					if (args.length <  2)
					{
						msg(p, "Usage: /area create <areaName>");
						return true;
					}
					new Area(args[1], (int) Math.min(rp.pos1.getX(), rp.pos2.getX()),
							(int) Math.max(rp.pos1.getX(), rp.pos2.getX()),
							(int) Math.min(rp.pos1.getZ(), rp.pos2.getZ()),
							(int) Math.max(rp.pos1.getZ(), rp.pos2.getZ()));
					msg(p, "Area \"" + args[1] + "\" created.");
					return true;
				}
				if (args[0].equalsIgnoreCase("editable"))
				{
					if (args.length <  2)
					{
						msg(p, "Usage: /area editable <areaName>");
						return true;
					}
					Area area = null;
					for (Area check: Area.areas)
						if (check.name.equalsIgnoreCase(args[1]))
							area = check;
					if (area == null)
					{
						msg(p, "Area \"" + args[1] + "\" does not exist.");
						return true;
					}
					area.editable = !area.editable;
					msg(p, "Area editability: " + area.editable);
					return true;
				}
				if (args[0].equalsIgnoreCase("list"))
				{
					msgNoTag(p, "&4===[ &cAreas &4]===");
					for (Area area: Area.areas)
						msgNoTag(p, "&4 - &c" + area.name + "");
					return true;
				}
				msgNoTag(p, helpArea);
				return true;
			}
			if (command.getName().equalsIgnoreCase("gold"))
			{
				if (args.length == 0)
				{
					msgNoTag(p, helpGold);
					if (p.hasPermission("rpgcore.gold"))
						msgNoTag(p, helpGoldAdmin);
					return true;
				}
				if (args[0].equalsIgnoreCase("withdraw") || args[0].equalsIgnoreCase("w"))
				{
					if (args.length < 2)
					{
						msg(p, "Usage: /gold withdraw/w <amt>");
						return true;
					}
					if (!CakeLibrary.playerHasVacantSlots(p))
					{
						msg(p, "You need inventory slots to withdraw Gold");
						return true;
					}
					int amount = 0;
					try
					{
						amount = Integer.parseInt(args[1]);
					} catch (Exception e)
					{
						msg(p, "Enter a number.");
						return true;
					}
					if (amount > rp.getGold())
					{
						msg(p, "You do not have that amount of money.");
						return true;
					}
					p.getInventory().addItem(getGoldItem(amount));
					rp.addGold(-amount);
					msg(p, "You've withdrawn &6" + amount + " &6Gold&e.");
					return true;
				}
				if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("a"))
				{
					if (args.length < 2)
					{
						msg(p, "Usage: /gold add/a <amt> [player]");
						return true;
					}
					int amount = 0;
					try
					{
						amount = Integer.parseInt(args[1]);
					} catch (Exception e)
					{
						msg(p, "Enter a number.");
						return true;
					}
					RPlayer target = rp;
					if (args.length > 2)
					{
						target = playerManager.getRPlayer(args[2]);
						if (target == null)
						{
							msg(p, "That player does not exist");
							return true;
						}
					}
					target.addGold(amount);
					msg(p, "You've added &6" + amount + " Gold &eto &6" + target.getPlayerName() + "&e's account.");
					return true;
				}
				if (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("r"))
				{
					if (args.length < 2)
					{
						msg(p, "Usage: /gold remove/r <amt> [player]");
						return true;
					}
					int amount = 0;
					try
					{
						amount = Integer.parseInt(args[1]);
					} catch (Exception e)
					{
						msg(p, "Enter a number.");
						return true;
					}
					RPlayer target = rp;
					if (args.length > 2)
					{
						target = playerManager.getRPlayer(args[2]);
						if (target == null)
						{
							msg(p, "That player does not exist");
							return true;
						}
					}
					target.addGold(-amount);
					msg(p, "You've removed &6" + amount + " Gold &efrom &6" + target.getPlayerName() + "&e's account.");
					return true;
				}
				msgNoTag(p, helpGold);
				if (p.hasPermission("rpgcore.gold"))
					msgNoTag(p, helpGoldAdmin);
				return true;
			}
			if (command.getName().equalsIgnoreCase("shop"))
			{
				if (!p.hasPermission("rpgcore.shop"))
				{
					msg(p, "You do not have permissions to do this.");
					return true;
				}
				if (args.length == 0)
				{
					msgNoTag(p, "&cShop list:");
					for (Shop shop: ShopManager.shopDatabase)
						msgNoTag(p, "&c - " + shop.dbName);
					return true;
				}
				Shop shop = ShopManager.getShopWithDB(args[0]);
				if (shop == null)
				{
					msgNoTag(p, "&cShop list:");
					for (Shop shop1: ShopManager.shopDatabase)
						msgNoTag(p, "&c - " + shop1.dbName);
					return true;
				}
				p.openInventory(shop.getShopInventory());
				return true;
			}
			if (command.getName().equalsIgnoreCase("pc"))
			{
				if (!p.hasPermission("rpgcore.previewchest"))
				{
					msg(p, "You do not have permissions to do this.");
					return true;
				}
				Block b = p.getTargetBlock(CakeLibrary.getPassableBlocks(), 6);
				Location l = b.getLocation();
				if (!(b.getState() instanceof Chest))
				{
					msg(p, "The target block is not a chest (" + b.getType() + ")");
					return true;
				}

				Chest chest = (Chest) b.getState();
				InventoryHolder ih = chest.getInventory().getHolder();

				if (ih instanceof DoubleChest)
				{
					DoubleChest dchest = (DoubleChest) ih;
					Location left = ((Chest) dchest.getLeftSide()).getBlock().getLocation();
					Location right = ((Chest) dchest.getRightSide()).getBlock().getLocation();
					Location getLeft = previewChestManager.getPreviewChest(left);
					Location getRight = previewChestManager.getPreviewChest(right);
					if (getLeft == null && getRight == null)
					{
						previewChestManager.previewChests.add(left);
						msg(p, "Preview chest added.");
					} else
					{
						if (getLeft != null)
							previewChestManager.previewChests.remove(getLeft);
						if (getRight != null)
							previewChestManager.previewChests.remove(getRight);
						msg(p, "Preview chest removed.");
					}
				} else
				{
					Location get = previewChestManager.getPreviewChest(l);
					if (get == null)
					{
						previewChestManager.previewChests.add(l);
						msg(p, "Preview chest added.");
					} else 
					{
						previewChestManager.previewChests.remove(get);
						msg(p, "Preview chest removed.");
					}
					previewChestManager.writeData();
				}
				return true;
			}
			if (command.getName().equalsIgnoreCase("si"))
			{
				if (!p.hasPermission("rpgcore.item"))
				{
					msg(p, "You do not have permissions to do this.");
					return true;
				}
				ItemStack is = p.getItemInHand();
				if (is == null)
				{
					msg(p, "Hold an item to save.");
					return true;
				}
				if (args.length < 1)
				{
					msg(p, "Usage: /si [filename]");
					return true;
				}
				RItem ri = new RItem(is, args[0]);
				ri.saveItemToFile(args[0]);
				itemDatabase.add(ri);
				msg(p, "Item saved");
				return true;
			}
			if (command.getName().equalsIgnoreCase("gi"))
			{
				if (!p.hasPermission("rpgcore.item"))
				{
					msg(p, "You do not have permissions to do this.");
					return true;
				}
				if (args.length < 1)
				{
					p.sendMessage(CakeLibrary.recodeColorCodes("&4Item Database List:"));
					for (RItem ri: itemDatabase)
						p.sendMessage(CakeLibrary.recodeColorCodes("&c - " + ri.databaseName));
					return true;
				}
				RItem ri = getItemFromDatabase(args[0]);
				if (ri == null)
				{
					msg(p, "This item does not exist in the database");
					return true;
				}
				if (args.length > 1)
					ri.itemVanilla.setAmount(Math.min(64, Integer.valueOf(args[1])));
				p.getInventory().addItem(ri.createItem());	
				msg(p, "Item obtained");
				return true;
			}
			if (command.getName().equalsIgnoreCase("npc"))
			{
				if (!p.hasPermission("rpgcore.npc"))
				{
					msg(p, "You do not have permissions to do this.");
					return true;
				}
				if (args.length == 0)
				{
					msg(p, "Usage: /npc <create/rename/delete/skin>");
					return true;
				}
				if (args[0].equalsIgnoreCase("create"))
				{
					String name = args[1];
					for (int i = 2; i < args.length - 1; i++)
						name += " " + args[i];
					String npcName = CakeLibrary.recodeColorCodes(name);
					if (npcName.length() > 16)
						npcName = npcName.substring(0, 16);
					rp.selectedNPC = npcManager.createNPC(p.getLocation(), npcName, args[args.length - 1]);
					rp.selectedNPC.saveNPC();
					msg(p, "NPC Created and selected.");
					return true;
				}
				if (args[0].equalsIgnoreCase("delete"))
				{
					if (rp.selectedNPC == null)
					{
						msg(p, "Select an NPC by sneak-clicking it first");
						return true;
					}
					rp.selectedNPC.deleteNPC();
					rp.selectedNPC = null;
					msg(p, "NPC Deleted.");
					return true;
				}
				if (args[0].equalsIgnoreCase("rename"))
				{
					if (rp.selectedNPC == null)
					{
						msg(p, "Select an NPC by sneak-clicking it first");
						return true;
					}
					if (args.length < 2)
					{
						msg(p, "Usage: /npc rename [newName]");
						return true;
					}
					String name = args[1];
					for (int i = 2; i < args.length; i++)
						name += " " + args[i];
					CustomNPC prev = rp.selectedNPC;
					rp.selectedNPC = prev.skinData != null ? npcManager.createNPC(prev.getBukkitLocation(), CakeLibrary.recodeColorCodes(name), prev.skinData.skinName)
							: npcManager.createNPC(prev.getBukkitLocation(), CakeLibrary.recodeColorCodes(name));
					prev.deleteNPC();
					rp.selectedNPC.saveNPC();
					msg(p, "NPC Renamed.");
					return true;
				}
				if (args[0].equalsIgnoreCase("skin"))
				{
					if (rp.selectedNPC == null)
					{
						msg(p, "Select an NPC by sneak-clicking it first");
						return true;
					}
					if (args.length < 2)
					{
						msg(p, "Usage: /npc skin [newSkinName]");
						return true;
					}
					CustomNPC prev = rp.selectedNPC;
					rp.selectedNPC = npcManager.createNPC(prev.getBukkitLocation(), prev.getName(), args[1]);
					prev.deleteNPC();
					rp.selectedNPC.saveNPC();
					msg(p, "NPC Skin changed.");
					return true;
				}
				msg(p, "Usage: /npc <create/rename/delete/skin>");
				return true;
			}
			if (command.getName().equalsIgnoreCase("crystal"))
			{
				if (!p.hasPermission("rpgcore.crystal"))
				{
					msg(p, "You do not have permissions to do this.");
					return true;
				}
				if (args.length < 1)
				{
					msg(p, "Usage: /crystal <green/yellow/cyan/pink>");
					return true;
				}
				ItemStack crystal = null;
				if (args[0].equalsIgnoreCase("green"))
					crystal = BonusStatCrystal.ALL_LINES_REROLL.getItemStack();
				else if (args[0].equalsIgnoreCase("yellow"))
					crystal = BonusStatCrystal.TIER_REROLL.getItemStack();
				else if (args[0].equalsIgnoreCase("cyan"))
					crystal = BonusStatCrystal.LINE_AMOUNT_REROLL.getItemStack();
				else if (args[0].equalsIgnoreCase("pink"))
					crystal = BonusStatCrystal.STAT_ADDER.getItemStack();
				else
					msg(p, "Usage: /crystal <red/yellow/cyan/pink>");
				if (crystal != null)
				{
					crystal.setAmount(64);
					p.getInventory().addItem(crystal);
				}
				return true;
			}
			if (command.getName().equalsIgnoreCase("party"))
			{
				if (args.length < 1)
				{
					msgNoTag(p, helpParty);
					return true;
				}
				if (args[0].equalsIgnoreCase("info"))
				{
					if (rp.partyID == -1)
					{
						RPGParty.msg(p, "You are not in a party.");
						return true;
					}
					RPGParty party = partyManager.getParty(rp.partyID);
					party.updatePartyInventory();

					/**msgNoTag(p, "&5---[&d Party Info &5]---");
					msgNoTag(p, "&5 * Players:");
					for (RPlayer member: party.players)
					{
						Player player = member.getPlayer();
							msgNoTag(p, "&5   -> &d" + member.getPlayerName() + " &5- " +
									(player == null ? "&dOffline" : "") +
									(player != null ? "&dClass: " + member.currentClass.toString() : "") + 
									(player != null ? "&5, &dDmg: " + member.getDamageOfClass() : "") + 
									(player != null ? "&5, &dCs: " + (1 / member.getCastDelayMultiplier()) : "") + 
									(member.getPlayerName().equalsIgnoreCase(party.host.getPlayerName()) ? "&5, &dHOST" : ""));
					}*/

					p.openInventory(party.partyInventory);
					return true;
				}
				if (args[0].equalsIgnoreCase("create"))
				{
					if (rp.partyID != -1)
					{
						RPGParty.msg(p, "You are already in a party.");
						RPGParty.msg(p, "Use &5/party disband &dto disband it.");
						return true;
					}
					partyManager.createNewParty(rp);
					RPGParty.msg(p, "Party created.");
					RPGParty.msg(p, "&dUse &5/party <invite/kick/disband> [player] &dto manage.");
					return true;
				}
				if (args[0].equalsIgnoreCase("join"))
				{
					if (args.length < 2)
					{
						RPGParty.msg(p, "Usage: /party join <host>");
						return true;
					}
					if (rp.partyID != -1)
					{
						RPGParty.msg(p, "You are already in a party.");
						RPGParty.msg(p, "Use &5/party leave &dto leave.");
						return true;
					}
					RPlayer target = playerManager.getRPlayer(CakeLibrary.completeName(args[1]));
					if (target == null)
					{
						RPGParty.msg(p, "No player by that name was found.");
						return true;
					}
					if (target.partyID == -1)
					{
						RPGParty.msg(p, "That player is not even in a party.");
						return true;
					}
					RPGParty party = partyManager.getParty(target.partyID);
					if (party.host != target)
					{
						RPGParty.msg(p, "That player is not the host of his party.");
						return true;
					}
					if (!party.invites.contains(p.getName()))
					{
						RPGParty.msg(p, "You aren't invited to this party.");
						return true;
					}
					if (party.players.size() >= 9)
					{
						RPGParty.msg(p, "This party is full (9 players).");
						return true;
					}
					party.invites.remove(p.getName());
					party.addPlayer(rp);
					party.updatePartyInventory();
					party.broadcastMessage("&5" + p.getName() + " &dhas joined the party.");
					return true;
				}
				if (args[0].equalsIgnoreCase("leave"))
				{
					if (rp.partyID == -1)
					{
						RPGParty.msg(p, "You aren't even in a party.");
						return true;
					}
					RPGParty party = partyManager.getParty(rp.partyID);
					if (party.host == rp)
					{
						RPGParty.msg(p, "You can't leave the party as a host.");
						RPGParty.msg(p, "Either disband the party or pass host to someone else.");
						return true;
					}
					party.removePlayer(rp);
					party.updatePartyInventory();
					party.broadcastMessage("&5" + p.getName() + " &dhas left the party.");
					RPGParty.msg(p, "You have left the party.");
					return true;
				}
				if (args[0].equalsIgnoreCase("invite"))
				{
					if (args.length < 2)
					{
						RPGParty.msg(p, "Usage: /party invite <player>");
						return true;
					}
					if (rp.partyID == -1)
					{
						RPGParty.msg(p, "You are not in a party.");
						return true;
					}
					RPGParty party = partyManager.getParty(rp.partyID);
					if (party.host != rp)
					{
						RPGParty.msg(p, "You are not the host, try asking &5" + party.host.getPlayerName() + " &dto do it.");
						return true;
					}
					RPlayer target = playerManager.getRPlayer(CakeLibrary.completeName(args[1]));
					if (target == null)
					{
						RPGParty.msg(p, "No player by that name was found.");
						return true;
					}
					if (party.players.contains(target))
					{
						RPGParty.msg(p, "That player is already in the party.");
						return true;
					}
					Player t = target.getPlayer();
					if (t == null)
					{
						RPGParty.msg(p, "That player is not online.");
						return true;
					}
					if (party.invites.contains(target.getPlayerName()))
					{
						RPGParty.msg(p, "You have already invited the player.");
						return true;
					}
					party.invites.add(target.getPlayerName());
					RPGParty.msg(t, "You have been invited to join &5" + p.getName() + "&d's party.");
					RPGParty.msg(t, "Type &5/party join " + p.getName() + " &dto join.");
					party.broadcastMessage("&5" + target.getPlayerName() + " &dhas been invited to the party.");
					return true;
				}
				if (args[0].equalsIgnoreCase("kick"))
				{
					if (args.length < 2)
					{
						RPGParty.msg(p, "Usage: /party kick <player>");
						return true;
					}
					if (rp.partyID == -1)
					{
						RPGParty.msg(p, "You are not in a party.");
						return true;
					}
					RPGParty party = partyManager.getParty(rp.partyID);
					if (party.host != rp)
					{
						RPGParty.msg(p, "You are not the host, try asking &5" + party.host.getPlayerName() + " &dto do it.");
						return true;
					}
					RPlayer target = playerManager.getRPlayer(CakeLibrary.completeName(args[1]));
					if (target == null)
					{
						RPGParty.msg(p, "No player by that name was found.");
						return true;
					}
					if (party.host == target)
					{
						RPGParty.msg(p, "You can't kick yourself from your party.");
						return true;
					}
					if (!party.players.contains(target))
					{
						RPGParty.msg(p, "That player is not in the party.");
						return true;
					}
					Player t = target.getPlayer();
					party.removePlayer(target);
					party.updatePartyInventory();
					party.broadcastMessage("&5" + target.getPlayerName() + " &dhas been kicked from the party.");
					if (t != null)
						RPGParty.msg(t, "You have been kicked from the party.");
					return true;
				}
				if (args[0].equalsIgnoreCase("sethost"))
				{
					if (args.length < 2)
					{
						RPGParty.msg(p, "Usage: /party sethost <player>");
						return true;
					}
					if (rp.partyID == -1)
					{
						RPGParty.msg(p, "You are not in a party.");
						return true;
					}
					RPGParty party = partyManager.getParty(rp.partyID);
					if (party.host != rp)
					{
						RPGParty.msg(p, "You are not the host, try asking &5" + party.host.getPlayerName() + " &dto do it.");
						return true;
					}
					RPlayer target = playerManager.getRPlayer(CakeLibrary.completeName(args[1]));
					if (target == null)
					{
						RPGParty.msg(p, "No player by that name was found.");
						return true;
					}
					if (!party.players.contains(target))
					{
						RPGParty.msg(p, "That player is not in the party.");
						return true;
					}
					party.host = target;
					party.updatePartyInventory();
					party.broadcastMessage("&5" + target.getPlayerName() + " &dhas been set as party host.");
					return true;
				}
				if (args[0].equalsIgnoreCase("disband"))
				{
					if (rp.partyID == -1)
					{
						RPGParty.msg(p, "You are not in a party.");
						return true;
					}
					RPGParty party = partyManager.getParty(rp.partyID);
					if (party.host != rp)
					{
						RPGParty.msg(p, "You are not the host, try asking &5" + party.host.getPlayerName() + " &dto do it.");
						return true;
					}
					party.disbandParty();
					return true;
				}
				msgNoTag(p, helpParty);
				return true;
			}
			if (command.getName().equalsIgnoreCase("stats"))
			{
				RPlayer target = rp;
				if (args.length > 0)
				{
					target = playerManager.getRPlayer(args[0]);
					if (target == null)
					{
						msg(p, "No player by that name can be found.");
						return true;
					}
					if (target.getPlayer() == null)
					{
						msg(p, "That player is not online.");
						return true;
					}
				}
				msgNoTag(p, "&b---[ Stats for &3" + target.getPlayerName() + "&b ]---");
				msgNoTag(p, "&6 * Class: &e" + target.currentClass.getClassName());
				msgNoTag(p, "&6 * Level: &e" + target.getCurrentClass().getLevel() + 
						" &6(&e" + target.getCurrentClass().xp + "&6/&e" + RPGClass.getXPRequiredForLevel(target.getCurrentClass().lastCheckedLevel + 1) + "XP&6)");
				msgNoTag(p, "&4 * Magic Damage: &c" + target.calculateMagicDamage());
				msgNoTag(p, "&4 * Brute Damage: &c" + target.calculateBruteDamage());
				msgNoTag(p, "&4 * Crit Chance: &c" + target.calculateCritChance() + "%");
				msgNoTag(p, "&4 * Crit Damage: &c" + (int) (target.calculateCritDamageMultiplier() * 100.0D) + "%");
				msgNoTag(p, "&2 * Attack Speed: &ax" + Double.parseDouble(String.format("%.1f", (1.0D / target.calculateCastDelayMultiplier()))));
				msgNoTag(p, "&2 * Cooldowns: &a-" + target.calculateCooldownReduction() + "%");
				return true;
			}
			if (command.getName().equalsIgnoreCase("item"))
			{
				if (!p.hasPermission("rpgcore.item"))
				{
					msg(p, "You do not have permissions to use this command.");
					return true;
				}
				if (args.length < 1)
				{
					msgNoTag(p, helpItem);
					return true;
				}
				ItemStack is = p.getItemInHand();
				RItem ri = new RItem(is);
				if (CakeLibrary.isItemStackNull(is))
				{
					msg(p, "Hold the item you want to edit.");
					return true;
				}
				if (args[0].equalsIgnoreCase("tier"))
				{
					if (args.length < 2)
					{
						msg(p, "Usage: /item tier <tier>");
						return true;
					}
					int tier = -1;
					try
					{
						tier = Integer.parseInt(args[1]);
					} catch (Exception e) {
						msg(p, "Enter a number.");
						return true;
					}
					if (tier < 0)
					{
						msg(p, "The minimum tier is 0.");
						return true;
					}
					if (tier > RItem.tiers.length)
					{
						msg(p, "The maximum tier is " + RItem.tiers.length);
						return true;
					}
					ri.setTier(tier);
					p.setItemInHand(ri.createItem());
					msg(p, "Tier attribute edited.");
					return true;
				}
				if (args[0].equalsIgnoreCase("lvrequirement"))
				{
					if (args.length < 2)
					{
						msg(p, "Usage: /item lvrequirement <damage>");
						return true;
					}
					int dmg = -1;
					try
					{
						dmg = Integer.parseInt(args[1]);
					} catch (Exception e) {
						msg(p, "Enter a number.");
						return true;
					}
					ri.levelRequirement = dmg;
					p.setItemInHand(ri.createItem());
					msg(p, "Lv requirement attribute edited.");
					return true;
				} else if (args[0].equalsIgnoreCase("magicdamage"))
				{
					if (args.length < 2)
					{
						msg(p, "Usage: /item magicdamage <damage>");
						return true;
					}
					int dmg = -1;
					try
					{
						dmg = Integer.parseInt(args[1]);
					} catch (Exception e) {
						msg(p, "Enter a number.");
						return true;
					}
					ri.magicDamage = dmg;
					p.setItemInHand(ri.createItem());
					msg(p, "Magic damage attribute edited.");
					return true;
				} else if (args[0].equalsIgnoreCase("brutedamage"))
				{
					if (args.length < 2)
					{
						msg(p, "Usage: /item brutedamage <damage>");
						return true;
					}
					int dmg = -1;
					try
					{
						dmg = Integer.parseInt(args[1]);
					} catch (Exception e) {
						msg(p, "Enter a number.");
						return true;
					}
					ri.bruteDamage = dmg;
					p.setItemInHand(ri.createItem());
					msg(p, "Brute damage attribute edited.");
					return true;
				} else if (args[0].equalsIgnoreCase("critchance"))
				{
					if (args.length < 2)
					{
						msg(p, "Usage: /item critchance <percentage>");
						return true;
					}
					int dmg = -1;
					try
					{
						dmg = Integer.parseInt(args[1]);
					} catch (Exception e) {
						msg(p, "Enter a number.");
						return true;
					}
					ri.critChance = dmg;
					p.setItemInHand(ri.createItem());
					msg(p, "Crit chance attribute edited.");
					return true;
				} else if (args[0].equalsIgnoreCase("critdamage"))
				{
					if (args.length < 2)
					{
						msg(p, "Usage: /item critchance <percentage>");
						return true;
					}
					int dmg = -1;
					try
					{
						dmg = Integer.parseInt(args[1]);
					} catch (Exception e) {
						msg(p, "Enter a number.");
						return true;
					}
					ri.critDamage = dmg;
					p.setItemInHand(ri.createItem());
					msg(p, "Crit damage attribute edited.");
					return true;
				} else if (args[0].equalsIgnoreCase("attackspeed"))
				{
					if (args.length < 2)
					{
						msg(p, "Usage: /item attackspeed <damage>");
						return true;
					}
					double amt = -1;
					try
					{
						amt = Double.parseDouble(args[1]);
					} catch (Exception e) {
						msg(p, "Enter a number.");
						return true;
					}
					ri.attackSpeed = amt;
					p.setItemInHand(ri.createItem());
					msg(p, "Attack Speed attribute edited.");
					return true;
				} else if (args[0].equalsIgnoreCase("cooldowns"))
				{
					if (args.length < 2)
					{
						msg(p, "Usage: /item cooldowns <percentage>");
						return true;
					}
					int amt = -1;
					try
					{
						amt = Integer.parseInt(args[1]);
					} catch (Exception e) {
						msg(p, "Enter a number.");
						return true;
					}
					ri.cooldownReduction = amt;
					p.setItemInHand(ri.createItem());
					msg(p, "Cooldown reduction attribute edited.");
					return true;
				} else if (args[0].equalsIgnoreCase("dmgreduction"))
				{
					if (args.length < 2)
					{
						msg(p, "Usage: /item dmgreduction <percentage>");
						return true;
					}
					int amt = -1;
					try
					{
						amt = Integer.parseInt(args[1]);
					} catch (Exception e) {
						msg(p, "Enter a number.");
						return true;
					}
					ri.damageReduction = amt;
					p.setItemInHand(ri.createItem());
					msg(p, "Damage reduction attribute edited.");
					return true;
				} else if (args[0].equalsIgnoreCase("settier"))
				{
					if (args.length < 2)
					{
						msg(p, "Usage: /item settier <tier>");
						return true;
					}
					int tier = -1;
					try
					{
						tier = Integer.parseInt(args[1]);
					} catch (Exception e) {
						msg(p, "Enter a number.");
						return true;
					}
					if (tier <= 0)
					{
						msg(p, "Enter a number more than 0.");
						return true;
					}
					ri.bonusStat.tier = tier;
					p.setItemInHand(ri.createItem());
					msg(p, "Tier changed.");
					return true;
				} else if (args[0].equalsIgnoreCase("unbreakable"))
				{
					ItemMeta im = is.getItemMeta();
					boolean u = im.spigot().isUnbreakable();	
					im.spigot().setUnbreakable(!u);
					is.setItemMeta(im);
					p.setItemInHand(is);
					msg(p, "Unbreakable attribute toggled to: " + !u);
					return true;
				}
				msgNoTag(p, helpItem);
				return true;
			}
			if (command.getName().equalsIgnoreCase("mob"))
			{
				if (!p.hasPermission("rpgcore.spawnmob"))
				{
					msg(p, "No access to this command lul");
					return true;
				}
				if (args.length < 1)
				{
					msg(p, "Usage: /mob <mobname>");
					return true;
				}
				int count = 1;
				if (args.length > 1)
					count = Math.min(10, Integer.parseInt(args[1]));
				if (args[0].equalsIgnoreCase("reinforcedzombie"))
				{
					for (int i = 0; i < count; i++)
					{
						Zombie z = p.getWorld().spawn(p.getLocation(), Zombie.class);
						new ReinforcedZombie(z);
					}
					msg(p, "Spawned.");
					return true;
				}
				if (args[0].equalsIgnoreCase("reinforcedskeleton"))
				{
					for (int i = 0; i < count; i++)
					{
						Skeleton z = p.getWorld().spawn(p.getLocation(), Skeleton.class);
						new ReinforcedSkeleton(z);
					}
					msg(p, "Spawned.");
					return true;
				}
				if (args[0].equalsIgnoreCase("magezombie"))
				{
					for (int i = 0; i < count; i++)
					{
						Zombie z = p.getWorld().spawn(p.getLocation(), Zombie.class);
						new MageZombie(z);
					}
					msg(p, "Spawned.");
					return true;
				}
				if (args[0].equalsIgnoreCase("warriorzombie"))
				{
					for (int i = 0; i < count; i++)
					{
						Zombie z = p.getWorld().spawn(p.getLocation(), Zombie.class);
						new WarriorZombie(z);
					}
					msg(p, "Spawned.");
					return true;
				}
				msg(p, "No such mob available for spawning.");
				return true;
			}
			if (command.getName().equalsIgnoreCase("boss"))
			{
				if (!p.hasPermission("rpgcore.spawnmob"))
				{
					msg(p, "No access to this command lul");
					return true;
				}
				if (args.length < 1)
				{
					msg(p, "Usage: /boss <bossname>");
					return true;
				}
				if (args[0].equalsIgnoreCase("undeademperor"))
				{
					Zombie z = p.getWorld().spawn(p.getLocation(), Zombie.class);
					new UndeadEmperor(z);
					msg(p, "Spawned.");
					return true;
				}
				if (args[0].equalsIgnoreCase("astrea"))
				{
					Zombie z = p.getWorld().spawn(p.getLocation(), Zombie.class);
					new Astrea(z);
					msg(p, "Spawned.");
					return true;
				}
				msg(p, "No such mob available for spawning.");
				return true;
			}
			if (command.getName().equalsIgnoreCase("bgm"))
			{
				if (args.length < 1)
				{
					msg(p, "Usage: /bgm <list/reload/play/stop> [song]");
					return true;
				}
				if (args[0].equalsIgnoreCase("list"))
				{
					msgNoTag(p, "&6===[ &eSongs &6]===");
					for (RPGSong song: songManager.songs)
						msgNoTag(p, "&e\"" + song.name + "\" &6- &e" + song.BPM + "BPM, " + song.tracks.size() + " tracks");
					return true;
				}
				if (args[0].equalsIgnoreCase("reload"))
				{
					songManager.readSongs();
					msg(p, "Songs reloaded - found " + songManager.songs.size() + ".");
					return true;
				}
				if (args[0].equalsIgnoreCase("rp"))
				{
					songManager.readSongs();
					if (args.length < 2)
					{
						msg(p, "Usage: /bgm rp <song>");
						return true;
					}
					String songName = args[1];
					RPGSong song = songManager.getSong(songName);
					if (song == null)
					{
						msg(p, "Unable to find song by name provided.");
						return true;
					}
					for (RunningTrack rt: RSongManager.runningTracks)
						if (rt.player.getName().equalsIgnoreCase(p.getName()))
							rt.stop();
					int offset = 0;
					if (args.length > 2)
					{
						try
						{
							offset = Integer.parseInt(args[2]);
						} catch (Exception e) {}
					}
					song.play(p, offset);
					return true;
				}
				if (args[0].equalsIgnoreCase("play") || args[0].equalsIgnoreCase("p"))
				{
					if (args.length < 2)
					{
						msg(p, "Usage: /bgm play <song>");
						return true;
					}
					String songName = args[1];
					RPGSong song = songManager.getSong(songName);
					if (song == null)
					{
						msg(p, "Unable to find song by name provided.");
						return true;
					}
					for (RunningTrack rt: RSongManager.runningTracks)
						if (rt.player.getName().equalsIgnoreCase(p.getName()))
							if (rt.player.getName().equalsIgnoreCase(p.getName()))
							{
								rt.stop();
								break;
							}
					int offset = 0;
					if (args.length > 2)
					{
						try
						{
							offset = Integer.parseInt(args[2]);
						} catch (Exception e) {}
					}
					song.play(p, offset);
					return true;
				}
				if (args[0].equalsIgnoreCase("stop") || args[0].equalsIgnoreCase("s"))
				{
					for (RunningTrack rt: RSongManager.runningTracks)
						if (rt.player.getName().equalsIgnoreCase(p.getName()))
							rt.stop();
					return true;
				}
				msg(p, "Usage: /bgm <list/reload/play/stop> [song]");
				return true;
			}
			if (command.getName().equalsIgnoreCase("sound"))
			{
				RPGEvents.scheduleRunnable(new RPGEvents.PlaySoundEffect(((Player) sender).getLocation(), Sound.valueOf(args[0]), args.length > 1 ? Float.parseFloat(args[1]) : 1.0F, args.length > 2 ? Float.parseFloat(args[2]) : 1.0F), 0);
				return true;
			}
			if (command.getName().equalsIgnoreCase("class"))
			{
				p.openInventory(ClassInventory.getClassInventory(rp));
				return true;
			}
			if (command.getName().equalsIgnoreCase("skills"))
			{
				if (p.hasPermission("rpgcore.skills") && args.length > 0)
				{
					if (args[0].equalsIgnoreCase("setsp"))
					{
						if (args.length < 2)
						{
							msg(p, "Usage: /skills setsp <amt> [player]");
							return true;
						}
						int amt = 0;
						try
						{
							amt = Integer.parseInt(args[1]);
						} catch (Exception e) {
							msg(p, "Enter a number.");
							return true;
						}
						RPlayer target = rp;
						if (args.length > 2)
							target = playerManager.getRPlayer(args[2]);
						if (target == null)
						{
							msg(p, "Unable to find player.");
							return true;
						}
						target.getCurrentClass().skillPoints = amt;
						msg(p, "Skill points set.");
						return true;
					}
					if (args[0].equalsIgnoreCase("addsp"))
					{
						if (args.length < 2)
						{
							msg(p, "Usage: /skills addsp <amt> [player]");
							return true;
						}
						int amt = 0;
						try
						{
							amt = Integer.parseInt(args[1]);
						} catch (Exception e) {
							msg(p, "Enter a number.");
							return true;
						}
						RPlayer target = rp;
						if (args.length > 2)
							target = playerManager.getRPlayer(args[2]);
						if (target == null)
						{
							msg(p, "Unable to find player.");
							return true;
						}
						target.getCurrentClass().skillPoints += amt;
						msg(p, "Skill points added.");
						return true;
					}
					return true;
				}
				p.openInventory(SkillInventory.getSkillInventory(rp, 0));
				return true;
			}
		}
		return false;
	}

	public static void msgNoTag(CommandSender p, String[] msgs)
	{
		for (String msg: msgs)
			p.sendMessage(CakeLibrary.recodeColorCodes(msg));
	}

	public static void msgNoTag(CommandSender p, String msg)
	{
		p.sendMessage(CakeLibrary.recodeColorCodes(msg));
	}

	public static void msg(CommandSender p, String[] msgs)
	{
		for (String msg: msgs)
			p.sendMessage(CakeLibrary.recodeColorCodes("&6[&eRPGCore&6] &e" + msg));
	}

	public static void msg(CommandSender p, String msg)
	{
		p.sendMessage(CakeLibrary.recodeColorCodes("&6[&eRPGCore&6] &e" + msg));
	}

	public static void msgConsole(String msg)
	{
		Bukkit.getConsoleSender().sendMessage(CakeLibrary.recodeColorCodes("&6[&eRPGCore&6] &e" + msg));
	}
}
