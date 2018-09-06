package rpgcore.skills;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGEvents;
import rpgcore.player.RPlayer;

public class Warcry extends RPGSkill
{
	public final static String skillName = "Warcry";
	public final static int skillTier = 2;
	public final static int castDelay = 10;
	public final static ClassType classType = ClassType.WARRIOR;
	public final static int heal = 10;
	public final static float bruteDamageMultiplierAdd = 1.3F;
	public final static int buffLength = 15 * 20;
	public Warcry(RPlayer caster)
	{
		super(skillName, caster, castDelay, 0, classType, skillTier);
	}

	public Warcry()
	{
		super(skillName, null, castDelay, 0, classType, skillTier);
	}

	@Override
	public void insantiate(RPlayer rp)
	{
		new Warcry(rp);
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.GOLDEN_APPLE, 1), 
				"&4Warcry"),
				"&7Buff:",
				"&7 * Brute Damage: +" + bruteDamageMultiplierAdd + "%",
				"&7 * Buff Duration: " + (buffLength / 20) + "s",
				"&7Heal: " + (heal / 2.0F) + " hearts",
				"&7Cooldown: 30s",
				"&f",
				"&8&oLet out a warrior's battlecry;",
				"&8&orestoring some health and increasing",
				"&8&odamage dealt for a short time.",
				"&f",
				"&7Skill Tier: " + CakeLibrary.convertToRoman(skillTier),
				"&7Class: " + classType.getClassName());
	}

	@Override
	public void activate()
	{
		super.applyCooldown(30);
		applyEffect(caster, caster);
	}

	public static void applyEffect(RPlayer caster, RPlayer rp)
	{
		Player castPlayer = caster.getPlayer();
		Player player = rp.getPlayer();
		if (player == null || castPlayer == null)
			return;
		if (player.getLocation().distance(castPlayer.getLocation()) > 16.0D)
			return;
		RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, player, 251), 0);
		player.sendMessage(CakeLibrary.recodeColorCodes("&e--- Buff &6[&4Warcry&6] &eapplied ---"));
		rp.removeBuff("Warcry");
		rp.buffs.add(new Buff(caster, classType.getTier1Class(), "Warcry", buffLength, "&e--- Buff &6[ &4Warcry&6 ] &eran out ---"));
		
		double health = player.getHealth();
		health += heal;
		player.setHealth(health > player.getMaxHealth() ? player.getMaxHealth() : health);
	}
}
