package rpgcore.skills;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;
import rpgcore.player.RPlayer;
import rpgcore.skillinventory.SkillInventory;
import rpgcore.skills.effect.ArmageddonE;

public class Armageddon extends RPGSkill
{
	public final static String skillName = "Armageddon";
	public final static int castDelay = 100;
	public final static ClassType classType = ClassType.ARCHMAGE;
	public Armageddon(RPlayer caster)
	{
		super(skillName, caster, castDelay, calculateDamage(caster.getSkillLevel(skillName)), classType);
	}
	
	public Armageddon()
	{
		super(skillName, null, castDelay, 0, classType);
	}
	
	@Override
	public void insantiate(RPlayer rp)
	{
		new Armageddon(rp);
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
		return CakeLibrary.addLore(CakeLibrary.renameItem(unlocked ? new ItemStack(Material.FIREBALL, 1) : SkillInventory.locked.clone(), 
				"&9A&br&fm&9a&bg&fe&9d&bd&fo&9n"),
				"&7Skill Level: " + (unlocked ? level : 0),
				"&7Damage/Projectile: " + (int) (calculateDamage(level) * 100) + "%",
				"&7Cooldown: 60s",
				"&f",
				"&8&oUnleashes a barrage of arcane",
				"&8&oprojectiles unto the target.",
				"&7Class: " + classType.getClassName());
	}

	public static double calculateDamage(int level)
	{
		return 5.4D + (level * 2.2D);
	}

	@Override
	public void activate()
	{
		ArmageddonE.newEffect(this);
		super.applyCooldown(60);
	}
}
