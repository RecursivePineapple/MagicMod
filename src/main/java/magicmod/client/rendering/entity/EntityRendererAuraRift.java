package magicmod.client.rendering.entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderEntity;
import net.minecraft.entity.Entity;

import org.lwjgl.opengl.GL11;

import com.gtnewhorizon.gtnhlib.client.renderer.TessellatorManager;
import com.gtnewhorizon.gtnhlib.client.renderer.vbo.VertexBuffer;
import com.gtnewhorizon.gtnhlib.client.renderer.vertex.DefaultVertexFormat;
import it.unimi.dsi.fastutil.Pair;
import magicmod.common.mechanics.IConcept;
import magicmod.common.mechanics.rifts.EntityAuraRift;
import materiallib.api.util.Color;
import materiallib.api.util.ImmutableColor;

public class EntityRendererAuraRift extends RenderEntity {

    public static final EntityRendererAuraRift INSTANCE = new EntityRendererAuraRift();

    private VertexBuffer core;

    private EntityRendererAuraRift() { }

    private void initCoreVBO() {
        TessellatorManager.startCapturing();

        Tessellator tessellator = TessellatorManager.get();

        tessellator.startDrawingQuads();

        tessellator.addVertex(-0.5, 0.5, -0.5);
        tessellator.addVertex(-0.5, 0.5, 0.5);
        tessellator.addVertex(0.5, 0.5, 0.5);
        tessellator.addVertex(0.5, 0.5, -0.5);

        tessellator.addVertex(-0.5, -0.5, -0.5);
        tessellator.addVertex(0.5, -0.5, -0.5);
        tessellator.addVertex(0.5, -0.5, 0.5);
        tessellator.addVertex(-0.5, -0.5, 0.5);

        tessellator.addVertex(0.5, -0.5, -0.5);
        tessellator.addVertex(-0.5, -0.5, -0.5);
        tessellator.addVertex(-0.5, 0.5, -0.5);
        tessellator.addVertex(0.5, 0.5, -0.5);

        tessellator.addVertex(0.5, -0.5, 0.5);
        tessellator.addVertex(0.5, 0.5, 0.5);
        tessellator.addVertex(-0.5, 0.5, 0.5);
        tessellator.addVertex(-0.5, -0.5, 0.5);

        tessellator.addVertex(-0.5, -0.5, 0.5);
        tessellator.addVertex(-0.5, 0.5, 0.5);
        tessellator.addVertex(-0.5, 0.5, -0.5);
        tessellator.addVertex(-0.5, -0.5, -0.5);

        tessellator.addVertex(0.5, -0.5, 0.5);
        tessellator.addVertex(0.5, -0.5, -0.5);
        tessellator.addVertex(0.5, 0.5, -0.5);
        tessellator.addVertex(0.5, 0.5, 0.5);

        tessellator.draw();

        core = TessellatorManager.stopCapturingToVBO(DefaultVertexFormat.POSITION);
    }

    @Override
    public void doRender(Entity entity, double posX, double posY, double posZ, float yaw, float partialTicks) {
        EntityAuraRift rift = (EntityAuraRift) entity;

        if (core == null) {
            initCoreVBO();
        }

        GL11.glPushMatrix();
        GL11.glTranslatef((float)posX, (float)posY, (float)posZ);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        Minecraft.getMinecraft().entityRenderer.disableLightmap(0);

        List<Pair<IConcept, String>> concepts = new ArrayList<>();

        rift.getProduction().forEachConcept((concept, amount) -> {
            concepts.add(Pair.of(concept, concept.toString()));
        });

        concepts.sort(Comparator.comparing(Pair::right));

        ImmutableColor color = switch (concepts.size()) {
            case 0 -> Color.WHITE;
            case 1 -> concepts.iterator().next().left().getColor();
            default -> {
                double k = (System.currentTimeMillis() / 2000d);

                IConcept lower = concepts.get((int) (k % concepts.size())).left();
                IConcept upper = concepts.get((int) ((k + 1) % concepts.size())).left();

                yield ImmutableColor.lerp(lower.getColor(), upper.getColor(), (float) (k % 1f));
            }
        };

        color.makeActive();

        int counter = (int) (System.currentTimeMillis() % 1_000_000);

        double t = counter / 2000d;

        for (int n = 0; n < 3; n++, t += 5) {
            for (int i = 0; i < 10; i++) {
                GL11.glPushMatrix();

                double theta = t + i * 0.1;

                double radius = Math.cos(theta * 0.05) * 0.2 + 0.3;

                GL11.glTranslated(Math.cos(theta * 5) * radius, (Math.cos(theta) * 0.5 + 0.5) * 4 + 0.5, Math.sin(theta * 5) * radius);
                GL11.glRotated(theta, 1, 0, 0);
                GL11.glRotated(theta * 0.2, 0, 1, 0);
                GL11.glRotated(theta * 0.6, 0, 0, 1);

                GL11.glScaled(0.25, 0.25, 0.25);

                core.render();

                GL11.glPopMatrix();
            }
        }

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
    }
}
