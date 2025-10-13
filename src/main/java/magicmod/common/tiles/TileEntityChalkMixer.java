package magicmod.common.tiles;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.widget.IParentWidget;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.item.IItemHandlerModifiable;
import com.cleanroommc.modularui.utils.item.InvWrapper;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.gtnewhorizon.gtnhlib.capability.Capabilities;
import magicmod.common.interfaces.IAuraStorageItem;
import magicmod.common.mechanics.chalk.ChalkRegistry;
import magicmod.common.util.DataUtils;
import magicmod.common.util.MCUtils;

public class TileEntityChalkMixer extends TileEntity implements ISidedInventory, IGuiHolder<PosGuiData> {

    private enum SlotUsage {
        ChalkBase,
        ChalkDopant,
        MixerUnspentFuel,
        MixerActiveFuel,
        MixerSpentFuel,
        MixerOutput;
    }

    private static class Slot {
        public final SlotUsage slotUsage;
        public ItemStack contents;

        public Slot(SlotUsage slotUsage) {
            this.slotUsage = slotUsage;
        }
    }

    private final List<Slot> inventory = new ArrayList<>();

    public TileEntityChalkMixer() {
        inventory.add(new Slot(SlotUsage.ChalkBase));

        inventory.add(new Slot(SlotUsage.ChalkDopant));
        inventory.add(new Slot(SlotUsage.ChalkDopant));
        inventory.add(new Slot(SlotUsage.ChalkDopant));
        inventory.add(new Slot(SlotUsage.ChalkDopant));

        inventory.add(new Slot(SlotUsage.MixerUnspentFuel));
        inventory.add(new Slot(SlotUsage.MixerActiveFuel));
        inventory.add(new Slot(SlotUsage.MixerSpentFuel));

        inventory.add(new Slot(SlotUsage.MixerOutput));
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int sideIndex) {
        return IntStream.range(0, inventory.size()).toArray();
    }

    @Override
    public boolean canInsertItem(int slotIndex, ItemStack stack, int sideIndex) {
        Slot slot = DataUtils.getIndexSafe(inventory, slotIndex);

        if (slot == null) return false;
        if (slot.contents != null && !MCUtils.areStacksBasicallyEqual(slot.contents, stack)) return false;

        return switch (slot.slotUsage) {
            case ChalkBase -> ChalkRegistry.isChalkBase(stack);
            case ChalkDopant -> {
                for (int i = 0; i < inventory.size(); i++) {
                    Slot slot2 = inventory.get(i);

                    if (slot2.slotUsage == SlotUsage.ChalkDopant && MCUtils.areStacksBasicallyEqual(slot2.contents, stack)) {
                        yield slotIndex == i;
                    }
                }

                yield ChalkRegistry.isChalkDopant(stack);
            }
            case MixerUnspentFuel -> stack != null && Capabilities.getCapability(stack, IAuraStorageItem.class) != null;
            case MixerActiveFuel, MixerSpentFuel, MixerOutput -> false;
        };
    }

    @Override
    public boolean canExtractItem(int slotIndex, ItemStack stack, int sideIndex) {
        Slot slot = DataUtils.getIndexSafe(inventory, slotIndex);

        if (slot == null || !MCUtils.areStacksBasicallyEqual(slot.contents, stack)) return false;

        return slot.slotUsage == SlotUsage.MixerSpentFuel || slot.slotUsage == SlotUsage.MixerOutput;
    }

    @Override
    public boolean isItemValidForSlot(int slotIndex, ItemStack stack) {
        Slot slot = DataUtils.getIndexSafe(inventory, slotIndex);

        if (slot == null) return false;
        if (slot.contents != null && !MCUtils.areStacksBasicallyEqual(slot.contents, stack)) return false;

        return switch (slot.slotUsage) {
            case ChalkBase -> ChalkRegistry.isChalkBase(stack);
            case ChalkDopant -> ChalkRegistry.isChalkDopant(stack);
            case MixerUnspentFuel -> stack != null && Capabilities.getCapability(stack, IAuraStorageItem.class) != null;
            case MixerActiveFuel, MixerSpentFuel, MixerOutput -> false;
        };
    }

    @Override
    public int getSizeInventory() {
        return inventory.size();
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        Slot slot = DataUtils.getIndexSafe(inventory, slotIndex);
        return slot == null ? null : slot.contents;
    }

    @Override
    public ItemStack decrStackSize(int slotIndex, int count) {
        Slot slot = DataUtils.getIndexSafe(inventory, slotIndex);

        if (slot == null || slot.contents == null) return null;

        ItemStack result = slot.contents.splitStack(Math.min(slot.contents.stackSize, count));

        if (slot.contents.stackSize == 0) slot.contents = null;

        markDirty();

        return result;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slotIndex) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int slotIndex, ItemStack stack) {
        Slot slot = DataUtils.getIndexSafe(inventory, slotIndex);

        if (slot != null) slot.contents = stack;
    }

    @Override
    public String getInventoryName() {
        return getBlockType().getLocalizedName();
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory() {

    }

    @Override
    public void closeInventory() {

    }

    private int getSlotIndex(SlotUsage slotUsage) {
        return DataUtils.indexOf(inventory, slot -> slot.slotUsage == slotUsage);
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return widget(null, new ModularPanel("ChalkMixer"), panel -> {
            panel.size(178, 200);
            panel.padding(4);

            widget(panel, new Column(), col -> {
                col.sizeRel(1);

                widget(col, new TextWidget(getBlockType().getUnlocalizedName()), title -> {
                    title.height(16);
                    title.alignX(0.5f);
                    title.alignment(Alignment.TopCenter);
                });

                IItemHandlerModifiable inv = new InvWrapper(this);

                widget(col, new Row(), chalkSlots -> {
                    chalkSlots.widthRel(1);
                    chalkSlots.alignX(0.5f);
                    chalkSlots.height(100);

                    syncManager.registerSlotGroup("base", 1, 150);
                    syncManager.registerSlotGroup("dopants", 4, 125);
                    syncManager.registerSlotGroup("fuel_input", 1, 110);
                    syncManager.registerSlotGroup("fuel_active", 1, false);
                    syncManager.registerSlotGroup("fuel_spent", 1, false);
                    syncManager.registerSlotGroup("output", 1, false);

                    widget(chalkSlots, new ItemSlot(), slot -> {
                        slot.left(16);
                        slot.alignY(0.5f);
                        slot.slot(new ModularSlot(inv, getSlotIndex(SlotUsage.ChalkBase)).slotGroup("base"));
                    });

                    int[] dopantSlots = new int[4];
                    int x = 0;

                    for (int i = 0; i < inventory.size(); i++) {
                        if (inventory.get(i).slotUsage == SlotUsage.ChalkDopant) {
                            dopantSlots[x++] = i;
                        }
                    }

                    widget(chalkSlots, new ItemSlot(), slot -> {
                        slot.top(8);
                        slot.leftRel(0.5f, -27, 0.5f);
                        slot.slot(new ModularSlot(inv, dopantSlots[0]).slotGroup("dopants"));
                    });

                    widget(chalkSlots, new ItemSlot(), slot -> {
                        slot.top(8);
                        slot.leftRel(0.5f, -9, 0.5f);
                        slot.slot(new ModularSlot(inv, dopantSlots[1]).slotGroup("dopants"));
                    });

                    widget(chalkSlots, new ItemSlot(), slot -> {
                        slot.top(8);
                        slot.leftRel(0.5f, 9, 0.5f);
                        slot.slot(new ModularSlot(inv, dopantSlots[2]).slotGroup("dopants"));
                    });

                    widget(chalkSlots, new ItemSlot(), slot -> {
                        slot.top(8);
                        slot.leftRel(0.5f, 27, 0.5f);
                        slot.slot(new ModularSlot(inv, dopantSlots[3]).slotGroup("dopants"));
                    });

                    widget(chalkSlots, new ItemSlot(), slot -> {
                        slot.bottom(8);
                        slot.leftRel(0.5f, -20, 0.5f);
                        slot.slot(new ModularSlot(inv, getSlotIndex(SlotUsage.MixerUnspentFuel)).slotGroup("fuel_input"));
                    });

                    widget(chalkSlots, new ItemSlot(), slot -> {
                        slot.bottom(8);
                        slot.leftRel(0.5f, 0, 0.5f);
                        slot.slot(new ModularSlot(inv, getSlotIndex(SlotUsage.MixerActiveFuel)).slotGroup("fuel_active"));
                    });

                    widget(chalkSlots, new ItemSlot(), slot -> {
                        slot.bottom(8);
                        slot.leftRel(0.5f, 20, 0.5f);
                        slot.slot(new ModularSlot(inv, getSlotIndex(SlotUsage.MixerSpentFuel)).slotGroup("fuel_spent"));
                    });

                    widget(chalkSlots, new ItemSlot(), slot -> {
                        slot.right(16);
                        slot.alignY(0.5f);
                        slot.slot(new ModularSlot(inv, getSlotIndex(SlotUsage.MixerOutput)).slotGroup("output"));
                    });
                });

                widget(col, new Row(), row -> {
                    row.widthRel(1);
                    row.height(76);
                    row.alignX(0.5f);
                    row.anchorBottom(0);
                    row.child(SlotGroupWidget.playerInventory(false).marginLeft(4));
                });
            });
        });
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private <I extends IWidget, W extends IParentWidget> I widget(W parent, I t, Consumer<I> configure) {
        configure.accept(t);
        if (parent != null) parent.child(t);
        return t;
    }
}
