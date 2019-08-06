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

package techreborn.blockentity.machine.tier3;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import reborncore.api.IToolDrop;
import reborncore.api.blockentity.InventoryProvider;
import reborncore.client.containerBuilder.IContainerProvider;
import reborncore.client.containerBuilder.builder.BuiltContainer;
import reborncore.client.containerBuilder.builder.ContainerBuilder;
import reborncore.common.powerSystem.PowerAcceptorBlockEntity;
import reborncore.common.registration.RebornRegister;
import reborncore.common.registration.config.ConfigRegistry;
import reborncore.common.util.RebornInventory;
import reborncore.common.util.ItemUtils;
import techreborn.TechReborn;
import techreborn.init.TRContent;
import techreborn.init.TRBlockEntities;

@RebornRegister(TechReborn.MOD_ID)
public class MatterFabricatorBlockEntity extends PowerAcceptorBlockEntity
		implements IToolDrop, InventoryProvider, IContainerProvider {

	@ConfigRegistry(config = "machines", category = "matter_fabricator", key = "MatterFabricatorMaxInput", comment = "Matter Fabricator Max Input (Value in EU)")
	public static int maxInput = 8192;
	@ConfigRegistry(config = "machines", category = "matter_fabricator", key = "MatterFabricatorMaxEnergy", comment = "Matter Fabricator Max Energy (Value in EU)")
	public static int maxEnergy = 10_000_000;
	@ConfigRegistry(config = "machines", category = "matter_fabricator", key = "MatterFabricatorFabricationRate", comment = "Matter Fabricator Fabrication Rate, amount of amplifier units per UUM")
	public static int fabricationRate = 6_000;
	@ConfigRegistry(config = "machines", category = "matter_fabricator", key = "MatterFabricatorEnergyPerAmp", comment = "Matter Fabricator EU per amplifier unit, multiply this with the rate for total EU")
	public static int energyPerAmp = 5;

	public RebornInventory<MatterFabricatorBlockEntity> inventory = new RebornInventory<>(12, "MatterFabricatorBlockEntity", 64, this);
	private int amplifier = 0;

	public MatterFabricatorBlockEntity() {
		super(TRBlockEntities.MATTER_FABRICATOR );
	}

	private boolean spaceForOutput() {
		for (int i = 6; i < 11; i++) {
			if (spaceForOutput(i)) {
				return true;
			}
		}
		return false;
	}

	private boolean spaceForOutput(int slot) {
		return inventory.getInvStack(slot).isEmpty()
				|| ItemUtils.isItemEqual(inventory.getInvStack(slot), TRContent.Parts.UU_MATTER.getStack(), true, true)
						&& inventory.getInvStack(slot).getCount() < 64;
	}

	private void addOutputProducts() {
		for (int i = 6; i < 11; i++) {
			if (spaceForOutput(i)) {
				addOutputProducts(i);
				break;
			}
		}
	}

	private void addOutputProducts(int slot) {
		if (inventory.getInvStack(slot).isEmpty()) {
			inventory.setInvStack(slot, TRContent.Parts.UU_MATTER.getStack());
		} 
		else if (ItemUtils.isItemEqual(this.inventory.getInvStack(slot), TRContent.Parts.UU_MATTER.getStack(), true, true)) {
			inventory.getInvStack(slot).setCount((Math.min(64, 1 + inventory.getInvStack(slot).getCount())));
		}
	}

	public boolean decreaseStoredEnergy(double aEnergy, boolean aIgnoreTooLessEnergy) {
		if (getEnergy() - aEnergy < 0 && !aIgnoreTooLessEnergy) {
			return false;
		} else {
			setEnergy(getEnergy() - aEnergy);
			if (getEnergy() < 0) {
				setEnergy(0);
				return false;
			} else {
				return true;
			}
		}
	}

	public int getValue(ItemStack itemStack) {
		if (itemStack.isItemEqualIgnoreDamage(TRContent.Parts.SCRAP.getStack())) {
			return 200;
		} else if (itemStack.getItem() == TRContent.SCRAP_BOX) {
			return 2000;
		}
		return 0;
	}

	public int getProgress() {
		return amplifier;
	}

	public void setProgress(int progress) {
		amplifier = progress;
	}

	public int getProgressScaled(int scale) {
		if (amplifier != 0) {
			return Math.min(amplifier * scale / fabricationRate, 100);
		}
		return 0;
	}

	// TilePowerAcceptor
	@Override
	public void tick() {
		if (world.isClient) {
			return;
		}

		super.tick();
		this.charge(11);

		for (int i = 0; i < 6; i++) {
			final ItemStack stack = inventory.getInvStack(i);
			if (!stack.isEmpty() && spaceForOutput()) {
				final int amp = getValue(stack);
				final int euNeeded = amp * energyPerAmp;
				if (amp != 0 && this.canUseEnergy(euNeeded)) {
					useEnergy(euNeeded);
					amplifier += amp;
					inventory.shrinkSlot(i, 1);
				}
			}
		}

		if (amplifier >= fabricationRate) {
			if (spaceForOutput()) {
				addOutputProducts();
				amplifier -= fabricationRate;
			}
		}
	}

	@Override
	public double getBaseMaxPower() {
		return maxEnergy;
	}

	@Override
	public boolean canAcceptEnergy(Direction direction) {
		return true;
	}

	@Override
	public boolean canProvideEnergy(Direction direction) {
		return false;
	}

	@Override
	public double getBaseMaxOutput() {
		return 0;
	}

	@Override
	public double getBaseMaxInput() {
		return maxInput;
	}

	// TileMachineBase
	@Override
	public boolean canBeUpgraded() {
		return false;
	}

	// IToolDrop
	@Override
	public ItemStack getToolDrop(PlayerEntity entityPlayer) {
		return TRContent.Machine.MATTER_FABRICATOR.getStack();
	}

	// ItemHandlerProvider
	@Override
	public RebornInventory<MatterFabricatorBlockEntity> getInventory() {
		return inventory;
	}

	// IContainerProvider
	@Override
	public BuiltContainer createContainer(int syncID, PlayerEntity player) {
		return new ContainerBuilder("matterfabricator").player(player.inventory).inventory().hotbar().addInventory()
				.blockEntity(this).slot(0, 30, 20).slot(1, 50, 20).slot(2, 70, 20).slot(3, 90, 20).slot(4, 110, 20)
				.slot(5, 130, 20).outputSlot(6, 40, 66).outputSlot(7, 60, 66).outputSlot(8, 80, 66)
				.outputSlot(9, 100, 66).outputSlot(10, 120, 66).energySlot(11, 8, 72).syncEnergyValue()
				.syncIntegerValue(this::getProgress, this::setProgress).addInventory().create(this, syncID);
	}
}
