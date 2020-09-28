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
import java.util.stream.Collectors;

@JEIPlugin
public class JEIHider implements IModPlugin {
    private IItemBlacklist itemBlacklist;
    private IIngredientRegistry iItemRegistry;
    private final List<ItemStack> itemStacksToHide = new ArrayList<>();
    private final boolean hideRecyclerRecipes;
    private final boolean hideEnchantmentRecipes;
    private final boolean hideAspectRecipes;
    private final boolean hideAnvilRecipes;
    private final boolean hideEmbersAnvilRecipes;
    private final Class<? extends IRecipeCategory> recyclerCategoryClass;
    private final Class<? extends IRecipeCategory> enchantmentCategoryClass;
    private final Class<? extends IRecipeCategory> aspectCategoryClass;
    private final Class<? extends IRecipeCategory> anvilCategoryClass;
    private final Class<? extends IRecipeCategory> embersAnvilCategoryClass;

    @SuppressWarnings("unchecked")
    public JEIHider() throws ClassNotFoundException {
        final Configuration config = new Configuration(new File("." + File.separatorChar + "config" + File.separatorChar + "jeih.cfg"));
        hideRecyclerRecipes = Loader.isModLoaded("IC2") && config.get(Configuration.CATEGORY_GENERAL, "hideRecyclerRecipes", false).getBoolean();
        hideEnchantmentRecipes = Loader.isModLoaded("jeresources") && config.get(Configuration.CATEGORY_GENERAL, "hideEnchantmentRecipes", false).getBoolean();
        hideAspectRecipes = Loader.isModLoaded("thaumicjei") && config.get(Configuration.CATEGORY_GENERAL, "hideAspectRecipes", false).getBoolean();
        hideAnvilRecipes = config.get(Configuration.CATEGORY_GENERAL, "hideAnvilRecipes", false).getBoolean();
        hideEmbersAnvilRecipes = Loader.isModLoaded("embers") && config.get(Configuration.CATEGORY_GENERAL, "hideEmbersAnvilRecipes", false).getBoolean();
        recyclerCategoryClass = hideRecyclerRecipes ? (Class<? extends IRecipeCategory>) Class.forName("ic2.jeiIntegration.recipe.misc.ScrapboxRecipeCategory") : null;
        enchantmentCategoryClass = hideEnchantmentRecipes ? (Class<? extends IRecipeCategory>) Class.forName("jeresources.jei.enchantment.EnchantmentCategory") : null;
        aspectCategoryClass = hideAspectRecipes ? (Class<? extends IRecipeCategory>) Class.forName("com.buuz135.thaumicjei.category.AspectFromItemStackCategory") : null;
        anvilCategoryClass = hideAnvilRecipes ? (Class<? extends IRecipeCategory>) Class.forName("mezz.jei.plugins.vanilla.anvil.AnvilRecipeCategory") : null;
        embersAnvilCategoryClass = hideEmbersAnvilRecipes ? (Class<? extends IRecipeCategory>) Class.forName("teamroots.embers.compat.jei.category.DawnstoneAnvilCategory") : null;
        if (config.hasChanged())
            config.save();
    }

    @Override
    public void register(@Nonnull IModRegistry iModRegistry) {
        itemBlacklist = iModRegistry.getJeiHelpers().getItemBlacklist();
        iItemRegistry = iModRegistry.getIngredientRegistry();
    }

    @Override
    public void onRuntimeAvailable(@Nonnull IJeiRuntime iJeiRuntime) {
        if (itemStacksToHide.isEmpty()) {
            final IRecipeRegistry iRecipeRegistry = iJeiRuntime.getRecipeRegistry();
            for (final ItemStack itemStack : iItemRegistry.getIngredients(ItemStack.class)) {
                List<IRecipeCategory> recipeCategoriesOfInput = (
                        iRecipeRegistry.getRecipeCategories(new Focus<>(IFocus.Mode.INPUT, itemStack))
                ).stream().filter(cat -> !(
                        hideRecyclerRecipes && cat.getClass() == recyclerCategoryClass
                        || hideEnchantmentRecipes && cat.getClass() == enchantmentCategoryClass
                        || hideAspectRecipes && cat.getClass() == aspectCategoryClass
                        || hideAnvilRecipes && cat.getClass() == anvilCategoryClass
                        || hideEmbersAnvilRecipes && cat.getClass() == embersAnvilCategoryClass)
                ).collect(Collectors.toList());

                List<IRecipeCategory> recipeCategoriesOfOutput = (
                        iRecipeRegistry.getRecipeCategories(new Focus<>(IFocus.Mode.OUTPUT, itemStack))
                ).stream().filter(cat -> !(
                        hideAnvilRecipes && cat.getClass() == anvilCategoryClass
                        || hideEmbersAnvilRecipes && cat.getClass() == embersAnvilCategoryClass)
                ).collect(Collectors.toList());
                if (recipeCategoriesOfInput.isEmpty() && recipeCategoriesOfOutput.isEmpty())
                    itemStacksToHide.add(itemStack);
            }
        }
        itemStacksToHide.forEach(itemBlacklist::addItemToBlacklist);
    }
}