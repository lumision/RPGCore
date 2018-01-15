package rpgcore.main;

import java.io.File;
import java.util.Random;
import java.util.Timer;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import rpgcore.classes.ClassInventory;
import rpgcore.classes.RPGClass;
import rpgcore.entities.bosses.Astrea;
import rpgcore.entities.bosses.UndeadEmperor;
import rpgcore.entities.mobs.CasterEntity;
import rpgcore.item.BonusStat.BonusStatCrystal;
import rpgcore.item.RItem;
import rpgcore.party.Party;
import rpgcore.party.RPartyManager;
import rpgcore.player.RPlayer;
import rpgcore.player.RPlayerManager;
import rpgcore.skillinventory.SkillInventory;
import rpgcore.songs.RPGSong;
import rpgcore.songs.RSongManager;
import rpgcore.songs.RunningTrack;

public class RPGCore extends JavaPlugin
{
	public RPGEvents events;
	public RPGListener listener;
	public RPlayerManager playerManager;
	public RSongManager songManager;
	public RPartyManager partyManager;
	public File pluginFolder = new File("plugins/RPGCore");
	public static RPGCore instance;
	public static Random rand = new Random();
	public static Timer timer = new Timer();
	@Override
	public void onEnable()
	{
		RPGCore.instance = this;
		pluginFolder.mkdirs();
		RPGClass.setXPTable();
		playerManager = new RPlayerManager(this);
		songManager = new RSongManager(this);
		partyManager = new RPartyManager(this);
		listener = new RPGListener(this);
		getServer().getPluginManager().registerEvents(listener, this);
		events = new RPGEvents(this);

		RPGEvents.scheduleRunnable(new RPGEvents.ConsoleCommand("gamerule sendCommandFeedback false"), 1);
		RPGEvents.scheduleRunnable(new RPGEvents.ConsoleCommand("gamerule mobGriefing false"), 1);
		RPGEvents.scheduleRunnable(new RPGEvents.ConsoleCommand("gamerule doFireTick false"), 1);
	}

	@Override
	public void onDisable()
	{
		timer.cancel();
		playerManager.writePlayerData();
		for (CasterEntity ce: CasterEntity.entities)
			ce.entity.remove();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String cmd, String[] args)
	{
		if (sender instanceof Player)
		{
			Player p = (Player) sender;
			if (CakeAPI.hasColor(p.getOpenInventory().getTitle()))
			{
				msg(p, ":^)");
				return true;
			}
			RPlayer rp = playerManager.getRPlayer(p.getName());
			if (rp == null)
				return false;
			if (command.getName().equalsIgnoreCase("crystal"))
			{
				if (!p.hasPermission("rpgcore.crystal"))
				{
					msg(p, "You do not have permissions to do this.");
					return true;
				}
				if (args.length < 1)
				{
					msg(p, "Usage: /crystal <spirit/wisdom/alchemy/passion>");
					return true;
				}
				ItemStack crystal = null;
				if (args[0].equalsIgnoreCase("alchemy"))
					crystal = BonusStatCrystal.ALL_LINES_REROLL.getItemStack();
				else if (args[0].equalsIgnoreCase("passion"))
					crystal = BonusStatCrystal.TIER_REROLL.getItemStack();
				else if (args[0].equalsIgnoreCase("wisdom"))
					crystal = BonusStatCrystal.LINE_AMOUNT_REROLL.getItemStack();
				else if (args[0].equalsIgnoreCase("spirit"))
					crystal = BonusStatCrystal.STAT_ADDER.getItemStack();
				else
					msg(p, "Usage: /crystal <spirit/wisdom/alchemy/passion>");
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
					msgNoTag(p, "&5===[&d /party Help &5]===");
					msgNoTag(p, "&5/party info: &dGives info about the party you're in");
					msgNoTag(p, "&5/party join <host>: &dJoins a party if you were invited");
					msgNoTag(p, "&5/party leave: &dLeaves the party as a member");
					msgNoTag(p, "&5/party create: &dCreates a party with you as the host");
					msgNoTag(p, "&5/party invite <player>: &dInvites a player to your party (Host)");
					msgNoTag(p, "&5/party kick <player>: &dKicks a player from your party (Host)");
					msgNoTag(p, "&5/party sethost <player>: &dPasses host to a member (Host)");
					msgNoTag(p, "&5/party disband: &dDisbands your party (Host)");
					msgNoTag(p, "&dBegin a message with '\\' to chat in the party.");
					return true;
				}
				if (args[0].equalsIgnoreCase("info"))
				{
					if (rp.partyID == -1)
					{
						Party.msg(p, "You are not in a party.");
						return true;
					}
					Party party = partyManager.getParty(rp.partyID);
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
						Party.msg(p, "You are already in a party.");
						Party.msg(p, "Use &5/party disband &dto disband it.");
						return true;
					}
					partyManager.createNewParty(rp);
					Party.msg(p, "Party created.");
					Party.msg(p, "&dUse &5/party <invite/kick/disband> [player] &dto manage.");
					return true;
				}
				if (args[0].equalsIgnoreCase("join"))
				{
					if (args.length < 2)
					{
						Party.msg(p, "Usage: /party join <host>");
						return true;
					}
					if (rp.partyID != -1)
					{
						Party.msg(p, "You are already in a party.");
						Party.msg(p, "Use &5/party leave &dto leave.");
						return true;
					}
					RPlayer target = playerManager.getRPlayer(CakeAPI.completeName(args[1]));
					if (target == null)
					{
						Party.msg(p, "No player by that name was found.");
						return true;
					}
					if (target.partyID == -1)
					{
						Party.msg(p, "That player is not even in a party.");
						return true;
					}
					Party party = partyManager.getParty(target.partyID);
					if (party.host != target)
					{
						Party.msg(p, "That player is not the host of his party.");
						return true;
					}
					if (!party.invites.contains(p.getName()))
					{
						Party.msg(p, "You aren't invited to this party.");
						return true;
					}
					if (party.players.size() >= 9)
					{
						Party.msg(p, "This party is full (9 players).");
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
						Party.msg(p, "You aren't even in a party.");
						return true;
					}
					Party party = partyManager.getParty(rp.partyID);
					if (party.host == rp)
					{
						Party.msg(p, "You can't leave the party as a host.");
						Party.msg(p, "Either disband the party or pass host to someone else.");
						return true;
					}
					party.removePlayer(rp);
					party.updatePartyInventory();
					party.broadcastMessage("&5" + p.getName() + " &dhas left the party.");
					Party.msg(p, "You have left the party.");
					return true;
				}
				if (args[0].equalsIgnoreCase("invite"))
				{
					if (args.length < 2)
					{
						Party.msg(p, "Usage: /party invite <player>");
						return true;
					}
					if (rp.partyID == -1)
					{
						Party.msg(p, "You are not in a party.");
						return true;
					}
					Party party = partyManager.getParty(rp.partyID);
					if (party.host != rp)
					{
						Party.msg(p, "You are not the host, try asking &5" + party.host.getPlayerName() + " &dto do it.");
						return true;
					}
					RPlayer target = playerManager.getRPlayer(CakeAPI.completeName(args[1]));
					if (target == null)
					{
						Party.msg(p, "No player by that name was found.");
						return true;
					}
					if (party.players.contains(target))
					{
						Party.msg(p, "That player is already in the party.");
						return true;
					}
					Player t = target.getPlayer();
					if (t == null)
					{
						Party.msg(p, "That player is not online.");
						return true;
					}
					if (party.invites.contains(target.getPlayerName()))
					{
						Party.msg(p, "You have already invited the player.");
						return true;
					}
					party.invites.add(target.getPlayerName());
					Party.msg(t, "You have been invited to join &5" + p.getName() + "&d's party.");
					Party.msg(t, "Type &5/party join " + p.getName() + " &dto join.");
					party.broadcastMessage("&5" + target.getPlayerName() + " &dhas been invited to the party.");
					return true;
				}
				if (args[0].equalsIgnoreCase("kick"))
				{
					if (args.length < 2)
					{
						Party.msg(p, "Usage: /party kick <player>");
						return true;
					}
					if (rp.partyID == -1)
					{
						Party.msg(p, "You are not in a party.");
						return true;
					}
					Party party = partyManager.getParty(rp.partyID);
					if (party.host != rp)
					{
						Party.msg(p, "You are not the host, try asking &5" + party.host.getPlayerName() + " &dto do it.");
						return true;
					}
					RPlayer target = playerManager.getRPlayer(CakeAPI.completeName(args[1]));
					if (target == null)
					{
						Party.msg(p, "No player by that name was found.");
						return true;
					}
					if (party.host == target)
					{
						Party.msg(p, "You can't kick yourself from your party.");
						return true;
					}
					if (!party.players.contains(target))
					{
						Party.msg(p, "That player is not in the party.");
						return true;
					}
					Player t = target.getPlayer();
					party.removePlayer(target);
					party.updatePartyInventory();
					party.broadcastMessage("&5" + target.getPlayerName() + " &dhas been kicked from the party.");
					if (t != null)
						Party.msg(t, "You have been kicked from the party.");
					return true;
				}
				if (args[0].equalsIgnoreCase("sethost"))
				{
					if (args.length < 2)
					{
						Party.msg(p, "Usage: /party sethost <player>");
						return true;
					}
					if (rp.partyID == -1)
					{
						Party.msg(p, "You are not in a party.");
						return true;
					}
					Party party = partyManager.getParty(rp.partyID);
					if (party.host != rp)
					{
						Party.msg(p, "You are not the host, try asking &5" + party.host.getPlayerName() + " &dto do it.");
						return true;
					}
					RPlayer target = playerManager.getRPlayer(CakeAPI.completeName(args[1]));
					if (target == null)
					{
						Party.msg(p, "No player by that name was found.");
						return true;
					}
					if (!party.players.contains(target))
					{
						Party.msg(p, "That player is not in the party.");
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
						Party.msg(p, "You are not in a party.");
						return true;
					}
					Party party = partyManager.getParty(rp.partyID);
					if (party.host != rp)
					{
						Party.msg(p, "You are not the host, try asking &5" + party.host.getPlayerName() + " &dto do it.");
						return true;
					}
					party.disbandParty();
					return true;
				}
				msgNoTag(p, "&5===[&d /party Help &5]===");
				msgNoTag(p, "&5/party info: &dGives info about the party you're in");
				msgNoTag(p, "&5/party join <host>: &dJoins a party if you were invited");
				msgNoTag(p, "&5/party leave: &dLeaves the party as a member");
				msgNoTag(p, "&5/party create: &dCreates a party with you as the host");
				msgNoTag(p, "&5/party invite <player>: &dInvites a player to your party (*)");
				msgNoTag(p, "&5/party kick <player>: &dKicks a player from your party (*)");
				msgNoTag(p, "&5/party sethost <player>: &dPasses host to another member (*)");
				msgNoTag(p, "&5/party disband: &dDisbands your party (*)");
				msgNoTag(p, "&dBegin a message with '\\' to chat in the party.");
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
				msgNoTag(p, "&b * Class: &3" + target.currentClass.toString());
				msgNoTag(p, "&b * Level: &3" + target.getCurrentClass().getLevel() + 
						" &b(&3" + target.getCurrentClass().xp + "&b/&3" + RPGClass.getXPRequiredForLevel(target.getCurrentClass().lastCheckedLevel + 1) + "XP&b)");
				msgNoTag(p, "&b * Magic Damage: &3" + target.calculateMagicDamage());
				msgNoTag(p, "&b * Brute Damage: &3" + target.calculateBruteDamage());
				msgNoTag(p, "&b * Crit Chance: &3" + target.calculateCritChance() + "%");
				msgNoTag(p, "&b * Crit Damage: &3" + (int) (target.calculateCritDamageMultiplier() * 100.0D) + "%");
				msgNoTag(p, "&b * Attack Speed: &3x" + (1.0D / target.calculateCastDelayMultiplier()));
				msgNoTag(p, "&b * Cooldowns: &3-" + target.calculateCooldownReduction() + "%");
				return true;
			}
			if (command.getName().equalsIgnoreCase("item"))
			{
				if (!p.hasPermission("rpgcore.item"))
				{
					msg(p, "You do not have permissions to use this godly command.");
					return true;
				}
				if (args.length < 1)
				{
					msgNoTag(p, "&6===[&e /item Commands &6]===");
					msgNoTag(p, "&6/item magicdamage <damage>");
					msgNoTag(p, "&6/item brutedamage <damage>");
					msgNoTag(p, "&6/item attackspeed <multiplier>");
					msgNoTag(p, "&6/item cooldowns <percentage>");
					msgNoTag(p, "&6/item unbreakable");
					msgNoTag(p, "&6===[&e /item Commands &6]===");
					return true;
				}
				ItemStack is = p.getItemInHand();
				RItem ri = new RItem(is);
				if (CakeAPI.isItemStackNull(is))
				{
					msg(p, "Hold the item you want to edit.");
					return true;
				}
				if (args[0].equalsIgnoreCase("magicdamage"))
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
				}
				if (args[0].equalsIgnoreCase("brutedamage"))
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
				}
				if (args[0].equalsIgnoreCase("critchance"))
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
				}
				if (args[0].equalsIgnoreCase("critdamage"))
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
				}
				if (args[0].equalsIgnoreCase("attackspeed"))
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
				}
				if (args[0].equalsIgnoreCase("cooldowns"))
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
				}
				if (args[0].equalsIgnoreCase("settier"))
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
				}
				if (args[0].equalsIgnoreCase("unbreakable"))
				{
					ItemMeta im = is.getItemMeta();
					boolean u = im.spigot().isUnbreakable();	
					im.spigot().setUnbreakable(!u);
					is.setItemMeta(im);
					p.setItemInHand(is);
					msg(p, "Unbreakable attribute toggled to: " + !u);
					return true;
				}
				msgNoTag(p, "&6===[&e /item Commands &6]===");
				msgNoTag(p, "&6/item magicdamage <damage>");
				msgNoTag(p, "&6/item brutedamage <damage>");
				msgNoTag(p, "&6/item attackspeed <multiplier>");
				msgNoTag(p, "&6/item cooldowns <percentage>");
				msgNoTag(p, "&6/item unbreakable");
				msgNoTag(p, "&6===[&e /item Commands &6]===");
				return true;
			}
			if (command.getName().equalsIgnoreCase("spawnmob"))
			{
				if (args.length < 1)
				{
					msg(p, "Usage: /spawnmob <mobname>");
					return true;
				}
				if (!p.hasPermission("rpgcore.spawnmob"))
				{
					msg(p, "No access to this command lul");
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
					msgNoTag(p, "&6===[ Songs ]===");
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

	public static void msgNoTag(CommandSender p, String msg)
	{
		p.sendMessage(CakeAPI.recodeColorCodes(msg));
	}

	public static void msg(CommandSender p, String msg)
	{
		p.sendMessage(CakeAPI.recodeColorCodes("&6[&eRPGCore&6] &e" + msg));
	}

	public static void msgConsole(String msg)
	{
		Bukkit.getConsoleSender().sendMessage(CakeAPI.recodeColorCodes("&6[&eRPGCore&6] &e" + msg));
	}
}
