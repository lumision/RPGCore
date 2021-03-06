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

public class CelestialBlessing extends RPGSkill
{
	public final static String skillName = "Celestial Blessing";
	public final static boolean passiveSkill = false;
	public final static int skillTier = 1;
	public final static int castDelay = 0;
	public final static ClassType classType = ClassType.ALL;
	public final static int cooldown = 10 * 60;
	public final static Stats buffStats = Stats.createStats("&fC&7e&fl&7e&fs&7t&fi&7a&fl &7B&fl&7e&fs&7s&fi&7n&fg", new ItemStack(Material.NETHER_STAR, 1))
			.setXPMultiplier(1.3F)
			.setBuffDuration(30 * 60 * 20);
	public CelestialBlessing()
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
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.NETHER_STAR, 1), 
				"&fC&7e&fl&7e&fs&7t&fi&7a&fl &7B&fl&7e&fs&7s&fi&7n&fg"),
				"&7Buff:",
				"&7 * Combat XP: +" + CakeLibrary.convertMultiplierToAddedPercentage(buffStats.xpMultiplier) + "%",
				"&7 * Buff Duration: " + CakeLibrary.convertTimeToString(buffStats.buffDuration / 20),
				"&7 * Party Buff",
				"&f",
				"&7Cooldown: " + CakeLibrary.convertTimeToString(cooldown),
				"&f",
				"&8&oEmpowers the affected with",
				"&8&olost magic; increasing their",
				"&8&ototal experience gain.",
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
