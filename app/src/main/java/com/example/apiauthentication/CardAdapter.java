package com.example.apiauthentication;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> {
    ArrayList<CardPojo> cards;
    String payToken,x_auth_token;
    View fView;
    Double toatalAmount;

    public CardAdapter(ArrayList<CardPojo> cards, String payToken, View fView, String x_auth_token, Double toatalAmount) {
        this.cards = cards;
        this.payToken = payToken;
        this.fView=fView;
        this.x_auth_token=x_auth_token;
        this.toatalAmount=toatalAmount;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_list_adapter_iyem, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder,  int position) {
        holder.cardNo.setText(" **** **** **** "+cards.get(position).getLast4());
        holder.message.setText("");
        holder.date.setText(cards.get(position).getMonth()+" / "+cards.get(position).getYear());

        if(cards.get(position).getBrand().contentEquals("Visa")){
            holder.imageView.setImageResource(R.drawable.visa);
        }
        final int pos=position;

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                makePayment(payToken,cards.get(pos).getCardId());
            }
        });

        holder.deleteCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteCard(payToken,cards.get(pos).getCardId(),pos);
            }
        });

    }


    public void deleteCard(final String clientId, final String clientToken, final int pos){
        StringBuilder sb = new StringBuilder();
        sb.append("https://payment12345.herokuapp.com/api/stripe");
        final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        final OkHttpClient client = new OkHttpClient();
        JSONObject postdata=new JSONObject();
        try {
            postdata.put ("stripeToken",clientToken);
            postdata.put ("id",clientId);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body =RequestBody.create(JSON,postdata.toString());
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
                String responseData = response.body().string();
                Log.d("demo123===",responseData);
                client.dispatcher().executorService().shutdown();
                Handler mainHandler = new Handler(Looper.getMainLooper());
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        cards.remove(pos);
                        notifyDataSetChanged();

                    }
                });
            }
        });

    }

    public void makePayment(final String clientId, final String clientToken){
        StringBuilder sb = new StringBuilder();
        sb.append("https://payment12345.herokuapp.com/api/stripe/paymentAuth");
        final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        final OkHttpClient client = new OkHttpClient();
        JSONObject postdata=new JSONObject();
        try {
            postdata.put ("stripeToken",clientToken);
            postdata.put ("id",clientId);
            postdata.put ("amount",toatalAmount*100);

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
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(fView.getContext());
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("payToken",clientId).apply();
                        editor.putString("payment","false").apply();
                        Toast.makeText(fView.getContext(), "Payment Done", Toast.LENGTH_SHORT).show();
                        ///// pop back strack
                        ((FragmentActivity) fView.getContext()). getSupportFragmentManager().popBackStack();

                    }
                });
            }
        });

    }
    @Override
    public int getItemCount() {
        return cards.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView cardNo,message,date;
        CircleImageView imageView;
        ImageButton deleteCard;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardNo = itemView.findViewById(R.id.CardTitle);
            date = itemView.findViewById(R.id.date);
            deleteCard = itemView.findViewById(R.id.deleteCard);

            imageView =(CircleImageView) itemView.findViewById(R.id.CardImage);

            message = fView.findViewById(R.id.paymessage);

        }
    }
}
