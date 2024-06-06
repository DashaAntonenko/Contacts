package com.mirea.kt.ribo.contacts;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.realm.Realm;

public class MainActivity extends AppCompatActivity {

    ContactsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        FloatingActionButton floatingActionButton = findViewById(R.id.floatingActionButton);

        Realm realm = Realm.getDefaultInstance();
        ArrayList<Contact> contacts = new ArrayList<>(realm.where(Contact.class).findAll());

        if (contacts.size() == 0) {
            String data = getIntent().getStringExtra("data");
            importContactsFromData(data, contacts, realm);
        }

        adapter = new ContactsAdapter(contacts);
        recyclerView.setAdapter(adapter);

        floatingActionButton.setOnClickListener(v -> showAddDialog(this));
    }

    private void importContactsFromData(String data, ArrayList<Contact> contacts, Realm realm) {
        if (data == null) return;
        JSONArray array;
        try {
            JSONObject object = new JSONObject(data);
            array = object.getJSONArray("data");
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        realm.beginTransaction();
        for (int i = 0; i < array.length(); i++) {
            Contact contact;
            try {
                JSONObject object = array.getJSONObject(i);
                String phone = object.getString("phone");
                String name = object.getString("name");
                String avatar = object.optString("avatar", "");
                contact = new Contact(phone, name, avatar);
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }
            contacts.add(contact);
            realm.insert(contact);
        }
        realm.commitTransaction();
    }

    private void showAddDialog(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        FrameLayout frame = new FrameLayout(context);
        View view = inflater.inflate(R.layout.dialog_add, frame);

        EditText nameEditText = view.findViewById(R.id.nameEditText);
        EditText avatarEditText = view.findViewById(R.id.avatarEditText);
        EditText phoneEditText = view.findViewById(R.id.phoneEditText);

        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle(R.string.main_new)
                .setView(frame)
                .setPositiveButton(R.string.main_add, null)
                .setNegativeButton(R.string.main_cancel, null)
                .show();

        Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        button.setOnClickListener(view1 -> {
            String name = nameEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();
            String avatar = avatarEditText.getText().toString().trim();

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(context, R.string.main_empty, Toast.LENGTH_LONG).show();
                return;
            }

            adapter.addOrEditContact(new Contact(phone, name, avatar));
            alertDialog.dismiss();
        });
    }
}