package com.example.apiauthentication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;
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

public class CartAdapter  extends RecyclerView.Adapter<CartAdapter.ViewHolder>  {
    ArrayList<ItemPojo> itemArrayList=new ArrayList<>();
    String x_auth_token;
    Context context;
    Double Cost;
    Context Fcontext;
    View Fview;



    public CartAdapter(ArrayList<ItemPojo> cartList, String x_auth_token, double cost, View view) {
        this.itemArrayList = cartList;
        this.x_auth_token = x_auth_token;
        this.Cost=cost;
        this.Fcontext=context;
        this.Fview=view;
    }

    @NonNull
    @Override
    public CartAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cart_adapter_item, parent, false);
        CartAdapter.ViewHolder viewHolder = new CartAdapter.ViewHolder(view);
        context=parent.getContext();
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull final CartAdapter.ViewHolder holder, final int position) {


        Float price= Float.valueOf((itemArrayList.get(position).Price));
        int no= Integer.parseInt(itemArrayList.get(position).Count);
        final float TotalItemAmount=price*no;

        holder.Title.setText(itemArrayList.get(position).Name);
        holder.ItemPrice.setText(itemArrayList.get(position).Price+"$");
        holder.Quantity.setText("X "+itemArrayList.get(position).Count );
        holder.CartPrice.setText(String.format("%.2f", TotalItemAmount)+"$");
        //int id=context.getResources().getIdentifier(itemArrayList.get(position).Photo,"drawable",context.getPackageName());
        holder.ItemImage.setImageBitmap(getImage(itemArrayList.get(position).Photo));
        holder.Add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int pos=position;
                ViewHolder holder1=holder;
                int c= Integer.parseInt(itemArrayList.get(position).Count) +1;
                final Double price= Double.valueOf((itemArrayList.get(pos)).Price);

                Cost=Cost+price;
                holder1.TotalAmount.setText(String.format("%.2f", Cost) +"$");
                itemArrayList.get(pos).setCount(String.valueOf(c));
              //  itemArrayList.get(pos).set(String.valueOf(c));
                notifyItemChanged(position);


                final String ItemName=itemArrayList.get(position).Name;
                StringBuilder sb = new StringBuilder();
                sb.append("https://payment12345.herokuapp.com/api/listJson");
                final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                final OkHttpClient client = new OkHttpClient();
                JSONObject postdata=new JSONObject();
                try {
                    postdata.put ("image",itemArrayList.get(pos).Photo);
                    postdata.put ("name",itemArrayList.get(pos).Name);
                    postdata.put (	"price", (itemArrayList.get(pos)).Price);

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
                        final String responseData=response.body().string();
                        Handler mainHandler = new Handler(Looper.getMainLooper());

                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, responseData+" "+ItemName+" added to cart", Toast.LENGTH_SHORT).show();


                                //notifyDataSetChanged();
                            }
                        });
                    }
                });
            }
        });



        holder.Minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int pos=position;
                final ViewHolder holder1=holder;
                final int c= Integer.parseInt(itemArrayList.get(position).Count) -1;
                final Double price= Double.valueOf((itemArrayList.get(pos)).Price);
                 Log.d("deletion---c", String.valueOf(c));
                Log.d("deletion---pos", String.valueOf(pos));
                Cost=Cost-price;
                holder1.TotalAmount.setText(String.format("%.2f", Cost) +"$");

                final String ItemName=itemArrayList.get(position).Name;
                StringBuilder sb = new StringBuilder();
                sb.append("https://payment12345.herokuapp.com/api/listJson/delete");
                final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                final OkHttpClient client = new OkHttpClient();
                JSONObject postdata=new JSONObject();
                try {
                    postdata.put ("image",itemArrayList.get(pos).Photo);
                    postdata.put ("name",itemArrayList.get(pos).Name);
                    postdata.put (	"price", (itemArrayList.get(pos)).Price);

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
                        final String responseData=response.body().string();
                        Handler mainHandler = new Handler(Looper.getMainLooper());

                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {

                                if(c == 0){
                                    itemArrayList.remove(pos);
                                    Toast.makeText(context, ItemName+" Deleted from cart", Toast.LENGTH_SHORT).show();
                                    if(itemArrayList.size()==0){
                                        holder.Messasge.setText("Cart is empty !!!");
                                        holder.TotalAmount.setText(0+"$");

                                    }
                                    notifyDataSetChanged();
                                }else {

                                    itemArrayList.get(pos).setCount(String.valueOf(c));
                                    Toast.makeText(context, "One "+ItemName+" Deleted", Toast.LENGTH_SHORT).show();
                                    notifyItemChanged(position);

                                }
                                //notifyDataSetChanged();
                            }
                        });
                    }
                });
            }
        });


    }
    public Bitmap getImage(String name){
        Log.d("demo123",name);
        switch (name){
            case "pineapple": return BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.pineapple);
            case "croissants": return BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.croissants);
            case "jellybeans": return BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.jellybeans);
            case "oranges": return BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.oranges);
            case "scotchbritesponges": return BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.scotchbritesponges);
            case "lettuce": return BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.lettuce);
            case "cocacola": return BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.cocacola);
            case "gatorade": return BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.gatorade);
        }
        return  BitmapFactory.decodeResource(context.getResources(),
                R.drawable.grocery);
    }
    @Override
    public int getItemCount() {
        return itemArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView Title;
        TextView Quantity;
        TextView ItemPrice;
        TextView CartPrice;
        ImageView ItemImage;
        Button Add,Minus;
        TextView Messasge,TotalAmount;
        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            Title = itemView.findViewById(R.id.CartTitle);
            CartPrice = itemView.findViewById(R.id.Cartmount);
            Quantity = itemView.findViewById(R.id.quantity);
            ItemPrice = itemView.findViewById(R.id.price);
            ItemImage = itemView.findViewById(R.id.CartImage);
            Add = itemView.findViewById(R.id.add);
            Minus = itemView.findViewById(R.id.minus);

            TotalAmount= Fview.findViewById(R.id.TotalAmount);
            Messasge=Fview.findViewById(R.id.message);

        }
    }

}
