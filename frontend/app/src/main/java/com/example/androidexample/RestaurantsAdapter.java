package com.example.androidexample;

import android.content.Context;
import android.view.*;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidexample.Restaurant;

import java.util.ArrayList;
import java.util.List;

public class RestaurantsAdapter extends RecyclerView.Adapter<RestaurantsAdapter.VH> {

    public interface OnRestaurantClick {
        void onRestaurantClick(Restaurant item);
    }

    private final List<Restaurant> items = new ArrayList<>();
    private final Context ctx;
    private final OnRestaurantClick cb;

    public RestaurantsAdapter(Context ctx, OnRestaurantClick cb) {
        this.ctx = ctx;
        this.cb = cb;
    }

    public void submit(List<Restaurant> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_restaurant, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        Restaurant r = items.get(pos);
        h.name.setText(r.name);
        h.addr.setText(r.address);
        h.price.setText(r.priceRange == null ? "" : r.priceRange);
        h.rating.setRating(Math.max(0, Math.min(5, r.rating)));
        h.itemView.setOnClickListener(v -> cb.onRestaurantClick(r));
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView name, addr, price;
        RatingBar rating;
        VH(@NonNull View v) {
            super(v);
            name = v.findViewById(R.id.tvName);
            addr = v.findViewById(R.id.tvAddress);
            price = v.findViewById(R.id.tvPriceRange);
            rating = v.findViewById(R.id.rbRatingDisplay);
        }
    }
}
