package rpgcore.party;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGCore;
import rpgcore.player.RPlayer;
import rpgcore.skillinventory.SkillInventory;

public class Party 
{
	public RPlayer host;
	public ArrayList<RPlayer> players;
	public ArrayList<String> invites;
	public int partyID;
	public Inventory partyInventory;
	public Party(int partyID, RPlayer host)
	{
		this.players = new ArrayList<RPlayer>();
		this.invites = new ArrayList<String>();
		this.host = host;
		host.partyID = partyID;
		this.players.add(host);
		this.partyID = partyID;
		setPartyInventory();
	}

	public void setPartyInventory()
	{
		this.partyInventory = Bukkit.createInventory(null, 9, CakeLibrary.recodeColorCodes("&5Party Info"));
		updatePartyInventory();
	}

	public void updatePartyInventory()
	{
		if (partyID == -1)
			return;
		for (int i = 0; i < players.size(); i++)
		{
			RPlayer rp = players.get(i);
			Player p = rp.getPlayer();
			String color = SkillInventory.getClassColor(rp.currentClass);
			ItemStack is = CakeLibrary.editNameAndLore(SkillInventory.getClassIcon(rp.currentClass),
					color + rp.getPlayerName(),
					"&7Class: " + SkillInventory.getClassColor(rp.currentClass) + rp.currentClass.toString(),
					"&7Level: " + color + rp.getCurrentClass().getLevel());

			if (p != null)
				is = CakeLibrary.addLore(is, "&7Raw Damage: " + color + rp.getDamageOfClass(),
						"&7Attack Speed: " + color + String.format("%.1f", 1.0D / rp.calculateCastDelayMultiplier()));
			else
				is = CakeLibrary.addLore(is, "&7 * Offline");
			if (rp == host)
				is = CakeLibrary.addLore(is, "&7 * Host");
			partyInventory.setItem(i, is);
		}
	}

	public void addPlayer(RPlayer rp)
	{
		players.add(rp);
		rp.partyID = this.partyID;
	}

	public void removePlayer(RPlayer rp)
	{
		players.remove(rp);
		rp.partyID = -1;
	}

	public void broadcastMessage(String msg)
	{
		for (RPlayer rp: players)
		{
			Player p = rp.getPlayer();
			if (p != null)
				msg(p, msg);
		}
	}

	public static void msg(Player p, String msg)
	{
		p.sendMessage(CakeLibrary.recodeColorCodes("&5[&dParties&5] &d" + msg));
	}

	public void disbandParty()
	{
		for (RPlayer rp: players)
		{
			rp.partyID = -1;
			Player p = rp.getPlayer();
			if (p != null)
				RPGCore.msg(p, "Party has been disbanded.");
		}
		this.partyID = -1;
		this.players.clear();
	}
}
