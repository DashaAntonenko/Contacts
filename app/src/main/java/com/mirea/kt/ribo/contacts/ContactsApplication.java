package com.mirea.kt.ribo.contacts;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class ContactsApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Realm.init(getApplicationContext());

        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("contacts.db")
                .schemaVersion(1)
                .build();

        Realm.setDefaultConfiguration(config);
    }
}
