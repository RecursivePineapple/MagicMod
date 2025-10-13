package magicmod.common.mechanics.aura;

import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.event.world.WorldEvent;

import com.gtnewhorizon.gtnhlib.eventbus.EventBusSubscriber;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import magicmod.common.util.ChunkMap;
import magicmod.common.util.MCUtils;

@EventBusSubscriber
public class NodeSaveData extends WorldSavedData {

    private static final String DATA_NAME = "magicmod.aura_nodes";

    private static final Int2ObjectMap<NodeSaveData> INSTANCES = new Int2ObjectOpenHashMap<>();

    private final Object2ObjectMap<String, ChunkMap<AuraNodeState>> auraFields = new Object2ObjectOpenHashMap<>();

    public NodeSaveData(String name) {
        super(name);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        auraFields.clear();

        //noinspection unchecked
        for (var e : ((Map<String, NBTTagCompound>) tag.tagMap).entrySet()) {
            ChunkMap<AuraNodeState> nodeStates = new ChunkMap<>();

            auraFields.put(e.getKey(), nodeStates);

            for (NBTTagCompound node : MCUtils.getTagList(e.getValue(), "nodes")) {
                AuraNodeState state = new AuraNodeState();

                state.lastDrawTime = node.getLong("d");
                state.remainingAuraPostDraw = node.getDouble("c");

                nodeStates.put(node.getInteger("x"), node.getInteger("z"), state);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        for (var e : auraFields.entrySet()) {
            NBTTagCompound dim = new NBTTagCompound();
            tag.setTag(e.getKey(), dim);

            NBTTagList nodes = new NBTTagList();
            dim.setTag("nodes", nodes);

            for (var e2 : e.getValue().fastEntryIterable()) {
                NBTTagCompound node = new NBTTagCompound();

                node.setInteger("x", e2.getChunkX());
                node.setInteger("z", e2.getChunkZ());
                node.setLong("d", e2.getValue().lastDrawTime);
                node.setDouble("c", e2.getValue().remainingAuraPostDraw);

                nodes.appendTag(node);
            }
        }
    }

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        if (event.world.isRemote) return;

        NodeSaveData saveData = (NodeSaveData) event.world.mapStorage.loadData(NodeSaveData.class, DATA_NAME);

        if (saveData == null) {
            saveData = new NodeSaveData(DATA_NAME);

            event.world.mapStorage.setData(DATA_NAME, saveData);
        }

        INSTANCES.put(event.world.provider.dimensionId, saveData);
        saveData.markDirty();
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        if (event.world.isRemote) return;

        INSTANCES.remove(event.world.provider.dimensionId);
    }

    public static AuraNodeState getNodeState(World world, String fieldId, int chunkX, int chunkZ) {
        NodeSaveData instance = INSTANCES.get(world.provider.dimensionId);

        if (instance == null) return null;

        var field = instance.auraFields.computeIfAbsent(fieldId, x -> new ChunkMap<>());

        var state = field.get(chunkX, chunkZ);

        if (state == null) {
            state = new AuraNodeState();
            field.put(chunkX, chunkZ, state);
        }

        return state;
    }

    public static class AuraNodeState {
        public long lastDrawTime;
        public double remainingAuraPostDraw;
    }
}
