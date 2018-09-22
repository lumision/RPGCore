package rpgcore.skills;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import net.minecraft.server.v1_12_R1.EnumParticle;
import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGEvents;
import rpgcore.player.RPlayer;

public class Fireball extends RPGSkill
{
	public final static String skillName = "Fireball";
	public final static boolean passiveSkill = false;
	public final static int skillTier = 2;
	public final static int castDelay = 10;
	public final static ClassType classType = ClassType.MAGE;
	public final static float damage = 2.4F;
	public final static int debuffLength = 10 * 20;
	public Fireball(RPlayer caster)
	{
		super(skillName, caster, passiveSkill, castDelay, damage, classType, skillTier);
	}

	@Override
	public void insantiate(RPlayer rp)
	{
		new Fireball(rp);
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.MAGMA_CREAM, 1), 
				"&cFireball"),
				"&7Damage: " + (int) (damage * 100.0F) + "%",
				"&7Interval: " + (castDelay / 20.0F) + "s",
				"&f",
				"&7Damage Over Time:",
				"&7 * Fire",
				"&7 * Duration: " + (debuffLength / 20) + "s",
				"&f",
				"&8&oFire a ball of flaming",
				"&8&oarcane energy forward.",
				"&f",
				"&7Skill Tier: " + RPGSkill.skillTierNames[skillTier],
				"&7Class: " + classType.getClassName());
	}
	
	public void activate()
	{
		ArrayList<LivingEntity> hit = new ArrayList<LivingEntity>();
		Vector vector = player.getLocation().getDirection().normalize().multiply(0.75D);
		int multiplier = 0;
        player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_GHAST_SHOOT, 0.2F, 0.8F);
		while (multiplier < 20)
		{
			multiplier++;
			Location point = player.getEyeLocation().add(vector.clone().multiply(multiplier));
			if (!CakeLibrary.getPassableBlocks().contains(point.getBlock().getType()))
			{
				RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, point, point.getBlock().getTypeId()), multiplier);
				break;
			}
			RPGEvents.scheduleRunnable(new RPGEvents.ParticleEffect(EnumParticle.FLAME, point, 0.1F, 4), multiplier);
			RPGEvents.scheduleRunnable(new RPGEvents.PlaySoundEffect(point, Sound.BLOCK_FURNACE_FIRE_CRACKLE, 0.4F, 1.2F), multiplier);
			RPGEvents.scheduleRunnable(new RPGEvents.AOEDetectionCustom(hit, point, 1.25D, player, new Callable<Void>()
			{
				@Override
				public Void call() throws Exception {
					LivingEntity e = RPGEvents.customHit;
					int damage = RPlayer.varyDamage(getUnvariedDamage());
					new RPGEvents.ApplyDamage(player, e, damage).run();
					new RPGEvents.PlayEffect(Effect.STEP_SOUND, e, 11).run();
					e.setFireTicks(debuffLength);
					return null;
				}
				
			}), multiplier);
		}
	}
}
