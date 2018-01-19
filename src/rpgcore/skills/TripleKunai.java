package rpgcore.skills;

import java.util.ArrayList;
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
import rpgcore.skillinventory.SkillInventory;

public class TripleKunai extends RPGSkill
{
	public final static String skillName = "Triple Kunai";
	public final static int castDelay = 10;
	public final static ClassType classType = ClassType.ASSASSIN;
	public TripleKunai(RPlayer caster)
	{
		super(skillName, caster, castDelay, calculateDamage(caster.getSkillLevel(skillName)), classType);
	}

	public TripleKunai()
	{
		super(skillName, null, castDelay, 0, classType);
	}

	@Override
	public void insantiate(RPlayer rp)
	{
		new TripleKunai(rp);
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
		return CakeLibrary.addLore(CakeLibrary.renameItem(unlocked ? new ItemStack(Material.GOLDEN_CARROT, 1) : SkillInventory.locked.clone(), 
				"&cTriple Kunai"),
				"&7Skill Level: " + (unlocked ? level : 0),
				"&7Damage: " + (int) (calculateDamage(level) * 100) + "%",
				"&7Cooldown: 2s",
				"&f",
				"&8&oThrows 3 kunai knives forward.",
				"&7Class: " + classType.getClassName());
	}

	public static double calculateDamage(int level)
	{
		return 0.8D + (level / 5.0D);
	}

	@Override
	public void activate()
	{
		super.applyCooldown(2);

		Vector vector1 = player.getLocation().getDirection();
		for (int i = 0; i < 3; i++)
		{
			ArrayList<LivingEntity> hit = new ArrayList<LivingEntity>();
			Vector vector = i == 0 ? vector1.clone() :
				i == 1 ? vector1.clone().setX(vector1.getX() - 0.25D).setZ(vector1.getZ() - 0.25D) :
					vector1.clone().setX(vector1.getX() + 0.25D).setZ(vector1.getZ() + 0.25D);
				vector.normalize().multiply(0.5D);
				int multiplier = 0;
				player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.2F, 1.0F);
				while (multiplier < 24)
				{
					multiplier++;
					Location point = player.getEyeLocation().add(vector.clone().multiply(multiplier));
					if (!CakeLibrary.getPassableBlocks().contains(point.getBlock().getType()))
						break;
					RPGEvents.scheduleRunnable(new RPGEvents.FireworkTrail(point, 0, 1), multiplier / 3);
					if (i == 0)
						RPGEvents.scheduleRunnable(new RPGEvents.PlaySoundEffect(point, Sound.BLOCK_GLASS_BREAK, 0.1F, 1.25F), multiplier/ 3);
					RPGEvents.scheduleRunnable(new RPGEvents.AOEDetectionAttackWithBlockBreakEffect(hit, point, 0.75D, getUnvariedDamage(), player, 20), multiplier / 3);
				}
		}
	}
}
