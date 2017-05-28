/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.kostal;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link KostalBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Michael Wydler - Initial contribution
 */
public class KostalBindingConstants {

    private static final String BINDING_ID = "kostal";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_PIKO = new ThingTypeUID(BINDING_ID, "piko");

    // List of all Channel ids
    public static final String CHANNEL_STATUS = "status";
    public static final String CHANNEL_AC_POWER = "ac_power";
    public static final String CHANNEL_EFFICIENCY = "efficiency";
    public static final String CHANNEL_DAILY_ENERGY = "daily_energy";
    public static final String CHANNEL_TOTAL_ENERGY = "total_energy";

    public static final byte COMMAND_STATUS = 0x57;
    public static final byte COMMAND_CURRENT_ENERGY = 0x43;
    public static final byte COMMAND_DAILY_ENERGY = (byte) 0x9d;
    public static final byte COMMAND_TOTAL_ENERGY = 0x45;

}
