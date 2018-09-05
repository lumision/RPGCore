package rpgcore.skills;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGEvents;
import rpgcore.player.RPlayer;
import rpgcore.skillinventory.SkillInventory;

public class PoisonBolt extends RPGSkill
{
	public final static String skillName = "Poison Bolt";
	public final static int castDelay = 10;
	public final static ClassType classType = ClassType.MAGE;
	public PoisonBolt(RPlayer caster)
	{
		super(skillName, caster, castDelay, calculateDamage(caster.getSkillLevel(skillName)), classType);
	}

	public PoisonBolt()
	{
		super(skillName, null, castDelay, 0, classType);
	}

	@Override
	public void insantiate(RPlayer rp)
	{
		new PoisonBolt(rp);
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
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(341, 1), 
				"&aPoison Bolt"),
				"&7Skill Level: " + (unlocked ? level : 0),
				"&7Damage: " + (int) (calculateDamage(level) * 100.0D) + "%",
				"&7Interval: 0.5s",
				"&f",
				"&7Damage Over Time:",
				"&7 * " + calculateDebuffDamage(level) + " Dmg/s",
				"&7 * Duration: " + calculateDebuffLength(level) / 20 + "s",
				"&f",
				"&8&oShoots a beam of poison energy",
				"&8&owhich applies DoT to any victims.",
				"&7Class: " + classType.getClassName());
	}

	public static double calculateDamage(int level)
	{
		return 0.3D + (level / 10.0D);
	}

	public static int calculateDebuffDamage(int level)
	{
		return level < 5 ? 1 : level < 10 ? 2 : 3;
	}

	public static int calculateDebuffLength(int level)
	{
		return (level < 5 ? 5 : level < 10 ? 10 : 15) * 20;
	}
	
	public void activate()
	{
		int level = caster.getSkillLevel(skillName);
		ArrayList<LivingEntity> hit = new ArrayList<LivingEntity>();
		Vector vector = player.getLocation().getDirection().normalize().multiply(0.5D);
		int multiplier = 0;
        player.getWorld().playSound(player.getEyeLocation(), Sound.BLOCK_ANVIL_LAND, 0.1F, 1.0F);
		while (multiplier < 30)
		{
			multiplier++;
			Location point = player.getEyeLocation().add(vector.clone().multiply(multiplier));
			if (!CakeLibrary.getPassableBlocks().contains(point.getBlock().getType()))
				break;
			RPGEvents.scheduleRunnable(new RPGEvents.FireworkTrail(point, 0, 1), multiplier);
			RPGEvents.scheduleRunnable(new RPGEvents.PlaySoundEffect(point, Sound.BLOCK_SLIME_BREAK, 0.1F, 1.25F), multiplier);
			RPGEvents.scheduleRunnable(new RPGEvents.AOEDetectionCustom(hit, point, 1.25D, player, new Callable<Void>()
					{
						@Override
						public Void call() throws Exception {
							LivingEntity e = RPGEvents.customHit;
							int damage = RPlayer.varyDamage(getUnvariedDamage());
							new RPGEvents.ApplyDamage(player, e, damage).run();
							new RPGEvents.PlayEffect(Effect.STEP_SOUND, e, 165).run();
							new RPGEvents.DamageOverTime(calculateDebuffLength(level), 20, calculateDebuffDamage(level), player, e);
							return null;
						}
						
					}), multiplier);
		}
	}
}
