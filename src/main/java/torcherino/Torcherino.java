package torcherino;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import torcherino.api.TorcherinoAPI;
import torcherino.api.blocks.entity.TorcherinoBlockEntity;
import torcherino.api.entrypoints.TorcherinoInitializer;
import torcherino.blocks.ModBlocks;
import torcherino.config.Config;

import java.util.ArrayList;
import java.util.HashSet;

@SuppressWarnings("SpellCheckingInspection")
public class Torcherino implements ModInitializer, TorcherinoInitializer
{
    public static final String MOD_ID = "torcherino";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    private static final HashSet<String> allowedUuids = new HashSet<>();
    public static ArrayList<DefaultParticleType> particles = new ArrayList<>();

    public static void playerConnected(String uuid) { allowedUuids.add(uuid); }

    public static boolean hasIsOnline(String uuid) { return allowedUuids.contains(uuid); }

    public static void playerDisconnect(String uuid) { if (Config.INSTANCE.online_mode.equals("ONLINE")) allowedUuids.remove(uuid); }

    @Override
    public void onInitialize()
    {
        Config.initialize();
        TorcherinoAPI.INSTANCE.getTiers().forEach((id, tier) ->
        {
            if (!id.getNamespace().equals(MOD_ID)) return;
            String path = id.getPath() + "_flame";
            if (path.equals("normal_flame")) path = "flame";
            particles.add(Registry.register(Registry.PARTICLE_TYPE, new Identifier(MOD_ID, path), FabricParticleTypes.simple()));
        });
        ModBlocks.INSTANCE.initialize();
        ServerSidePacketRegistry.INSTANCE.register(new Identifier(Torcherino.MOD_ID, "utv"), (PacketContext context, PacketByteBuf buffer) ->
        {
            World world = context.getPlayer().getEntityWorld();
            BlockPos pos = buffer.readBlockPos();
            buffer.retain();
            context.getTaskQueue().execute(() ->
            {
                try
                {
                    BlockEntity blockEntity = world.getBlockEntity(pos);
                    if (blockEntity instanceof TorcherinoBlockEntity) ((TorcherinoBlockEntity) blockEntity).readClientData(buffer);
                }
                finally
                {
                    buffer.release();
                }
            });
        });
        FabricLoader.getInstance().getEntrypoints("torcherinoInitializer", TorcherinoInitializer.class).forEach(TorcherinoInitializer::onTorcherinoInitialize);
    }

    @Override
    public void onTorcherinoInitialize()
    {
        TorcherinoAPI.INSTANCE.blacklistBlock(Blocks.WATER);
        TorcherinoAPI.INSTANCE.blacklistBlock(Blocks.LAVA);
        TorcherinoAPI.INSTANCE.blacklistBlock(Blocks.AIR);
        TorcherinoAPI.INSTANCE.blacklistBlock(Blocks.CAVE_AIR);
        TorcherinoAPI.INSTANCE.blacklistBlock(Blocks.VOID_AIR);
        if (FabricLoader.getInstance().isModLoaded("computercraft"))
        {
            TorcherinoAPI.INSTANCE.blacklistBlockEntity(new Identifier("computercraft", "turtle_normal"));
            TorcherinoAPI.INSTANCE.blacklistBlockEntity(new Identifier("computercraft", "turtle_advanced"));
        }
    }
}
