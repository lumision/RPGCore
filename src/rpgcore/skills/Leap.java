package rpgcore.skills;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;
import rpgcore.player.RPlayer;
import rpgcore.skillinventory.SkillInventory;

public class Leap extends RPGSkill
{
	public final static String skillName = "Leap";
	public final static int castDelay = 10;
	public final static ClassType classType = ClassType.WARRIOR;
	public Leap(RPlayer caster)
	{
		super(skillName, caster, castDelay, 0, classType);
	}
	
	public Leap()
	{
		super(skillName, null, castDelay, 0, classType);
	}
	
	@Override
	public void insantiate(RPlayer rp)
	{
		new Leap(rp);
	}
	
	public static ItemStack getSkillItem(RPlayer player)
	{
		int level = player.getSkillLevel(skillName);
		boolean unlocked = level > 0;
		level += unlocked ? 0 : 1;
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.ARROW, 1), 
				"&eLeap"),
				"&7Skill Level: " + (unlocked ? level : 0),
				"&7Cooldown: " + getCooldown(level) + "s",
				"&f",
				"&8&oLeaps forward in the direction",
				"&8&oyou face. Hold down &7&o[SNEAK] &8&oto",
				"&8&oleap in the opposite direction.",
				"&7Class: " + classType.getClassName());
	}
	
	public static float getCooldown(int level)
	{
		return level < 3 ? 3 : level < 6 ? 2 : level < 10 ? 2 : 1;
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
		super.applyCooldown(getCooldown(caster.getSkillLevel(skillName)));
	}
}
