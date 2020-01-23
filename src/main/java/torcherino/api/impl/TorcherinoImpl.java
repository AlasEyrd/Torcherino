package torcherino.api.impl;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import torcherino.api.Tier;
import torcherino.api.TorcherinoAPI;

import java.util.HashMap;
import java.util.HashSet;

/**
 * DO NOT USE THIS CLASS DIRECTLY. Use TorcherinoAPI.INSTANCE instead.
 */
@SuppressWarnings("SpellCheckingInspection")
public class TorcherinoImpl implements TorcherinoAPI
{
    @Deprecated
    public static final TorcherinoImpl INSTANCE = new TorcherinoImpl();

    private final Logger LOGGER = LogManager.getLogger("torcherino-api");
    private final HashMap<Identifier, Tier> localTiers;
    private final HashSet<Identifier> blacklistedBlocks;
    private final HashSet<Identifier> blacklistedBlockEntities;
    private HashMap<Identifier, Tier> remoteTiers;

    private TorcherinoImpl()
    {
        localTiers = new HashMap<>();
        blacklistedBlocks = new HashSet<>();
        blacklistedBlockEntities = new HashSet<>();
    }

    @Override
    public ImmutableMap<Identifier, Tier> getTiers() { return ImmutableMap.copyOf(localTiers); }

    @Override
    public Tier getTier(Identifier tierIdentifier) { return remoteTiers.getOrDefault(tierIdentifier, null); }

    @Override
    public boolean registerTier(Identifier tierIdentifier, int maxSpeed, int xzRange, int yRange)
    {
        Tier tier = new Tier(maxSpeed, xzRange, yRange);
        if (localTiers.containsKey(tierIdentifier))
        {
            LOGGER.error("[Torcherino] Tier with id {} has already been declared.", tierIdentifier);
            return false;
        }
        localTiers.put(tierIdentifier, tier);
        return true;
    }

    @Override
    public boolean blacklistBlock(Identifier blockIdentifier)
    {
        if (blacklistedBlocks.contains(blockIdentifier))
        {
            LOGGER.warn("[Torcherino] Block with id {} has already been blacklisted.", blockIdentifier);
            return false;
        }
        blacklistedBlocks.add(blockIdentifier);
        return true;
    }

    @Override
    public boolean blacklistBlock(Block block)
    {
        Identifier blockIdentifier = Registry.BLOCK.getId(block);
        if (Registry.BLOCK.get(blockIdentifier) != block)
        {
            LOGGER.error("[Torcherino] Please register your block before attempting to blacklist.");
            return false;
        }
        else if (blacklistedBlocks.contains(blockIdentifier))
        {
            LOGGER.warn("[Torcherino] Block with id {} has already been blacklisted.", blockIdentifier);
            return false;
        }
        blacklistedBlocks.add(blockIdentifier);
        return true;
    }

    @Override
    public boolean blacklistBlockEntity(Identifier blockEntityIdentifier)
    {
        if (blacklistedBlockEntities.contains(blockEntityIdentifier))
        {
            LOGGER.warn("[Torcherino] Block entity with id {} has already been blacklisted.", blockEntityIdentifier);
            return false;
        }
        blacklistedBlockEntities.add(blockEntityIdentifier);
        return true;
    }

    @Override
    public boolean blacklistBlockEntity(BlockEntityType<?> blockEntityType)
    {
        Identifier blockEntityTypeIdentifier = Registry.BLOCK_ENTITY_TYPE.getId(blockEntityType);
        if (blockEntityTypeIdentifier == null)
        {
            LOGGER.error("[Torcherino] Please register your block entity type before attempting to blacklist.");
            return false;
        }
        else if (blacklistedBlockEntities.contains(blockEntityTypeIdentifier))
        {
            LOGGER.warn("[Torcherino] Block entity with id {} has already been blacklisted.", blockEntityTypeIdentifier);
            return false;
        }
        blacklistedBlockEntities.add(blockEntityTypeIdentifier);
        return true;
    }

    @Override
    public boolean isBlockBlacklisted(Block block) { return blacklistedBlocks.contains(Registry.BLOCK.getId(block)); }

    @Override
    public boolean isBlockEntityBlacklisted(BlockEntityType<?> blockEntityType)
    {
        return blacklistedBlockEntities.contains(BlockEntityType.getId(blockEntityType));
    }

    // Internal do not use.
    public void setRemoteTiers(HashMap<Identifier, Tier> tiers) { remoteTiers = tiers; }
}
