package dev.radjenovic.carbontracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TransportDataAdapter extends RecyclerView.Adapter<TransportDataAdapter.ViewHolder> {

    private List<TransportData> transportDataList;

    public TransportDataAdapter(List<TransportData> transportDataList) {
        this.transportDataList = transportDataList;
    }

    public void updateData(List<TransportData> newData) {
        transportDataList = newData;
        notifyDataSetChanged();
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
        holder.transportModeTextView.setText(data.getTransportMode());
        holder.distanceTextView.setText(String.format(Locale.getDefault(), "%.2f km", data.getDistanceTraveled() / 1000));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        holder.timestampTextView.setText(sdf.format(data.getTimestamp()));
    }

    @Override
    public int getItemCount() {
        return transportDataList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView transportModeTextView;
        TextView distanceTextView;
        TextView timestampTextView;

        ViewHolder(View itemView) {
            super(itemView);
            transportModeTextView = itemView.findViewById(R.id.transportModeTextView);
            distanceTextView = itemView.findViewById(R.id.distanceTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
        }
    }
}
