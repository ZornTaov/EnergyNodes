package org.zornco.energynodes.tiers;

import org.zornco.energynodes.EnergyNodeConstants;

import java.util.HashMap;

public class ControllerTiers {

    public static final IControllerTier BASE =      new ControllerTier(2000, 2, 16);
    public static final IControllerTier ADVANCED =  new ControllerTier(200000, 4, 32);
    public static final IControllerTier EXPERT =    new ControllerTier(20000000, 8, 64);
    public static final IControllerTier MAX =       new ControllerTier(EnergyNodeConstants.UNLIMITED_RATE, 16, 128);

    public static final HashMap<String, IControllerTier> CONTROLLER_TIERS = new HashMap<String, IControllerTier>() {{
        put(BASE.getSerializedName(), BASE);
        put(ADVANCED.getSerializedName(), ADVANCED);
        put(EXPERT.getSerializedName(), EXPERT);
        put(MAX.getSerializedName(), MAX);
    }};
}
