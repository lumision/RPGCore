package rpgcore.entities.bosses;

import java.util.ArrayList;

import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import net.minecraft.server.v1_12_R1.EnumParticle;
import rpgcore.entities.mobs.RPGMonster;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGCore;
import rpgcore.main.RPGEvents;

public class UndeadEmperor extends RPGMonster
{
	public static double maxHealth = 10000.0D;
	public static String name = CakeLibrary.recodeColorCodes("&e&lUndead Emperor&7 Lv. 54");

	public UndeadEmperor(Monster m)
	{
		super(m, true);
		entity.setRemoveWhenFarAway(false);
		entity.setMaxHealth(maxHealth);
		entity.setHealth(maxHealth);
		entity.setCustomName(name);
		entity.setCustomNameVisible(true);

		entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 6));
		entity.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0));

		EntityEquipment eq = entity.getEquipment();
		if (RPGCore.heads.containsKey("skull"))
			eq.setHelmet(CakeLibrary.getSkullWithTexture(RPGCore.heads.get("skull")));
		eq.setHelmetDropChance(0.0F);
		eq.setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
		eq.setChestplateDropChance(0.0F);
		eq.setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
		eq.setLeggingsDropChance(0.0F);
		eq.setBoots(new ItemStack(Material.DIAMOND_BOOTS));
		eq.setBootsDropChance(0.0F);
	}

	@Override
	public boolean tick()
	{
		super.tick();
		
		if (isDead())
			return true;
		if (castDelay > 0 || target == null)
			return false;
		
		int r = rand.nextInt(10) + 1;
		if (r <= 7)
		{
			castArcaneBeam();
			castDelay = 10;
		} else if (r > 7 && r <= 9)
		{
			castLightningRain();
			castDelay = 30;
		} else
		{
			castSmash();
			castDelay = 30;
		}
		return false;
	}

	public void castLightningRain() //Fire particles and sound effects on every player's location which stay for about 1 second before exploding into a lightning strike
	{
		ArrayList<LivingEntity> hit = new ArrayList<LivingEntity>();
		FireworkEffect fe = FireworkEffect.builder().with(Type.BURST).withColor(Color.WHITE).withColor(Color.RED).build();
		int speed = 2;
		for (Player p: CakeLibrary.getNearbyPlayers(entity.getLocation(), 32.0D))
		{
			for (int i1 = 0; i1 < 3; i1++)
			{
				Location boom = p.getLocation().add(rand.nextInt(7) - 3, 1, rand.nextInt(7) - 3);
				int i = 0;
				for (; i < 7; i++)
				{
					RPGEvents.scheduleRunnable(new RPGEvents.PlaySoundEffect(boom, Sound.ENTITY_GHAST_SHOOT, 0.05F, 2.0F), i * speed);
					RPGEvents.scheduleRunnable(new RPGEvents.ParticleEffect(EnumParticle.FLAME, boom, 0.5F, 10), i * speed);
				}
				RPGEvents.scheduleRunnable(new RPGEvents.AOEDetectionAttackWithFireworkEffect(hit, boom, 2.0D, 8, entity, fe), i * speed);
				RPGEvents.scheduleRunnable(new RPGEvents.PlayLightningEffect(boom), i * speed);
			}
		}
	}

	public void castSmash()
	{
		entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20, 32767));
		ArrayList<LivingEntity> hit = new ArrayList<LivingEntity>();
		for (int i = 0; i < 20; i++)
			RPGEvents.scheduleRunnable(new RPGEvents.PlaySoundEffect(entity, Sound.ENTITY_GHAST_SHOOT, 0.2F, 1.0F + (i / 20.0F)), i);
		RPGEvents.scheduleRunnable(new RPGEvents.AOEDetectionAttack(hit, entity.getLocation(), 10.0D, 8, entity, 2.0D), 20);
		RPGEvents.scheduleRunnable(new RPGEvents.PlayExplosionEffect(entity.getLocation().add(0, 1.5D, 0)), 20);
	}

	public void castArcaneBeam()
	{
		ArrayList<LivingEntity> hit = new ArrayList<LivingEntity>();
		Vector vector = entity.getLocation().getDirection().normalize().multiply(0.75D);
		int multiplier = 0;
        entity.getWorld().playSound(entity.getEyeLocation(), Sound.BLOCK_ANVIL_LAND, 0.05F, 1.0F);
		while (multiplier < 20)
		{
			multiplier++;
			Location point = entity.getEyeLocation().add(vector.clone().multiply(multiplier));
			if (!CakeLibrary.getPassableBlocks().contains(point.getBlock().getType()))
			{
				RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, point, point.getBlock().getTypeId()), multiplier);
				break;
			}
			RPGEvents.scheduleRunnable(new RPGEvents.FireworkTrail(point, 0.1F, 3), multiplier);
			RPGEvents.scheduleRunnable(new RPGEvents.PlaySoundEffect(point, Sound.BLOCK_GLASS_BREAK, 0.05F, 1.25F), multiplier);
			RPGEvents.scheduleRunnable(new RPGEvents.AOEDetectionAttackWithBlockBreakEffect(hit, point, 1.25D, 5, entity, 20), multiplier);
		}
	}
	
	public ArrayList<ItemStack> getDrops()
	{
		ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
		return drops;
	}
}
