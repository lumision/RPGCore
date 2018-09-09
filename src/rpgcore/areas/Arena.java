package rpgcore.areas;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.util.Vector;

import rpgcore.main.CakeLibrary;

public class Arena 
{
	public static ArrayList<Arena> arenaList = new ArrayList<Arena>();
	public static File arenasFile = new File("plugins/RPGCore/Arenas.yml");

	public String schematicName;
	public float yaw, pitch;
	public HashMap<String, Vector> mobSpawns = new HashMap<String, Vector>();

	public Arena(String schematicName)
	{
		for (Arena a: arenaList)
			if (a.schematicName.equalsIgnoreCase(schematicName))
				return;
		this.schematicName = schematicName;
		arenaList.add(this);
		writeArenaData();
	}

	public static Arena getArena(String schematicName)
	{
		for (Arena a: arenaList)
			if (a.schematicName.equalsIgnoreCase(schematicName))
				return a;
		return null;
	}

	public static void readArenaData()
	{
		ArrayList<String> lines = CakeLibrary.readFile(arenasFile);
		for (String line: lines)
		{
			String[] split = line.split(", ");
			if (split.length == 0)
				continue;
			Arena a = new Arena(split[0]);
			if (split.length > 1)
			{
				a.yaw = Float.parseFloat(split[1]);
				a.pitch = Float.parseFloat(split[2]);
			}
			String[] split1 = line.split("::");
			if (split1.length > 1)
			{
				for (int i = 1; i < split1.length; i++)
				{
					String[] split2 = split1[i].split(", ");
					a.mobSpawns.put(split2[0], new Vector(Double.valueOf(split2[1]), Double.valueOf(split2[2]), Double.valueOf(split2[3])));
				}
			}
		}
	}

	public static void writeArenaData()
	{
		ArrayList<String> lines = new ArrayList<String>();
		for (Arena a: arenaList)
		{
			if (a == null)
				continue;
			String line = a.schematicName + ", " + a.yaw + ", " + a.pitch;
			if (a.mobSpawns.size() > 0)
				for (String mob: a.mobSpawns.keySet())
				{
					Vector v = a.mobSpawns.get(mob);
					line += "::" + mob + ", " + (v.getBlockX() + 0.5F) + ", " + (v.getBlockY() + 0.5F) + ", " + (v.getBlockZ() + 0.5F);
				}
				lines.add(line);
		}
		CakeLibrary.writeFile(lines, arenasFile);
	}
}
