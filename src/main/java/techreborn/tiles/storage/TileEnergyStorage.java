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

package techreborn.tiles.storage;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import reborncore.api.IToolDrop;
import reborncore.api.power.EnumPowerTier;
import reborncore.api.tile.InventoryProvider;
import reborncore.common.powerSystem.ExternalPowerSystems;
import reborncore.common.powerSystem.TilePowerAcceptor;
import reborncore.common.util.RebornInventory;
import techreborn.blocks.storage.BlockEnergyStorage;

/**
 * Created by Rushmead
 */
public class TileEnergyStorage extends TilePowerAcceptor 
		implements IToolDrop, InventoryProvider {

	public RebornInventory<TileEnergyStorage> inventory;
	public String name;
	public Block wrenchDrop;
	public EnumPowerTier tier;
	public int maxInput;
	public int maxOutput;
	public int maxStorage;

	public TileEnergyStorage(BlockEntityType<?> tileEntityType, String name, int invSize, Block wrenchDrop, EnumPowerTier tier, int maxInput, int maxOuput, int maxStorage) {
		super(tileEntityType);
		inventory = new RebornInventory<>(invSize, "Tile" + name, 64, this).withConfiguredAccess();
		this.wrenchDrop = wrenchDrop;
		this.tier = tier;
		this.name = name;
		this.maxInput = maxInput;
		this.maxOutput = maxOuput;
		this.maxStorage = maxStorage;
	}

	// TilePowerAcceptor
	@Override
	public void tick() {
		super.tick();
		if (!inventory.getInvStack(0).isEmpty()) {
			ItemStack stack = inventory.getInvStack(0);

			if (ExternalPowerSystems.isPoweredItem(stack)) {
				ExternalPowerSystems.chargeItem(this, stack);
			}
		}
		if (!inventory.getInvStack(1).isEmpty()) {
			charge(1);
		}
	}
	
	@Override
	public double getBaseMaxPower() {
		return maxStorage;
	}

	@Override
	public boolean canAcceptEnergy(Direction direction) {
		return getFacing() != direction;
	}
	
	@Override
	public boolean canProvideEnergy(Direction direction) {
		return getFacing() == direction;
	}

	@Override
	public double getBaseMaxOutput() {
		return maxOutput;
	}

	@Override
	public double getBaseMaxInput() {
		return maxInput;
	}

	// TileMachineBase
	@Override
	public void setFacing(Direction enumFacing) {
		world.setBlockState(pos, world.getBlockState(pos).with(BlockEnergyStorage.FACING, enumFacing));
	}
	
	@Override
	public Direction getFacingEnum() {
		Block block = world.getBlockState(pos).getBlock();
		if (block instanceof BlockEnergyStorage) {
			return ((BlockEnergyStorage) block).getFacing(world.getBlockState(pos));
		}
		return null;
	}
	
	@Override
	public boolean canBeUpgraded() {
		return false;
	}

	// IToolDrop
	@Override
	public ItemStack getToolDrop(PlayerEntity entityPlayer) {
		return new ItemStack(wrenchDrop);
	}
	
	// ItemHandlerProvider
	@Override
	public RebornInventory<TileEnergyStorage> getInventory() {
		return inventory;
	}
}
