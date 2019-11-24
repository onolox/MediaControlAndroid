package fabio.IntentProxy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

import static android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends AppCompatActivity {
    BroadcastReceiver receiverScreenOn;
    static Context contexto;
    static ArrayAdapter<String> mAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        contexto = this;
        ListView lstView = findViewById(R.id.lstView);
        ArrayList<String> listaItens = new ArrayList<String>();
        
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listaItens);
        lstView.setAdapter(mAdapter);
        lstView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                LeitorArquivos.ultimoArquivo = LeitorArquivos.listaArquivos.get(i).getLastPathSegment().split(":")[1];
                
                LeitorArquivos.lerArquivos();
                Intent enviarIntent = new Intent(MainActivity.contexto, EnviarActivity.class);
                MainActivity.contexto.startActivity(enviarIntent);
            }
            
        });
        
        File sdCard = LeitorArquivos.localizarSDCard();
        if (sdCard == null) {
            Log.e("ProxyIntent", "SD Card ausente.");
            Toast.makeText(this, "SD Card ausente", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        Intent enviarIntent = new Intent(this, PermissaoDir.class);
        startActivity(enviarIntent);
        
        configureReceiverScreenOn();
        //registerUpdateReceiver();
    }
    
    private void configureReceiverScreenOn() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_ON");
        receiverScreenOn = new ScreenOnReceiver();
        registerReceiver(receiverScreenOn, filter);
    }
    
    private void registerUpdateReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.MEDIA_MOUNTED");
        filter.addDataScheme("file");
        receiverScreenOn = new ScreenOnReceiver();
        registerReceiver(receiverScreenOn, filter);
    }
}

