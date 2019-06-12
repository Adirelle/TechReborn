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

package techreborn.blocks.storage;

import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateFactory;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import reborncore.api.ToolManager;
import reborncore.api.tile.IMachineGuiHandler;
import reborncore.client.models.ModelCompound;
import reborncore.client.models.RebornModelRegistry;
import reborncore.common.BaseTileBlock;
import reborncore.common.blocks.BlockWrenchEventHandler;
import reborncore.common.util.WrenchUtils;
import techreborn.TechReborn;

/**
 * Created by Rushmead
 */
public abstract class BlockEnergyStorage extends BaseTileBlock {
	public static DirectionProperty FACING = Properties.FACING;
	public String name;
	public IMachineGuiHandler gui;

	public BlockEnergyStorage(String name, IMachineGuiHandler gui) {
		super(FabricBlockSettings.of(Material.METAL).strength(2f, 2f).build());
		this.setDefaultState(this.stateFactory.getDefaultState().with(FACING, Direction.NORTH));
		this.name = name;
		this.gui = gui;
		RebornModelRegistry.registerModel(new ModelCompound(TechReborn.MOD_ID, this, "machines/energy"));
		BlockWrenchEventHandler.wrenableBlocks.add(this);
	}

	public void setFacing(Direction facing, World world, BlockPos pos) {
		world.setBlockState(pos, world.getBlockState(pos).with(FACING, facing));
	}

	public Direction getFacing(BlockState state) {
		return state.get(FACING);
	}

	public String getSimpleName(String fullName) {
		if (fullName.equalsIgnoreCase("Batbox")) {
			return "lv_storage";
		}
		if (fullName.equalsIgnoreCase("MEDIUM_VOLTAGE_SU")) {
			return "mv_storage";
		}
		if (fullName.equalsIgnoreCase("HIGH_VOLTAGE_SU")) {
			return "hv_storage";
		}
		if (fullName.equalsIgnoreCase("AESU")) {
			return "ev_storage_adjust";
		}
		if (fullName.equalsIgnoreCase("IDSU")) {
			return "ev_storage_transmitter";
		}
		if (fullName.equalsIgnoreCase("LESU")) {
			return "ev_multi";
		}
		return fullName.toLowerCase();
	}

	// Block
	@Override
	protected void appendProperties(StateFactory.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean activate(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockHitResult hitResult) {
		ItemStack stack = playerIn.getStackInHand(Hand.MAIN_HAND);
		BlockEntity tileEntity = worldIn.getBlockEntity(pos);

		// We extended BlockTileBase. Thus we should always have tile entity. I hope.
		if (tileEntity == null) {
			return false;
		}

		if (!stack.isEmpty() && ToolManager.INSTANCE.canHandleTool(stack)) {
			if (WrenchUtils.handleWrench(stack, worldIn, pos, playerIn, hitResult.getSide())) {
				return true;
			}
		}

		if (!playerIn.isSneaking() && gui != null) {
			gui.open(playerIn, pos, worldIn);
			return true;
		}

		return super.activate(state, worldIn, pos, playerIn, hand, hitResult);
	}

	@Override
	public void onPlaced(World worldIn, BlockPos pos, BlockState state, LivingEntity placer,
			ItemStack stack) {
		super.onPlaced(worldIn, pos, state, placer, stack);
		Direction facing = placer.getHorizontalFacing().getOpposite();
		if (placer.pitch < -50) {
			facing = Direction.DOWN;
		} else if (placer.pitch > 50) {
			facing = Direction.UP;
		}
		setFacing(facing, worldIn, pos);
	}
}
