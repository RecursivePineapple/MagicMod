package magicmod.common.runes.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import magicmod.common.mechanics.AuraBuffer;
import magicmod.common.mechanics.AuraStack;
import magicmod.common.mechanics.IAuraBuffer;
import magicmod.common.mechanics.IConcept;
import magicmod.common.mechanics.MergedAuraBuffer;
import magicmod.common.mechanics.aura.AuraFieldRegistry;
import magicmod.common.mechanics.aura.IAuraField;
import magicmod.common.tiles.TileEntityRune;
import magicmod.common.util.DataUtils;

public class ExtractRune extends TileEntityRune {

    public enum ExtractionTarget {
        AuraField,
        //
        ;
    }

    @Override
    protected void doRuneTick() {
        Collection<ExtractionTarget> targets = arrayNetwork.getComponents(ExtractionTarget.class);

        if (targets.size() != 1) return;

        ExtractionTarget target = targets.iterator().next();

        switch (target) {
            case AuraField -> {
                Collection<IConcept> filters = arrayNetwork.getComponents(IConcept.class);

                List<IAuraBuffer> validFields = new ArrayList<>();

                for (IAuraField field : AuraFieldRegistry.getFields(worldObj)) {
                    for (IConcept filter : filters) {
                        if (!field.contains(filter)) continue;

                        validFields.add(field.getBuffer(xCoord, yCoord, zCoord, 1));
                    }
                }

                if (validFields.isEmpty()) return;

                MergedAuraBuffer mergedFields = new MergedAuraBuffer(validFields);

                double pressure = 50;

                double stored = getEffectiveAuraBuffer().stream().mapToDouble(AuraStack::getAmount).sum();

                double toTransfer = DataUtils.clamp(pressure - stored, 0, 10);

                int cost = filters.size() + arrayNetwork.elements.size();
                double efficiency = 20d / (cost + 19d);

                mergedFields.transferAmount(getEffectiveAuraBuffer(), toTransfer, efficiency);
            }
        }
    }
}
