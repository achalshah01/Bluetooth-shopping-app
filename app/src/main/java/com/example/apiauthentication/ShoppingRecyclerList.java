package com.example.apiauthentication;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.coresdk.common.requirements.SystemRequirementsChecker;
import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.recognition.packets.Beacon;
import com.estimote.coresdk.service.BeaconManager;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.estimote.coresdk.common.config.EstimoteSDK.getApplicationContext;


public class ShoppingRecyclerList extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    String x_auth_token;
    SharedPreferences sharedpreferences;
    ArrayList<ItemPojo>ItemList=new ArrayList<ItemPojo>();
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private BeaconManager beaconManager;
    private BeaconRegion region;
    TextView beaconMes;
    String imChanged="nothing",beaconChanged="noUpdate";
    Long currTime= Long.valueOf(0);
    Long updatedTime= Long.valueOf(1);
    public ShoppingRecyclerList() {
        // Required empty public constructor
    }


    public static ShoppingRecyclerList newInstance(String param1, String param2) {
        ShoppingRecyclerList fragment = new ShoppingRecyclerList();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        beaconManager = new BeaconManager(getContext());
        region = new BeaconRegion("ranged region",
                UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);




      View view=inflater.inflate(R.layout.fragment_shopping_recycler_list, container, false);
        getActivity().setTitle("SpencerPlex-Online Groceries");
      //  beaconManager = new BeaconManager(getContext());
//        region = new BeaconRegion("ranged region",
//                UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);
        recyclerView = view.findViewById(R.id.container);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
         beaconMes= view.findViewById(R.id.beaconMes);
        recyclerView.setLayoutManager(layoutManager);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        x_auth_token=preferences.getString("x-auth-token","");
        StringBuilder sb = new StringBuilder();
        sb.append("https://payment12345.herokuapp.com/api/listJson");
        final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        final OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(sb.toString())
                .header("Content-Type", "application/json")
                 .header("x-auth-token",x_auth_token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseData=response.body().string();
                try {
                    JSONObject responseList=new JSONObject(responseData);
                    JSONArray list =responseList.getJSONArray("results");
                    for(int i=0;i<list.length();i++){
                        JSONObject item=list.getJSONObject(i);
                        ItemPojo itemPojo=new ItemPojo();
                        itemPojo.setName(item.get("name").toString());
                        itemPojo.setDiscount(item.get("discount").toString());
                        itemPojo.setPhoto(item.get("photo").toString());
                        itemPojo.setPrice(item.get("price").toString());
                        itemPojo.setRegion(item.get("region").toString());
                          ItemList.add(itemPojo);
                    }
                    Log.d("DEMO123=",ItemList.toString());
                    // onComplete(Token);
                    client.dispatcher().executorService().shutdown();
                    Handler mainHandler = new Handler(Looper.getMainLooper());
 
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            beaconMes.setText("");

                            mAdapter = new ItemListAdapter(ItemList, x_auth_token);
                            recyclerView.setAdapter(mAdapter);
                            beaconListSelector(ItemList);
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        return view;
    }

    private void beaconListSelector(final ArrayList<ItemPojo> itemList) {
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startMonitoring(new BeaconRegion(
                        "monitored region",
                        UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"),
                        null, null));
            }
        });

        beaconManager.setMonitoringListener(new BeaconManager.BeaconMonitoringListener() {
            String regionCat=null;
            @Override
            public void onEnteredRegion(BeaconRegion region, List<Beacon> beacons) {
                if (!beacons.isEmpty()) {

//                    List<Beacon> beaconsSorted=new ArrayList<>() ;
//                    beaconsSorted=beacons;
//                    Collections.sort(beaconsSorted, new Comparator<Beacon>(){
//                        @Override
//                        public int compare(Beacon o1, Beacon o2) {
//                            return Integer.valueOf(o1.getMeasuredPower()).compareTo(Integer.valueOf(o2.getMeasuredPower())); // To compare string values
//                        }
//                    });
//                    beaconMes.setText("");
//                    Beacon nearestBeacon = beaconsSorted.get(0);
//                    //  List<String> places = placesNearBeacon(nearestBeacon);
//                    // TODO: update the UI here
//                    Log.d("Airport", "uuid: " + nearestBeacon.getUniqueKey()+" major="+nearestBeacon.getMajor()+" minor="+nearestBeacon.getMinor());
///// grocery
//                    if( nearestBeacon.getMinor()==58596){
//                        regionCat="grocery";
//
//                    }
//////produce
//                    if( nearestBeacon.getMinor()==25324){
//                        regionCat="produce";
//                    }
///////lifestyle
//                    if(nearestBeacon.getMinor()==5896){
//                        regionCat="grocery";
//                    }
//                    ArrayList<ItemPojo>beaconItemList=new ArrayList<ItemPojo>();
//                    for(int m=0;m<itemList.size();m++){
//                        if(itemList.get(m).getRegion().contentEquals(regionCat)){
//                            beaconItemList.add(itemList.get(m));
//                        }
//                    }

//                    mAdapter=new ItemListAdapter(beaconItemList,x_auth_token);
//                    recyclerView.setAdapter(mAdapter);
               }
            }
            @Override
            public void onExitedRegion(BeaconRegion region) {
                //beaconMes.setText("No Iteams ,Please walk into the store to view Items");
                Toast.makeText(getContext(),"visit again ", Toast.LENGTH_SHORT).show();
                ArrayList<ItemPojo>beaconItemList=new ArrayList<ItemPojo>();
                mAdapter=new ItemListAdapter(beaconItemList,x_auth_token);
                recyclerView.setAdapter(mAdapter);
            }
        });


        beaconManager.setRangingListener(new BeaconManager.BeaconRangingListener() {
            @Override
            public void onBeaconsDiscovered(BeaconRegion beaconRegion, List<Beacon> beacons) {
                String region=null;

                if (!beacons.isEmpty()) {
                    currTime= System.currentTimeMillis();
                    currTime=currTime/1000;
                    Log.d("currentTime", String.valueOf(currTime));
                    Beacon nearestBeacon = beacons.get(0);
                    //  List<String> places = placesNearBeacon(nearestBeacon);
                    // TODO: update the UI here
                    Log.d("Airport", "uuid: " + nearestBeacon.getUniqueKey()+" major="+nearestBeacon.getMajor()+" minor="+nearestBeacon.getMinor());
/// grocery
                    if( beacons.get(0).getMinor()==58596){
                       region="grocery";
                           imChanged = region;
                        //imChanged=region;

                    }
////produce
                    if( beacons.get(0).getMinor()==25324){
                        region="produce";
                        imChanged=region;
                    }
/////lifestyle
                    if( beacons.get(0).getMinor()==5896){
                        region="grocery";
                        imChanged=region;
                    }

                    if(!beaconChanged.contentEquals(imChanged)) {
                        beaconChanged=imChanged;
                        updatedTime= System.currentTimeMillis();
                        updatedTime=updatedTime/1000;
                        Log.d("updatedTime", String.valueOf(updatedTime));
                        Log.d("updatedTime+5", String.valueOf(updatedTime+5));
                        ArrayList<ItemPojo> beaconItemList = new ArrayList<ItemPojo>();
                        for (int m = 0; m < itemList.size(); m++) {
                            if (itemList.get(m).getRegion().contentEquals(region)) {
                                beaconItemList.add(itemList.get(m));
                            }
                        }
                        if(updatedTime+1>currTime) {
                            Handler handler = new Handler();
                            Log.d("waiting", "waiting");
                            String finalRegion = region;
                            handler.postDelayed(new Runnable() {
                                public void run() {
                                    beaconMes.setText("");
                                    Log.d("waitinginse ", "5 second waiting");
                                    mAdapter = new ItemListAdapter(beaconItemList, x_auth_token);
                                    recyclerView.setAdapter(mAdapter);
                                    Toast.makeText(getContext(),"Do you like buy some "+beaconChanged+" products", Toast.LENGTH_SHORT).show();

                                }
                            }, 2500);
                        }else{
                            mAdapter = new ItemListAdapter(beaconItemList, x_auth_token);
                            recyclerView.setAdapter(mAdapter);
                            Toast.makeText(getContext(),"Do you like buy some "+beaconChanged+" products", Toast.LENGTH_SHORT).show();

                        }

                    }
                }
                else{
                    ArrayList<ItemPojo> beaconItemList = new ArrayList<ItemPojo>();
                    mAdapter = new ItemListAdapter(itemList, x_auth_token);
                    recyclerView.setAdapter(mAdapter);
                    Toast.makeText(getContext(),"Walk around to filter", Toast.LENGTH_SHORT).show();

                    // beaconMes.setText("No Iteams ,Please walk into the store to view Items");

                }

            }

            private void yourMethod(String region) {
                Log.d("demo","i ma witing here");
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        SystemRequirementsChecker.checkWithDefaultDialogs((Activity) getContext());

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(region);
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.logout:
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("x-auth-token","");
                editor.putString("email","");
                editor.putString("payToken","");
                editor.putString("payment","");
                editor.apply();
                getActivity().recreate();
                return true;
            case R.id.buying:
               getActivity(). getSupportFragmentManager().beginTransaction().addToBackStack("cart")
                        .replace(R.id.container, new Cart())
                        .commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
