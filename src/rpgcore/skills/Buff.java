package rpgcore.skills;

import org.bukkit.entity.Player;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeAPI;
import rpgcore.player.RPlayer;

public class Buff 
{
	public RPlayer buffer;
	public int buffLevel;
	public String buffName;
	public int duration;
	public String buffReceive;
	public String buffRunout;
	public Buff(RPlayer buffer, int buffLevel, String buffName, int duration, String buffRunoutMessage)
	{
		this.buffer = buffer;
		this.buffLevel = buffLevel;
		this.buffName = buffName;
		this.duration = duration * 20;
		this.buffRunout = buffRunoutMessage;
	}
	
	public void tick()
	{
		if (this.duration > 0)
			this.duration--;
		if (!buffer.currentClass.getTier1Class().equals(ClassType.PRIEST))
			this.duration = 0;
	}
	
	public void removeBuff(Player p)
	{
		p.sendMessage(CakeAPI.recodeColorCodes(buffRunout));
	}
}
