package rpgcore.areas;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import rpgcore.main.RPGCore;
import rpgcore.songs.RPGSong;

public class Area 
{
	public static ArrayList<Area> areas = new ArrayList<Area>();
	public String name;
	public int minX;
	public int maxX;
	public int minZ;
	public int maxZ;
	public String bgm;
	public Area(String name, int minX, int maxX, int minZ, int maxZ)
	{
		this.name = name;
		this.minX = minX;
		this.maxX = maxX;
		this.minZ = minZ;
		this.maxZ = maxZ;
		areas.add(this);
	}
	
	public static void tick()
	{
		for (Player p: Bukkit.getOnlinePlayers())
		{
			Location l = p.getLocation();
			boolean cont = false;
			for (Area a: areas)
			{
				if (l.getX() < a.minX)
					continue;
				if (l.getX() > a.maxX)
					continue;
				if (l.getZ() < a.minZ)
					continue;
				if (l.getZ() > a.maxZ)
					continue;
				//Player is in Area 'a'
				
				if (a.bgm != null)
				{
					RPGSong r = RPGCore.songManager.getSong(a.bgm);
				}
			}
		}
	}
}
