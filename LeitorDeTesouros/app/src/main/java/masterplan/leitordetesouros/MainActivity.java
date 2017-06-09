package masterplan.leitordetesouros;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.integration.android.*;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

import static android.util.Base64.decode;

public class MainActivity extends AppCompatActivity {

    AsyncHttpClient client;
    private Double latitude;
    private Double longitude;
    private LocationManager locationManager;
    private  Bitmap bmpTemp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bmpTemp = null;

        client = new AsyncHttpClient();
        final IntentIntegrator integrator = new IntentIntegrator(this);

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                integrator.initiateScan();
            }
        });

        latitude = new Double(0);
        longitude = new Double(0);
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = String.valueOf(locationManager.getBestProvider(criteria, true)).toString();
        locationManager.requestLocationUpdates(bestProvider, 1000, 0, locationListener);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            String re = scanResult.getContents();
            Toast toast = Toast.makeText(getApplicationContext(), re, Toast.LENGTH_SHORT);
            toast.show();

            Calendar c = Calendar.getInstance();
            c.add(Calendar.DATE, 1);
            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String formatted = format1.format(c.getTime());

            Map<String, String> params = new HashMap<String, String>();
            params.put("action", "get");
            params.put("ra", "15194");
            params.put("dt", formatted);
            params.put("lat", latitude.toString());
            params.put("lon", longitude.toString());
            params.put("id", re);

            SergioRestClient.post(new RequestParams(params), new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    try {
                        String resposta = new String(responseBody);
                        JSONObject serverResp = new JSONObject(resposta);
                        Toast toast = Toast.makeText(getApplicationContext(), serverResp.getString("dica"), Toast.LENGTH_SHORT);
                        toast.show();

                        String imageDataString = serverResp.getString("image");
                        byte[] imageDataBytes = decode(imageDataString, Base64.DEFAULT);

                        bmpTemp = BitmapFactory.decodeByteArray(imageDataBytes, 0, imageDataBytes.length);
                        updateImage();
                    }
                    catch (Exception e){
                        Log.v("Deu ruim: ", e.getMessage());
                    }

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                }
            });
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putParcelable("BitmapImage", bmpTemp);
        super.onSaveInstanceState(savedInstanceState);
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState){
        bmpTemp = savedInstanceState.getParcelable("BitmapImage");
        ImageView image = (ImageView) findViewById(R.id.imageView);
        ViewTreeObserver vto = image.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                updateImage();
            }
        });
    }

    protected void updateImage(){
        if (bmpTemp != null){
            ImageView image = (ImageView) findViewById(R.id.imageView);
            image.setImageBitmap(Bitmap.createScaledBitmap(bmpTemp, image.getWidth(), image.getHeight(), false));
            image.requestLayout();
        }
    }
}