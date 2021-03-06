package org.masterandroid.wander;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Registro extends AppCompatActivity {
    private EditText email;
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        ConsultaBD.inicializaBD(this);

        email = (EditText) findViewById(R.id.email);

        Bundle extras = getIntent().getExtras();
        String email_s = extras.getString("email");
        email.setText(email_s);
    }

    /*
    * Método para crear un usuario.
    * Comprueba si el usuario existe, después intenta crearlo y le devuelve a la pantalla de inicio para que ingrese con sus credenciales
    * */
    public void registrarUsuario (View view){
        EditText contraseña = (EditText) findViewById(R.id.contraseña);
        EditText nombre = (EditText) findViewById(R.id.nombre);
        EditText apellido = (EditText) findViewById(R.id.apellido);
        EditText contraseña2 = (EditText) findViewById(R.id.contraseña2);

        if (validate(email.getText().toString())){
            if (!checkEmpty(contraseña)) {
                if(contraseña.getText().toString().equals(contraseña2.getText().toString())){
                    String email_s = email.getText().toString();
                    String contraseña_s = contraseña.getText().toString();
                    String nombre_s = nombre.getText().toString();
                    String apellido_s = apellido.getText().toString();

                    ConsultaBD.inicializaBD(view.getContext());
                    if (ConsultaBD.emailUnico(email_s)){
                        if (ConsultaBD.newUser(email_s,contraseña_s,nombre_s,apellido_s)){
                            Intent intent = new Intent();
                            intent.putExtra("email", email.getText().toString());
                            setResult(RESULT_OK,intent);
                            finish();
                        }
                        else{
                            Toast.makeText(this, R.string.registrar_usuario_error, Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                    else{
                        email.setError(getString(R.string.registrar_usuario_existe));
                        email.requestFocus();
                    }

                    Transition slide = TransitionInflater.from(this).inflateTransition(R.transition.transition_slide);
                    getWindow().setExitTransition(slide);
                }else{
                    contraseña2.requestFocus();
                    Toast.makeText(this, R.string.contrasenas_no_coinciden, Toast.LENGTH_SHORT).show();
                }
            }
        }else{
            email.setError(getString(R.string.email_no_valido));
            email.requestFocus();
        }



    }

    /*
    * Comprueba si un campo está vacio, en caso de estarlo le asigna un mensaje de error
    * */
    public boolean checkEmpty(EditText input){
        if(TextUtils.isEmpty(input.getText().toString())){
            //input.setError(getString(R.string.campo_vacio_error));
            Toast.makeText(this, R.string.no_hay_contraseña, Toast.LENGTH_SHORT).show();
            input.requestFocus();
            return true;
        }
        return false;
    }
    public static boolean validate(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(emailStr);
        return matcher.find();
    }

}
