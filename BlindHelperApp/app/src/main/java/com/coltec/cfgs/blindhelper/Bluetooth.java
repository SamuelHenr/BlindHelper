package com.coltec.cfgs.blindhelper;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by Dener.
 * Adapted by Chrystian Melo
 */

public class Bluetooth extends AppCompatActivity {

    public static int ENABLE_BLUETOOTH = 1;
    public static int SELECT_PAIRED_DEVICE = 2;
    public static int SELECT_DISCOVERED_DEVICE = 3;
    public static TextView statusMessage;
    public BluetoothConnection connection;
    public Button btn_paired;
    public Button btn_search;
    public Button btn_visibility;
    public Button btn_connect_client;
    public Button btn_connect_server;
    public Button btn_send;
    public static TextView output_text;
    public static Context mContext;
    public static  String output_text_string = "";
    public EditText input_text;
    public String macSelected = "";

    public static Handler handler = new Handler() {
        String msn = "nada";

        @Override
        public void handleMessage(Message msg) {

            Bundle bundle = msg.getData();
            byte[] data = bundle.getByteArray("data");
            String dataString= new String(data);

            if(dataString.equals("---N"))
                statusMessage.setText("Ocorreu um erro durante a conexão D:");
            else if(dataString.equals("---S"))
                statusMessage.setText("Conectado :D");
            else {
                output_text.setMovementMethod(new ScrollingMovementMethod());
                output_text_string += "ele: " + dataString + "\n";
                output_text.setText(output_text_string);
                this.msn = dataString;
                Bluetooth.verify(this.msn);//a cada msn confiro se é oq eu espero
            }
        }
    };

    public static void verify(String x){
        if (x.equals("0")) //obstaculo à esquerda
            MediaPlayer.create(mContext, R.raw.ob_esq).start();
        else if(x.equals("1"))//obstaculo à direira
            MediaPlayer.create(mContext, R.raw.ob_dir).start();
        else if(x.equals("2"))//obstaculo frente
            MediaPlayer.create(mContext, R.raw.ob_fren).start();
        else if(x.equals("3"))//obstaculo abaixo
            MediaPlayer.create(mContext, R.raw.ob_abai).start();
        else if(x.equals("4"))//vire à Esq
            MediaPlayer.create(mContext, R.raw.vir_esq).start();
        else if(x.equals("5")) //vire à Dir
            MediaPlayer.create(mContext, R.raw.vir_dir).start();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        mContext = getApplicationContext();
        statusMessage = findViewById(R.id.lbl_status);
        btn_paired = findViewById(R.id.btn_paired);
        btn_search = findViewById(R.id.btn_search);
        btn_visibility = findViewById(R.id.btn_visibility);
        btn_connect_client = findViewById(R.id.btn_connect_client);
        btn_connect_server = findViewById(R.id.btn_connect_server);
        //btn_send = findViewById(R.id.btn_send);
        //input_text = findViewById(R.id.input_text);
        output_text = findViewById(R.id.output_text);

        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            statusMessage.setText("Que pena! Hardware Bluetooth não está funcionando :(");
            return;
        } else {
            statusMessage.setText("Ótimo! Hardware Bluetooth está funcionando :)");
        }

        if(!btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH);
            statusMessage.setText("Solicitando ativação do Bluetooth...");
        } else {
            statusMessage.setText("Bluetooth está pronto para ser usado :)");
        }

        btn_paired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchPairedDevices();
            }
        });

        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                discoverDevices();
            }
        });

        btn_visibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enableVisibility();
            }
        });

        btn_connect_client.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectAsClient();
            }
        });

        btn_connect_server.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectAsServer();
            }
        });

        /*btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == ENABLE_BLUETOOTH) {
            if(resultCode == RESULT_OK) {
                statusMessage.setText("Bluetooth ativado");
            }
            else {
                statusMessage.setText("Falha ao ativar bluetooth");
            }
        }
        if(requestCode == SELECT_PAIRED_DEVICE || requestCode == SELECT_DISCOVERED_DEVICE) {
            if(resultCode == RESULT_OK) {
                statusMessage.setText("Você selecionou " + data.getStringExtra("btDevName") + "\n"
                        + data.getStringExtra("btDevAddress"));
                macSelected = data.getStringExtra("btDevAddress");
            }
            else {
                statusMessage.setText("Nenhum dispositivo selecionado :(");
            }
        }
    }

    public void searchPairedDevices() {
        Intent searchPairedDevicesIntent = new Intent(this, BluetoothPairedDevices.class);
        startActivityForResult(searchPairedDevicesIntent, SELECT_PAIRED_DEVICE);
    }

    public void discoverDevices() {
        Intent searchPairedDevicesIntent = new Intent(this, BluetoothDiscoveredDevices.class);
        startActivityForResult(searchPairedDevicesIntent, SELECT_DISCOVERED_DEVICE);
    }

    public void enableVisibility() {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 30);
        startActivity(discoverableIntent);
    }

    public void connectAsClient() {
        System.out.println(macSelected);
        connection = new BluetoothConnection(macSelected);
        connection.start();
    }

    public void connectAsServer() {
        connection = new BluetoothConnection();
        connection.start();
    }

    /*public void sendMessage() {
        String msg = input_text.getText().toString();
        output_text_string += "você: " + msg + "\n";
        output_text.setText(output_text_string);
        input_text.setText("");
        byte[] data = msg.getBytes();
        connection.write(data);
    }*/
}