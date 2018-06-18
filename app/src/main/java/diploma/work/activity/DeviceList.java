package diploma.work.activity;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.widget.TextView;
import android.widget.Toast;
import diploma.work.R;
import java.util.ArrayList;
import java.util.Set;


public class DeviceList extends ActionBarActivity {
    private ListView listPairedDevices;
    private Toast toast;
    private BluetoothAdapter bluetoothAdapter = null;
    public static String EXTRA_ADDRESS = "device_address";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        listPairedDevices = (ListView) findViewById(R.id.listView);
        Button btnPaired = (Button) findViewById(R.id.button);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth is not available on this device", Toast.LENGTH_LONG).show();
            finish();
        } else if (!bluetoothAdapter.isEnabled()) {
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTon, 1);
        } else if (bluetoothAdapter.isEnabled()) {
            pairedDevicesList();
        }

        btnPaired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pairedDevicesList();
            }
        });
    }

    private void pairedDevicesList() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        ArrayList<String> list = new ArrayList<>();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice bt : pairedDevices) {
                list.add(bt.getName() + "\n" + bt.getAddress());
            }
        } else {
            if (!bluetoothAdapter.isEnabled() && bluetoothAdapter != null) {
                try {
                    toast.getView().isShown();
                    toast.setText("Turn on Bluetooth, please");
                } catch (Exception e) {
                    toast = Toast.makeText(getApplicationContext(), "Turn on Bluetooth, please", Toast.LENGTH_LONG);
                }
                toast.show();
            } else {
                try {
                    toast.getView().isShown();
                    toast.setText("No paired bluetooth devices found");
                } catch (Exception e) {
                    toast = Toast.makeText(getApplicationContext(), "No paired bluetooth devices found", Toast.LENGTH_LONG);
                }
                toast.show();
            }
        }

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
        listPairedDevices.setAdapter(adapter);
        listPairedDevices.setOnItemClickListener(myListClickListener);
    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View view, int arg2, long arg3) {
            String info = ((TextView) view).getText().toString();
            String address = info.substring(info.length() - 17);
            Intent intent = new Intent(DeviceList.this, SBCControl.class);

            intent.putExtra(EXTRA_ADDRESS, address);
            startActivity(intent);
        }
    };

}
