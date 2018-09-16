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
import rpgcore.skills.effect.HellfireTerminusE;

public class HellfireTerminus extends RPGSkill
{
	public final static String skillName = "Hellfire Terminus";
	public final static boolean passiveSkill = false;
	public final static int skillTier = 13;
	public final static int castDelay = 100;
	public final static ClassType classType = ClassType.MAGE;
	public final static float damage = 12F;
	public final static int hits = 20;
	public HellfireTerminus(RPlayer caster)
	{
		super(skillName, caster, passiveSkill, castDelay, damage, classType, skillTier);
	}
	
	public HellfireTerminus()
	{
		super(skillName, null, passiveSkill, castDelay, 0, classType, skillTier);
	}
	
	@Override
	public void insantiate(RPlayer rp)
	{
		new HellfireTerminus(rp);
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.FIREBALL, 1), 
				"&4H&ce&el&4l&cf&ei&4r&ce &eT&4e&cr&em&4i&cn&eu&4s"),
				"&7Damage: " + (int) (damage * 100) + "% x " + hits + " Hits",
				"&7Cooldown: 5m",
				"&f",
				"&8&oForce a bind onto your target,",
				"&8&oand ravage it with an unmerciful",
				"&8&obarrage of arcane hellfire.",
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
		
		new HellfireTerminusE(this, target);
	}
}
