package rpgcore.tutorial;

import org.bukkit.entity.Player;

import rpgcore.classes.ClassInventory;
import rpgcore.external.Title;
import rpgcore.item.RItem;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGCore;
import rpgcore.player.RPlayer;

public class Tutorial 
{
	public RPlayer player;
	public boolean welcomeMessage, classMessage, classSelect, classResponse, 
	weaponGive, weaponEquip, weaponExplain, equipmentExplain, 
	equipmentExplain11, equipmentExplain12, equipmentExplain13, equipmentExplain14, equipmentExplain15, equipmentExplain2, equipmentExplain3, equipmentExplain4, equipmentExplain5;
	public static Title titleWelcome = new Title("&b- Welcome -", "&bto CakeCraft!", 20, 80, 20);
	public static Title titleClass = new Title("", "&bYou will now pick a class...", 20, 60, 20);
	public static Title titleWeapon = new Title("", "&eYou have been gifted an appropriate weapon.", 20, 60, 20);
	public static Title titleEquip = new Title("", "&a&nPlace that weapon in your &2&nSHIELD&a&n slot to equip it.", 20, 32767, 20);
	public static Title titleEquipped = new Title("", "&aThe stats of that weapon have now been applied.", 20, 100, 20);
	public static Title titleEquipment = new Title("", "&aOnly items in these equipment slots:", 20, 40, 20);
	public static Title titleEquipment11 = new Title("&d&nShield&d", "", 10, 20, 10);
	public static Title titleEquipment12 = new Title("&d&nHelmet&d", "", 10, 20, 10);
	public static Title titleEquipment13 = new Title("&d&nChestplate&d", "", 10, 20, 10);
	public static Title titleEquipment14 = new Title("&d&nLeggings&d", "", 10, 20, 10);
	public static Title titleEquipment15 = new Title("&dand &d&nBoots&d", "", 10, 20, 10);
	public static Title titleEquipment2 = new Title("", "&d...will have their stats &napplied&d to you.", 20, 80, 20);
	public static Title titleEquipment3 = new Title("", "&cWhat you hold in your &nmain hand&d...", 20, 60, 20);
	public static Title titleEquipment4 = new Title("", "&c...has &nno effect&c on your stats.", 20, 60, 20);
	public static Title titleEquipment5 = new Title("", "&cBe sure to keep that in mind.", 20, 60, 20);
	public long ticks;
	public long equipTicks = Long.MAX_VALUE;
	public Tutorial(RPlayer player)
	{
		this.player = player;
	}
	
	public void check()
	{
		if (player.tutorialCompleted)
			return;
		Player p = player.getPlayer();
		if (p == null)
			return;
		if (p.getOpenInventory() == null || p.getOpenInventory().getTitle() == null || CakeLibrary.hasColor(p.getOpenInventory().getTitle()))
			return;
		ticks += 10;
		double spawnDistance = p.getWorld().getSpawnLocation().distance(p.getLocation());
		if (ticks > 20 && spawnDistance > 5 && !welcomeMessage)
		{
			titleWelcome.sendPlayer(p);
			welcomeMessage = true;
		} else if (ticks >= 180 && !classMessage)
		{
			titleClass.sendPlayer(p);
			classMessage = true;
		} else if (ticks >= 300 && !classSelect)
		{
			p.openInventory(ClassInventory.getClassInventory1(player));
			classSelect = true;
		} else if (ticks >= 300 && !classResponse)
		{
			Title title = new Title("&c< " + player.currentClass.getClassName() + " >", "&e...Is the name of the class you've chosen.", 20, 60, 20);
			title.sendPlayer(p);
			p.sendMessage(CakeLibrary.recodeColorCodes("&7&oPsst... you are able to change your class later on if you want to!"));
			classResponse = true;
		} else if (ticks >= 420 && !weaponGive)
		{
			titleWeapon.sendPlayer(p);
			RItem ri = RPGCore.getItemFromDatabase(player.currentClass.getDamageType() == 1 ? "BeginnerWand" : "BeginnerSword");
			p.getInventory().addItem(ri.createItem());
			weaponGive = true;
		} else if (ticks >= 540 && !weaponEquip)
		{
			titleEquip.sendPlayer(p);
			weaponEquip = true;
		} else if (ticks >= 560 && !CakeLibrary.isItemStackNull(p.getEquipment().getItemInOffHand()) && !weaponExplain)
		{
			titleEquipped.sendPlayer(p);
			weaponExplain = true;
			equipTicks = ticks;
		} else if (ticks - equipTicks >= 140 && !equipmentExplain)
		{
			titleEquipment.sendPlayer(p);
			equipmentExplain = true;
		} else if (ticks - equipTicks >= 220 && !equipmentExplain11)
		{
			titleEquipment11.sendPlayer(p);
			equipmentExplain11 = true;
		} else if (ticks - equipTicks >= 260 && !equipmentExplain12)
		{
			titleEquipment12.sendPlayer(p);
			equipmentExplain12 = true;
		} else if (ticks - equipTicks >= 300 && !equipmentExplain13)
		{
			titleEquipment13.sendPlayer(p);
			equipmentExplain13 = true;
		} else if (ticks - equipTicks >= 340 && !equipmentExplain14)
		{
			titleEquipment14.sendPlayer(p);
			equipmentExplain14 = true;
		} else if (ticks - equipTicks >= 380 && !equipmentExplain15)
		{
			titleEquipment15.sendPlayer(p);
			equipmentExplain15 = true;
		} else if (ticks - equipTicks >= 420 && !equipmentExplain2)
		{
			titleEquipment2.sendPlayer(p);
			equipmentExplain2 = true;
		} else if (ticks - equipTicks >= 540 && !equipmentExplain3)
		{
			titleEquipment3.sendPlayer(p);
			equipmentExplain3 = true;
		} else if (ticks - equipTicks >= 640 && !equipmentExplain4)
		{
			titleEquipment4.sendPlayer(p);
			equipmentExplain4 = true;
		} else if (ticks - equipTicks >= 740 && !equipmentExplain5)
		{
			titleEquipment5.sendPlayer(p);
			equipmentExplain5 = true;
		}
	}
}
