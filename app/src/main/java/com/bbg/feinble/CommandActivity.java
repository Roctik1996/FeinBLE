package com.bbg.feinble;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bbg.feinble.comm.Observer;
import com.bbg.feinble.comm.ObserverManager;
import com.bbg.feinblelib.BleManager;
import com.bbg.feinblelib.callback.BleMtuChangedCallback;
import com.bbg.feinblelib.callback.BleReadCallback;
import com.bbg.feinblelib.data.BleDevice;
import com.bbg.feinblelib.exception.BleException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class CommandActivity extends AppCompatActivity implements Observer {
    public static final String KEY_DATA = "key_data";
    private BleDevice bleDevice;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_command);
        Button protocolBtn = findViewById(R.id.btn_protocol);
        Button setChargingBtn = findViewById(R.id.btn_set_charging_mode);
        Button readChargingBtn = findViewById(R.id.btn_read_charging_mode);
        Button logMemoryBtn = findViewById(R.id.btn_log_memory);
        Button flashBtn = findViewById(R.id.btn_flash);
        Button setsBtn = findViewById(R.id.btn_sets);
        Button batteryDataBtn = findViewById(R.id.btn_battery_data);
        Button chargerLogBtn = findViewById(R.id.btn_charger_log_data);

        TextView resultCommand = findViewById(R.id.txt);

        bleDevice = getIntent().getParcelableExtra(KEY_DATA);
        if (bleDevice == null)
            finish();

        BleManager.getInstance().setMtu(bleDevice, 256, new BleMtuChangedCallback() {
            @Override
            public void onSetMTUFailure(BleException exception) {

            }

            @Override
            public void onMtuChanged(int mtu) {
                System.out.println(mtu);
            }
        });

        protocolBtn.setOnClickListener(v ->
                BleManager.getInstance().readProtocolVersion(
                        bleDevice,
                        new BleReadCallback() {

                            @Override
                            public void onReadSuccess(HashMap data) {
                                resultCommand.setText("");
                                TreeMap<String, String> sorted = new TreeMap<>(data);
                                Set<Map.Entry<String, String>> mappings = sorted.entrySet();
                                for (Map.Entry<String, String> result : mappings) {
                                    resultCommand.setText(resultCommand.getText() + "" + result.getKey() + " : " + result.getValue() + "\n");
                                }
                            }

                            @Override
                            public void onReadFailure(BleException exception) {

                            }
                        }));
        setChargingBtn.setOnClickListener(v ->
                BleManager.getInstance().setChargingMode(
                        bleDevice,
                        76,
                        new BleReadCallback() {
                            @Override
                            public void onReadSuccess(HashMap data) {
                                resultCommand.setText("");
                                TreeMap<String, String> sorted = new TreeMap<>(data);
                                Set<Map.Entry<String, String>> mappings = sorted.entrySet();
                                for (Map.Entry<String, String> result : mappings) {
                                    resultCommand.setText(resultCommand.getText() + "" + result.getKey() + " : " + result.getValue() + "\n");
                                }
                            }

                            @Override
                            public void onReadFailure(BleException exception) {

                            }
                        }));
        readChargingBtn.setOnClickListener(v ->
                BleManager.getInstance().readChargingMode(
                        bleDevice,
                        new BleReadCallback() {

                            @Override
                            public void onReadSuccess(HashMap data) {
                                resultCommand.setText("");
                                TreeMap<String, String> sorted = new TreeMap<>(data);
                                Set<Map.Entry<String, String>> mappings = sorted.entrySet();
                                for (Map.Entry<String, String> result : mappings) {
                                    resultCommand.setText(resultCommand.getText() + "" + result.getKey() + " : " + result.getValue() + "\n");
                                }
                            }

                            @Override
                            public void onReadFailure(BleException exception) {

                            }
                        }));
        logMemoryBtn.setOnClickListener(v ->
                BleManager.getInstance().readBatteryLogMemory(
                        bleDevice, new BleReadCallback() {
                            @Override
                            public void onReadSuccess(HashMap data) {
                                resultCommand.setText("");
                                TreeMap<String, String> sorted = new TreeMap<>(data);
                                Set<Map.Entry<String, String>> mappings = sorted.entrySet();
                                for (Map.Entry<String, String> result : mappings) {
                                    resultCommand.setText(resultCommand.getText() + "" + result.getKey() + " : " + result.getValue() + "\n");
                                }
                            }

                            @Override
                            public void onReadFailure(BleException exception) {

                            }
                        }));
        flashBtn.setOnClickListener(v ->
                BleManager.getInstance().readBatteryDataStored(
                        bleDevice,
                        new BleReadCallback() {
                            @Override
                            public void onReadSuccess(HashMap data) {
                                resultCommand.setText("");
                                TreeMap<String, String> sorted = new TreeMap<>(data);
                                Set<Map.Entry<String, String>> mappings = sorted.entrySet();
                                for (Map.Entry<String, String> result : mappings) {
                                    resultCommand.setText(resultCommand.getText() + "" + result.getKey() + " : " + result.getValue() + "\n");
                                }
                            }

                            @Override
                            public void onReadFailure(BleException exception) {

                            }
                        }));
        setsBtn.setOnClickListener(v ->
                BleManager.getInstance().readBatteryDataSetsNumber(
                        bleDevice,
                        0,
                        0,
                        new BleReadCallback() {
                            @Override
                            public void onReadSuccess(HashMap data) {
                                resultCommand.setText("");
                                TreeMap<String, String> sorted = new TreeMap<>(data);
                                Set<Map.Entry<String, String>> mappings = sorted.entrySet();
                                for (Map.Entry<String, String> result : mappings) {
                                    resultCommand.setText(resultCommand.getText() + "" + result.getKey() + " : " + result.getValue() + "\n");
                                }
                            }

                            @Override
                            public void onReadFailure(BleException exception) {

                            }
                        }));
        batteryDataBtn.setOnClickListener(v ->
                BleManager.getInstance().readCurrentBatteryLogData(
                        bleDevice, new BleReadCallback() {

                            @Override
                            public void onReadSuccess(HashMap data) {
                                resultCommand.setText("");
                                TreeMap<String, String> sorted = new TreeMap<>(data);
                                Set<Map.Entry<String, String>> mappings = sorted.entrySet();
                                for (Map.Entry<String, String> result : mappings) {
                                    resultCommand.setText(resultCommand.getText() + "" + result.getKey() + " : " + result.getValue() + "\n");
                                }
                            }

                            @Override
                            public void onReadFailure(BleException exception) {

                            }
                        }));
        chargerLogBtn.setOnClickListener(v ->
                BleManager.getInstance().readChargerLogData(
                        bleDevice, new BleReadCallback() {
                            @Override
                            public void onReadSuccess(HashMap data) {
                                resultCommand.setText("");
                                TreeMap<String, String> sorted = new TreeMap<>(data);
                                Set<Map.Entry<String, String>> mappings = sorted.entrySet();
                                for (Map.Entry<String, String> result : mappings) {
                                    resultCommand.setText(resultCommand.getText() + "" + result.getKey() + " : " + result.getValue() + "\n");
                                }
                            }

                            @Override
                            public void onReadFailure(BleException exception) {

                            }
                        }));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BleManager.getInstance().clearCharacterCallback(bleDevice);
        ObserverManager.getInstance().deleteObserver(this);
    }

    @Override
    public void disConnected(BleDevice device) {
        if (device != null && bleDevice != null && device.getKey().equals(bleDevice.getKey())) {
            finish();
        }
    }
}
