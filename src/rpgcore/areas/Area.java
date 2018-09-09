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
	
	public boolean editable;
	
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
	
	public boolean isInArea(Location location)
	{
		return location.getX() >= minX && location.getX() <= maxX && location.getZ() >= minZ && location.getZ() <= maxZ;
	}
	
	public static void tick()
	{
		/*
		for (Player p: Bukkit.getOnlinePlayers())
		{
			Location l = p.getLocation();
			for (Area a: areas)
			{
				if (!a.isInArea(l))
					continue;
				//Player is in Area 'a'
				
				if (a.bgm != null)
				{
					RPGSong r = RPGCore.songManager.getSong(a.bgm);
				}
				break;
			}
		}
		*/
	}
}