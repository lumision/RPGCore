package rpgcore.buff;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import rpgcore.classes.RPGClass;
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
		rp.updatePlayerREquips();
		getInventory().clear();
		for (Buff b: rp.buffs)
		{
			if (CakeLibrary.isItemStackNull(b.buffStats.icon))
				continue;
			getInventory().addItem(b.getBuffIcon());
		}
		getInventory().setItem(16, getCumulativeBuffEffectsIcon());
		getInventory().setItem(17, getPlayerStatsIcon());
	}

	public ItemStack getCumulativeBuffEffectsIcon()
	{
		BuffStats bf = BuffStats.createBuffStats("&f&nCumulative Buff Stats", new ItemStack(Material.PAPER));
		bf.buffDuration = 0;
		for (Buff b: rp.buffs)
		{
			if (b.buffStats.attackSpeedMultiplier != 0)
				bf.attackSpeedMultiplier += b.buffStats.attackSpeedMultiplier - 1.0F;
			bf.bruteDamageAdd += b.buffStats.bruteDamageAdd;
			if (b.buffStats.bruteDamageMultiplier != 0)
				bf.bruteDamageMultiplier += b.buffStats.bruteDamageMultiplier - 1.0F;
			bf.cooldownReductionAdd += b.buffStats.cooldownReductionAdd;
			bf.critChanceAdd += b.buffStats.critChanceAdd;
			bf.critDamageAdd += b.buffStats.critDamageAdd;
			bf.damageReductionAdd += b.buffStats.damageReductionAdd;
			bf.magicDamageAdd += b.buffStats.magicDamageAdd;
			if (b.buffStats.magicDamageMultiplier != 0)
				bf.magicDamageMultiplier += b.buffStats.magicDamageMultiplier - 1.0F;
			if (b.buffStats.xpMultiplier != 0)
				bf.xpMultiplier += b.buffStats.xpMultiplier - 1.0F;
		}
		if (bf.attackSpeedMultiplier != 0)
			bf.attackSpeedMultiplier += 1.0F;
		if (bf.bruteDamageMultiplier != 0)
			bf.bruteDamageMultiplier += 1.0F;
		if (bf.magicDamageMultiplier != 0)
			bf.magicDamageMultiplier += 1.0F;
		if (bf.xpMultiplier != 0)
			bf.xpMultiplier += 1.0F;
	
		return Buff.createBuff(bf).getBuffIcon();
	}

	public ItemStack getPlayerStatsIcon()
	{
		ItemStack is = SkillInventory2.getClassIcon(rp.currentClass);
		is = CakeLibrary.editNameAndLore(is, "&4&n" + rp.getPlayerName(), "&6Level: &e" + rp.getCurrentClass().getLevel() + 
				" &6(&e" + rp.getCurrentClass().xp + "&6/&e" + RPGClass.getXPRequiredForLevel(rp.getCurrentClass().lastCheckedLevel + 1) + "XP&6)",
				"&f",
				"&3 * Magic Damage: &b" + rp.calculateMagicDamage(),
				"&3 * Brute Damage: &b" + rp.calculateBruteDamage(),
				"&f",
				"&4 * Crit Chance: &c" + rp.calculateCritChance() + "%",
				"&4 * Crit Damage: &c" + (int) (rp.calculateCritDamageMultiplier() * 100.0F) + "%",
				"&f",
				"&2 * Attack Speed: &ax" + Float.parseFloat(String.format("%.1f", (1.0F / rp.calculateCastDelayMultiplier()))),
				"&2 * Cooldown Reduction: &a" + rp.calculateCooldownReduction() + "%",
				"&f",
				"&5 * Damage Reduction: &d" + rp.calculateDamageReduction() + "%");
		return is;
	}

	public Inventory getInventory()
	{
		if (inventory != null)
			return inventory;
		return inventory = Bukkit.createInventory(null, 18, CakeLibrary.recodeColorCodes("&4Stats / Buffs"));
	}
}
