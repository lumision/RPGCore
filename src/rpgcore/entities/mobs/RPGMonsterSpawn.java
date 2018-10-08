package rpgcore.entities.mobs;

import java.io.File;
import java.util.ArrayList;

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

import rpgcore.entities.bosses.CorruptedMage;
import rpgcore.entities.bosses.UndeadEmperor;
import rpgcore.entities.minibosses.DiamondZombie;
import rpgcore.entities.minibosses.QueenSpider;
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
	public ArrayList<RItem> drops = new ArrayList<RItem>();
	public int minSpawnDistanceSquared, maxSpawnDistanceSquared;
	public int spawnRoll;
	Inventory dropsInventory;
	
	public static final ItemStack itemDrop = CakeLibrary.editNameAndLore(new ItemStack(Material.PAPER, 1, (short) 0),
			"§c§nInfo",
			"§7The drop rate is determined",
			"§7with a (1 in §lx§7) chance; §lx",
			"§7being the item stack amount.",
			"&f",
			"&7Click on an item in &oyour",
			"&7&oinventory&7 to &oadd&7 it to the",
			"&7mob's drop list; click on",
			"&7an item in the &odrop list&7 to",
			"&7&osubtract&7 it from there.",
			"&f",
			"&7&lShift-click&7 an item in the",
			"&7drop list to receive its copy.",
			"&f",
			"&7&lShift-click&7 an item in your",
			"&7inventory to add the stack.");

	
	//ADDMOB
	public static void onEnable()
	{
		dropsFolder.mkdirs();
		
		spawns.clear();
		
		//Mobs
		spawns.add(new RPGMonsterSpawn(ReinforcedSkeleton.class, Skeleton.class, 0, 1000, 4));
		spawns.add(new RPGMonsterSpawn(ReinforcedSpider.class, Spider.class, 0, 1000, 4));
		spawns.add(new RPGMonsterSpawn(ReinforcedZombie.class, Zombie.class, 0, 1000, 4));
		
		spawns.add(new RPGMonsterSpawn(AssassinSpider.class, Spider.class, 500, 1500, 4));
		spawns.add(new RPGMonsterSpawn(MageZombie.class, Zombie.class, 500, 1500, 4));
		spawns.add(new RPGMonsterSpawn(WarriorZombie.class, Zombie.class, 500, 1500, 4));

		spawns.add(new RPGMonsterSpawn(SorcererZombie.class, Zombie.class, 1200, 2200, 4));
		spawns.add(new RPGMonsterSpawn(FighterZombie.class, Zombie.class, 1200, 2200, 4));
		spawns.add(new RPGMonsterSpawn(RogueSpider.class, Spider.class, 1200, 2200, 4));

		spawns.add(new RPGMonsterSpawn(TricksterSpider.class, Spider.class, 2000, 3000, 4));
		spawns.add(new RPGMonsterSpawn(SageSkeleton.class, Skeleton.class, 2000, 3000, 4));
		spawns.add(new RPGMonsterSpawn(BrawlerZombie.class, Zombie.class, 2000, 3000, 4));
		

		//Mini-bosses
		spawns.add(new RPGMonsterSpawn(QueenSpider.class, Spider.class, 1000, 10000, 64));
		spawns.add(new RPGMonsterSpawn(DiamondZombie.class, Zombie.class, 1000, 10000, 128));
		
		//Bosses
		spawns.add(new RPGMonsterSpawn(CorruptedMage.class, Zombie.class));
		spawns.add(new RPGMonsterSpawn(UndeadEmperor.class, Zombie.class));
		
		readDrops();
	}
	
	/**
	 * HP GUIDELINES:
	 *  -> Warrior-type mobs have ~1.4x more HP
	 * 
	 *  -> Normal Mob: 
	 *      -> HP: Distance ^ 2 / 10000
	 *      
	 *  -> Custom Mob: 
	 *      -> HP: Level ^ 2
	 *      
	 *  -> Miniboss: 
	 *      -> HP: Level ^ 2.5
	 *  
	 *  -> Boss:
	 *      -> HP: Level ^ 3
	 */
	public static int getHPFromSquaredSpawnDistance(double distanceSquared)
	{
		if (distanceSquared <= 40000)
			return 20;
		int calc = (int) (distanceSquared / 10000.0F);
		return calc < 0 ? 2147483647 : calc;
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
				
				RItem ri = RItem.readFromFile(file);
				if (ri == null)
				{
					RPGCore.msgConsole("&4Error reading drops file: " + file.getName() + "; RItem parsing");
					continue;
				}
				if (split.length >= 3)
					ri.dropRoll = Integer.parseInt(split[2]);
				
				spawn.drops.add(ri);
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
	}

	public RPGMonsterSpawn(Class<? extends RPGMonster> rpgMonster, Class<? extends Monster> monsterType, int minSpawnDistance, int maxSpawnDistance, int spawnRoll)
	{
		this.monsterType = monsterType;
		this.rpgMonster = rpgMonster;
		this.rpgMonsterName = rpgMonster.getSimpleName();
		this.minSpawnDistanceSquared = minSpawnDistance * minSpawnDistance;
		this.maxSpawnDistanceSquared = maxSpawnDistance * maxSpawnDistance;
		this.spawnRoll = spawnRoll;
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
		for (RItem key: drops)
		{
			try
			{
				key.saveToFile(new File(dropsFolder.getPath() + "/" + rpgMonsterName + "_" + index++ + ".yml"));
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
		dropsInventory = Bukkit.createInventory(null, 27, CakeLibrary.recodeColorCodes("&4Drops - " + rpgMonsterName));
		int index = 0;
		for (RItem key: drops)
		{
			ItemStack item = key.createItem();
			item.setAmount(key.dropRoll);
			dropsInventory.setItem(index++, item);
		}
		dropsInventory.setItem(dropsInventory.getSize() - 1, itemDrop);
		return dropsInventory;
	}
	
	public boolean isNaturalSpawn()
	{
		return this.minSpawnDistanceSquared > 0 && this.maxSpawnDistanceSquared > 0 && spawnRoll > 0;
	}

	public RPGMonster spawnMonster(Location location)
	{
		Monster m = location.getWorld().spawn(location, monsterType);
		if (m instanceof Zombie)
			((Zombie) m).setBaby(false);
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
