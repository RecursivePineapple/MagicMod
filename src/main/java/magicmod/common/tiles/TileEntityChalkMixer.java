package magicmod.common.tiles;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.widget.IParentWidget;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.item.IItemHandlerModifiable;
import com.cleanroommc.modularui.utils.item.InvWrapper;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.gtnewhorizon.gtnhlib.capability.Capabilities;
import magicmod.common.interfaces.IAuraStorageItem;
import magicmod.common.interop.waila.IWailaTile;
import magicmod.common.items.ItemChalk;
import magicmod.common.mechanics.AuraBuffer;
import magicmod.common.mechanics.chalk.ChalkRegistry;
import magicmod.common.mechanics.chalk.IChalkModifier;
import magicmod.common.util.DataUtils;
import magicmod.common.util.MCUtils;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;

public class TileEntityChalkMixer extends TileEntity implements ISidedInventory, IGuiHolder<PosGuiData>, IWailaTile {

    private enum SlotUsage {
        ChalkBase,
        ChalkDopant,
        MixerUnspentFuel,
        MixerActiveFuel,
        MixerSpentFuel,
        MixerOutput;
    }

    private class Slot {
        public final SlotUsage slotUsage;
        public final int index;
        public ItemStack contents;

        public Slot(SlotUsage slotUsage, int index) {
            this.slotUsage = slotUsage;
            this.index = index;
        }

        public boolean tryPut(ItemStack stack) {
            if (contents == null) {
                contents = stack;

                markDirty();
                return true;
            }

            if (MCUtils.areStacksBasicallyEqual(contents, stack)) {
                int total = contents.stackSize + stack.stackSize;

                if (total <= stack.getMaxStackSize()) {
                    contents.stackSize += stack.stackSize;
                }

                markDirty();

                return true;
            }

            return false;
        }
    }

    private final List<Slot> inventory = new ArrayList<>();

    private AuraBuffer remainingMixAura = null;
    private ItemStack pendingChalk = null;

    public TileEntityChalkMixer() {
        int i = 0;

        inventory.add(new Slot(SlotUsage.ChalkBase, i++));

        inventory.add(new Slot(SlotUsage.ChalkDopant, i++));
        inventory.add(new Slot(SlotUsage.ChalkDopant, i++));
        inventory.add(new Slot(SlotUsage.ChalkDopant, i++));
        inventory.add(new Slot(SlotUsage.ChalkDopant, i++));

        inventory.add(new Slot(SlotUsage.MixerUnspentFuel, i++));
        inventory.add(new Slot(SlotUsage.MixerActiveFuel, i++));
        inventory.add(new Slot(SlotUsage.MixerSpentFuel, i++));

        inventory.add(new Slot(SlotUsage.MixerOutput, i));
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int sideIndex) {
        return IntStream.range(0, inventory.size()).toArray();
    }

    @Override
    public boolean canInsertItem(int slotIndex, ItemStack stack, int sideIndex) {
        return isItemValidForSlot(slotIndex, stack);
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

    private Slot getSlot(SlotUsage slotUsage) {
        return DataUtils.find(inventory, slot -> slot.slotUsage == slotUsage);
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        NBTTagList slots = new NBTTagList();
        compound.setTag("inv", slots);

        for (Slot slot : inventory) {
            if (slot.contents == null) continue;

            NBTTagCompound slotTag = slot.contents.writeToNBT(new NBTTagCompound());
            slotTag.setInteger("index", slot.index);

            slots.appendTag(slotTag);
        }

        if (remainingMixAura != null) {
            compound.setTag("remainingMixAura", remainingMixAura.writeToNBT(new NBTTagCompound()));
        }

        if (pendingChalk != null) {
            compound.setTag("pendingChalk", pendingChalk.writeToNBT(new NBTTagCompound()));
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        for (NBTTagCompound slotTag : MCUtils.getTagList(compound, "inv")) {
            inventory.get(slotTag.getInteger("index")).contents = ItemStack.loadItemStackFromNBT(slotTag);
        }

        remainingMixAura = null;
        if (compound.hasKey("remainingMixAura")) {
            remainingMixAura = new AuraBuffer();
            remainingMixAura.readFromNBT(compound.getCompoundTag("remainingMixAura"));
        }

        pendingChalk = null;
        if (compound.hasKey("pendingChalk")) {
            pendingChalk = ItemStack.loadItemStackFromNBT(compound.getCompoundTag("pendingChalk"));
        }
    }

    private void mixChalk() {
        if (remainingMixAura != null || pendingChalk != null) return;

        Slot base = getSlot(SlotUsage.ChalkBase);

        if (base.contents == null || !ChalkRegistry.isChalkBase(base.contents)) return;

        AuraBuffer requiredAura = new AuraBuffer();

        IChalkModifier baseModifier = ChalkRegistry.getChalkModifier(base.contents);

        if (baseModifier != null) {
            requiredAura.addAll(baseModifier.getRequiredMixingAura());
        }

        List<ItemStack> dopants = new ArrayList<>();

        List<Slot> dopantSlots = DataUtils.filterList(inventory, slot -> slot.slotUsage == SlotUsage.ChalkDopant);

        for (Slot slot : dopantSlots) {
            if (slot.contents == null || !ChalkRegistry.isChalkDopant(slot.contents)) continue;

            IChalkModifier dopantModifier = ChalkRegistry.getChalkModifier(slot.contents);

            if (dopantModifier != null) {
                requiredAura.addAll(dopantModifier.getRequiredMixingAura());
            }

            dopants.add(decrStackSize(slot.index, 1));
        }

        this.remainingMixAura = requiredAura;
        this.pendingChalk = ItemChalk.createChalkStack(decrStackSize(base.index, 1), dopants);

        markDirty();
    }

    @Override
    public void getWailaNBTData(EntityPlayerMP player, NBTTagCompound tag) {
        if (remainingMixAura != null) {
            tag.setTag("mixAura", remainingMixAura.writeToNBT(new NBTTagCompound()));
        }
    }

    @Override
    public void getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {

        if (accessor.getNBTData().hasKey("mixAura")) {
            AuraBuffer buffer = new AuraBuffer();
            buffer.readFromNBT(accessor.getNBTData().getCompoundTag("mixAura"));

            currenttip.add("Remaining Required Aura:");

            buffer.forEachConcept((concept, amount) -> {
                currenttip.add(concept + ": " + amount);

                return true;
            });
        }
    }

    @Override
    public void updateEntity() {
        super.updateEntity();

        if (!worldObj.isRemote) {
            Slot unspentFuel = getSlot(SlotUsage.MixerUnspentFuel);
            Slot activeFuel = getSlot(SlotUsage.MixerActiveFuel);
            Slot spentFuel = getSlot(SlotUsage.MixerSpentFuel);
            Slot outputSlot = getSlot(SlotUsage.MixerOutput);

            if (activeFuel.contents == null && unspentFuel.contents != null) {
                activeFuel.contents = decrStackSize(unspentFuel.index, 1);
                markDirty();
            }

            AuraBuffer stored = IAuraStorageItem.getBufferStatic(activeFuel.contents);

            if (activeFuel.contents != null && (stored == null || stored.isEmpty())) {
                if (spentFuel.tryPut(activeFuel.contents)) {
                    activeFuel.contents = null;
                }
            }

            if (remainingMixAura != null && !remainingMixAura.isEmpty() && stored != null) {
                remainingMixAura.toMap().forEach((concept, amount) -> {
                    double extracted = stored.extract(concept, amount, false);

                    remainingMixAura.extract(concept, extracted, false);
                });

                IAuraStorageItem.setBufferStatic(activeFuel.contents, stored);

                if (!remainingMixAura.isEmpty() || stored.isEmpty()) {
                    if (spentFuel.tryPut(activeFuel.contents)) {
                        activeFuel.contents = null;
                    }
                }

                markDirty();
            }

            if (remainingMixAura != null && remainingMixAura.isEmpty()) {
                remainingMixAura = null;
            }

            if (remainingMixAura == null && pendingChalk != null) {
                if (outputSlot.tryPut(pendingChalk)) {
                    pendingChalk = null;
                }
            }
        }
    }

    private int getSlotIndex(SlotUsage slotUsage) {
        return DataUtils.indexOf(inventory, slot -> slot.slotUsage == slotUsage);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return widget(null, new ModularPanel("ChalkMixer"), panel -> {
            panel.size(178, 200);
            panel.padding(4);

            widget(panel, new Column(), col -> {
                col.sizeRel(1);

                widget(col, new TextWidget(getBlockType().getLocalizedName()), title -> {
                    title.height(16);
                    title.alignX(0.5f);
                    title.alignment(Alignment.TopCenter);
                });

                IItemHandlerModifiable inv = new InvWrapper(this);

                widget(col, new Row(), chalkSlots -> {
                    chalkSlots.widthRel(1);
                    chalkSlots.alignX(0.5f);
                    chalkSlots.expanded();

                    syncManager.registerSlotGroup("base", 1, 150);
                    syncManager.registerSlotGroup("dopants", 4, 125);
                    syncManager.registerSlotGroup("fuel_input", 1, 110);
                    syncManager.registerSlotGroup("fuel_active", 1, false);
                    syncManager.registerSlotGroup("fuel_spent", 1, false);
                    syncManager.registerSlotGroup("output", 1, false);

                    widget(chalkSlots, new ChalkModifierTooltipSlot(), slot -> {
                        slot.left(16);
                        slot.alignY(0.5f);
                        slot.slot(new ModularSlot(inv, getSlotIndex(SlotUsage.ChalkBase)).slotGroup("base"));
                    });

                    List<Slot> dopantSlots = DataUtils.filterList(inventory, slot -> slot.slotUsage == SlotUsage.ChalkDopant);

                    widget(chalkSlots, new ChalkModifierTooltipSlot(), slot -> {
                        slot.top(8);
                        slot.leftRel(0.5f, -27, 0.5f);
                        slot.slot(new ModularSlot(inv, dopantSlots.get(0).index).slotGroup("dopants"));
                    });

                    widget(chalkSlots, new ChalkModifierTooltipSlot(), slot -> {
                        slot.top(8);
                        slot.leftRel(0.5f, -9, 0.5f);
                        slot.slot(new ModularSlot(inv, dopantSlots.get(1).index).slotGroup("dopants"));
                    });

                    widget(chalkSlots, new ChalkModifierTooltipSlot(), slot -> {
                        slot.top(8);
                        slot.leftRel(0.5f, 9, 0.5f);
                        slot.slot(new ModularSlot(inv, dopantSlots.get(2).index).slotGroup("dopants"));
                    });

                    widget(chalkSlots, new ChalkModifierTooltipSlot(), slot -> {
                        slot.top(8);
                        slot.leftRel(0.5f, 27, 0.5f);
                        slot.slot(new ModularSlot(inv, dopantSlots.get(3).index).slotGroup("dopants"));
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

                    widget(chalkSlots, new ButtonWidget(), button -> {
                        syncManager.registerSyncedAction("mix", packet -> {
                            if (!NetworkUtils.isClient()) {
                                mixChalk();
                            }
                        });

                        button.alignX(0.5f);
                        button.alignY(0.5f);
                        button.child(new TextWidget("Mix"));
                        button.onMousePressed((mouseButton) -> {
                            syncManager.callSyncedAction("mix", buffer -> {});

                            return true;
                        });
                    });
                });

                widget(col, new Row(), row -> {
                    row.widthRel(1);
                    row.height(76);
                    row.alignX(0.5f);
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

    private static class ChalkModifierTooltipSlot extends ItemSlot {

        @Override
        public void buildTooltip(ItemStack stack, RichTooltip tooltip) {
            super.buildTooltip(stack, tooltip);

            IChalkModifier baseModifier = ChalkRegistry.getChalkModifier(stack);

            if (baseModifier != null) {
                AuraBuffer requirement = baseModifier.getRequiredMixingAura();

                if (requirement != null && !requirement.isEmpty()) {
                    tooltip.addLine("Required Aura:");

                    requirement.forEachConcept((concept, amount) -> {
                        tooltip.addLine(concept.toString() + ": " + amount);
                    });
                }
            }
        }
    }
}
