package rpgcore.player;

import java.io.File;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import rpgcore.classes.RPGClass;
import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeAPI;
import rpgcore.main.RPGCore;

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
			RPlayer rp = getRPlayer(p.getName());
			if (rp == null)
				rp = addRPlayer(p.getName());
			rp.updatePlayerREquips();
		}
	}

	public RPlayer addRPlayer(String playerName)
	{
		RPlayer rp = new RPlayer(playerName);
		players.add(rp);
		writePlayerData(rp);
		return rp;
	}

	public RPlayer addRPlayer(String playerName, ArrayList<RPGClass> classes, ClassType currentClass, ArrayList<String> skills, ArrayList<Integer> skillLevels)
	{
		RPlayer rp = new RPlayer(playerName, classes, currentClass, skills, skillLevels);
		players.add(rp);
		writePlayerData(rp);
		return rp;
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
			String playerName = file.getName().substring(0, file.getName().length() - 4);
			ClassType currentClass = ClassType.MAGE;
			ArrayList<RPGClass> classes = new ArrayList<RPGClass>();
			ArrayList<String> skills = new ArrayList<String>();
			ArrayList<Integer> skillLevels = new ArrayList<Integer>();

			ArrayList<String> lines = CakeAPI.readFile(file);
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
					header = s.substring(0, s.length() - 1);
					continue;
				}
				s = s.substring(1);
				if (header.equals("classes"))
				{
					String[] split = s.split("::");
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
				} else if (header.equals("skills"))
				{
					String[] split = s.split("::");
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
				}
			}
			addRPlayer(playerName, classes, currentClass, skills, skillLevels);
		}
	}

	public void writePlayerData(RPlayer rp)
	{
		File file = new File("plugins/RPGCore/players/" + rp.getPlayerName() + ".yml");
		ArrayList<String> lines = new ArrayList<String>();
		lines.add("class: " + rp.currentClass.toString());
		lines.add("classes:");
		for (RPGClass rc: rp.classes)
			lines.add(" " + rc.classType.toString() + "::" + rc.xp + "::" + rc.skillPoints); 
		lines.add("skills:");
		for (int i = 0; i < rp.skills.size(); i++)
			lines.add(" " + rp.skills.get(i) + "::" + rp.skillLevels.get(i));
		CakeAPI.writeFile(lines, file);
	}

	public void writePlayerData()
	{
		for (RPlayer rp: players)
			writePlayerData(rp);
	}
}
