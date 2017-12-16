package org.masterandroid.wander;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by rodriii on 6/12/17.
 */

public class EditarPerfilUsuarioActivity extends AppCompatActivity {


    private TextView  nombre, apellidos, telefono, username, localidad, pais, direccion;
    private SharedPreferences pref;
    private Usuario usuario;
    private ImageView imageView;
    private Uri uriFoto;
    private String uri2;
    private int id;
    final static int RESULTADO_GALERIA = 2;
    final static int RESULTADO_FOTO = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contenedor_editar_perfil);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        nombre = findViewById(R.id.nombre);
        apellidos = findViewById(R.id.apellidos);
        telefono = findViewById(R.id.telefono);
        username = findViewById(R.id.username);
        localidad = findViewById(R.id.poblacion);
        pais = findViewById(R.id.pais);
        direccion = findViewById(R.id.direccion);
        imageView = findViewById(R.id.foto);

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        id = pref.getInt("id", -1);
        if (id == -1) {
            this.finish();
        }

        rellenarInfo();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.editar_perfil, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.accion_guardar:
                guardar(null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void guardar(View view){
        getInfo();
        ConsultaBD.updateUser(usuario, id);
        setResult(RESULT_OK);
        finish();
    }

    public void tomarFoto(View view) {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        uriFoto = Uri.fromFile(new File(
                Environment.getExternalStorageDirectory() + File.separator
                        + "img_" + (System.currentTimeMillis() / 1000) + ".jpg"));
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uriFoto);
        startActivityForResult(intent, RESULTADO_FOTO);
    }

    public void eliminarFoto(View view)
    {
        uri2 = "";
        ponerFoto(imageView, null);
    }

    public void galeria(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, RESULTADO_GALERIA);
    }

    protected void ponerFoto(ImageView imageView, String uri) {
        if (uri != null) {
            imageView.setImageBitmap(reduceBitmap(this, uri, 1024, 1024));
        } else {
            imageView.setImageBitmap(null);
        }
    }

    public static Bitmap reduceBitmap(Context contexto, String uri, int maxAncho, int maxAlto) {
        try {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(contexto.getContentResolver()
                    .openInputStream(Uri.parse(uri)), null, options);
            options.inSampleSize = (int) Math.max(
                    Math.ceil(options.outWidth / maxAncho),
                    Math.ceil(options.outHeight / maxAlto));
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeStream(contexto.getContentResolver() .openInputStream(Uri.parse(uri)), null, options);
        } catch (FileNotFoundException e) {
            Toast.makeText(contexto, "Fichero/recurso no encontrado",
                    Toast.LENGTH_LONG).show(); e.printStackTrace();
            return null; }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULTADO_GALERIA && resultCode == Activity.RESULT_OK) {
            uri2 =  data.getDataString();
            ponerFoto(imageView, uri2);
        } else if (requestCode == RESULTADO_FOTO && resultCode == Activity.RESULT_OK &&  uriFoto!=null) {
            uri2 = uriFoto.toString();
            ponerFoto(imageView, uri2);
        }
    }

    public void rellenarInfo(){
        usuario = ConsultaBD.infoUser(id);
        if(usuario != null){
            if (!usuario.getNombre().equals("") && usuario.getNombre()!= null){
                nombre.setText(usuario.getNombre());
            }

            if (!usuario.getApellidos().equals("") && usuario.getApellidos()!= null){
                apellidos.setText(usuario.getApellidos());
            }

            if (!usuario.getUsername().equals("") && usuario.getUsername()!= null){
                username.setText(usuario.getUsername());
            }

            if (!usuario.getWeb().equals("") && usuario.getWeb()!= null){
                direccion.setText(usuario.getWeb());
            }

            if (!usuario.getLugar().equals("") && usuario.getLugar()!= null){
                localidad.setText(usuario.getLugar());
            }

            if (!usuario.getPais().equals("") && usuario.getPais()!= null){
                pais.setText(usuario.getPais());
            }

            if (usuario.getTelefono()!=0){
                telefono.setText(String.valueOf(usuario.getTelefono()));
            }

            //poner la foto
            if(!usuario.getPhoto().equals("") && usuario.getPhoto()!= null){

                ponerFoto(imageView,usuario.getPhoto());
            }

        }
    }
    private void getInfo(){
        if (usuario==null){
            usuario = new Usuario();
        }

        usuario.setNombre(""+nombre.getText());
        usuario.setApellidos(""+apellidos.getText());
        usuario.setLugar(""+localidad.getText());
        usuario.setPais(""+pais.getText());
        usuario.setUsername(""+username.getText());
        usuario.setWeb(""+direccion.getText());
        int tel = 0;
        try {
           tel = Integer.parseInt(telefono.getText().toString());
        }catch (Exception e){
            Log.e("error","parse "+telefono.getText().toString());
        }
        usuario.setTelefono(0+tel);

        if(uri2!=null){
            usuario.setPhoto(uri2);
        }


    }

}
