package rpgcore.skills;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGCore;
import rpgcore.main.RPGEvents;
import rpgcore.player.RPlayer;
import rpgcore.skillinventory.SkillInventory;

public class Enlightenment extends RPGSkill
{
	public final static String skillName = "Enlightenment";
	public final static int castDelay = 10;
	public final static ClassType classType = ClassType.PRIEST;
	public Enlightenment(RPlayer caster)
	{
		super(skillName, caster, castDelay, 0, classType);
	}
	
	public Enlightenment()
	{
		super(skillName, null, castDelay, 0, classType);
	}
	
	@Override
	public void insantiate(RPlayer rp)
	{
		new Enlightenment(rp);
	}
	
	@Override 
	public ItemStack instanceGetSkillItem(RPlayer player)
	{
		return getSkillItem(player);
	}

	public static ItemStack getSkillItem(RPlayer player)
	{
		int level = player.getSkillLevel(skillName);
		boolean unlocked = level > 0;
		level += unlocked ? 0 : 1;
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.GOLDEN_APPLE, 1), 
				"&eEnlightenment"),
				"&7Skill Level: " + (unlocked ? level : 0),
				"&7Buff:",
				"&7 * Magic Damage: +" + (5 + (level * 2)) + "%",
				"&7 * Brute Damage: +" + (5 + (level * 2)) + "%",
				"&7 * Buff Duration: " + (60 + (level * 6)) + "s",
				"&7Cooldown: 60s",
				"&f",
				"&8&oApplies a buff with the above",
				"&8&oeffects to the user and all",
				"&8&oparty members within 16 blocks.",
				"&7Class: " + classType.getClassName());
	}

	@Override
	public void activate()
	{
		super.applyCooldown(60);
		int level = caster.getSkillLevel(skillName);
		if (caster.partyID == -1)
			applyEffect(caster, caster, level);
		else 
			for (RPlayer partyMember: RPGCore.partyManager.getParty(caster.partyID).players)
				applyEffect(caster, partyMember, level);
	}

	public static void applyEffect(RPlayer caster, RPlayer rp, int level)
	{
		Player castPlayer = caster.getPlayer();
		Player player = rp.getPlayer();
		if (player == null || castPlayer == null)
			return;
		if (player.getLocation().distance(castPlayer.getLocation()) > 16.0D)
			return;
		RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, player, 251), 0);
		player.sendMessage(CakeLibrary.recodeColorCodes("&e--- Buff &6[Enlightenment &7Lv. " + level + "&6] &eapplied ---"));
		rp.removeBuff("Enlightenment");
		rp.buffs.add(new Buff(caster, classType.getTier1Class(), level, "Enlightenment", 60 + (level * 6), "&e--- Buff &6[Enlightenment] &eran out ---"));
	}
}
