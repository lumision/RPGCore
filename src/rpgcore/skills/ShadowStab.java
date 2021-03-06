package rpgcore.skills;

import java.util.ArrayList;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGEvents;
import rpgcore.player.RPlayer;

public class ShadowStab extends RPGSkill
{
	public final static String skillName = "Shadow Stab";
	public final static boolean passiveSkill = false;
	public final static int skillTier = 1;
	public final static int castDelay = 5;
	public final static ClassType classType = ClassType.ASSASSIN;
	public final static float damage = 1.3F;
	public ShadowStab(RPlayer caster)
	{
		super(skillName, caster, passiveSkill, castDelay, damage, classType, skillTier);
	}
	
	public ShadowStab()
	{
		super(skillName, null, passiveSkill, castDelay, 0, classType, skillTier);
	}
	
	@Override
	public void insantiate(RPlayer rp)
	{
		new ShadowStab(rp);
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.STONE_SWORD, 1), 
				"&fShadow Stab"),
				"&7Damage: " + (int) (damage * 100) + "%",
				"&7Interval: 0.25s",
				"&f",
				"&8&oShort-ranged attack that resets",
				"&8&othe cooldown of &7&o[Dash] &8&owhen",
				"&8&oa successful hit is landed.",
				"&f",
				"&7Skill Tier: " + RPGSkill.skillTierNames[skillTier],
				"&7Class: " + classType.getClassName());
	}
	
	@Override
	public void activate()
	{
		
		ArrayList<LivingEntity> hit = new ArrayList<LivingEntity>();
		Vector vector = player.getLocation().getDirection().normalize().multiply(0.5D);
		int multiplier = 0;
        player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.2F, 1.0F);
        boolean resetDash = false;
		while (multiplier < 10)
		{
			multiplier++;
			Location point = player.getEyeLocation().add(vector.clone().multiply(multiplier));
			if (!CakeLibrary.getPassableBlocks().contains(point.getBlock().getType()))
				break;
			RPGEvents.scheduleRunnable(new RPGEvents.FireworkTrail(point, 0, 1), 0);
			RPGEvents.scheduleRunnable(new RPGEvents.PlaySoundEffect(point, Sound.BLOCK_GLASS_BREAK, 0.05F, 1.25F), 0);
			for (LivingEntity e: CakeLibrary.getNearbyLivingEntities(point, 0.75D))
			{
				if (e instanceof Player)
					continue;
				if (hit.contains(e))
					continue;
				hit.add(e);
				RPGEvents.scheduleRunnable(new RPGEvents.ApplyDamage(player, e, RPlayer.varyDamage(getUnvariedDamage())), 0);
				RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, e, 20), 0);
				resetDash = true;
			}
		}
		if (resetDash)
		{
			caster.cooldowns.remove(Dash.skillName);
			caster.castDelays.remove(Dash.skillName);
		}
	}
}
