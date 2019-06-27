package torcherino.api.impl;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import torcherino.api.Tier;
import torcherino.api.TorcherinoAPI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TorcherinoImpl implements TorcherinoAPI
{
	private Map<ResourceLocation, Tier> tiers = new HashMap<>();

	private Set<Block> blacklistedBlocks = new HashSet<>();

	private Set<TileEntityType> blacklistedTiles = new HashSet<>();

	public boolean registerTier(ResourceLocation name, int maxSpeed, int xzRange, int yRange)
	{
		Tier tier = new Tier(maxSpeed, xzRange, yRange);
		if (tiers.containsKey(name))
		{
			return false;
		}
		tiers.put(name, tier);
		return true;
	}

	public boolean blacklistBlock(ResourceLocation block)
	{
		if (ForgeRegistries.BLOCKS.containsKey(block))
		{
			Block b = ForgeRegistries.BLOCKS.getValue(block);
			if (blacklistedBlocks.contains(b)) return false;
			blacklistedBlocks.add(b);
			return true;
		}
		return false;
	}

	@Override public boolean blacklistBlock(Block block)
	{
		if(blacklistedBlocks.contains(block)) return false;
		blacklistedBlocks.add(block);
		return true;
	}

	@Override public boolean blacklistTileEntity(ResourceLocation tileEntity)
	{
		if (ForgeRegistries.TILE_ENTITIES.containsKey(tileEntity))
		{
			TileEntityType type = ForgeRegistries.TILE_ENTITIES.getValue(tileEntity);
			if (blacklistedTiles.contains(type)) return false;
			blacklistedTiles.add(type);
			return true;
		}
		return false;
	}

	@Override public boolean blacklistTileEntity(TileEntityType tileEntity)
	{
		if(blacklistedTiles.contains(tileEntity)) return false;
		blacklistedTiles.add(tileEntity);
		return true;
	}

	@Override public boolean isBlockBlacklisted(Block block){ return blacklistedBlocks.contains(block); }

	@Override public boolean isTileEntityBlacklisted(TileEntityType tileEntityType){ return blacklistedTiles.contains(tileEntityType); }

	public ImmutableMap<ResourceLocation, Tier> getTiers(){ return ImmutableMap.copyOf(tiers); }
}
