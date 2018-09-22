package rpgcore.entities.mobs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.bukkit.GameMode;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

import rpgcore.main.CakeLibrary;

public abstract class RPGMonster 
{
	public static ArrayList<RPGMonster> entities = new ArrayList<RPGMonster>();

	public boolean isBoss;
	public int reachDistance;
	public int reachDistanceLoseSquared;
	public Monster entity;
	public Player target;
	public int castDelay;
	public double aliveTicks;
	public boolean bound;
	public static Random rand = new Random();
	public static HashMap<String, Class<? extends RPGMonster>> mobList = new HashMap<String, Class<? extends RPGMonster>>();
	
	public RPGMonster(Monster entity, boolean isBoss)
	{
		this.entity = entity;
		this.isBoss = isBoss;
		this.reachDistance = 16;
		this.reachDistanceLoseSquared = isBoss ? 32 * 32 : 16 * 16;
		entities.add(this);
	}

	public static RPGMonster getRPGMob(int entityID)
	{
		for (RPGMonster ce: entities)
			if (ce.entity.getEntityId() == entityID)
				return ce;
		return null;
	}

	public boolean isDead()
	{
		if (entity == null)
			return true;
		if (entity.isDead())
			return true;
		if (entity.getHealth() <= 0)
			return true;
		return false;
	}

	public boolean tick()
	{
		if (isDead())
			return true;
		if (bound)
		{
			this.castDelay = 1;
			return false;
		}
		if (aliveTicks % 10 == 0)
			findTarget();
		if (this.castDelay > 0)
			this.castDelay--;
		this.aliveTicks++;
		if (target != null)
		{
			if (target.getHealth() <= 0 || target.isDead())
			{
				target = null;
				return false;
			}
			if (target.getWorld() != entity.getWorld())
			{
				target = null;
				return false;
			}
			if (target.getLocation().distanceSquared(entity.getLocation()) > reachDistanceLoseSquared)
			{
				target = null;
				return false;
			}
			if (!target.getGameMode().equals(GameMode.SURVIVAL))
			{
				target = null;
				return false;
			}
			entity.setTarget(target);
		}
		return false;
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
}
