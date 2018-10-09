package rpgcore.skills;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.entities.mobs.RPGMonster;
import rpgcore.main.CakeLibrary;
import rpgcore.player.RPlayer;

public class RPGSkill 
{
	public double baseDamageMultiplier;
	public boolean passiveSkill;
	public String skillName;
	public String cooldownName;
	public int skillTier;
	public ClassType classType;
	public static Random rand = new Random();

	public static final RPGSkill[] skillList = { //ADDSKILL

			new HellfireTerminus(),
			new Armageddon(),
			new Heartspan(),
			new TripleKunai(),
			//new Supernova(),
			//new BlackHole(),
			//new Asteroid(),

			//MAGE TIER 1
			new MagicMastery1(),
			new Wisdom(),
			new ArcaneBolt(),
			new WindDrive(),
			new ArcaneBlast(),
			new IceBolt(),
			new ArcaneSpears(),
			new PoisonBolt(),
			
			//MAGE TIER 2
			new Teleport1(),
			new Accelerate(),
			new Lightning(),
			new ArcaneBeam(),
			new IceField(),
			new Fireball(),
			
			//MAGE TIER 3
			new Teleport2(),
			new MagicMastery2(),
			new ArcaneStorm(),
			
			//MAGE TIER 4
			new Sunfire(),
			
			//MAGE TIER 5
			new MagicMastery3(),
			new ArcaneBarrage(),
			
			//--------------------
			
			//WARRIOR TIER 1
			new IronBody(),
			new PowerPierce(),
			new Leap(),
			new PowerSlash1(),
			new Tenacity1(),
			new ShieldBash(),
			new IcyStab(),
			new Warcry(),
			
			//WARRIOR TIER 2
			new Enrage(),
			new PowerSlash2(),
			new FieryThrust(),
			
			//WARRIOR TIER 3
			new Tenacity2(),
			new TurtleShield(),
			
			//WARRIOR TIER 5
			new Tenacity3(),

			//--------------------

			//PRIEST TIER 1
			new HolyBolt(),
			new Heal1(),
			new Bless(),
			new Protect(),
			
			//PRIEST TIER 2
			new Heal2(),
			new Absolve(),
			
			//PRIEST TIER 4
			new Heal3(),
			new Enlightenment(),
			
			//--------------------
			
			//ASSASSIN TIER 1
			new ShadowStab1(),
			new Dash1(),
			new Bind1(),
			new QuickSlash1(),
			new BladeMastery1(),
			new Kunai(),
			new LightFeet(),

			//ASSASSIN TIER 2
			new BladeMastery2(),
			new BladeStorm(),
			new ShadowStab2(),
			new Dash2(),

			//ASSASSIN TIER 3
			new BladeMastery3(),
			
			//ASSASSIN TIER 3

			//--------------------

			//MISC
			new Vitality1(),
			new Vitality2(),
			new Vitality3(),
			new Vitality4(),
			new Vitality5(),
			new Vitality6(),
			new Vitality7(),
			new Vitality8(),
			new Vitality9(),
			new Vitality10(),
			
			new Vigor1(),
			new Vigor2(),
			new Vigor3(),
			new Vigor4(),
			new Vigor5(),

			new CelestialBlessing(),

	};

	public static final String[] skillTierNames = {
			"",
			"I",
			"II",
			"III",
			"IV",
			"V",
			"VI",
			"VII",
			"VIII",
			"IX",
			"X",
			CakeLibrary.recodeColorCodes("&c&lAlpha"),
			CakeLibrary.recodeColorCodes("&e&lDelta"),
			CakeLibrary.recodeColorCodes("&b&m&lZ&b&leta"),
	};

	public static RPGSkill getSkill(String skillName)
	{
		for (RPGSkill skill: skillList)
			if (skill.skillName.equals(skillName))
				return skill;
		return null;
	}

	public RPGSkill(String skillName, boolean passiveSkill, int castDelay, double baseDamageMultiplier, ClassType classType, int skillTier)
	{
		this.passiveSkill = passiveSkill;
		this.skillName = skillName;
		this.cooldownName = skillName;
		this.classType = classType;
		this.skillTier = skillTier;
	}

	public RPGSkill(String skillName, boolean passiveSkill, int castDelay, double baseDamageMultiplier, ClassType classType, int skillTier, String cooldownName)
	{
		this.passiveSkill = passiveSkill;
		this.skillName = skillName;
		this.cooldownName = cooldownName;
		this.classType = classType;
		this.skillTier = skillTier;
	}
	
	public ItemStack getSkillbook()
	{
		ItemStack skillItem = getSkillItem();
		skillItem = CakeLibrary.renameItem(skillItem, "&eSkillbook &6< " + CakeLibrary.getItemName(skillItem) + "&6 >");
		skillItem.setType(Material.ENCHANTED_BOOK);
		return skillItem;
	}

	public ItemStack instanceGetSkillItem(RPlayer player)
	{
		return null;
	}

	public void applyCooldown(RPlayer player, float seconds)
	{
		int reduction = player.getStats().cooldownReductionAdd;
		double total = seconds * 20.0F;
		total -= total / 100.0F * reduction;
		if (total <= 0)
			return;
		player.cooldowns.put(cooldownName, (int) total);
	}

	public int getUnvariedDamage(RPlayer player)
	{
		return (int) (player.getDamageOfClass() * baseDamageMultiplier);
	}

	public void activate() {}
	public ItemStack getSkillItem() { return null; }

	public void instantiate(RPlayer player) {}
	
	public static abstract class SkillEffect 
	{
		public static ArrayList<SkillEffect> skillEffects = new ArrayList<SkillEffect>();
		public static final Random rand = new Random();
		
		public RPGSkill skill;
		public RPlayer player;
		public RPGMonster mob;
		public int tick;
		
		public SkillEffect(RPGSkill skill, RPlayer player)
		{
			this.skill = skill;
			this.player = player;
			skillEffects.add(this);
		}
		
		public SkillEffect(RPGMonster mob)
		{
			this.mob = mob;
			skillEffects.add(this);
		}
		
		public abstract boolean tick();
		
		public static void globalTick()
		{
			for (int i = 0; i < skillEffects.size(); i++)
				if (skillEffects.get(i).tick())
				{
					skillEffects.remove(i);
					i--;
				}
		}
	}
}
