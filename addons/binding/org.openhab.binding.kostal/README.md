# Kostal Binding

This is the binding for [Kostal Solar Electric](http://www.kostal-solar-electric.com/).
This binding allows you to integrate the PIKO inverters in the openHAB environment

## Supported Things

At the moment only PIKO inverters are supported.

Tested devices:
- PIKO 3.0
- PIKO 5.5

## Thing Configuration

The IP and RS485 address of the inverter needs to be configured.

```
Thing kostal:piko:e96edc10 [ ip="192.168.0.43", address=255 ]
```

## Channels

| Channel Type ID | Item Type    | Description |
|-----------------|--------------|-------------|
| status | String | This channel indicates the status of the inverter. |
| ac_power | Number | This channel indicates the current power, in W. |
| efficiency | Number | This channel indicates the efficiency, in %. |
| daily_energy | Number | This channel indicates the daily generated energy, in kWh. |
| total_energy | Number | This channel indicates the total generated energy, in kWh. |

## Full Example

demo.things:

```
Thing kostal:piko:demo [ ip="192.168.0.43", address=255, refreshInterval=30 ]
```

demo.items:

```
String status "Status" { channel="kostal:piko:demo:status" }
Number power "Power [%d W]" { channel="kostal:piko:demo:ac_power" }
Number efficiency "Power [%.1f %%]" { channel="kostal:piko:demo:efficiency" }
Number daily_energy "Daily Energy [%.2f kWh]" { channel="kostal:piko:demo:daily_energy" }
Number total_energy "Total Energy [%d kWh]" { channel="kostal:piko:demo:total_energy" }
```
