package rpgcore.skills;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeAPI;
import rpgcore.main.RPGEvents;
import rpgcore.player.RPlayer;
import rpgcore.skillinventory.SkillInventory;

public class ArcaneBarrage extends RPGSkill
{
	public final static String skillName = "Arcane Barrage";
	public final static int castDelay = 20;
	public final static ClassType classType = ClassType.ARCHMAGE;
	public ArcaneBarrage(RPlayer caster)
	{
		super(skillName, caster, castDelay, calculateDamage(caster.getSkillLevel(skillName)), classType);
	}
	
	public ArcaneBarrage()
	{
		super(skillName, null, castDelay, 0, classType);
	}
	
	@Override
	public void insantiate(RPlayer rp)
	{
		new ArcaneBarrage(rp);
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
		return CakeAPI.addLore(CakeAPI.renameItem(unlocked ? new ItemStack(Material.FEATHER, 1) : SkillInventory.locked.clone(), 
				"&fA&7r&fc&7a&fn&7e &fB&7a&fr&7r&fa&7g&fe"),
				"&7Skill Level: " + (unlocked ? level : 0),
				"&7Damage/Projectile: " + (int) (calculateDamage(level) * 100) + "%",
				"&7Interval: 1s",
				"&f",
				"&8&oUnleashes a barrage of arcane",
				"&8&oprojectiles unto the target.",
				"&7Class: " + classType.getClassName());
	}

	public static double calculateDamage(int level)
	{
		return 2.8D + (level / 5.0D);
	}

	@Override
	public void activate()
	{
		Random rand = new Random();
		player.getWorld().playSound(player.getEyeLocation(), Sound.BLOCK_ANVIL_LAND, 0.1F, 1.0F);
		FireworkEffect fe = FireworkEffect.builder().with(Type.BURST).withColor(Color.WHITE).withColor(Color.GRAY).build();
		Location a = player.getTargetBlock(CakeAPI.getPassableBlocks(), 16).getLocation();
		for (int i = 0; i < 4; i++)
		{
			ArrayList<LivingEntity> hit = new ArrayList<LivingEntity>();
			int multiplier = 0;
			Location start = player.getLocation().add(rand.nextInt(7) - 3, 4 + rand.nextInt(4), rand.nextInt(7) - 3);
			Location b = a.clone().add(rand.nextInt(2) - 1 + 0.5D, 0, rand.nextInt(2) - 1 + 0.5D);
			Vector vector = new Vector(b.getX() - start.getX(), b.getY() - start.getY(), b.getZ() - start.getZ()).normalize().multiply(1.0D);
			double distance = start.distance(b);
			while (multiplier < distance * 2)
			{
				multiplier++;
				Location point = start.clone().add(vector.clone().multiply(multiplier));
				if (!CakeAPI.getPassableBlocks().contains(point.getBlock().getType()))
					break;
				RPGEvents.scheduleRunnable(new RPGEvents.FireworkTrail(point, 0.2F, 2), multiplier);
				if (i == 0)
					RPGEvents.scheduleRunnable(new RPGEvents.PlaySoundEffect(point, Sound.BLOCK_GLASS_BREAK, 0.1F, 1.25F), multiplier);
				RPGEvents.scheduleRunnable(new RPGEvents.AOEDetectionAttackWithFireworkEffect(hit, point, 1.25D, super.getUnvariedDamage(), player, fe), multiplier);
			}
		}
	}
}
