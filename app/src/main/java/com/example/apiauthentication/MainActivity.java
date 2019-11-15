package com.example.apiauthentication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.estimote.coresdk.common.requirements.SystemRequirementsChecker;
import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.recognition.packets.Beacon;
import com.estimote.coresdk.service.BeaconManager;
import com.stripe.android.Stripe;

import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements SignUp.OnFragmentInteractionListener,
        Login.OnFragmentInteractionListener,UserProfile.OnFragmentInteractionListener,ShoppingRecyclerList.OnFragmentInteractionListener,
Cart.OnFragmentInteractionListener{

    String x_auth_token;
    SharedPreferences sharedpreferences;
    private Stripe mStripe;;
    private BeaconManager beaconManager;
    private BeaconRegion region;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
         x_auth_token=preferences.getString("x-auth-token","");
        if(x_auth_token.contentEquals("")){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new Login())
                    .commit();
        }else

        getSupportFragmentManager().beginTransaction().addToBackStack("list")
                .replace(R.id.container, new ShoppingRecyclerList())
                .commit();


//        beaconManager = new BeaconManager(this);
//        region = new BeaconRegion("ranged region",
//                UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);

//        beaconManager.setRangingListener(new BeaconManager.BeaconRangingListener() {
//            @Override
//            public void onBeaconsDiscovered(BeaconRegion beaconRegion, List<Beacon> beacons) {
//                if (!beacons.isEmpty()) {
//                    Beacon nearestBeacon = beacons.get(0);
//                    //  List<String> places = placesNearBeacon(nearestBeacon);
//                    // TODO: update the UI here
//                    Log.d("Airport", "uuid: " + nearestBeacon.getUniqueKey()+" major="+nearestBeacon.getMajor()+" minor="+nearestBeacon.getMinor());
//                }
//            }
//        });

    }


    @Override
    public void changeToHomeFragment(String token, String email) {
        x_auth_token=token;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("x-auth-token",token);
        editor.putString("email",email);
        editor.putString("payToken","null");
        editor.putString("payment","true");

        editor.apply();
      //  Toast.makeText(getApplicationContext(), token, Toast.LENGTH_SHORT).show();
        getSupportFragmentManager().beginTransaction().addToBackStack("list")
                .replace(R.id.container, new ShoppingRecyclerList())
                .commit();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }


    @Override
    public void changeToHomeFragment(String token, String email, String payToken) {
        x_auth_token=token;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("x-auth-token",token);
        editor.putString("email",email);
        editor.putString("payToken",payToken);
        editor.putString("payment","true");
        editor.apply();
        //  Toast.makeText(getApplicationContext(), token, Toast.LENGTH_SHORT).show();
        getSupportFragmentManager().beginTransaction().addToBackStack("list")
                .replace(R.id.container, new ShoppingRecyclerList())
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SystemRequirementsChecker.checkWithDefaultDialogs(this);
    }
}
