package fi.jamk.l3329.stockpile;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by peter on 6.11.2017.
 */

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    private List<Item> items;
    private Context mContext;
    private FragmentManager fragmentManager;
    @NonNull
    private OnItemCheckListener onItemCheckListener;

    public ItemAdapter(List<Item> items, Context mContext, @NonNull OnItemCheckListener onItemCheckListener, FragmentManager fragmentManager) {
        this.items = items;
        this.mContext = mContext;
        this.onItemCheckListener = onItemCheckListener;
        this.fragmentManager = fragmentManager;
    }

//    @Override
//    public void onDialogPositiveClick(DialogFragment dialogFragment, int position) {
//        onItemCheckListener.onLongItemClick(dialogFragment,position);
//    }

    interface OnItemCheckListener {
        void onItemCheck(int position, boolean isChecked);
//        void onLongItemClick(DialogFragment dialogFragment, int position);
    }



    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView image;
        private TextView item_name;
        private TextView item_price;
        private TextView item_amount;
        private CheckBox is_bought;
        private CardView cardView;

        public ViewHolder(View view) {
            super(view);

            image = (ImageView) view.findViewById(R.id.item_picture);
            item_name = (TextView) view.findViewById(R.id.item_name);
            item_price = (TextView) view.findViewById(R.id.item_price);
            item_amount = (TextView) view.findViewById(R.id.item_amount);
            is_bought  = (CheckBox) view.findViewById(R.id.chckbx_bought);
            cardView = (CardView) view.findViewById(R.id.card_view_item);


            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, ShoppingListDetail.class);
                    mContext.startActivity(intent);
                }
            });


        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.item,parent,false);
        return new ViewHolder(view);
    }



    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

            holder.is_bought.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        onItemCheckListener.onItemCheck(position, isChecked);
                }
            });

        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                DeleteItemDialog deleteItemDialog = new DeleteItemDialog();
                Bundle b = new Bundle();
                b.putInt("position", position);
                deleteItemDialog.setArguments(b);
                deleteItemDialog.show(fragmentManager,"Delete?");
                return true;
            }
        });

        Item cur = items.get(position);
        holder.item_name.setText(cur.getName());
        holder.item_price.setText(cur.getPrice() + " " + cur.getCurrency());
        holder.item_amount.setText(cur.getAmount() + " " + cur.getUnits());
        holder.is_bought.setChecked(cur.isBought());

    }

    @Override
    public int getItemCount() {
        return items.size();
    }



}