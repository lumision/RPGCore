package rpgcore.skills;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGEvents;
import rpgcore.player.RPlayer;

public class PoisonBolt extends RPGSkill
{
	public final static String skillName = "Poison Bolt";
	public final static boolean passiveSkill = false;
	public final static int skillTier = 1;
	public final static int castDelay = 10;
	public final static ClassType classType = ClassType.MAGE;
	public final static float damage = 1.7F;
	public final static int debuffDamage = 3;
	public final static int debuffLength = 10 * 20;
	public PoisonBolt(RPlayer caster)
	{
		super(skillName, caster, passiveSkill, castDelay, damage, classType, skillTier);
	}

	public PoisonBolt()
	{
		super(skillName, null, passiveSkill, castDelay, 0, classType, skillTier);
	}

	@Override
	public void insantiate(RPlayer rp)
	{
		new PoisonBolt(rp);
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(341, 1), 
				"&aPoison Bolt"),
				"&7Damage: " + (int) (damage * 100.0F) + "%",
				"&7Interval: 0.5s",
				"&f",
				"&7Damage Over Time:",
				"&7 * " + (debuffDamage / 2.0F) + " Hearts/s",
				"&7 * Duration: " + (debuffLength / 20) + "s",
				"&f",
				"&8&oShoots a bolt of poison energy;",
				"&8&oapplying DoT to any victims.",
				"&f",
				"&7Skill Tier: " + RPGSkill.skillTierNames[skillTier],
				"&7Class: " + classType.getClassName());
	}
	
	public void activate()
	{
		ArrayList<LivingEntity> hit = new ArrayList<LivingEntity>();
		Vector vector = player.getLocation().getDirection().normalize().multiply(0.75D);
		int multiplier = 0;
        player.getWorld().playSound(player.getEyeLocation(), Sound.BLOCK_ANVIL_LAND, 0.1F, 1.0F);
		while (multiplier < 20)
		{
			multiplier++;
			Location point = player.getEyeLocation().add(vector.clone().multiply(multiplier));
			if (!CakeLibrary.getPassableBlocks().contains(point.getBlock().getType()))
			{
				RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, point, point.getBlock().getTypeId()), multiplier);
				break;
			}
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
							new RPGEvents.DamageOverTime(debuffLength, 20, debuffDamage, player, e);
							return null;
						}
						
					}), multiplier);
		}
	}
}
