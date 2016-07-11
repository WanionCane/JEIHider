package wanion.jeihider;

/*
 * Created by WanionCane(https://github.com/WanionCane).
 */

import mezz.jei.api.*;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@JEIPlugin
public class JEIHider implements IModPlugin
{
    private IItemBlacklist itemBlacklist;
    private IItemRegistry iItemRegistry;
    private final List<ItemStack> itemStacksToHide = new ArrayList<ItemStack>();

    @Override
    public void register(@Nonnull IModRegistry iModRegistry)
    {
        itemBlacklist = iModRegistry.getJeiHelpers().getItemBlacklist();
        iItemRegistry = iModRegistry.getItemRegistry();
    }

    @Override
    public void onRuntimeAvailable(@Nonnull IJeiRuntime iJeiRuntime)
    {
        if (itemStacksToHide.isEmpty()) {
            final IRecipeRegistry iRecipeRegistry = iJeiRuntime.getRecipeRegistry();
            for (final ItemStack itemStack : iItemRegistry.getItemList())
                if (iRecipeRegistry.getRecipeCategoriesWithInput(itemStack).isEmpty() && iRecipeRegistry.getRecipeCategoriesWithOutput(itemStack).isEmpty())
                    itemStacksToHide.add(itemStack);
        }
        for (final ItemStack itemStack : itemStacksToHide)
            itemBlacklist.addItemToBlacklist(itemStack);
    }
}