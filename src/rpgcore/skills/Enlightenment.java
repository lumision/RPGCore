package rpgcore.skills;

import org.bukkit.Effect;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import rpgcore.buff.Buff;
import rpgcore.buff.Stats;
import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGCore;
import rpgcore.main.RPGEvents;
import rpgcore.player.RPlayer;

public class Enlightenment extends RPGSkill
{
	public final static String skillName = "Enlightenment";
	public final static boolean passiveSkill = false;
	public final static int skillTier = 4;
	public final static int castDelay = 0;
	public final static ClassType classType = ClassType.PRIEST;
	public final static int cooldown = 60;
	public final static Stats buffStats = Stats.createStats("&eEnlightenment", new ItemStack(38, 1, (short) 6))
			.setMagicDamageMultiplier(1.3F)
			.setBruteDamageMultiplier(1.3F)
			.setBuffDuration(5 * 60 * 20);
	public Enlightenment()
	{
		super(skillName, passiveSkill, castDelay, 0, classType, skillTier);
	}

	@Override
	public void instantiate(RPlayer player)
	{
		super.applyCooldown(player, cooldown);
		Buff b = Buff.createBuff(buffStats);
		if (player.partyID == -1)
			applyEffect(player, b);
		else
			for (RPlayer partyMember: RPGCore.partyManager.getParty(player.partyID).players)
				applyEffect(partyMember, b);
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(38, 1, (short) 6), 
				"&eEnlightenment"),
				"&7Buff:",
				"&7 * Magic Damage: +" + CakeLibrary.convertMultiplierToAddedPercentage(buffStats.magicDamageMultiplier) + "%",
				"&7 * Brute Damage: +" + CakeLibrary.convertMultiplierToAddedPercentage(buffStats.bruteDamageMultiplier) + "%",
				"&7 * Buff Duration: " + CakeLibrary.convertTimeToString(buffStats.buffDuration / 20),
				"&7 * Party Buff",
				"&f",
				"&7Cooldown: " + CakeLibrary.convertTimeToString(cooldown),
				"&f",
				"&8&oGrants intellectual light to",
				"&8&othe affected; increasing",
				"&8&ooverall attack efficiency.",
				"&f",
				"&7Skill Tier: " + RPGSkill.skillTierNames[skillTier],
				"&7Class: " + classType.getClassName());
	}

	public static void applyEffect(RPlayer player, Buff b)
	{
		Player p = player.getPlayer();
		if (p == null)
			return;
		RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, p, 89), 0);
		b.applyBuff(player);
		player.updateScoreboard = true;
	}
}
