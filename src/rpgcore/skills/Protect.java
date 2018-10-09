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

public class Protect extends RPGSkill
{
	public final static String skillName = "Protect";
	public final static boolean passiveSkill = false;
	public final static int skillTier = 1;
	public final static int castDelay = 0;
	public final static ClassType classType = ClassType.PRIEST;
	public final static int cooldown = 60;
	public final static Stats buffStats = Stats.createStats("&bProtect", new ItemStack(Material.IRON_INGOT, 1))
			.setDamageReductionAdd(20)
			.setBuffDuration(5 * 60 * 20);
	public Protect()
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
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.IRON_INGOT, 1), 
				"&bProtect"),
				"&7Buff:",
				"&7 * Damage Reduction: +" + buffStats.damageReductionAdd + "%",
				"&7 * Buff Duration: " + CakeLibrary.convertTimeToString(buffStats.buffDuration / 20) + "s",
				"&7 * Party Buff",
				"&f",
				"&7Cooldown: " + CakeLibrary.convertTimeToString(cooldown),
				"&f",
				"&8&oApplies a holy protection to",
				"&8&othe affected; decreasing",
				"&8&ooverall damage received.",
				"&f",
				"&7Skill Tier: " + RPGSkill.skillTierNames[skillTier],
				"&7Class: " + classType.getClassName());
	}

	public static void applyEffect(RPlayer player, Buff b)
	{
		Player p = player.getPlayer();
		if (p == null)
			return;
		RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, p, 41), 0);
		b.applyBuff(player);
		player.updateScoreboard = true;
	}
}
