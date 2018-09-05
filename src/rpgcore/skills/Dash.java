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

public class Dash extends RPGSkill
{
	public final static String skillName = "Dash";
	public final static int castDelay = 10;
	public final static ClassType classType = ClassType.THIEF;
	public Dash(RPlayer caster)
	{
		super(skillName, caster, castDelay, 0, classType);
	}
	
	public Dash()
	{
		super(skillName, null, castDelay, 0, classType);
	}
	
	@Override
	public void insantiate(RPlayer rp)
	{
		new Dash(rp);
	}
	
	public static ItemStack getSkillItem(RPlayer player)
	{
		int level = player.getSkillLevel(skillName);
		boolean unlocked = level > 0;
		level += unlocked ? 0 : 1;
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.ARROW, 1), 
				"&eDash"),
				"&7Skill Level: " + (unlocked ? level : 0),
				"&7Distance: " + (int) (3 + (level / 2)) + " blocks",
				"&7Cooldown: 2s",
				"&f",
				"&8&oDashes forward in the direction",
				"&8&oyou face. Hold down &7&o[SNEAK] &8&oto",
				"&8&odash in the opposite direction.",
				"&7Class: " + classType.getClassName());
	}

	@Override
	public void activate()
	{
		Location b = null;
		int length = 3 + (caster.getSkillLevel(skillName) / 2);
		if (player.isSneaking())
		{
			Vector vector = player.getLocation().getDirection().setY(0);
			vector.setX(-vector.getX()).setZ(-vector.getZ()).normalize();
			for (int i = 1; i <= length; i++)
			{
				Location point = player.getLocation().add(vector.clone().multiply(i)).add(0.5f, 0, 0.5f);
				Location b1 = point.clone().add(0.5f, 1, 0.5f);
				if (!CakeLibrary.getPassableBlocks().contains(point.getBlock().getType()) || !CakeLibrary.getPassableBlocks().contains(b1.getBlock().getType()))
					break;
				b = point.add(0, -1, 0);
			}
		} else {
			b = player.getTargetBlock(CakeLibrary.getPassableBlocks(), length).getLocation();
		}
		if (b == null)
			return;
		Location b1 = b.clone().add(0, 1, 0);
		Location b2 = b.clone().add(0, 2, 0);
		if (!CakeLibrary.getPassableBlocks().contains(b1.getBlock().getType()) || !CakeLibrary.getPassableBlocks().contains(b2.getBlock().getType()))
		{
			player.sendMessage(CakeLibrary.recodeColorCodes("&c* You cannot dash into a block!"));
			return;
		}
		int yDiff = 0;
		for (int y = b.getBlockY(); y > 0; y--)
		{
			b.setY(y);
			yDiff++;
			if (!CakeLibrary.getPassableBlocks().contains(b.getBlock().getType()))
				break;
		}
		if (yDiff > 5)
		{
			player.sendMessage(CakeLibrary.recodeColorCodes("&c* The dropdown is too huge to dash to"));
			return;
		}
		Location start = player.getLocation();
		Location teleport = b.clone().add(0.5D, 1, 0.5D);
		teleport.setYaw(start.getYaw());
		teleport.setPitch(start.getPitch());
		teleport.getWorld().playEffect(teleport, Effect.STEP_SOUND, 20);
		teleport.getWorld().playEffect(teleport.clone().add(0, 1, 0), Effect.STEP_SOUND, 20);
		teleport.getWorld().playEffect(start, Effect.STEP_SOUND, 20);
		teleport.getWorld().playEffect(start.clone().add(0, 1, 0), Effect.STEP_SOUND, 20);
		player.teleport(teleport);
		caster.addInstantCast("Shadow Stab");
		super.applyCooldown(2);
	}
}
