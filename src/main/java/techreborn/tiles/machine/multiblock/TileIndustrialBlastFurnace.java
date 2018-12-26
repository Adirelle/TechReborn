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
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import reborncore.common.multiblock.IMultiblockPart;
import reborncore.common.recipes.RecipeCrafter;
import reborncore.common.registration.RebornRegister;
import reborncore.common.registration.impl.ConfigRegistry;
import reborncore.common.util.Inventory;
import techreborn.TechReborn;
import techreborn.api.Reference;
import techreborn.api.recipe.ITileRecipeHandler;
import techreborn.api.recipe.machines.BlastFurnaceRecipe;
import techreborn.blocks.BlockMachineCasing;
import techreborn.init.TRContent;
import reborncore.client.containerBuilder.IContainerProvider;
import reborncore.client.containerBuilder.builder.BuiltContainer;
import reborncore.client.containerBuilder.builder.ContainerBuilder;
import techreborn.lib.ModInfo;
import techreborn.multiblocks.MultiBlockCasing;
import techreborn.tiles.TileGenericMachine;
import techreborn.tiles.TileMachineCasing;

@RebornRegister(modID = TechReborn.MOD_ID)
public class TileIndustrialBlastFurnace extends TileGenericMachine implements IContainerProvider, ITileRecipeHandler<BlastFurnaceRecipe>  {
	
	@ConfigRegistry(config = "machines", category = "industrial_furnace", key = "IndustrialFurnaceMaxInput", comment = "Industrial Blast Furnace Max Input (Value in EU)")
	public static int maxInput = 128;
	@ConfigRegistry(config = "machines", category = "industrial_furnace", key = "IndustrialFurnaceMaxEnergy", comment = "Industrial Blast Furnace Max Energy (Value in EU)")
	public static int maxEnergy = 40_000;

	public MultiblockChecker multiblockChecker;
	private int cachedHeat;

	public TileIndustrialBlastFurnace() {
		super("IndustrialBlastFurnace", maxInput, maxEnergy, ModBlocks.INDUSTRIAL_BLAST_FURNACE, 4);
		final int[] inputs = new int[] { 0, 1 };
		final int[] outputs = new int[] { 2, 3 };
		this.inventory = new Inventory<>(5, "TileIndustrialBlastFurnace", 64, this).withConfiguredAccess();
		this.crafter = new RecipeCrafter(Reference.BLAST_FURNACE_RECIPE, this, 2, 2, this.inventory, inputs, outputs);
	}
	
	public int getHeat() {
		if (!getMutliBlock()){
			return 0;
		}
		
		// Bottom center of multiblock
		final BlockPos location = pos.offset(getFacing().getOpposite(), 2);
		final TileEntity tileEntity = world.getTileEntity(location);

		if (tileEntity instanceof TileMachineCasing) {
			if (((TileMachineCasing) tileEntity).isConnected()
					&& ((TileMachineCasing) tileEntity).getMultiblockController().isAssembled()) {
				final MultiBlockCasing casing = ((TileMachineCasing) tileEntity).getMultiblockController();

				int heat = 0;

				// Bottom center shouldn't have any tile entities below it
				if (world.getBlockState(new BlockPos(location.getX(), location.getY() - 1, location.getZ()))
						.getBlock() == tileEntity.getBlockType()) {
					return 0;
				}

				for (final IMultiblockPart part : casing.connectedParts) {
					heat += BlockMachineCasing.getHeatFromState(part.getBlockState());
				}

				if (world.getBlockState(location.offset(EnumFacing.UP, 1)).getBlock().getTranslationKey().equals("tile.lava")
						&& world.getBlockState(location.offset(EnumFacing.UP, 2)).getBlock().getTranslationKey().equals("tile.lava")) {
					heat += 500;
				}
				return heat;
			}
		}

		return 0;
	}
	
	public boolean getMutliBlock() {
		final boolean layer0 = multiblockChecker.checkRectY(1, 1, MultiblockChecker.CASING_ANY, MultiblockChecker.ZERO_OFFSET);
		final boolean layer1 = multiblockChecker.checkRingY(1, 1, MultiblockChecker.CASING_ANY, new BlockPos(0, 1, 0));
		final boolean layer2 = multiblockChecker.checkRingY(1, 1, MultiblockChecker.CASING_ANY, new BlockPos(0, 2, 0));
		final boolean layer3 = multiblockChecker.checkRectY(1, 1, MultiblockChecker.CASING_ANY, new BlockPos(0, 3, 0));
		final Material centerBlock1 = multiblockChecker.getBlock(0, 1, 0).getMaterial();
		final Material centerBlock2 = multiblockChecker.getBlock(0, 2, 0).getMaterial();
		final boolean center1 = (centerBlock1 == Material.AIR || centerBlock1 == Material.LAVA);
		final boolean center2 = (centerBlock2 == Material.AIR || centerBlock2 == Material.LAVA);
		return layer0 && layer1 && layer2 && layer3 && center1 && center2;
	}
	
	public void setHeat(final int heat) {
		cachedHeat = heat;
	}

	public int getCachedHeat() {
		return cachedHeat;
	}
	
	// TileGenericMachine
	@Override
	public void update() {
		if (multiblockChecker == null) {
			final BlockPos downCenter = pos.offset(getFacing().getOpposite(), 2);
			multiblockChecker = new MultiblockChecker(world, downCenter);
		}
		
		if (!world.isRemote && getMutliBlock()){ 
			super.update();
		}		
	}

	// TileMachineBase
	@Override
	public void onDataPacket(final NetworkManager net, final SPacketUpdateTileEntity packet) {
		world.markBlockRangeForRenderUpdate(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
		readFromNBT(packet.getNbtCompound());
	}
	
	// IContainerProvider
	@Override
	public BuiltContainer createContainer(final EntityPlayer player) {
		return new ContainerBuilder("blastfurnace").player(player.inventory).inventory().hotbar().addInventory()
				.tile(this).slot(0, 50, 27).slot(1, 50, 47).outputSlot(2, 93, 37).outputSlot(3, 113, 37)
				.energySlot(4, 8, 72).syncEnergyValue().syncCrafterValue()
				.syncIntegerValue(this::getHeat, this::setHeat).addInventory().create(this);
	}
	
	// ITileRecipeHandler
	@Override
	public boolean canCraft(final TileEntity tile, final BlastFurnaceRecipe recipe) {
		if (tile instanceof TileIndustrialBlastFurnace) {
			final TileIndustrialBlastFurnace blastFurnace = (TileIndustrialBlastFurnace) tile;
			return blastFurnace.getHeat() >= recipe.neededHeat;
		}
		return false;
	}

	@Override
	public boolean onCraft(final TileEntity tile, final BlastFurnaceRecipe recipe) {
		return true;
	}
}
