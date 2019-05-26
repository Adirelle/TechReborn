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

package techreborn.client.container;

import net.minecraft.container.Slot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import reborncore.client.gui.slots.SlotFilteredVoid;
import reborncore.common.container.RebornContainer;
import reborncore.common.util.Inventory;
import techreborn.init.TRContent;

public class ContainerDestructoPack extends RebornContainer {

	private PlayerEntity player;
	private Inventory<?> inv;

	public ContainerDestructoPack(PlayerEntity player) {
		super(null);
		this.player = player;
		inv = new Inventory<>(1, "destructopack", 64, null);
		buildContainer();
	}

	@Override
	public boolean canUse(PlayerEntity arg0) {
		return true;
	}

	private void buildContainer() {
		this.addSlot(
			new SlotFilteredVoid(inv, 0, 80, 36, new ItemStack[] { TRContent.Parts.MACHINE_PARTS.getStack() }));
		int i;

		for (i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				this.addSlot(new Slot(player.inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}

		for (i = 0; i < 9; ++i) {
			this.addSlot(new Slot(player.inventory, i, 8 + i * 18, 142));
		}
	}
}
