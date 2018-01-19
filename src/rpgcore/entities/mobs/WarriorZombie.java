package rpgcore.entities.mobs;

import java.util.ArrayList;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGCore;
import rpgcore.main.RPGEvents;

public class WarriorZombie extends CasterEntity
{
	public static double maxHealth = 140.0D;
	public static String name = "§cWarrior Zombie §7Lv. 10";

	public WarriorZombie(Monster entity)
	{
		super(entity);
		entity.setMaxHealth(maxHealth);
		entity.setHealth(maxHealth);
		entity.setCustomName(name);
		entity.setCustomNameVisible(true);

		entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 4));
		entity.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0));
		
		EntityEquipment eq = entity.getEquipment();
		eq.setItemInHand(RPGCore.instance.getItemFromDatabase("ZombieWarriorSword").createItem());
		eq.setItemInHandDropChance(0);
	}

	@Override
	public void tick()
	{
		super.tick();
		if (isDead())
			return;
		if (castDelay > 0 || target == null)
			return;
		castPowerPierce();
		castDelay = 40;
	}

	public void castPowerPierce()
	{
		if (target.getLocation().distance(entity.getLocation()) > 4)
			return;
		ArrayList<LivingEntity> hit = new ArrayList<LivingEntity>();
		Vector vector = entity.getLocation().getDirection().normalize();
		int multiplier = 1;
        entity.getWorld().playSound(entity.getEyeLocation(), Sound.ENTITY_GHAST_SHOOT, 0.2F, 1.5F);
		while (multiplier < 7)
		{
			multiplier++;
			Location point = entity.getEyeLocation().add(vector.clone().multiply(multiplier));
			if (!CakeLibrary.getPassableBlocks().contains(point.getBlock().getType()))
				break;
			RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, point, 20), multiplier);
			RPGEvents.scheduleRunnable(new RPGEvents.AOEDetectionAttackWithBlockBreakEffect(hit, point, 1.25D, 4, entity, 20), multiplier);
		}
	}
}
