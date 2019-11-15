package com.example.apiauthentication;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.SharedElementCallback;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.stripe.android.ApiResultCallback;
import com.stripe.android.PaymentIntentResult;
import com.stripe.android.Stripe;
import com.stripe.android.model.Card;
import com.stripe.android.model.ConfirmPaymentIntentParams;
import com.stripe.android.model.PaymentMethod;
import com.stripe.android.model.PaymentMethodCreateParams;
import com.stripe.android.model.Token;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class StripePayment extends Fragment {

Double ToatalAmount;
    public StripePayment(double s) {
        // Required empty public constructor
        this.ToatalAmount= s;
    }
    Button payment;
    private Stripe mStripe;;
    CardMultilineWidget cardMultilineWidget;
    PaymentMethodCreateParams.Card paymentMethodCreateParams;
    String ClientSecret;
    PaymentMethod.BillingDetails billingDetails;
    String x_auth_token,email,payToken,paymentFlag;
    TextView message;
    ArrayList<CardPojo>cards;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    View fview;
    CheckBox checkBox;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mStripe = new Stripe(getActivity().getApplicationContext(),
                "pk_test_sEQhYpC6SKZ2sbPsx0aoCZUU00v2sLLIRe");

         cards=new ArrayList<CardPojo>();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        x_auth_token=preferences.getString("x-auth-token","");
        email=preferences.getString("email","");
        payToken=preferences.getString("payToken","");
        paymentFlag=preferences.getString("payment","");




        View view=inflater.inflate(R.layout.fragment_stripe_payment, container, false);
        cardMultilineWidget=view.findViewById(R.id.card_widget);
       payment=view.findViewById(R.id.button);
       message=view.findViewById(R.id.paymessage);
        recyclerView = view.findViewById(R.id.cards);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        checkBox=view.findViewById(R.id.checkBox);

        if(payToken.contentEquals("null")){
           message.setText("No cards ,Please add card to proceed with payment");
       }else{

            Log.d("List123===",payToken);

           StringBuilder sb = new StringBuilder();
           sb.append("https://payment12345.herokuapp.com/api/stripe/listCard");
           final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
           final OkHttpClient client = new OkHttpClient();
           JSONObject postdata=new JSONObject();
           try {
               postdata.put ("id",payToken);

           } catch (JSONException e) {
               e.printStackTrace();
           }
           RequestBody body =RequestBody.create(JSON,postdata.toString());
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
                   Log.d("res123===", responseData);

                   try {
                                    JSONObject result =new JSONObject(responseData);
                                    JSONObject keys= result.getJSONObject("cards");
                       JSONArray data=keys.getJSONArray("data");
                       for(int i=0;i<data.length();i++) {
                           JSONObject cardDetail = data.getJSONObject(i);
                           CardPojo cardPojo = new CardPojo();
                           cardPojo.setBrand(cardDetail.getString("brand"));
                           cardPojo.setCardId(cardDetail.getString("id"));
                           cardPojo.setLast4(cardDetail.getString("last4"));
                           cardPojo.setMonth(cardDetail.getString("exp_month"));
                           cardPojo.setYear(cardDetail.getString("exp_year"));

                           Log.d("card1234==", cardPojo.getCardId());
                           cards.add(cardPojo);
                       }
                     //  Log.d("List123===", String.valueOf(cards.size()));

                       client.dispatcher().executorService().shutdown();
                       Handler mainHandler = new Handler(Looper.getMainLooper());
                       mainHandler.post(new Runnable() {
                           @Override
                           public void run() {
                               if(cards.size()>0) {
                                   mAdapter = new CardAdapter(cards, payToken, getView(),x_auth_token,ToatalAmount);
                                   recyclerView.setAdapter(mAdapter);
                               }
                              // TotalAmount.setText(Cost+"$");
                           }
                       });
                   } catch (JSONException e) {
                       e.printStackTrace();
                   }
               }
           });
       }

        payment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCardSaved();
               // Toast.makeText(getContext(), ToatalAmount.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }


    private void onCardSaved() {
        Card cardToSave = cardMultilineWidget.getCard();
        if (cardToSave != null) {
          tokenizeCard(cardToSave);
        }

    }
    
    public void  UpdateDbPayToken(final String clientId, final String clientToken){
        StringBuilder sb = new StringBuilder();
        sb.append("https://payment12345.herokuapp.com/api/users");
        final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        final OkHttpClient client = new OkHttpClient();
        JSONObject postdata=new JSONObject();
        try {
            postdata.put ("payToken",clientId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body =RequestBody.create(JSON,postdata.toString());
        Request request = new Request.Builder()
                .url(sb.toString())
                .header("Content-Type", "application/json")
                .header("x-auth-token",x_auth_token)
                .put(body)
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

                       makePayment(clientId,clientToken);

                    }
                });
            }
        });
        
    }

    /////////////////////////making pyment using payment auth

    public void makePayment(final String clientId, final String clientToken){
        StringBuilder sb = new StringBuilder();
        sb.append("https://payment12345.herokuapp.com/api/stripe/paymentAuth");
        final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        final OkHttpClient client = new OkHttpClient();
        JSONObject postdata=new JSONObject();
        try {
            postdata.put ("stripeToken",clientToken);
            postdata.put ("id",clientId);
            postdata.put ("amount",ToatalAmount*100);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body =RequestBody.create(JSON,postdata.toString());
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
                client.dispatcher().executorService().shutdown();
                Handler mainHandler = new Handler(Looper.getMainLooper());
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("payToken",clientId).apply();
                        editor.putString("payment","false").apply();
                        Toast.makeText(getContext(), "Payment Done", Toast.LENGTH_SHORT).show();
                        ///// pop back strack
                        ((FragmentActivity) getContext()). getSupportFragmentManager().popBackStack();

                    }
                });
            }
        });

    }



    private void tokenizeCard(@NonNull Card card) {
        mStripe.createToken(
                card,
                new ApiResultCallback<Token>() {
                    public void onSuccess(@NonNull Token token) {
                        Log.d("demo123qq==", token.getId());

                        ////////////////////creating a client
                        if (payToken.contentEquals("null") && checkBox.isChecked()) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("https://payment12345.herokuapp.com/api/stripe/customer");
                            final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                            final OkHttpClient client = new OkHttpClient();
                            JSONObject postdata = new JSONObject();
                            try {
                                postdata.put("email", email);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            RequestBody body = RequestBody.create(JSON, postdata.toString());
                            Request request = new Request.Builder()
                                    .url(sb.toString())
                                    .header("Content-Type", "application/json")
                                    .header("x-auth-token", x_auth_token)
                                    .post(body)
                                    .build();
                            client.newCall(request).enqueue(new Callback() {
                                @Override
                                public void onFailure(@NotNull Call call, @NotNull IOException e) {

                                }

                                @Override
                                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                    String responseData = response.body().string();
                                    String customerPayId = null, cardToken = null;
                                    try {
                                        JSONObject result = new JSONObject(responseData);
                                        JSONObject data = result.getJSONObject("id");
                                        customerPayId = data.getString("id");
                                        cardToken = data.getString("default_source");

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    client.dispatcher().executorService().shutdown();
                                    Handler mainHandler = new Handler(Looper.getMainLooper());
                                    final String finalCustomerPayId = customerPayId;
                                    final String finalcardToken = cardToken;

                                    mainHandler.post(new Runnable() {
                                        @Override
                                        public void run() {

                                            UpdateDbPayToken(finalCustomerPayId, finalcardToken);

                                        }
                                    });
                                }
                            });
                            ////// client is already present to jyst add card and do payment
                        } else if (!payToken.contentEquals("null") && checkBox.isChecked()) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("https://payment12345.herokuapp.com/api/stripe/saveCard");
                            final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                            final OkHttpClient client = new OkHttpClient();
                            JSONObject postdata = new JSONObject();
                            try {
                                postdata.put("stripeToken", token.getId());
                                postdata.put("id", payToken);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            RequestBody body = RequestBody.create(JSON, postdata.toString());
                            Request request = new Request.Builder()
                                    .url(sb.toString())
                                    .header("Content-Type", "application/json")
                                    .header("x-auth-token", x_auth_token)
                                    .post(body)

                                    .build();
                            client.newCall(request).enqueue(new Callback() {
                                @Override
                                public void onFailure(@NotNull Call call, @NotNull IOException e) {

                                }

                                @Override
                                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                    String responseData = response.body().string();
                                    String cardToken = null;
                                    try {
                                        JSONObject result = new JSONObject(responseData);
                                        JSONObject data = result.getJSONObject("card");
                                        cardToken = data.getString("id");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    client.dispatcher().executorService().shutdown();
                                    Handler mainHandler = new Handler(Looper.getMainLooper());
                                    final String finalCardToken = cardToken;
                                    mainHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            String finalcardToken = finalCardToken;
                                            makePayment(payToken, finalcardToken);

                                        }
                                    });
                                }
                            });
                        }
///just do payment
                        else {
                            StringBuilder sb = new StringBuilder();
                            sb.append("https://payment12345.herokuapp.com/api/stripe");
                            final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                            final OkHttpClient client = new OkHttpClient();
                            JSONObject postdata = new JSONObject();
                            try {
                                postdata.put("stripeToken", token.getId());
                                postdata.put("amount", ToatalAmount * 100);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            RequestBody body = RequestBody.create(JSON, postdata.toString());
                            Request request = new Request.Builder()
                                    .url(sb.toString())
                                    .header("Content-Type", "application/json")
                                    .header("x-auth-token", x_auth_token)
                                    .post(body)
                                    .build();
                            client.newCall(request).enqueue(new Callback() {
                                @Override
                                public void onFailure(@NotNull Call call, @NotNull IOException e) {

                                }

                                @Override
                                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                    final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putString("payment","false").apply();
                                    client.dispatcher().executorService().shutdown();
                                    Handler mainHandler = new Handler(Looper.getMainLooper());
                                    mainHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            ((FragmentActivity) getContext()). getSupportFragmentManager().popBackStack();

                                        }
                                    });
                                }
                            });

                        }

                    }

                    @Override
                    public void onError(@NonNull Exception e) {

                    }
                });
    }
}
