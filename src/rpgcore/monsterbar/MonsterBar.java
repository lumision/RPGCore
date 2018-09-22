package rpgcore.monsterbar;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import rpgcore.main.RPGCore;
import rpgcore.main.RPGEvents;
import rpgcore.player.RPlayer;

public class MonsterBar 
{
	public static ArrayList<MonsterBar> monsterBars = new ArrayList<MonsterBar>();

	public LivingEntity entity;
	public BossBar bar;
	private MonsterBar(LivingEntity entity)
	{
		this.entity = entity;
		bar = Bukkit.createBossBar(entity.getCustomName() == null ? entity.getName() : entity.getCustomName(), BarColor.RED, BarStyle.SOLID);
	}

	public void updateBarOneTickLater()
	{
		RPGEvents.scheduleRunnable(new RPGEvents.UpdateMonsterBar(this), 1);
	}

	public void updateBar()
	{
		if (entity == null)
		{
			destroy();
			return;
		}
		if (entity.isDead())
		{
			destroy();
			return;
		}
		bar.setProgress(entity.getHealth() / entity.getMaxHealth());
	}

	public void destroy()
	{
		for (Player p: bar.getPlayers())
		{
			RPlayer rp = RPGCore.playerManager.getRPlayer(p.getUniqueId());
			rp.monsterBar = null;
		}
		bar.removeAll();
		monsterBars.remove(this);
	}

	public void showForPlayer(RPlayer rp)
	{
		if (rp.getPlayer() == null)
			return;
		if (rp.monsterBar != null)
			rp.monsterBar.bar.removePlayer(rp.getPlayer());
		rp.monsterBar = this;
		bar.addPlayer(rp.getPlayer());
	}

	public static MonsterBar getMonsterBar(LivingEntity entity)
	{
		for (MonsterBar check: monsterBars)
			if (check.entity.getEntityId() == entity.getEntityId())
				return check;
		if (entity == null)
			return null;
		if (entity.isDead())
			return null;
		MonsterBar mb = new MonsterBar(entity);
		mb.updateBarOneTickLater();
		monsterBars.add(mb);
		return mb;
	}
}
