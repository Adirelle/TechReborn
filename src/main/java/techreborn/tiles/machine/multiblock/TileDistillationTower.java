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

package techreborn.tiles.machine.multiblock;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import reborncore.client.containerBuilder.IContainerProvider;
import reborncore.client.containerBuilder.builder.BuiltContainer;
import reborncore.client.containerBuilder.builder.ContainerBuilder;
import reborncore.common.recipes.RecipeCrafter;
import reborncore.common.registration.RebornRegister;
import reborncore.common.registration.config.ConfigRegistry;
import reborncore.common.util.Inventory;
import techreborn.TechReborn;
import techreborn.api.Reference;
import techreborn.init.TRContent;
import techreborn.init.TRTileEntities;
import techreborn.tiles.TileGenericMachine;

@RebornRegister(TechReborn.MOD_ID)
public class TileDistillationTower extends TileGenericMachine implements IContainerProvider {

	@ConfigRegistry(config = "machines", category = "distillation_tower", key = "DistillationTowerMaxInput", comment = "Distillation Tower Max Input (Value in EU)")
	public static int maxInput = 128;
	@ConfigRegistry(config = "machines", category = "distillation_tower", key = "DistillationTowerMaxEnergy", comment = "Distillation Tower Max Energy (Value in EU)")
	public static int maxEnergy = 10_000;
	
	public MultiblockChecker multiblockChecker;

	public TileDistillationTower() {
		super(TRTileEntities.DISTILLATION_TOWER,"DistillationTower", maxInput, maxEnergy, TRContent.Machine.DISTILLATION_TOWER.block, 6);
		final int[] inputs = new int[] { 0, 1 };
		final int[] outputs = new int[] { 2, 3, 4, 5 };
		this.inventory = new Inventory<>(7, "TileDistillationTower", 64, this).withConfiguredAccess();
		this.crafter = new RecipeCrafter(Reference.DISTILLATION_TOWER_RECIPE, this, 2, 4, this.inventory, inputs, outputs);
	}
	
	public boolean getMutliBlock() {
		if (multiblockChecker == null) {
			return false;
		}
		final boolean layer0 = multiblockChecker.checkRectY(1, 1, MultiblockChecker.STANDARD_CASING, MultiblockChecker.ZERO_OFFSET);
		final boolean layer1 = multiblockChecker.checkRingY(1, 1, MultiblockChecker.ADVANCED_CASING, new BlockPos(0, 1, 0));
		final boolean layer2 = multiblockChecker.checkRingY(1, 1, MultiblockChecker.STANDARD_CASING, new BlockPos(0, 2, 0));
		final boolean layer3 = multiblockChecker.checkRectY(1, 1, MultiblockChecker.ADVANCED_CASING, new BlockPos(0, 3, 0));
		final Material centerBlock1 = multiblockChecker.getBlock(0, 1, 0).getMaterial();
		final Material centerBlock2 = multiblockChecker.getBlock(0, 2, 0).getMaterial();
		final boolean center1 = (centerBlock1 == Material.AIR);
		final boolean center2 = (centerBlock2 == Material.AIR);
		return layer0 && layer1 && layer2 && layer3 && center1 && center2;
	}

	// TileGenericMachine
	@Override
	public void tick() {
		if (multiblockChecker == null) {
			final BlockPos downCenter = pos.offset(getFacing().getOpposite(), 2);
			multiblockChecker = new MultiblockChecker(world, downCenter);
		}
		
		if (!world.isRemote && getMutliBlock()){ 
			super.tick();
		}	
	}

	// IContainerProvider
	@Override
	public BuiltContainer createContainer(final EntityPlayer player) {
		return new ContainerBuilder("distillationtower").player(player.inventory).inventory().hotbar().addInventory()
				.tile(this).slot(0, 35, 27).slot(1, 35, 47).outputSlot(2, 79, 37).outputSlot(3, 99, 37)
				.outputSlot(4, 119, 37).outputSlot(5, 139, 37).energySlot(6, 8, 72).syncEnergyValue().syncCrafterValue()
				.addInventory().create(this);
	}
}