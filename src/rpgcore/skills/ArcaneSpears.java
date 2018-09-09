package rpgcore.skills;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGEvents;
import rpgcore.player.RPlayer;

public class ArcaneSpears extends RPGSkill
{
	public final static String skillName = "Arcane Spears";
	public final static boolean passiveSkill = false;
	public final static int skillTier = 1;
	public final static int castDelay = 10;
	public final static ClassType classType = ClassType.MAGE;
	public final static float damage = 0.8F;
	public final static int hits = 4;
	public final static int cooldown = 4;
	public ArcaneSpears(RPlayer caster)
	{
		super(skillName, caster, passiveSkill, castDelay, damage, classType, skillTier);
	}

	public ArcaneSpears()
	{
		super(skillName, null, passiveSkill, castDelay, 0, classType, skillTier);
	}

	@Override
	public void insantiate(RPlayer rp)
	{
		new ArcaneSpears(rp);
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.ARROW, 1), 
				"&dArcane Spears"),
				"&7Damage: " + (int) (damage * 100.0F) + "% x " + hits + " Hits",
				"&7Cooldown: " + cooldown + "s",
				"&f",
				"&8&oRains arcane spears on",
				"&8&othe target block.",
				"&f",
				"&7Skill Tier: " + RPGSkill.skillTierNames[skillTier],
				"&7Class: " + classType.getClassName());
	}
	
	public void activate()
	{
		super.applyCooldown(cooldown);
		Location target = player.getTargetBlock(CakeLibrary.getPassableBlocks(), 16).getLocation();
		
		for (int i = 0; i < hits; i++)
		{
			ArrayList<LivingEntity> hit = new ArrayList<LivingEntity>();
			Location start = target.clone().add
					(rand.nextInt(3) - rand.nextInt(3), 5 + rand.nextInt(5), rand.nextInt(3) - rand.nextInt(3));
			Vector vector = target.clone().subtract(start).toVector().normalize().multiply(0.5F);
			
			int multiplier = 0;
			int length = (int) start.distance(target) * 2;
	        player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_GHAST_SHOOT, 0.2F, 1.0F);
	        
			while (multiplier < length)
			{
				multiplier++;
				Location point = start.clone().add(vector.clone().multiply(multiplier));
				if (!CakeLibrary.getPassableBlocks().contains(point.getBlock().getType()))
					break;
				RPGEvents.scheduleRunnable(new RPGEvents.FireworkTrail(point, 0, 1), multiplier);
				RPGEvents.scheduleRunnable(new RPGEvents.PlaySoundEffect(point, Sound.BLOCK_GLASS_BREAK, 0.05F, 1.25F), multiplier);
				RPGEvents.scheduleRunnable(new RPGEvents.AOEDetectionAttackWithBlockBreakEffect(hit, point, 1.0D, getUnvariedDamage(), player, 20), multiplier);
			}
		}
	}
}
