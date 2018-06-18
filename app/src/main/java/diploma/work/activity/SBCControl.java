package diploma.work.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;

import diploma.work.R;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;

import me.itangqi.waveloadingview.WaveLoadingView;

public class SBCControl extends ActionBarActivity {

    private String address = null;
    private ProgressDialog connectionProgress;

    private BluetoothAdapter myBluetooth = null;
    private BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    private int option;
    private Button buttonOrder;
    private Button buttonDisconnect;
    private ImageButton buttonOptions;
    private ProgressBar progressBar;
    private TextView textView;
    private WaveLoadingView waveLoadingView;
    private Toast toast;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent newint = getIntent();
        address = newint.getStringExtra(DeviceList.EXTRA_ADDRESS);
        setContentView(R.layout.activity_sbc_control);

        buttonOptions = (ImageButton) findViewById(R.id.button_options);
        buttonOrder = (Button) findViewById(R.id.button_order);
        buttonDisconnect = (Button) findViewById(R.id.button_disconnect);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        textView = (TextView) findViewById(R.id.text_view);
        waveLoadingView = (WaveLoadingView) findViewById(R.id.wave_loading_view);

        new BtConnect().execute();

        buttonOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                order();
            }
        });

        buttonDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnect();
            }
        });

        buttonOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SBCControl.this);
                View view = getLayoutInflater().inflate(R.layout.activity_settings, null);
                builder.setView(view);
                builder.setTitle("Select milliliters you want to fill into glass");
                AlertDialog dialog;
                CharSequence[] items = {"Full (350ml)", "Almost full (275ml)", "A half (175ml)", "Just to taste (75ml)"};
                builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        switch (item) {
                            default:
                            case 0:
                                option = 0;
                                break;
                            case 1:
                                option = 1;
                                break;
                            case 2:
                                option = 2;
                                break;
                            case 3:
                                option = 3;
                                break;
                        }
                        dialog.dismiss();
                    }
                });
                dialog = builder.create();
                dialog.show();
            }
        });
    }

    private void disconnect() {
        if (btSocket != null) {
            try {
                btSocket.close();
            } catch (IOException e) {
                msg("Could not disconnect. Try again");
            }
        }
        finish();

    }

    private void order() {
        byte[] message = {(byte) Integer.parseInt("1" + String.valueOf(option))};
        if (btSocket != null) {
            try {
                btSocket.getOutputStream().write(message);
                btSocket.getOutputStream().flush();

            } catch (IOException e) {
                msg("Could not order. Try again");
            }
            if (myBluetooth.isEnabled() && btSocket.isConnected()) {
                new BtResponse().execute();
            }
        }
    }

    private void msg(String messageToDisplay) {
        try {
            toast.getView().isShown();
            toast.setText(messageToDisplay);
        } catch (Exception e) {
            toast = Toast.makeText(getApplicationContext(), messageToDisplay, Toast.LENGTH_LONG);
        }
        toast.show();
    }


    private class BtConnect extends AsyncTask<Void, Void, Void> {
        private boolean ConnectSuccess = true;

        @Override
        protected void onPreExecute() {
            connectionProgress = ProgressDialog.show(SBCControl.this, "Connecting...", "Please wait...");
        }

        @Override
        protected Void doInBackground(Void... devices) {
            try {
                if (btSocket == null || !isBtConnected) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice device = myBluetooth.getRemoteDevice(address);
                    btSocket = device.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();
                }
            } catch (IOException e) {
                ConnectSuccess = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                msg("Connection failed");
                finish();
            } else {
                msg("Connected");
                isBtConnected = true;
            }
            connectionProgress.dismiss();
        }
    }

    private class BtResponse extends AsyncTask<Void, Void, Void> {
        CountDownTimer countDownTimer;
        private boolean responseSuccess = false;
        private int duration;
        private String message;
        private int counter = 2;
        private byte[] buffer = new byte[1];
        private char percentSymbol = '%';


        @Override
        protected void onPreExecute() {
            buttonOrder.setClickable(false);
            buttonDisconnect.setClickable(false);
            buttonOptions.setClickable(false);
            switch (option) {
                default:
                case 0:
                    duration = 25;
                    break;
                case 1:
                    duration = 20;
                    break;
                case 2:
                    duration = 15;
                    break;
                case 3:
                    duration = 10;
                    break;
            }
            countDownTimer = new CountDownTimer(duration * 1000, duration * 10) {
                @Override
                public void onTick(long l) {
                    textView.setText(String.format(Locale.getDefault(), "%d%c", counter, percentSymbol));
                    progressBar.setProgress(counter++);
                    waveLoadingView.setProgressValue(counter);
                    waveLoadingView.setAmplitudeRatio(counter / 2);
                }

                @Override
                public void onFinish() {
                    try {
                        btSocket.getInputStream().read(buffer);
                        message = new String(buffer);
                        if (message.equals("4")) {
                            responseSuccess = true;
                        }
                    } catch (IOException e) {
                        responseSuccess = false;
                    }
                    cleanUp();
                    if (responseSuccess) {
                        msg("Device has finished its job");
                    } else {
                        msg("Device could not receive confirmation due to connection problem");
                    }
                }
            }.start();
        }

        @Override
        protected Void doInBackground(Void... devices) {
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }

        private void cleanUp() {
            textView.setText(String.format(Locale.getDefault(), "0%c", percentSymbol));
            progressBar.setProgress(0);
            waveLoadingView.setProgressValue(5);
            waveLoadingView.setAmplitudeRatio(2);
            buttonOrder.setClickable(true);
            buttonDisconnect.setClickable(true);
            buttonOptions.setClickable(true);
        }
    }
}
