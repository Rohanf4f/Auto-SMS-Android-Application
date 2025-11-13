package com.whozzjp.smsapp;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BlockRVAdapter extends RecyclerView.Adapter<BlockRVAdapter.ViewHolder> {

    private Context context;
    private List<PhoneNumber> phoneNumbers;



    ArrayList<String> phoneNumbersList = new ArrayList<>();

    public BlockRVAdapter(List<PhoneNumber> phoneNumbers,Context context) {
        this.phoneNumbers = phoneNumbers;
        this.context= context;
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    private OnDeleteClickListener onDeleteClickListener;


    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        onDeleteClickListener = listener;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //  View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.blocksmsrecycleview,parent,false);
        return new ViewHolder(view,onDeleteClickListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PhoneNumber phoneNumber = phoneNumbers.get(position);

        holder.phoneTextView.setText(phoneNumber.getNumber());

        holder.setOnDeleteClickListener(new OnDeleteClickListener() {
            @Override
            public void onDeleteClick(int position) {
                PhoneNumber deletedPhoneNumber = phoneNumbers.remove(position);
                notifyDataSetChanged();

                // Update the phone numbers list in SharedPreferences
                SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                Set<String> phoneNumberSet = new HashSet<>();
                for (PhoneNumber phoneNumberItem : phoneNumbers) {
                    phoneNumberSet.add(phoneNumberItem.getNumber());
                }
                editor.putStringSet("phoneNumbers", phoneNumberSet);
                editor.putString("deletedPhoneNumber", deletedPhoneNumber.getNumber()); // Store the deleted phone number in SharedPreferences

                editor.apply();

                // Toast.makeText(context, "Block : "+deletedPhoneNumber.getNumber(), Toast.LENGTH_LONG).show();
                // Start the background service and pass the deleted phone number to it
                /*Intent i = new Intent(context, MyBackgroundService.class);
                i.putExtra("DeletedphoneNumber", deletedPhoneNumber.getNumber());
                context.startService(i);*/
            }
        });

        SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("PhoneNumber", phoneNumber.getNumber()); // Store the deleted phone number in SharedPreferences
        editor.apply();
       /* Intent intent = new Intent(context, MyBackgroundService.class);
        intent.putExtra("phoneNumber", phoneNumber.getNumber());
        context.startService(intent);
        phoneNumbersList.add("+91"+phoneNumber.getNumber());*/


    }





    @Override
    public int getItemCount() {
        return phoneNumbers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView phoneTextView;
        public ImageView deleteButton;
        public ViewHolder(View itemView, final OnDeleteClickListener onDeleteClickListener) {
            super(itemView);
            phoneTextView = itemView.findViewById(R.id.blockNRV);
            deleteButton = itemView.findViewById(R.id.DeleteBtn);
        }
        public void setOnDeleteClickListener(OnDeleteClickListener listener) {
            deleteButton.setOnClickListener(view -> listener.onDeleteClick(getAdapterPosition()));
        }
    }
}
