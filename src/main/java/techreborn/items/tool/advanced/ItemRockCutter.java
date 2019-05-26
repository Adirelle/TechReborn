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

package techreborn.items.tool.advanced;

import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;


import reborncore.api.power.IEnergyItemInfo;
import reborncore.common.powerSystem.ExternalPowerSystems;
import reborncore.common.powerSystem.PowerSystem;
import reborncore.common.powerSystem.PoweredItemContainerProvider;
import reborncore.common.powerSystem.forge.ForgePowerItemManager;
import reborncore.common.util.ItemUtils;
import techreborn.TechReborn;
import techreborn.config.ConfigTechReborn;
import techreborn.init.TRContent;

import javax.annotation.Nullable;
import java.util.Random;

public class ItemRockCutter extends PickaxeItem implements IEnergyItemInfo {

	public static final int maxCharge = ConfigTechReborn.RockCutterCharge;
	public int transferLimit = 1_000;
	public int cost = 500;

	// 400k FE with 1k FE\t charge rate
	public ItemRockCutter() {
		super(ToolMaterials.DIAMOND, 1, 1, new Item.Settings().itemGroup(TechReborn.ITEMGROUP).stackSize(1));
		blockBreakingSpeed = 16F;
	}

	// ItemPickaxe
	@Override
	public boolean isEffectiveOn(BlockState state) {
		if (Items.DIAMOND_PICKAXE.isEffectiveOn(state)) {
			return true;
		}
		return false;
	}

	@Override
	public float getBlockBreakingSpeed(ItemStack stack, BlockState state) {
		if (new ForgePowerItemManager(stack).getEnergyStored() < cost) {
			return 2F;
		} else {
			return Items.DIAMOND_PICKAXE.getBlockBreakingSpeed(stack, state);
		}
	}

	// ItemTool
	@Override
	public boolean onBlockBroken(ItemStack stack, World worldIn, BlockState blockIn, BlockPos pos, LivingEntity entityLiving) {
		Random rand = new Random();
		if (rand.nextInt(EnchantmentHelper.getLevel(Enchantments.UNBREAKING, stack) + 1) == 0) {
			ForgePowerItemManager capEnergy = new ForgePowerItemManager(stack);

			capEnergy.extractEnergy(cost, false);
			ExternalPowerSystems.requestEnergyFromArmor(capEnergy, entityLiving);
		}
		return true;
	}

	@Override
	public int getHarvestLevel(ItemStack stack, ToolType toolType, @Nullable PlayerEntity player, @Nullable BlockState blockState) {
		if (!stack.hasEnchantments()) {
			stack.addEnchantment(Enchantments.SILK_TOUCH, 1);
		}
		return super.getHarvestLevel(stack, toolType, player, blockState);
	}

	// Item
	@Override
	public boolean isRepairable() {
		return false;
	}

	@Override
	public void onCrafted(ItemStack stack, World worldIn, PlayerEntity playerIn) {
		if (!stack.hasEnchantments()) {
			stack.addEnchantment(Enchantments.SILK_TOUCH, 1);
		}
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
	@Nullable
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
		return new PoweredItemContainerProvider(stack);
	}

	@Override
	public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
		return !(newStack.isEqualIgnoreTags(oldStack));
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void appendItemsForGroup(ItemGroup par2ItemGroup, DefaultedList<ItemStack> itemList) {
		if (!isInItemGroup(par2ItemGroup)) {
			return;
		}
		ItemStack uncharged = new ItemStack(this);
		uncharged.addEnchantment(Enchantments.SILK_TOUCH, 1);
		ItemStack charged = new ItemStack(TRContent.ROCK_CUTTER);
		charged.addEnchantment(Enchantments.SILK_TOUCH, 1);
		ForgePowerItemManager capEnergy = new ForgePowerItemManager(charged);
		capEnergy.setEnergyStored(capEnergy.getMaxEnergyStored());

		itemList.add(uncharged);
//		itemList.add(charged);
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
}
