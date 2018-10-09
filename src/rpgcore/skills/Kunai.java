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

public class Kunai extends RPGSkill
{
	public final static String skillName = "Kunai";
	public final static boolean passiveSkill = false;
	public final static int skillTier = 1;
	public final static int castDelay = 10;
	public final static ClassType classType = ClassType.ASSASSIN;
	public final static float damage = 2.4F;
	public Kunai()
	{
		super(skillName, passiveSkill, castDelay, damage, classType, skillTier);
	}

	@Override
	public void instantiate(RPlayer player)
	{
		super.applyCooldown(player, 2);
		new KunaiE(this, player);
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.GOLDEN_CARROT, 1), 
				"&eKunai"),
				"&7Damage: " + (int) (damage * 100) + "%",
				"&7Cooldown: 2s",
				"&f",
				"&8&oThrows a kunai knife forward.",
				"&f",
				"&7Skill Tier: " + RPGSkill.skillTierNames[skillTier],
				"&7Class: " + classType.getClassName());
	}
	
	public static class KunaiE extends SkillEffect
	{
		Location origin;
		Vector vector;
		ArrayList<LivingEntity> hit;
		public KunaiE(RPGSkill skill, RPlayer player)
		{
			super(skill, player);
			
			origin = player.getPlayer().getEyeLocation();
			vector = player.getPlayer().getLocation().getDirection().normalize().multiply(0.75F).clone();
			hit = new ArrayList<LivingEntity>();

			player.getPlayer().getWorld().playSound(player.getPlayer().getEyeLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.2F, 1.0F);
		}

		@Override
		public boolean tick() 
		{
			if (tick < 32)
			{
				tick++;
				Location point = origin.clone().add(vector.clone().multiply(tick));
				if (hit.size() > 0 || !CakeLibrary.getPassableBlocks().contains(point.getBlock().getType()))
				{
					new RPGEvents.PlayEffect(Effect.STEP_SOUND, point, point.getBlock().getTypeId()).run();
					return true;
				}
				new RPGEvents.FireworkTrail(point, 0, 1).run();
				new RPGEvents.PlaySoundEffect(point, Sound.BLOCK_GLASS_BREAK, 0.05F, 1.25F).run();
				ArrayList<LivingEntity> splash = CakeLibrary.getNearbyLivingEntitiesExcludePlayers(point, 1.25D);
				if (splash.size() > 0)
				{
					for (LivingEntity entity: splash)
					{
						new RPGEvents.ApplyDamage(player.getPlayer(), entity, RPlayer.varyDamage(skill.getUnvariedDamage(player))).run();
						new RPGEvents.PlayEffect(Effect.STEP_SOUND, entity, 20).run();
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
