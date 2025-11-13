package com.whozzjp.smsapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Blocksms extends AppCompatActivity {

    private static final int RESULT_PICK_CONTACT =1;

    private List<PhoneNumber> phoneNumbers = new ArrayList<>();
    private EditText phoneNumberEditText;
    private Button submitButton;
    private Button pickContacts;
    private RecyclerView phoneNumberRecyclerView;
    private BlockRVAdapter phoneNumberAdapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocksms);

        phoneNumberEditText = findViewById(R.id.blocksmscontact);
        submitButton = findViewById(R.id.blockNbtn);
        pickContacts=findViewById(R.id.Block_pick_contact);
        phoneNumberRecyclerView = findViewById(R.id.blocksmsRV);

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        Set<String> phoneNumberSet = sharedPreferences.getStringSet("phoneNumbers", null);
        if (phoneNumberSet != null) {
            for (String phoneNumberString : phoneNumberSet) {
                PhoneNumber phoneNumber = new PhoneNumber(phoneNumberString);
                phoneNumbers.add(phoneNumber);
            }
        }

        BlockRVAdapter adapter = new BlockRVAdapter(phoneNumbers, Blocksms.this);
        phoneNumberRecyclerView.setAdapter(adapter);
        phoneNumberRecyclerView.setLayoutManager(new LinearLayoutManager(Blocksms.this));

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumberString = phoneNumberEditText.getText().toString().trim();
                if (!phoneNumberString.isEmpty()) {
                    PhoneNumber phoneNumber = new PhoneNumber(phoneNumberString);
                    phoneNumbers.add(phoneNumber);
                    adapter.notifyDataSetChanged();

                    // Save the updated phone numbers list to SharedPreferences
                    SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    Set<String> phoneNumberSet = new HashSet<>();
                    for (PhoneNumber phoneNumberItem : phoneNumbers) {
                        phoneNumberSet.add(phoneNumberItem.getNumber());
                    }
                    editor.putStringSet("phoneNumbers", phoneNumberSet);
                    editor.apply();
                }
            }
        });

        try {
            adapter.setOnDeleteClickListener(new BlockRVAdapter.OnDeleteClickListener() {
                @Override
                public void onDeleteClick(int position) {
                    phoneNumbers.remove(position);
                    adapter.notifyItemRemoved(position);
                    adapter.notifyDataSetChanged();

                    // Save the updated phone numbers list to SharedPreferences
                    SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    Set<String> phoneNumberSet = new HashSet<>();
                    for (PhoneNumber phoneNumberItem : phoneNumbers) {
                        phoneNumberSet.add(phoneNumberItem.getNumber());
                    }
                    editor.putStringSet("phoneNumbers", phoneNumberSet);
                    editor.apply();
                }
            });
            phoneNumberRecyclerView.setAdapter(adapter);
        }catch (Exception e){
            Log.d("recyclerview Exception",e.getMessage());
        }

       /* Intent intent = new Intent(this, MyBackgroundService.class);
        ArrayList<String> phoneNumbersList = new ArrayList<>();
        for (PhoneNumber phoneNumber : phoneNumbers) {
            phoneNumbersList.add("+91" + phoneNumber.getNumber());
        }
        intent.putStringArrayListExtra("phoneNumbersList", phoneNumbersList);
        try {
            Toast.makeText(this, "" + phoneNumbersList.get(0), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
        }
        startService(intent);*/


        pickContacts.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    Intent in = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                    startActivityForResult(in, RESULT_PICK_CONTACT);
                }catch (Exception e){
                    Log.d("Contact",e.getMessage());
                }
            }
        });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RESULT_PICK_CONTACT:
                    contactPicked(data);
                    break;
            }
        } else {
            Toast.makeText(this, "Failed To pick contact", Toast.LENGTH_SHORT).show();
        }
    }
    private void contactPicked(Intent data) {
        Cursor cursor = null;

        try {
            String phoneNo = null;
            Uri uri = data.getData ();
            cursor = getContentResolver ().query (uri, null, null,null,null);
            cursor.moveToFirst ();
            int phoneIndex = cursor.getColumnIndex (ContactsContract.CommonDataKinds.Phone.NUMBER);

            phoneNo = cursor.getString (phoneIndex).trim();

            phoneNumberEditText.setText (phoneNo.trim());

        } catch (Exception e) {
            e.printStackTrace ();
        }


    }
}
