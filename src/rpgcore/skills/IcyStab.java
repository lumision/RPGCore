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

public class IcyStab extends RPGSkill
{
	public final static String skillName = "Icy Stab";
	public final static boolean passiveSkill = false;
	public final static int skillTier = 1;
	public final static int castDelay = 15;
	public final static ClassType classType = ClassType.WARRIOR;
	public final static float damage = 1.6F;
	public final static int debuffLevel = 1;
	public final static int debuffLength = 10 * 20;
	public IcyStab()
	{
		super(skillName, passiveSkill, castDelay, damage, classType, skillTier);
	}

	@Override
	public void instantiate(RPlayer player)
	{
		ArrayList<LivingEntity> hit = new ArrayList<LivingEntity>();
		Vector vector = player.getPlayer().getLocation().getDirection().normalize();
		int multiplier = 1;
		player.getPlayer().getWorld().playSound(player.getPlayer().getEyeLocation(), Sound.ENTITY_GHAST_SHOOT, 0.1F, 1.5F);
		while (multiplier < 7)
		{
			multiplier++;
			Location point = player.getPlayer().getEyeLocation().add(vector.clone().multiply(multiplier));
			if (!CakeLibrary.getPassableBlocks().contains(point.getBlock().getType()))
				break;
			RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, point, 79), multiplier);
			RPGEvents.scheduleRunnable(new RPGEvents.AOEDetectionCustom(hit, point, 1.25D, player.getPlayer(), new Callable<Void>()
			{
				@Override
				public Void call() throws Exception {
					LivingEntity e = RPGEvents.customHit;
					int damage = RPlayer.varyDamage(getUnvariedDamage(player));
					new RPGEvents.ApplyDamage(player.getPlayer(), e, damage).run();
					new RPGEvents.PlayEffect(Effect.STEP_SOUND, e, 79).run();
					CakeLibrary.addPotionEffectIfBetterOrEquivalent(e, new PotionEffect(PotionEffectType.SLOW, debuffLength, debuffLevel));
					return null;
				}
				
			}), multiplier);
		}
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.GOLD_SWORD, 1), 
				"&bIcy Stab"),
				"&7Damage: " + (int) (damage * 100) + "%",
				"&7Interval: " + (castDelay / 20.0F) + "s",
				"&f",
				"&7Debuff:",
				"&7 * Slow " + CakeLibrary.convertToRoman(debuffLevel + 1),
				"&7 * Duration: " + (debuffLength / 20) + "s",
				"&f",
				"&8&oSends a short ice-elemented",
				"&8&opierce forward.",
				"&f",
				"&7Skill Tier: " + RPGSkill.skillTierNames[skillTier],
				"&7Class: " + classType.getClassName());
	}
}
