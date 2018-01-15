package rpgcore.party;

import java.util.ArrayList;

import rpgcore.main.RPGCore;
import rpgcore.player.RPlayer;

public class RPartyManager 
{
	public ArrayList<Party> parties;
	public RPGCore instance;
	public RPartyManager(RPGCore instance)
	{
		this.instance = instance;
		this.parties = new ArrayList<Party>();
	}
	
	public Party getParty(int id)
	{
		for (Party p: parties)
			if (p.partyID == id)
				return p;
		return null;
	}
	
	public Party createNewParty(RPlayer host)
	{
		int id = 0;
		while(getParty(id) != null)
			id++;
		Party p = new Party(id, host);
		parties.add(p);
		return p;
	}
}
