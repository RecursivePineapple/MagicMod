package magicmod.client.rendering;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.util.ForgeDirection;

import org.joml.Matrix4d;
import org.joml.Matrix4dc;
import org.joml.Vector4d;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;

import com.gtnewhorizon.gtnhlib.eventbus.EventBusSubscriber;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import magicmod.MagicMod;
import magicmod.common.tiles.TileEntityRune;

@EventBusSubscriber
public class RuneISBRH implements ISimpleBlockRenderingHandler {

    public static final RuneISBRH INSTANCE = new RuneISBRH();

    public final int id;

    private static IIcon strongConnection;

    private RuneISBRH() {
        id = RenderingRegistry.getNextAvailableRenderId();
    }

    @SubscribeEvent
    public static void onRegisterTextures(TextureStitchEvent.Pre event) {
        if (event.map.getTextureType() == 0) {
            strongConnection = event.map.registerIcon(MagicMod.MODID + ":misc/StrongConnection");
        }
    }

    @Override
    public int getRenderId() {
        return id;
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        return true;
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        GL11.glRotatef(90f, 0.0F, 1.0F, 0.0F);
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);

        Tessellator tessellator = Tessellator.instance;

        tessellator.startDrawingQuads();

        IIcon icon = block.getIcon(0, metadata);

        renderQuad(0, 0, 0, icon, Color.WHITE, IDENTITY);

        tessellator.draw();

        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
        IIcon icon = block.getIcon(0, world.getBlockMetadata(x, y, z));

        TileEntityRune rune = (TileEntityRune) world.getTileEntity(x, y, z);

        for (ForgeDirection strong : rune.strongConnections) {
            renderQuad(x, y, z, strongConnection, Color.RED, switch (strong) {
                case SOUTH -> ROT_SOUTH;
                case WEST -> ROT_WEST;
                case EAST -> ROT_EAST;
                default -> IDENTITY;
            });
        }

        for (ForgeDirection weak : rune.weakConnections) {
            renderQuad(x, y + 0.01, z, strongConnection, Color.BLUE, switch (weak) {
                case SOUTH -> ROT_SOUTH;
                case WEST -> ROT_WEST;
                case EAST -> ROT_EAST;
                default -> IDENTITY;
            });
        }

        renderQuad(x, y + 0.02, z, icon, Color.WHITE, IDENTITY);

        return true;
    }

    private void renderQuad(double x, double y, double z, IIcon base, ReadableColor color, Matrix4dc transform) {
        Tessellator tessellator = Tessellator.instance;

        tessellator.setColorOpaque(color.getRed(), color.getGreen(), color.getBlue());

        Vector4d a = transform.transform(new Vector4d(0.5, -0.49, 0.5, 1));
        Vector4d b = transform.transform(new Vector4d(0.5, -0.49, -0.5, 1));
        Vector4d c = transform.transform(new Vector4d(-0.5, -0.49, -0.5, 1));
        Vector4d d = transform.transform(new Vector4d(-0.5, -0.49, 0.5, 1));

        x += 0.5;
        y += 0.5;
        z += 0.5;

        tessellator.addVertexWithUV(x + a.x, y + a.y, z + a.z, base.getMaxU(), base.getMaxV());
        tessellator.addVertexWithUV(x + b.x, y + b.y, z + b.z, base.getMaxU(), base.getMinV());
        tessellator.addVertexWithUV(x + c.x, y + c.y, z + c.z, base.getMinU(), base.getMinV());
        tessellator.addVertexWithUV(x + d.x, y + d.y, z + d.z, base.getMinU(), base.getMaxV());
    }

    private static final Matrix4dc IDENTITY = new Matrix4d();
    private static final Matrix4dc ROT_WEST = new Matrix4d().rotate(Math.PI / 2, 0, 1, 0);
    private static final Matrix4dc ROT_SOUTH = new Matrix4d().rotate(Math.PI, 0, 1, 0);
    private static final Matrix4dc ROT_EAST = new Matrix4d().rotate(-Math.PI/2, 0, 1, 0);
}
