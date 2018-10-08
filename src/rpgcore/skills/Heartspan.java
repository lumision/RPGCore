package rpgcore.skills;

import java.util.ArrayList;

import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
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

public class Heartspan extends RPGSkill
{
	public final static String skillName = "Heartspan";
	public final static boolean passiveSkill = false;
	public final static int skillTier = 6;
	public final static int castDelay = 50;
	public final static ClassType classType = ClassType.ASSASSIN;
	public final static int duration = 10 * 20;
	public final static float damage = 4.8F;
	public Heartspan(RPlayer caster)
	{
		super(skillName, caster, passiveSkill, castDelay, 0, classType, skillTier);
	}

	@Override
	public void instantiate(RPlayer rp)
	{
		for (RPGSkill skill: rp.skillCasts)
			if (skill.skillName.equals(skillName))
			{
				skill.casterDamage = rp.getDamageOfClass();
				skill.caster.lastSkill = skillName;
				skill.caster.castDelays.put(skillName, (int) (castDelay * skill.caster.getStats().attackSpeedMultiplier));
				skill.caster.globalCastDelay = 1;
				skill.activate();
				return;
			}
		rp.skillCasts.add(new Heartspan(rp));
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.ARROW, 1), 
				"&4H&7e&ca&4r&7t&cs&4p&7a&cn"),
				"&7Damage per strike: " + (int) (damage * 100) + "%",
				"&7Duration: 10s",
				"&7Cooldown: 60s",
				"&f",
				"&8&oEnters a frenzy where you are",
				"&8&oable to constantly dash whilst",
				"&8&ostriking anything in your path.",
				"&c",
				"&8&oNote: Click to dash",
				"&f",
				"&7Skill Tier: " + RPGSkill.skillTierNames[skillTier],
				"&7Class: " + classType.getClassName());
	}
	
	@Override
	public void activate()
	{
		caster.heartspanTicks = duration;
		strike(caster);
		caster.getPlayer().sendMessage(CakeLibrary.recodeColorCodes("&c**HEARTSPAN ACTIVATED**"));
		super.applyCooldown(60);
	}

	public static void strike(RPlayer caster)
	{
		Player player = caster.getPlayer();
		Location b = player.getTargetBlock(CakeLibrary.getPassableBlocks(), 8).getLocation();
		Location b1 = b.clone().add(0, 1, 0);
		Location b2 = b.clone().add(0, 2, 0);
		if (!CakeLibrary.getPassableBlocks().contains(b1.getBlock().getType()) || !CakeLibrary.getPassableBlocks().contains(b2.getBlock().getType()))
			return;
		int yDiff = 0;
		for (int y = b.getBlockY(); y > 0; y--)
		{
			b.setY(y);
			yDiff++;
			if (!CakeLibrary.getPassableBlocks().contains(b.getBlock().getType()))
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
					(int) (caster.getDamageOfClass() * damage), player, fe), 0);
			RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, check, 20), 0);
		}
		
		teleport.getWorld().playEffect(teleport, Effect.STEP_SOUND, 20);
		teleport.getWorld().playEffect(teleport.clone().add(0, 1, 0), Effect.STEP_SOUND, 20);
		teleport.getWorld().playEffect(start, Effect.STEP_SOUND, 20);
		teleport.getWorld().playEffect(start.clone().add(0, 1, 0), Effect.STEP_SOUND, 20);
		player.teleport(teleport);
		caster.castDelays.put(skillName, 3);
	}
}
