package rpgcore.skills;

import org.bukkit.entity.Player;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;
import rpgcore.player.RPlayer;

public class Buff 
{
	public RPlayer buffer;
	public ClassType tier1Class;
	public int buffLevel;
	public String buffName;
	public int duration;
	public String buffReceive;
	public String buffRunout;
	public Buff(RPlayer buffer, ClassType tier1Class, int buffLevel, String buffName, int duration, String buffRunoutMessage)
	{
		this.buffer = buffer;
		this.tier1Class = tier1Class;
		this.buffLevel = buffLevel;
		this.buffName = buffName;
		this.duration = duration * 20;
		this.buffRunout = buffRunoutMessage;
	}
	
	public void tick()
	{
		if (this.duration > 0)
			this.duration--;
		if (!buffer.currentClass.getTier1Class().equals(tier1Class))
			this.duration = 0;
	}
	
	public void removeBuff(Player p)
	{
		p.sendMessage(CakeLibrary.recodeColorCodes(buffRunout));
	}
}
