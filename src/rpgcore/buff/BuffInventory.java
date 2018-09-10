package rpgcore.buff;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import rpgcore.classes.RPGClass;
import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;
import rpgcore.player.RPlayer;
import rpgcore.skillinventory2.SkillInventory2;

public class BuffInventory 
{
	private Inventory inventory;
	public RPlayer rp;
	public BuffInventory(RPlayer rp)
	{
		this.rp = rp;
	}
	
	public boolean isOpen()
	{
		return getInventory().getViewers().size() > 0;
	}
	
	public void updateInventory()
	{
		getInventory().clear();
		for (Buff b: rp.buffs)
		{
			if (CakeLibrary.isItemStackNull(b.buffStats.icon))
				continue;
			getInventory().addItem(b.getBuffIcon());
		}
		getInventory().setItem(17, getPlayerStatsIcon());
	}
	
	public ItemStack getPlayerStatsIcon()
	{
		ItemStack is = SkillInventory2.getClassIcon(rp.currentClass);
		is = CakeLibrary.editNameAndLore(is, "&4&nStats", "&6Level: &e" + rp.getCurrentClass().getLevel() + 
				" &6(&e" + rp.getCurrentClass().xp + "&6/&e" + RPGClass.getXPRequiredForLevel(rp.getCurrentClass().lastCheckedLevel + 1) + "XP&6)",
				"&f",
				"&3 * Magic Damage: &b" + rp.calculateMagicDamage(),
				"&3 * Brute Damage: &b" + rp.calculateBruteDamage(),
				"&f",
				"&4 * Crit Chance: &c" + rp.calculateCritChance() + "%",
				"&4 * Crit Damage: &c" + (int) (rp.calculateCritDamageMultiplier() * 100.0F) + "%",
				"&f",
				"&2 * Attack Speed: &ax" + Float.parseFloat(String.format("%.1f", (1.0F / rp.calculateCastDelayMultiplier()))),
				"&2 * Cooldowns: &a-" + rp.calculateCooldownReduction() + "%");
		return is;
	}
	
	public Inventory getInventory()
	{
		if (inventory != null)
			return inventory;
		return inventory = Bukkit.createInventory(null, 18, CakeLibrary.recodeColorCodes("&4Buffs"));
	}
}
