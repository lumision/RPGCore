package rpgcore.skills;

import java.util.ArrayList;

import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeAPI;
import rpgcore.main.RPGEvents;
import rpgcore.player.RPlayer;
import rpgcore.skillinventory.SkillInventory;

public class Heartspan extends RPGSkill
{
	public final static String skillName = "Heartspan";
	public final static int castDelay = 50;
	public final static ClassType classType = ClassType.THIEF;
	public Heartspan(RPlayer caster)
	{
		super(skillName, caster, castDelay, 0, classType);
	}
	
	public Heartspan()
	{
		super(skillName, null, castDelay, 0, classType);
	}
	
	@Override
	public void insantiate(RPlayer rp)
	{
		new Heartspan(rp);
	}
	
	public static ItemStack getSkillItem(RPlayer player)
	{
		int level = player.getSkillLevel(skillName);
		boolean unlocked = level > 0;
		level += unlocked ? 0 : 1;
		return CakeAPI.addLore(CakeAPI.renameItem(unlocked ? new ItemStack(Material.ARROW, 1) : SkillInventory.locked.clone(), 
				"&4H&7e&ca&4r&7t&cs&4p&7a&cn"),
				"&7Skill Level: " + (unlocked ? level : 0),
				"&7Damage per strike: " + (int) (calculateDamage(level) * 100) + "%",
				"&7Duration: " + calculateDuration(level) + "s",
				"&7Cooldown: 60s",
				"&f",
				"&8&oEnters a frenzy where you are",
				"&8&oable to constantly dash whilst",
				"&8&ostriking anything in your path.",
				"&c",
				"&8&oNote: Left-click to dash",
				"&7Class: " + classType.getClassName());
	}
	
	public static double calculateDuration(int level)
	{
		return 5.0D + (level / 2.0D);
	}

	public static double calculateDamage(int level)
	{
		return 2.8D + (level / 5.0D);
	}
	
	@Override
	public void activate()
	{
		caster.heartspanTicks = (int) calculateDuration(caster.getSkillLevel("Heartspan")) * 20;
		strike(caster);
		caster.getPlayer().sendMessage(CakeAPI.recodeColorCodes("&c**HEARTSPAN ACTIVATED**"));
		super.applyCooldown(60);
	}

	public static void strike(RPlayer caster)
	{
		Player player = caster.getPlayer();
		Location b = player.getTargetBlock(CakeAPI.getPassableBlocks(), 8).getLocation();
		Location b1 = b.clone().add(0, 1, 0);
		Location b2 = b.clone().add(0, 2, 0);
		if (!CakeAPI.getPassableBlocks().contains(b1.getBlock().getType()) || !CakeAPI.getPassableBlocks().contains(b2.getBlock().getType()))
			return;
		int yDiff = 0;
		for (int y = b.getBlockY(); y > 0; y--)
		{
			b.setY(y);
			yDiff++;
			if (!CakeAPI.getPassableBlocks().contains(b.getBlock().getType()))
				break;
		}
		if (yDiff > 5)
			return;
		Location start = player.getLocation();
		Location teleport = b.clone().add(0.5D, 1, 0.5D);
		teleport.setYaw(start.getYaw());
		teleport.setPitch(start.getPitch());
		
		//damage
		ArrayList<LivingEntity> hit = new ArrayList<LivingEntity>();
		FireworkEffect fe = FireworkEffect.builder().with(Type.BURST).withColor(Color.GRAY).withColor(Color.RED).build();
		Vector vector = teleport.clone().subtract(start).toVector().normalize();
		double distance = teleport.distance(start);
		for (int i = 1; i <= distance; i++)
		{
			Location check = start.clone().add(vector.clone().multiply(i));
			RPGEvents.scheduleRunnable(new RPGEvents.AOEDetectionAttackWithFireworkEffect(hit, check, 2.0D,
					(int) (caster.getDamageOfClass() * calculateDamage(caster.getSkillLevel("Heartspan"))), player, fe), 0);
			RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, check, 20), 0);
		}
		
		teleport.getWorld().playEffect(teleport, Effect.STEP_SOUND, 20);
		teleport.getWorld().playEffect(teleport.clone().add(0, 1, 0), Effect.STEP_SOUND, 20);
		teleport.getWorld().playEffect(start, Effect.STEP_SOUND, 20);
		teleport.getWorld().playEffect(start.clone().add(0, 1, 0), Effect.STEP_SOUND, 20);
		player.teleport(teleport);
		caster.castDelay = 5;
	}
}
