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

public class Accelerate extends RPGSkill
{
	public final static String skillName = "Accelerate";
	public final static int skillTier = 2;
	public final static int castDelay = 0;
	public final static ClassType classType = ClassType.MAGE;
	public final static float attackSpeedMultiplierAdd = 0.2F;
	public final static int buffLength = 120 * 20;
	public final static int cooldown = 60;
	public Accelerate(RPlayer caster)
	{
		super(skillName, caster, castDelay, 0, classType, skillTier);
	}
	
	public Accelerate()
	{
		super(skillName, null, castDelay, 0, classType, skillTier);
	}
	
	@Override
	public void insantiate(RPlayer rp)
	{
		new Accelerate(rp);
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.FEATHER, 1), 
				"&bAccelerate"),
				"&7Buff:",
				"&7 * Attack Speed: +" + (int) (attackSpeedMultiplierAdd * 100.0F) + "%",
				"&7 * Buff Duration: " + (buffLength / 20) + "s",
				"&7Cooldown: 60s",
				"&f",
				"&8&oApplies a buff with the above",
				"&8&oeffects to the user and all",
				"&8&oparty members within 16 blocks.",
				"&f",
				"&7Skill Tier: " + CakeLibrary.convertToRoman(skillTier),
				"&7Class: " + classType.getClassName());
	}

	@Override
	public void activate()
	{
		super.applyCooldown(60);
		if (caster.partyID == -1)
			applyEffect(caster, caster);
		else 
			for (RPlayer partyMember: RPGCore.partyManager.getParty(caster.partyID).players)
				applyEffect(caster, partyMember);
	}

	public static void applyEffect(RPlayer caster, RPlayer rp)
	{
		Player castPlayer = caster.getPlayer();
		Player player = rp.getPlayer();
		if (player == null || castPlayer == null)
			return;
		if (player.getLocation().distance(castPlayer.getLocation()) > 16.0D)
			return;
		RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, player, 169), 0);
		player.sendMessage(CakeLibrary.recodeColorCodes("&e--- Buff &6[ &bAccelerate&6 ] &eapplied ---"));
		rp.removeBuff(skillName);
		rp.buffs.add(new Buff(caster, classType.getTier1Class(), skillName, buffLength, "&e--- Buff &6[ &bAccelerate&6 ] &eran out ---"));
	}
}
