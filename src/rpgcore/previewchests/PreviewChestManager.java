package rpgcore.previewchests;

import java.io.File;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGCore;

public class PreviewChestManager 
{
	public RPGCore instance;
	public static final File chestsFile = new File("plugins/RPGCore/PreviewChests.yml");
	
	public ArrayList<Location> previewChests = new ArrayList<Location>();
	public PreviewChestManager(RPGCore instance)
	{
		this.instance = instance;
		readData();
	}
	
	public Location getPreviewChest(Location l)
	{
		for (Location check: previewChests)
			if (check.getWorld() == l.getWorld()
			&& check.getBlockX() == l.getBlockX()
			&& check.getBlockY() == l.getBlockY()
			&& check.getBlockZ() == l.getBlockZ())
				return check;
		return null;
	}
	
	public void readData()
	{
		previewChests.clear();
		
		ArrayList<String> lines = CakeLibrary.readFile(chestsFile);
		for (String line: lines)
		{
			try
			{
				String[] split = line.split(", ");
				Location l = new Location(Bukkit.getWorld(split[0]), 
						Integer.valueOf(split[1]), Integer.valueOf(split[2]), Integer.valueOf(split[3]));
				previewChests.add(l);
			} catch (Exception e) {}
		}
	}
	
	public void writeData()
	{
		ArrayList<String> lines = new ArrayList<String>();
		for (Location l: previewChests)
			lines.add(l.getWorld().getName() + ", " + l.getBlockX() + ", " + l.getBlockY() + ", " + l.getBlockZ());
		CakeLibrary.writeFile(lines, chestsFile);
	}
}
