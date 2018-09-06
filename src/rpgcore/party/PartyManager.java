package rpgcore.party;

import java.util.ArrayList;

import rpgcore.main.RPGCore;
import rpgcore.player.RPlayer;

public class PartyManager 
{
	public ArrayList<RPGParty> parties;
	public RPGCore instance;
	public PartyManager(RPGCore instance)
	{
		this.instance = instance;
		this.parties = new ArrayList<RPGParty>();
	}
	
	public RPGParty getParty(int id)
	{
		for (RPGParty p: parties)
			if (p.partyID == id)
				return p;
		return null;
	}
	
	public RPGParty createNewParty(RPlayer host)
	{
		int id = 0;
		while(getParty(id) != null)
			id++;
		RPGParty p = new RPGParty(id, host);
		parties.add(p);
		return p;
	}
}
