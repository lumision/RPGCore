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

public class Warcry extends RPGSkill
{
	public final static String skillName = "Warcry";
	public final static boolean passiveSkill = false;
	public final static int skillTier = 2;
	public final static int castDelay = 10;
	public final static ClassType classType = ClassType.WARRIOR;
	public final static int heal = 10;
	public final static int buffLength = 15 * 20;
	public final static int cooldown = 30;
	public final static BuffStats buffStats = BuffStats.createBuffStats("&4Warcry", new ItemStack(Material.REDSTONE, 1))
			.setBruteDamageMultiplier(1.3F)
			.setAttackSpeedMultiplier(1.3F)
			.setBuffDuration(15 * 20);
	public Warcry(RPlayer caster)
	{
		super(skillName, caster, passiveSkill, castDelay, 0, classType, skillTier);
	}

	public Warcry()
	{
		super(skillName, null, passiveSkill, castDelay, 0, classType, skillTier);
	}

	@Override
	public void insantiate(RPlayer rp)
	{
		new Warcry(rp);
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.REDSTONE, 1), 
				"&4Warcry"),
				"&7Buff:",
				"&7 * Brute Damage: +" + CakeLibrary.convertMultiplierToAddedPercentage(buffStats.bruteDamageMultiplier) + "%",
				"&7 * Attack Speed: +" + CakeLibrary.convertMultiplierToAddedPercentage(buffStats.attackSpeedMultiplier) + "%",
				"&7 * Buff Duration: " + (buffStats.buffDuration / 20) + "s",
				"&7Heal: " + (heal / 2.0F) + " hearts",
				"&7Cooldown: " + cooldown + "s",
				"&f",
				"&8&oLet out a warrior's battlecry;",
				"&8&orestoring some health and increasing",
				"&8&odamage dealt for a short time.",
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
