package wanion.jeihider;

/*
 * Created by WanionCane(https://github.com/WanionCane).
 */

import mezz.jei.api.*;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.gui.Focus;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Loader;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@JEIPlugin
public class JEIHider implements IModPlugin
{
    private IItemBlacklist itemBlacklist;
    private IIngredientRegistry iItemRegistry;
    private final List<ItemStack> itemStacksToHide = new ArrayList<>();
    private final boolean hideRecyclerRecipes;
    private final boolean hideEnchantmentRecipes;
    private final Class<? extends IRecipeCategory> recyclerCategoryClass;
    private final Class<? extends IRecipeCategory> enchantmentCategoryClass;

    @SuppressWarnings("unchecked")
    public JEIHider() throws ClassNotFoundException
    {
        final Configuration config = new Configuration(new File("." + File.separatorChar + "config" + File.separatorChar + "jeih.cfg"));
        hideRecyclerRecipes = Loader.isModLoaded("IC2") && config.get(Configuration.CATEGORY_GENERAL, "hideRecyclerRecipes", false).getBoolean();
        hideEnchantmentRecipes = Loader.isModLoaded("jeresources") && config.get(Configuration.CATEGORY_GENERAL, "hideEnchantmentRecipes", false).getBoolean();
        recyclerCategoryClass = hideRecyclerRecipes ? (Class<? extends IRecipeCategory>) Class.forName("ic2.jeiIntegration.recipe.misc.ScrapboxRecipeCategory") : null;
        enchantmentCategoryClass = hideEnchantmentRecipes ? (Class<? extends IRecipeCategory>) Class.forName("jeresources.jei.enchantment.EnchantmentCategory") : null;
        if (config.hasChanged())
            config.save();
    }

    @Override
    public void register(@Nonnull IModRegistry iModRegistry)
    {
        itemBlacklist = iModRegistry.getJeiHelpers().getItemBlacklist();
        iItemRegistry = iModRegistry.getIngredientRegistry();
    }

    @Override
    public void onRuntimeAvailable(@Nonnull IJeiRuntime iJeiRuntime)
    {
        if (itemStacksToHide.isEmpty()) {
            final IRecipeRegistry iRecipeRegistry = iJeiRuntime.getRecipeRegistry();
            for (final ItemStack itemStack : iItemRegistry.getIngredients(ItemStack.class)) {
                final List<IRecipeCategory> recipeCategoriesOfInput = iRecipeRegistry.getRecipeCategories(new Focus<ItemStack>(IFocus.Mode.INPUT, itemStack));
                final List<IRecipeCategory> recipeCategoriesOfOutput = iRecipeRegistry.getRecipeCategories(new Focus<ItemStack>(IFocus.Mode.OUTPUT, itemStack));
                if (recipeCategoriesOfInput.isEmpty() && recipeCategoriesOfOutput.isEmpty())
                    itemStacksToHide.add(itemStack);
                if ((hideRecyclerRecipes || hideEnchantmentRecipes) && !recipeCategoriesOfInput.isEmpty() && recipeCategoriesOfOutput.isEmpty()) {
                    if (recipeCategoriesOfInput.size() == 1) {
                        if (hideRecyclerRecipes && recipeCategoriesOfInput.get(0).getClass() == recyclerCategoryClass)
                            itemStacksToHide.add(itemStack);
                        else if (hideEnchantmentRecipes && recipeCategoriesOfInput.get(0).getClass() == enchantmentCategoryClass)
                            itemStacksToHide.add(itemStack);
                    } else if (recipeCategoriesOfInput.size() == 2 && hideRecyclerRecipes && hideEnchantmentRecipes)
                        if ((recipeCategoriesOfInput.get(0).getClass() == recyclerCategoryClass && recipeCategoriesOfInput.get(1).getClass() == enchantmentCategoryClass) ||
                                (recipeCategoriesOfInput.get(1).getClass() == recyclerCategoryClass && recipeCategoriesOfInput.get(0).getClass() == enchantmentCategoryClass))
                            itemStacksToHide.add(itemStack);
                }
            }
        }
        itemStacksToHide.forEach(itemBlacklist::addItemToBlacklist);
    }
}
