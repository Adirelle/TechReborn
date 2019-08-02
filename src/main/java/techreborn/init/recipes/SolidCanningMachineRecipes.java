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

package techreborn.init.recipes;

import reborncore.api.praescriptum.recipes.RecipeHandler;

import techreborn.api.recipe.Recipes;
import techreborn.items.ingredients.ItemParts;

/**
 * @author estebes
 */
public class SolidCanningMachineRecipes {
	public static void init() {
		Recipes.solidCanningMachine = new RecipeHandler();

		Recipes.solidCanningMachine.createRecipe()
			.withInput("plateRefinedIron")
			.withInput("plateElectrum", 2)
			.withOutput(ItemParts.getPartByName("basic_circuit_board", 2))
			.withEnergyCostPerTick(1)
			.withOperationDuration(800)
			.register();
	}
}
