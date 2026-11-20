package com.moyuer233.deepseekchatapp;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(DeepSeekAccountPlugin.class);
        super.onCreate(savedInstanceState);
    }
}
