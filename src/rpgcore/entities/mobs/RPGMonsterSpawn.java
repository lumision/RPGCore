package rpgcore.entities.mobs;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import rpgcore.entities.bosses.Astrea;
import rpgcore.entities.bosses.KingZombie;
import rpgcore.entities.bosses.QueenSpider;
import rpgcore.entities.bosses.UndeadEmperor;
import rpgcore.item.RItem;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGCore;

public class RPGMonsterSpawn
{
	public static ArrayList<RPGMonsterSpawn> spawns = new ArrayList<RPGMonsterSpawn>();
	
	public static final File dropsFolder = new File("plugins/RPGCore/mob-drops");

	public String rpgMonsterName;
	public Class<? extends Monster> monsterType;
	public Class<? extends RPGMonster> rpgMonster;
	public HashMap<RItem, Integer> drops = new HashMap<RItem, Integer>();
	public int minSpawnDistance, maxSpawnDistance;
	public int spawnRoll;
	Inventory dropsInventory;
	
	public static final ItemStack itemDrop = CakeLibrary.editNameAndLore(new ItemStack(Material.PAPER, 1, (short) 0),
			"§c§nDrop Rate",
			"§7The drop rate is determined",
			"§7with a (1 in §lx§7) chance; §lx",
			"§7being the item stack amount.");

	
	//ADDMOB
	public static void onEnable()
	{
		dropsFolder.mkdirs();
		
		spawns.clear();
		spawns.add(new RPGMonsterSpawn(ReinforcedSkeleton.class, Skeleton.class, 300, 1000, 4));
		spawns.add(new RPGMonsterSpawn(ReinforcedSpider.class, Spider.class, 300, 1000, 4));
		spawns.add(new RPGMonsterSpawn(ReinforcedZombie.class, Zombie.class, 300, 1000, 4));
		
		spawns.add(new RPGMonsterSpawn(AssassinSpider.class, Spider.class, 800, 2000, 4));
		spawns.add(new RPGMonsterSpawn(MageZombie.class, Zombie.class, 800, 2000, 4));
		spawns.add(new RPGMonsterSpawn(WarriorZombie.class, Zombie.class, 800, 2000, 4));
		
		spawns.add(new RPGMonsterSpawn(SorcererZombie.class, Zombie.class, 1500, 3000, 4));
		
		spawns.add(new RPGMonsterSpawn(Astrea.class, Zombie.class));
		spawns.add(new RPGMonsterSpawn(KingZombie.class, Zombie.class));
		spawns.add(new RPGMonsterSpawn(QueenSpider.class, Spider.class));
		spawns.add(new RPGMonsterSpawn(UndeadEmperor.class, Zombie.class));
		
		readDrops();
	}
	
	public static void readDrops()
	{
		for (File file: dropsFolder.listFiles())
		{
			try
			{
				if (!file.getName().endsWith(".yml"))
					continue;
				String[] split = file.getName().substring(0, file.getName().length() - 4).split("_");
				
				RPGMonsterSpawn spawn = getRPGMonsterSpawn(split[0]);
				if (spawn == null)
				{
					RPGCore.msgConsole("&4Error reading drops file: " + file.getName() + "; spawn \"" + split[1] + "\" does not exist.");
					continue;
				}
				
				RItem ri = RItem.readRItemFile(file);
				if (ri == null)
				{
					RPGCore.msgConsole("&4Error reading drops file: " + file.getName() + "; RItem parsing");
					continue;
				}
				
				spawn.drops.put(ri, Integer.parseInt(split[2]));
			} catch (Exception e) 
			{
				RPGCore.msgConsole("&4Error reading drops file: " + file.getName());
				e.printStackTrace();
			}
		}
	}

	public RPGMonsterSpawn(Class<? extends RPGMonster> rpgMonster, Class<? extends Monster> monsterType)
	{
		this.monsterType = monsterType;
		this.rpgMonster = rpgMonster;
		this.rpgMonsterName = rpgMonster.getSimpleName();

		spawns.add(this);
	}

	public RPGMonsterSpawn(Class<? extends RPGMonster> rpgMonster, Class<? extends Monster> monsterType, int minSpawnDistance, int maxSpawnDistance, int spawnRoll)
	{
		this.monsterType = monsterType;
		this.rpgMonster = rpgMonster;
		this.rpgMonsterName = rpgMonster.getSimpleName();
		this.minSpawnDistance = minSpawnDistance;
		this.maxSpawnDistance = maxSpawnDistance;
		this.spawnRoll = spawnRoll;

		spawns.add(this);
	}
	
	public static RPGMonsterSpawn getRPGMonsterSpawn(String mobName)
	{
		for (RPGMonsterSpawn spawn: spawns)
			if (spawn.rpgMonsterName.equalsIgnoreCase(mobName))
				return spawn;
		return null;
	}
	
	public void saveDrops()
	{
		for (File file: dropsFolder.listFiles())
		{
			if (!file.getName().endsWith(".yml"))
				continue;
			if (file.getName().startsWith(rpgMonsterName + "_"))
				file.delete();
		}
		int index = 0;
		for (RItem key: drops.keySet())
		{
			try
			{
				key.saveItemToFile(new File(dropsFolder.getPath() + "/" + rpgMonsterName + "_" + index++ + "_" + drops.get(key) + ".yml"));
			} catch (Exception e)
			{
				RPGCore.msgConsole("&4Error writing drops file: " + rpgMonsterName + " / " + CakeLibrary.getItemName(key.itemVanilla));
				e.printStackTrace();
			}
		}
	}
	
	public Inventory getDropsInventory()
	{
		if (dropsInventory != null)
			return dropsInventory;
		dropsInventory = Bukkit.createInventory(null, 9, CakeLibrary.recodeColorCodes("&4Drops - " + rpgMonsterName));
		for (RItem key: drops.keySet())
		{
			ItemStack item = key.createItem();
			item.setAmount(drops.get(key));
			dropsInventory.addItem(item);
		}
		dropsInventory.setItem(8, itemDrop);
		return dropsInventory;
	}
	
	public boolean isNaturalSpawn()
	{
		return this.minSpawnDistance > 0 && this.maxSpawnDistance > 0 && spawnRoll > 0;
	}

	public RPGMonster spawnMonster(Location location)
	{
		Monster m = location.getWorld().spawn(location, monsterType);
		try {
			return rpgMonster.getConstructor(Monster.class).newInstance(m);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public RPGMonster replaceMonster(LivingEntity m)
	{
		try {
			return rpgMonster.getConstructor(Monster.class).newInstance(m);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
