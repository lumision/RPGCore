package rpgcore.skills;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import rpgcore.buff.Buff;
import rpgcore.buff.BuffStats;
import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGCore;
import rpgcore.main.RPGEvents;
import rpgcore.player.RPlayer;

public class Enlightenment extends RPGSkill
{
	public final static String skillName = "Enlightenment";
	public final static boolean passiveSkill = false;
	public final static int skillTier = 1;
	public final static int castDelay = 0;
	public final static ClassType classType = ClassType.PRIEST;
	public final static int cooldown = 60;
	public final static BuffStats buffStats = BuffStats.createBuffStats("&eEnlightenment", new ItemStack(Material.GOLDEN_APPLE, 1))
			.setMagicDamageMultiplier(1.2F)
			.setBruteDamageMultiplier(1.2F)
			.setBuffDuration(120 * 20);
	public Enlightenment(RPlayer caster)
	{
		super(skillName, caster, passiveSkill, castDelay, 0, classType, skillTier);
	}

	public Enlightenment()
	{
		super(skillName, null, passiveSkill, castDelay, 0, classType, skillTier);
	}

	@Override
	public void insantiate(RPlayer rp)
	{
		new Enlightenment(rp);
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.GOLDEN_APPLE, 1), 
				"&eEnlightenment"),
				"&7Buff:",
				"&7 * Magic Damage: +" + CakeLibrary.convertMultiplierToAddedPercentage(buffStats.magicDamageMultiplier) + "%",
				"&7 * Brute Damage: +" + CakeLibrary.convertMultiplierToAddedPercentage(buffStats.bruteDamageMultiplier) + "%",
				"&7 * Buff Duration: " + (buffStats.buffDuration / 20) + "s",
				"&7Cooldown: 60s",
				"&f",
				"&8&oApplies a buff with the above",
				"&8&oeffects to the user and all",
				"&8&oparty members within 16 blocks.",
				"&f",
				"&7Skill Tier: " + RPGSkill.skillTierNames[skillTier],
				"&7Class: " + classType.getClassName());
	}

	@Override
	public void activate()
	{
		super.applyCooldown(cooldown);
		applyEffect(caster);
	}

	public static void applyEffect(RPlayer rp)
	{
		Buff b = Buff.createBuff(buffStats);
		if (rp.partyID == -1)
		{
			Player p = rp.getPlayer();
			if (p == null)
				return;
			RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, p, 41), 0);
			b.applyBuff(rp);
			rp.updateScoreboard();
		}
		else
		{
			for (RPlayer partyMember: RPGCore.partyManager.getParty(rp.partyID).players)
			{
				Player p = partyMember.getPlayer();
				if (p == null)
					continue;
				RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, p, 41), 0);
				b.applyBuff(partyMember);
				partyMember.updateScoreboard();
			}
		}
	}
}
