package rpgcore.skills;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import rpgcore.buff.Buff;
import rpgcore.buff.Stats;
import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGCore;
import rpgcore.main.RPGEvents;
import rpgcore.player.RPlayer;

public class Accelerate extends RPGSkill
{
	public final static String skillName = "Accelerate";
	public final static boolean passiveSkill = false;
	public final static int skillTier = 2;
	public final static int castDelay = 0;
	public final static ClassType classType = ClassType.MAGE;
	public final static int cooldown = 60;
	public final static Stats buffStats = Stats.createStats("&bAccelerate", new ItemStack(Material.FEATHER, 1))
			.setAttackSpeedMultiplier(1.2F)
			.setBuffDuration(5 * 60 * 20);
	public Accelerate(RPlayer caster)
	{
		super(skillName, caster, passiveSkill, castDelay, 0, classType, skillTier);
	}

	@Override
	public void instantiate(RPlayer rp)
	{
		for (RPGSkill skill: rp.skillCasts)
			if (skill.skillName.equals(skillName))
			{
				skill.casterDamage = rp.getDamageOfClass();
				skill.caster.lastSkill = skillName;
				skill.caster.castDelays.put(skillName, (int) (castDelay * skill.caster.getStats().attackSpeedMultiplier));
				skill.caster.globalCastDelay = 1;
				skill.activate();
				return;
			}
		rp.skillCasts.add(new Accelerate(rp));
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.SUGAR, 1), 
				"&bAccelerate"),
				"&7Buff:",
				"&7 * Attack Speed: +" + CakeLibrary.convertMultiplierToAddedPercentage(buffStats.attackSpeedMultiplier) + "%",
				"&7 * Buff Duration: " + CakeLibrary.convertTimeToString(buffStats.buffDuration / 20),
				"&7 * Party Buff",
				"&7Cooldown: " + cooldown + "s",
				"&f",
				"&8&oApplies an arcane buff that",
				"&8&oassists mobility; increasing",
				"&8&ooverall attack speed.",
				"&f",
				"&7Skill Tier: " + RPGSkill.skillTierNames[skillTier],
				"&7Class: " + classType.getClassName());
	}

	@Override
	public void activate()
	{
		super.applyCooldown(cooldown);
		Buff b = Buff.createBuff(buffStats);
		if (caster.partyID == -1)
			applyEffect(caster, b);
		else
			for (RPlayer partyMember: RPGCore.partyManager.getParty(caster.partyID).players)
				applyEffect(partyMember, b);
	}

	public static void applyEffect(RPlayer rp, Buff b)
	{
		Player p = rp.getPlayer();
		if (p == null)
			return;
		RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, p, 20), 0);
		b.applyBuff(rp);
		rp.updateScoreboard = true;
	}
}
