package rpgcore.skills;

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
	public int skillTier;
	public ClassType classType;
	public static Random rand = new Random();

	public static final RPGSkill[] skillList = { //ADDSKILL

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
			new Teleport(),
			new Accelerate(),
			new Lightning(),
			new ArcaneBeam(),
			new IceField(),
			new Fireball(),

			//WARRIOR TIER 1
			new PowerPierce(),
			new IronBody(),
			new Leap(),

			//PRIEST TIER 1
			new HolyBolt(),
			new Heal1(),
			new Bless(),
			new Protect(),

			//ASSASSIN TIER 1
			new ShadowStab(),
			new Dash(),
			new BladeMastery(),



			//PRIEST TIER 2
			new Heal2(),

			//WARRIOR TIER 2
			new Warcry(),

			//ASSASSIN TIER 2
			new Kunai(),
			new LightFeet(),
			
			
			//MAGE TIER 3
			new MagicMastery2(),
			new ArcaneStorm(),

			
			//PRIEST TIER 4
			new Heal3(),
			new Enlightenment(),
			
			
			//MAGE TIER 5
			new MagicMastery3(),
			new ArcaneBarrage(),
			

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

	public RPGSkill(String skillName, RPlayer caster, boolean passiveSkill, int castDelay, double baseDamageMultiplier, ClassType classType, int skillTier)
	{
		this.passiveSkill = passiveSkill;
		this.skillName = skillName;
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
		caster.castDelays.put(skillName, (int) (castDelay * caster.calculateCastDelayMultiplier()));
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
		int reduction = caster.calculateCooldownReduction();
		double total = seconds * 20.0F;
		total -= total / 100.0F * reduction;
		if (total <= 0)
			return;
		caster.cooldowns.put(skillName, (int) total);
	}

	public int getUnvariedDamage()
	{
		return (int) (casterDamage * baseDamageMultiplier);
	}

	public void activate() {}
	public ItemStack getSkillItem() { return null; }

	public void insantiate(RPlayer rp) {}
}
