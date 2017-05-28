/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.kostal.handler;

import static org.openhab.binding.kostal.KostalBindingConstants.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.kostal.config.KostalPIKOConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link KostalPIKOHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Michael Wydler - Initial contribution
 */
public class KostalPIKOHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(KostalPIKOHandler.class);

    private KostalPIKOConfig configuration;
    private Map<Byte, String> states;

    public KostalPIKOHandler(Thing thing) {
        super(thing);

        states = new HashMap<Byte, String>();
        states.put((byte) 0, "Off");
        states.put((byte) 1, "Idle");
        states.put((byte) 2, "Starting");
        states.put((byte) 3, "Running-MPP");
        states.put((byte) 4, "Running-Regulated");
        states.put((byte) 5, "Running");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // No commands
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);

        configuration = getConfigAs(KostalPIKOConfig.class);

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
        Socket socket = new Socket(configuration.ip, 81);
        socket.setSoTimeout(5000);

        updateStatus(socket);
        updateCurrent(socket);
        updateDaily(socket);
        updateTotal(socket);

        socket.close();
    }

    private void updateStatus(Socket socket) throws Exception {
        OutputStream outStream = socket.getOutputStream();
        InputStream inStream = socket.getInputStream();

        byte address = (byte) configuration.address;
        byte[] command = buildCommand(COMMAND_STATUS, address);
        outStream.write(command);
        outStream.flush();

        byte[] data = new byte[4096];
        inStream.read(data);
        final ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        byte status = bb.get(5);

        String statusText = states.get(status);
        logger.debug("Status text: {} ", statusText);

        updateState(CHANNEL_STATUS, new StringType(statusText));
    }

    private void updateCurrent(Socket socket) throws Exception {
        OutputStream outStream = socket.getOutputStream();
        InputStream inStream = socket.getInputStream();

        byte address = (byte) configuration.address;
        byte[] command = buildCommand(COMMAND_CURRENT_ENERGY, address);
        outStream.write(command);
        outStream.flush();

        byte[] data = new byte[4096];
        inStream.read(data);
        final ByteBuffer bb4 = ByteBuffer.wrap(data);
        bb4.order(ByteOrder.LITTLE_ENDIAN);
        short dc_1 = bb4.getShort(9);
        short dc_2 = bb4.getShort(19);
        short dc_3 = bb4.getShort(29);
        int dc_total = dc_1 + dc_2 + dc_3;

        short ac_1 = bb4.getShort(39);
        short ac_2 = bb4.getShort(47);
        short ac_3 = bb4.getShort(55);
        int ac_total = ac_1 + ac_2 + ac_3;

        logger.debug("AC power: {} ", ac_total);
        updateState(CHANNEL_AC_POWER, new DecimalType(ac_total));

        double eff = ac_total * 100.0 / dc_total;
        logger.debug("Efficiency: {} ", eff);
        updateState(CHANNEL_EFFICIENCY, new DecimalType(eff));
    }

    private void updateDaily(Socket socket) throws Exception {
        OutputStream outStream = socket.getOutputStream();
        InputStream inStream = socket.getInputStream();

        byte address = (byte) configuration.address;
        byte[] command = buildCommand(COMMAND_DAILY_ENERGY, address);
        outStream.write(command);
        outStream.flush();

        byte[] data = new byte[4096];
        inStream.read(data);
        final ByteBuffer bb = ByteBuffer.wrap(data, 5, 4);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        double value = bb.getInt() / 1000.0;
        logger.debug("Daily energy: {} ", value);
        updateState(CHANNEL_DAILY_ENERGY, new DecimalType(value));
    }

    private void updateTotal(Socket socket) throws Exception {
        OutputStream outStream = socket.getOutputStream();
        InputStream inStream = socket.getInputStream();

        byte address = (byte) configuration.address;
        byte[] command = buildCommand(COMMAND_TOTAL_ENERGY, address);
        outStream.write(command);
        outStream.flush();

        byte[] data = new byte[4096];
        inStream.read(data);
        final ByteBuffer bb = ByteBuffer.wrap(data, 5, 4);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        double value = bb.getInt() / 1000.0;
        logger.debug("Total energy: {} ", value);
        updateState(CHANNEL_TOTAL_ENERGY, new DecimalType(value));
    }

    private static byte[] buildCommand(byte command, byte address) {
        ByteBuffer a = ByteBuffer.allocate(8);
        a.put((byte) 0x62);
        a.put(address);
        a.put((byte) 0x03);
        a.put(address);
        a.put((byte) 0x00);
        a.put(command);

        byte sum = 0;
        for (byte b : a.array()) {
            sum -= b;
        }

        a.put(sum);
        a.put((byte) 0x00);

        return a.array();
    }

}
