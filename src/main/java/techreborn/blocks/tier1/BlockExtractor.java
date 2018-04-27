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

package techreborn.blocks.tier1;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import prospector.shootingstar.ShootingStar;
import prospector.shootingstar.model.ModelCompound;
import reborncore.api.tile.IMachineGuiHandler;
import reborncore.common.blocks.BlockMachineBase;
import techreborn.client.EGui;
import techreborn.client.TechRebornCreativeTab;
import techreborn.lib.ModInfo;
import techreborn.tiles.tier1.TileExtractor;

public class BlockExtractor extends BlockMachineBase {

	public BlockExtractor() {
		super();
		this.setCreativeTab(TechRebornCreativeTab.instance);
		ShootingStar.registerModel(new ModelCompound(ModInfo.MOD_ID, this, "machines/tier1_machines"));
	}

	@Override
	public TileEntity createNewTileEntity(final World world, final int meta) {
		return new TileExtractor();
	}

	@Override
	public IMachineGuiHandler getGui() {
		return EGui.EXTRACTOR;
	}
}
