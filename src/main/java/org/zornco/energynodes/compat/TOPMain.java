package org.zornco.energynodes.compat;

import mcjty.theoneprobe.api.ITheOneProbe;
import org.zornco.energynodes.compat.jei.EnergyControllerProvider;
import org.zornco.energynodes.compat.jei.EnergyNodeProvider;

import java.util.function.Function;

class TOPMain implements Function<Object, Void> {
    static ITheOneProbe PROBE;

    @Override
    public Void apply(Object o) {
        PROBE = (ITheOneProbe) o;
        PROBE.registerProvider(new EnergyControllerProvider());
        PROBE.registerProvider(new EnergyNodeProvider());

        return null;
    }
}
