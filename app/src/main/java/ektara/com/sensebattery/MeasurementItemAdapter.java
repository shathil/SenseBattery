package ektara.com.sensebattery;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Mesbahul Islam on 1/15/2017.
 */

public class MeasurementItemAdapter extends RecyclerView.Adapter<MeasurementItemAdapter.ItemViewHolder> {
    private Context context;
    private List<MeasurementItem> items;

    public MeasurementItemAdapter(Context context, List<MeasurementItem> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_card_view_item, parent, false);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        MeasurementItem item = items.get(position);

        holder.cardTitle.setText(item.getTitle());
        holder.cardValue.setText(item.getValue());
        holder.cardSubtitle.setText(item.getSubTitle());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.card_title)
        TextView cardTitle;
        @BindView(R.id.card_value)
        TextView cardValue;
        @BindView(R.id.card_subtitle)
        TextView cardSubtitle;

        public ItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
