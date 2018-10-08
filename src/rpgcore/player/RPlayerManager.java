package rpgcore.player;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import rpgcore.classes.RPGClass;
import rpgcore.classes.RPGClass.ClassType;
import rpgcore.item.RItem;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGCore;
import rpgcore.sideclasses.RPGSideClass;
import rpgcore.sideclasses.RPGSideClass.SideClassType;
import rpgcore.skills.RPGSkill;

public class RPlayerManager 
{
	public RPGCore instance;
	public ArrayList<RPlayer> players = new ArrayList<RPlayer>();
	public static final File playersFolder = new File("plugins/RPGCore/players");
	public static final File accessoriesFolder = new File("plugins/RPGCore/players/accessories");
	public static final File mailboxFolder = new File("plugins/RPGCore/players/mailbox");
	public RPlayerManager(RPGCore instance)
	{
		this.instance = instance;
		playersFolder.mkdirs();
		accessoriesFolder.mkdirs();
		readData();
		for (Player p: Bukkit.getOnlinePlayers())
		{
			RPlayer rp = getRPlayer(p.getUniqueId());
			if (rp == null)
				rp = addRPlayer(p.getUniqueId());
			rp.updatePlayerREquips();
			rp.updateStats();
			p.setScoreboard(rp.scoreboard);
		}
	}

	public RPlayer addRPlayer(UUID uuid)
	{
		RPlayer rp = new RPlayer(uuid);
		players.add(rp);
		writeData(rp);
		return rp;
	}

	public RPlayer addRPlayer(UUID uuid, ArrayList<RPGClass> classes, ArrayList<RPGSideClass> sideClasses, ClassType currentClass, ArrayList<String> skills, 
			int gold, int tokens, Map<String, String> npcFlags, boolean tutorialCompleted)
	{
		RPlayer rp = new RPlayer(uuid, classes, sideClasses, currentClass, skills, gold, tokens);
		rp.npcFlags = npcFlags;
		rp.tutorialCompleted = tutorialCompleted;
		players.add(rp);
		return rp;
	}

	public RPlayer getRPlayer(UUID uuid)
	{
		for (RPlayer rp: players)
			if (rp.getUniqueID().equals(uuid))
				return rp;
		return null;
	}

	public RPlayer getRPlayer(String name)
	{
		for (RPlayer rp: players)
			if (rp.getPlayerName().equalsIgnoreCase(name))
				return rp;
		return null;
	}

	public void playersTick()
	{
		for (int i = 0; i < players.size(); i++)
		{
			RPlayer rp = players.get(i);
			if (RPGCore.serverAliveTicks % 20 == 0)
				rp.tick20();
			if (RPGCore.serverAliveTicks % 10 == 0)
				rp.tick10();
			rp.tick();
		}
	}

	public int getRPlayerAmount()
	{
		return players.size();
	}

	public void readData()
	{
		players.clear();
		UUID uuid = null;
		ClassType currentClass = ClassType.ALL;
		ArrayList<RPGClass> classes = new ArrayList<RPGClass>();
		ArrayList<RPGSideClass> sideClasses = new ArrayList<RPGSideClass>();
		ArrayList<String> skills = new ArrayList<String>();
		Map<String, String> npcFlags = new HashMap<String, String>();
		ArrayList<String> recipes = new ArrayList<String>();
		boolean tutorialCompleted = false;
		int lastSkillbookTier = 1;
		int lastRecipeBookPage = 1;
		int globalGiftIndex = 0;
		int gold = 0;
		int tokens = 0;
		int arenaInstanceID = -1;
		int cooldownDisplayMode = 0;
		Location leftForArenaLocation = null;
		for (File file: playersFolder.listFiles())
		{
			if (!file.getName().endsWith(".yml"))
				continue;
			RPlayer rp = null;
			String uuidString = file.getName().substring(0, file.getName().length() - 4);
			uuid = UUID.fromString(uuidString);
			try
			{

				ArrayList<String> lines = CakeLibrary.readFile(file);
				String header = "";
				for (String s: lines)
				{
					if (!s.startsWith(" "))
					{
						if (s.startsWith("class: "))
						{
							try
							{
								currentClass = ClassType.valueOf(s.split(": ")[1]);
							} catch (Exception e) {}
							continue;
						}
						if (s.startsWith("gold: "))
						{
							try
							{
								gold = Integer.parseInt(s.split(": ")[1]);
							} catch (Exception e) {}
							continue;
						}
						if (s.startsWith("tokens: "))
						{
							try
							{
								tokens = Integer.parseInt(s.split(": ")[1]);
							} catch (Exception e) {}
							continue;
						}
						if (s.startsWith("tutorialCompleted: "))
						{
							try
							{
								tutorialCompleted = Boolean.parseBoolean(s.split(": ")[1]);
							} catch (Exception e) {}
							continue;
						}
						if (s.startsWith("lastSkillbookTier: "))
						{
							try
							{
								lastSkillbookTier = Integer.parseInt(s.split(": ")[1]);
							} catch (Exception e) {}
							continue;
						}
						if (s.startsWith("lastRecipeBookPage: "))
						{
							try
							{
								lastRecipeBookPage = Integer.parseInt(s.split(": ")[1]);
							} catch (Exception e) {}
							continue;
						}
						if (s.startsWith("globalGiftIndex: "))
						{
							try
							{
								globalGiftIndex = Integer.parseInt(s.split(": ")[1]);
							} catch (Exception e) {}
							continue;
						}
						if (s.startsWith("cooldownDisplayMode: "))
						{
							try
							{
								cooldownDisplayMode = Integer.parseInt(s.split(": ")[1]);
							} catch (Exception e) {}
							continue;
						}
						if (s.startsWith("arenaInstanceID: "))
						{
							try
							{
								arenaInstanceID = Integer.parseInt(s.split(": ")[1]);
							} catch (Exception e) {}
							continue;
						}
						if (s.startsWith("leftForArenaLocation: "))
						{
							try
							{
								String[] split = s.split(": ");
								leftForArenaLocation = new Location(Bukkit.getWorld(split[0])
										, Integer.valueOf(split[1]), Integer.valueOf(split[2]), Integer.valueOf(split[3]));
							} catch (Exception e) {}
							continue;
						}
						if (s.startsWith("lastRecordedName: "))
							continue;
						header = s.substring(0, s.length() - 1);
						continue;
					}
					s = s.substring(1);
					if (header.equals("classes"))
					{
						String[] split = s.split(", ");
						if (split.length < 2)
							continue;
						ClassType classType = null;
						double xp = 0;
						try
						{
							classType = ClassType.valueOf(split[0]);
							xp = Double.parseDouble(split[1]);
						} catch (Exception e) {
							continue;
						}
						classes.add(new RPGClass(classType, xp));
						continue;
					} else if (header.equals("sideclasses"))
					{
						String[] split = s.split(", ");
						if (split.length < 2)
							continue;
						SideClassType sideClassType = null;
						int xp = 0;
						try
						{
							sideClassType = SideClassType.valueOf(split[0]);
							xp = Integer.parseInt(split[1]);
						} catch (Exception e) {
							continue;
						}
						sideClasses.add(new RPGSideClass(sideClassType, xp));
						continue;
					} else if (header.equals("skills"))
					{
						if (RPGSkill.getSkill(s) != null)
							skills.add(s);
					} else if (header.equals("npcflags"))
					{
						String[] split = s.split(", ");
						if (split.length < 2)
							continue;
						npcFlags.put(split[0], split[1]);
					} else if (header.equals("recipes"))
						recipes.add(s);
				}

				for (ClassType classType: ClassType.values())
				{
					boolean contains = false;
					for (RPGClass c: classes)
						if (c.classType.equals(classType))
							contains = true;
					if (!contains)
						classes.add(new RPGClass(classType));
				}

				for (SideClassType sideClassType: SideClassType.values())
				{
					boolean contains = false;
					for (RPGSideClass sideClass: sideClasses)
						if (sideClass.sideClassType.equals(sideClassType))
							contains = true;
					if (!contains)
						sideClasses.add(new RPGSideClass(sideClassType, 0));
				}

				rp = addRPlayer(uuid, classes, sideClasses, currentClass, skills, gold, tokens, npcFlags, tutorialCompleted);
				rp.lastSkillbookTier = lastSkillbookTier;
				rp.arenaInstanceID = arenaInstanceID;
				rp.leftForArenaLocation = leftForArenaLocation;
				rp.cooldownDisplayMode = cooldownDisplayMode;
				rp.globalGiftIndex = globalGiftIndex;
				rp.lastRecipeBookPage = lastRecipeBookPage;
			} catch (Exception e)
			{
				RPGCore.msgConsole("&4Error reading RPlayer file: " + file.getName());
				e.printStackTrace();
			}
			try
			{
				if (rp != null)
					for (int i = 0; i < 3; i++)
					{
						File riFile = new File(accessoriesFolder.getPath() + "/" + i + "_" + uuidString + ".yml");
						if (!riFile.exists())
							continue;
						rp.accessoryInventory.slots[i] = RItem.readFromFile(riFile);
					}
			} catch (Exception e)
			{
				RPGCore.msgConsole("&4Error reading RPlayer accessories: " + file.getName());
				e.printStackTrace();
			}
			try
			{
				if (rp != null)
				{
					File mailbox = new File(mailboxFolder.getPath() + "/" + rp.getUniqueID());
					if (mailbox.exists())
						for (File mailItem: mailbox.listFiles())
							if (mailItem.getName().endsWith(".yml"))
								rp.mailbox.items.add(RItem.readFromFile(mailItem));
				}
			} catch (Exception e)
			{
				RPGCore.msgConsole("&4Error reading RPlayer accessories: " + file.getName());
				e.printStackTrace();
			}

			uuid = null;
			currentClass = ClassType.ALL;
			classes = new ArrayList<RPGClass>();
			sideClasses = new ArrayList<RPGSideClass>();
			skills = new ArrayList<String>();
			npcFlags = new HashMap<String, String>();
			recipes = new ArrayList<String>();
			tutorialCompleted = false;
			lastSkillbookTier = 1;
			globalGiftIndex = 0;
			gold = 0;
			tokens = 0;
			arenaInstanceID = -1;
			cooldownDisplayMode = 0;
			leftForArenaLocation = null;
		}
	}

	public void writeData(RPlayer rp)
	{
		File file = new File(playersFolder.getPath() + "/" + rp.getUniqueID() + ".yml");
		try
		{
			ArrayList<String> lines = new ArrayList<String>();
			lines.add("lastRecordedName: " + rp.getPlayerName());
			lines.add("class: " + rp.currentClass.toString());
			lines.add("gold: " + rp.getGold());
			lines.add("tutorialCompleted: " + rp.tutorialCompleted);
			lines.add("lastSkillbookTier: " + rp.lastSkillbookTier);
			lines.add("globalGiftIndex: " + rp.globalGiftIndex);
			if (rp.arenaInstanceID != -1)
				lines.add("arenaInstanceID: " + rp.arenaInstanceID);
			if (rp.cooldownDisplayMode != 0)
				lines.add("cooldownDisplayMode: " + rp.cooldownDisplayMode);
			lines.add("classes:");
			for (RPGClass rc: rp.classes)
				lines.add(" " + rc.classType.toString() + ", " + rc.xp); 
			lines.add("sideclasses:");
			for (RPGSideClass rc: rp.sideClasses)
				lines.add(" " + rc.sideClassType.toString() + ", " + rc.xp); 
			lines.add("skills:");
			for (int i = 0; i < rp.skills.size(); i++)
				lines.add(" " + rp.skills.get(i));
			lines.add("recipes:");
			for (int i = 0; i < rp.recipes.size(); i++)
				lines.add(" " + rp.recipes.get(i));
			lines.add("npcflags:");
			ArrayList<String> keys = new ArrayList<String>();
			keys.addAll(rp.npcFlags.keySet());
			ArrayList<String> values = new ArrayList<String>();
			values.addAll(rp.npcFlags.values());
			for (int i = 0; i < keys.size(); i++)
				lines.add(" " + keys.get(i) + ", " + values.get(i));
			if (rp.leftForArenaLocation != null)
				lines.add("leftForArenaLocation: " + rp.leftForArenaLocation.getWorld().getName()
						+ ", " + rp.leftForArenaLocation.getBlockX() 
						+ ", " + rp.leftForArenaLocation.getBlockY() 
						+ ", " + rp.leftForArenaLocation.getBlockZ());

			CakeLibrary.writeFile(lines, file);

		} catch (Exception e) {
			RPGCore.msgConsole("&4Error writing RPlayer file: " + file.getName());
			e.printStackTrace();
		}
		try
		{
			for (int i = 0; i < 3; i++)
			{
				File riFile = new File(accessoriesFolder.getPath() + "/" + i + "_" + rp.getUniqueID() + ".yml");
				if (riFile.exists())
					riFile.delete();
			}
			for (int i = 0; i < rp.accessoryInventory.slots.length; i++)
			{
				RItem ri = rp.accessoryInventory.slots[i];
				if (ri == null)
					continue;
				File riFile = new File(accessoriesFolder.getPath() + "/" + i + "_" + rp.getUniqueID() + ".yml");
				ri.saveToFile(riFile);
			}
		} catch (Exception e)
		{
			RPGCore.msgConsole("&4Error writing RPlayer accessories: " + file.getName());
			e.printStackTrace();
		}
		try
		{
			File mailbox = new File(mailboxFolder.getPath() + "/" + rp.getUniqueID());
			if (mailbox.exists())
				for (File m: mailbox.listFiles())
					m.delete();
			mailbox.mkdirs();
			for (int i = 0; i < rp.mailbox.items.size(); i++)
			{
				RItem ri = rp.mailbox.items.get(i);
				if (ri == null)
					continue;
				File mailItem = new File(mailboxFolder.getPath() + "/" + rp.getUniqueID() + "/" + i + ".yml");
				ri.saveToFile(mailItem);
			}
		} catch (Exception e)
		{
			RPGCore.msgConsole("&4Error writing RPlayer mailbox: " + file.getName());
			e.printStackTrace();
		}
	}

	public void writeData()
	{
		for (RPlayer rp: players)
			writeData(rp);
	}
}
