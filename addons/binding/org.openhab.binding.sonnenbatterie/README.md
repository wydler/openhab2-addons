# sonnenBatterie Binding

This binding integrates the [sonnenBatterie](https://www.sonnenbatterie.de).

## Supported Things

sonnenBatterie eco

## Binding Configuration

The binding uses the IP address in order to access the sonnenBatterie status API.

## Thing Configuration

The sonnenBatterie Thing requires the IP address as a configuration value in order for the binding to access the status API page.
In the thing file, this looks e.g. like

```
Thing sonnenbatterie:eco:home [ ip=192.168.0.100 ]
```

## Channels

All devices support the following channels (non exhaustive):

| Channel Type ID | Item Type    | Description                                                  |
|-----------------|--------------|--------------------------------------------------------------|
| charge          | Number       | This channel indicates the actual state of the battery, in % |
| production      | Number       | This channel indicates the actual production, in W           |
| consumption     | Number       | This channel indicates the actual consumption, in W          |
| gridfeedin      | Number       | This channel indicates the actual grid feed-in, in W         |

## Full Example

sonnenbatterie.things:

```
Thing sonnenbatterie:eco:home [ ip = "192.168.0.100", refreshInterval = 30 ]
```
