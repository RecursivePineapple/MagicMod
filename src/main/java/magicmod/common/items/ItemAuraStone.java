package magicmod.common.items;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;

import com.gtnewhorizon.gtnhlib.util.data.Lazy;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectDoublePair;
import magicmod.MagicMod;
import magicmod.client.rendering.ItemTexture;
import magicmod.common.interfaces.IAuraStorageItem;
import magicmod.common.interfaces.IItemTexture;
import magicmod.common.interfaces.ItemWithTextures;
import magicmod.common.mechanics.AuraBuffer;
import magicmod.common.mechanics.IConcept;
import magicmod.common.util.MCUtils;
import materiallib.api.util.Color;
import materiallib.api.util.ImmutableColor;

public class ItemAuraStone extends Item implements ItemWithTextures, IAuraStorageItem {

    public static final ItemAuraStone INSTANCE = new ItemAuraStone();

    public static IIcon background, foreground;

    public ItemAuraStone() {
        setUnlocalizedName("aura-stone");
    }

    @Override
    public void registerIcons(IIconRegister register) {
        background = register.registerIcon(MagicMod.MODID + ":materials/AuraStoneBackground");
        foreground = register.registerIcon(MagicMod.MODID + ":materials/AuraStoneForeground");
    }

    private static final Lazy<IItemTexture[]> TEXTURES = new Lazy<>(() -> {
        ItemTexture background = new ItemTexture(stack -> ItemAuraStone.background, stack -> Color.WHITE);

        ItemTexture foreground = new ItemTexture(stack -> ItemAuraStone.foreground, stack -> {
            AuraBuffer buffer = ItemAuraStone.getStoredAura(stack);

            List<Pair<IConcept, String>> concepts = new ArrayList<>();

            buffer.forEachConcept((concept, amount) -> {
                concepts.add(Pair.of(concept, concept.toString()));
            });

            concepts.sort(Comparator.comparing(Pair::right));

            return switch (concepts.size()) {
                case 0 -> null;
                case 1 -> concepts.iterator().next().left().getColor();
                default -> {
                    double k = (System.currentTimeMillis() / 2000d);

                    IConcept lower = concepts.get((int) (k % concepts.size())).left();
                    IConcept upper = concepts.get((int) ((k + 1) % concepts.size())).left();

                    yield ImmutableColor.lerp(lower.getColor(), upper.getColor(), (float) (k % 1f));
                }
            };
        });

        return new IItemTexture[] { background, foreground };
    });

    @Override
    public IItemTexture[] getTextures(ItemStack stack) {
        return TEXTURES.get();
    }

    @Override
    public AuraBuffer getBuffer(ItemStack stack) {
        return getStoredAura(stack);
    }

    @Override
    public void setBuffer(ItemStack stack, AuraBuffer buffer) {
        setStoredAura(stack, buffer);
    }

    public static AuraBuffer getStoredAura(ItemStack auraStone) {
        NBTTagCompound tag = auraStone.getTagCompound();

        AuraBuffer buffer = new AuraBuffer();

        if (tag == null) return buffer;

        try {
            buffer.readFromNBT(tag);
        } catch (Throwable t) {
            MagicMod.LOG.error("Invalid NBT tag for Aura Stone: {}", tag, t);
        }

        return buffer;
    }

    public static void setStoredAura(ItemStack auraStone, AuraBuffer buffer) {
        NBTTagCompound tag = MCUtils.getOrCreateTag(auraStone);

        buffer.writeToNBT(tag);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advancedTooltips) {
        super.addInformation(stack, player, tooltip, advancedTooltips);

        AuraBuffer buffer = getStoredAura(stack);

        if (buffer.getTotalAmount() == 0) {
            tooltip.add(MCUtils.translate("magicmod.tooltips.aura-stone.empty"));
        } else {
            tooltip.add(MCUtils.translate("magicmod.tooltips.aura-stone.header"));

            List<ObjectDoublePair<String>> stored = new ArrayList<>();

            buffer.forEachConcept((concept, amount) -> {
                stored.add(ObjectDoublePair.of(concept.toString(), amount));
            });

            stored.sort(Comparator.comparing(Pair::left));

            for (ObjectDoublePair<String> p : stored) {
                tooltip.add(MCUtils.translate("magicmod.tooltips.aura-stone.line", p.left(), p.rightDouble()));
            }
        }
    }
}
