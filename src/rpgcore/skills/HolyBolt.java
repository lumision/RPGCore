package rpgcore.skills;

import java.util.ArrayList;

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

public class HolyBolt extends RPGSkill
{
	public final static String skillName = "Holy Bolt";
	public final static boolean passiveSkill = false;
	public final static int skillTier = 1;
	public final static int castDelay = 10;
	public final static ClassType classType = ClassType.PRIEST;
	public final static float damage = 1.2F;
	public HolyBolt()
	{
		super(skillName, passiveSkill, castDelay, damage, classType, skillTier);
	}

	@Override
	public void instantiate(RPlayer player)
	{
		new HolyBoltE(this, player);
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(175, 1), 
				"&fHoly Bolt"),
				"&7Damage: " + (int) (damage * 100) + "%",
				"&7Interval: " + (castDelay / 20.0F) + "s",
				"&f",
				"&8&oShoots a beam of holy energy.",
				"&f",
				"&7Skill Tier: " + RPGSkill.skillTierNames[skillTier],
				"&7Class: " + classType.getClassName());
	}
	
	public static class HolyBoltE extends SkillEffect
	{
		Location origin;
		Vector vector;
		ArrayList<LivingEntity> hit;
		public HolyBoltE(RPGSkill skill, RPlayer player)
		{
			super(skill, player);
			
			origin = player.getPlayer().getEyeLocation();
			vector = player.getPlayer().getLocation().getDirection().normalize().multiply(0.75F).clone();
			hit = new ArrayList<LivingEntity>();
			
			player.getPlayer().getWorld().playSound(player.getPlayer().getEyeLocation(), Sound.BLOCK_ANVIL_LAND, 0.05F, 1.0F);
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
