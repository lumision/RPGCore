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

public class IceBolt extends RPGSkill
{
	public final static String skillName = "Ice Bolt";
	public final static boolean passiveSkill = false;
	public final static int skillTier = 1;
	public final static int castDelay = 10;
	public final static ClassType classType = ClassType.MAGE;
	public final static float damage = 1.6F;
	public final static int debuffLevel = 1;
	public final static int debuffLength = 10 * 20;
	public IceBolt(RPlayer caster)
	{
		super(skillName, caster, passiveSkill, castDelay, damage, classType, skillTier);
	}

	@Override
	public void insantiate(RPlayer rp)
	{
		new IceBolt(rp);
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.SNOW_BALL, 1), 
				"&bIce Bolt"),
				"&7Damage: " + (int) (damage * 100.0F) + "%",
				"&7Interval: " + (castDelay / 20.0F) + "s",
				"&f",
				"&7Debuff:",
				"&7 * Slow " + CakeLibrary.convertToRoman(debuffLevel + 1),
				"&7 * Duration: " + (debuffLength / 20) + "s",
				"&f",
				"&8&oShoots a bolt of ice energy;",
				"&8&oslows down anything it hits.",
				"&f",
				"&7Skill Tier: " + RPGSkill.skillTierNames[skillTier],
				"&7Class: " + classType.getClassName());
	}
	
	public void activate()
	{
		ArrayList<LivingEntity> hit = new ArrayList<LivingEntity>();
		Vector vector = player.getLocation().getDirection().normalize().multiply(0.75D);
		int multiplier = 0;
        player.getWorld().playSound(player.getEyeLocation(), Sound.BLOCK_ANVIL_LAND, 0.05F, 1.0F);
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
			RPGEvents.scheduleRunnable(new RPGEvents.PlaySoundEffect(point, Sound.BLOCK_GLASS_BREAK, 0.05F, 1.25F), multiplier);
			RPGEvents.scheduleRunnable(new RPGEvents.AOEDetectionCustom(hit, point, 1.25D, player, new Callable<Void>()
					{
						@Override
						public Void call() throws Exception {
							LivingEntity e = RPGEvents.customHit;
							int damage = RPlayer.varyDamage(getUnvariedDamage());
							new RPGEvents.ApplyDamage(player, e, damage).run();
							new RPGEvents.PlayEffect(Effect.STEP_SOUND, e, 79).run();
							CakeLibrary.addPotionEffectIfBetterOrEquivalent(e, new PotionEffect(PotionEffectType.SLOW, debuffLength, debuffLevel));
							return null;
						}
						
					}), multiplier);
		}
	}
}
