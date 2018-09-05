package rpgcore.skills;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGEvents;
import rpgcore.player.RPlayer;
import rpgcore.skillinventory.SkillInventory;

public class Warcry extends RPGSkill
{
	public final static String skillName = "Warcry";
	public final static int castDelay = 10;
	public final static ClassType classType = ClassType.WARRIOR;
	public Warcry(RPlayer caster)
	{
		super(skillName, caster, castDelay, 0, classType);
	}

	public Warcry()
	{
		super(skillName, null, castDelay, 0, classType);
	}

	@Override
	public void insantiate(RPlayer rp)
	{
		new Warcry(rp);
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
				"&4Warcry"),
				"&7Skill Level: " + (unlocked ? level : 0),
				"&7Buff:",
				"&7 * Brute Damage: +" + (10 + (level * 2)) + "%",
				"&7 * Buff Duration: " + (30 + (level * 3)) + "s",
				"&7Heal: " + (level) + " hearts",
				"&7Cooldown: 30s",
				"&f",
				"&8&oLet out a warrior's battlecry,",
				"&8&orestoring some health and increases",
				"&8&odamage dealt for a short time.",
				"&7Class: " + classType.getClassName());
	}

	@Override
	public void activate()
	{
		super.applyCooldown(30);
		int level = caster.getSkillLevel(skillName);
		applyEffect(caster, caster, level);
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
		player.sendMessage(CakeLibrary.recodeColorCodes("&e--- Buff &6[Warcry &7Lv. " + level + "&6] &eapplied ---"));
		rp.removeBuff("Warcry");
		rp.buffs.add(new Buff(caster, classType.getTier1Class(), level, "Warcry", 30 + (level * 3), "&e--- Buff &6[Warcry] &eran out ---"));
		
		double health = player.getHealth();
		health += level * 2;
		player.setHealth(health > player.getMaxHealth() ? player.getMaxHealth() : health);
	}
}
