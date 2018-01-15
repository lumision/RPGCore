package rpgcore.entities.mobs;

import java.util.ArrayList;

import org.bukkit.GameMode;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

import rpgcore.main.CakeAPI;

public class CasterEntity 
{
	public static ArrayList<CasterEntity> entities = new ArrayList<CasterEntity>();
	public static ArrayList<CasterEntity> remove = new ArrayList<CasterEntity>();

	public double reachDistance = 16.0D;
	public Monster entity;
	public Player target;
	public int castDelay;
	public int aliveTicks;
	public CasterEntity(Monster entity)
	{
		this.entity = entity;
		entities.add(this);
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
		} else
			entity.setTarget(target);
	}

	public void findTarget()
	{
		if (target != null)
			return;
		ArrayList<Player> players = CakeAPI.getNearbyPlayers(entity.getLocation(), reachDistance);
		if (players.size() < 1)
			return;
		for (Player player: players)
		{
			if (player.getGameMode().equals(GameMode.SURVIVAL) && player.getHealth() > 0 && !player.isDead())
			{
				target = player;
				return;
			}
		}
	}
}
