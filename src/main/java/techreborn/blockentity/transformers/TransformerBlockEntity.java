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

package techreborn.blockentity.transformers;

import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import reborncore.api.IListInfoProvider;
import reborncore.api.IToolDrop;
import reborncore.api.power.EnumPowerTier;
import reborncore.common.powerSystem.PowerSystem;
import reborncore.common.powerSystem.PowerAcceptorBlockEntity;
import reborncore.common.registration.RebornRegister;
import reborncore.common.registration.config.ConfigRegistry;
import reborncore.common.util.StringUtils;
import techreborn.TechReborn;
import techreborn.blocks.transformers.BlockTransformer;

import java.util.List;

/**
 * Created by Rushmead
 */
@RebornRegister(TechReborn.MOD_ID)
public class TransformerBlockEntity extends PowerAcceptorBlockEntity
		implements IToolDrop, IListInfoProvider {
	
	@ConfigRegistry(config = "misc", category = "general", key = "IC2TransformersStyle", comment = "Input from dots side, output from other sides, like in IC2.")
	public static boolean IC2TransformersStyle = true;

	public String name;
	public Block wrenchDrop;
	public EnumPowerTier inputTier;
	public EnumPowerTier ouputTier;
	public int maxInput;
	public int maxOutput;
	public int maxStorage;

	public TransformerBlockEntity(BlockEntityType<?> blockEntityType, String name, Block wrenchDrop, EnumPowerTier tier) {
		super(blockEntityType);
		this.wrenchDrop = wrenchDrop;
		this.inputTier = tier;
		if (tier != EnumPowerTier.MICRO) {
			ouputTier = EnumPowerTier.values()[tier.ordinal() - 1];
		} else {
			ouputTier = EnumPowerTier.MICRO;
		}
		this.name = name;
		this.maxInput = tier.getMaxInput();
		this.maxOutput = tier.getMaxOutput();
		this.maxStorage = tier.getMaxInput() * 2;

		// Should always be 4, except if we're tier MICRO, in which it will be 1.
		super.setMaxPacketsPerTick(tier.getMaxOutput() / ouputTier.getMaxInput());
	}
	
	// TilePowerAcceptor
	@Override
	public double getBaseMaxPower() {
		return maxStorage;
	}

	@Override
	public boolean canAcceptEnergy(Direction direction) {
		if (IC2TransformersStyle == true){
			return getFacingEnum() == direction;
		}
		return getFacingEnum() != direction;
	}
	
	@Override
	public boolean canProvideEnergy(Direction direction) {
		if (IC2TransformersStyle == true){
			return getFacingEnum() != direction;
		}
		return getFacing() == direction;
	}

	@Override
	public double getBaseMaxOutput() {
		return ouputTier.getMaxOutput();
	}

	@Override
	public double getBaseMaxInput() {
		return inputTier.getMaxInput();
	}
	
	@Override
	public EnumPowerTier getPushingTier() {
		return ouputTier;
	}

	@Override
	public void checkTier() {
		//Nope
		//TODO: really nope? needs review
	}
	
	// TileMachineBase
	@Override
	public Direction getFacingEnum() {
		Block block = world.getBlockState(pos).getBlock();
		if (block instanceof BlockTransformer) {
			return ((BlockTransformer) block).getFacing(world.getBlockState(pos));
		}
		return null;
	}

	// IToolDrop
	@Override
	public ItemStack getToolDrop(PlayerEntity playerIn) {
		return new ItemStack(wrenchDrop);
	}

	// IListInfoProvider
	@Override
	public void addInfo(List<Text> info, boolean isReal, boolean hasData) {
		info.add(new LiteralText(Formatting.GRAY + "Input Rate: " + Formatting.GOLD + PowerSystem.getLocaliszedPowerFormatted((int) getMaxInput())));
		info.add(new LiteralText(Formatting.GRAY + "Input Tier: " + Formatting.GOLD + StringUtils.toFirstCapitalAllLowercase(inputTier.toString())));
		info.add(new LiteralText(Formatting.GRAY + "Output Rate: " + Formatting.GOLD + PowerSystem.getLocaliszedPowerFormatted((int) getMaxOutput())));
		info.add(new LiteralText(Formatting.GRAY + "Output Tier: " + Formatting.GOLD + StringUtils.toFirstCapitalAllLowercase(ouputTier.toString())));
	}

}
