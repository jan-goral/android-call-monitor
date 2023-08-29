## Problems

### LogCall.Log start/end

The following log shows a dump of the incoming connection from pixel 6a with android 13.

The problem here is that CallLog beginning date matches the date of RINGING state, not the OFFHOOK.
Based on CallLog results from the content resolver is not possible to assume when the call was
really started and ended.

```
 1. 18:40:34.004  D  (android.intent.action.PHONE_STATE, {incoming_number=510978679, state=RINGING})
 2. 18:40:34.014  I  Status(time=Tue Aug 29 18:40:34 GMT+02:00 2023, outgoing=false, ongoing=false, number=510978679, name=Krzyś)
 3. 18:40:45.587  D  (android.intent.action.PHONE_STATE, {incoming_number=510978679, state=OFFHOOK})
 4. 18:40:45.588  I  Status(time=Tue Aug 29 18:40:45 GMT+02:00 2023, outgoing=false, ongoing=true, number=510978679, name=Krzyś)
 5. 18:40:48.805  I  Status(time=Tue Aug 29 18:40:48 GMT+02:00 2023, outgoing=false, ongoing=true, number=510978679, name=Krzyś) - get
 6. 18:40:49.424  I  Status(time=Tue Aug 29 18:40:49 GMT+02:00 2023, outgoing=false, ongoing=true, number=510978679, name=Krzyś) - get
 7. 18:40:50.150  I  Status(time=Tue Aug 29 18:40:50 GMT+02:00 2023, outgoing=false, ongoing=true, number=510978679, name=Krzyś) - get
 8. 18:40:51.660  D  (android.intent.action.PHONE_STATE, {incoming_number=510978679, state=IDLE})
 9. 18:40:51.660  I  Status(time=Tue Aug 29 18:40:51 GMT+02:00 2023, outgoing=false, ongoing=true, number=510978679, name=Krzyś) - finish
10. 18:40:51.701  I  Log(beginning=Tue Aug 29 18:40:33 GMT+02:00 2023, duration=6, number=510978679, name=Krzyś, timesQueried=0)
```

As a result, calculating `timesQueried` is not as straightforward as just saving request timestamps
in the database, and counting occurrences between the start date & end date.

### Handling multiple connection at the same time

The following logs show data gathered via broadcast receiver observing
on `android.intent.action.PHONE_STATE` intent.

The problem is the insufficient information about handling two connections at the same time.

#### 1. No information about second call stop.

The following log shows the case when during the first outgoing call the second incoming call is
accepted and finished, and after a while the first is finished.

```
1. 19:20:36.378  D  (android.intent.action.PHONE_STATE, {incoming_number=6505551212, state=OFFHOOK})
2. 19:20:43.662  D  (android.intent.action.PHONE_STATE, {incoming_number=+16505556789, state=RINGING})
3. 19:20:48.861  D  (android.intent.action.PHONE_STATE, {incoming_number=+16505556789, state=OFFHOOK})
4. 19:20:58.114  D  (android.intent.action.PHONE_STATE, {incoming_number=6505551212, state=IDLE})
```

Read the log as follows:

1. First connection (outgoing) started
2. Second connection (incoming) ringing
3. Second connection (incoming) started
4. First connection (outgoing) finished

As a result, observing on `android.intent.action.PHONE_STATE` intent doesn't provide information
about the second call finish.

#### 2. No information about second call reject.

The following log looks pretty much the same as from point 1. but the actions were different.
The main difference was the second call was rejected instead of accepted.

```
19:33:33.133  D  (android.intent.action.PHONE_STATE, {incoming_number=6505551212, state=OFFHOOK})
19:33:40.512  D  (android.intent.action.PHONE_STATE, {incoming_number=+16505556789, state=RINGING})
19:33:43.942  D  (android.intent.action.PHONE_STATE, {incoming_number=+16505556789, state=OFFHOOK})
19:33:46.945  D  (android.intent.action.PHONE_STATE, {incoming_number=6505551212, state=IDLE})
```

As a result, observing on `android.intent.action.PHONE_STATE` intent doesn't provide information
about the second call reject.

#### 3. No information about call switch.

The following log shows the case when during the first outgoing call the second incoming call is
accepted, switched to the first, and finished, and after a while, the second is finished.

```
19:44:34.206  D  (android.intent.action.PHONE_STATE, {incoming_number=6505551212, state=OFFHOOK})
19:44:40.043  D  (android.intent.action.PHONE_STATE, {incoming_number=+16505556789, state=RINGING})
19:44:43.391  D  (android.intent.action.PHONE_STATE, {incoming_number=+16505556789, state=OFFHOOK})
19:44:53.613  D  (android.intent.action.PHONE_STATE, {incoming_number=+16505556789, state=IDLE})
```

As a result, observing on `android.intent.action.PHONE_STATE` intent doesn't provide information
about the call switch also the first call finish is omitted.
