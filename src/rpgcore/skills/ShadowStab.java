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
import rpgcore.skillinventory.SkillInventory;

public class ShadowStab extends RPGSkill
{
	public final static String skillName = "Shadow Stab";
	public final static int castDelay = 5;
	public final static ClassType classType = ClassType.THIEF;
	public ShadowStab(RPlayer caster)
	{
		super(skillName, caster, castDelay, calculateDamage(caster.getSkillLevel(skillName)), classType);
	}
	
	public ShadowStab()
	{
		super(skillName, null, castDelay, 0, classType);
	}
	
	@Override
	public void insantiate(RPlayer rp)
	{
		new ShadowStab(rp);
	}
	
	@Override 
	public ItemStack instanceGetSkillItem(RPlayer player)
	{
		return getSkillItem(player);
	}
	
	public static ItemStack getSkillItem(RPlayer player)
	{
		int level = player.getSkillLevel(skillName);
		boolean unlocked = level > 0;
		level += unlocked ? 0 : 1;
		return CakeLibrary.addLore(CakeLibrary.renameItem(unlocked ? new ItemStack(Material.STONE_SWORD, 1) : SkillInventory.locked.clone(), 
				"&fShadow Stab"),
				"&7Skill Level: " + (unlocked ? level : 0),
				"&7Damage: " + (int) (calculateDamage(level) * 100) + "%",
				"&7Interval: 0.25s",
				"&f",
				"&8&oShort-ranged attack that resets",
				"&8&othe cooldown of &7&o[Dash] &8&owhen",
				"&8&oa successful hit is landed.",
				"&7Class: " + classType.getClassName());
	}
	
	public static double calculateDamage(int level)
	{
		return 0.8D + (level / 20.0D);
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
			RPGEvents.scheduleRunnable(new RPGEvents.PlaySoundEffect(point, Sound.BLOCK_GLASS_BREAK, 0.1F, 1.25F), 0);
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
			caster.addInstantCast("Dash");
	}
}
