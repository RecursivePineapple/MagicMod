package magicmod.common.worldgen.structure.core;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;

import org.joml.Vector3d;
import org.joml.Vector3i;

import lombok.Setter;
import lombok.SneakyThrows;
import magicmod.common.util.VoxelAABB;

public class StructurePieceCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "structure-piece";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/structure-piece [scan|place] (player name) [name|path] [x] [y] [z] (for scan: [origin x] [origin y] [origin z])";
    }

    private EntityPlayerMP generatingPlayer;
    private Vector3i pos1;

    @SneakyThrows
    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length < 5) {
            throw new WrongUsageException(this.getCommandUsage(sender));
        }

        ArgumentParser parser = new ArgumentParser(args, sender);

        switch (parser.nextString().toLowerCase()) {
            case "scan" -> {
                EntityPlayerMP player = parser.getPlayerIfNeeded();

                if (generatingPlayer != null && generatingPlayer != player) {
                    throw new WrongUsageException("Another player is already scanning a structure");
                }

                String pieceName = parser.nextString();

                Vector3i pos = parser.nextBlockPos();

                if (pos1 != null) {
                    VoxelAABB aabb = new VoxelAABB(pos1, pos);

                    if (parser.hasNext()) {
                        aabb.origin = parser.nextBlockPos();
                    }

                    StructurePiece piece = StructurePiece.fromWorld(player.worldObj, aabb);

                    generatingPlayer = null;
                    pos1 = null;

                    byte[] data;

                    if (pieceName.endsWith("nbt")) {
                        data = CompressedStreamTools.compress(StructurePiece.saveNBT(piece));
                    } else {
                        data = StructurePiece.GSON.toJson(StructurePiece.saveJson(piece)).getBytes(StandardCharsets.UTF_8);
                    }

                    Path outfile = Paths.get("structure-pieces", pieceName).toAbsolutePath();

                    if (!Files.exists(outfile.getParent())) {
                        Files.createDirectories(outfile.getParent());
                    }

                    Files.write(outfile, data);

                    sender.addChatMessage(new ChatComponentText("Generated " + outfile));
                } else {
                    generatingPlayer = player;
                    pos1 = pos;

                    sender.addChatMessage(new ChatComponentText("Re-run /structure-piece scan with the second coordinate to complete the piece"));
                }
            }
            case "place" -> {
                EntityPlayerMP player = parser.getPlayerIfNeeded();

                String pieceName = parser.nextString();

                Vector3i pos = parser.nextBlockPos();

                StructurePiece piece = StructurePiece.load(pieceName);

                piece.place(player.worldObj, pos, null);
            }
            default -> {
                throw new WrongUsageException(this.getCommandUsage(sender));
            }
        }
    }

    private static int parseLocation(ICommandSender sender, double k, String args) {
        return MathHelper.floor_double(func_110666_a(sender, k, args));
    }

    private final class ArgumentParser {
        private int index;
        private final String[] args;
        private final ICommandSender sender;
        @Setter
        private EntityPlayerMP player;

        public ArgumentParser(String[] args, ICommandSender sender) {
            this.args = args;
            this.sender = sender;
            this.player = sender instanceof EntityPlayerMP p ? p : null;
        }

        public boolean hasNext() {
            return index < args.length;
        }

        public String nextString() {
            if (!hasNext()) {
                throw new WrongUsageException(getCommandUsage(sender));
            }

            return args[index++];
        }

        public int nextInteger() {
            return parseInt(sender, nextString());
        }

        public int nextInteger(int min) {
            return parseIntWithMin(sender, nextString(), min);
        }

        public int nextInteger(int min, int max) {
            return parseIntBounded(sender, nextString(), min, max);
        }

        public double nextDouble() {
            return parseDouble(sender, nextString());
        }

        public double nextDouble(double min) {
            return parseDoubleWithMin(sender, nextString(), min);
        }

        public double nextDouble(double min, double max) {
            return parseDoubleBounded(sender, nextString(), min, max);
        }

        public boolean nextBoolean() {
            return parseBoolean(sender, nextString());
        }

        public double nextX() {
            return player == null ? nextDouble() : func_110665_a(sender, player.posX, nextString(), Integer.MIN_VALUE, Integer.MAX_VALUE);
        }

        public double nextY() {
            return player == null ? nextDouble() : func_110665_a(sender, player.posY, nextString(), Integer.MIN_VALUE, Integer.MAX_VALUE);
        }

        public double nextZ() {
            return player == null ? nextDouble() : func_110665_a(sender, player.posZ, nextString(), Integer.MIN_VALUE, Integer.MAX_VALUE);
        }

        public Vector3d nextPos() {
            return new Vector3d(nextX(), nextY(), nextZ());
        }

        public Vector3i nextBlockPos() {
            return new Vector3i(MathHelper.floor_double(nextX()), MathHelper.floor_double(nextY()), MathHelper.floor_double(nextZ()));
        }

        public EntityPlayerMP nextPlayer() {
            return getPlayer(sender, nextString());
        }

        public EntityPlayerMP getPlayerIfNeeded() {
            if (this.player == null) {
                this.player = nextPlayer();
            }

            return this.player;
        }
    }
}
