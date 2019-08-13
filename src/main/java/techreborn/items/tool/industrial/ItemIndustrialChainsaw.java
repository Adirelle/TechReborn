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

package techreborn.items.tool.industrial;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterials;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import reborncore.api.power.ItemPowerManager;
import reborncore.common.powerSystem.ExternalPowerSystems;
import reborncore.common.util.ChatUtils;
import reborncore.common.util.ItemUtils;
import techreborn.config.TechRebornConfig;
import techreborn.init.TRContent;
import techreborn.items.tool.ItemChainsaw;
import techreborn.utils.InitUtils;
import techreborn.utils.MessageIDs;
import techreborn.utils.TagUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ItemIndustrialChainsaw extends ItemChainsaw {

	private static final Direction[] SEARCH_ORDER = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.UP};


	// 4M FE max charge with 1k charge rate
	public ItemIndustrialChainsaw() {
		super(ToolMaterials.DIAMOND, TechRebornConfig.IndustrialChainsawCharge, 1.0F);
		this.cost = 250;
		this.transferLimit = 1000;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void appendStacks(ItemGroup par2ItemGroup, DefaultedList<ItemStack> itemList) {
		if (!isIn(par2ItemGroup)) {
			return;
		}
		InitUtils.initPoweredItems(TRContent.INDUSTRIAL_CHAINSAW, itemList);
	}

	@Override
	public boolean postMine(ItemStack stack, World worldIn, BlockState blockIn, BlockPos pos, LivingEntity entityLiving) {
		List<BlockPos> wood = new ArrayList<>();
		findWood(worldIn, pos, wood, new ArrayList<>());
		wood.forEach(pos1 -> breakBlock(pos1, stack, worldIn, entityLiving, pos));
		return super.postMine(stack, worldIn, blockIn, pos, entityLiving);
	}

	private void findWood(World world, BlockPos pos, List<BlockPos> wood, List<BlockPos> leaves){
		//Limit the amount of wood to be broken to 64 blocks.
		if(wood.size() >= 64){
			return;
		}
		//Search 150 leaves for wood
		if(leaves.size() >= 150){
			return;
		}
		for(Direction facing : SEARCH_ORDER){
			BlockPos checkPos = pos.offset(facing);
			if(!wood.contains(checkPos) && !leaves.contains(checkPos)){
				BlockState state = world.getBlockState(checkPos);
				if(TagUtils.hasTag(state.getBlock(), BlockTags.LOGS)){
					wood.add(checkPos);
					findWood(world, checkPos, wood, leaves);
				} else if(TagUtils.hasTag(state.getBlock(), BlockTags.LEAVES)){
					leaves.add(checkPos);
					findWood(world, checkPos, wood, leaves);
				}
			}

		}
	}

	@Override
	public TypedActionResult<ItemStack> use(final World world, final PlayerEntity player, final Hand hand) {
		final ItemStack stack = player.getStackInHand(hand);
		if (player.isSneaking()) {
			if (new ItemPowerManager(stack).getEnergyStored() < cost) {
				ChatUtils.sendNoSpamMessages(MessageIDs.nanosaberID, new LiteralText(
					Formatting.GRAY + I18n.translate("techreborn.message.nanosaberEnergyErrorTo") + " "
						+ Formatting.GOLD + I18n
						.translate("techreborn.message.nanosaberActivate")));
			} else {
				if (!ItemUtils.isActive(stack)) {
					if (stack.getTag() == null) {
						stack.setTag(new CompoundTag());
					}
					stack.getTag().putBoolean("isActive", true);
					if (world.isClient) {
						ChatUtils.sendNoSpamMessages(MessageIDs.nanosaberID, new LiteralText(
							Formatting.GRAY + I18n.translate("techreborn.message.setTo") + " "
								+ Formatting.GOLD + I18n
								.translate("techreborn.message.nanosaberActive")));
					}
				} else {
					stack.getTag().putBoolean("isActive", false);
					if (world.isClient) {
						ChatUtils.sendNoSpamMessages(MessageIDs.nanosaberID, new LiteralText(
							Formatting.GRAY + I18n.translate("techreborn.message.setTo") + " "
								+ Formatting.GOLD + I18n
								.translate("techreborn.message.nanosaberInactive")));
					}
				}
			}
			return new TypedActionResult<>(ActionResult.SUCCESS, stack);
		}
		return new TypedActionResult<>(ActionResult.PASS, stack);
	}

	@Override
	public void usageTick(World world, LivingEntity entity,  ItemStack stack, int i) {
		if (ItemUtils.isActive(stack) && new ItemPowerManager(stack).getEnergyStored() < cost) {
			if(entity.world.isClient){
				ChatUtils.sendNoSpamMessages(MessageIDs.nanosaberID, new LiteralText(
					Formatting.GRAY + I18n.translate("techreborn.message.nanosaberEnergyError") + " "
						+ Formatting.GOLD + I18n
						.translate("techreborn.message.nanosaberDeactivating")));
			}
			stack.getTag().putBoolean("isActive", false);
		}
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void appendTooltip(ItemStack stack, @Nullable World worldIn, List<Text> tooltip, TooltipContext flagIn) {
		if (!ItemUtils.isActive(stack)) {
			tooltip.add(new LiteralText(Formatting.YELLOW + "Shear: " + Formatting.RED + I18n.translate("techreborn.message.nanosaberInactive")));
		} else {
			tooltip.add(new LiteralText(Formatting.YELLOW + "Shear: " + Formatting.GREEN + I18n.translate("techreborn.message.nanosaberActive")));
		}
	}

	@Override
	public boolean isEffectiveOn(BlockState blockIn) {
		return Items.DIAMOND_AXE.isEffectiveOn(blockIn);
	}

	public void breakBlock(BlockPos pos, ItemStack stack, World world, LivingEntity entityLiving, BlockPos oldPos) {
		if (oldPos == pos) {
			return;
		}

		ItemPowerManager capEnergy = new ItemPowerManager(stack);
		if (capEnergy.getEnergyStored() < cost) {
			return;
		}

		BlockState blockState = world.getBlockState(pos);
		if (blockState.getHardness(world, pos) == -1.0F) {
			return;
		}
		if(!(entityLiving instanceof PlayerEntity)){
			return;
		}

		capEnergy.useEnergy(cost, false);
		ExternalPowerSystems.requestEnergyFromArmor(capEnergy, entityLiving);

		blockState.getBlock().afterBreak(world, (PlayerEntity) entityLiving, pos, blockState, world.getBlockEntity(pos), stack);
		world.setBlockState(pos, Blocks.AIR.getDefaultState());
		world.removeBlockEntity(pos);
	}
}
