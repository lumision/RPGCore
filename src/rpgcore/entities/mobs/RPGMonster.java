package rpgcore.entities.mobs;

import java.util.ArrayList;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;

import rpgcore.entities.bosses.QueenSpider;
import rpgcore.entities.bosses.UndeadEmperor;
import rpgcore.main.CakeLibrary;

public abstract class RPGMonster 
{
	public static ArrayList<RPGMonster> entities = new ArrayList<RPGMonster>();
	public static ArrayList<RPGMonster> remove = new ArrayList<RPGMonster>();

	public double reachDistance = 16.0D;
	public Monster entity;
	public Player target;
	public int castDelay;
	public double aliveTicks;
	
	public RPGMonster(Monster entity)
	{
		this.entity = entity;
		entities.add(this);
	}
	
	public static RPGMonster getRPGMob(int entityID)
	{
		for (RPGMonster ce: entities)
			if (ce.entity.getEntityId() == entityID)
				return ce;
		return null;
	}
	
	public static Monster spawnMob(String mobName, Location location)
	{
		if (mobName.equalsIgnoreCase("reinforcedzombie"))
		{
			Zombie m = location.getWorld().spawn(location, Zombie.class);
			new ReinforcedZombie(m);
			return m;
		}
		if (mobName.equalsIgnoreCase("reinforcedskeleton"))
		{
			Skeleton m = location.getWorld().spawn(location, Skeleton.class);
			new ReinforcedSkeleton(m);
			return m;
		}
		if (mobName.equalsIgnoreCase("magezombie"))
		{
			Zombie m = location.getWorld().spawn(location, Zombie.class);
			new MageZombie(m);
			return m;
		}
		if (mobName.equalsIgnoreCase("sorcererzombie"))
		{
			Zombie m = location.getWorld().spawn(location, Zombie.class);
			new SorcererZombie(m);
			return m;
		}
		if (mobName.equalsIgnoreCase("warriorzombie"))
		{
			Zombie m = location.getWorld().spawn(location, Zombie.class);
			new WarriorZombie(m);
			return m;
		}

		
		if (mobName.equalsIgnoreCase("undeademperor"))
		{
			Zombie m = location.getWorld().spawn(location, Zombie.class);
			new UndeadEmperor(m);
			return m;
		}
		if (mobName.equalsIgnoreCase("queenspider"))
		{
			Spider m = location.getWorld().spawn(location, Spider.class);
			new QueenSpider(m);
			return m;
		}
		return null;
	}

	public boolean isDead()
	{
		if (entity == null)
			return true;
		if (entity.isDead() || entity.getHealth() <= 0)
			return true;
		return false;
	}

	public void tick()
	{
		if (isDead())
		{
			remove.add(this);
			return;
		}
		if (this.castDelay > 0)
			this.castDelay--;
		this.aliveTicks++;
		if (target != null)
		{
			if (target.getHealth() <= 0 || target.isDead())
			{
				target = null;
				return;
			}
			if (target.getLocation().distance(entity.getLocation()) > reachDistance)
			{
				target = null;
				return;
			}
			if (!target.getGameMode().equals(GameMode.SURVIVAL))
			{
				target = null;
				return;
			}
			return;
		}
	}

	public void findTarget()
	{
		if (target != null)
			return;
		ArrayList<Player> players = CakeLibrary.getNearbyPlayers(entity.getLocation(), reachDistance);
		if (players.size() < 1)
			return;
		for (Player player: players)
		{
			if (player.getGameMode().equals(GameMode.SURVIVAL) && player.getHealth() > 0 && !player.isDead())
			{
				target = player;
				entity.setTarget(player);
				return;
			}
		}
	}
	
	public abstract ItemStack[] getDrops();
}
