package com.vitaliimordak.bluetoothreceiver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements Bluetooth.CommunicationCallback {

    private ScrollView scrollView;
    private TextView textView;
    private Bluetooth b;
    private boolean registered = false;
    public String fileName;
    private ArrayList<String> data = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.text);
        scrollView = findViewById(R.id.scrollView);

        textView.setMovementMethod(new ScrollingMovementMethod());
        b = new Bluetooth(this);
        b.enableBluetooth();
        b.setCommunicationCallback(this);
        int pos = getIntent().getExtras().getInt("pos");
        Display(getString(R.string.connecting)); //"Connecting..."
        b.connectToDevice(b.getPairedDevices().get(pos));

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        registered = true;
        Intent intent = getIntent();
        fileName = intent.getStringExtra("name");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (registered) {
            unregisterReceiver(mReceiver);
            registered = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Handle item selection
        switch (item.getItemId()) {
            case R.id.close:
                b.removeCommunicationCallback();
                b.disconnect();
                try {
                    saveExcelFile(data, fileName);
//                    Toast.makeText(this, data.get(0), Toast.LENGTH_LONG).show();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                Intent intent1 = new Intent(this, SelectFilename.class);
                finish();
                startActivity(intent1);
                return true;
            case R.id.name_of_file:
                Intent intent = new Intent(this, SelectFilename.class);
                startActivity(intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConnect(BluetoothDevice device) {
        Display(getString(R.string.connected_to) + device.getName() + " - " + device.getAddress()); // "Connected to"
    }

    @Override
    public void onDisconnect(BluetoothDevice device, String message) {
        Display(getString(R.string.disconnected)); //"Disconnected!"
        Display(getString(R.string.connecting_again));// "Connecting again..."
        b.connectToDevice(device);
    }

    @Override
    public void onMessage(String message) {
        Display(message);
        data.add(message);
    }

    @Override
    public void onError(String message) {
        Display(getString(R.string.error) + message);// "Error: "
    }

    @Override
    public void onConnectError(final BluetoothDevice device, String message) {
        Display("Error: " + message);
        Display("Trying again in 3 sec.");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        b.connectToDevice(device);
                    }
                }, 2000);
            }
        });
    }

    public void Display(final String s) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.append(s + "\n");
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String acrion = intent.getAction();

            if (acrion.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                Intent intent1 = new Intent(MainActivity.this, Select.class);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        if (registered) {
                            unregisterReceiver(mReceiver);
                            registered = false;
                        }
                        startActivity(intent1);
                        finish();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        if (registered) {
                            unregisterReceiver(mReceiver);
                            registered = false;
                        }
                        startActivity(intent1);
                        finish();
                        break;
                }
            }
        }
    };

    public void saveExcelFile(List<String> data, String fileName) throws IOException {

        if (!SelectFilename.isExternalStorageAvailable() || SelectFilename.isExternalStorageReadOnly()) {
            Log.w("FileUtils", "Storage not available or read only");
        }
        boolean success = true;
        String xlsFileName = fileName + ".xls";
        File file = new File(this.getExternalFilesDir(null), xlsFileName);

        FileOutputStream fos = new FileOutputStream(file);
        Toast.makeText(this, xlsFileName+getString(R.string.saved), Toast.LENGTH_LONG).show();
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("ECG_data");
        for (int i = 0; i < data.size() - 1; i++) {
            Row row = sheet.createRow(i);
            Cell cell = null;
//            Cell cell1 = row.createCell(1);
//            Cell cell2 = row.createCell(2);
            String[] buff = data.get(0).split(", ");
            try {
                cell = row.createCell(0);
                cell.setCellValue(data.get(i+1).split(", ")[0]);
                cell = row.createCell(1);
                cell.setCellValue(data.get(i+1).split(", ")[1]);
                cell = row.createCell(2);
                cell.setCellValue(data.get(i+1).split(", ")[2]);
            } catch (Exception e) {
                Log.w("FileUtils", "Error Writing!");
            }
        }

        workbook.write(fos);
        fos.flush();
        fos.close();
    }
}
