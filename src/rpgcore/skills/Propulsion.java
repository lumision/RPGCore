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

public class Propulsion extends RPGSkill
{
	public final static String skillName = "Propulsion";
	public final static int skillTier = 1;
	public final static int castDelay = 10;
	public final static ClassType classType = ClassType.MAGE;
	public final static int radius = 4;
	public Propulsion(RPlayer caster)
	{
		super(skillName, caster, castDelay, 0, classType, skillTier);
	}
	
	public Propulsion()
	{
		super(skillName, null, castDelay, 0, classType, skillTier);
	}
	
	@Override
	public void insantiate(RPlayer rp)
	{
		new Propulsion(rp);
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.PISTON_BASE, 1), 
				"&ePropulsion"),
				"&7Radius: " + radius + " blocks",
				"&7Cooldown: 8s",
				"&f",
				"&8&oKnocks back all monsters",
				"&8&owithin a set radius.",
				"&f",
				"&7Skill Tier: " + CakeLibrary.convertToRoman(skillTier),
				"&7Class: " + classType.getClassName());
	}

	@Override
	public void activate()
	{
		super.applyCooldown(8.0D);
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
