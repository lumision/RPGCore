package rpgcore.skills;

import java.util.ArrayList;

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
	public Fireball()
	{
		super(skillName, passiveSkill, castDelay, damage, classType, skillTier);
	}

	@Override
	public void instantiate(RPlayer player)
	{
		new FireballE(this, player);
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
	}
	
	public static class FireballE extends SkillEffect
	{
		Location origin;
		Vector vector;
		ArrayList<LivingEntity> hit;
		public FireballE(RPGSkill skill, RPlayer player)
		{
			super(skill, player);
			
			origin = player.getPlayer().getEyeLocation();
			vector = player.getPlayer().getLocation().getDirection().normalize().multiply(0.75F).clone();
			hit = new ArrayList<LivingEntity>();
			
			player.getPlayer().getWorld().playSound(player.getPlayer().getEyeLocation(), Sound.ENTITY_GHAST_SHOOT, 0.2F, 0.8F);
		}

		@Override
		public boolean tick() 
		{
			if (tick < 24)
			{
				tick++;
				Location point = origin.clone().add(vector.clone().multiply(tick));
				if (hit.size() > 0 || !CakeLibrary.getPassableBlocks().contains(point.getBlock().getType()))
				{
					new RPGEvents.PlayEffect(Effect.STEP_SOUND, point, point.getBlock().getTypeId()).run();
					return true;
				}
				new RPGEvents.ParticleEffect(EnumParticle.FLAME, point, 0.1F, 4).run();
				new RPGEvents.PlaySoundEffect(point, Sound.BLOCK_FURNACE_FIRE_CRACKLE, 0.4F, 1.2F).run();
				ArrayList<LivingEntity> splash = CakeLibrary.getNearbyLivingEntitiesExcludePlayers(point, 1.25D);
				if (splash.size() > 0)
				{
					splash = CakeLibrary.getNearbyLivingEntitiesExcludePlayers(point, 2.0D);
					new RPGEvents.ParticleEffect(EnumParticle.FLAME, point, 0.5F, 32).run();
					for (LivingEntity entity: splash)
					{
						new RPGEvents.ApplyDamage(player.getPlayer(), entity, RPlayer.varyDamage(skill.getUnvariedDamage(player))).run();
						new RPGEvents.PlayEffect(Effect.STEP_SOUND, entity, 11).run();
						entity.setFireTicks(debuffLength);
					}
					return true;
				}
			} else
				return true;
			tick++;
			return false;
		}
	}
}
