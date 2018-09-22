package rpgcore.skills;

import java.util.ArrayList;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGEvents;
import rpgcore.player.RPlayer;

public class Kunai extends RPGSkill
{
	public final static String skillName = "Kunai";
	public final static boolean passiveSkill = false;
	public final static int skillTier = 1;
	public final static int castDelay = 10;
	public final static ClassType classType = ClassType.ASSASSIN;
	public final static float damage = 2.4F;
	public Kunai(RPlayer caster)
	{
		super(skillName, caster, passiveSkill, castDelay, damage, classType, skillTier);
	}
	
	@Override
	public void insantiate(RPlayer rp)
	{
		new Kunai(rp);
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.GOLDEN_CARROT, 1), 
				"&eKunai"),
				"&7Damage: " + (int) (damage * 100) + "%",
				"&7Cooldown: 2s",
				"&f",
				"&8&oThrows a kunai knife forward.",
				"&f",
				"&7Skill Tier: " + RPGSkill.skillTierNames[skillTier],
				"&7Class: " + classType.getClassName());
	}

	@Override
	public void activate()
	{
		super.applyCooldown(2);
		
		ArrayList<LivingEntity> hit = new ArrayList<LivingEntity>();
		Vector vector = player.getLocation().getDirection().normalize();
		int multiplier = 0;
        player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.2F, 1.0F);
        int delay = 0;
		while (multiplier < 18)
		{
			multiplier++;
			delay = multiplier / 3;
			Location point = player.getEyeLocation().add(vector.clone().multiply(multiplier));
			if (!CakeLibrary.getPassableBlocks().contains(point.getBlock().getType()))
			{
				RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, point, point.getBlock().getTypeId()), delay);
				break;
			}
			RPGEvents.scheduleRunnable(new RPGEvents.FireworkTrail(point, 0, 1), multiplier / 3);
			RPGEvents.scheduleRunnable(new RPGEvents.PlaySoundEffect(point, Sound.BLOCK_GLASS_BREAK, 0.1F, 1.25F), delay);
			RPGEvents.scheduleRunnable(new RPGEvents.AOEDetectionAttackWithBlockBreakEffect(hit, point, 0.75D, getUnvariedDamage(), player, 20), delay);
		}
	}
}
