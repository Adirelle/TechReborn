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

package techreborn.blocks.cable;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateFactory;
import net.minecraft.state.property.AbstractProperty;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import reborncore.api.ToolManager;
import reborncore.api.power.EnergyBlockEntity;
import reborncore.common.blocks.BlockWrenchEventHandler;
import reborncore.common.registration.RebornRegister;
import reborncore.common.registration.config.ConfigRegistry;
import reborncore.common.util.WrenchUtils;
import techreborn.TechReborn;
import techreborn.init.ModSounds;
import techreborn.init.TRContent;
import techreborn.blockentity.cable.CableBlockEntity;
import techreborn.utils.damageSources.ElectrialShockSource;

import javax.annotation.Nullable;

/**
 * Created by modmuss50 on 19/05/2017.
 */
@RebornRegister(TechReborn.MOD_ID)
public class BlockCable extends BlockWithEntity {

	public static final BooleanProperty EAST = BooleanProperty.of("east");
	public static final BooleanProperty WEST = BooleanProperty.of("west");
	public static final BooleanProperty NORTH = BooleanProperty.of("north");
	public static final BooleanProperty SOUTH = BooleanProperty.of("south");
	public static final BooleanProperty UP = BooleanProperty.of("up");
	public static final BooleanProperty DOWN = BooleanProperty.of("down");

	@ConfigRegistry(config = "misc", category = "cable", key = "uninsulatedElectrocutionDamage", comment = "When true an uninsulated cable will cause damage to entities")
	public static boolean uninsulatedElectrocutionDamage = true;

	@ConfigRegistry(config = "misc", category = "cable", key = "uninsulatedElectrocutionSound", comment = "When true an uninsulated cable will create a spark sound when an entity touches it")
	public static boolean uninsulatedElectrocutionSound = true;

	@ConfigRegistry(config = "misc", category = "cable", key = "uninsulatedElectrocutionParticles", comment = "When true an uninsulated cable will create a spark when an entity touches it")
	public static boolean uninsulatedElectrocutionParticles = true;

	public final TRContent.Cables type;

	public BlockCable(TRContent.Cables type) {
		super(Block.Settings.of(Material.STONE).strength(1f, 8f));
		this.type = type;
		setDefaultState(this.stateFactory.getDefaultState().with(EAST, false).with(WEST, false).with(NORTH, false)
				.with(SOUTH, false).with(UP, false).with(DOWN, false));
		BlockWrenchEventHandler.wrenableBlocks.add(this);
	}

	public AbstractProperty<Boolean> getProperty(Direction facing) {
		switch (facing) {
		case EAST:
			return EAST;
		case WEST:
			return WEST;
		case NORTH:
			return NORTH;
		case SOUTH:
			return SOUTH;
		case UP:
			return UP;
		case DOWN:
			return DOWN;
		default:
			return EAST;
		}
	}

	private BlockState makeConnections(World world, BlockPos pos) {
		Boolean down = canConnectTo(world, pos.down(), Direction.UP);
		Boolean up = canConnectTo(world, pos.up(), Direction.DOWN);
		Boolean north = canConnectTo(world, pos.north(), Direction.SOUTH);
		Boolean east = canConnectTo(world, pos.east(), Direction.WEST);
		Boolean south = canConnectTo(world, pos.south(), Direction.NORTH);
		Boolean west = canConnectTo(world, pos.west(), Direction.WEST);

		return this.getDefaultState().with(DOWN, down).with(UP, up).with(NORTH, north).with(EAST, east)
				.with(SOUTH, south).with(WEST, west);
	}

	private Boolean canConnectTo(IWorld world, BlockPos pos, Direction facing) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity != null && (blockEntity instanceof EnergyBlockEntity || blockEntity instanceof CableBlockEntity)) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	// BlockContainer
	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockView worldIn) {
		return new CableBlockEntity();
	}

	// Block
	@Override
	public boolean activate(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockHitResult hitResult) {
		ItemStack stack = playerIn.getStackInHand(Hand.MAIN_HAND);
		BlockEntity blockEntity = worldIn.getBlockEntity(pos);

		// We should always have blockEntity entity. I hope.
		if (blockEntity == null) {
			return false;
		}

		if (!stack.isEmpty() && ToolManager.INSTANCE.canHandleTool(stack)) {
			if (WrenchUtils.handleWrench(stack, worldIn, pos, playerIn, hitResult.getSide())) {
				return true;
			}
		}
		return super.activate(state, worldIn, pos, playerIn, hand, hitResult);
	}

	@Override
	protected void appendProperties(StateFactory.Builder<Block, BlockState> builder) {
		builder.add(EAST, WEST, NORTH, SOUTH, UP, DOWN);
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext context) {
		return makeConnections(context.getWorld(), context.getBlockPos());
	}

	@Override
	public BlockState getStateForNeighborUpdate(BlockState ourState, Direction ourFacing, BlockState otherState,
			IWorld worldIn, BlockPos ourPos, BlockPos otherPos) {
		Boolean value = canConnectTo(worldIn, otherPos, ourFacing.getOpposite());
		return ourState.with(getProperty(ourFacing), value);
	}

	@Override
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, EntityContext entityContext) {
		if (type != null) {
			double culling = type.cableThickness / 2;
			return Block.createCuboidShape(culling, culling, culling, 16.0D - culling, 16.0D - culling,
					16.0D - culling);
		}
		return Block.createCuboidShape(6, 6, 6, 10, 10, 10);
	}

	@Override
	public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
		super.onEntityCollision(state, worldIn, pos, entityIn);
		if (!type.canKill) {
			return;
		}
		if (!(entityIn instanceof LivingEntity)) {
			return;
		}

		BlockEntity blockEntity = worldIn.getBlockEntity(pos);
		if (blockEntity == null) {
			return;
		}
		if (!(blockEntity instanceof CableBlockEntity)) {
			return;
		}

		CableBlockEntity blockEntityCable = (CableBlockEntity) blockEntity;
		if (blockEntityCable.getEnergy() <= 0) {
			return;
		}

		if (uninsulatedElectrocutionDamage) {
			if (type == TRContent.Cables.HV) {
				entityIn.setOnFireFor(1);
			}
			entityIn.damage(new ElectrialShockSource(), 1F);
		}
		if (uninsulatedElectrocutionSound) {
			worldIn.playSound(null, entityIn.x, entityIn.y, entityIn.z, ModSounds.CABLE_SHOCK, SoundCategory.BLOCKS,
					0.6F, 1F);
		}
		if (uninsulatedElectrocutionParticles) {
			worldIn.addParticle(ParticleTypes.CRIT, entityIn.x, entityIn.y, entityIn.z, 0, 0, 0);
		}
	}
}
