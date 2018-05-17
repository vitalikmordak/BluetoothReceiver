package com.vitaliimordak.bluetoothreceiver;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vitalii on 17.12.2017.
 */

public class Scan extends Activity implements Bluetooth.DiscoveryCallback, AdapterView.OnItemClickListener {
    private ProgressBar progressBar;
    private TextView textView;
    private ListView listView;
    private Button button;
    private ArrayAdapter<String> adapter;
    private Bluetooth bluetooth;
    private List<BluetoothDevice> devices;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan_activity);

        progressBar = findViewById(R.id.scan_progressbar);
        textView = findViewById(R.id.scan_state);
        listView = findViewById(R.id.scan_listview);
        button = findViewById(R.id.scan_again_button);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new ArrayList<String>());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        bluetooth = new Bluetooth(this);
        bluetooth.setDiscoveryCallback(this);

        bluetooth.scanDevices();
        progressBar.setVisibility(View.VISIBLE);
        textView.setText(getString(R.string.scanning)); //"Scanning..."
        listView.setEnabled(false);

        button.setEnabled(false);
        devices = new ArrayList<>();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.clear();
                        button.setEnabled(false);
                    }
                });

                devices = new ArrayList<>();
                progressBar.setVisibility(View.VISIBLE);
                textView.setText(getString(R.string.scanning)); //"Scanning..."
                bluetooth.scanDevices();
            }
        });

    }

    private void setText(final String txt) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(txt);
            }
        });
    }

    private void setProgressVisibility(final int id) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(id);
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        setProgressVisibility(View.VISIBLE);
        setText(getString(R.string.pairing)); // "Pairing..."
        bluetooth.pair(devices.get(i));
    }

    @Override
    public void onFinish() {
        setProgressVisibility(View.INVISIBLE);
        setText(getString(R.string.scan_finished)); // "Scan finished!"
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                button.setEnabled(true);
                listView.setEnabled(true);
            }
        });
    }

    @Override
    public void onDevice(BluetoothDevice device) {
        final BluetoothDevice tmp = device;
        devices.add(device);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.add(tmp.getAddress() + " - " + tmp.getName());
            }
        });
    }

    @Override
    public void onPair(BluetoothDevice device) {
        setProgressVisibility(View.INVISIBLE);
        setText(getString(R.string.paired)); // "Paired!"
        Intent i = new Intent(Scan.this, Select.class);
        startActivity(i);
        finish();
    }

    @Override
    public void onUnpair(BluetoothDevice device) {
        setProgressVisibility(View.INVISIBLE);
        setText(getString(R.string.paired)); // "Paired!"
    }

    @Override
    public void onError(String message) {
        setProgressVisibility(View.INVISIBLE);
        setText(getString(R.string.error) + message); // "Error: "
    }
}
