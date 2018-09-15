package rpgcore.skills;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;
import rpgcore.player.RPlayer;
import rpgcore.skills.effect.ArmageddonE;
import rpgcore.skills.effect.InfinityTerminusE;

public class InfinityTerminus extends RPGSkill
{
	public final static String skillName = "Infinity Terminus";
	public final static boolean passiveSkill = false;
	public final static int skillTier = 13;
	public final static int castDelay = 100;
	public final static ClassType classType = ClassType.MAGE;
	public final static float damage = 12F;
	public final static int hits = 20;
	public InfinityTerminus(RPlayer caster)
	{
		super(skillName, caster, passiveSkill, castDelay, damage, classType, skillTier);
	}
	
	public InfinityTerminus()
	{
		super(skillName, null, passiveSkill, castDelay, 0, classType, skillTier);
	}
	
	@Override
	public void insantiate(RPlayer rp)
	{
		new InfinityTerminus(rp);
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.FIREBALL, 1), 
				"&fI&7n&8f&7i&fn&7i&8t&7y &fT&7e&8r&7m&fi&7n&8u&7s"),
				"&7Damage: " + (int) (damage * 100) + "% x " + hits + " Hits",
				"&7Cooldown: 5m",
				"&f",
				"&8&oForce a bind onto your target,",
				"&8&oand ravage it with an unmerciful",
				"&8&obarrage of arcane release.",
				"&f",
				"&7Skill Tier: " + RPGSkill.skillTierNames[skillTier],
				"&7Class: " + classType.getClassName());
	}

	@Override
	public void activate()
	{
		LivingEntity target = null;
		
		Vector direction = player.getLocation().getDirection().normalize();
		int check = 0;
		boolean b = false;
		while (check < 24)
		{
			check++;
			Location point1 = player.getEyeLocation().add(direction.clone().multiply(check));
			ArrayList<LivingEntity> nearby = CakeLibrary.getNearbyLivingEntities(point1, 1.0F);
			for (LivingEntity n: nearby)
				if (!(n instanceof Player))	
				{
					target = n;
					b = true;
					break;
				}
			if (b)
				break;
		}
		
		if (target == null)
		{
			player.playSound(player.getLocation(), Sound.ENTITY_EGG_THROW, 0.2F, 0.5F);
			return;
		}
		
		new InfinityTerminusE(this, target);
	}
}
