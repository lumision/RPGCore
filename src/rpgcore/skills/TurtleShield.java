package rpgcore.skills;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import rpgcore.buff.Buff;
import rpgcore.buff.Stats;
import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGEvents;
import rpgcore.player.RPlayer;

public class TurtleShield extends RPGSkill
{
	public final static String skillName = "Turtle Shield";
	public final static boolean passiveSkill = false;
	public final static int skillTier = 3;
	public final static int castDelay = 10;
	public final static ClassType classType = ClassType.WARRIOR;
	public final static int heal = 10;
	public final static Stats buffStats = Stats.createStats("&2Turtle Shield", new ItemStack(Material.EMERALD, 1))
			.setAttackSpeedMultiplier(0.5F)
			.setDamageReductionAdd(50)
			.setRecoverySpeedAdd(50)
			.setBuffDuration(60 * 20);
	public TurtleShield(RPlayer caster)
	{
		super(skillName, caster, passiveSkill, castDelay, 0, classType, skillTier);
	}

	@Override
	public void insantiate(RPlayer rp)
	{
		new TurtleShield(rp);
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.EMERALD, 1), 
				"&2Turtle Shield"),
				"&7Buff:",
				"&7 * Attack Speed: " + CakeLibrary.convertMultiplierToAddedPercentage(buffStats.attackSpeedMultiplier) + "%",
				"&7 * Damage Reduction: +" + buffStats.damageReductionAdd + "%",
				"&7 * Recovery Speed: +" + buffStats.recoverySpeedAdd + "%",
				"&7 * Buff Duration: " + CakeLibrary.convertTimeToString(buffStats.buffDuration / 20),
				"&f",
				"&8&oTrade off attack speed for defense",
				"&8&oand recovery; allowing you to last",
				"&8&osignificantly longer in battle.",
				"&f",
				"&7Skill Tier: " + RPGSkill.skillTierNames[skillTier],
				"&7Class: " + classType.getClassName());
	}

	@Override
	public void activate()
	{
		Buff b = Buff.createBuff(buffStats);
		applyEffect(caster, b);
	}

	public static void applyEffect(RPlayer rp, Buff b)
	{
		Player p = rp.getPlayer();
		if (p == null)
			return;
		RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, p, 152), 0);
		b.applyBuff(rp);
		rp.updateScoreboard = true;
	}
}
