package rpgcore.skills;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.minecraft.server.v1_12_R1.EnumParticle;
import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeAPI;
import rpgcore.main.RPGCore;
import rpgcore.main.RPGEvents;
import rpgcore.player.RPlayer;
import rpgcore.skillinventory.SkillInventory;

public class Heal extends RPGSkill
{
	public final static String skillName = "Heal";
	public final static int castDelay = 10;
	public final static ClassType classType = ClassType.PRIEST;
	public Heal(RPlayer caster)
	{
		super(skillName, caster, castDelay, 0, classType);
	}
	
	public Heal()
	{
		super(skillName, null, castDelay, 0, classType);
	}
	
	@Override
	public void insantiate(RPlayer rp)
	{
		new Heal(rp);
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
		return CakeAPI.addLore(CakeAPI.renameItem(unlocked ? new ItemStack(38, 1, (short) 6) : SkillInventory.locked.clone(), 
				"&cHeal"),
				"&7Skill Level: " + (unlocked ? level : 0),
				"&7Heal: " + getHealAmount(level) / 2.0D + " hearts",
				"&7Cooldown: 3s",
				"&f",
				"&8&oHeals the user and all party",
				"&8&omembers within 16 blocks.",
				"&7Class: " + classType.getClassName());
	}

	public static double getHealAmount(int level)
	{
		switch(level)
		{
		case 1: return 1.0D;
		case 2: return 1.4D;
		case 3: return 1.8D;
		case 4: return 2.2D;
		case 5: return 2.6D;
		case 6: return 3.0D;
		case 7: return 3.2D;
		case 8: return 3.4D;
		case 9: return 3.8D;
		case 10: return 4.0D;
		default: return 1.0D;
		}
	}

	@Override
	public void activate()
	{
		super.applyCooldown(3);
		double healAmount = getHealAmount(caster.getSkillLevel(skillName));
		if (caster.partyID == -1)
			applyEffect(caster, player, healAmount);
		else
			for (RPlayer partyMember: RPGCore.instance.partyManager.getParty(caster.partyID).players)
				applyEffect(partyMember, player, healAmount);
	}

	public static void applyEffect(RPlayer rp, Player caster, double healAmount)
	{
		Player player = rp.getPlayer();
		if (player == null)
			return;
		if (player.getLocation().distance(caster.getLocation()) > 16.0D)
			return;
		for (int i = 0; i < 3; i++)
			RPGEvents.scheduleRunnable(new RPGEvents.PlaySoundEffect(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.2F, 0.8F + (i * 0.2F)), i * 2);
		CakeAPI.spawnParticle(EnumParticle.BLOCK_CRACK, player.getLocation().add(0, 1.25D, 0), 0.5F, player, 32, 0, new int[]{35});
		double health = player.getHealth();
		health += healAmount;
		player.setHealth(health > player.getMaxHealth() ? player.getMaxHealth() : health);
	}
}
