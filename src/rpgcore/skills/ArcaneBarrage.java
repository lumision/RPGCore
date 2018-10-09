package rpgcore.skills;

import java.util.ArrayList;

import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGEvents;
import rpgcore.player.RPlayer;

public class ArcaneBarrage extends RPGSkill
{
	public final static String skillName = "Arcane Barrage";
	public final static boolean passiveSkill = false;
	public final static int skillTier = 5;
	public final static int castDelay = 20;
	public final static ClassType classType = ClassType.MAGE;
	public final static float damage = 4.8F;
	public final static int hits = 4;
	public final static FireworkEffect fe = 
			FireworkEffect.builder().with(Type.BURST).withColor(Color.WHITE).withColor(Color.GRAY).build();
	public ArcaneBarrage()
	{
		super(skillName, passiveSkill, castDelay, damage, classType, skillTier);
	}

	@Override
	public void instantiate(RPlayer player)
	{
		player.getPlayer().getWorld().playSound(player.getPlayer().getEyeLocation(), Sound.BLOCK_ANVIL_LAND, 0.1F, 1.0F);
		
		Location target = player.getPlayer().getTargetBlock(CakeLibrary.getPassableBlocks(), 16).getLocation();
		
		Vector direction = player.getPlayer().getLocation().getDirection().normalize();
		int check = 0;
		boolean b = false;
		while (check < 16)
		{
			check++;
			Location point1 = player.getPlayer().getEyeLocation().add(direction.clone().multiply(check));
			ArrayList<LivingEntity> nearby = CakeLibrary.getNearbyLivingEntities(point1, 1.0F);
			for (LivingEntity n: nearby)
				if (!(n instanceof Player))	
				{
					b = true;
					target = n.getEyeLocation();
					break;
				}
			if (b)
				break;
		}
		
		
		
		for (int i = 0; i < hits; i++)
		{
			ArrayList<LivingEntity> hit = new ArrayList<LivingEntity>();
			int multiplier = 0;
			Location start = player.getPlayer().getLocation().add(rand.nextInt(7) - 3, 4 + rand.nextInt(4), rand.nextInt(7) - 3);
			Location b1 = target.clone().add(rand.nextInt(2) - 1 + 0.5D, 0, rand.nextInt(2) - 1 + 0.5D);
			Vector vector = new Vector(b1.getX() - start.getX(), b1.getY() - start.getY(), b1.getZ() - start.getZ()).normalize().multiply(1.0D);
			double distance = start.distance(b1);
			while (multiplier < distance * 2)
			{
				multiplier++;
				Location point = start.clone().add(vector.clone().multiply(multiplier));
				if (!CakeLibrary.getPassableBlocks().contains(point.getBlock().getType()))
				{
					RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, point, point.getBlock().getTypeId()), multiplier);
					break;
				}
				RPGEvents.scheduleRunnable(new RPGEvents.FireworkTrail(point, 0.1F, 2), multiplier);
				if (i == 0)
					RPGEvents.scheduleRunnable(new RPGEvents.PlaySoundEffect(point, Sound.BLOCK_GLASS_BREAK, 0.1F, 1.25F), multiplier);
				RPGEvents.scheduleRunnable(new RPGEvents.AOEDetectionAttackWithFireworkEffect(hit, point, 1.25D, super.getUnvariedDamage(player), player.getPlayer(), fe), multiplier);
			}
		}
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.FEATHER, 1), 
				"&fA&7r&fc&7a&fn&7e &fB&7a&fr&7r&fa&7g&fe"),
				"&7Damage: " + (int) (damage * 100.0F) + "% x " + hits + " Hits",
				"&7Interval: " + (castDelay / 20.0F) + "s",
				"&f",
				"&8&oUnleash a barrage of arcane",
				"&8&oprojectiles unto the target.",
				"&f",
				"&7Skill Tier: " + RPGSkill.skillTierNames[skillTier],
				"&7Class: " + classType.getClassName());
	}
}
