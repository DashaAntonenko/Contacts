package com.mirea.kt.ribo.contacts;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Contact extends RealmObject {
    @PrimaryKey
    String phone;
    String name;
    String avatar;

    public Contact(){}
    Contact(String phone, String name, String avatar) {
        this.phone = phone;
        this.name = name;
        this.avatar = avatar;
    }
}
