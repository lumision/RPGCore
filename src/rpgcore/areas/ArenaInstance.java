package rpgcore.areas;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Monster;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldedit.world.DataException;

import rpgcore.entities.mobs.RPGMonster;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGCore;

public class ArenaInstance 
{
	static BukkitWorld arenaInstanceWorld;
	public static ArrayList<ArenaInstance> arenaInstanceList = new ArrayList<ArenaInstance>();
	public static File arenasFile = new File("plugins/RPGCore/ArenaInstances.yml");

	public Arena arena;
	public boolean occupied;
	public boolean pasted;
	public int arenaInstanceID;
	public ArrayList<Monster> mobList = new ArrayList<Monster>();

	public ArenaInstance(Arena arena, int arenaInstanceID, boolean pasted, boolean occupied)
	{
		for (ArenaInstance ai: arenaInstanceList)
			if (ai.arenaInstanceID == arenaInstanceID)
				return;
		this.arena = arena;
		this.arenaInstanceID = arenaInstanceID;
		this.pasted = pasted;
		this.occupied = occupied;
		if (!pasted)
		{
			try {
				File file = new File("plugins/WorldEdit/schematics/" + arena.schematicName + ".schematic");
				EditSession es = WorldEdit.getInstance().getEditSessionFactory().getEditSession(getArenaInstanceWorld()
						, WorldEdit.getInstance().getConfiguration().maxChangeLimit);
				CuboidClipboard clip = SchematicFormat.MCEDIT.load(file);
				try {
					clip.paste(es, new Vector(arenaInstanceID * 256, 64, 0), true);
				} catch (MaxChangedBlocksException e) {
					e.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (DataException e) {
				e.printStackTrace();
			}
		}
		arenaInstanceList.add(this);
		writeArenaInstanceData();
	}

	public void spawnMobs()
	{
		for (String mob: arena.mobSpawns.keySet())
		{
			try
			{
				org.bukkit.util.Vector v = arena.mobSpawns.get(mob);
				mobList.add(RPGMonster.spawnMob(mob, getSpawnLocation().add(v)));
			} catch (Exception e) {
				Bukkit.broadcastMessage("Error spawning arena mob - \"" + mob + "\" (maybe it does not exist?)");
			}
		}
	}

	public Location getSpawnLocation()
	{
		Location l = new Location(Bukkit.getWorld(RPGCore.areaInstanceWorld), (arenaInstanceID * 256) + 0.5F, 64, 0.5F);
		l.setYaw(arena.yaw);
		l.setPitch(arena.pitch);
		return l;
	}

	public static ArenaInstance getArenaInstance(Arena arena)
	{
		for (ArenaInstance instance: arenaInstanceList)
		{
			if (instance.arena.equals(arena) && !instance.occupied)
				return instance;
		}
		return new ArenaInstance(arena, arenaInstanceList.size(), false, false);
	}

	public static BukkitWorld getArenaInstanceWorld()
	{
		if (arenaInstanceWorld != null)
			return arenaInstanceWorld;
		return arenaInstanceWorld = new BukkitWorld(Bukkit.getWorld(RPGCore.areaInstanceWorld));
	}

	public static void readArenaInstanceData()
	{
		ArrayList<String> lines = CakeLibrary.readFile(arenasFile);
		for (String line: lines)
		{
			String[] split = line.split(", ");
			if (split.length < 3)
				continue;
			new ArenaInstance(Arena.getArena(split[0]), Integer.valueOf(split[1]), true, Boolean.valueOf(split[2]));
		}
	}

	public static void writeArenaInstanceData()
	{
		ArrayList<String> lines = new ArrayList<String>();
		for (ArenaInstance ai: arenaInstanceList)
			if (ai != null)
				lines.add(ai.arena.schematicName + ", " + ai.arenaInstanceID + ", " + ai.occupied);
		CakeLibrary.writeFile(lines, arenasFile);
	}
}
