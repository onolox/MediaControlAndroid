package fabio.IntentProxy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import java.io.File;

public class ScreenOnReceiver extends BroadcastReceiver {
    public ScreenOnReceiver() { }
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("ProxyIntent", "Tela ligada");
        
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                File sdCard = LeitorArquivos.localizarSDCard();
                if (sdCard != null) {
                    LeitorArquivos.lerArquivos();
                    Intent enviarIntent = new Intent(MainActivity.contexto, EnviarActivity.class);
                    MainActivity.contexto.startActivity(enviarIntent);
                }
            }
        }, 7000);
    }
}
