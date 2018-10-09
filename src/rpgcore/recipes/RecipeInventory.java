package rpgcore.recipes;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import rpgcore.item.EnhancementInventory;
import rpgcore.item.RItem;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGCore;
import rpgcore.player.RPlayer;

public class RecipeInventory 
{
	static final String inventoryName = CakeLibrary.recodeColorCodes("&eRecipe Book");

	public static void handleRecipeUnlock(ItemStack obtain, RPlayer rp)
	{
		int size = rp.recipes.size();
		String name = CakeLibrary.getItemName(obtain);
		boolean hasColor = CakeLibrary.hasColor(name);
		name = CakeLibrary.removeColorCodes(name);
		if (obtain.getType().equals(Material.IRON_INGOT))
		{
			for (RPGRecipe recipe: RPGRecipe.recipes)
				if (recipe.requiresUnlock)
					if (!recipe.cannotUnlock)
						if (recipe.result.databaseName.startsWith("Iron")
								&& !rp.recipes.contains(recipe.result.databaseName))
							rp.recipes.add(recipe.result.databaseName);
		} else if (hasColor && name.equals("Calcite"))
		{
			for (RPGRecipe recipe: RPGRecipe.recipes)
				if (recipe.requiresUnlock)
					if (!recipe.cannotUnlock)
						if (recipe.result.databaseName.startsWith("Calcite")
								&& !rp.recipes.contains(recipe.result.databaseName))
					rp.recipes.add(recipe.result.databaseName);
		} else if (hasColor && name.equals("Platinum"))
		{
			for (RPGRecipe recipe: RPGRecipe.recipes)
				if (recipe.requiresUnlock)
					if (!recipe.cannotUnlock)
						if (recipe.result.databaseName.startsWith("Platinum")
								&& !rp.recipes.contains(recipe.result.databaseName))
					rp.recipes.add(recipe.result.databaseName);
		} else if (hasColor && name.equals("Topaz"))
		{
			for (RPGRecipe recipe: RPGRecipe.recipes)
				if (recipe.requiresUnlock)
					if (!recipe.cannotUnlock)
						if (recipe.result.databaseName.startsWith("Topaz")
								&& !rp.recipes.contains(recipe.result.databaseName))
					rp.recipes.add(recipe.result.databaseName);
		} else if (obtain.getType().equals(Material.DIAMOND))
		{
			for (RPGRecipe recipe: RPGRecipe.recipes)
				if (recipe.requiresUnlock)
					if (!recipe.cannotUnlock)
						if (recipe.result.databaseName.startsWith("Diamond")
								&& !rp.recipes.contains(recipe.result.databaseName))
					rp.recipes.add(recipe.result.databaseName);
		}else if (hasColor && name.equals("Sapphire"))
		{
			for (RPGRecipe recipe: RPGRecipe.recipes)
				if (recipe.requiresUnlock)
					if (!recipe.cannotUnlock)
						if (recipe.result.databaseName.startsWith("Sapphire")
								&& !rp.recipes.contains(recipe.result.databaseName))
					rp.recipes.add(recipe.result.databaseName);
		}
		if (rp.recipes.size() != size)
		{
			RPGCore.msg(rp.getPlayer(), "There are new recipes in your recipe book!");
			RPGCore.playerManager.writeData(rp);
		}
	}

	public static ArrayList<RPGRecipe> getPlayerRecipes(RPlayer rp)
	{
		ArrayList<RPGRecipe> recipes = new ArrayList<RPGRecipe>();
		for (RPGRecipe recipe: RPGRecipe.recipes)
			if (!recipe.cannotUnlock && (!recipe.requiresUnlock || rp.recipes.contains(recipe.result.databaseName)))
				recipes.add(recipe);
		return recipes;
	}

	public static void handleRecipeBookOpen(RPlayer rp)
	{
		if (rp.getPlayer() == null)
			return;
		rp.viewableRecipes = getPlayerRecipes(rp);
		rp.getPlayer().openInventory(getRecipeBookInventory(rp.viewableRecipes, rp.lastRecipeBookPage));
	}

	public static Inventory getRecipeBookInventory(ArrayList<RPGRecipe> viewableRecipes, int page)
	{
		Inventory inv = Bukkit.createInventory(null, 54, inventoryName);
		setBlanks(inv);
		setInvPageContents(inv, viewableRecipes, 1);
		return inv;
	}

	public static void setBlanks(Inventory inv)
	{
		for (int i = 0; i < 36; i++)
		{
			if ((i >= 1 && i <= 3) || (i >= 10 && i <= 12) || (i >= 19 && i <= 21) 
					|| i == 5 || i == 14 || i == 23 || (i >= 32 && i <= 35)
					|| i == 16)
				continue;
			inv.setItem(i, EnhancementInventory.slotBlack.clone());
		}
	}

	public static void setInvPageContents(Inventory inv, ArrayList<RPGRecipe> viewableRecipes, int page)
	{
		inv.setItem(45 + 3, getPrevPageItem(viewableRecipes, page));
		inv.setItem(45 + 4, getCurrentPageItem(viewableRecipes, page));
		inv.setItem(45 + 5, getNextPageItem(viewableRecipes, page));
		for (int i = (page - 1) * 9; i < page * 9; i++)
			inv.setItem(36 + i - ((page - 1) * 9), 
					i >= viewableRecipes.size() ? new ItemStack(Material.AIR) : viewableRecipes.get(i).result.createItem());
	}

	public static void setInvRecipeDetails(Inventory inv, RPGRecipe recipe)
	{
		// . I I I . C . R .
		for (int i = 0; i < 27; i++)
			if ((i >= 1 && i <= 3) || (i >= 10 && i <= 12) || (i >= 19 && i <= 21))
				inv.setItem(i, new ItemStack(Material.AIR));
		for (int i = 0; i < recipe.shape.length; i++)
		{
			char[] arr = recipe.shape[i].toCharArray();
			for (int i1 = 0; i1 < arr.length; i1++)
			{
				ItemStack is = null;
				for (RItem ri: recipe.ingredients.keySet())
					if (recipe.ingredients.get(ri) == arr[i1])
						is = ri.createItem();
				if (is != null)
				{
					is.setAmount(1);
					inv.setItem((i * 9) + 1 + i1, is);
				}
			}
		}
		inv.setItem(9 + 7, recipe.getResult());
	}

	static ItemStack getCurrentPageItem(ArrayList<RPGRecipe> viewableRecipes, int page)
	{
		return CakeLibrary.renameItem(new ItemStack(Material.ENCHANTED_BOOK), 
				"&ePage: " + page + "/" + ((viewableRecipes.size() / 9) + 1));
	}

	static ItemStack getNextPageItem(ArrayList<RPGRecipe> viewableRecipes, int page)
	{
		return CakeLibrary.editNameAndLore(new ItemStack(Material.BOOK), 
				(page >= (((viewableRecipes.size() - 1) / 9) + 1) ? "&8" : "&6&n") + "Next Page -->",
				"&7 * Page " + page + "/" + ((viewableRecipes.size() / 9) + 1));
	}

	static ItemStack getPrevPageItem(ArrayList<RPGRecipe> viewableRecipes, int page)
	{
		return CakeLibrary.editNameAndLore(new ItemStack(Material.BOOK), 
				(page <= 1 ? "&8" : "&6&n") + "<-- Previous Page",
				"&7 * Page " + page + "/" + ((viewableRecipes.size() / 9) + 1));
	}
}
