package rpgcore.npc;

import java.io.File;
import java.util.ArrayList;

import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGCore;

public class ConversationData
{
	public static ArrayList<ConversationData> dataList = new ArrayList<ConversationData>();
	public static File dataFolder = new File("plugins/RPGCore/npc-conversations");
	public String npcName;
	public ArrayList<String> openingLines = new ArrayList<String>();
	public ArrayList<String> closingLines = new ArrayList<String>();
	public ArrayList<ConversationPart> masters;
	public ConversationData(String npcName, ArrayList<String> openingLines, ArrayList<String> closingLines, ArrayList<ConversationPart> masters)
	{
		this.npcName = npcName;
		this.openingLines = openingLines;
		this.closingLines = closingLines;
		this.masters = masters;
	}

	public static class ConversationPart
	{
		public ConversationPartType type;
		public String string;
		public ArrayList<ConversationPart> next = new ArrayList<ConversationPart>();
		public String flagKey;
		public String flagValue;
		public ConversationPart(ConversationPartType type, String string, ArrayList<ConversationPart> next)
		{
			this.type = type;
			this.string = string;
			this.next = next;
		}
		
		public ConversationPart(ConversationPartType type, String string, ArrayList<ConversationPart> next, String npcFlag, String flagValue)
		{
			this.type = type;
			this.string = string;
			this.next = next;
			this.flagKey = npcFlag;
			this.flagValue = flagValue;
		}
	}

	public static enum ConversationPartType
	{
		NPC, PLAYER;
	}

	public static void loadConversationData()
	{
		dataList.clear();
		for (File file: dataFolder.listFiles())
		{
			String npcName = "";
			ArrayList<String> openingLines = new ArrayList<String>();
			ArrayList<String> closingLines = new ArrayList<String>();
			ArrayList<ConversationPart> masters = new ArrayList<ConversationPart>();
			ConversationPart prevPart = null;
			int prevSpaces = 0;
			ConversationPart[] conversationParts = new ConversationPart[32];
			try
			{
				ArrayList<String> lines = CakeLibrary.readFile(file);
				String[] headers = new String[32];
				for (String s: lines)
				{
					int spacebars = 0;
					String trimmed = "";
					boolean ended = false;
					for (char c: s.toCharArray())
						if (c == ' ' && !ended)
							spacebars++;
						else 
						{
							ended = true;
							trimmed += c;
						}
					if (trimmed.length() <= 0)
						continue;
					headers[spacebars] = trimmed;

					if (spacebars > 0)
					{
						if (headers[0].equals("openinglines:"))
							openingLines.add(trimmed);
						else if (headers[0].equals("closinglines:"))
							closingLines.add(trimmed);
						else if (headers[0].startsWith("conversation:"))
						{
							boolean player = spacebars % 2 == 0;
							ConversationPart cp = new ConversationPart(player ? ConversationPartType.PLAYER : ConversationPartType.NPC
									, trimmed, new ArrayList<ConversationPart>());
							
							String flagKey = null;
							String flagValue = null;
							String[] vars = headers[0].split(": ");
							if (vars.length > 1)
							{
								String[] vars1 = vars[1].split(", ");
								flagKey = vars1[0];
								flagValue = vars1[1];
								cp.flagKey = flagKey;
								cp.flagValue = flagValue;
							}
							
							if (player && spacebars > 1)
								conversationParts[spacebars - 1].next.add(cp);
							else
							{
								if (conversationParts[spacebars] != null && 
										prevSpaces == spacebars && 
										(prevPart.flagKey == null || (prevPart.flagKey != null && prevPart.flagKey.equals(cp.flagKey) && prevPart.flagValue.equals(cp.flagValue))))
									conversationParts[spacebars].next.add(cp);
								else if (spacebars > 1)
									conversationParts[spacebars - 1].next.add(cp);
							}
							
							conversationParts[spacebars] = cp;
							prevSpaces = spacebars;
							prevPart = cp;
							
							ConversationPart get = null;
							for (ConversationPart check: masters)
								if (check.flagKey != null && check.flagKey.equals(flagKey) && check.flagValue.equals(flagValue))
									get = check;
							if (get == null)
								masters.add(cp);
							
							/** this shit doesn't work
							 * 
							if (conversationParts[spacebars] != null && spacebars % 2 != 0)
							{
								if (spacebars > 1 && conversationParts[spacebars - 1].type == ConversationPartType.PLAYER)
								{
									conversationParts[spacebars] = cp;
									conversationParts[spacebars - 1].next.add(cp);
								} else
								{
									conversationParts[spacebars].next.add(cp);
									conversationParts[spacebars] = cp;
								}
							} else	
							{
								conversationParts[spacebars] = cp;
								if (spacebars > 1)
									conversationParts[spacebars - 1].next.add(cp);
							}
							*/
						}
					} else if (s.startsWith("npcname: "))
					{
						String[] split = s.split(": ");
						npcName = CakeLibrary.recodeColorCodes(split[1]);
					}
				}
				ConversationData cd = new ConversationData(CakeLibrary.recodeColorCodes(npcName), openingLines, closingLines, masters);
				dataList.add(cd);
			} catch (Exception e) {
				RPGCore.msgConsole("&4Error reading conversation data file: " + file.getName());
				e.printStackTrace();
				continue;
			}
		}
	}
}
