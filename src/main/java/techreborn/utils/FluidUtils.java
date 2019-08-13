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

package techreborn.utils;

import io.github.prospector.silk.fluid.FluidInstance;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import org.checkerframework.checker.nullness.qual.NonNull;
import reborncore.common.fluid.container.GenericFluidContainer;
import net.minecraft.fluid.Fluid;
import reborncore.common.fluid.container.ItemFluidInfo;
import reborncore.mixin.extensions.FluidBlockExtensions;

import java.util.List;
import java.util.stream.Collectors;

public class FluidUtils {

	@NonNull
	public static Fluid fluidFromBlock(Block block){
		if(block instanceof FluidBlockExtensions){
			return ((FluidBlockExtensions) block).getFluid();
		}
		return Fluids.EMPTY;
	}

	public static List<Fluid> getAllFluids() {
		return Registry.FLUID.stream().collect(Collectors.toList());
	}

	public static boolean drainContainers(GenericFluidContainer<Direction> target, Inventory inventory, int inputSlot, int outputSlot) {
		ItemStack inputStack = inventory.getInvStack(inputSlot);
		ItemStack outputStack = inventory.getInvStack(outputSlot);

		if (outputStack.getCount() >= outputStack.getMaxCount()) return false;
		if (FluidUtils.isContainerEmpty(inputStack)) return false;

		ItemFluidInfo itemFluidInfo = (ItemFluidInfo) inputStack.getItem();
		FluidInstance targetFluidInstance = target.getFluidInstance(null);
		Fluid currentFluid = targetFluidInstance.getFluid();

		if(currentFluid == Fluids.EMPTY || currentFluid == itemFluidInfo.getFluid(inputStack)) {
			int freeSpace = target.getCapacity(null) - targetFluidInstance.getAmount();

			if(!outputStack.isEmpty()){
				if(outputStack.getCount() + 1 >= outputStack.getMaxCount()){
					return false;
				}
			}

			if(freeSpace >= 1000){
				inputStack.decrement(1);
				targetFluidInstance.setFluid(itemFluidInfo.getFluid(inputStack));
				targetFluidInstance.addAmount(1000);

				if(outputStack.isEmpty()){
					inventory.setInvStack(outputSlot, itemFluidInfo.getEmpty());
				} else {
					outputStack.increment(1);
				}
			}
		}

		return true;
	}

	public static boolean fillContainers(GenericFluidContainer<Direction> source, Inventory inventory, int inputSlot, int outputSlot, Fluid fluidToFill) {
		ItemStack inputStack = inventory.getInvStack(inputSlot);
		ItemStack outputStack = inventory.getInvStack(outputSlot);
		
		if (!FluidUtils.isContainerEmpty(inputStack)) return false;

		ItemFluidInfo itemFluidInfo = (ItemFluidInfo) inputStack.getItem();
		FluidInstance sourceFluid = source.getFluidInstance(null);

		if(sourceFluid.getFluid() == Fluids.EMPTY || sourceFluid.getAmount() < 1000){
			return false;
		}

		if(!outputStack.isEmpty()){
			if (outputStack.getCount() >= outputStack.getMaxCount()) return false;

			if(!(outputStack.getItem() instanceof ItemFluidInfo)) return false;

			ItemFluidInfo outputFluidInfo = (ItemFluidInfo) outputStack.getItem();

			if(outputFluidInfo.getFluid(outputStack) != sourceFluid.getFluid()){
				return false;
			}
		}

		sourceFluid.subtractAmount(1000);

		if(outputStack.isEmpty()){
			inventory.setInvStack(outputSlot, itemFluidInfo.getFull(sourceFluid.getFluid()));
		} else {
			outputStack.increment(1);
		}

		inputStack.decrement(1);

		return false;
	}

	public static boolean fluidEquals(@NonNull Fluid fluid, @NonNull Fluid fluid1) {
		return fluid == fluid1;
	}

	public static FluidInstance getFluidStackInContainer(@NonNull ItemStack invStack) {
		return null;
	}
	
	public static boolean isContainerEmpty(ItemStack inputStack) {
		if (inputStack.isEmpty())
			return false;
		if (!(inputStack.getItem() instanceof ItemFluidInfo))
			return false;
		ItemFluidInfo itemFluidInfo = (ItemFluidInfo) inputStack.getItem();
		if (itemFluidInfo.getFluid(inputStack) != Fluids.EMPTY)
			return false;
		return true;
	}
}
