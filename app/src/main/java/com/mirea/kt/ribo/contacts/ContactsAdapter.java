package com.mirea.kt.ribo.contacts;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import io.realm.Realm;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder> {

    private final ArrayList<Contact> contacts;

    ContactsAdapter(ArrayList<Contact> contacts) {
        Collections.sort(contacts, (Comparator<Contact>) (c1, c2) -> c1.name.compareToIgnoreCase(c2.name));
        this.contacts = contacts;
    }

    @NonNull
    @Override
    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_contact, parent, false);
        return new ContactsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactsViewHolder holder, int position) {
        holder.bind(contacts.get(position), v -> removeContact(holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public void addOrEditContact(Contact contact) {
        Realm realm = Realm.getDefaultInstance();
        Contact contactInDB = realm.where(Contact.class).equalTo("phone", contact.phone).findFirst();
        if (contactInDB == null) {
            realm.executeTransactionAsync(it -> it.insert(contact));
            int i = 0;
            for (; i < contacts.size(); i++) {
                if (contacts.get(i).name.compareToIgnoreCase(contact.name) > 0) {
                    break;
                }
            }
            contacts.add(i, contact);
            notifyItemInserted(i);
        } else {
            realm.beginTransaction();
            contactInDB.avatar = contact.avatar;
            contactInDB.name = contact.name;
            realm.commitTransaction();
            for (int i = 0; i < contacts.size(); i++) {
                if (contacts.get(i).phone.equals(contact.phone)) {
                    notifyItemChanged(i);
                    break;
                }
            }
        }
    }

    private void removeContact(int position) {
        Realm realm = Realm.getDefaultInstance();
        String phone = contacts.get(position).phone;

        Contact contact = realm.where(Contact.class).equalTo("phone", phone).findFirst();
        realm.beginTransaction();
        if (contact != null) contact.deleteFromRealm();
        realm.commitTransaction();

        contacts.remove(position);
        notifyItemRemoved(position);
    }

    static class ContactsViewHolder extends RecyclerView.ViewHolder {

        TextView nameTextView;
        TextView phoneTextView;
        ImageView avatarImageView;
        ImageButton shareButton;
        ImageButton deleteButton;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            phoneTextView = itemView.findViewById(R.id.phoneTextView);
            avatarImageView = itemView.findViewById(R.id.avatarImageView);
            shareButton = itemView.findViewById(R.id.shareButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }

        void bind(Contact contact, View.OnClickListener removeContactOnClickListener) {
            itemView.setOnLongClickListener(v -> {
                Uri uri = new Uri.Builder()
                        .scheme("tel")
                        .authority(contact.phone)
                        .build();
                Intent intent = new Intent(Intent.ACTION_DIAL, uri);
                v.getContext().startActivity(intent);
                return true;
            });
            nameTextView.setText(contact.name);
            phoneTextView.setText(contact.phone);
            if (contact.avatar.trim().isEmpty()) {
                avatarImageView.setImageResource(R.drawable.avatar);
            } else {
                Picasso
                        .get()
                        .load(contact.avatar)
                        .placeholder(R.drawable.avatar)
                        .error(R.drawable.avatar)
                        .into(avatarImageView);
            }
            shareButton.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                String text = v.getResources()
                        .getString(R.string.main_share_format, contact.name, contact.phone);
                intent.putExtra(Intent.EXTRA_TEXT, text);
                String title = v.getResources().getString(R.string.main_share_title);
                v.getContext().startActivity(Intent.createChooser(intent, title));
            });
            deleteButton.setOnClickListener(removeContactOnClickListener);
        }
    }
}
