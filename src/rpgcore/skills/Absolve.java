package rpgcore.skills;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import rpgcore.buff.Buff;
import rpgcore.buff.Stats;
import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGCore;
import rpgcore.main.RPGEvents;
import rpgcore.player.RPlayer;

public class Absolve extends RPGSkill
{
	public final static String skillName = "Absolve";
	public final static boolean passiveSkill = false;
	public final static int skillTier = 2;
	public final static int castDelay = 0;
	public final static ClassType classType = ClassType.PRIEST;
	public final static int cooldown = 60;
	public final static Stats buffStats = Stats.createStats("&dAbsolve", new ItemStack(Material.WATER_BUCKET, 1))
			.setXPMultiplier(1.2F)
			.setBuffDuration(5 * 60 * 20);
	public Absolve()
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
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.WATER_BUCKET, 1), 
				"&dAbsolve"),
				"&7Buff:",
				"&7 * Combat XP: +" + CakeLibrary.convertMultiplierToAddedPercentage(buffStats.xpMultiplier) + "%",
				"&7 * Buff Duration: " + CakeLibrary.convertTimeToString(buffStats.buffDuration / 20),
				"&7 * Party Buff",
				"&f",
				"&7Cooldown: " + CakeLibrary.convertTimeToString(cooldown),
				"&f",
				"&8&oLiberates the affected;",
				"&8&oincreasing their overall",
				"&8&oexperience gain.",
				"&f",
				"&7Skill Tier: " + RPGSkill.skillTierNames[skillTier],
				"&7Class: " + classType.getClassName());
	}

	public static void applyEffect(RPlayer player, Buff b)
	{
		Player p = player.getPlayer();
		if (p == null)
			return;
		RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, p, 20), 0);
		RPGEvents.scheduleRunnable(new RPGEvents.PlaySoundEffect(p, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.5F, 1.5F), 0);
		b.applyBuff(player);
		player.updateScoreboard = true;
	}
}
