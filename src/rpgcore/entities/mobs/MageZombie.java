package rpgcore.entities.mobs;

import java.util.ArrayList;

import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGCore;
import rpgcore.main.RPGEvents;

public class MageZombie extends RPGMonster
{
	public static double maxHealth = 90.0D;
	public static String name = "§cMage Zombie §7Lv. 9";

	public MageZombie(Monster entity)
	{
		super(entity);
		entity.setMaxHealth(maxHealth);
		entity.setHealth(maxHealth);
		entity.setCustomName(name);
		entity.setCustomNameVisible(true);

		entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
		entity.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0));
		
		ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
		LeatherArmorMeta meta = (LeatherArmorMeta) helmet.getItemMeta();
		meta.setColor(Color.fromBGR(64, 64, 255));
		helmet.setItemMeta(meta);
		
		EntityEquipment eq = entity.getEquipment();
		eq.setItemInMainHand(RPGCore.getItemFromDatabase("ZombieMageStaff").createItem());
		eq.setItemInMainHandDropChance(0);
		eq.setHelmet(helmet);
		eq.setHelmetDropChance(0);
	}

	@Override
	public void tick()
	{
		super.tick();
		if (isDead())
			return;
		if (castDelay > 0 || target == null)
			return;
		castArcaneBolt();
		castDelay = 60;
	}

	public void castArcaneBolt()
	{
		if (target.getLocation().distance(entity.getLocation()) > 15)
			return;
		ArrayList<LivingEntity> hit = new ArrayList<LivingEntity>();
		Vector vector = entity.getLocation().getDirection().normalize().multiply(0.5F);
		int multiplier = 0;
		entity.getWorld().playSound(entity.getEyeLocation(), Sound.BLOCK_ANVIL_LAND, 0.1F, 1.0F);
		while (multiplier < 30)
		{
			multiplier++;
			Location point = entity.getEyeLocation().add(vector.clone().multiply(multiplier));
			if (!CakeLibrary.getPassableBlocks().contains(point.getBlock().getType()))
			{
				RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, point, point.getBlock().getTypeId()), multiplier);
				break;
			}
			RPGEvents.scheduleRunnable(new RPGEvents.FireworkTrail(point, 0, 1), multiplier);
			RPGEvents.scheduleRunnable(new RPGEvents.PlaySoundEffect(point, Sound.BLOCK_GLASS_BREAK, 0.1F, 1.25F), multiplier);
			RPGEvents.scheduleRunnable(new RPGEvents.AOEDetectionAttackWithBlockBreakEffect(hit, point, 1.25D, 4, entity, 20), multiplier);
		}
	}
}
