package rpgcore.skills;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGEvents;
import rpgcore.player.RPlayer;
import rpgcore.skillinventory.SkillInventory;

public class Propulsion extends RPGSkill
{
	public final static String skillName = "Propulsion";
	public final static int castDelay = 10;
	public final static ClassType classType = ClassType.MAGE;
	public Propulsion(RPlayer caster)
	{
		super(skillName, caster, castDelay, 0, classType);
	}
	
	public Propulsion()
	{
		super(skillName, null, castDelay, 0, classType);
	}
	
	@Override
	public void insantiate(RPlayer rp)
	{
		new Propulsion(rp);
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
		return CakeLibrary.addLore(CakeLibrary.renameItem(unlocked ? new ItemStack(Material.PISTON_BASE, 1) : SkillInventory.locked.clone(), 
				"&ePropulsion"),
				"&7Skill Level: " + (unlocked ? level : 0),
				"&7Radius: " + calculateRadius(level) + " blocks",
				"&7Cooldown: 8s",
				"&f",
				"&8&oKnocks back all monsters",
				"&8&owithin a set radius.",
				"&7Class: " + classType.getClassName());
	}

	public static double calculateRadius(int level)
	{
		switch (level)
		{
		case 1:
			return 2.0D;
		case 2:
			return 2.3D;
		case 3:
			return 2.6D;
		case 4:
			return 2.9D;
		case 5:
			return 3.3D;
		case 6:
			return 3.6D;
		case 7:
			return 3.9D;
		case 8:
			return 4.2D;
		case 9:
			return 4.5D;
		case 10:
			return 5.0D;
		default:
			return 2.0D;
		}
	}

	@Override
	public void activate()
	{
		super.applyCooldown(8.0D);
		double radius = calculateRadius(caster.getSkillLevel(skillName));
		Location origin = player.getLocation();
		for (LivingEntity e: CakeLibrary.getNearbyLivingEntities(player.getLocation(), radius))
		{
			if (e instanceof Player)
				continue;
			Location end = e.getLocation();
			Vector direction = new Vector(end.getX() - origin.getX(), 0.5D, end.getZ() - origin.getZ()).normalize().multiply(1.5D);
			RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, e, 20), 0);
			e.setVelocity(direction);
		}
	}
}
