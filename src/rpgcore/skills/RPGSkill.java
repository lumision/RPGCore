package rpgcore.skills;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;
import rpgcore.player.RPlayer;

public class RPGSkill 
{
	public RPlayer caster;
	public Player player;
	public double baseDamageMultiplier;
	public int casterDamage;
	public boolean passiveSkill;
	public String skillName;
	public String cooldownName;
	public int skillTier;
	public ClassType classType;
	public static Random rand = new Random();

	public static final RPGSkill[] skillList = { //ADDSKILL

			new HellfireTerminus(null),
			new Armageddon(null),
			new Heartspan(null),
			new TripleKunai(null),
			//new Supernova(),
			//new BlackHole(),
			//new Asteroid(),

			//MAGE TIER 1
			new MagicMastery1(),
			new Wisdom(),
			new ArcaneBolt(null),
			new WindDrive(null),
			new ArcaneBlast(null),
			new IceBolt(null),
			new ArcaneSpears(null),
			new PoisonBolt(null),
			
			//MAGE TIER 2
			new Teleport1(null),
			new Accelerate(null),
			new Lightning(null),
			new ArcaneBeam(null),
			new IceField(null),
			new Fireball(null),
			
			//MAGE TIER 3
			new Teleport2(null),
			new MagicMastery2(),
			new ArcaneStorm(null),
			
			//MAGE TIER 4
			new Sunfire(null),
			
			//MAGE TIER 5
			new MagicMastery3(),
			new ArcaneBarrage(null),
			
			//--------------------
			
			//WARRIOR TIER 1
			new IronBody(),
			new PowerPierce(null),
			new Leap(null),
			new PowerSlash1(null),
			new Tenacity1(),
			new ShieldBash(null),
			new IcyStab(null),
			new Warcry(null),
			
			//WARRIOR TIER 2
			new Enrage(null),
			new PowerSlash2(null),
			new FieryThrust(null),
			
			//WARRIOR TIER 3
			new Tenacity2(),
			new TurtleShield(null),
			
			//WARRIOR TIER 5
			new Tenacity3(),

			//--------------------

			//PRIEST TIER 1
			new HolyBolt(null),
			new Heal1(null),
			new Bless(null),
			new Protect(null),
			
			//PRIEST TIER 2
			new Heal2(null),
			new Absolve(null),
			
			//PRIEST TIER 4
			new Heal3(null),
			new Enlightenment(null),
			
			//--------------------
			
			//ASSASSIN TIER 1
			new ShadowStab1(null),
			new Dash1(null),
			new Bind1(null),
			new QuickSlash1(null),
			new BladeMastery1(),
			new Kunai(null),
			new LightFeet(),

			//ASSASSIN TIER 2
			new BladeMastery2(),
			new BladeStorm(null),
			new ShadowStab2(null),
			new Dash2(null),

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

			new CelestialBlessing(null),

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

	public RPGSkill(String skillName, RPlayer caster, boolean passiveSkill, int castDelay, double baseDamageMultiplier, ClassType classType, int skillTier)
	{
		this.passiveSkill = passiveSkill;
		this.skillName = skillName;
		this.cooldownName = skillName;
		this.classType = classType;
		this.skillTier = skillTier;
		if (caster == null)
			return;
		this.caster = caster;
		this.player = caster.getPlayer();
		this.baseDamageMultiplier = baseDamageMultiplier;
		this.casterDamage = caster.getDamageOfClass();
		if (this.player == null)
			return;
		if (this.player.getPlayer() == null)
			return;

		/*
		if (!caster.currentClass.getAdvancementTree().contains(classType))
			return;
		 */
		caster.lastSkill = skillName;
		caster.castDelays.put(skillName, (int) (castDelay * caster.getStats().attackSpeedMultiplier));
		caster.globalCastDelay = 1;
		activate();
	}

	public RPGSkill(String skillName, RPlayer caster, boolean passiveSkill, int castDelay, double baseDamageMultiplier, ClassType classType, int skillTier, String cooldownName)
	{
		this.passiveSkill = passiveSkill;
		this.skillName = skillName;
		this.cooldownName = cooldownName;
		this.classType = classType;
		this.skillTier = skillTier;
		if (caster == null)
			return;
		this.caster = caster;
		this.player = caster.getPlayer();
		this.baseDamageMultiplier = baseDamageMultiplier;
		this.casterDamage = caster.getDamageOfClass();
		if (this.player == null)
			return;
		if (this.player.getPlayer() == null)
			return;

		/*
		if (!caster.currentClass.getAdvancementTree().contains(classType))
			return;
		 */
		caster.lastSkill = skillName;
		caster.castDelays.put(skillName, (int) (castDelay * caster.getStats().attackSpeedMultiplier));
		caster.globalCastDelay = 1;
		activate();
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

	public void applyCooldown(float seconds)
	{
		int reduction = caster.getStats().cooldownReductionAdd;
		double total = seconds * 20.0F;
		total -= total / 100.0F * reduction;
		if (total <= 0)
			return;
		caster.cooldowns.put(cooldownName, (int) total);
	}

	public int getUnvariedDamage()
	{
		return (int) (casterDamage * baseDamageMultiplier);
	}

	public void activate() {}
	public ItemStack getSkillItem() { return null; }

	public void instantiate(RPlayer rp) {}
	
	public static abstract class SkillEffect 
	{
		public static ArrayList<SkillEffect> skillEffects = new ArrayList<SkillEffect>();
		public static final Random rand = new Random();
		
		public RPGSkill skill;
		public int tick;
		
		public SkillEffect(RPGSkill skill)
		{
			this.skill = skill;
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
