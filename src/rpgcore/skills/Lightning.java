package rpgcore.skills;

import java.util.ArrayList;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGEvents;
import rpgcore.player.RPlayer;

public class Lightning extends RPGSkill
{
	public final static String skillName = "Lightning";
	public final static boolean passiveSkill = false;
	public final static int skillTier = 2;
	public final static int castDelay = 10;
	public final static ClassType classType = ClassType.MAGE;
	public final static float damage = 3.1F;
	public final static int hits = 4;
	public final static int cooldown = 4;
	public final static FireworkEffect fe = 
			FireworkEffect.builder().with(Type.BURST).withColor(Color.YELLOW).withColor(Color.WHITE).withColor(Color.GRAY).build();
	public Lightning()
	{
		super(skillName, passiveSkill, castDelay, damage, classType, skillTier);
	}

	@Override
	public void instantiate(RPlayer player)
	{
		super.applyCooldown(player, cooldown);
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
		
		ArrayList<LivingEntity> hit = new ArrayList<LivingEntity>();
        player.getPlayer().getWorld().playSound(player.getPlayer().getEyeLocation(), Sound.BLOCK_STONE_BREAK, 1.0F, 0.5F);
        player.getPlayer().getWorld().playSound(target, Sound.ENTITY_LIGHTNING_IMPACT, 0.3F, 1.0F);
        player.getPlayer().getWorld().playSound(target, Sound.ENTITY_LIGHTNING_THUNDER, 0.3F, 1.0F);
        new RPGEvents.PlayLightningEffect(target).run();
        new RPGEvents.AOEDetectionAttackWithFireworkEffect(hit, target, 5, getUnvariedDamage(player), player.getPlayer(), fe).run();
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.QUARTZ, 1), 
				"&eLightning"),
				"&7Damage: " + (int) (damage * 100.0F) + "%",
				"&7Radius: 4 blocks",
				"&7Cooldown: " + cooldown + "s",
				"&f",
				"&8&oCalls forth a lightning",
				"&8&ostrike to hit an area.",
				"&f",
				"&7Skill Tier: " + RPGSkill.skillTierNames[skillTier],
				"&7Class: " + classType.getClassName());
	}
}
