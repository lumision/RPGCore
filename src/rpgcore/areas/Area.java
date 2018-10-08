package rpgcore.areas;

import java.io.File;
import java.util.ArrayList;

import org.bukkit.Location;

import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGCore;

public class Area 
{
	public static ArrayList<Area> areas = new ArrayList<Area>();
	public static final File areasFile = new File("plugins/RPGCore/areas.yml");
	
	public boolean editable;
	
	public String name;
	public String world;
	public int minX;
	public int maxX;
	public int minZ;
	public int maxZ;
	public String bgm;
	public Area(String name, String world, int minX, int maxX, int minZ, int maxZ)
	{
		this.name = name;
		this.world = world;
		this.minX = minX;
		this.maxX = maxX;
		this.minZ = minZ;
		this.maxZ = maxZ;
		this.editable = true;
	}
	
	public boolean isInArea(Location location)
	{
		if (!location.getWorld().getName().equalsIgnoreCase(world))
			return false;
		return location.getX() >= minX && location.getX() <= maxX && location.getZ() >= minZ && location.getZ() <= maxZ;
	}

	public static void readData()
	{
		ArrayList<String> lines = CakeLibrary.readFile(areasFile);
		for (String line: lines)
		{
			try
			{
				String[] split = line.split(", ");
				if (split.length < 5)
					continue;
				Area a;
				areas.add(a = new Area(split[0], split[1], Integer.valueOf(split[2]), Integer.valueOf(split[3]), Integer.valueOf(split[4]), Integer.valueOf(split[5])));
				if (split.length > 6)
					a.editable = Boolean.valueOf(split[6]);
				if (split.length > 7)
					a.bgm = split[7];
			} catch (Exception e)
			{
				RPGCore.msgConsole("Error reading area data line: &4" + line);
			}
		}
	}

	public static void writeData()
	{
		ArrayList<String> lines = new ArrayList<String>();
		for (Area a: areas)
				lines.add(a.name + ", " + a.world + ", " + a.minX + ", " + a.maxX + ", " + a.minZ + ", " + a.maxZ + ", " + a.editable + (a.bgm != null ? ", " + a.bgm : ""));
		CakeLibrary.writeFile(lines, areasFile);
	}
}