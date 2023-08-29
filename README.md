# Call Monitor

Simple Android application for serving data about phone calls via REST API in a local network.

## Features

* REST API server:
    * Metadata about server start time and available endpoints.
    * Information about current phone call.
    * Information about previous phone calls from the launch of the app.
* Foreground service:
    * Keeps API server available.
    * Displays ongoing notification.
* Graphical user interface:
    * Displays local IP address and port for server.
    * Displays call log history.
    * Allows to start/stop of API service.

### NOTE

Handling more than one call at the same time is not supported due to limitations
of `android.intent.action.PHONE_STATE` broadcast receiver, which does not provide information about
second ongoing call end or switching between two ongoing calls.

## API

### GET `/`

Provides metadata about server start time and available endpoints.

#### Example response:

```json
{
  "start": "2023-08-28T18:18:49+02:00",
  "services": [
    {
      "name": "status",
      "uri": "http://0.0.0.0:12345/status"
    },
    {
      "name": "log",
      "uri": "http://0.0.0.0:12345/log"
    }
  ]
}
```

### GET `/status`

Provides information about current phone call. Additionally, each call is
incrementing `timesQueried` value but only for ongoing calls.

#### Example responses:

The empty JSON if there is no phone call currently:

```json
{}
```

Incoming call "ringing":

```json
{
  "time": "2023-08-28T18:18:49+02:00",
  "outgoing": false,
  "ongoing": false,
  "number": "6505551212",
  "name": null
}
```

Incoming call "offhook":

```json
{
  "time": "2023-08-28T18:18:49+02:00",
  "outgoing": false,
  "ongoing": true,
  "number": "6505551212",
  "name": null
}
```

Outgoing call:

```json
{
  "time": "2023-08-28T18:18:49+02:00",
  "outgoing": false,
  "ongoing": true,
  "number": "6505551212",
  "name": null
}
```

#### NOTES:

* The `name` value can be `null` if the phone `number` is not named in the phone book.
* Outgoing calls always have the value `ongoing` set as `true`.

### GET `/log`

Provides information about phone calls.

#### Example response:

```json
[
  {
    "beginning": "2023-08-27T15:28:50+02:00",
    "duration": 26,
    "number": "+48500444555",
    "name": "Test",
    "timesQueried": 5
  },
  {
    "beginning": "2023-08-27T15:20:22+02:00",
    "duration": 0,
    "number": "+48500444555",
    "name": "Test",
    "timesQueried": 0
  },
  {
    "beginning": "2023-08-27T15:20:06+02:00",
    "duration": 25,
    "number": "6505551212",
    "name": null,
    "timesQueried": 0
  }
]
```

## Development

The following section describes how to test the application's HTTP server during development.

To connect to the server from the host machine while the app is running on an emulator is needed to
forward the port via adb:

```shell
adb forward tcp:12345 tcp:12345
```

For getting server metadata, execute:

```shell
curl -v localhost:12345
```

For getting status about current connection metadata, execute:

```shell
curl -v localhost:12345/status
```

For getting status about call log, execute:

```shell
curl -v localhost:12345/log
```
