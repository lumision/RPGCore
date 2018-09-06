package rpgcore.skills;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;
import rpgcore.player.RPlayer;

public class Leap extends RPGSkill
{
	public final static String skillName = "Leap";
	public final static int skillTier = 2;
	public final static int castDelay = 10;
	public final static ClassType classType = ClassType.WARRIOR;
	public final static int cooldown = 2;
	public Leap(RPlayer caster)
	{
		super(skillName, caster, castDelay, 0, classType, skillTier);
	}
	
	public Leap()
	{
		super(skillName, null, castDelay, 0, classType, skillTier);
	}
	
	@Override
	public void insantiate(RPlayer rp)
	{
		new Leap(rp);
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.ARROW, 1), 
				"&eLeap"),
				"&7Cooldown: " + cooldown + "s",
				"&f",
				"&8&oLeaps forward in the direction",
				"&8&oyou face. Hold down &7&o[SNEAK] &8&oto",
				"&8&oleap in the opposite direction.",
				"&f",
				"&7Skill Tier: " + CakeLibrary.convertToRoman(skillTier),
				"&7Class: " + classType.getClassName());
	}

	@Override
	public void activate()
	{
		Location location = player.getLocation();
		Vector vector = location.getDirection();
		if (player.isSneaking())
			vector.multiply(-1f);
		vector.setY(0.5f);
		player.setVelocity(vector);
		player.setFallDistance(0);
		location.getWorld().playEffect(location, Effect.STEP_SOUND, 20);
		super.applyCooldown(cooldown);
	}
}
