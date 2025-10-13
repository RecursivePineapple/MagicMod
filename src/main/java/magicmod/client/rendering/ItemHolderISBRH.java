package magicmod.client.rendering;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Color;

import com.gtnewhorizon.gtnhlib.blockpos.BlockPos;
import com.gtnewhorizon.gtnhlib.client.model.ModelLoader;
import com.gtnewhorizon.gtnhlib.client.renderer.quad.QuadProvider;
import com.gtnewhorizon.gtnhlib.client.renderer.quad.QuadView;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import magicmod.MagicMod;
import magicmod.common.blocks.BlockItemHolder;
import magicmod.common.tiles.TileEntityRune;
import magicmod.common.util.XSTR;

public class ItemHolderISBRH implements ISimpleBlockRenderingHandler {

    public static final ItemHolderISBRH INSTANCE = new ItemHolderISBRH();

    public final int id;

    private ItemHolderISBRH() {
        id = RenderingRegistry.getNextAvailableRenderId();
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
        Tessellator tessellator = Tessellator.instance;

        QuadProvider model = ModelLoader.getModel(BlockItemHolder.MODEL);

        tessellator.setTranslation(0, 0, 0);

        for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
            List<QuadView> quads = model.getQuads(
                null,
                null,
                block, metadata,
                direction,
                new XSTR(),
                0,
                null);

            for (QuadView quad : quads) {
                for (int i = 0; i < 4; i++) {
                    tessellator.setColorOpaque_I(quad.getColor(i));
                    tessellator.setBrightness(quad.getLight(i));

                    tessellator.addVertexWithUV(quad.getX(i), quad.getY(i), quad.getZ(i), quad.getTexU(i), quad.getTexV(i));
                }
            }
        }

        tessellator.setColorOpaque_I(0xFFFFFF);
        tessellator.setBrightness(15);
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {

        Tessellator tessellator = Tessellator.instance;

        QuadProvider model = ModelLoader.getModel(BlockItemHolder.MODEL);

        tessellator.setTranslation(x, y, z);

        for (ForgeDirection direction : ForgeDirection.values()) {
            if (direction != ForgeDirection.UNKNOWN) {
                if (world.getBlock(x + direction.offsetX, y + direction.offsetY, z + direction.offsetZ).isOpaqueCube()) continue;
            }

            List<QuadView> quads = model.getQuads(
                world,
                new BlockPos(x, y, z),
                block, world.getBlockMetadata(x, y, z),
                direction,
                new XSTR(),
                0,
                null);

            for (QuadView quad : quads) {
                for (int i = 0; i < 4; i++) {
                    tessellator.setColorOpaque_I(quad.getColor(i));
                    tessellator.setBrightness(quad.getLight(i));

                    tessellator.addVertexWithUV(quad.getX(i), quad.getY(i), quad.getZ(i), quad.getTexU(i), quad.getTexV(i));
                }
            }
        }

        tessellator.setColorOpaque_I(0xFFFFFF);
        tessellator.setBrightness(15);
        tessellator.setTranslation(0, 0, 0);

        return true;
    }
}
