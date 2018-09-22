package rpgcore.buff;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import rpgcore.classes.RPGClass;
import rpgcore.main.CakeLibrary;
import rpgcore.player.RPlayer;
import rpgcore.sideclasses.RPGSideClass.SideClassType;
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
		getInventory().setItem(15, getCumulativeBuffEffectsIcon());
		getInventory().setItem(16, getSideclassesIcon());
		getInventory().setItem(17, getPlayerStatsIcon());
	}

	public ItemStack getCumulativeBuffEffectsIcon()
	{
		Stats bf = Stats.createStats("&f&nCumulative Buff Stats", new ItemStack(Material.PAPER));
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
				"&3 * Magic Damage: &b" + rp.getStats().magicDamageAdd,
				"&3 * Brute Damage: &b" + rp.getStats().bruteDamageAdd,
				"&3 * Total Damage: &bx" + String.format("%.2f", rp.getStats().totalDamageMultiplier),
				"&3 * Boss Damage: &bx" + String.format("%.2f", rp.getStats().bossDamageMultiplier),
				"&f",
				"&4 * Crit Chance: &c" + rp.getStats().critChanceAdd + "%",
				"&4 * Crit Damage: &c" + rp.getStats().critDamageAdd + "%",
				"&f",
				"&2 * Attack Speed: &ax" + String.format("%.1f", (1.0F / rp.getStats().attackSpeedMultiplier)),
				"&2 * Cooldown Reduction: &a" + rp.getStats().cooldownReductionAdd + "%",
				"&f",
				"&5 * Damage Reduction: &d" + rp.getStats().damageReductionAdd + "%",
				"&5 * Recovery Interval: &d" + String.format("%.2f", (rp.recoverMaxTicks / 20.0F)) + "s",
				"&f",
				"&6 * Combat XP: &e" + (100 + CakeLibrary.convertMultiplierToAddedPercentage(rp.getStats().xpMultiplier)) + "%");
		return is;
	}

	public ItemStack getSideclassesIcon()
	{
		ItemStack is = new ItemStack(Material.WORKBENCH);
		is = CakeLibrary.editNameAndLore(is, "&e&nSideclasses",
				"&cProspector",
				"&4 * Level: &c" + rp.getSideClass(SideClassType.PROSPECTOR).lastCheckedLevel,
				"&f",
				"&dAlchemist",
				"&5 * Level: &d" + rp.getSideClass(SideClassType.ALCHEMIST).lastCheckedLevel,
				"&f",
				"&aCrafter",
				"&2 * Level: &a" + rp.getSideClass(SideClassType.CRAFTER).lastCheckedLevel,
				"&f",
				"&eEnchanter",
				"&6 * Level: &e" + rp.getSideClass(SideClassType.ENCHANTER).lastCheckedLevel);
		return is;
	}

	public Inventory getInventory()
	{
		if (inventory != null)
			return inventory;
		return inventory = Bukkit.createInventory(null, 18, CakeLibrary.recodeColorCodes("&4Stats / Buffs"));
	}
}
