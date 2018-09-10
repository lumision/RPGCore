package rpgcore.buff;

import java.util.ArrayList;

import org.bukkit.inventory.ItemStack;

public class BuffStats 
{
	private static ArrayList<BuffStats> constructedBuffStats = new ArrayList<BuffStats>();

	public int buffDuration;
	public String buffStatsName;
	public int magicDamageAdd, bruteDamageAdd, damageReductionAdd, cooldownReductionAdd, critChanceAdd, critDamageAdd;
	public float magicDamageMultiplier, bruteDamageMultiplier, attackSpeedMultiplier, xpMultiplier;
	public ItemStack icon;

	private BuffStats(String buffStatsName, ItemStack icon)
	{
		this.buffStatsName = buffStatsName;
		this.icon = icon.clone();
		boolean duplicate = false;
		for (BuffStats bs: constructedBuffStats)
			if (bs.buffStatsName.equals(buffStatsName))
				duplicate = true;
		if (!duplicate)
			constructedBuffStats.add(this);
	}

	public static BuffStats createBuffStats(String buffStatsName, ItemStack icon)
	{
		return new BuffStats(buffStatsName, icon);
	}
	
	public static BuffStats getBuffStats(String buffStatsName)
	{
		for (BuffStats bs: constructedBuffStats)
			if (bs.buffStatsName.equals(buffStatsName))
				return bs;
		return null;
	}

	public BuffStats setBuffDuration(int buffDuration)
	{
		this.buffDuration = buffDuration;
		return this;
	}

	public BuffStats setMagicDamageAdd(int magicDamageAdd)
	{
		this.magicDamageAdd = magicDamageAdd;
		return this;
	}

	public BuffStats setBruteDamageAdd(int bruteDamageAdd)
	{
		this.bruteDamageAdd = bruteDamageAdd;
		return this;
	}

	public BuffStats setDamageReductionAdd(int damageReductionAdd)
	{
		this.damageReductionAdd = damageReductionAdd;
		return this;
	}

	public BuffStats setMagicDamageMultiplier(float magicDamageMultiplier)
	{
		this.magicDamageMultiplier = magicDamageMultiplier;
		return this;
	}

	public BuffStats setBruteDamageMultiplier(float bruteDamageMultiplier)
	{
		this.bruteDamageMultiplier = bruteDamageMultiplier;
		return this;
	}

	public BuffStats setAttackSpeedMultiplier(float attackSpeedMultiplier)
	{
		this.attackSpeedMultiplier = attackSpeedMultiplier;
		return this;
	}

	public BuffStats setCooldownReductionAdd(int cooldownReductionAdd)
	{
		this.cooldownReductionAdd = cooldownReductionAdd;
		return this;
	}

	public BuffStats setCritChanceAdd(int critChanceAdd)
	{
		this.critChanceAdd = critChanceAdd;
		return this;
	}

	public BuffStats setCritDamageAdd(int critDamageAdd)
	{
		this.critDamageAdd = critDamageAdd;
		return this;
	}

	public BuffStats setXPMultiplier(float xpMultiplier)
	{
		this.xpMultiplier = xpMultiplier;
		return this;
	}
}
