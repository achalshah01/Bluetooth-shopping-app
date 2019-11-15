package com.example.apiauthentication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.icu.text.CaseMap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

public class ItemListAdapter extends RecyclerView.Adapter<ItemListAdapter.ViewHolder> {
    ArrayList<ItemPojo> itemArrayList;
    String x_auth_token;
    Context context;

    public ItemListAdapter(ArrayList<ItemPojo> itemArrayList, String x_auth_token) {
        this.itemArrayList = itemArrayList;
        this.x_auth_token=x_auth_token;
    }

    @NonNull
    @Override
    public ItemListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.iteam, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        context=parent.getContext();
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull ItemListAdapter.ViewHolder holder, final int position) {
        String price=itemArrayList.get(position).Price;
        holder.Title.setText(itemArrayList.get(position).Name);
        holder.Region.setText(itemArrayList.get(position).Region);
        holder.Discount.setText("( Discount "+itemArrayList.get(position).Discount+"$ off)" );
        holder.Price.setText(itemArrayList.get(position).Price+"$");
        //int id=context.getResources().getIdentifier(itemArrayList.get(position).Photo,"drawable",context.getPackageName());
        holder.ItemImage.setImageBitmap(getImage(itemArrayList.get(position).Photo));
        holder.AddCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos=position;
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
//          default:return BitmapFactory.decodeResource(context.getResources(),
//                  R.drawable.grocery);
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
        TextView Region;
        TextView Discount;
        TextView Price;
        ImageView ItemImage;
        Button AddCart;
        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            Title = itemView.findViewById(R.id.title);
            Region = itemView.findViewById(R.id.region2);
            Discount = itemView.findViewById(R.id.discount);
            Price = itemView.findViewById(R.id.amount);
            AddCart = itemView.findViewById(R.id.addCart);
            ItemImage = itemView.findViewById(R.id.itemImage);


        }
    }
}
