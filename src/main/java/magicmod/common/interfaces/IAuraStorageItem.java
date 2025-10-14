package magicmod.common.interfaces;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import com.gtnewhorizon.gtnhlib.capability.Capabilities;
import magicmod.common.mechanics.AuraBuffer;

public interface IAuraStorageItem {

    AuraBuffer getBuffer(ItemStack stack);

    void setBuffer(ItemStack stack, AuraBuffer buffer);

    @Nullable
    static AuraBuffer getBufferStatic(ItemStack stack) {
        IAuraStorageItem storageItem = Capabilities.getCapability(stack, IAuraStorageItem.class);

        return storageItem == null ? null : storageItem.getBuffer(stack);
    }

    static void setBufferStatic(ItemStack stack, AuraBuffer buffer) {
        IAuraStorageItem storageItem = Capabilities.getCapability(stack, IAuraStorageItem.class);

        if (storageItem == null) throw new IllegalStateException("Cannot set buffer on an item that does not have a IAuraStorageItem capability");

        storageItem.setBuffer(stack, buffer);
    }
}
