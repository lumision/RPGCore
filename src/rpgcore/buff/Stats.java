package rpgcore.buff;

import java.util.ArrayList;

import org.bukkit.inventory.ItemStack;

public class Stats 
{
	private static ArrayList<Stats> constructedBuffStats = new ArrayList<Stats>();

	public int buffDuration;
	public String buffStatsName;
	public int magicDamageAdd, bruteDamageAdd, damageReductionAdd, cooldownReductionAdd, recoverySpeedAdd, critChanceAdd, critDamageAdd;
	public float magicDamageMultiplier, bruteDamageMultiplier, attackSpeedMultiplier, xpMultiplier;
	public float totalDamageMultiplier, bossDamageMultiplier;
	public ItemStack icon;

	private Stats(String buffStatsName, ItemStack icon)
	{
		this.buffStatsName = buffStatsName;
		this.icon = icon.clone();
		this.icon.setAmount(1);
		boolean duplicate = false;
		for (Stats bs: constructedBuffStats)
			if (bs.buffStatsName.equals(buffStatsName))
				duplicate = true;
		if (!duplicate)
			constructedBuffStats.add(this);
	}

	public static Stats createStats(String buffStatsName, ItemStack icon)
	{
		return new Stats(buffStatsName, icon);
	}
	
	public static Stats getBuffStats(String buffStatsName)
	{
		for (Stats bs: constructedBuffStats)
			if (bs.buffStatsName.equals(buffStatsName))
				return bs;
		return null;
	}

	public Stats setBuffDuration(int buffDuration)
	{
		this.buffDuration = buffDuration;
		return this;
	}

	public Stats setMagicDamageAdd(int magicDamageAdd)
	{
		this.magicDamageAdd = magicDamageAdd;
		return this;
	}

	public Stats setBruteDamageAdd(int bruteDamageAdd)
	{
		this.bruteDamageAdd = bruteDamageAdd;
		return this;
	}

	public Stats setRecoverySpeedAdd(int recoverySpeedAdd)
	{
		this.recoverySpeedAdd = recoverySpeedAdd;
		return this;
	}
	
	public Stats setDamageReductionAdd(int damageReductionAdd)
	{
		this.damageReductionAdd = damageReductionAdd;
		return this;
	}

	public Stats setMagicDamageMultiplier(float magicDamageMultiplier)
	{
		this.magicDamageMultiplier = magicDamageMultiplier;
		return this;
	}

	public Stats setBruteDamageMultiplier(float bruteDamageMultiplier)
	{
		this.bruteDamageMultiplier = bruteDamageMultiplier;
		return this;
	}

	public Stats setAttackSpeedMultiplier(float attackSpeedMultiplier)
	{
		this.attackSpeedMultiplier = attackSpeedMultiplier;
		return this;
	}

	public Stats setCooldownReductionAdd(int cooldownReductionAdd)
	{
		this.cooldownReductionAdd = cooldownReductionAdd;
		return this;
	}

	public Stats setCritChanceAdd(int critChanceAdd)
	{
		this.critChanceAdd = critChanceAdd;
		return this;
	}

	public Stats setCritDamageAdd(int critDamageAdd)
	{
		this.critDamageAdd = critDamageAdd;
		return this;
	}

	public Stats setXPMultiplier(float xpMultiplier)
	{
		this.xpMultiplier = xpMultiplier;
		return this;
	}
}
