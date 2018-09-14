package rpgcore.entities.mobs;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Zombie;

import rpgcore.entities.bosses.Astrea;
import rpgcore.entities.bosses.KingZombie;
import rpgcore.entities.bosses.QueenSpider;
import rpgcore.entities.bosses.UndeadEmperor;

public class RPGMonsterSpawn
{
	public static ArrayList<RPGMonsterSpawn> spawns = new ArrayList<RPGMonsterSpawn>();

	public String rpgMonsterName;
	public Class<? extends Monster> monsterType;
	public Class<? extends RPGMonster> rpgMonster;
	public int minSpawnDistance, maxSpawnDistance;
	public int spawnRoll;
	
	//ADDMOB
	public static void onEnable()
	{
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
