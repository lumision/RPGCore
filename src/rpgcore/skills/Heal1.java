package rpgcore.skills;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.minecraft.server.v1_12_R1.EnumParticle;
import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGCore;
import rpgcore.main.RPGEvents;
import rpgcore.player.RPlayer;

public class Heal1 extends RPGSkill
{
	public final static String skillName = "Heal I";
	public final static boolean passiveSkill = false;
	public final static int skillTier = 1;
	public final static int castDelay = 10;
	public final static ClassType classType = ClassType.PRIEST;
	public final static float healAmount = 2;
	public Heal1(RPlayer caster)
	{
		super(skillName, caster, passiveSkill, castDelay, 0, classType, skillTier, "Heal");
	}

	@Override
	public void instantiate(RPlayer rp)
	{
		for (RPGSkill skill: rp.skillCasts)
			if (skill.skillName.equals(skillName))
			{
				skill.casterDamage = rp.getDamageOfClass();
				skill.caster.lastSkill = skillName;
				skill.caster.castDelays.put(skillName, (int) (castDelay * skill.caster.getStats().attackSpeedMultiplier));
				skill.caster.globalCastDelay = 1;
				skill.activate();
				return;
			}
		rp.skillCasts.add(new Heal1(rp));
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(38), 
				"&cHeal I"),
				"&7Heal: " + (healAmount / 2.0F) + " hearts",
				"&7Cooldown: 3s",
				"&f",
				"&8&oHeals the user and all party",
				"&8&omembers within 16 blocks.",
				"&f",
				"&7Skill Tier: " + RPGSkill.skillTierNames[skillTier],
				"&7Class: " + classType.getClassName());
	}

	@Override
	public void activate()
	{
		super.applyCooldown(3);
		if (caster.partyID == -1)
			applyEffect(caster, player, healAmount);
		else
			for (RPlayer partyMember: RPGCore.partyManager.getParty(caster.partyID).players)
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
		new RPGEvents.ParticleEffect(EnumParticle.BLOCK_CRACK, player.getLocation().add(0, 1.25D, 0), 0.5F, 32, 0, 35).run();
		double health = player.getHealth();
		health += healAmount;
		player.setHealth(health > player.getMaxHealth() ? player.getMaxHealth() : health);
	}
}
