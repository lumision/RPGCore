package rpgcore.skills;

import java.util.ArrayList;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import net.minecraft.server.v1_12_R1.EnumParticle;
import rpgcore.classes.RPGClass.ClassType;
import rpgcore.external.InstantFirework;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGEvents;
import rpgcore.player.RPlayer;

public class HellfireTerminus extends RPGSkill
{
	public final static String skillName = "Hellfire Terminus";
	public final static boolean passiveSkill = false;
	public final static int skillTier = 13;
	public final static int castDelay = 100;
	public final static ClassType classType = ClassType.MAGE;
	public final static float damage = 12F;
	public final static int hits = 20;
	public HellfireTerminus()
	{
		super(skillName, passiveSkill, castDelay, damage, classType, skillTier);
	}

	@Override
	public void instantiate(RPlayer player)
	{
		LivingEntity target = null;
		
		Vector direction = player.getPlayer().getLocation().getDirection().normalize();
		int check = 0;
		boolean b = false;
		while (check < 24)
		{
			check++;
			Location point1 = player.getPlayer().getEyeLocation().add(direction.clone().multiply(check));
			ArrayList<LivingEntity> nearby = CakeLibrary.getNearbyLivingEntities(point1, 1.0F);
			for (LivingEntity n: nearby)
				if (!(n instanceof Player))	
				{
					target = n;
					b = true;
					break;
				}
			if (b)
				break;
		}
		
		if (target == null)
		{
			player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.ENTITY_EGG_THROW, 0.2F, 0.5F);
			return;
		}
		
		new HellfireTerminusE(this, player, target);
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.FIREBALL, 1), 
				"&4H&ce&el&4l&cf&ei&4r&ce &eT&4e&cr&em&4i&cn&eu&4s"),
				"&7Damage: " + (int) (damage * 100) + "% x " + hits + " Hits",
				"&7Cooldown: 5m",
				"&f",
				"&8&oForce a bind onto your target,",
				"&8&oand ravage it with an unmerciful",
				"&8&obarrage of arcane hellfire.",
				"&f",
				"&7Skill Tier: " + RPGSkill.skillTierNames[skillTier],
				"&7Class: " + classType.getClassName());
	}
	
	public static class HellfireTerminusE extends SkillEffect
	{
		public static FireworkEffect fe = FireworkEffect.builder().with(Type.BURST).withColor(Color.RED).withColor(Color.YELLOW).build();
		public ArrayList<LivingEntity> hit = new ArrayList<LivingEntity>();
		public LivingEntity target;
		Location targetOrigin;
		Location[] orbs = new Location[4];
		
		public HellfireTerminusE(HellfireTerminus skill, RPlayer player, LivingEntity target)
		{
			super(skill, player);
			this.target = target;
			targetOrigin = target.getLocation();
			for (int i = 0; i < orbs.length; i++)
				orbs[i] = target.getLocation().add(4 - rand.nextInt(9), 6 + i * 2, 4 - rand.nextInt(9));
			
			skill.applyCooldown(player, 60 * 5);
		}

		public boolean tick()
		{
			target.teleport(targetOrigin);
			for (Location orb: orbs)
			{
				new RPGEvents.ParticleEffect(EnumParticle.FLAME, orb, 0.4F, Math.min(tick, 32)).run();
		        orb.getWorld().playSound(orb, Sound.ENTITY_GHAST_SHOOT, 0.2F, Math.min(1.5F, 0.5F + (tick / 32.0F)));
				
				if (tick < 32)
					continue;
				Location line = orb.clone().subtract(target.getLocation());
				Vector vector = line.toVector().normalize().multiply(0.5D);
				int length = (int) (line.getX() / vector.getX());
				
				int multiplier = 0;
				while (multiplier < length)
				{
					multiplier++;
					Location point = orb.clone().add(vector.clone().multiply(-multiplier));
					new RPGEvents.ParticleEffect(EnumParticle.FLAME, point, 0.1F, 2).run();
				}
			}
			
			if (tick == 32)
		        new RPGEvents.PlayLightningEffect(target).run();
			
			int damage;
			float pitch;
			if (tick >= 32 && tick % 2 == 0)
			{
				damage = RPlayer.varyDamage(skill.getUnvariedDamage(player));
				new RPGEvents.ApplyDamage(player.getPlayer(), target, damage).run();
				pitch = 1.5F - ((tick - 32.0F) / 38.0F);
		        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_LIGHTNING_IMPACT, 0.2F, pitch);
		        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_LIGHTNING_THUNDER, 0.2F, pitch);
				new InstantFirework(fe, target.getLocation());
				if (rand.nextInt(10) == 0 && tick != 32)
			        new RPGEvents.PlayLightningEffect(target).run();
			}
			
			tick++;
			if (tick > 70)
				return true;
			return false;
		}
	}
}
