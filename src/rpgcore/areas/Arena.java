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
	public static final File arenasFolder = new File("plugins/RPGCore/Arenas");

	public String schematicName;
	public float yaw, pitch;
	public ArrayList<String> mobSpawns = new ArrayList<String>();
	public ArrayList<Vector> mobSpawnOffsets = new ArrayList<Vector>();
	public ArrayList<Location> entrances = new ArrayList<Location>();
	public ArrayList<Vector> exitInternals = new ArrayList<Vector>();
	public Location exitExternal;
	public boolean enabled;

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
				String header = "";

				String schematicName = "";
				boolean enabled = false;
				float yaw = 0;
				float pitch = 0;
				Location exitExternal = null;
				ArrayList<Location> entrances = new ArrayList<Location>();
				ArrayList<Vector> exitInternals = new ArrayList<Vector>();
				ArrayList<String> mobSpawns = new ArrayList<String>();
				ArrayList<Vector> mobSpawnOffsets = new ArrayList<Vector>();

				for (String line: lines)
				{
					if (line.startsWith(" "))
					{
						if (header.equals("mobSpawns:"))
						{
							String[] args = line.substring(1).split(", ");
							mobSpawns.add(args[0]);
							mobSpawnOffsets.add(new Vector(Float.valueOf(args[1]), Float.valueOf(args[2]), Float.valueOf(args[3])));
						} else if (header.equals("entrances:"))
						{
							String[] args = line.substring(1).split(", ");
							entrances.add(
									new Location(Bukkit.getWorld(args[0]), 
											Double.valueOf(args[1]), Double.valueOf(args[2]), Double.valueOf(args[3])));
						} else if (header.equals("exitInternals:"))
						{
							String[] args = line.substring(1).split(", ");
							exitInternals.add(
									new Vector(Float.valueOf(args[0]), Float.valueOf(args[1]), Float.valueOf(args[2])));
						}
						continue;
					}
					header = line;
					String[] split = line.split(": ");
					if (split[0].equals("schematicName"))
						schematicName = split[1];
					else if (split[0].equals("enabled"))
						enabled = Boolean.valueOf(split[1]);
					else if (split[0].equals("spawnYaw"))
						yaw = Float.valueOf(split[1]);
					else if (split[0].equals("spawnPitch"))
						pitch = Float.valueOf(split[1]);
					else if (split[0].equals("exitExternal"))
					{
						String[] args = split[1].split(", ");
						exitExternal = new Location(Bukkit.getWorld(args[0]), Double.valueOf(args[1]), Double.valueOf(args[2]), Double.valueOf(args[3]));
						exitExternal.setYaw(Float.valueOf(args[4]));
						exitExternal.setPitch(Float.valueOf(args[5]));
					}
				}

				if (schematicName.length() == 0)
					continue;
				Arena a = new Arena(schematicName);
				a.enabled = enabled;
				a.yaw = yaw;
				a.pitch = pitch;
				a.entrances = entrances;
				a.exitExternal = exitExternal;
				a.exitInternals = exitInternals;
				a.mobSpawns = mobSpawns;
				a.mobSpawnOffsets = mobSpawnOffsets;
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
			File file = new File(arenasFolder.getPath() + "/" + a.schematicName + ".yml");
			ArrayList<String> lines = new ArrayList<String>();
			lines.add("enabled: " + a.enabled);
			lines.add("schematicName: " + a.schematicName);
			lines.add("spawnYaw: " + a.yaw);
			lines.add("spawnPitch: " + a.pitch);
			if (a.entrances.size() > 0)
			{
				lines.add("entrances:");
				for (Location entrance: a.entrances)
					lines.add(" " + entrance.getWorld().getName() + ", " + 
							entrance.getX() + ", " + entrance.getY() + ", " + entrance.getZ());
			}
			if (a.exitExternal != null)
				lines.add("exitExternal: " + a.exitExternal.getWorld().getName() + ", " + a.exitExternal.getX() + ", " + a.exitExternal.getY() + ", " + a.exitExternal.getZ()
				+ ", " + a.exitExternal.getYaw() + ", " + a.exitExternal.getPitch());
			if (a.exitInternals.size() > 0)
			{
				lines.add("exitInternals:");
				for (Vector exitInternal: a.exitInternals)
					lines.add(" " + exitInternal.getX() + ", " + exitInternal.getY() + ", " + exitInternal.getZ());
			}
			if (a.mobSpawns.size() > 0)
			{
				lines.add("mobSpawns:");
				for (int i = 0; i < a.mobSpawns.size(); i++)
				{
					Vector v = a.mobSpawnOffsets.get(i);
					lines.add(" " + a.mobSpawns.get(i) + ", " + (v.getBlockX() + 0.5F) + ", " + (v.getBlockY() + 0.5F) + ", " + (v.getBlockZ() + 0.5F));
				}
			}
			CakeLibrary.writeFile(lines, file);
		}
	}
}
