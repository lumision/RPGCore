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

public class Enrage extends RPGSkill
{
	public final static String skillName = "Enrage";
	public final static boolean passiveSkill = false;
	public final static int skillTier = 2;
	public final static int castDelay = 10;
	public final static ClassType classType = ClassType.WARRIOR;
	public final static int cooldown = 3 * 60;
	public final static Stats buffStats = Stats.createStats("&cEnrage", new ItemStack(Material.REDSTONE, 1))
			.setBruteDamageMultiplier(1.3F)
			.setAttackSpeedMultiplier(1.3F)
			.setBuffDuration(60 * 20);
	public Enrage()
	{
		super(skillName, passiveSkill, castDelay, 0, classType, skillTier);
	}

	@Override
	public void instantiate(RPlayer player)
	{
		super.applyCooldown(player, cooldown);
		Buff b = Buff.createBuff(buffStats);
		applyEffect(player, b);
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.GLOWSTONE_DUST, 1), 
				"&eEnrage"),
				"&7Buff:",
				"&7 * Brute Damage: +" + CakeLibrary.convertMultiplierToAddedPercentage(buffStats.bruteDamageMultiplier) + "%",
				"&7 * Attack Speed: +" + CakeLibrary.convertMultiplierToAddedPercentage(buffStats.attackSpeedMultiplier) + "%",
				"&7 * Buff Duration: " + CakeLibrary.convertTimeToString(buffStats.buffDuration / 20),
				"&f",
				"&7Cooldown: " + CakeLibrary.convertTimeToString(cooldown),
				"&f",
				"&8&oBecome enraged; increasing damage",
				"&8&oand attack speed temporarily.",
				"&f",
				"&7Skill Tier: " + RPGSkill.skillTierNames[skillTier],
				"&7Class: " + classType.getClassName());
	}

	public static void applyEffect(RPlayer player, Buff b)
	{
		Player p = player.getPlayer();
		if (p == null)
			return;
		RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, p, 152), 0);
		b.applyBuff(player);
		player.updateScoreboard = true;
	}
}
