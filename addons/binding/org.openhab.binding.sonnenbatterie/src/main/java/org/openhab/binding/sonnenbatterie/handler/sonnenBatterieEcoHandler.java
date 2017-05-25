/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sonnenbatterie.handler;

import static org.openhab.binding.sonnenbatterie.sonnenBatterieBindingConstants.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.sonnenbatterie.config.sonnenBatterieConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link sonnenBatterieEcoHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Michael Wydler - Initial contribution
 */
public class sonnenBatterieEcoHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(sonnenBatterieEcoHandler.class);

    private sonnenBatterieConfiguration configuration;

    private GsonBuilder builder = new GsonBuilder();
    private Gson gson;

    public sonnenBatterieEcoHandler(Thing thing) {
        super(thing);

        logger.debug("Create a sonnenBatterie Handler for thing '{}'", getThing().getUID());

        builder.setDateFormat("yyyy-MM-dd HH:mm:ss");
        gson = builder.create();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Read only
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);

        configuration = getConfigAs(sonnenBatterieConfiguration.class);
        scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    refresh();
                    updateStatus(ThingStatus.ONLINE);
                } catch (Exception e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            e.getClass().getName() + ":" + e.getMessage());
                    logger.debug("Error refreshing source {} ", getThing().getUID(), e);
                }
            }
        }, 0, configuration.refreshInterval, TimeUnit.SECONDS);
    }

    private void refresh() throws Exception {
        String json = readUrl(API_PROTOCOL + "://" + configuration.ip + ":" + API_PORT + "/" + API_VERSION + "status");

        JsonData data = gson.fromJson(json, JsonData.class);
        logger.debug("Data: {} ", gson.toJson(data));

        updateState(CHANNEL_SOC, new DecimalType(new BigDecimal(data.USOC)));
        updateState(CHANNEL_CHARGE, new DecimalType(new BigDecimal(-data.Pac_total_W)));
        updateState(CHANNEL_PRODUCTION, new DecimalType(data.Production_W));
        updateState(CHANNEL_CONSUMPTION, new DecimalType(data.Consumption_W));
        updateState(CHANNEL_GRIDFEEDIN, new DecimalType(data.GridFeedIn_W));
    }

    private static String readUrl(String urlString) throws Exception {
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuffer buffer = new StringBuffer();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1) {
                buffer.append(chars, 0, read);
            }

            return buffer.toString();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    // {
    // "Consumption_W":354,
    // "Fac":50,
    // "GridFeedIn_W":880,
    // "IsSystemInstalled":1,
    // "Pac_total_W":-5,
    // "Production_W":1239,
    // "RSOC":100,
    // "Timestamp":"2017-05-25 14:21:01",
    // "USOC":100,
    // "Uac":234,
    // "Ubat":54
    // }
    static class JsonData {
        int Consumption_W;
        int Fac;
        int GridFeedIn_W;
        int IsSystemInstalled;
        int Pac_total_W;
        int Production_W;
        int RSOC;
        Date Timestamp;
        int USOC;
        int Uac;
        int Ubat;
    }
}
