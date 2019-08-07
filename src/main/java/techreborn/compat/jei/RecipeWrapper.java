/*
 * This file is part of TechReborn, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2018 TechReborn
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package techreborn.compat.jei;

import net.minecraft.item.ItemStack;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import reborncore.api.praescriptum.ingredients.input.FluidStackInputIngredient;
import reborncore.api.praescriptum.ingredients.input.ItemStackInputIngredient;
import reborncore.api.praescriptum.ingredients.input.OreDictionaryInputIngredient;
import reborncore.api.praescriptum.ingredients.output.FluidStackOutputIngredient;
import reborncore.api.praescriptum.ingredients.output.ItemStackOutputIngredient;
import reborncore.api.praescriptum.recipes.Recipe;
import reborncore.common.util.ItemUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author estebes
 */
public abstract class RecipeWrapper implements IRecipeWrapper {
	public RecipeWrapper(Recipe recipe) {
		this.recipe = recipe;

		// inputs
		recipe.getInputIngredients().stream()
			.filter(entry -> entry instanceof ItemStackInputIngredient)
			.map(entry -> (ItemStack) entry.ingredient)
			.collect(Collectors.toCollection(() -> itemInputs)); // map ItemStacks

		recipe.getInputIngredients().stream()
			.filter(entry -> entry instanceof OreDictionaryInputIngredient)
			.flatMap(entry -> OreDictionary.getOres((String) entry.ingredient).stream()
				.map(stack -> copyWithSize(stack, entry.getCount())))
			.collect(Collectors.toCollection(() -> itemInputs)); // map OreDictionary entries

		recipe.getInputIngredients().stream()
			.filter(entry -> entry instanceof FluidStackInputIngredient)
			.map(entry -> (FluidStack) entry.ingredient)
			.collect(Collectors.toCollection(() -> fluidInputs)); // map FluidStacks

		// outputs
		recipe.getOutputIngredients().stream()
			.filter(entry -> entry instanceof ItemStackOutputIngredient)
			.map(entry -> (ItemStack) entry.ingredient)
			.collect(Collectors.toCollection(() -> itemOutputs)); // map ItemStacks

		recipe.getOutputIngredients().stream()
			.filter(entry -> entry instanceof FluidStackOutputIngredient)
			.map(entry -> (FluidStack) entry.ingredient)
			.collect(Collectors.toCollection(() -> fluidOutputs)); // map FluidStacks
	}

	@Override
	public void getIngredients(IIngredients ingredients) {
		ingredients.setInputs(VanillaTypes.ITEM, itemInputs);
		ingredients.setInputs(VanillaTypes.FLUID, fluidInputs);
		ingredients.setOutputs(VanillaTypes.ITEM, itemOutputs);
		ingredients.setOutputs(VanillaTypes.FLUID, fluidOutputs);
	}

	public static ItemStack copyWithSize(ItemStack stack, int size) {
		if (ItemUtils.isEmpty(stack)) return ItemStack.EMPTY;

		return ItemUtils.setSize(stack.copy(), size);
	}

	// Fields >>
	protected final Recipe recipe;

	protected final List<ItemStack> itemInputs = new ArrayList<>();
	protected final List<FluidStack> fluidInputs = new ArrayList<>();
	protected final List<ItemStack> itemOutputs = new ArrayList<>();
	protected final List<FluidStack> fluidOutputs = new ArrayList<>();
	// << Fields
}
