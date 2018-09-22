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

import rpgcore.entities.mobs.RPGMonsterSpawn;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGCore;

public class ArenaInstance 
{
	static BukkitWorld arenaInstanceWorld;
	public static ArrayList<ArenaInstance> arenaInstanceList = new ArrayList<ArenaInstance>();
	public static final File arenasFile = new File("plugins/RPGCore/ArenaInstances.yml");
	public static int nextInstanceID = 0;

	public Arena arena;
	public boolean occupied;
	public boolean pasted;
	public boolean mobsSpawned;
	public int arenaInstanceID;
	public ArrayList<Monster> mobList = new ArrayList<Monster>();
	
	Location spawnLocation;
	Location[] exitLocations;

	public ArenaInstance(Arena arena, int arenaInstanceID, boolean pasted, boolean occupied)
	{
		for (ArenaInstance ai: arenaInstanceList)
			if (ai.arenaInstanceID == arenaInstanceID)
				return;
		if (nextInstanceID <= arenaInstanceID)
			nextInstanceID = arenaInstanceID + 1;
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
	
	public static ArenaInstance getArenaInstance(int arenaInstanceID)
	{
		for (ArenaInstance ai: arenaInstanceList)
			if (ai.arenaInstanceID == arenaInstanceID)
				return ai;
		return null;
	}

	public void spawnMobs()
	{
		mobsSpawned = true;
		for (int i = 0; i < arena.mobSpawns.size(); i++)
		{
			try
			{
				org.bukkit.util.Vector v = arena.mobSpawnOffsets.get(i);
				mobList.add(RPGMonsterSpawn.getRPGMonsterSpawn(arena.mobSpawns.get(i)).spawnMonster(getSpawnLocation().clone().add(v)).entity);
			} catch (Exception e) {
				Bukkit.broadcastMessage("Error spawning arena mob - \"" + arena.mobSpawns.get(i) + "\" (maybe it does not exist?)");
			}
		}
	}

	public Location getSpawnLocation()
	{
		if (spawnLocation != null)
			return spawnLocation.clone();
		spawnLocation = new Location(Bukkit.getWorld(RPGCore.areaInstanceWorld), (arenaInstanceID * 256) + 0.5F, 64, 0.5F);
		spawnLocation.setYaw(arena.yaw);
		spawnLocation.setPitch(arena.pitch);
		return spawnLocation.clone();
	}
	
	public Location[] getExitLocations()
	{
		if (exitLocations != null)
			return exitLocations.clone();
		exitLocations = new Location[arena.exitInternals.size()];
		for (int i = 0; i < exitLocations.length; i++)
			exitLocations[i] = new Location(
					Bukkit.getWorld(RPGCore.areaInstanceWorld), arenaInstanceID * 256, 64, 0).add(arena.exitInternals.get(i));
		return exitLocations;
	}

	public static ArenaInstance getArenaInstance(Arena arena)
	{
		for (ArenaInstance instance: arenaInstanceList)
		{
			if (instance.arena.equals(arena) && !instance.occupied)
				return instance;
		}
		return createArenaInstance(arena);
	}

	public static ArenaInstance createArenaInstance(Arena arena)
	{
		return new ArenaInstance(arena, nextInstanceID++, false, false);
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
			try
			{
				if (line.startsWith("nextInstanceID: "))
				{
					nextInstanceID = Integer.parseInt(line.split(": ")[1]);
					continue;
				}
				String[] split = line.split(", ");
				if (split.length < 3)
					continue;
				Arena a = Arena.getArena(split[0]);
				if (a == null)
					continue;
				new ArenaInstance(Arena.getArena(split[0]), Integer.valueOf(split[1]), true, Boolean.valueOf(split[2]));
			} catch (Exception e)
			{
				RPGCore.msgConsole("Error reading arena instance data line: &4" + line);
			}
		}
	}

	public static void writeArenaInstanceData()
	{
		ArrayList<String> lines = new ArrayList<String>();
		lines.add("nextInstanceID: " + nextInstanceID);
		for (ArenaInstance ai: arenaInstanceList)
			if (ai != null)
				lines.add(ai.arena.schematicName + ", " + ai.arenaInstanceID + ", " + ai.occupied);
		CakeLibrary.writeFile(lines, arenasFile);
	}
}
