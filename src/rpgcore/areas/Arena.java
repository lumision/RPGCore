package rpgcore.areas;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGCore;

public class Arena 
{
	public static ArrayList<Arena> arenaList = new ArrayList<Arena>();
	public static File arenasFolder = new File("plugins/RPGCore/Arenas");

	public String schematicName;
	public float yaw, pitch;
	public HashMap<String, Vector> mobSpawns = new HashMap<String, Vector>();
	public Location entrance, exitExternal;
	public Vector exitInternal;

	public Arena(String schematicName)
	{
		for (Arena a: arenaList)
			if (a.schematicName.equalsIgnoreCase(schematicName))
				return;
		this.schematicName = schematicName;
		arenaList.add(this);
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
		arenasFolder.mkdirs();
		for (File file: arenasFolder.listFiles())
		{
			if (!file.getName().endsWith(".yml"))
				continue;
			try
			{
				ArrayList<String> lines = CakeLibrary.readFile(file);
				
				String schematicName = "";
				float yaw = 0;
				float pitch = 0;
				Location entrance = null;
				Location exitExternal = null;
				Vector exitInternal = null;
				HashMap<String, Vector> mobSpawns = new HashMap<String, Vector>();
				
				for (String line: lines)
				{
					if (line.startsWith(" "))
					{
						String[] args = line.substring(1).split(", ");
						mobSpawns.put(args[0], new Vector(Float.valueOf(args[1]), Float.valueOf(args[2]), Float.valueOf(args[3])));
						continue;
					}
					String[] split = line.split(": ");
					if (split[0].equals("schematicName"))
						schematicName = split[1];
					else if (split[0].equals("spawnYaw"))
						yaw = Float.valueOf(split[1]);
					else if (split[0].equals("spawnPitch"))
						pitch = Float.valueOf(split[1]);
					else if (split[0].equals("entrance"))
					{
						String[] args = split[1].split(", ");
						entrance = new Location(Bukkit.getWorld(args[0]), Double.valueOf(args[1]), Double.valueOf(args[2]), Double.valueOf(args[3]));
					}
					else if (split[0].equals("exitExternal"))
					{
						String[] args = split[1].split(", ");
						exitExternal = new Location(Bukkit.getWorld(args[0]), Double.valueOf(args[1]), Double.valueOf(args[2]), Double.valueOf(args[3]));
						exitExternal.setYaw(Float.valueOf(args[4]));
						exitExternal.setPitch(Float.valueOf(args[5]));
					}
					else if (split[0].equals("exitInternal"))
					{
						String[] args = split[1].split(", ");
						exitInternal = new Vector(Float.valueOf(args[0]), Float.valueOf(args[1]), Float.valueOf(args[2]));
					}
				}
				
				if (schematicName.length() == 0)
					continue;
				Arena a = new Arena(schematicName);
				a.yaw = yaw;
				a.pitch = pitch;
				a.entrance = entrance;
				a.exitExternal = exitExternal;
				a.exitInternal = exitInternal;
				a.mobSpawns = mobSpawns;
			} catch (Exception e) 
			{
				RPGCore.msgConsole("Error reading arena file: " + file.getName());
			}
		}
	}

	public static void writeArenaData()
	{
		arenasFolder.mkdirs();
		for (File file: arenasFolder.listFiles())
			file.delete();
		for (Arena a: arenaList)
		{
			if (a == null)
				continue;
			File file = new File("plugins/RPGCore/Arenas/" + a.schematicName + ".yml");
			ArrayList<String> lines = new ArrayList<String>();
			lines.add("schematicName: " + a.schematicName);
			lines.add("spawnYaw: " + a.yaw);
			lines.add("spawnPitch: " + a.pitch);
			if (a.entrance != null)
				lines.add("entrance: " + a.entrance.getWorld().getName() + ", " + a.entrance.getX() + ", " + a.entrance.getY() + ", " + a.entrance.getZ());
			if (a.exitExternal != null)
				lines.add("exitExternal: " + a.exitExternal.getWorld().getName() + ", " + a.exitExternal.getX() + ", " + a.exitExternal.getY() + ", " + a.exitExternal.getZ()
				+ ", " + a.exitExternal.getYaw() + ", " + a.exitExternal.getPitch());
			if (a.exitInternal != null)
				lines.add("exitInternal: " + a.exitInternal.getX() + ", " + a.exitInternal.getY() + ", " + a.exitInternal.getZ());
			//QueenSpider, 90, 0::QueenSpider, 16, 2, 16
			if (a.mobSpawns.size() > 0)
			{
				lines.add("mobSpawns:");
				for (String mob: a.mobSpawns.keySet())
				{
					Vector v = a.mobSpawns.get(mob);
					lines.add(" " + mob + ", " + (v.getBlockX() + 0.5F) + ", " + (v.getBlockY() + 0.5F) + ", " + (v.getBlockZ() + 0.5F));
				}
			}
			CakeLibrary.writeFile(lines, file);
		}
	}
}
