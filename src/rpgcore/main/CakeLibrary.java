package rpgcore.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.EntityLightning;
import net.minecraft.server.v1_12_R1.EnumParticle;
import net.minecraft.server.v1_12_R1.PacketPlayOutSpawnEntityWeather;
import net.minecraft.server.v1_12_R1.PacketPlayOutWorldParticles;
import net.minecraft.server.v1_12_R1.TileEntitySkull;

public class CakeLibrary
{
	//§
	private static final Random random = new Random();
	private static final String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	// <ITEM-RELATED>
	public static ItemStack getSkullWithURL(String url) 
	{
		ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
		if(url.isEmpty())return head;


		SkullMeta headMeta = (SkullMeta) head.getItemMeta();
		GameProfile profile = new GameProfile(UUID.randomUUID(), null);
		byte[] encodedData = Base64.encodeBase64(String.format("{textures:{SKIN:{url:\"%s\"}}}", url).getBytes());
		profile.getProperties().put("textures", new Property("textures", new String(encodedData)));
		Field profileField = null;
		try {
			profileField = headMeta.getClass().getDeclaredField("profile");
			profileField.setAccessible(true);
			profileField.set(headMeta, profile);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e1) {
			e1.printStackTrace();
		}
		head.setItemMeta(headMeta);
		return head;
	}
	
	public static ItemStack getSkullWithTexture(String texture) 
	{
		ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
		SkullMeta headMeta = (SkullMeta) head.getItemMeta();
		GameProfile profile = new GameProfile(UUID.randomUUID(), null);
		profile.getProperties().put("textures", new Property("textures", texture));
		Field profileField = null;
		try {
			profileField = headMeta.getClass().getDeclaredField("profile");
			profileField.setAccessible(true);
			profileField.set(headMeta, profile);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e1) {
			e1.printStackTrace();
		}
		head.setItemMeta(headMeta);
		return head;
	}

	// Real Method
	public static GameProfile getNonPlayerProfile(String skinURL, boolean randomName) 
	{
		GameProfile newSkinProfile = new GameProfile(UUID.randomUUID(), randomName ? getRandomString(16) : null);
		newSkinProfile.getProperties().put("textures", new Property("textures", Base64Coder.encodeString("{textures:{SKIN:{url:\"" + skinURL + "\"}}}")));
		return newSkinProfile;
	}

	// Example Usage
	public static void setSkullWithNonPlayerProfile(String skinURL, boolean randomName, Block skull) 
	{
		if(skull.getType() != Material.SKULL)
			throw new IllegalArgumentException("Block must be a skull.");
		TileEntitySkull skullTile = (TileEntitySkull)((CraftWorld)skull.getWorld()).getHandle().getTileEntity(new BlockPosition(skull.getX(), skull.getY(), skull.getZ()));
		skullTile.setGameProfile(getNonPlayerProfile(skinURL, randomName));
		skull.getWorld().refreshChunk(skull.getChunk().getX(), skull.getChunk().getZ());
	}

	// Util
	public static String getRandomString(int length) 
	{
		StringBuilder b = new StringBuilder(length);
		for(int j = 0; j < length; j++)
			b.append(chars.charAt(random.nextInt(chars.length())));
		return b.toString();
	}
	
	public static String convertTimeToString(int seconds)
	{
		int h = 0;
		int m = 0;
		int s = 0;
		while (seconds >= 3600)
		{
			seconds -= 3600;
			h++;
		}
		while (seconds >= 60)
		{
			seconds -= 60;
			m++;
		}
		while (seconds > 0)
		{
			seconds--;
			s++;
		}
		
		return (h > 0 ? h + "h " : "") + (m > 0 ? m + "m " : "") + (s > 0 ? s + "s" : "");
	}

	/**
	 * Takes an item and returns the name of the item
	 * @param is - The item's name to check
	 * @return The name of the item
	 */
	public static String getItemName(ItemStack is)
	{
		try
		{
			return CraftItemStack.asNMSCopy(is).getName();
		} catch (Exception e) {
		}
		return is.getType().name();
	}

	/**
	 * Takes an item and renames it to the target name
	 * @param is - The item to rename
	 * @param name - What to rename to rename to
	 * @return The renamed ItemStack
	 */
	public static ItemStack renameItem(ItemStack is, String name)
	{
		name = recodeColorCodes(name);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(name);
		is.setItemMeta(im);
		return is;
	}

	public static ArrayList<String> getItemLore(ItemStack is)
	{
		if (is.getItemMeta() == null)
			return new ArrayList<String>();
		if (is.getItemMeta().getLore() == null)
			return new ArrayList<String>();
		return new ArrayList<String>(is.getItemMeta().getLore());
	}

	public static ItemStack setItemLore(ItemStack is, ArrayList<String> lore)
	{
		ItemMeta im = is.getItemMeta();
		im.setLore(lore);
		is.setItemMeta(im);
		return is;
	}

	public static ItemStack addLore(ItemStack is, String... lore)
	{
		ItemMeta im = is.getItemMeta();
		List<String> list = im.getLore();
		if (list == null)
			list = new ArrayList<String>();
		for (String s : lore)
		{
			if (s == null)
				continue;
			if (s.equals(""))
				continue;
			s = recodeColorCodes(s);
			list.add(s);
		}
		im.setLore(list);
		is.setItemMeta(im);
		return is;
	}

	public static ItemStack editNameAndLore(ItemStack is, String name, String... lore)
	{
		is = renameItem(is, name);
		ItemMeta im = is.getItemMeta();
		List<String> list = im.getLore();
		if (list == null)
			list = new ArrayList<String>();
		for (String s : lore)
		{
			if (s == null)
				continue;
			if (s.equals(""))
				continue;
			s = recodeColorCodes(s);
			list.add(s);
		}
		im.setLore(list);
		is.setItemMeta(im);
		return is;
	}

	public static ItemStack delLore(ItemStack is, int index)
	{
		ItemMeta im = is.getItemMeta();
		if (im.getLore() == null)
			return is;
		List<String> list = im.getLore();
		list.remove(index);
		im.setLore(list);
		is.setItemMeta(im);
		return is;
	}

	public static ItemStack delLore(ItemStack is, String s)
	{
		ItemMeta im = is.getItemMeta();
		if (im.getLore() == null)
			return is;
		boolean removed = false;
		List<String> list = im.getLore();
		for (String lore: im.getLore())
		{
			if (removed)
				continue;
			if (!removed && lore.equalsIgnoreCase(s))
			{
				list.remove(lore);
				removed = true;
			}
			if (!removed && lore.toLowerCase().contains(s.toLowerCase()))
			{
				list.remove(lore);
				removed = true;
			}
		}
		im.setLore(list);
		is.setItemMeta(im);
		return is;
	}

	public static String getFullLoreLine(ItemStack is, String s)
	{
		if (is == null)
			return null;
		if (is.getType() == Material.AIR)
			return null;
		if (is.getItemMeta().getLore() == null)
			return null;
		for (String lore: is.getItemMeta().getLore())
			if (lore.toLowerCase().contains(s.toLowerCase()))
				return lore;
		return null;
	}

	public static void givePlayerItem(Player player, ItemStack item)
	{
		if (!playerHasVacantSlots(player))
			player.getWorld().dropItem(player.getLocation(), item.clone());
		else
			player.getInventory().addItem(item.clone());
	}
	
	public static int convertMultiplierToAddedPercentage(float multiplier)
	{
		return Math.round(((multiplier - 1.0F) * 100.0F));
	}
	
	public static float convertAddedPercentageToMultiplier(int percentage)
	{
		return (percentage / 100.0F) + 1.0F;
	}

	public static boolean isItemStackNull(ItemStack is)
	{
		if (is == null)
			return true;
		if (is.getType() == Material.AIR)
			return true;
		if (is.getTypeId() == 0)
			return true;
		return false;
	}

	public static int getInventoryVacantSlots(Inventory inv)
	{
		int vacant = inv.getSize();
		for (int i = 0; i < inv.getSize(); i++)
			if (!isItemStackNull(inv.getItem(i)))
				vacant--;
		return vacant;
	}

	public static boolean playerHasVacantSlots(Player p)
	{
		int slots = 36;
		Inventory inv = p.getInventory();
		for (int i = 0; i < 36; i++)
		{
			ItemStack is = inv.getItem(i);
			if (!CakeLibrary.isItemStackNull(is))
				slots--;
		}
		return slots > 0;
	}

	public static TextComponent getItemAsTextComponent(ItemStack is)
	{
		if (CakeLibrary.isItemStackNull(is))
			return new TextComponent("");
		TextComponent item = new TextComponent("§f" + CakeLibrary.getItemName(is) + (is.getAmount() > 1 ? CakeLibrary.recodeColorCodes(" &7(" + is.getAmount()) + ")" : ""));
		ComponentBuilder base = new ComponentBuilder(CakeLibrary.getItemName(is));
		ArrayList<String> lore = CakeLibrary.getItemLore(is);
		Map<Enchantment, Integer> enchs = is.getEnchantments();
		if (enchs.size() > 0)
		{
			ArrayList<Enchantment> eList = new ArrayList<Enchantment>();
			eList.addAll(enchs.keySet());
			ArrayList<Integer> eValues = new ArrayList<Integer>();
			eValues.addAll(enchs.values());
			for (int i = 0; i < eList.size(); i++)
				base.append(CakeLibrary.recodeColorCodes("\n" + "&7" + CakeLibrary.getEnchantmentName(eList.get(i)) + " " + CakeLibrary.convertToRoman(eValues.get(i))));
		}
		if (lore.size() > 0)
			for (int i = 0; i < lore.size(); i++)
				base.append("\n" + lore.get(i));
		item.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, base.create()));
		return item;
	}
	// </ITEM-RELATED>

	public static void spawnParticle(EnumParticle particle, Location location, float range, Player player, int particles, float speed)
	{
		PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(particle, true, (float) location.getX(), (float) location.getY(), (float) location.getZ(), range, range, range, speed, particles, null );
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
		//		CakeLibrary.spawnParticle(EnumParticle.FLAME, player.getLocation(), 128, player, 99999, 0);
	}

	public static void spawnParticle(EnumParticle particle, Location location, float range, Player player, int particles, float speed, int... data)
	{
		PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(particle, true, (float) location.getX(), (float) location.getY(), (float) location.getZ(), range, range, range, speed, particles, data );
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
		//		CakeLibrary.spawnParticle(EnumParticle.FLAME, player.getLocation(), 128, player, 99999, 0);
	}

	public static class FireworkEffectPlayer {

		/*
		 * Example use:
		 *
		 * public class FireWorkPlugin implements Listener {
		 *
		 * FireworkEffectPlayer fplayer = new FireworkEffectPlayer();
		 *
		 * @EventHandler
		 * public void onPlayerLogin(PlayerLoginEvent event) {
		 *   fplayer.playFirework(event.getPlayer().getWorld(), event.getPlayer.getLocation(), Util.getRandomFireworkEffect());
		 * }
		 *
		 * }
		 */

		// internal references, performance improvements
		private static Method world_getHandle = null;
		private static Method nms_world_broadcastEntityEffect = null;
		private static Method firework_getHandle = null;

		/**
		 * Play a pretty firework at the location with the FireworkEffect when called
		 * @param world
		 * @param loc
		 * @param fe
		 * @throws Exception
		 */
		public static void playFirework(Location loc, FireworkEffect fe) throws Exception {
			// Bukkity load (CraftFirework)
			World world = loc.getWorld();
			Firework fw = (Firework) world.spawn(loc, Firework.class);
			// the net.minecraft.server.World
			Object nms_world = null;
			Object nms_firework = null;
			/*
			 * The reflection part, this gives us access to funky ways of messing around with things
			 */
			if(world_getHandle == null) {
				// get the methods of the craftbukkit objects
				world_getHandle = getMethod(world.getClass(), "getHandle");
				firework_getHandle = getMethod(fw.getClass(), "getHandle");
			}
			// invoke with no arguments
			nms_world = world_getHandle.invoke(world, (Object[]) null);
			nms_firework = firework_getHandle.invoke(fw, (Object[]) null);
			// null checks are fast, so having this seperate is ok
			if(nms_world_broadcastEntityEffect == null) {
				// get the method of the nms_world
				nms_world_broadcastEntityEffect = getMethod(nms_world.getClass(), "broadcastEntityEffect");
			}
			/*
			 * Now we mess with the metadata, allowing nice clean spawning of a pretty firework (look, pretty lights!)
			 */
			// metadata load
			FireworkMeta data = (FireworkMeta) fw.getFireworkMeta();
			// clear existing
			data.clearEffects();
			// power of one
			data.setPower(1);
			// add the effect
			data.addEffect(fe);
			// set the meta
			fw.setFireworkMeta(data);
			/*
			 * Finally, we broadcast the entity effect then kill our fireworks object
			 */
			// invoke with arguments
			nms_world_broadcastEntityEffect.invoke(nms_world, new Object[] {nms_firework, (byte) 17});
			// remove from the game
			fw.remove();
		}

		/**
		 * Internal method, used as shorthand to grab our method in a nice friendly manner
		 * @param cl
		 * @param method
		 * @return Method (or null)
		 */
		private static Method getMethod(Class<?> cl, String method) {
			for(Method m : cl.getMethods()) {
				if(m.getName().equals(method)) {
					return m;
				}
			}
			return null;
		}

	}

	public static HashSet<Material> getPassableBlocks()
	{
		HashSet<Material> bypass = new HashSet<Material>();
		bypass.add(Material.AIR);
		bypass.add(Material.LAVA);
		bypass.add(Material.WEB);
		bypass.add(Material.SUGAR_CANE_BLOCK);
		bypass.add(Material.FLOWER_POT);
		bypass.add(Material.RAILS);
		bypass.add(Material.ACTIVATOR_RAIL);
		bypass.add(Material.DETECTOR_RAIL);
		bypass.add(Material.RED_MUSHROOM);
		bypass.add(Material.BROWN_MUSHROOM);
		bypass.add(Material.RED_ROSE);
		bypass.add(Material.YELLOW_FLOWER);
		bypass.add(Material.LONG_GRASS);
		bypass.add(Material.DOUBLE_PLANT);
		bypass.add(Material.VINE);
		bypass.add(Material.WATER);
		bypass.add(Material.STATIONARY_WATER);
		bypass.add(Material.LAVA);
		bypass.add(Material.STATIONARY_LAVA);
		bypass.add(Material.YELLOW_FLOWER);
		bypass.add(Material.LONG_GRASS);
		bypass.add(Material.TORCH);
		bypass.add(Material.REDSTONE);
		bypass.add(Material.REDSTONE_TORCH_ON);
		bypass.add(Material.REDSTONE_TORCH_OFF);
		return bypass;
	}

	public static boolean hasPermissionIgnoreOp(Player p, String node)
	{
		Permission perm = new Permission(node, PermissionDefault.FALSE);
		return p.hasPermission(perm);
	}

	public static int getPotionEffectAmplifier(LivingEntity p, PotionEffectType type)
	{
		PotionEffect effect = p.getPotionEffect(type);
		if (effect == null)
			return -1;
		return effect.getAmplifier();
	}

	public static void addPotionEffectIfBetterOrEquivalent(LivingEntity e, PotionEffect pe)
	{
		int initialLevel = getPotionEffectAmplifier(e, pe.getType());
		if (initialLevel <= pe.getAmplifier())
			e.addPotionEffect(pe, true);
	}

	public static Block getTargetBlock(LivingEntity entity, int range)
	{
		return entity.getTargetBlock(getPassableBlocks(), range);
	}

	public static String getTime()
	{
		Calendar calendar = Calendar.getInstance();
		String month = "January";
		if (calendar.get(Calendar.MONTH) + 1 == 2)
			month = "February";
		if (calendar.get(Calendar.MONTH) + 1 == 3)
			month = "March";
		if (calendar.get(Calendar.MONTH) + 1 == 4)
			month = "April";
		if (calendar.get(Calendar.MONTH) + 1 == 5)
			month = "May";
		if (calendar.get(Calendar.MONTH) + 1 == 6)
			month = "June";
		if (calendar.get(Calendar.MONTH) + 1 == 7)
			month = "July";
		if (calendar.get(Calendar.MONTH) + 1 == 8)
			month = "August";
		if (calendar.get(Calendar.MONTH) + 1 == 9)
			month = "September";
		if (calendar.get(Calendar.MONTH) + 1 == 10)
			month = "October";
		if (calendar.get(Calendar.MONTH) + 1 == 11)
			month = "November";
		if (calendar.get(Calendar.MONTH) + 1 == 12)
			month = "December";
		String minutes = calendar.get(Calendar.MINUTE) + "";
		if (minutes.length() < 2)
			minutes = "0" + minutes;
		String time = calendar.get(Calendar.HOUR_OF_DAY) + ":" + minutes + "AM";
		if (calendar.get(Calendar.HOUR_OF_DAY) >= 12)
			time = (calendar.get(Calendar.HOUR_OF_DAY) - 12) + ":" + minutes + "PM";
		return month + " " + calendar.get(Calendar.DAY_OF_MONTH) + ", " + time;
	}

	public static boolean hasColor(String s)
	{
		return s.contains("§");
	}

	public static String removeColorCodes(String s)
	{
		char[] c = s.toCharArray();
		String build = "";
		for (int i = 0; i < c.length; i++)
		{
			if ((c[i] == '§' || c[i] == '&') && i < c.length - 1)
				if (c[i + 1] == '1'
				|| c[i + 1] == '2'
				|| c[i + 1] == '3'
				|| c[i + 1] == '4'
				|| c[i + 1] == '5'
				|| c[i + 1] == '6'
				|| c[i + 1] == '7'
				|| c[i + 1] == '8'
				|| c[i + 1] == '9'
				|| c[i + 1] == '0'
				|| c[i + 1] == 'a'
				|| c[i + 1] == 'b'
				|| c[i + 1] == 'c'
				|| c[i + 1] == 'd'
				|| c[i + 1] == 'e'
				|| c[i + 1] == 'f'
				|| c[i + 1] == 'k'
				|| c[i + 1] == 'l'
				|| c[i + 1] == 'm'
				|| c[i + 1] == 'n'
				|| c[i + 1] == 'o')
				{
					i++;
					continue;
				}

			build += c[i];
		}
		return build;
	}

	public static String insertColorFormatCode(String s, String code)
	{
		if (code == null || code.length() == 0)
			return s;
		code = CakeLibrary.recodeColorCodes(code);
		char[] c = s.toCharArray();
		String build = "";
		for (int i = 0; i < c.length; i++)
		{
			if ((c[i] == '§' || c[i] == '&') && i < c.length - 1)
				if (c[i + 1] == '1'
				|| c[i + 1] == '2'
				|| c[i + 1] == '3'
				|| c[i + 1] == '4'
				|| c[i + 1] == '5'
				|| c[i + 1] == '6'
				|| c[i + 1] == '7'
				|| c[i + 1] == '8'
				|| c[i + 1] == '9'
				|| c[i + 1] == '0'
				|| c[i + 1] == 'a'
				|| c[i + 1] == 'b'
				|| c[i + 1] == 'c'
				|| c[i + 1] == 'd'
				|| c[i + 1] == 'e'
				|| c[i + 1] == 'f'
				|| c[i + 1] == 'm'
				|| c[i + 1] == 'n'
				|| c[i + 1] == 'l'
				|| c[i + 1] == 'k'
				|| c[i + 1] == 'o')
				{
					build += c[i];
					build += c[i + 1];
					build += code;
					i ++;
					continue;
				}

			build += c[i];
		}
		return build;
	}

	public static String getFinalColorCombination(String s)
	{
		String comb = "";
		char[] c = s.toCharArray();
		for (int i = 0; i < c.length; i++)
		{
			if ((c[i] == '§' || c[i] == '&') && i < c.length - 1)
			{
				if (c[i + 1] == '1'
						|| c[i + 1] == '2'
						|| c[i + 1] == '3'
						|| c[i + 1] == '4'
						|| c[i + 1] == '5'
						|| c[i + 1] == '6'
						|| c[i + 1] == '7'
						|| c[i + 1] == '8'
						|| c[i + 1] == '9'
						|| c[i + 1] == '0'
						|| c[i + 1] == 'a'
						|| c[i + 1] == 'b'
						|| c[i + 1] == 'c'
						|| c[i + 1] == 'd'
						|| c[i + 1] == 'e'
						|| c[i + 1] == 'f')
					comb = "§" + c[i + 1];

				if (c[i + 1] == 'k'
						|| c[i + 1] == 'l'
						|| c[i + 1] == 'm'
						|| c[i + 1] == 'n'
						|| c[i + 1] == 'o')
					comb += "§" + c[i + 1];
			}
		}
		return comb;
	}

	public static String recodeColorCodes(String s)
	{
		char[] c = s.toCharArray();
		String build = "";
		for (int i = 0; i < c.length; i++)
		{
			if (c[i] == '&' && i < c.length - 1)
				if (c[i + 1] == '1'
				|| c[i + 1] == '2'
				|| c[i + 1] == '3'
				|| c[i + 1] == '4'
				|| c[i + 1] == '5'
				|| c[i + 1] == '6'
				|| c[i + 1] == '7'
				|| c[i + 1] == '8'
				|| c[i + 1] == '9'
				|| c[i + 1] == '0'
				|| c[i + 1] == 'a'
				|| c[i + 1] == 'b'
				|| c[i + 1] == 'c'
				|| c[i + 1] == 'd'
				|| c[i + 1] == 'e'
				|| c[i + 1] == 'f'
				|| c[i + 1] == 'm'
				|| c[i + 1] == 'n'
				|| c[i + 1] == 'l'
				|| c[i + 1] == 'k'
				|| c[i + 1] == 'o')
				{
					build += "§";
					continue;
				}

			build += c[i];
		}
		return build;
	}

	public static int getNoteId(String note)
	{
		int octave = -1;
		try
		{
			octave = Integer.parseInt(note.substring(0, 1));
		} catch (Exception e) {
			return -1;
		}
		String note1 = note.substring(1);
		int id = 0;
		if (note1.equalsIgnoreCase("C"))
			id += 1;
		if (note1.equalsIgnoreCase("C#"))
			id += 2;
		if (note1.equalsIgnoreCase("D"))
			id += 3;
		if (note1.equalsIgnoreCase("D#"))
			id += 4;
		if (note1.equalsIgnoreCase("E"))
			id += 5;
		if (note1.equalsIgnoreCase("F"))
			id += 6;
		if (note1.equalsIgnoreCase("F#"))
			id += 7;
		if (note1.equalsIgnoreCase("G"))
			id += 8;
		if (note1.equalsIgnoreCase("G#"))
			id += 9;
		if (note1.equalsIgnoreCase("A"))
			id += 10;
		if (note1.equalsIgnoreCase("A#"))
			id += 11;
		if (note1.equalsIgnoreCase("B"))
			id += 12;
		id += (octave * 12);
		return id;
	}

	public static String getNote(int id)
	{
		int octave = 0;
		while (id > 12)
		{
			id -= 12;
			octave++;
		}
		switch (id)
		{
		default:
			return octave + "";
		case 1:
			return octave + "C";
		case 2:
			return octave + "C#";
		case 3:
			return octave + "D";
		case 4:
			return octave + "D#";
		case 5:
			return octave + "E";
		case 6:
			return octave + "F";
		case 7:
			return octave + "F#";
		case 8:
			return octave + "G";
		case 9:
			return octave + "G#";
		case 10:
			return octave + "A";
		case 11:
			return octave + "A#";
		case 12:
			return octave + "B";
		}
	}

	public static float getPitchFromNote(String note, int offset)
	{
		String note1 = getNote(getNoteId(note) + offset);
		return getPitchFromNote(note1);
	}

	public static float getPitchFromNote(String note)
	{
		int octave = -1;
		try
		{
			octave = Integer.parseInt(note.substring(0, 1));
		} catch (Exception e) {
			return -1;
		}
		String note1 = note.substring(1);
		if (octave == 1)
		{
			/**
			if (note1.equalsIgnoreCase("C"))
				return 0.353553F;
			if (note1.equalsIgnoreCase("C#"))
				return 0.374576F;
			if (note1.equalsIgnoreCase("D"))
				return 0.396850F;
			if (note1.equalsIgnoreCase("D#"))
				return 0.420448F;
			if (note1.equalsIgnoreCase("E"))
				return 0.445449F;
			if (note1.equalsIgnoreCase("F"))
				return 0.471937F;
			 */
			if (note1.equalsIgnoreCase("F#"))
				return 0.5F;
			if (note1.equalsIgnoreCase("G"))
				return 0.529732F;
			if (note1.equalsIgnoreCase("G#"))
				return 0.561231F;
			if (note1.equalsIgnoreCase("A"))
				return 0.594604F;
			if (note1.equalsIgnoreCase("A#"))
				return 0.629961F;
			if (note1.equalsIgnoreCase("B"))
				return 0.667420F;
		}
		if (octave == 2)
		{
			if (note1.equalsIgnoreCase("C"))
				return 0.707107F;
			if (note1.equalsIgnoreCase("C#"))
				return 0.749154F;
			if (note1.equalsIgnoreCase("D"))
				return 0.793701F;
			if (note1.equalsIgnoreCase("D#"))
				return 0.840896F;
			if (note1.equalsIgnoreCase("E"))
				return 0.890899F;
			if (note1.equalsIgnoreCase("F"))
				return 0.943874F;
			if (note1.equalsIgnoreCase("F#"))
				return 1.0F;
			if (note1.equalsIgnoreCase("G"))
				return 1.05946F;
			if (note1.equalsIgnoreCase("G#"))
				return 1.12246F;
			if (note1.equalsIgnoreCase("A"))
				return 1.18921F;
			if (note1.equalsIgnoreCase("A#"))
				return 1.25992F;
			if (note1.equalsIgnoreCase("B"))
				return 1.33484F;
		}
		if (octave == 3)
		{
			if (note1.equalsIgnoreCase("C"))
				return 1.41421F;
			if (note1.equalsIgnoreCase("C#"))
				return 1.49831F;
			if (note1.equalsIgnoreCase("D"))
				return 1.58740F;
			if (note1.equalsIgnoreCase("D#"))
				return 1.68179F;
			if (note1.equalsIgnoreCase("E"))
				return 1.78180F;
			if (note1.equalsIgnoreCase("F"))
				return 1.88775F;
			if (note1.equalsIgnoreCase("F#"))
				return 2.0F;
			/**
			if (note1.equalsIgnoreCase("G"))
				return 2.118926F;
			if (note1.equalsIgnoreCase("G#"))
				return 2.244924F;
			if (note1.equalsIgnoreCase("A"))
				return 2.378414F;
			if (note1.equalsIgnoreCase("A#"))
				return 2.519842F;
			if (note1.equalsIgnoreCase("B"))
				return 2.669679F;
			 */
		}
		return 0F;
	}

	public static boolean isAirBorne(Player player)
	{
		Location loc = player.getLocation();
		if (player.getVehicle() != null)
			return false;
		if (loc.getWorld().getBlockTypeIdAt(loc) > 0)
			return false;
		if (loc.getY() == loc.getBlockY() || loc.getY() == loc.getBlockY() + 0.5 || loc.getY() == loc.getBlockY() - 0.5)
			return false;
		return true;
	}

	public static boolean isSolidBlock(int i)
	{
		if (i == 0 || i == 27 || i == 50 || i == 63 || i == 68 || i == 76)
			return false;
		return true;
	}

	public static ArrayList<Location> getLocationsInALine(Location from, Location to)
	{
		ArrayList<Location> blocks = new ArrayList<Location>();
		double xDiff = to.getX() - from.getX();
		double yDiff = to.getY() - from.getY();
		double zDiff = to.getZ() - from.getZ();
		int highest = 0;
		if (xDiff > 0)
			highest = (int) xDiff;
		else
			highest = (int) -xDiff;
		if (yDiff > 0)
		{
			if (yDiff > highest)
				highest = (int) yDiff;
		} else {
			if (-yDiff > highest)
				highest = (int) -yDiff;
		}
		if (zDiff > 0)
		{
			if (zDiff > highest)
				highest = (int) zDiff;
		} else {
			if (-zDiff > highest)
				highest = (int) -zDiff;
		}
		Location lastLoc = null;
		for (double i = 0; i < highest; i++)
		{
			double sX = (xDiff / highest) * i;
			double sY = (yDiff / highest) * i;
			double sZ = (zDiff / highest) * i;
			Location efLoc = new Location(to.getWorld(), from.getX() + sX, from.getY() + sY, from.getZ() + sZ);
			if (lastLoc != null)
				if (lastLoc.getBlockX() == efLoc.getBlockX() && lastLoc.getBlockY() == efLoc.getBlockY() && lastLoc.getBlockZ() == efLoc.getBlockZ())
					continue;
			lastLoc = efLoc;
			blocks.add(efLoc);
		}
		return blocks;
	}

	public static String getColorCodesAt(String s, int index)
	{
		String cc = "";
		for (int i = 0; i < index; i++)
		{
			if (s.charAt(i) == '§')
				cc += String.valueOf(s.charAt(i)) + String.valueOf(s.charAt(i + 1));
		}
		return cc;
	}

	public static PotionEffectType getPotionEffectType(String name)
	{
		name = name.replace("_", "").replace(" ", "");
		for (PotionEffectType pet: PotionEffectType.values())
			if (pet != null)
				if (pet.getName() != null)
					if (pet.getName().replace("_", "").equalsIgnoreCase(name))
						return pet;
		if (name.equalsIgnoreCase("nausea"))
			return PotionEffectType.CONFUSION;
		if (name.equalsIgnoreCase("protection"))
			return PotionEffectType.DAMAGE_RESISTANCE;
		if (name.equalsIgnoreCase("resistance"))
			return PotionEffectType.DAMAGE_RESISTANCE;
		if (name.equalsIgnoreCase("haste"))
			return PotionEffectType.FAST_DIGGING;
		if (name.equalsIgnoreCase("minespeed"))
			return PotionEffectType.FAST_DIGGING;
		if (name.equalsIgnoreCase("miningspeed"))
			return PotionEffectType.FAST_DIGGING;
		if (name.equalsIgnoreCase("instantdamage"))
			return PotionEffectType.HARM;
		if (name.equalsIgnoreCase("strength"))
			return PotionEffectType.INCREASE_DAMAGE;
		if (name.equalsIgnoreCase("leap"))
			return PotionEffectType.JUMP;
		if (name.equalsIgnoreCase("fatigue"))
			return PotionEffectType.SLOW_DIGGING;
		if (name.equalsIgnoreCase("miningfatigue"))
			return PotionEffectType.SLOW_DIGGING;
		if (name.equalsIgnoreCase("swiftness"))
			return PotionEffectType.SPEED;
		if (name.equalsIgnoreCase("decreasedamage"))
			return PotionEffectType.WEAKNESS;
		if (name.equalsIgnoreCase("waterbreathing"))
			return PotionEffectType.WATER_BREATHING;
		return null;
	}

	public static String convertToRoman(int i)
	{
		String build = "";
		if (i > 3888)
			return i + "";
		while (i >= 1000)
		{
			build += "M";
			i -= 1000;
		}
		if (i >= 900)
		{
			build += "CM";
			i -= 900;
		}
		if (i >= 500)
		{
			build += "D";
			i -= 500;
		}
		if (i >= 400)
		{
			build += "CD";
			i -= 400;
		}
		while (i >= 100)
		{
			build += "C";
			i -= 100;
		}
		while (i >= 90)
		{
			build += "XC";
			i -= 90;
		}
		if (i >= 50)
		{
			build += "L";
			i -= 50;
		}
		if (i >= 40)
		{
			build += "XL";
			i -= 40;
		}
		while (i >= 10)
		{
			build += "X";
			i -= 10;
		}
		while (i >= 9)
		{
			build += "IX";
			i -= 9;
		}
		if (i >= 5)
		{
			build += "V";
			i -= 5;
		}
		if (i >= 4)
		{
			build += "IV";
			i -= 4;
		}
		while (i >= 1)
		{
			build += "I";
			i -= 1;
		}

		return build;
	}

	public static String getEnchantmentName(Enchantment ench)
	{
		if (ench.getId() == 0)
			return "Protection";
		if (ench.getId() == 1)
			return "Fire Protection";
		if (ench.getId() == 2)
			return "Feather Falling";
		if (ench.getId() == 3)
			return "Blast Protection";
		if (ench.getId() == 4)
			return "Projectile Protection";
		if (ench.getId() == 5)
			return "Respiration";
		if (ench.getId() == 6)
			return "Aqua Affinity";
		if (ench.getId() == 7)
			return "Thorns";
		if (ench.getId() == 8)
			return "Depth Strider";
		if (ench.getId() == 9)
			return "Frost Walker";
		if (ench.getId() == 10)
			return "Curse of Binding";
		if (ench.getId() == 16)
			return "Sharpness";
		if (ench.getId() == 17)
			return "Smite";
		if (ench.getId() == 18)
			return "Bane of Arthropods";
		if (ench.getId() == 19)
			return "Knockback";
		if (ench.getId() == 20)
			return "Fire Aspect";
		if (ench.getId() == 21)
			return "Looting";
		if (ench.getId() == 22)
			return "Sweeping Edge";
		if (ench.getId() == 32)
			return "Efficiency";
		if (ench.getId() == 33)
			return "Silk Touch";
		if (ench.getId() == 34)
			return "Unbreaking";
		if (ench.getId() == 35)
			return "Fortune";
		if (ench.getId() == 48)
			return "Power";
		if (ench.getId() == 49)
			return "Punch";
		if (ench.getId() == 50)
			return "Flame";
		if (ench.getId() == 51)
			return "Infinity";
		if (ench.getId() == 61)
			return "Luck of the Sea";
		if (ench.getId() == 62)
			return "Lure";
		if (ench.getId() == 70)
			return "Mending";
		if (ench.getId() == 71)
			return "Curse of Vanishing";
		return null;
	}

	public static ItemStack setUnbreakable(ItemStack is)
	{
		ItemMeta im = is.getItemMeta();
		im.spigot().setUnbreakable(true);
		is.setItemMeta(im);
		return is;
	}

	/**
	 * Tries to find an enchantment with regards to a string
	 * @param enchName - The enchant's name
	 * @return The enchantment got from the name
	 */
	public static Enchantment getEnchantment(String enchName)
	{
		if (enchName.equalsIgnoreCase("protection"))
			return Enchantment.getById(0);
		if (enchName.equalsIgnoreCase("prot"))
			return Enchantment.getById(0);
		if (enchName.equalsIgnoreCase("fireprotection"))
			return Enchantment.getById(1);
		if (enchName.equalsIgnoreCase("fireprot"))
			return Enchantment.getById(1);
		if (enchName.equalsIgnoreCase("featherfalling"))
			return Enchantment.getById(2);
		if (enchName.equalsIgnoreCase("featherfall"))
			return Enchantment.getById(2);
		if (enchName.equalsIgnoreCase("blastprotection"))
			return Enchantment.getById(3);
		if (enchName.equalsIgnoreCase("blastprot"))
			return Enchantment.getById(3);
		if (enchName.equalsIgnoreCase("projectileprot"))
			return Enchantment.getById(4);
		if (enchName.equalsIgnoreCase("projectileprotection"))
			return Enchantment.getById(4);
		if (enchName.equalsIgnoreCase("respiration"))
			return Enchantment.getById(5);
		if (enchName.equalsIgnoreCase("aquaaffinity"))
			return Enchantment.getById(6);
		if (enchName.equalsIgnoreCase("affinity"))
			return Enchantment.getById(6);
		if (enchName.equalsIgnoreCase("thorns"))
			return Enchantment.getById(7);
		if (enchName.equalsIgnoreCase("depthstrider"))
			return Enchantment.getById(8);
		if (enchName.equalsIgnoreCase("frostwalker"))
			return Enchantment.getById(9);
		if (enchName.equalsIgnoreCase("curseofbinding"))
			return Enchantment.getById(10);
		if (enchName.equalsIgnoreCase("sharp"))
			return Enchantment.getById(16);
		if (enchName.equalsIgnoreCase("sharpness"))
			return Enchantment.getById(16);
		if (enchName.equalsIgnoreCase("smite"))
			return Enchantment.getById(17);
		if (enchName.equalsIgnoreCase("bane"))
			return Enchantment.getById(18);
		if (enchName.equalsIgnoreCase("arthropods"))
			return Enchantment.getById(18);
		if (enchName.equalsIgnoreCase("baneofarthropods"))
			return Enchantment.getById(18);
		if (enchName.equalsIgnoreCase("knock"))
			return Enchantment.getById(19);
		if (enchName.equalsIgnoreCase("knockback"))
			return Enchantment.getById(19);
		if (enchName.equalsIgnoreCase("fire"))
			return Enchantment.getById(20);
		if (enchName.equalsIgnoreCase("fireaspect"))
			return Enchantment.getById(20);
		if (enchName.equalsIgnoreCase("loot"))
			return Enchantment.getById(21);
		if (enchName.equalsIgnoreCase("looting"))
			return Enchantment.getById(21);
		if (enchName.equalsIgnoreCase("sweepingedge"))
			return Enchantment.getById(22);
		if (enchName.equalsIgnoreCase("efficiency"))
			return Enchantment.getById(32);
		if (enchName.equalsIgnoreCase("silk"))
			return Enchantment.getById(33);
		if (enchName.equalsIgnoreCase("silktouch"))
			return Enchantment.getById(33);
		if (enchName.equalsIgnoreCase("unbreaking"))
			return Enchantment.getById(34);
		if (enchName.equalsIgnoreCase("fortune"))
			return Enchantment.getById(35);
		if (enchName.equalsIgnoreCase("power"))
			return Enchantment.getById(48);
		if (enchName.equalsIgnoreCase("punch"))
			return Enchantment.getById(49);
		if (enchName.equalsIgnoreCase("flame"))
			return Enchantment.getById(50);
		if (enchName.equalsIgnoreCase("infinity"))
			return Enchantment.getById(51);
		if (enchName.equalsIgnoreCase("luckofthesea"))
			return Enchantment.getById(61);
		if (enchName.equalsIgnoreCase("lure"))
			return Enchantment.getById(62);
		if (enchName.equalsIgnoreCase("mending"))
			return Enchantment.getById(70);
		if (enchName.equalsIgnoreCase("cureofvanishing"))
			return Enchantment.getById(71);
		return Enchantment.getByName(enchName);
	}

	/**
	 * Breaks a block with effects and sounds like it was broken by another person
	 * @param block - The block to break
	 * @param drops - Whether to drop the block when broken or not
	 */
	public static void breakBlock(Block block, boolean drops)
	{
		if (drops)
		{
			for (ItemStack item: block.getDrops())
				block.getWorld().dropItemNaturally(block.getLocation(), item);
		}
		block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getTypeId());
		block.setType(Material.AIR);
	}

	public static String getFinalArg(final String[] args, final int start)
	{
		final StringBuilder bldr = new StringBuilder();
		for (int i = start; i < args.length; i++)
		{
			if (i != start)
			{
				bldr.append(" ");
			}
			bldr.append(args[i]);
		}
		return bldr.toString();
	}

	public static double getDirectionX(Player p, double multiplier)
	{
		return p.getLocation().getDirection().getX() * multiplier;
	}

	public static double getDirectionY(Player p, double multiplier)
	{
		return p.getLocation().getDirection().getY() * multiplier;
	}

	public static double getDirectionZ(Player p, double multiplier)
	{
		return p.getLocation().getDirection().getZ() * multiplier;
	}

	public static void equipEntity(LivingEntity e, ItemStack eq, float chance)
	{
		EntityEquipment ee = e.getEquipment();
		int id = eq.getTypeId();
		if (id >= 298 && id <= 317)
		{
			if (id == 314 || id == 310 || id == 306 || id == 302 || id == 298)
			{
				ee.setHelmet(eq);
				ee.setHelmetDropChance(chance);
			} else if (id == 315 || id == 311 || id == 307 || id == 303 || id == 299) {
				ee.setChestplate(eq);
				ee.setChestplateDropChance(chance);
			} else if (id == 316 || id == 312 || id == 308 || id == 304 || id == 300) {
				ee.setLeggings(eq);
				ee.setLeggingsDropChance(chance);
			} else if (id == 317 || id == 313 || id == 309 || id == 305 || id == 301) {
				ee.setBoots(eq);
				ee.setBootsDropChance(chance);
			}
		} else {
			ee.setItemInMainHand(eq);
			ee.setItemInMainHandDropChance(chance);
		}
	}

	public static void equipEntityHelmet(LivingEntity e, ItemStack eq, float chance)
	{
		EntityEquipment ee = e.getEquipment();
		ee.setHelmet(eq);
		ee.setHelmetDropChance(chance);
	}

	public static void equipEntityChestplate(LivingEntity e, ItemStack eq, float chance)
	{
		EntityEquipment ee = e.getEquipment();
		ee.setChestplate(eq);
		ee.setChestplateDropChance(chance);
	}

	public static void equipEntityLeggings(LivingEntity e, ItemStack eq, float chance)
	{
		EntityEquipment ee = e.getEquipment();
		ee.setLeggings(eq);
		ee.setLeggingsDropChance(chance);
	}

	public static void equipEntityShoes(LivingEntity e, ItemStack eq, float chance)
	{
		EntityEquipment ee = e.getEquipment();
		ee.setBoots(eq);
		ee.setBootsDropChance(chance);
	}

	/**
	 * Calculates damage with respect to worn armor; doesn't calculate protection enchants because Bukkit calculates it even with entity.damage(arg);
	 * @param damage - The raw damage that is not armored
	 * @param armorContents - The armor contents of the damage taker
	 * @return The damage calculated with the armor
	 */
	public static int calculateDamageWithArmor(int damage, ItemStack[] armorContents)
	{
		int armorLevel = 0;
		for (ItemStack armor: armorContents)
		{
			if (armor.getType() == Material.LEATHER_HELMET)
				armorLevel += 1;
			if (armor.getType() == Material.LEATHER_CHESTPLATE)
				armorLevel += 3;
			if (armor.getType() == Material.LEATHER_LEGGINGS)
				armorLevel += 2;
			if (armor.getType() == Material.LEATHER_BOOTS)
				armorLevel += 1;

			if (armor.getType() == Material.GOLD_HELMET)
				armorLevel += 2;
			if (armor.getType() == Material.GOLD_CHESTPLATE)
				armorLevel += 5;
			if (armor.getType() == Material.GOLD_LEGGINGS)
				armorLevel += 3;
			if (armor.getType() == Material.GOLD_BOOTS)
				armorLevel += 1;

			if (armor.getType() == Material.CHAINMAIL_HELMET)
				armorLevel += 2;
			if (armor.getType() == Material.CHAINMAIL_CHESTPLATE)
				armorLevel += 5;
			if (armor.getType() == Material.CHAINMAIL_LEGGINGS)
				armorLevel += 4;
			if (armor.getType() == Material.CHAINMAIL_BOOTS)
				armorLevel += 1;

			if (armor.getType() == Material.IRON_HELMET)
				armorLevel += 2;
			if (armor.getType() == Material.IRON_CHESTPLATE)
				armorLevel += 6;
			if (armor.getType() == Material.IRON_LEGGINGS)
				armorLevel += 5;
			if (armor.getType() == Material.IRON_BOOTS)
				armorLevel += 2;

			if (armor.getType() == Material.DIAMOND_HELMET)
				armorLevel += 3;
			if (armor.getType() == Material.DIAMOND_CHESTPLATE)
				armorLevel += 8;
			if (armor.getType() == Material.DIAMOND_LEGGINGS)
				armorLevel += 6;
			if (armor.getType() == Material.DIAMOND_BOOTS)
				armorLevel += 3;
		}
		int damagePercentage = 100 - (armorLevel * 4);
		int armoredDamage = damage * damagePercentage / 100;
		return armoredDamage;
	}

	public static void strikeLightning(Location location)
	{
		for (Player p: getNearbyPlayers(location, 64))
			strikeLightningForPlayer(p, location);
	}

	public static void strikeLightningForPlayer(Player player, Location loc)
	{
		EntityLightning el = new EntityLightning(((CraftWorld) player.getWorld()).getHandle(), loc.getX(), loc.getY(), loc.getZ(), true);
		PacketPlayOutSpawnEntityWeather packet = new PacketPlayOutSpawnEntityWeather(el);
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
		player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_THUNDER, 0.75F, 1.0F);
	}

	/**
	 * Reads a file and returns the lines read in an ArrayList and ignores lines that starts with '#' 
	 * @param file - The file to read 
	 * @return The lines read
	 */
	public static ArrayList<String> readFile(File file)
	{
		ArrayList<String> readLines = new ArrayList<String>();
		try
		{
			BufferedReader bReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
			for (String s = ""; (s = bReader.readLine()) != null;)
			{
				if (s.startsWith("#"))
					continue;
				readLines.add(s);
			}
			bReader.close();
		} catch (Exception e) {
		}
		return readLines;
	}

	/**
	 * Prints lines of text to a file
	 * @param lines - The lines wanted to be printed out to the file
	 * @param file - The file to print the lines to
	 */
	public static void writeFile(ArrayList<String> lines, File file)
	{
		try
		{
			createFolderDirectory(file);
			file.createNewFile();
			BufferedWriter pWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
			for (int i = 0; i < lines.size(); i++)
			{
				pWriter.write(lines.get(i));
				if (i != lines.size())
					pWriter.newLine();
			}
			pWriter.close();
		} catch (Exception e) {
		}
	}

	/**
	 * 
	 * @param file -  The folder of the file you want to create
	 * @return Boolean of if the folder was created or not (it won't if a folder is already there)
	 */
	public static void createFolderDirectory(File file)
	{
		try
		{
			/*
			String[] sM = file.getPath().split("/");
			if (sM.length == 0)
				return;
			String directory;
			directory = file.getPath().replace("\\" + file.getName(), "");
			File folder = new File(directory);
			if (!folder.exists())
				folder.mkdirs();
			 */
			File parent = file.getParentFile();
			if (parent != null)
				parent.mkdirs();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static ArrayList<Entity> sortAscend(ArrayList<Entity> list, ArrayList<Double> index)
	{
		ArrayList<Entity> entities = new ArrayList<Entity>();
		ArrayList<Entity> entities1 = new ArrayList<Entity>();
		for (Entity o: list)
			entities1.add(o);
		for (int i = 0; i < index.size(); i++)
		{
			Entity nearest = null;
			double distance = 2147483647;
			for (Entity o: entities1)
			{
				if (o == null)
					continue;
				double dist = index.get(i);
				if (dist < distance || nearest == null)
				{
					nearest = o;
					distance = dist;
				}
			}
			if (nearest != null)
			{
				entities1.remove(nearest);
				if (!entities.contains(nearest))
					entities.add(nearest);
			}
		}
		return entities;
	}


	/**
	 * 
	 * @param loc - The target location
	 * @param radius - How far you want it to detect
	 * @return An ArrayList of living entities near the target location
	 */
	public static ArrayList<LivingEntity> getNearbyLivingEntities(Location loc, double radius)
	{
		radius = Math.pow(radius + 1, 2);
		ArrayList<LivingEntity> nearbyEntities = new ArrayList<LivingEntity>();
		for (Entity entity: loc.getWorld().getEntities())
		{
			if (entity instanceof ArmorStand)
				continue;
			if (!(entity instanceof LivingEntity))
				continue;
			if (entity.isDead())
				continue;
			LivingEntity lEntity = (LivingEntity) entity;
			if (lEntity.hasMetadata("NPC"))
				continue;
			Location eLoc = lEntity.getLocation().add(0, lEntity.getEyeHeight() / 2.0D, 0);

			double distanceSq = Math.pow(eLoc.getX() - loc.getX(), 2) 
					+ Math.pow(eLoc.getY() - loc.getY(), 2) 
					+ Math.pow(eLoc.getZ() - loc.getZ(), 2);
			if (distanceSq < radius)
				nearbyEntities.add((LivingEntity) entity);
		}
		return nearbyEntities;
	}

	/**
	 * 
	 * @param loc - The target location
	 * @param radius - How far you want it to detect
	 * @return An ArrayList of players near the target location
	 */
	public static ArrayList<Player> getNearbyPlayers(Location loc, double radius)
	{
		radius = Math.pow(radius + 1, 2);
		ArrayList<Player> nearbyEntities = new ArrayList<Player>();
		for (Player entity: Bukkit.getOnlinePlayers())
		{
			if (((Player) entity).hasMetadata("NPC"))
				continue;
			if (entity.isDead())
				continue;
			Location eLoc = entity.getLocation().add(0, entity.getEyeHeight() / 2.0D, 0);

			double distanceSq = Math.pow(eLoc.getX() - loc.getX(), 2) 
					+ Math.pow(eLoc.getY() - loc.getY(), 2) 
					+ Math.pow(eLoc.getZ() - loc.getZ(), 2);
			if (distanceSq < radius)
				nearbyEntities.add(entity);
		}
		return nearbyEntities;
	}


	/**
	 * 
	 * @param loc - The target location
	 * @param radius - How far you want it to detect
	 * @return An ArrayList of entities near the target location
	 */
	public static ArrayList<Entity> getNearbyEntities(Location loc, double radius)
	{
		radius = Math.pow(radius + 1, 2);
		ArrayList<Entity> nearbyEntities = new ArrayList<Entity>();
		for (Entity entity: loc.getWorld().getEntities())
		{
			if (entity instanceof ArmorStand)
				continue;
			if (entity instanceof LivingEntity)
				if (((LivingEntity) entity).hasMetadata("NPC"))
					continue;
			if (entity.isDead())
				continue;
			Location eLoc = entity.getLocation();

			double distanceSq = Math.pow(eLoc.getX() - loc.getX(), 2) 
					+ Math.pow(eLoc.getY() - loc.getY(), 2) 
					+ Math.pow(eLoc.getZ() - loc.getZ(), 2);
			if (distanceSq < radius)
				nearbyEntities.add(entity);
		}
		return nearbyEntities;
	}

	/**
	 * 
	 * @param to - The location to look at
	 * @param from - From where
	 * @return The yaw and pitch calculated
	 */
	public static float[] aimEntity(Location from, Location to)
	{
		float[] yawPitch = new float[2];
		double d = to.getX() - from.getX();
		double d2 = to.getZ() - from.getZ();
		double d1 = from.getY() - to.getY();
		double d3 = Math.sqrt(d * d + d2 * d2);
		float f = (float)((Math.atan2(d2, d) * 180D) / Math.PI) - 90F;
		float f1 = (float)(-((Math.atan2(d1, d3) * 180D) / Math.PI));
		yawPitch[0] = f;
		yawPitch[1] = -f1;
		return yawPitch;
	}

	/**
	 * Takes a string and returns a name with the closest match
	 * @param rawName - The raw name that you want to complete
	 * @return The name that has the closest match with rawName
	 */
	public static String completeName(String rawName)
	{
		for (Player player: Bukkit.getOnlinePlayers())
		{
			if (player.getName().equalsIgnoreCase(rawName))
				return player.getName();
		}
		for (Player player: Bukkit.getOnlinePlayers())
		{
			if (player.getName().toLowerCase().startsWith(rawName.toLowerCase()))
				return player.getName();
		}
		for (Player player: Bukkit.getOnlinePlayers())
		{
			if (player.getName().toLowerCase().contains(rawName.toLowerCase()))
				return player.getName();
		}
		for (OfflinePlayer oPlayer: Bukkit.getOfflinePlayers())
		{
			if (oPlayer.getName().toLowerCase().equalsIgnoreCase(rawName.toLowerCase()))
				return oPlayer.getName();
		}
		for (OfflinePlayer oPlayer: Bukkit.getOfflinePlayers())
		{
			if (oPlayer.getName().toLowerCase().startsWith(rawName.toLowerCase()))
				return oPlayer.getName();
		}
		for (OfflinePlayer oPlayer: Bukkit.getOfflinePlayers())
		{
			if (oPlayer.getName().toLowerCase().contains(rawName.toLowerCase()))
				return oPlayer.getName();
		}
		return rawName;
	}

	public static Player findPlayer(String nameToComplete)
	{
		return Bukkit.getPlayer(completeName(nameToComplete));
	}

	public static Player findPlayer(UUID uuid)
	{
		return Bukkit.getPlayer(uuid);
	}

	public static String seperateNumberWithCommas(double number, boolean onedp)
	{
		String numberString = new BigDecimal(number).toPlainString();
		if (numberString.contains("\\.") && onedp)
			numberString = numberString.split("\\.")[0];
		int remainder = numberString.length() % 3;
		String numberBuild = "";
		int mode = 0;
		for (int i = 0; i < numberString.toCharArray().length; i++)
		{
			int index = i + 1;
			char character = numberString.toCharArray()[i];
			if (character == '.')
				mode = 1;
			if (mode == 1)
				continue;
			if (remainder == 1 && index == 1)
			{
				numberBuild += character + ",";
				continue;
			}
			if (index == numberString.toCharArray().length)
			{
				numberBuild += character;
				continue;
			}
			if (remainder == 1 && i % 3 == 0)
			{
				numberBuild += character + ",";
				continue;
			}
			if (remainder == 2 && (i - 1) % 3 == 0)
			{
				numberBuild += character + ",";
				continue;
			}
			if (remainder == 0 && (i - 2) % 3 == 0)
			{
				numberBuild += character + ",";
				continue;
			}
			numberBuild += character;
		}
		return numberBuild;
	}
}