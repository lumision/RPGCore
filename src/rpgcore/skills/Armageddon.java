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

public class Armageddon extends RPGSkill
{
	public final static String skillName = "Armageddon";
	public final static boolean passiveSkill = false;
	public final static int skillTier = 7;
	public final static int castDelay = 100;
	public final static ClassType classType = ClassType.MAGE;
	public final static float damage = 24.8F;
	public final static int radius = 16;
	public Armageddon()
	{
		super(skillName, passiveSkill, castDelay, damage, classType, skillTier);
	}

	@Override
	public void instantiate(RPlayer player)
	{
		new ArmageddonE(this, player);
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.FIREBALL, 1), 
				"&9A&br&fm&9a&bg&fe&9d&bd&fo&9n"),
				"&7Damage/Projectile: " + (int) (damage * 100) + "%",
				"&7Radius: " + radius + " blocks",
				"&7Cooldown: 60s",
				"&f",
				"&8&oUnleashes a barrage of arcane",
				"&8&oprojectiles unto the target.",
				"&f",
				"&7Skill Tier: " + RPGSkill.skillTierNames[skillTier],
				"&7Class: " + classType.getClassName());
	}
	
	public static class ArmageddonE extends SkillEffect
	{
		public static FireworkEffect fe = FireworkEffect.builder().with(Type.BURST).withColor(Color.WHITE).withColor(Color.GRAY).build();
		public ArrayList<LivingEntity> hit = new ArrayList<LivingEntity>();
		public Location origin;
		public ArrayList<Location> offset = new ArrayList<Location>();

		public ArmageddonE(Armageddon skill, RPlayer player)
		{
			super(skill, player);
			ArrayList<LivingEntity> nearby = CakeLibrary.getNearbyLivingEntities(player.getPlayer().getLocation(), Armageddon.radius);
			this.origin = player.getPlayer().getLocation().clone().add(0, 3, 0);
			for (LivingEntity e: nearby)
			{
				if (hit.size() > 16)
					break;
				if (e instanceof Player)
					continue;
				if (hit.contains(e))
					continue;
				hit.add(e);
				offset.add(new Location(player.getPlayer().getWorld(), rand.nextInt(5) - 2, rand.nextInt(3) + 8, rand.nextInt(5) - 2));
			}
			if (hit.size() <= 0)
			{
				player.getPlayer().sendMessage(CakeLibrary.recodeColorCodes("&cNo nearby monsters."));
				tick = 32767;
				return;
			}
			skill.applyCooldown(player, 60);
		}

		@Override
		public boolean tick()
		{
			if (tick <= 40 && tick % 2 == 0)
				new RPGEvents.PlaySoundEffect(player.getPlayer(), Sound.BLOCK_ANVIL_LAND, 0.2F, 0.5F + (tick / 40F)).run();
			
			int damage;
			for (int index = 0; index < hit.size(); index++)
			{
				LivingEntity e = hit.get(index);
				if (e.isDead() || e.getHealth() <= 0)
					continue;
				if (tick == 41)
				{
					for (int i = 0; i < 8; i++)
					{
						Vector movement = e.getLocation().add(offset.get(index)).subtract(e.getLocation()).toVector().multiply(i / 8D);
						Location point = e.getLocation().add(movement);
						new RPGEvents.ParticleEffect(EnumParticle.BLOCK_DUST, point, 0.2F, 32, 0, 155).run();
					}
				} else if (tick == 43) {
					damage = RPlayer.varyDamage(skill.getUnvariedDamage(player));
					new RPGEvents.ApplyDamage(player.getPlayer(), e, damage).run();
					new RPGEvents.PlayLightningEffect(e).run();
					new InstantFirework(fe, e.getLocation());
				} else if (tick % 2 == 0) {
					Vector movement = e.getLocation().add(offset.get(index)).subtract(origin).toVector().multiply(tick / 40D);
					Location point = origin.clone().add(movement);
					new RPGEvents.ParticleEffect(EnumParticle.BLOCK_DUST, point, 0.2F, 32, 0, 20).run();
					//new RPGEvents.ParticleEffect(EnumParticle.REDSTONE, point, 0.1F, 4, 0, 1, 1, 1, 1).run();
				}
			}


			tick++;
			if (tick > 43)
				return true;
			return false;
		}
	}
}
