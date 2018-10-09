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

public class TripleKunai extends RPGSkill
{
	public final static String skillName = "Triple Kunai";
	public final static boolean passiveSkill = false;
	public final static int skillTier = 4;
	public final static int castDelay = 10;
	public final static ClassType classType = ClassType.ASSASSIN;
	public final static float damage = 2.8F;
	public TripleKunai()
	{
		super(skillName, passiveSkill, castDelay, damage, classType, skillTier);
	}

	@Override
	public void instantiate(RPlayer player)
	{
		super.applyCooldown(player, 2);

		Vector vector1 = player.getPlayer().getLocation().getDirection();
		for (int i = 0; i < 3; i++)
		{
			ArrayList<LivingEntity> hit = new ArrayList<LivingEntity>();
			Vector vector = i == 0 ? vector1.clone() :
				i == 1 ? vector1.clone().setX(vector1.getX() - 0.25D).setZ(vector1.getZ() - 0.25D) :
					vector1.clone().setX(vector1.getX() + 0.25D).setZ(vector1.getZ() + 0.25D);
				vector.normalize().multiply(0.5D);
				int multiplier = 0;
				player.getPlayer().getWorld().playSound(player.getPlayer().getEyeLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.2F, 1.0F);
				while (multiplier < 24)
				{
					multiplier++;
					Location point = player.getPlayer().getEyeLocation().add(vector.clone().multiply(multiplier));
					if (!CakeLibrary.getPassableBlocks().contains(point.getBlock().getType()))
					{
						RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, point, point.getBlock().getTypeId()), multiplier);
						break;
					}
					RPGEvents.scheduleRunnable(new RPGEvents.FireworkTrail(point, 0, 1), multiplier / 3);
					if (i == 0)
						RPGEvents.scheduleRunnable(new RPGEvents.PlaySoundEffect(point, Sound.BLOCK_GLASS_BREAK, 0.1F, 1.25F), multiplier/ 3);
					RPGEvents.scheduleRunnable(new RPGEvents.AOEDetectionAttackWithBlockBreakEffect(hit, point, 0.75D, getUnvariedDamage(player), player.getPlayer(), 20), multiplier / 3);
				}
		}
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.GOLDEN_CARROT, 1), 
				"&cTriple Kunai"),
				"&7Damage: " + (int) (damage * 100) + "%",
				"&7Cooldown: 2s",
				"&f",
				"&8&oThrows 3 kunai knives forward.",
				"&f",
				"&7Skill Tier: " + RPGSkill.skillTierNames[skillTier],
				"&7Class: " + classType.getClassName());
	}
}
