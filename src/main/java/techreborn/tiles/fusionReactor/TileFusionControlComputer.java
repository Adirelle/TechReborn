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

package techreborn.tiles.fusionReactor;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import reborncore.api.IToolDrop;
import reborncore.api.tile.ItemHandlerProvider;
import reborncore.client.containerBuilder.IContainerProvider;
import reborncore.client.containerBuilder.builder.BuiltContainer;
import reborncore.client.containerBuilder.builder.ContainerBuilder;
import reborncore.common.RebornCoreConfig;
import reborncore.common.powerSystem.TilePowerAcceptor;
import reborncore.common.registration.RebornRegister;
import reborncore.common.registration.config.ConfigRegistry;
import reborncore.common.util.RebornInventory;
import reborncore.common.util.ItemUtils;
import reborncore.common.util.Torus;
import techreborn.TechReborn;
import techreborn.api.reactor.FusionReactorRecipe;
import techreborn.api.reactor.FusionReactorRecipeHelper;
import techreborn.init.TRContent;
import techreborn.init.TRTileEntities;

import java.util.List;

@RebornRegister(TechReborn.MOD_ID)
public class TileFusionControlComputer extends TilePowerAcceptor
		implements IToolDrop, ItemHandlerProvider, IContainerProvider {

	@ConfigRegistry(config = "machines", category = "fusion_reactor", key = "FusionReactorMaxInput", comment = "Fusion Reactor Max Input (Value in EU)")
	public static int maxInput = 8192;
	@ConfigRegistry(config = "machines", category = "fusion_reactor", key = "FusionReactorMaxOutput", comment = "Fusion Reactor Max Output (Value in EU)")
	public static int maxOutput = 1_000_000;
	@ConfigRegistry(config = "machines", category = "fusion_reactor", key = "FusionReactorMaxEnergy", comment = "Fusion Reactor Max Energy (Value in EU)")
	public static int maxEnergy = 100_000_000;
	@ConfigRegistry(config = "machines", category = "fusion_reactor", key = "FusionReactorMaxCoilSize", comment = "Fusion Reactor Max Coil size (Radius)")
	public static int maxCoilSize = 50;

	public RebornInventory<TileFusionControlComputer> inventory;

	public int coilCount = 0;
	public int crafingTickTime = 0;
	public int finalTickTime = 0;
	public int neededPower = 0;
	public int size = 6;
	public int state = -1;
	int topStackSlot = 0;
	int bottomStackSlot = 1;
	int outputStackSlot = 2;
	FusionReactorRecipe currentRecipe = null;
	boolean hasStartedCrafting = false;
	long lastTick = -1;

	public TileFusionControlComputer() {
		super(TRTileEntities.FUSION_CONTROL_COMPUTER);
		checkOverfill = false;
		this.inventory = new RebornInventory<>(3, "TileFusionControlComputer", 64, this).withConfiguredAccess();
	}

	/**
	 * Check that reactor has all necessary coils in place
	 * 
	 * @return boolean Return true if coils are present
	 */
	public boolean checkCoils() {
		List<BlockPos> coils = Torus.generate(pos, size);
		for(BlockPos coilPos : coils){
			if (!isCoil(coilPos)) {
				coilCount = 0;
				return false;
			}
		}
		coilCount = coils.size();
		return true;
	}

	/**
	 * Checks if block is fusion coil
	 * 
	 * @param pos coordinate for block
	 * @return boolean Returns true if block is fusion coil
	 */
	public boolean isCoil(BlockPos pos) {
		return world.getBlockState(pos).getBlock() == TRContent.Machine.FUSION_COIL.block;
	}

	/**
	 * Resets crafter progress and recipe
	 */
	private void resetCrafter() {
		currentRecipe = null;
		crafingTickTime = 0;
		finalTickTime = 0;
		neededPower = 0;
		hasStartedCrafting = false;
	}

	/**
	 * Checks that ItemStack could be inserted into slot provided, including check
	 * for existing item in slot and maximum stack size
	 * 
	 * @param stack ItemStack ItemStack to insert
	 * @param slot int Slot ID to check
	 * @param tags boolean Should we use tags
	 * @return boolean Returns true if ItemStack will fit into slot
	 */
	public boolean canFitStack(ItemStack stack, int slot, boolean tags) {// Checks to see if it can
																								// fit the stack
		if (stack.isEmpty()) {
			return true;
		}
		if (inventory.getInvStack(slot).isEmpty()) {
			return true;
		}
		if (ItemUtils.isItemEqual(inventory.getInvStack(slot), stack, true, tags)) {
			if (stack.getCount() + inventory.getInvStack(slot).getCount() <= stack.getMaxCount()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns progress scaled to input value
	 * 
	 * @param scale int Maximum value for progress
	 * @return int Scale of progress
	 */
	public int getProgressScaled(int scale) {
		if (crafingTickTime != 0 && finalTickTime != 0) {
			return crafingTickTime * scale / finalTickTime;
		}
		return 0;
	}

	/**
	 * Tries to set current recipe based in inputs in reactor
	 */
	private void updateCurrentRecipe() {
		for (final FusionReactorRecipe reactorRecipe : FusionReactorRecipeHelper.reactorRecipes) {
			if (validateReactorRecipe(reactorRecipe)) {
				currentRecipe = reactorRecipe;
				crafingTickTime = 0;
				finalTickTime = currentRecipe.getTickTime();
				neededPower = (int) currentRecipe.getStartEU();
				hasStartedCrafting = false;
				break;
			}
		}
	}

	/**
	 * Validates that reactor can execute recipe provided, e.g. has all inputs and can fit output
	 * 
	 * @param recipe FusionReactorRecipe Recipe to validate
	 * @return boolean True if reactor can execute recipe provided 
	 */
	private boolean validateReactorRecipe(FusionReactorRecipe recipe) {
		boolean validRecipe = validateReactorRecipeInputs(recipe, inventory.getInvStack(topStackSlot), inventory.getInvStack(bottomStackSlot)) || validateReactorRecipeInputs(recipe, inventory.getInvStack(bottomStackSlot), inventory.getInvStack(topStackSlot));
		return validRecipe && getSize() >= recipe.getMinSize();
	}

	private boolean validateReactorRecipeInputs(FusionReactorRecipe recipe, ItemStack slot1, ItemStack slot2) {
		if (ItemUtils.isItemEqual(slot1, recipe.getTopInput(), true, true)) {
			if (recipe.getBottomInput() != null) {
				if (!ItemUtils.isItemEqual(slot2, recipe.getBottomInput(), true, true)) {
					return false;
				}
			}
			if (canFitStack(recipe.getOutput(), outputStackSlot, true)) {
				return true;
			}
		}
		return false;
	}

	// TilePowerAcceptor
	@Override
	public void tick() {
		super.tick();

		if (world.isClient) {
			return;
		}

		if(lastTick == world.getTime()){
			//Prevent tick accerators, blame obstinate for this.
			return;
		}
		lastTick = world.getTime();

		// Force check every second
		if (world.getTime() % 20 == 0) {
			checkCoils();
			inventory.setChanged();
		}

		if (coilCount == 0) {
			resetCrafter();
			return;
		}

		if (currentRecipe == null && inventory.hasChanged()) {
			updateCurrentRecipe();
		}

		if (currentRecipe != null) {
			if (!hasStartedCrafting && inventory.hasChanged() && !validateReactorRecipe(currentRecipe)) {
				resetCrafter();
				return;
			}

			if (!hasStartedCrafting) {
				// Ignition!
				if (canUseEnergy(currentRecipe.getStartEU())) {
					useEnergy(currentRecipe.getStartEU());
					hasStartedCrafting = true;
					inventory.shrinkSlot(topStackSlot, currentRecipe.getTopInput().getCount());
					if (!currentRecipe.getBottomInput().isEmpty()) {
						inventory.shrinkSlot(bottomStackSlot, currentRecipe.getBottomInput().getCount());
					}
				}
			}
			if (hasStartedCrafting && crafingTickTime < finalTickTime) {
				crafingTickTime++;
				// Power gen
				if (currentRecipe.getEuTick() > 0) {
					// Waste power if it has no where to go
					addEnergy(currentRecipe.getEuTick() * getPowerMultiplier());
					powerChange = currentRecipe.getEuTick() * getPowerMultiplier();
				} else { // Power user
					if (canUseEnergy(currentRecipe.getEuTick() * -1)) {
						setEnergy(getEnergy() - currentRecipe.getEuTick() * -1);
					}
				}
			} else if (crafingTickTime >= finalTickTime) {
				if (canFitStack(currentRecipe.getOutput(), outputStackSlot, true)) {
					if (inventory.getInvStack(outputStackSlot).isEmpty()) {
						inventory.setInvStack(outputStackSlot, currentRecipe.getOutput().copy());
					} else {
						inventory.shrinkSlot(outputStackSlot, -currentRecipe.getOutput().getCount());
					}
					if (validateReactorRecipe(this.currentRecipe)) {
						crafingTickTime = 0;
						inventory.shrinkSlot(topStackSlot, currentRecipe.getTopInput().getCount());
						if (!currentRecipe.getBottomInput().isEmpty()) {
							inventory.shrinkSlot(bottomStackSlot, currentRecipe.getBottomInput().getCount());
						}
					} else {
						resetCrafter();
					}
				}
			}
			markDirty();
		}

		inventory.resetChanged();
	}


	@Override
	public double getPowerMultiplier() {
		double calc = (1F/2F) * Math.pow(size -5, 1.8);
		return Math.max(Math.round(calc * 100D) / 100D, 1D);
	}

	@Override
	public double getBaseMaxPower() {
		return Math.min(maxEnergy * getPowerMultiplier(), Integer.MAX_VALUE / RebornCoreConfig.euPerFU);
	}

	@Override
	public boolean canAcceptEnergy(Direction direction) {
		return !(direction == Direction.DOWN || direction == Direction.UP);
	}

	@Override
	public boolean canProvideEnergy(Direction direction) {
		return direction == Direction.DOWN || direction == Direction.UP;
	}

	@Override
	public double getBaseMaxOutput() {
		if (!hasStartedCrafting) {
			return 0;
		}
		return Integer.MAX_VALUE / RebornCoreConfig.euPerFU;
	}

	@Override
	public double getBaseMaxInput() {
		if (hasStartedCrafting) {
			return 0;
		}
		return maxInput;
	}

	@Override
	public void fromTag(final CompoundTag tagCompound) {
		super.fromTag(tagCompound);
		this.crafingTickTime = tagCompound.getInt("crafingTickTime");
		this.finalTickTime = tagCompound.getInt("finalTickTime");
		this.neededPower = tagCompound.getInt("neededPower");
		this.hasStartedCrafting = tagCompound.getBoolean("hasStartedCrafting");
		if(tagCompound.containsKey("hasActiveRecipe") && tagCompound.getBoolean("hasActiveRecipe") && this.currentRecipe == null){
			for (final FusionReactorRecipe reactorRecipe : FusionReactorRecipeHelper.reactorRecipes) {
				if (validateReactorRecipe(reactorRecipe)) {
					this.currentRecipe = reactorRecipe;
				}
			}
		}
		if(tagCompound.containsKey("size")){
			this.size = tagCompound.getInt("size");
		}
		this.size = Math.min(size, maxCoilSize);//Done here to force the samller size, will be useful if people lag out on a large one.
	}

	@Override
	public CompoundTag toTag(final CompoundTag tagCompound) {
		super.toTag(tagCompound);
		tagCompound.putInt("crafingTickTime", this.crafingTickTime);
		tagCompound.putInt("finalTickTime", this.finalTickTime);
		tagCompound.putInt("neededPower", this.neededPower);
		tagCompound.putBoolean("hasStartedCrafting", this.hasStartedCrafting);
		tagCompound.putBoolean("hasActiveRecipe", this.currentRecipe != null);
		tagCompound.putInt("size", size);
		return tagCompound;
	}

	// TileLegacyMachineBase
	@Override
	public void onLoad() {
		super.onLoad();
		this.checkCoils();
	}

	@Override
	public boolean canBeUpgraded() {
		return false;
	}

	// IToolDrop
	@Override
	public ItemStack getToolDrop(PlayerEntity playerIn) {
		return TRContent.Machine.FUSION_CONTROL_COMPUTER.getStack();
	}

	// ItemHandlerProvider
	@Override
	public RebornInventory<TileFusionControlComputer> getInventory() {
		return inventory;
	}

	// IContainerProvider
	@Override
	public BuiltContainer createContainer(final PlayerEntity player) {
		return new ContainerBuilder("fusionreactor").player(player.inventory).inventory().hotbar()
				.addInventory().tile(this).slot(0, 34, 47).slot(1, 126, 47).outputSlot(2, 80, 47).syncEnergyValue()
				.syncIntegerValue(this::getCoilStatus, this::setCoilStatus)
				.syncIntegerValue(this::getCrafingTickTime, this::setCrafingTickTime)
				.syncIntegerValue(this::getFinalTickTime, this::setFinalTickTime)
				.syncIntegerValue(this::getSize, this::setSize)
				.syncIntegerValue(this::getState, this::setState)
				.syncIntegerValue(this::getNeededPower, this::setNeededPower).addInventory().create(this);
	}

	public int getCoilStatus() {
		return coilCount;
	}

	public void setCoilStatus(int coilStatus) {
		this.coilCount = coilStatus;
	}

	public int getCrafingTickTime() {
		return crafingTickTime;
	}

	public void setCrafingTickTime(int crafingTickTime) {
		this.crafingTickTime = crafingTickTime;
	}

	public int getFinalTickTime() {
		return finalTickTime;
	}

	public void setFinalTickTime(int finalTickTime) {
		this.finalTickTime = finalTickTime;
	}

	public int getNeededPower() {
		return neededPower;
	}

	public void setNeededPower(int neededPower) {
		this.neededPower = neededPower;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public void changeSize(int sizeDelta){
		int newSize = size + sizeDelta;
		this.size = Math.max(6, Math.min(maxCoilSize, newSize));
	}

	public int getState(){
		if(currentRecipe == null ){
			return 0; //No Recipe
		}
		if(!hasStartedCrafting){
			return 1; //Waiting on power
		}
		if(hasStartedCrafting){
			return 2; //Crafting
		}
		return -1;
	}

	public void setState(int state){
		this.state = state;
	}

	public String getStateString(){
		if(state == -1){
			return "";
		} else if (state == 0){
			return "No recipe";
		} else if (state == 1){
			return "Charging";
		} else if (state == 2){
			return "Crafting";
		}
		return "";
	}
}
