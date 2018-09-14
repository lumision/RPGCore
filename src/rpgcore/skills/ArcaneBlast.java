package rpgcore.skills;

import java.util.ArrayList;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGEvents;
import rpgcore.player.RPlayer;

public class ArcaneBlast extends RPGSkill
{
	public final static String skillName = "Arcane Blast";
	public final static boolean passiveSkill = false;
	public final static int skillTier = 1;
	public final static int castDelay = 20;
	public final static ClassType classType = ClassType.MAGE;
	public final static float damage = 1.7F;
	public final static int radius = 4;
	public ArcaneBlast(RPlayer caster)
	{
		super(skillName, caster, passiveSkill, castDelay, damage, classType, skillTier);
	}

	public ArcaneBlast()
	{
		super(skillName, null, passiveSkill, castDelay, 0, classType, skillTier);
	}

	@Override
	public void insantiate(RPlayer rp)
	{
		new ArcaneBlast(rp);
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.INK_SACK, 1, (short) 15), 
				"&dArcane Blast"),
				"&7Damage: " + (int) (damage * 100.0D) + "%",
				"&7Radius: " + radius + " blocks",
				"&7Interval: " + (castDelay / 20.0F) + "s",
				"&f",
				"&8&oBlast a plane of arcane energy",
				"&8&ounto the target area.",
				"&f",
				"&7Skill Tier: " + RPGSkill.skillTierNames[skillTier],
				"&7Class: " + classType.getClassName());
	}

	@Override
	public void activate()
	{
		Location target = player.getTargetBlock(CakeLibrary.getPassableBlocks(), 32).getLocation();
		
		int x = -radius;
		int z = 0;
		for (z = -radius; z < radius; z++)
		{
			Location l = target.clone().add(x, 1, z);
			RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, l, 20), z + radius);
		}
		
		z = -radius;
		for (x = -radius; x < radius; x++)
		{
			Location l = target.clone().add(x, 1, z);
			RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, l, 20), x + radius);
		}
		
		x = radius;
		for (z = radius; z > -radius; z--)
		{
			Location l = target.clone().add(x, 1, z);
			RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, l, 20), -z + radius);
		}
		
		z = radius;
		for (x = radius; x > -radius; x--)
		{
			Location l = target.clone().add(x, 1, z);
			RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, l, 20), -x + radius);
		}
		
		new RPGEvents.AOEDetectionAttackWithBlockBreakEffect(new ArrayList<LivingEntity>(), target, radius, getUnvariedDamage(), player, 20).run();

	}
}
