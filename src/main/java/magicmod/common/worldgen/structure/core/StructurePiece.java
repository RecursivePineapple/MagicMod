package magicmod.common.worldgen.structure.core;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.common.util.ForgeDirection;

import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.SerializedName;
import com.gtnewhorizon.gtnhlib.blockstate.core.BlockState;
import com.gtnewhorizon.gtnhlib.blockstate.core.BlockStateImpl;
import com.gtnewhorizon.gtnhlib.blockstate.core.BlockStateSerializer;
import com.gtnewhorizon.gtnhlib.blockstate.registry.BlockPropertyRegistry;
import com.gtnewhorizon.gtnhlib.geometry.VectorTransform;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import magicmod.common.data.BlockPosMap;
import magicmod.common.util.MCUtils;
import magicmod.common.util.VoxelAABB;

public class StructurePiece {

    @SerializedName("DataVersion")
    public int dataVersion;
    public VoxelAABB aabb;
    public List<EntitySpec> entities = Collections.emptyList();
    public StructureBlocks blocks = new StructureBlocks();
    public List<PaletteBlock> palette = Collections.emptyList();
    public List<Socket> sockets = Collections.emptyList();
    public float weight = 1f;
    public List<BudgetOperation> budget = Collections.emptyList();

    @EqualsAndHashCode
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class EntitySpec {
        public Vector3i blockPos;
        public Vector3f pos;
        public JsonElement tag;
    }

    public static class StructureBlocks extends ArrayList<BlockSpec> {

    }

    @EqualsAndHashCode
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class BlockSpec {
        public Vector3i pos;
        @SerializedName("state")
        public int blockIndex;
    }

    @EqualsAndHashCode
    @NoArgsConstructor
    @ToString
    public static class PaletteBlock {
        public BlockState blockState;

        public PaletteBlock(BlockState blockState) {
            this.blockState = blockState;
        }

        @Override
        public PaletteBlock clone() {
            return new PaletteBlock(this.blockState);
        }
    }

    private static class PaletteBlockSerializer implements JsonSerializer<PaletteBlock>, JsonDeserializer<PaletteBlock> {

        @Override
        public JsonElement serialize(PaletteBlock src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(src.blockState);
        }

        @Override
        public PaletteBlock deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            return new PaletteBlock(context.deserialize(json, BlockState.class));
        }
    }

    @EqualsAndHashCode
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class Socket {
        public Vector3i pos;
        public String category;
        public String id;
        public ForgeDirection forward;
        public List<BudgetOperation> budget = Collections.emptyList();
    }

    public enum BudgetAction {
        @SerializedName("consume")
        Consume,
        @SerializedName("reset")
        Reset,
        @SerializedName("reset-random")
        ResetRandom,
    }

    @EqualsAndHashCode
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class BudgetOperation {
        public BudgetAction action;
        public String category;
        public int amount;
        // For reset-random
        public int lower, upper;
    }

    /// @param aabb The transformed structure bounding box
    public void place(World world, Vector3i origin, StructureBoundingBox aabb, VectorTransform transform) {
        if (transform == null) {
            transform = new VectorTransform() {
                @Override
                public Vector3f transform(Vector3f v) {
                    return v;
                }

                @Override
                public Vector3f inverse(Vector3f v) {
                    return v;
                }
            };
        }

        if (aabb != null) {
            aabb = new StructureBoundingBox(aabb);
            aabb.offset(-origin.x, -origin.y, -origin.z);
            aabb.offset(this.aabb.origin.x, this.aabb.origin.y, this.aabb.origin.z);
        }

        List<PaletteBlock> transformedPalette = new ArrayList<>(this.palette);

        final VectorTransform transform2 = transform;

        transformedPalette.replaceAll(palette -> {
            palette = palette.clone();

            palette.blockState.transform(transform2);

            return palette;
        });

        Vector3f v = new Vector3f();

        for (BlockSpec block : blocks) {
            transform.transform(v.set(block.pos));

            int x = Math.round(v.x);
            int y = Math.round(v.y);
            int z = Math.round(v.z);

            if (aabb != null && !aabb.isVecInside(x, y, z)) continue;

            place(
                world,
                origin.x + x,
                origin.y + y,
                origin.z + z,
                this.palette.get(block.blockIndex));
        }

        if (entities != null) {
            for (EntitySpec spec : entities) {
                if (aabb != null && !aabb.isVecInside(spec.blockPos.x, spec.blockPos.y, spec.blockPos.z)) continue;

                Entity entity = EntityList.createEntityFromNBT(MCUtils.toNbt(spec.tag), world);

                entity.setPosition(spec.pos.x + origin.x, spec.pos.y + origin.y, spec.pos.z + origin.z);

                world.spawnEntityInWorld(entity);
            }
        }
    }

    protected void place(World world, int x, int y, int z, PaletteBlock block) {
        block.blockState.place(world, x, y, z);
    }

    public List<Socket> getSockets() {
        return sockets == null ? Collections.emptyList() : sockets;
    }

    public static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(StructureBlocks.class, new StructureBlocksAdapter())
        .registerTypeAdapter(BlockStateImpl.class, BlockStateSerializer.INSTANCE)
        .registerTypeAdapter(BlockState.class, BlockStateSerializer.INSTANCE)
        .registerTypeAdapter(PaletteBlock.class, new PaletteBlockSerializer())
        .create();

    public static StructurePiece load(String path) throws IOException {
        Path fsPath = path.contains(File.separator) ? Paths.get(path) : Paths.get("structure-pieces", path);

        InputStream stream = Files.exists(fsPath) ?
            Files.newInputStream(fsPath) :
            StructurePiece.class.getClassLoader().getResourceAsStream(path);

        if (stream == null) return null;

        try (InputStream input = stream) {
            if (path.endsWith("nbt")) {
                NBTTagCompound tag = CompressedStreamTools.func_152456_a(new DataInputStream(input), new NBTSizeTracker(Long.MAX_VALUE));

                return load(tag);
            } else if (path.endsWith("json")) {
                return GSON.fromJson(new InputStreamReader(input), StructurePiece.class);
            } else {
                throw new IllegalArgumentException("Structure piece must be a .nbt or .json file");
            }
        }
    }

    public static StructurePiece load(NBTTagCompound tag) {
        JsonObject json = MCUtils.toJsonObject(tag);

        return GSON.fromJson(json, StructurePiece.class);
    }

    public static StructurePiece load(JsonObject json) {
        return GSON.fromJson(json, StructurePiece.class);
    }

    public static NBTTagCompound saveNBT(StructurePiece piece) {
        return MCUtils.toNbt(GSON.toJsonTree(piece));
    }

    public static JsonObject saveJson(StructurePiece piece) {
        return (JsonObject) GSON.toJsonTree(piece);
    }

    public static StructurePiece fromWorld(World world, VoxelAABB aabb) {
        ArrayList<PaletteBlock> palette = new ArrayList<>();
        Object2IntOpenHashMap<PaletteBlock> paletteMap = new Object2IntOpenHashMap<>();

        StructureBlocks blocks = new StructureBlocks();

        int cX = aabb.origin.x;
        int cY = aabb.origin.y;
        int cZ = aabb.origin.z;

        Vector3i min = aabb.min(), max = aabb.max();

        BlockPosMap<BlockSpec> specs = new BlockPosMap<>();

        for (Vector3ic v : aabb) {
            int x = v.x();
            int y = v.y();
            int z = v.z();

            PaletteBlock block = new PaletteBlock();

            block.blockState = BlockPropertyRegistry.getBlockState(world, x, y, z);

            int index = paletteMap.computeIfAbsent(block, (PaletteBlock b) -> {
                int nextIndex = palette.size();
                palette.add(b);
                return nextIndex;
            });

            BlockSpec spec = new BlockSpec(new Vector3i(x - cX, y - cY, z - cZ), index);
            blocks.add(spec);
            specs.put(x, y, z, spec);
        }

        for (Vector3ic v : aabb) {
            int x = v.x();
            int y = v.y();
            int z = v.z();

            boolean extraneous = scan(aabb, x, y, z, (x2, y2, z2) -> {
                int index = specs.get(x2, y2, z2).blockIndex;

                if (index == -1) {
                    return true;
                }

                PaletteBlock block = palette.get(index);

                return !block.blockState.getBlock().isAir(world, x2, y2, z2);
            });

            if (extraneous) {
                specs.get(x, y, z).blockIndex = -1;
            }
        }

        blocks.removeIf(spec -> spec.blockIndex == -1);

        StructurePiece piece = new StructurePiece();
        piece.dataVersion = 1;

        piece.palette = palette;
        piece.blocks = blocks;
        piece.aabb = aabb.clone();

        piece.entities = new ArrayList<>();

        List<Entity> entitiesInPiece = world.getEntitiesWithinAABB(Entity.class, aabb.toBoundingBox());

        for (Entity entity : entitiesInPiece) {
            if (entity instanceof EntityPlayer) continue;

            NBTTagCompound tag = new NBTTagCompound();

            if (!entity.writeToNBTOptional(tag)) continue;

            piece.entities.add(new EntitySpec(
                new Vector3i(MathHelper.floor_double(entity.posX), MathHelper.floor_double(entity.posY), MathHelper.floor_double(entity.posZ)).sub(cX, cY, cZ),
                new Vector3f((float) (entity.posX - cX), (float) (entity.posY - cY), (float) (entity.posZ - cZ)),
                MCUtils.toJsonObject(tag)
            ));
        }

        piece.aabb.setOrigin(new Vector3i(0, 0, 0));

        return piece;
    }

    interface ScanPredicate {
        boolean test(int x, int y, int z);
    }

    private static boolean scan(VoxelAABB aabb, int x, int y, int z, ScanPredicate test) {
        Vector3i min = aabb.min(), max = aabb.max();

        boolean neg = false, pos = false;

        for (int x2 = x; x2 >= min.x; x2--) {
            if (test.test(x2, y, z)) {
                neg = true;
                break;
            }
        }

        for (int x2 = x; x2 <= max.x; x2++) {
            if (test.test(x2, y, z)) {
                pos = true;
                break;
            }
        }

        if (neg && pos) return false;

        neg = false;
        pos = false;

        for (int y2 = y; y2 >= min.y; y2--) {
            if (test.test(x, y2, z)) {
                neg = true;
                break;
            }
        }

        for (int y2 = y; y2 <= max.y; y2++) {
            if (test.test(x, y2, z)) {
                pos = true;
                break;
            }
        }

        if (neg && pos) return false;

        neg = false;
        pos = false;

        for (int z2 = z; z2 >= min.z; z2--) {
            if (test.test(x, y, z2)) {
                neg = true;
                break;
            }
        }

        for (int z2 = z; z2 <= max.z; z2++) {
            if (test.test(x, y, z2)) {
                pos = true;
                break;
            }
        }

        if (neg && pos) return false;

        return true;
    }

    private static class CompactBlockList {
        public int offsetX, offsetY, offsetZ;
        public String[][] structure;
    }

    private static class StructureBlocksAdapter implements JsonSerializer<StructureBlocks>, JsonDeserializer<StructureBlocks> {

        @Override
        public StructureBlocks deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonArray()) {
                ArrayList<BlockSpec> blocks = context.deserialize(json, new TypeToken<ArrayList<BlockSpec>>(){}.getType());

                StructureBlocks structure = new StructureBlocks();

                structure.addAll(blocks);

                return structure;
            }

            CompactBlockList structure = context.deserialize(json, CompactBlockList.class);

            int oX = structure.offsetX;
            int oY = structure.offsetY;
            int oZ = structure.offsetZ;

            StructureBlocks list = new StructureBlocks();

            for (int y = 0; y < structure.structure.length; y++) {
                // y plane
                String[] layer = structure.structure[y];

                for (int z = 0; z < layer.length; z++) {
                    // z row
                    int[] chars = layer[z].chars().toArray();

                    for (int x = 0; x < chars.length; x++) {
                        int ch = chars[x];

                        int ordinal = char2index(ch);

                        if (ordinal == -1) continue;

                        list.add(new BlockSpec(new Vector3i(x + oX, y + oY, z + oZ), ordinal));
                    }
                }
            }

            return list;
        }

        private static int char2index(int ch) {
            if (ch >= 'A' && ch <= 'Z') {
                return ch - 'A';
            }

            if (ch >= 'a' && ch <= 'z') {
                return ch - 'a' + 26;
            }

            if (ch >= '0' && ch <= '9') {
                return ch - '0' + 26 + 26;
            }

            return -1;
        }

        @Override
        public JsonElement serialize(StructureBlocks src, Type typeOfSrc, JsonSerializationContext context) {
            Vector3i min = new Vector3i(), max = new Vector3i();

            for (BlockSpec block : src) {
                min.min(block.pos);
                max.max(block.pos);
            }

            int dY = max.y - min.y + 1;
            int dZ = max.z - min.z + 1;
            int dX = max.x - min.x + 1;

            int[][][] structure = new int[dY][dZ][dX];

            for (BlockSpec block : src) {
                structure[block.pos.y - min.y][block.pos.z - min.z][block.pos.x - min.x] = block.blockIndex + 1; // 1 indexed :doom:
            }

            String[][] out = new String[dY][dZ];

            StringBuilder sb = new StringBuilder();
            sb.ensureCapacity(dX);

            for (int y = 0; y < dY; y++) {
                // y plane
                String[] layer = out[y];
                int[][] intLayer = structure[y];

                for (int z = 0; z < dZ; z++) {
                    // z row
                    int[] intRow = intLayer[z];

                    sb.setLength(0);

                    for (int x = 0; x < dX; x++) {
                        char c = index2char(intRow[x] - 1);

                        sb.append(c);
                    }

                    layer[z] = sb.toString();
                }
            }

            CompactBlockList blockList = new CompactBlockList();

            blockList.offsetX = min.x;
            blockList.offsetY = min.y;
            blockList.offsetZ = min.z;
            blockList.structure = out;

            return context.serialize(blockList);
        }

        private static char index2char(int index) {
            if (index == -1) {
                return ' ';
            }

            if (index < 26) {
                return (char) (index + 'A');
            }

            if (index < 26 + 26) {
                return (char) (index - 26 + 'a');
            }

            if (index < 26 + 26 + 10) {
                return (char) (index - 26 - 26 + '0');
            }

            return '?';
        }
    }
}
