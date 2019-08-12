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

package techreborn.events;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.Item.Settings;
import reborncore.RebornRegistry;
import techreborn.TechReborn;
import techreborn.blocks.misc.BlockComputerCube;
import techreborn.blocks.misc.BlockNuke;
import techreborn.blocks.misc.BlockRefinedIronFence;
import techreborn.blocks.misc.BlockReinforcedGlass;
import techreborn.blocks.misc.BlockRubberLeaves;
import techreborn.blocks.misc.BlockRubberLog;
import techreborn.blocks.misc.BlockRubberPlank;
import techreborn.blocks.misc.BlockRubberPlankSlab;
import techreborn.blocks.misc.BlockRubberPlankStair;
import techreborn.blocks.misc.BlockRubberSapling;
import techreborn.config.TechRebornConfig;
import techreborn.init.ModFluids;
import techreborn.init.ModSounds;
import techreborn.init.TRArmorMaterial;
import techreborn.init.TRContent;
import techreborn.init.TRContent.*;
import techreborn.init.TRToolTier;
import techreborn.items.ItemDynamicCell;
import techreborn.items.ItemFrequencyTransmitter;
import techreborn.items.ItemManual;
import techreborn.items.ItemScrapBox;
import techreborn.items.armor.ItemCloakingDevice;
import techreborn.items.armor.ItemLapotronicOrbpack;
import techreborn.items.armor.ItemLithiumIonBatpack;
import techreborn.items.armor.ItemTRArmour;
import techreborn.items.battery.*;
import techreborn.items.tool.ItemDebugTool;
import techreborn.items.tool.ItemTreeTap;
import techreborn.items.tool.ItemWrench;
import techreborn.items.tool.advanced.ItemAdvancedChainsaw;
import techreborn.items.tool.advanced.ItemAdvancedDrill;
import techreborn.items.tool.advanced.ItemAdvancedJackhammer;
import techreborn.items.tool.advanced.ItemRockCutter;
import techreborn.items.tool.basic.ItemBasicChainsaw;
import techreborn.items.tool.basic.ItemBasicDrill;
import techreborn.items.tool.basic.ItemBasicJackhammer;
import techreborn.items.tool.basic.ItemElectricTreetap;
import techreborn.items.tool.industrial.*;
import techreborn.items.tool.vanilla.*;
import techreborn.utils.InitUtils;

import java.util.Arrays;

/**
 * @author drcrazy
 *
 */

public class ModRegistry {

	public static void setupShit(){
		registerBlocks();
		registerItems();
		registerFluids();
		registerSounds();
	}

	private static void registerBlocks() {
		Settings itemGroup = new Item.Settings().group(TechReborn.ITEMGROUP);
		Arrays.stream(Ores.values()).forEach(value -> RebornRegistry.registerBlock(value.block, itemGroup));
		Arrays.stream(StorageBlocks.values()).forEach(value -> RebornRegistry.registerBlock(value.block, itemGroup));
		Arrays.stream(MachineBlocks.values()).forEach(value -> {
			RebornRegistry.registerBlock(value.frame, itemGroup);
			RebornRegistry.registerBlock(value.casing, itemGroup);
		});
		Arrays.stream(SolarPanels.values()).forEach(value -> RebornRegistry.registerBlock(value.block, itemGroup));
		Arrays.stream(Cables.values()).forEach(value -> RebornRegistry.registerBlock(value.block, itemGroup));
		Arrays.stream(Machine.values()).forEach(value -> RebornRegistry.registerBlock(value.block, itemGroup));

		// Misc. blocks
		RebornRegistry.registerBlock(TRContent.COMPUTER_CUBE = InitUtils.setup(new BlockComputerCube(), "computer_cube"), itemGroup);
		RebornRegistry.registerBlock(TRContent.NUKE = InitUtils.setup(new BlockNuke(), "nuke"), itemGroup);
		RebornRegistry.registerBlock(TRContent.REFINED_IRON_FENCE = InitUtils.setup(new BlockRefinedIronFence(), "refined_iron_fence"), itemGroup);
		RebornRegistry.registerBlock(TRContent.REINFORCED_GLASS = InitUtils.setup(new BlockReinforcedGlass(), "reinforced_glass"), itemGroup);
		RebornRegistry.registerBlock(TRContent.RUBBER_LEAVES = InitUtils.setup(new BlockRubberLeaves(), "rubber_leaves"), itemGroup);
		RebornRegistry.registerBlock(TRContent.RUBBER_LOG = InitUtils.setup(new BlockRubberLog(), "rubber_log"), itemGroup);
		RebornRegistry.registerBlock(TRContent.RUBBER_PLANKS = InitUtils.setup(new BlockRubberPlank(), "rubber_planks"), itemGroup);
		RebornRegistry.registerBlock(TRContent.RUBBER_SAPLING = InitUtils.setup(new BlockRubberSapling(), "rubber_sapling"), itemGroup);
		RebornRegistry.registerBlock(TRContent.RUBBER_PLANK_SLAB = InitUtils.setup(new BlockRubberPlankSlab(), "rubber_plank_slab"), itemGroup);
		RebornRegistry.registerBlock(TRContent.RUBBER_PLANK_STAIR = InitUtils.setup(new BlockRubberPlankStair(), "rubber_plank_stair"), itemGroup);

		TechReborn.LOGGER.debug("TechReborns Blocks Loaded");
	}

	private static void registerItems() {
		Arrays.stream(Ingots.values()).forEach(value -> RebornRegistry.registerItem(value.item));
		Arrays.stream(Nuggets.values()).forEach(value -> RebornRegistry.registerItem(value.item));
		Arrays.stream(Gems.values()).forEach(value -> RebornRegistry.registerItem(value.item));
		Arrays.stream(Dusts.values()).forEach(value -> RebornRegistry.registerItem(value.item));
		Arrays.stream(SmallDusts.values()).forEach(value -> RebornRegistry.registerItem(value.item));
		Arrays.stream(Plates.values()).forEach(value -> RebornRegistry.registerItem(value.item));
		Arrays.stream(Parts.values()).forEach(value -> RebornRegistry.registerItem(value.item));
		Arrays.stream(Upgrades.values()).forEach(value -> RebornRegistry.registerItem(value.item));

		// Gem armor & tools
		if (TechRebornConfig.enableGemArmorAndTools) {
			//Todo: repair with tags
			RebornRegistry.registerItem(TRContent.BRONZE_SWORD = InitUtils.setup(new ItemTRSword(TRToolTier.BRONZE), "bronze_sword"));
			RebornRegistry.registerItem(TRContent.BRONZE_PICKAXE = InitUtils.setup(new ItemTRPickaxe(TRToolTier.BRONZE), "bronze_pickaxe"));
			RebornRegistry.registerItem(TRContent.BRONZE_SPADE = InitUtils.setup(new ItemTRSpade(TRToolTier.BRONZE), "bronze_spade"));
			RebornRegistry.registerItem(TRContent.BRONZE_AXE = InitUtils.setup(new ItemTRAxe(TRToolTier.BRONZE), "bronze_axe"));
			RebornRegistry.registerItem(TRContent.BRONZE_HOE = InitUtils.setup(new ItemTRHoe(TRToolTier.BRONZE), "bronze_hoe"));

			RebornRegistry.registerItem(TRContent.BRONZE_HELMET = InitUtils.setup(new ItemTRArmour(TRArmorMaterial.BRONZE, EquipmentSlot.HEAD), "bronze_helmet"));
			RebornRegistry.registerItem(TRContent.BRONZE_CHESTPLATE = InitUtils.setup(new ItemTRArmour(TRArmorMaterial.BRONZE, EquipmentSlot.CHEST), "bronze_chestplate"));
			RebornRegistry.registerItem(TRContent.BRONZE_LEGGINGS = InitUtils.setup(new ItemTRArmour(TRArmorMaterial.BRONZE, EquipmentSlot.LEGS), "bronze_leggings"));
			RebornRegistry.registerItem(TRContent.BRONZE_BOOTS = InitUtils.setup(new ItemTRArmour(TRArmorMaterial.BRONZE, EquipmentSlot.FEET), "bronze_boots"));

			RebornRegistry.registerItem(TRContent.RUBY_SWORD = InitUtils.setup(new ItemTRSword(TRToolTier.RUBY), "ruby_sword"));
			RebornRegistry.registerItem(TRContent.RUBY_PICKAXE = InitUtils.setup(new ItemTRPickaxe(TRToolTier.RUBY), "ruby_pickaxe"));
			RebornRegistry.registerItem(TRContent.RUBY_SPADE = InitUtils.setup(new ItemTRSpade(TRToolTier.RUBY), "ruby_spade"));
			RebornRegistry.registerItem(TRContent.RUBY_AXE = InitUtils.setup(new ItemTRAxe(TRToolTier.RUBY), "ruby_axe"));
			RebornRegistry.registerItem(TRContent.RUBY_HOE = InitUtils.setup(new ItemTRHoe(TRToolTier.RUBY), "ruby_hoe"));

			RebornRegistry.registerItem(TRContent.RUBY_HELMET = InitUtils.setup(new ItemTRArmour(TRArmorMaterial.RUBY, EquipmentSlot.HEAD), "ruby_helmet"));
			RebornRegistry.registerItem(TRContent.RUBY_CHESTPLATE = InitUtils.setup(new ItemTRArmour(TRArmorMaterial.RUBY, EquipmentSlot.CHEST), "ruby_chestplate"));
			RebornRegistry.registerItem(TRContent.RUBY_LEGGINGS = InitUtils.setup(new ItemTRArmour(TRArmorMaterial.RUBY, EquipmentSlot.LEGS), "ruby_leggings"));
			RebornRegistry.registerItem(TRContent.RUBY_BOOTS = InitUtils.setup(new ItemTRArmour(TRArmorMaterial.RUBY, EquipmentSlot.FEET), "ruby_boots"));

			RebornRegistry.registerItem(TRContent.SAPPHIRE_SWORD = InitUtils.setup(new ItemTRSword(TRToolTier.SAPPHIRE), "sapphire_sword"));
			RebornRegistry.registerItem(TRContent.SAPPHIRE_PICKAXE = InitUtils.setup(new ItemTRPickaxe(TRToolTier.SAPPHIRE), "sapphire_pickaxe"));
			RebornRegistry.registerItem(TRContent.SAPPHIRE_SPADE = InitUtils.setup(new ItemTRSpade(TRToolTier.SAPPHIRE), "sapphire_spade"));
			RebornRegistry.registerItem(TRContent.SAPPHIRE_AXE = InitUtils.setup(new ItemTRAxe(TRToolTier.SAPPHIRE), "sapphire_axe"));
			RebornRegistry.registerItem(TRContent.SAPPHIRE_HOE = InitUtils.setup(new ItemTRHoe(TRToolTier.SAPPHIRE), "sapphire_hoe"));

			RebornRegistry.registerItem(TRContent.SAPPHIRE_HELMET = InitUtils.setup(new ItemTRArmour(TRArmorMaterial.SAPPHIRE, EquipmentSlot.HEAD), "sapphire_helmet"));
			RebornRegistry.registerItem(TRContent.SAPPHIRE_CHESTPLATE = InitUtils.setup(new ItemTRArmour(TRArmorMaterial.SAPPHIRE, EquipmentSlot.CHEST), "sapphire_chestplate"));
			RebornRegistry.registerItem(TRContent.SAPPHIRE_LEGGINGS = InitUtils.setup(new ItemTRArmour(TRArmorMaterial.SAPPHIRE, EquipmentSlot.LEGS), "sapphire_leggings"));
			RebornRegistry.registerItem(TRContent.SAPPHIRE_BOOTS = InitUtils.setup(new ItemTRArmour(TRArmorMaterial.SAPPHIRE, EquipmentSlot.FEET), "sapphire_boots"));

			RebornRegistry.registerItem(TRContent.PERIDOT_SWORD = InitUtils.setup(new ItemTRSword(TRToolTier.PERIDOT), "peridot_sword"));
			RebornRegistry.registerItem(TRContent.PERIDOT_PICKAXE = InitUtils.setup(new ItemTRPickaxe(TRToolTier.PERIDOT), "peridot_pickaxe"));
			RebornRegistry.registerItem(TRContent.PERIDOT_SPADE = InitUtils.setup(new ItemTRSpade(TRToolTier.PERIDOT), "peridot_spade"));
			RebornRegistry.registerItem(TRContent.PERIDOT_AXE = InitUtils.setup(new ItemTRAxe(TRToolTier.PERIDOT), "peridot_axe"));
			RebornRegistry.registerItem(TRContent.PERIDOT_HOE = InitUtils.setup(new ItemTRHoe(TRToolTier.PERIDOT), "peridot_hoe"));

			RebornRegistry.registerItem(TRContent.PERIDOT_HELMET = InitUtils.setup(new ItemTRArmour(TRArmorMaterial.PERIDOT, EquipmentSlot.HEAD), "peridot_helmet"));
			RebornRegistry.registerItem(TRContent.PERIDOT_CHESTPLATE = InitUtils.setup(new ItemTRArmour(TRArmorMaterial.PERIDOT, EquipmentSlot.CHEST), "peridot_chestplate"));
			RebornRegistry.registerItem(TRContent.PERIDOT_LEGGINGS = InitUtils.setup(new ItemTRArmour(TRArmorMaterial.PERIDOT, EquipmentSlot.LEGS), "peridot_leggings"));
			RebornRegistry.registerItem(TRContent.PERIDOT_BOOTS = InitUtils.setup(new ItemTRArmour(TRArmorMaterial.PERIDOT, EquipmentSlot.FEET), "peridot_boots"));
		}

		// Battery
		RebornRegistry.registerItem(TRContent.RED_CELL_BATTERY = InitUtils.setup(new ItemRedCellBattery(), "red_cell_battery"));
		RebornRegistry.registerItem(TRContent.LITHIUM_ION_BATTERY = InitUtils.setup(new ItemLithiumIonBattery(), "lithium_ion_battery"));
		RebornRegistry.registerItem(TRContent.LITHIUM_ION_BATPACK = InitUtils.setup(new ItemLithiumIonBatpack(), "lithium_ion_batpack"));
		RebornRegistry.registerItem(TRContent.ENERGY_CRYSTAL = InitUtils.setup(new ItemEnergyCrystal(), "energy_crystal"));
		RebornRegistry.registerItem(TRContent.LAPOTRON_CRYSTAL = InitUtils.setup(new ItemLapotronCrystal(), "lapotron_crystal"));
		RebornRegistry.registerItem(TRContent.LAPOTRONIC_ORB = InitUtils.setup(new ItemLapotronicOrb(), "lapotronic_orb"));
		RebornRegistry.registerItem(TRContent.LAPOTRONIC_ORBPACK = InitUtils.setup(new ItemLapotronicOrbpack(), "lapotronic_orbpack"));

		// Tools
		RebornRegistry.registerItem(TRContent.TREE_TAP = InitUtils.setup(new ItemTreeTap(), "treetap"));
		RebornRegistry.registerItem(TRContent.WRENCH = InitUtils.setup(new ItemWrench(), "wrench"));

		RebornRegistry.registerItem(TRContent.BASIC_DRILL = InitUtils.setup(new ItemBasicDrill(), "basic_drill"));
		RebornRegistry.registerItem(TRContent.BASIC_CHAINSAW = InitUtils.setup(new ItemBasicChainsaw(), "basic_chainsaw"));
		RebornRegistry.registerItem(TRContent.BASIC_JACKHAMMER = InitUtils.setup(new ItemBasicJackhammer(), "basic_jackhammer"));
		RebornRegistry.registerItem(TRContent.ELECTRIC_TREE_TAP = InitUtils.setup(new ItemElectricTreetap(), "electric_treetap"));

		RebornRegistry.registerItem(TRContent.ADVANCED_DRILL = InitUtils.setup(new ItemAdvancedDrill(), "advanced_drill"));
		RebornRegistry.registerItem(TRContent.ADVANCED_CHAINSAW = InitUtils.setup(new ItemAdvancedChainsaw(), "advanced_chainsaw"));
		RebornRegistry.registerItem(TRContent.ADVANCED_JACKHAMMER = InitUtils.setup(new ItemAdvancedJackhammer(), "advanced_jackhammer"));
		RebornRegistry.registerItem(TRContent.ROCK_CUTTER = InitUtils.setup(new ItemRockCutter(), "rock_cutter"));

		RebornRegistry.registerItem(TRContent.INDUSTRIAL_DRILL = InitUtils.setup(new ItemIndustrialDrill(), "industrial_drill"));
		RebornRegistry.registerItem(TRContent.INDUSTRIAL_CHAINSAW = InitUtils.setup(new ItemIndustrialChainsaw(), "industrial_chainsaw"));
		RebornRegistry.registerItem(TRContent.INDUSTRIAL_JACKHAMMER = InitUtils.setup(new ItemIndustrialJackhammer(), "industrial_jackhammer"));
		RebornRegistry.registerItem(TRContent.NANOSABER = InitUtils.setup(new ItemNanosaber(), "nanosaber"));
		RebornRegistry.registerItem(TRContent.OMNI_TOOL = InitUtils.setup(new ItemOmniTool(), "omni_tool"));

		// Armor
		RebornRegistry.registerItem(TRContent.CLOAKING_DEVICE = InitUtils.setup(new ItemCloakingDevice(), "cloaking_device"));

		// Other
		RebornRegistry.registerItem(TRContent.FREQUENCY_TRANSMITTER = InitUtils.setup(new ItemFrequencyTransmitter(), "frequency_transmitter"));
		RebornRegistry.registerItem(TRContent.SCRAP_BOX = InitUtils.setup(new ItemScrapBox(), "scrap_box"));
		RebornRegistry.registerItem(TRContent.MANUAL = InitUtils.setup(new ItemManual(), "manual"));
		RebornRegistry.registerItem(TRContent.DEBUG_TOOL = InitUtils.setup(new ItemDebugTool(), "debug_tool"));
		RebornRegistry.registerItem(TRContent.CELL = InitUtils.setup(new ItemDynamicCell(), "cell"));

		TechReborn.LOGGER.debug("TechReborns Items Loaded");
	}

	private static void registerFluids() {
		Arrays.stream(ModFluids.values()).forEach(ModFluids::register);
	}
	
	private static void registerSounds() {
		ModSounds.ALARM = InitUtils.setup("alarm");
		ModSounds.ALARM_2 = InitUtils.setup("alarm_2");
		ModSounds.ALARM_3 = InitUtils.setup("alarm_3");
		ModSounds.AUTO_CRAFTING = InitUtils.setup("auto_crafting");
		ModSounds.BLOCK_DISMANTLE = InitUtils.setup("block_dismantle");
		ModSounds.CABLE_SHOCK = InitUtils.setup("cable_shock");
		ModSounds.MACHINE_RUN = InitUtils.setup("machine_run");
		ModSounds.MACHINE_START = InitUtils.setup("machine_start");
		ModSounds.SAP_EXTRACT = InitUtils.setup("sap_extract");
	}
}
