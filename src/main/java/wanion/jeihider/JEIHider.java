package ftblag.fluidcows.integration.jei;

/*
 * Created by WanionCane(https://github.com/WanionCane).
 */

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.gui.Focus;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class JEIHider implements IModPlugin
{
    private static final ResourceLocation UID = new ResourceLocation("jeihider", "jeihider");
    private final List<ItemStack> itemStacksToHide = new ArrayList<>();
    private final boolean hideRecyclerRecipes;
    private final boolean hideEnchantmentRecipes;
    private final Class<? extends IRecipeCategory> recyclerCategoryClass;
    private final Class<? extends IRecipeCategory> enchantmentCategoryClass;

    public static final String CATEGORY_GENERAL = "general";

    @SuppressWarnings("unchecked")
    public JEIHider() throws ClassNotFoundException
    {
        ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
        COMMON_BUILDER.comment("JEI Hider Configs").push(CATEGORY_GENERAL);

        ForgeConfigSpec.BooleanValue boolHideRecyclerRecipes = COMMON_BUILDER.define("hideRecyclerRecipes", false);
        ForgeConfigSpec.BooleanValue boolHideEnchantmentRecipes = COMMON_BUILDER.define("hideEnchantmentRecipes", false);

        COMMON_BUILDER.pop();
        CommentedFileConfig configData = CommentedFileConfig.builder(FMLPaths.CONFIGDIR.get().resolve("jeih.toml")).sync().autosave().writingMode(WritingMode.REPLACE).build();
        configData.load();
        COMMON_BUILDER.build().setConfig(configData);

        hideRecyclerRecipes = ModList.get().isLoaded("ic2") && boolHideRecyclerRecipes.get();
        hideEnchantmentRecipes = ModList.get().isLoaded("jeresources") && boolHideEnchantmentRecipes.get();
        recyclerCategoryClass = hideRecyclerRecipes ? (Class<? extends IRecipeCategory>) Class.forName("ic2.jeiIntegration.recipe.misc.ScrapboxRecipeCategory") : null;
        enchantmentCategoryClass = hideEnchantmentRecipes ? (Class<? extends IRecipeCategory>) Class.forName("jeresources.jei.enchantment.EnchantmentCategory") : null;
    }

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void onRuntimeAvailable(@Nonnull IJeiRuntime iJeiRuntime)
    {
        if (itemStacksToHide.isEmpty()) {
            final IRecipeManager iRecipeRegistry = iJeiRuntime.getRecipeManager();
            for (final ItemStack itemStack : iJeiRuntime.getIngredientManager().getAllIngredients(VanillaTypes.ITEM)) {
                final List<IRecipeCategory> recipeCategoriesOfInput = iRecipeRegistry.getRecipeCategories(new Focus<>(IFocus.Mode.INPUT, itemStack));
                final List<IRecipeCategory> recipeCategoriesOfOutput = iRecipeRegistry.getRecipeCategories(new Focus<>(IFocus.Mode.OUTPUT, itemStack));
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
        iJeiRuntime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM, itemStacksToHide);
    }
}
