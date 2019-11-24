package fabio.IntentProxy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class PermissaoDir extends AppCompatActivity {
    static Uri uri;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        teste();
    }
    
    void teste() {
        StorageManager sm = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        StorageVolume volume = sm.getStorageVolume(LeitorArquivos.localizarSDCard());
        Intent intent = volume.createAccessIntent(null);
        startActivityForResult(intent, 699);
        
    }
    
    void triggerStorageAccessFramework() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.setType("*/*");
        startActivityForResult(intent, 699);
    }
    
    @Override
    public final void onActivityResult(final int requestCode, final int resultCode, final Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == 699) {
            Uri treeUri = null;
            if (resultCode == Activity.RESULT_OK) {
                treeUri = resultData.getData();
                PermissaoDir.uri = treeUri;
                
                // Persist URI in shared preference so that you can use it later.
                setSharedPreferenceUri(123456789, treeUri);
                
                // Persist access permissions.
                final int takeFlags = resultData.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                getContentResolver().takePersistableUriPermission(treeUri, takeFlags);
                LeitorArquivos.lerArquivos();
                finish();
            }
        }
    }
    
    public static void setSharedPreferenceUri(final int preferenceId, @Nullable final Uri uri) {
        Editor editor = getSharedPreferences().edit();
        if (uri == null) {
            editor.putString(MainActivity.contexto.getString(preferenceId), null);
        }
        else {
            editor.putString(preferenceId + "", uri.toString());
        }
        editor.apply();
    }
    
    private static SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(MainActivity.contexto);
    }
}
