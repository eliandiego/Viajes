package org.masterandroid.wander;

import android.app.ActivityOptions;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.mxn.soul.flowingdrawer_core.ElasticDrawer;
import com.mxn.soul.flowingdrawer_core.FlowingDrawer;

import org.json.JSONException;
import org.json.JSONObject;

public class ListaItinerariosActivity extends AppCompatActivity implements AdaptadorItinerarios.OnItemClickListener {
    private RecyclerView recyclerViewClientes;
    public AdaptadorItinerarios adaptador;
    private RecyclerView.LayoutManager lManager;
    private SharedPreferences pref;

    private String nombreItinerario = "";
    private FlowingDrawer mDrawer;

    private ItemTouchHelper mItemTouchHelper;

    //RateApp
    private RateApp rateApp;
    //Shared preference storage
    private SharedPreferenceStorage spStorage;

    //Anuncios
    private AdView adView;
    private InterstitialAd interstitialAd;
    private String ID_BLOQUE_ANUNCIOS_INTERSTICIAL;
    private String ID_INICIALIZADOR_ADS;
    private boolean showInterticial=false;

    //Clases tipo sigleton
    private ApplicationClass app;

    //InAppBilling
    private IInAppBillingService serviceBilling;
    private final String ID_ARTICULO = "org.masterandroid.wander.quitaranuncios";
    private final int INAPP_BILLING = 1;
    private final String developerPayLoad = "clave de seguridad";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_itinerarios);

        app = (ApplicationClass) getApplication();

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        final int id = pref.getInt("id", -1);
        //Log.e("id_user en prefe"," "+id);
        if (id == -1) {
            this.finish();
        }

        //Shared prefereces storage (Esto seria mejor meterlo en la clase aplication e inicializarlo solo una vez)
        spStorage = new SharedPreferenceStorage(this);
        //Rate App
        rateApp = new RateApp(this, spStorage);


        //inicializo la base de datos, si no existe la crea
        ConsultaBD.inicializaBD(this);

        recyclerViewClientes = (RecyclerView) findViewById(R.id.reciclador);
        recyclerViewClientes.setHasFixedSize(true);

        // Usar un administrador para LinearLayout
        lManager = new LinearLayoutManager(this);
        recyclerViewClientes.setLayoutManager(lManager);

        //Inicializar los elementos
        listaitinerarios();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageView imagen = findViewById(R.id.aprendizaje);
                imagen.setVisibility(View.GONE);
                TextView b_entendido = findViewById(R.id.entendido);
                b_entendido.setVisibility(View.GONE);
                /*
                String confirmar = getString(R.string.confirmar);
                String cancelar = getString(R.string.cancelar);
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle(getString(R.string.nombre_itinerario));
                // Set up the input
                final EditText input = new EditText(view.getContext());
                //input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                builder.setView(input);
                // Set up the buttons
                builder.setPositiveButton(confirmar, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        nombreItinerario = input.getText().toString();
                        if (!nombreItinerario.isEmpty()) {
                            int id = pref.getInt("id", 0);
                            ConsultaBD.newRoute(id, nombreItinerario);
                            listaitinerarios();
                        }
                    }
                });
                builder.setNegativeButton(cancelar, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();*/

                Intent intent = new Intent(ListaItinerariosActivity.this, CrearItinerario.class);
                intent.putExtra("id", (long)id);
                startActivityForResult(intent,1694);

                rateApp.addOneRatePoint();

                if (interstitialAd.isLoaded()) {
                    interstitialAd.show();
                }
            }
        });

        // Toolbar
        Toolbar toolbar = findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        final Intent intent1 = new Intent(this, ListaItinerariosActivity.class);
        final Intent intent2 = new Intent(this, InicioSesionActivity.class);
        NavigationView navigationView = (NavigationView) findViewById(R.id.vNavigation);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @SuppressWarnings("StatementWithEmptyBody")
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_itinerarios) {
                    if (!this.getClass().equals(ListaItinerariosActivity.class)) {
                        startActivity(intent1);
                    }
                } else if (id == R.id.nav_preferencias) {
                    lanzarPreferencias(null);
                    return true;
                } else if (id == R.id.nav_salir) {
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putInt("id", 0);
                    editor.putBoolean("rememberMe", false);
                    editor.commit();
                    startActivity(intent2);
                    finish();
                } else if (id == R.id.nav_rate_us) {
                    rateUsBtn();
                    return true;
                }else if (id == R.id.nav_shareapp) {
                    shareAppBtn();
                    return true;
                }else if (id == R.id.nav_privacy) {
                    privacyPolicyBtn();
                    return true;
                }else if(id == R.id.user_menu){
                    Intent i = new Intent(ListaItinerariosActivity.this, PerfilUsuarioActivity.class);
                    startActivity(i);
                }else if (id == R.id.nav_quitar_anuncios) {
                    comprarQuitarAds();
                    return true;
                }


                mDrawer.closeMenu();
                return true;
            }
        });
        mDrawer = (FlowingDrawer) findViewById(R.id.drawerlayout);
        mDrawer.setTouchMode(ElasticDrawer.TOUCH_MODE_BEZEL);
        toolbar.setNavigationIcon(R.drawable.ic_menu_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawer.toggleMenu();
            }
        });

        Transition lista_enter = TransitionInflater.from(this).inflateTransition(R.transition.transition_explode);
        getWindow().setEnterTransition(lista_enter);

        //Anuncios:
        ID_BLOQUE_ANUNCIOS_INTERSTICIAL = getString(R.string.ads_intersticial_id_test);
        ID_INICIALIZADOR_ADS = getString(R.string.ads_initialize_test);

        MobileAds.initialize(this, ID_INICIALIZADOR_ADS);

        adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(ID_BLOQUE_ANUNCIOS_INTERSTICIAL);
        interstitialAd.loadAd(new AdRequest.Builder().build());
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                interstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });
    }


    public void listaitinerarios() {
        int id = pref.getInt("id", 0);
        //Obtenemos el cursor con todas las rutas del usuario
        Cursor c = ConsultaBD.listadoItinerarios(id);

        //creamos el adaptador
        adaptador = new AdaptadorItinerarios(this, c, this);
        //Esto seria para el caso de que no existireran rutas para este usuario
        // emptyview seria lo que se mostraria en una lista vacia
        if (adaptador.getItemCount() == 0) {
            //emptyview.setVisibility(View.VISIBLE);
            recyclerViewClientes.setVisibility(View.GONE);
        } else {
            //emptyview.setVisibility(View.GONE);
            recyclerViewClientes.setVisibility(View.VISIBLE);
        }

        //Implementamos la funcionalidad del longClick
        /*adaptador.setOnItemLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(final View v) {
                final int posicion = recyclerViewClientes.getChildAdapterPosition(v);
                final long id = adaptador.getId(posicion);
                AlertDialog.Builder menu = new AlertDialog.Builder(ListaItinerariosActivity.this);
                CharSequence[] opciones = {getString(R.string.abrir),getString(R.string.eliminar), getString(R.string.visitado)};
                menu.setItems(opciones, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int opcion) {
                        switch (opcion) {
                            case 0: //Abrir
                                Intent intent = new Intent(ListaItinerariosActivity.this, ListaPuntosInteresActivity.class);
                                intent.putExtra("id", id);
                                startActivity(intent);
                                break;
                            case 1: //Borrar
                                String confirmar = getString(R.string.confirmar);
                                String cancelar = getString(R.string.cancelar);
                                String mensaje = getString(R.string.borrar_itinerario_pregunta);
                                new AlertDialog.Builder(ListaItinerariosActivity.this)
                                        .setTitle(getString(R.string.borrar_itinerario))
                                        .setMessage(mensaje)
                                        .setPositiveButton(confirmar, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                ConsultaBD.deleteRoute((int) id);
                                                listaitinerarios();

                                            }
                                        })
                                        .setNegativeButton(cancelar, null)
                                        .show();

                                break;
                            case 2: //Marcar como visto
                                ConsultaBD.changeCheck((int) id, true);
                                listaitinerarios();
                                break;


                        }
                    }
                });
                menu.create().show();
                return true;
            }
        });*/


        //rellenamos el reciclerview
        recyclerViewClientes.setAdapter(adaptador);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adaptador);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerViewClientes);
    }

    //accion de pulsar sobre un elemento de la lista
    @Override
    public void onClick(AdaptadorItinerarios.ViewHolder holder, long id) {
        Intent intent = new Intent(this, ListaPuntosInteresActivity.class);
        intent.putExtra("id", id);
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
    }


    @Override
    public void onBackPressed() {
        if (mDrawer.isMenuVisible()) {
            mDrawer.closeMenu();
        } else {
            super.onBackPressed();
        }
    }


    public void lanzarPreferencias(View view) {
        Intent i = new Intent(this, PreferenciasActivity.class);
        startActivity(i);
    }

    private void shareAppBtn(){
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, getPackageName());
        String sAux = "http://play.google.com/store/apps/details?id=" + getPackageName();
        i.putExtra(Intent.EXTRA_TEXT, sAux);
        startActivity(Intent.createChooser(i, "choose one"));
    }

    private void privacyPolicyBtn(){
        Uri uri2 = Uri.parse("https://drive.google.com/open?id=1rRXJLMj4ixMvFJNHAiYIYHp5sU2Xk2I9");
        Intent intent2 = new Intent(Intent.ACTION_VIEW, uri2);
        startActivity(intent2);
    }

    private void rateUsBtn(){
        rateApp.openAppStoreToRate(ListaItinerariosActivity.this);
    }

    public void ententdido(View view){
        ImageView imagen = findViewById(R.id.aprendizaje);
        imagen.setVisibility(View.GONE);
        TextView b_entendido = findViewById(R.id.entendido);
        b_entendido.setVisibility(View.GONE);
    }

    public void abrePrimeraVez(){
        SharedPreferences sp = getSharedPreferences("mispreferencias", 0);
        boolean primerAcceso = sp.getBoolean("abrePrimeraVez", true);
        if (primerAcceso) {
            ImageView imagen = findViewById(R.id.aprendizaje);
            imagen.setVisibility(View.VISIBLE);
            TextView b_entendido = findViewById(R.id.entendido);
            b_entendido.setVisibility(View.VISIBLE);

            SharedPreferences.Editor e = sp.edit();
            e.putBoolean("abrePrimeraVez", false).commit();
        }
    }

    private void setAds(Boolean adsEnabled) {
        if (adsEnabled) {
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
            interstitialAd = new InterstitialAd(this);
            interstitialAd.setAdUnitId(ID_BLOQUE_ANUNCIOS_INTERSTICIAL);
            interstitialAd.loadAd(new AdRequest.Builder().build());
            interstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    interstitialAd.loadAd(new AdRequest.Builder().build());
                }
            });
            showInterticial = true;
        } else {
            showInterticial = false;
            adView.setVisibility(View.GONE);
        }
    }

    public void comprarQuitarAds() {
        if (serviceBilling != null) {
            Bundle buyIntentBundle = null;
            try {
                buyIntentBundle = serviceBilling.getBuyIntent(3, getPackageName(), ID_ARTICULO, "inapp", developerPayLoad);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
            try {
                if (pendingIntent != null) {
                    startIntentSenderForResult(pendingIntent.getIntentSender(), INAPP_BILLING, new Intent(), 0, 0, 0);
                }
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "InApp Billing service not available", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case INAPP_BILLING:
                {
                int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
                String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
                String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");
                if (resultCode == RESULT_OK) {
                    try {
                        JSONObject jo = new JSONObject(purchaseData);
                        String sku = jo.getString("productId");
                        String developerPayload = jo.getString("developerPayload");
                        String purchaseToken = jo.getString("purchaseToken");
                        if (sku.equals(ID_ARTICULO)) {
                            Toast.makeText(this, "Compra completada", Toast.LENGTH_LONG).show();
                            setAds(false);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                }
                break;
            case 1694:
                if(resultCode == RESULT_OK){
                    listaitinerarios();
                }
        }
    }





}
