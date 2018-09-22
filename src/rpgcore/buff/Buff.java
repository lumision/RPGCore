package rpgcore.buff;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGCore;
import rpgcore.player.RPlayer;

public class Buff 
{
	public static ArrayList<Buff> buffs = new ArrayList<Buff>();
	public int duration;
	public Stats buffStats;
	
	private Buff(Stats buffStats)
	{
		this.buffStats = buffStats;
		this.duration = buffStats.buffDuration;
	}
	
	public static Buff createBuff(Stats buffStats)
	{
		Buff b = new Buff(buffStats);
		buffs.add(b);
		return b;
	}
	
	public ItemStack getBuffIcon()
	{
		ItemStack icon = buffStats.icon.clone();
		ItemMeta im = icon.getItemMeta();
		im.setDisplayName(CakeLibrary.recodeColorCodes(buffStats.buffStatsName));
		
		ArrayList<String> lore = new ArrayList<String>();
		if (buffStats.magicDamageAdd != 0)
			lore.add(CakeLibrary.recodeColorCodes("&7 * Magic Damage: +" + buffStats.magicDamageAdd));
		if (buffStats.magicDamageMultiplier != 0)
			lore.add(CakeLibrary.recodeColorCodes("&7 * Magic Damage: +" + CakeLibrary.convertMultiplierToAddedPercentage(buffStats.magicDamageMultiplier) + "%"));
		if (buffStats.bruteDamageAdd != 0)
			lore.add(CakeLibrary.recodeColorCodes("&7 * Brute Damage: +" + buffStats.bruteDamageAdd));
		if (buffStats.bruteDamageMultiplier != 0)
			lore.add(CakeLibrary.recodeColorCodes("&7 * Brute Damage: +" + CakeLibrary.convertMultiplierToAddedPercentage(buffStats.bruteDamageMultiplier) + "%"));
		if (buffStats.attackSpeedMultiplier != 0)
			lore.add(CakeLibrary.recodeColorCodes("&7 * Attack Speed: +" + CakeLibrary.convertMultiplierToAddedPercentage(buffStats.attackSpeedMultiplier) + "%"));
		if (buffStats.critChanceAdd != 0)
			lore.add(CakeLibrary.recodeColorCodes("&7 * Crit Chance: +" + buffStats.critChanceAdd + "%"));
		if (buffStats.critDamageAdd != 0)
			lore.add(CakeLibrary.recodeColorCodes("&7 * Crit Damage: +" + buffStats.critDamageAdd + "%"));
		if (buffStats.damageReductionAdd != 0)
			lore.add(CakeLibrary.recodeColorCodes("&7 * Damage Reduction: +" + buffStats.damageReductionAdd + "%"));
		if (buffStats.cooldownReductionAdd != 0)
			lore.add(CakeLibrary.recodeColorCodes("&7 * Cooldown Reduction: +" + buffStats.cooldownReductionAdd + "%"));
		if (buffStats.xpMultiplier != 0)
			lore.add(CakeLibrary.recodeColorCodes("&7 * Combat XP: +" + CakeLibrary.convertMultiplierToAddedPercentage(buffStats.xpMultiplier) + "%"));
		
		if (duration > 0)
		{
			lore.add(CakeLibrary.recodeColorCodes("&f"));
			lore.add(CakeLibrary.recodeColorCodes("&7Buff Duration: " + CakeLibrary.convertTimeToString(duration / 20)));
		}
		
		im.setLore(lore);
		icon.setItemMeta(im);
		return icon;
	}
	
	public boolean tick()
	{
		if (this.duration > 0)
			this.duration--;
		return duration == 0;
	}
	
	public void applyBuff(RPlayer rp)
	{
		Player p = rp.getPlayer();
		if (p == null)
			return;
		for (int i = 0; i < rp.buffs.size(); i++)
		{
			Buff b = rp.buffs.get(i);
			if (b.buffStats.buffStatsName.equals(buffStats.buffStatsName))
			{
				b.duration = duration;
				RPGCore.msgNoTag(p, "&e--- Buff &6[ " + buffStats.buffStatsName + "&6 ] &eapplied ---");
				return;
			}
		}
		rp.buffs.add(this);
		RPGCore.msgNoTag(p, "&e--- Buff &6[ " + buffStats.buffStatsName + "&6 ] &eapplied ---");
		rp.updateScoreboard = true;
	}
	
	public void removeBuff(Player p)
	{
		if (p != null)
			RPGCore.msgNoTag(p, "&7--- Buff &8[ " + buffStats.buffStatsName + "&8 ] &7expired ---");
	}
}
