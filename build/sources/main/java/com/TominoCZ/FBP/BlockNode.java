package com.TominoCZ.FBP;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.state.IBlockState;

public class BlockNode {
	public IBlockState state;
	public Block originalBlock;
	// public BlockSlab.EnumBlockHalf half;

	public BlockNode(IBlockState s) {// , @Nullable BlockSlab.EnumBlockHalf half) {
		// this.half = half;
		state = s;
		originalBlock = s.getBlock();
	}
}