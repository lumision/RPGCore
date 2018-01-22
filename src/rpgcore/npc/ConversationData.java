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
	public ArrayList<ConversationLine> conversationLines = new ArrayList<ConversationLine>();
	public ArrayList<ConversationPart> masters;
	public ConversationData(String npcName, ArrayList<ConversationLine> conversationLines, ArrayList<ConversationPart> masters)
	{
		this.npcName = npcName;
		this.conversationLines = conversationLines;
		this.masters = masters;
	}

	public static class ConversationLine
	{
		public ConversationPartType type;
		public ArrayList<String> lines;
		public String flagKey = null;
		public String flagValue = null;

		public ConversationLine(ConversationPartType type, ArrayList<String> lines)
		{
			this.type = type;
			this.lines = lines;
		}

		public ConversationLine(ConversationPartType type, ArrayList<String> lines, String flagKey, String flagValue)
		{
			this.type = type;
			this.lines = lines;
			this.flagKey = flagKey;
			this.flagValue = flagValue;
		}

		public ConversationLine(ConversationPartType type, String line, String flagKey, String flagValue)
		{
			this.type = type;
			this.lines = new ArrayList<String>();
			lines.add(line);
			this.flagKey = flagKey;
			this.flagValue = flagValue;
		}

		public String getRandomLine()
		{
			return lines.get(RPGCore.rand.nextInt(lines.size()));
		}

		public String getChatLine(String npcName)
		{
			return CakeLibrary.recodeColorCodes("&f<" + npcName + "&f> " + getRandomLine());
		}
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
		NPC, PLAYER, OPENING, CLOSING;
	}

	public static void loadConversationData()
	{
		dataList.clear();
		for (File file: dataFolder.listFiles())
		{
			String npcName = "";
			ArrayList<ConversationLine> conversationLines = new ArrayList<ConversationLine>();
			ConversationLine prevLine = null;
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
						if (headers[0].toLowerCase().startsWith("openinglines:"))
						{
							String[] vars = headers[0].split(": ");
							if (vars.length > 1)
							{
								String[] vars1 = vars[1].split(", ");
								String flagKey = vars1[0];
								String flagValue = vars1[1];
								if (prevLine != null && 
										prevLine.type.equals(ConversationPartType.OPENING) && 
										prevLine.flagKey != null && prevLine.flagKey.equals(flagKey) && prevLine.flagValue.equals(flagValue))
									prevLine.lines.add(trimmed);
								else
								{
									prevLine = new ConversationLine(ConversationPartType.OPENING, trimmed, flagKey, flagValue);
									conversationLines.add(prevLine);
								}
							} else
							{
								if (prevLine != null && prevLine.flagKey == null && prevLine.type.equals(ConversationPartType.OPENING))
									prevLine.lines.add(trimmed);
								else
								{
									prevLine = new ConversationLine(ConversationPartType.OPENING, trimmed, null, null);
									conversationLines.add(prevLine);
								}
							}
						} else if (headers[0].toLowerCase().startsWith("closinglines:"))
						{
							String[] vars = headers[0].split(": ");
							if (vars.length > 1)
							{
								String[] vars1 = vars[1].split(", ");
								String flagKey = vars1[0];
								String flagValue = vars1[1];
								if (prevLine != null && 
										prevLine.type.equals(ConversationPartType.CLOSING) && 
										prevLine.flagKey != null && prevLine.flagKey.equals(flagKey) && prevLine.flagValue.equals(flagValue))
									prevLine.lines.add(trimmed);
								else
								{
									prevLine = new ConversationLine(ConversationPartType.CLOSING, trimmed, flagKey, flagValue);
									conversationLines.add(prevLine);
								}
							} else
							{
								if (prevLine != null && prevLine.flagKey == null && prevLine.type.equals(ConversationPartType.CLOSING))
									prevLine.lines.add(trimmed);
								else
								{
									prevLine = new ConversationLine(ConversationPartType.CLOSING, trimmed, null, null);
									conversationLines.add(prevLine);
								}
							}
						}
						else if (headers[0].toLowerCase().startsWith("conversation:"))
						{
							boolean player = spacebars % 2 == 0;
							ConversationPart cp = new ConversationPart(player ? ConversationPartType.PLAYER :
								ConversationPartType.NPC, trimmed, new ArrayList<ConversationPart>());

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
										(prevPart.flagKey == null || 
										(prevPart.flagKey != null && 
										prevPart.flagKey.equals(cp.flagKey) && 
										prevPart.flagValue.equals(cp.flagValue))))
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
					} else if (s.toLowerCase().startsWith("npcname: "))
					{
						String[] split = s.split(": ");
						npcName = CakeLibrary.recodeColorCodes(split[1]);
					}
				}
				ConversationData cd = new ConversationData(CakeLibrary.recodeColorCodes(npcName), conversationLines, masters);
				dataList.add(cd);
			} catch (Exception e) {
				RPGCore.msgConsole("&4Error reading conversation data file: " + file.getName());
				e.printStackTrace();
				continue;
			}
		}
	}
}
