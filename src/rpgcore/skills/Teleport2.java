package rpgcore.skills;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;
import rpgcore.player.RPlayer;

public class Teleport2 extends RPGSkill
{
	public final static String skillName = "Teleport II";
	public final static boolean passiveSkill = false;
	public final static int skillTier = 3;
	public final static int castDelay = 10;
	public final static ClassType classType = ClassType.MAGE;
	public Teleport2(RPlayer caster)
	{
		super(skillName, caster, passiveSkill, castDelay, 0, classType, skillTier, "Teleport");
	}

	@Override
	public void insantiate(RPlayer rp)
	{
		new Teleport2(rp);
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.FEATHER, 1), 
				"&bTeleport II"),
				"&7Distance: 16 blocks",
				"&7Cooldown: 2s",
				"&f",
				"&8&oTeleports forward into the",
				"&8&odirection you face. ",
				"&f",
				"&7Skill Tier: " + RPGSkill.skillTierNames[skillTier],
				"&7Class: " + classType.getClassName());
	}

	@Override
	public void activate()
	{
		Location b = null;
		int length = 16;
		b = player.getTargetBlock(CakeLibrary.getPassableBlocks(), length).getLocation();
		if (b == null)
			return;
		Location b1 = b.clone().add(0, 1, 0);
		Location b2 = b.clone().add(0, 2, 0);
		if (!CakeLibrary.getPassableBlocks().contains(b1.getBlock().getType()) || !CakeLibrary.getPassableBlocks().contains(b2.getBlock().getType()))
		{
			player.sendMessage(CakeLibrary.recodeColorCodes("&c* You cannot teleport into a block!"));
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
			player.sendMessage(CakeLibrary.recodeColorCodes("&c* The dropdown is too huge to teleport to"));
			return;
		}
		Location start = player.getLocation();
		Location teleport = b.clone().add(0.5D, 1, 0.5D);
		teleport.setYaw(start.getYaw());
		teleport.setPitch(start.getPitch());
		teleport.getWorld().playEffect(teleport, Effect.STEP_SOUND, 20);
		teleport.getWorld().playEffect(teleport.clone().add(0, 1, 0), Effect.STEP_SOUND, 20);
		teleport.getWorld().playEffect(teleport, Effect.ENDER_SIGNAL, 20);
		teleport.getWorld().playEffect(teleport.clone().add(0, 2, 0), Effect.ENDER_SIGNAL, 20);
		teleport.getWorld().playEffect(start, Effect.STEP_SOUND, 20);
		teleport.getWorld().playEffect(start.clone().add(0, 1, 0), Effect.STEP_SOUND, 20);
		teleport.getWorld().playEffect(start, Effect.ENDER_SIGNAL, 20);
		teleport.getWorld().playEffect(start.clone().add(0, 2, 0), Effect.ENDER_SIGNAL, 20);
		player.setVelocity(new Vector(0, 0, 0));
		player.setFallDistance(0);
		player.teleport(teleport);
		super.applyCooldown(2);
	}
}
