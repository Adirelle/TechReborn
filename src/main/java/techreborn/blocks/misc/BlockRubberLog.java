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

package techreborn.blocks.misc;

import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateFactory;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import reborncore.api.power.ItemPowerManager;
import reborncore.common.powerSystem.ExternalPowerSystems;
import reborncore.common.util.WorldUtils;
import techreborn.events.TRRecipeHandler;
import techreborn.init.ModSounds;
import techreborn.init.TRContent;
import techreborn.items.tool.ItemTreeTap;
import techreborn.items.tool.basic.ItemElectricTreetap;

import java.util.Random;

/**
 * Created by modmuss50 on 19/02/2016.
 */
public class BlockRubberLog extends LogBlock {

	public static DirectionProperty SAP_SIDE = Properties.HORIZONTAL_FACING;
	public static BooleanProperty HAS_SAP = BooleanProperty.of("hassap");

	public BlockRubberLog() {
		super(MaterialColor.SPRUCE, FabricBlockSettings.of(Material.WOOD, MaterialColor.BROWN).strength(2.0F, 2f).sounds(BlockSoundGroup.WOOD).ticksRandomly().build());
		this.setDefaultState(this.getDefaultState().with(SAP_SIDE, Direction.NORTH).with(HAS_SAP, false).with(AXIS, Direction.Axis.Y));
		((FireBlock) Blocks.FIRE).registerFlammableBlock(this, 5, 5);
	}

	@Override
	protected void appendProperties(StateFactory.Builder<Block, BlockState> builder) {
		super.appendProperties(builder);
		builder.add(SAP_SIDE, HAS_SAP);
	}

	@Override
	public boolean matches(Tag<Block> tagIn) {
		return tagIn == BlockTags.LOGS;
	}

	@Override
	public void onBreak(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
		int i = 4;
		int j = i + 1;
		if (worldIn.isAreaLoaded(pos.add(-j, -j, -j), pos.add(j, j, j))) {
			for (BlockPos blockpos : BlockPos.iterate(pos.add(-i, -i, -i), pos.add(i, i, i))) {
				BlockState state1 = worldIn.getBlockState(blockpos);
				if (state1.matches(BlockTags.LEAVES)) {
					state1.scheduledTick(worldIn, blockpos, worldIn.getRandom());
					state1.onRandomTick(worldIn, blockpos, worldIn.getRandom());
				}
			}
		}
	}

	@Override
	public void onScheduledTick(BlockState state, World worldIn, BlockPos pos, Random random) {
		super.onScheduledTick(state, worldIn, pos, random);
		if (state.get(AXIS) != Direction.Axis.Y) {
			return;
		}
		if (state.get(HAS_SAP)) {
			return;
		}
		if (random.nextInt(50) == 0) {
			Direction facing = Direction.fromHorizontal(random.nextInt(4));
			if (worldIn.getBlockState(pos.down()).getBlock() == this
					&& worldIn.getBlockState(pos.up()).getBlock() == this) {
				worldIn.setBlockState(pos, state.with(HAS_SAP, true).with(SAP_SIDE, facing));
			}
		}
	}

	@Override
	public boolean activate(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn,
	                                Hand hand, BlockHitResult hitResult) {
		super.activate(state, worldIn, pos, playerIn, hand, hitResult);
		ItemStack stack = playerIn.getStackInHand(Hand.MAIN_HAND);
		if (stack.isEmpty()) {
			return false;
		}
		
		//TODO: Should it be here??
		ItemPowerManager capEnergy = null;
		if (stack.getItem() instanceof ItemElectricTreetap) {
			capEnergy = new ItemPowerManager(stack);
		}
		if ((capEnergy != null && capEnergy.getEnergyStored() > 20) || stack.getItem() instanceof ItemTreeTap) {
			if (state.get(HAS_SAP) && state.get(SAP_SIDE) == hitResult.getSide()) {
				worldIn.setBlockState(pos, state.with(HAS_SAP, false).with(SAP_SIDE, Direction.fromHorizontal(0)));
				worldIn.playSound(null, pos.getX(), pos.getY(), pos.getZ(), ModSounds.SAP_EXTRACT, SoundCategory.BLOCKS,
						0.6F, 1F);
				if (!worldIn.isClient) {
					if (capEnergy != null) {
						capEnergy.useEnergy(20, false);

						ExternalPowerSystems.requestEnergyFromArmor(capEnergy, playerIn);
					} else {
						playerIn.getStackInHand(Hand.MAIN_HAND).damage(1, playerIn, playerEntity -> {});
					}
					if (!playerIn.inventory.insertStack(TRContent.Parts.SAP.getStack())) {
						WorldUtils.dropItem(TRContent.Parts.SAP.getStack(), worldIn, pos.offset(hitResult.getSide()));
					}
					if (playerIn instanceof ServerPlayerEntity) {
						TRRecipeHandler.unlockTRRecipes((ServerPlayerEntity) playerIn);
					}
				}
				return true;
			}
		}
		return false;
	}

//	@Override
//	public void getDrops(BlockState state, DefaultedList<ItemStack> drops, World world, BlockPos pos, int fortune) {
//		drops.add(new ItemStack(this));
//		if (state.get(HAS_SAP)) {
//			if (new Random().nextInt(4) == 0) {
//				drops.add(TRContent.Parts.SAP.getStack());
//			}
//		}
//	}
}
