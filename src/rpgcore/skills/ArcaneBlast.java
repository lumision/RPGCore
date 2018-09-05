package rpgcore.skills;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;
import rpgcore.player.RPlayer;
import rpgcore.skillinventory.SkillInventory;

public class ArcaneBlast extends RPGSkill
{
	public final static String skillName = "Arcane Blast";
	public final static int castDelay = 10;
	public final static ClassType classType = ClassType.MAGE;
	public ArcaneBlast(RPlayer caster)
	{
		super(skillName, caster, castDelay, calculateDamage(caster.getSkillLevel(skillName)), classType);
	}
	
	public ArcaneBlast()
	{
		super(skillName, null, castDelay, 0, classType);
	}
	
	@Override
	public void insantiate(RPlayer rp)
	{
		new ArcaneBlast(rp);
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
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.FEATHER, 1), 
				"&fArcane Blast"),
				"&7Skill Level: " + (unlocked ? level : 0),
				"&7Damage: " + (int) (calculateDamage(level) * 100.0D) + "%",
				"&7Radius: " + calculateRadius(level) + " blocks",
				"&7Cooldown: 2s",
				"&f",
				"&8&oBlasts a plane of arcane energy",
				"&8&ounto the target area.",
				"&7Class: " + classType.getClassName());
	}
	
	public static double calculateDamage(int level)
	{
		return 1.0D + (level / 5.0D);
	}
	
	public static int calculateRadius(int level)
	{
		return (int) (2.0D + (level / 5.0D));
	}
	
	@Override
	public void activate()
	{
		super.applyCooldown(2.0D);

		Location target = player.getTargetBlock(CakeLibrary.getPassableBlocks(), 16).getLocation();
		
	}
}
