package fabio.IntentProxy;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

public class EnviarActivity extends AppCompatActivity {
    static AppCompatActivity enviarActivity;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enviarActivity = this;
        enviar();
    }
    
    void enviar() {
        if (BootReceiver.primeiroBoot) {
            BootReceiver.primeiroBoot = false;
            finish();
        }
        if (LeitorArquivos.listaArquivos.size() > 0) {
            Uri uri = LeitorArquivos.buscarProximoLista();
            if (LeitorArquivos.lerPosicao() < 1000) {
                LeitorArquivos.gravarPosicao(LeitorArquivos.removerInicio());
            }
            
            if (LeitorArquivos.listaArquivos.isEmpty()) {
                Log.d("ProxyIntent", "Sem arquivos para tocar");
                return;
            }
            
            if (LeitorArquivos.listaArquivos.size() > 0) {
                Log.d("ProxyIntent", "Tocando da posição: " + LeitorArquivos.lerPosicao());
                Intent mxIntent = new Intent(Intent.ACTION_VIEW);
                mxIntent.setPackage("com.mxtech.videoplayer.pro");
                mxIntent.setDataAndTypeAndNormalize(uri, "video/*");
                mxIntent.putExtra("position", LeitorArquivos.lerPosicao());
                mxIntent.putExtra("return_result", true);
                startActivityForResult(mxIntent, 69);
            }
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("ProxyIntent", "Retorno Intent:" + resultCode);
        
        if (data != null) {
            if (requestCode == 69) {
                Log.d("ProxyIntent", "Retorno Intent: " + resultCode + "  -  " + data.getIntExtra("position", -1) + "  - " + data.getStringExtra("end_by"));
                
                if (resultCode == RESULT_OK || resultCode == RESULT_CANCELED) {
                    if (data.getStringExtra("end_by") != null) {
                        if (data.getStringExtra("end_by").contains("user")) {
                            Log.d("ProxyIntent", "Retorno Intent: user");
                            LeitorArquivos.gravarPosicao(data.getIntExtra("position", 0));
                            finish();
                        }
                        else if (data.getStringExtra("end_by").contains("playback_completion")) {
                            Log.d("ProxyIntent", "Deletando arquivo finalizado");
                            
                            for (Uri uri : LeitorArquivos.listaArquivos) {
                                if (uri.getLastPathSegment().contains(LeitorArquivos.ultimoArquivo)) {
                                    DocumentFile.fromSingleUri(MainActivity.contexto, uri).delete();
                                }
                            }
                            LeitorArquivos.gravarPosicao(1);
                            LeitorArquivos.lerArquivos();
                            enviar();
                        }
                        else {
                            Log.d("ProxyIntent", "Retorno Intent: Erro");
                        }
                    }
                }
                
            }
        }
    }
    
}
















