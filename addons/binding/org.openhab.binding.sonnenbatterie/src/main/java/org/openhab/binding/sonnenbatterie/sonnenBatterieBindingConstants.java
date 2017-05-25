/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sonnenbatterie;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link sonnenBatterieBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Michael Wydler - Initial contribution
 */
public class sonnenBatterieBindingConstants {

    private static final String BINDING_ID = "sonnenbatterie";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ECO = new ThingTypeUID(BINDING_ID, "eco");

    // List of all Channel ids
    public static final String CHANNEL_SOC = "soc";
    public static final String CHANNEL_CHARGE = "charge";
    public static final String CHANNEL_PRODUCTION = "production";
    public static final String CHANNEL_CONSUMPTION = "consumption";
    public static final String CHANNEL_GRIDFEEDIN = "gridfeedin";

    public static final String API_PROTOCOL = "http";
    public static final int API_PORT = 8080;
    public static final String API_VERSION = "api/v1/";

}
