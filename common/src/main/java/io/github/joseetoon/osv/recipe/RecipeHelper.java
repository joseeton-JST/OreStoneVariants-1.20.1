package io.github.joseetoon.osv.recipe;

import lombok.extern.log4j.Log4j2;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import io.github.joseetoon.osv.ModRegistries;
import io.github.joseetoon.osv.config.Cfg;
import io.github.joseetoon.osv.preset.data.RecipeSettings;

import java.util.ArrayList;
import java.util.List;

@Log4j2
public class RecipeHelper {

    public static void injectRecipes(final RecipeManager registry) {
        if (Cfg.furnaceRecipes()) {
            log.info("Injecting furnace recipes for {} items.", ModRegistries.ITEMS.size());
            injectAll(registry);
        }
    }

    private static void injectAll(final RecipeManager registry) {
        final List<RecipeHolder<?>> recipes = new ArrayList<>(registry.getRecipes());

        ModRegistries.ITEMS.forEach((id, variant) -> {
            final RecipeSettings.Checked recipe = variant.getPreset().getCheckedRecipe(registry);
            if (!recipe.isNone()) {
                final ResourceLocation smeltingId = ResourceLocation.fromNamespaceAndPath("osv", id.getPath() + "_smelting");
                final ResourceLocation blastingId = ResourceLocation.fromNamespaceAndPath("osv", id.getPath() + "_blasting");
                final AbstractCookingRecipe smelting = recipe.getRecipe(id, variant, false);
                final AbstractCookingRecipe blasting = recipe.getRecipe(id, variant, true);
                recipes.add(new RecipeHolder<>(smeltingId, smelting));
                recipes.add(new RecipeHolder<>(blastingId, blasting));
            }
        });
        registry.replaceRecipes(recipes);
    }
}
