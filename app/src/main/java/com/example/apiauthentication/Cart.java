package com.example.apiauthentication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.service.BeaconManager;
import com.stripe.android.model.Card;

import androidx.appcompat.app.AppCompatActivity;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.braintreepayments.api.dropin.DropInActivity;
import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.DropInResult;
import com.braintreepayments.api.dropin.utils.PaymentMethodType;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.Stripe;
import com.stripe.android.model.PaymentMethod;
import com.stripe.android.view.CardMultilineWidget;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;


public class Cart extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    String x_auth_token;
    SharedPreferences sharedpreferences;
    ArrayList<ItemPojo> CartList=new ArrayList<ItemPojo>();
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private OnFragmentInteractionListener mListener;
     TextView TotalAmount,Message;
     double Cost;
     Button Pay;
     String paymentStatus;
     View mView;

    SharedPreferences preferences;
    public Cart() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static Cart newInstance(String param1, String param2) {
        Cart fragment = new Cart();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        getActivity().setTitle("CheckOut");
         preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        paymentStatus=preferences.getString("payment","");
        x_auth_token=preferences.getString("x-auth-token","");

        // PaymentConfiguration.init(getActivity().getApplicationContext(),"pk_test_sEQhYpC6SKZ2sbPsx0aoCZUU00v2sLLIRe");


        final View view=inflater.inflate(R.layout.fragment_cart, container, false);
        mView=view;
        Cost=0;

       // mAdapter = new CartAdapter(CartList,x_auth_token);
        TotalAmount=view.findViewById(R.id.TotalAmount);
        Message=view.findViewById(R.id.message);

        if(paymentStatus.contentEquals("false")){
           // Toast.makeText(getActivity(), "paymentStatus inside="+paymentStatus, Toast.LENGTH_SHORT).show();

            CartList.clear();
//            mAdapter = new CartAdapter(CartList,x_auth_token,Cost,view);
//            recyclerView.setAdapter(mAdapter);
            SharedPreferences.Editor editor = preferences.edit();
           // editor.putString("payToken",clientId);
            editor.putString("payment","true").apply();
            TotalAmount.setText("0$");
            StringBuilder sb = new StringBuilder();
            sb.append("https://payment12345.herokuapp.com/api/listJson/deleteAll");
            final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            final OkHttpClient client = new OkHttpClient();
            RequestBody body =RequestBody.create(JSON,"");
            Request request = new Request.Builder()
                    .url(sb.toString())
                    .header("Content-Type", "application/json")
                    .header("x-auth-token",x_auth_token)
                    .delete(body)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {

                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    client.dispatcher().executorService().shutdown();
                    Handler mainHandler = new Handler(Looper.getMainLooper());

                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(CartList.size()>0){
                                Message.setText("");
                            }
                            mAdapter = new CartAdapter(CartList,x_auth_token,Cost,view);
                            recyclerView.setAdapter(mAdapter);
                            TotalAmount.setText(Cost+"$");
                            Message.setText("Cart is Empty!!");
                        }
                    });

                }
            });

        }
        if(CartList.size()<=0){
            Message.setText("Cart is Empty!!");
        }
        Pay=view.findViewById(R.id.pay);
        recyclerView = view.findViewById(R.id.containerCart);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        //SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        StringBuilder sb = new StringBuilder();
        sb.append("https://payment12345.herokuapp.com/api/listJson");
        final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        final OkHttpClient client = new OkHttpClient();
        RequestBody body =RequestBody.create(JSON,"");
        Request request = new Request.Builder()
                .url(sb.toString())
                .header("Content-Type", "application/json")
                .header("x-auth-token",x_auth_token)
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseData=response.body().string();
                try {
                    JSONArray list =new JSONArray(responseData);
                    for(int i=0;i<list.length();i++){
                        JSONObject item=list.getJSONObject(i);
                        ItemPojo itemPojo=new ItemPojo();
                        itemPojo.setName(item.get("name").toString());
                        itemPojo.setCount(item.get("count").toString());
                        itemPojo.setPhoto(item.get("image").toString());
                        itemPojo.setPrice(item.get("price").toString());
                        double p=Double.parseDouble(item.get("price").toString());
                        int c=Integer.parseInt(item.get("count").toString());
                        Cost=Cost+(p*c);
                        itemPojo.setTotalAmount(Cost);

                        CartList.add(itemPojo);
                    }
                    Log.d("DEMO123=",CartList.toString());
                    // onComplete(Token);
                    client.dispatcher().executorService().shutdown();
                    Handler mainHandler = new Handler(Looper.getMainLooper());

                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(CartList.size()>0){
                                Message.setText("");
                            }
                            mAdapter = new CartAdapter(CartList,x_auth_token,Cost,view);
                            recyclerView.setAdapter(mAdapter);
                            TotalAmount.setText(Cost+"$");
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        Pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
   if(TotalAmount.getText().toString().contentEquals("0.0$") || TotalAmount.getText().toString().contentEquals("0$")){
       Toast.makeText(getContext(), "Please add Iteams to Make Payment", Toast.LENGTH_SHORT).show();
   }else {

       getActivity().getSupportFragmentManager().beginTransaction().addToBackStack("cart")
               .replace(R.id.container, new StripePayment(Cost))
               .commit();
       // onCardSaved();


   }
//

            }
        });

        return view;
    }





    @Override
    public void onStart() {
        super.onStart();
//        if(paymentStatus.contentEquals("false")){
//            CartList.clear();
//            SharedPreferences.Editor editor = preferences.edit();
//            // editor.putString("payToken",clientId);
//            editor.putString("payment","true");
//            mAdapter = new CartAdapter(CartList,x_auth_token,Cost,mView);
//            recyclerView.setAdapter(mAdapter);
//            TotalAmount.setText("0$");
//        }
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
        inflater.inflate(R.menu.menucart, menu);
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
                editor.apply();
                getActivity().recreate();
                return true;
            case R.id.home:
                getActivity(). getSupportFragmentManager().popBackStack();
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
