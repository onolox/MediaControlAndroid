package fabio.IntentProxy;

import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Objects;

import androidx.documentfile.provider.DocumentFile;

public class LeitorArquivos {
    static LinkedList<Uri> listaArquivos;
    static String ultimoArquivo = "";
    
    static void lerArquivos() {
        listaArquivos = new LinkedList();
        try {
            DocumentFile sdCard = getSdCard();
            if (sdCard == null) return;
            
            DocumentFile[] files2 = sdCard.listFiles();
            
            for (int x = 0; x < files2.length; x++) {
                if (files2[x].getName().endsWith("mp4") || files2[x].getName().endsWith("mkv") || files2[x].getName().endsWith("avi")) {
                    listaArquivos.add(files2[x].getUri());
                }
            }
            listaArquivos.sort(new Comparator<Uri>() {
                @Override
                public int compare(Uri uri, Uri t1) {
                    return uri.getLastPathSegment().split(":")[1].compareToIgnoreCase(t1.getLastPathSegment().split(":")[1]);
                }
            });
            
            MainActivity.mAdapter.clear();
            for (Uri s : listaArquivos) {
                MainActivity.mAdapter.add(s.getLastPathSegment().split(":")[1]);
            }
            Log.d("ProxyIntent", "QTD arquivos " + listaArquivos.size());
            
            MainActivity.mAdapter.notifyDataSetChanged();
        }
        catch (SecurityException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    static Uri buscarProximoLista() {
        Uri retorno = null;
        DocumentFile sdCard = getSdCard();
        if (sdCard == null) return null;
        
        if (ultimoArquivo == null || ultimoArquivo.isEmpty()) {
            DocumentFile ultimoArq = sdCard.findFile("UltimoArquivo.txt");
            if (ultimoArq != null && ultimoArq.exists()) {
                try (InputStream inputStream = MainActivity.contexto.getContentResolver().openInputStream(ultimoArq.getUri()); BufferedReader reader =
                        new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream)))) {
                    String s = reader.readLine();
                    if (s != null) {
                        ultimoArquivo = s;
                        Log.d("ProxyIntent", "Último arquivo lido: " + s);
                    }
                }
                catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                ultimoArquivo = listaArquivos.getFirst().getLastPathSegment().split(":")[1];
                return listaArquivos.getFirst();
            }
        }
        
        for (Uri uri : listaArquivos) {
            String nome = uri.getLastPathSegment().split(":")[1].substring(0, 3);
            
            if (ultimoArquivo.startsWith(nome)) {
                ultimoArquivo = uri.getLastPathSegment().split(":")[1];
                retorno = uri;
                break;
            }
        }
        
        return retorno;
    }
    
    public static void gravarPosicao(int posicao) {
        try {
            DocumentFile sdCard = getSdCard();
            if (sdCard == null) return;
            
            DocumentFile file = sdCard.findFile(ultimoArquivo.substring(0, 3) + ".posicao");
            if (file == null || !file.exists()) {
                file = sdCard.createFile(null, ultimoArquivo.substring(0, 3) + ".posicao");
            }
            
            DocumentFile ultimoArq = sdCard.findFile("UltimoArquivo.txt");
            if (ultimoArq == null || !ultimoArq.exists()) {
                ultimoArq = sdCard.createFile("text/plain", "UltimoArquivo.txt");
            }
            
            if (!file.canWrite()) {
                Log.d("ProxyIntent", "Erro ao tentar gravar, cant write");
            }
            else {
                OutputStream outputStream = MainActivity.contexto.getContentResolver().openOutputStream(file.getUri());
                outputStream.write((posicao + "").getBytes());
                outputStream.close();
                
                outputStream = MainActivity.contexto.getContentResolver().openOutputStream(ultimoArq.getUri());
                outputStream.write(ultimoArquivo.getBytes());
                outputStream.close();
                
                Log.d("ProxyIntent", file.getName() + " gravado, ultimo arquivo gravado: " + ultimoArquivo);
            }
        }
        catch (SecurityException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static int lerPosicao() {
        int posicao = 1;
        
        try {
            DocumentFile sdCard = getSdCard();
            if (sdCard == null) return -1;
            
            DocumentFile file = sdCard.findFile(ultimoArquivo.substring(0, 3) + ".posicao");
            if (file == null || !file.exists() && !file.canRead()) {
                Log.d("ProxyIntent", "Erro ao tentar ler, arquivo " + ultimoArquivo.substring(0, 3) + ".posicao não encontrado.");
            }
            else {
                StringBuilder stringBuilder = new StringBuilder();
                try (InputStream inputStream = MainActivity.contexto.getContentResolver().openInputStream(file.getUri()); BufferedReader reader =
                        new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream)))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                }
                
                posicao = Integer.parseInt(stringBuilder.toString()) - 5000;
            }
        }
        catch (SecurityException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        return posicao;
    }
    
    static int removerInicio() {
        int retorno = 0;
        DocumentFile sdCard = getSdCard();
        if (sdCard == null) return 1;
        
        DocumentFile[] files = sdCard.listFiles();
        for (DocumentFile doc : files) {
            if (doc.getName().endsWith("inicio") && ultimoArquivo.startsWith(doc.getName().split("\\.")[0])) {
                try (InputStream inputStream = MainActivity.contexto.getContentResolver().openInputStream(doc.getUri()); BufferedReader reader =
                        new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream)))) {
                    retorno = Integer.parseInt(reader.readLine()) * 1000;
                }
                catch (FileNotFoundException e) {
                    Log.d("ProxyIntent", "Erro ao tentar ler, arquivo de remoção de inicio não encontrado");
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return retorno;
    }
    
    static File localizarSDCard() {
        File raiz = new File("/storage/");
        String[] files = raiz.list();
        
        for (int i = 0; i < files.length; i++) {
            File dir = new File("/storage/" + files[i]);
            
            if (!dir.getName().endsWith("self") && !dir.getName().endsWith("emulated")) {
                return dir;
            }
        }
        return null;
    }
    
    static DocumentFile getSdCard() {
        DocumentFile sdCard = DocumentFile.fromTreeUri(MainActivity.contexto, PermissaoDir.uri);
        if (sdCard == null) {
            Log.d("ProxyIntent", "Erro ao tentar ler, SDCard é NULL");
            return null;
        }
        return sdCard;
    }
    
}
