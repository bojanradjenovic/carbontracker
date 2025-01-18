package dev.radjenovic.carbontracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransportDataAdapter extends RecyclerView.Adapter<TransportDataAdapter.ViewHolder> {

    private List<TransportData> transportDataList;

    public TransportDataAdapter(List<TransportData> transportDataList) {
        this.transportDataList = transportDataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transport_data, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TransportData data = transportDataList.get(position);
        Date date = new Date(data.getTimestamp());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        holder.transportModeTextView.setText("Mode: " + data.getTransportMode());
        holder.distanceTextView.setText(String.format("Distance: %.2f km", data.getDistanceTraveled()));
        holder.co2TextView.setText(String.format("CO2: %.2f kg", data.getCo2Emissions()));
        holder.timestampTextView.setText(dateFormat.format(date));
    }

    @Override
    public int getItemCount() {
        return transportDataList.size();
    }

    public void updateData(List<TransportData> newData) {
        transportDataList = newData;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView transportModeTextView;
        TextView distanceTextView;
        TextView co2TextView;
        TextView timestampTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            transportModeTextView = itemView.findViewById(R.id.transportModeTextView);
            distanceTextView = itemView.findViewById(R.id.distanceTextView);
            co2TextView = itemView.findViewById(R.id.co2TextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
        }
    }
}
