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

package techreborn.items.tool;

import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;


import reborncore.api.power.IEnergyItemInfo;
import reborncore.common.powerSystem.ExternalPowerSystems;
import reborncore.common.powerSystem.PowerSystem;
import reborncore.common.powerSystem.PoweredItemContainerProvider;
import reborncore.common.powerSystem.ItemPowerManager;
import reborncore.common.util.ItemDurabilityExtensions;
import reborncore.common.util.ItemUtils;
import techreborn.TechReborn;

import javax.annotation.Nullable;
import java.util.Random;

public class ItemChainsaw extends AxeItem implements IEnergyItemInfo, ItemDurabilityExtensions {

	public int maxCharge = 1;
	public int cost = 250;
	public float poweredSpeed = 20F;
	public float unpoweredSpeed = 2.0F;
	public int transferLimit = 100;
	public boolean isBreaking = false;

	public ItemChainsaw(ToolMaterials material, int energyCapacity, float unpoweredSpeed) {
		super(material, (int) material.getAttackDamage(), unpoweredSpeed, new Item.Settings().itemGroup(TechReborn.ITEMGROUP).stackSize(1));
		this.maxCharge = energyCapacity;
		this.blockBreakingSpeed = unpoweredSpeed;

		this.addProperty(new Identifier("techreborn", "animated"), new ItemPropertyGetter() {
			@Override
			@Environment(EnvType.CLIENT)
			public float call(ItemStack stack, @Nullable World worldIn, @Nullable LivingEntity entityIn) {
				if (!stack.isEmpty() && new ItemPowerManager(stack).getEnergyStored() >= cost
						&& entityIn != null && entityIn.getMainHandStack().equals(stack)) {
					return 1.0F;
				}
				return 0.0F;
			}
		});
	}

	// ItemAxe
	@Override
	public float getBlockBreakingSpeed(ItemStack stack, BlockState state) {
		if (new ItemPowerManager(stack).getEnergyStored() >= cost
				&& (state.getBlock().isToolEffective(state, ToolType.AXE) || state.getMaterial() == Material.WOOD)) {
			return this.poweredSpeed;
		} else {
			return super.getBlockBreakingSpeed(stack, state);
		}
	}

	// ItemTool
	@Override
	public boolean onBlockBroken(ItemStack stack, World worldIn, BlockState blockIn, BlockPos pos, LivingEntity entityLiving) {
		Random rand = new Random();
		if (rand.nextInt(EnchantmentHelper.getLevel(Enchantments.UNBREAKING, stack) + 1) == 0) {
			ItemPowerManager capEnergy = new ItemPowerManager(stack);

			capEnergy.extractEnergy(cost, false);
			ExternalPowerSystems.requestEnergyFromArmor(capEnergy, entityLiving);
		}
		return true;
	}

	@Override
	public boolean onEntityDamaged(ItemStack itemstack, LivingEntity entityliving, LivingEntity entityliving1) {
		return true;
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		return 1 - ItemUtils.getPowerForDurabilityBar(stack);
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return true;
	}

	@Override
	public int getRGBDurabilityForDisplay(ItemStack stack) {
		return PowerSystem.getDisplayPower().colour;
	}

	@Override
	public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
		return !(newStack.isEqualIgnoreTags(oldStack));
	}

	@Override
	public boolean isRepairable() {
		return false;
	}

	// IEnergyItemInfo
	@Override
	public int getCapacity() {
		return maxCharge;
	}

	@Override
	public int getMaxInput() {
		return transferLimit;
	}

	@Override
	public int getMaxOutput() {
		return 0;
	}

	@Override
	public int getEnchantability() {
		return 20;
	}
}
