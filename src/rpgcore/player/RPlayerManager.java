package rpgcore.player;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import rpgcore.classes.RPGClass;
import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGCore;
import rpgcore.sideclasses.RPGSideClass;
import rpgcore.sideclasses.RPGSideClass.SideClassType;

public class RPlayerManager 
{
	public RPGCore instance;
	private ArrayList<RPlayer> players = new ArrayList<RPlayer>();
	public File playersFolder = new File("plugins/RPGCore/players");
	public RPlayerManager(RPGCore instance)
	{
		this.instance = instance;
		playersFolder.mkdirs();
		readPlayerData();
		for (Player p: Bukkit.getOnlinePlayers())
		{
			RPlayer rp = getRPlayer(p.getUniqueId());
			if (rp == null)
				rp = addRPlayer(p.getUniqueId());
			rp.updatePlayerREquips();
			p.setScoreboard(rp.scoreboard);
		}
	}

	public RPlayer addRPlayer(UUID uuid)
	{
		RPlayer rp = new RPlayer(uuid);
		players.add(rp);
		writePlayerData(rp);
		return rp;
	}

	public RPlayer addRPlayer(UUID uuid, ArrayList<RPGClass> classes, ArrayList<RPGSideClass> sideClasses, ClassType currentClass, ArrayList<String> skills, 
			ArrayList<Integer> skillLevels, int gold, int tokens, Map<String, String> npcFlags, boolean tutorialCompleted)
	{
		RPlayer rp = new RPlayer(uuid, classes, sideClasses, currentClass, skills, skillLevels, gold, tokens);
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
		for (RPlayer rp: players)
			rp.tick();
	}

	public void playersTick10()
	{
		for (RPlayer rp: players)
			rp.tick10();
	}

	public void playersTick20()
	{
		for (RPlayer rp: players)
			if (rp.getPlayer() != null)
				rp.updatePlayerREquips();
	}

	public int getRPlayerAmount()
	{
		return players.size();
	}

	public void readPlayerData()
	{
		players.clear();
		for (File file: playersFolder.listFiles())
		{
			try
			{
				UUID uuid = UUID.fromString(file.getName().substring(0, file.getName().length() - 4));
				ClassType currentClass = ClassType.MAGE;
				ArrayList<RPGClass> classes = new ArrayList<RPGClass>();
				ArrayList<RPGSideClass> sideClasses = new ArrayList<RPGSideClass>();
				ArrayList<String> skills = new ArrayList<String>();
				ArrayList<Integer> skillLevels = new ArrayList<Integer>();
				Map<String, String> npcFlags = new HashMap<String, String>();
				boolean tutorialCompleted = false;
				int gold = 0;
				int tokens = 0;

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
						if (s.startsWith("lastRecordedName: "))
							continue;
						header = s.substring(0, s.length() - 1);
						continue;
					}
					s = s.substring(1);
					if (header.equals("classes"))
					{
						String[] split = s.split(", ");
						if (split.length < 3)
							continue;
						ClassType classType = null;
						int xp = 0;
						int skillPoints = 0;
						try
						{
							classType = ClassType.valueOf(split[0]);
							xp = Integer.parseInt(split[1]);
							skillPoints = Integer.parseInt(split[2]);
						} catch (Exception e) {
							continue;
						}
						classes.add(new RPGClass(classType, xp, skillPoints));
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
						String[] split = s.split(", ");
						if (split.length < 2)
							continue;
						int lv = 0;
						try
						{
							lv = Integer.parseInt(split[1]);
						} catch (Exception e) {
							continue;
						}
						skills.add(split[0]);
						skillLevels.add(lv);
					} else if (header.equals("npcflags"))
					{
						String[] split = s.split(", ");
						if (split.length < 2)
							continue;
						npcFlags.put(split[0], split[1]);
					}
				}
				
				for (ClassType classType: ClassType.values())
				{
					boolean contains = false;
					for (RPGClass c: classes)
						if (c.classType.equals(classType))
							contains = true;
					if (!contains)
						classes.add(new RPGClass(classType, 0, 3));
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
				
				addRPlayer(uuid, classes, sideClasses, currentClass, skills, skillLevels, gold, tokens, npcFlags, tutorialCompleted);
			} catch (Exception e) {
				RPGCore.msgConsole("&4Error reading RPlayer file: " + file.getName());
			}
		}
	}

	public void writePlayerData(RPlayer rp)
	{
		File file = new File("plugins/RPGCore/players/" + rp.getUniqueID() + ".yml");
		try
		{
			ArrayList<String> lines = new ArrayList<String>();
			lines.add("lastRecordedName: " + rp.getPlayerName());
			lines.add("class: " + rp.currentClass.toString());
			lines.add("gold: " + rp.getGold());
			lines.add("tutorialCompleted: " + rp.tutorialCompleted);
			lines.add("classes:");
			for (RPGClass rc: rp.classes)
				lines.add(" " + rc.classType.toString() + ", " + rc.xp + ", " + rc.skillPoints); 
			lines.add("sideclasses:");
			for (RPGSideClass rc: rp.sideClasses)
				lines.add(" " + rc.sideClassType.toString() + ", " + rc.xp); 
			lines.add("skills:");
			for (int i = 0; i < rp.skills.size(); i++)
				lines.add(" " + rp.skills.get(i) + ", " + rp.skillLevels.get(i));
			lines.add("npcflags:");
			ArrayList<String> keys = new ArrayList<String>();
			keys.addAll(rp.npcFlags.keySet());
			ArrayList<String> values = new ArrayList<String>();
			values.addAll(rp.npcFlags.values());
			for (int i = 0; i < keys.size(); i++)
				lines.add(" " + keys.get(i) + ", " + values.get(i));
			CakeLibrary.writeFile(lines, file);
		} catch (Exception e) {
			RPGCore.msgConsole("&4Error reading RPlayer file: " + file.getName());
			e.printStackTrace();
		}
	}

	public void writePlayerData()
	{
		for (RPlayer rp: players)
			writePlayerData(rp);
	}
}
