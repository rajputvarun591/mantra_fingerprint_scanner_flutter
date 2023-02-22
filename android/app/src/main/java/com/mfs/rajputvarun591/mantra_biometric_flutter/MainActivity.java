package com.mfs.rajputvarun591.mantra_biometric_flutter;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodChannel;

public class MainActivity extends FlutterActivity implements  EventChannel.StreamHandler{

    private EventChannel eventChannel;
    private EventChannel.EventSink eventSink;

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);

        MethodChannel methodChannel = new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), "com.mantra.biometric");
        methodChannel.setMethodCallHandler((call, result) -> {
            if (call.method.contains("readFingerPrintData")) {
                Intent intent = new Intent(MainActivity.this, MFS100CodeHubs.class);
                startActivityForResult(intent, 101);
            }
        });

        eventChannel = new EventChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), "com.mantra.biometric.event");
        eventChannel.setStreamHandler(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check that it is the SecondActivity with an OK result
        if (requestCode == 101) {
            if (resultCode == RESULT_OK) {
                String base64FingerPrintData = data.getStringExtra("base64FingerPrintData");
                System.out.print(base64FingerPrintData);
                if (eventSink != null) {
                    eventSink.success(base64FingerPrintData);
                }
            }
        }
    }

    @Override
    public void onListen(Object arguments, EventChannel.EventSink eventSink) {
        if (eventSink == null) {
            this.eventSink = eventSink;
        }
    }

    @Override
    public void onCancel(Object arguments) {

    }
}
