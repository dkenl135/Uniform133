package com.songjin.usum.entities;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class ProductEntity implements Parcelable {
    public static final String COLLECTION_NAME = "products";
//    public static final String PROPERTY_CREATED = BaasioBaseEntity.PROPERTY_CREATED;
    public static final String PROPERTY_CREATED = "product_created";
//    public static final String PROPERTY_USER_UUID = "user_uuid";
    public static final String PROPERTY_USER_ID = "product_user_id";
//    public static final String PROPERTY_UUID = BaasioBaseEntity.PROPERTY_UUID;
    public static final String PROPERTY_ID = "product_id";
    public static final String PROPERTY_PRODUCT_NAME = "product_name";
    public static final String PROPERTY_SCHOOL_ID = "product_school_id";
    public static final String PROPERTY_CATEGORY = "product_category";
    public static final String PROPERTY_SIZE = "product_size";
    public static final String PROPERTY_CONDITION = "product_condition";
    public static final String PROPERTY_SEX = "product_sex";
    public static final String PROPERTY_CONTENTS = "product_content";
    public static final Creator<ProductEntity> CREATOR = new Creator<ProductEntity>() {
        public ProductEntity createFromParcel(Parcel in) {
            return new ProductEntity(in);
        }

        public ProductEntity[] newArray(int size) {
            return new ProductEntity[size];
        }
    };
    public long created;
//    public String uuid;
    public String id;
    public String product_name;
//    public String user_uuid;
    public String user_id;
    public int school_id;
    public int category;
    public int size;
    public int condition;
    public int sex;
    public String contents;

    public ProductEntity() {
        this.product_name = UUID.randomUUID().toString();
    }

    public ProductEntity(JSONObject jsonObject) {
        set(jsonObject);
    }

    public ProductEntity(Bundle bundle) {
        set(bundle);
    }

    public ProductEntity(Parcel in) {
        set(in.readBundle());
    }

    public void set(Bundle bundle) {
        this.created = bundle.getLong(PROPERTY_CREATED);
        this.id = bundle.getString(PROPERTY_ID);
        this.product_name = bundle.getString(PROPERTY_PRODUCT_NAME);
//        this.user_uuid = bundle.getString(PROPERTY_USER_UUID);
        this.user_id = bundle.getString(PROPERTY_USER_ID);
        this.school_id = bundle.getInt(PROPERTY_SCHOOL_ID);
        this.category = bundle.getInt(PROPERTY_CATEGORY);
        this.size = bundle.getInt(PROPERTY_SIZE);
        this.condition = bundle.getInt(PROPERTY_CONDITION);
        this.sex = bundle.getInt(PROPERTY_SEX);
        this.contents = bundle.getString(PROPERTY_CONTENTS);
    }


    public boolean isSame(ProductEntity productEntity) {
        if (created != productEntity.created)
            return false;
        if (!id.equals(productEntity.id))
            return false;
        if (!product_name.equals(productEntity.product_name))
            return false;
        if (!user_id.equals(productEntity.user_id))
            return false;
        if (school_id != productEntity.school_id)
            return false;
        if (category != productEntity.category)
            return false;
        if (size != productEntity.size)
            return false;
        if (condition != productEntity.condition)
            return false;
        if (sex != productEntity.sex)
            return false;
        if (!contents.equals(productEntity.contents))
            return false;
        return true;
    }


    public void set(JSONObject object) {
        try {
            if (!object.get(PROPERTY_CREATED).equals(null)) {
                this.created = object.getLong(PROPERTY_CREATED);
            }

            if (object.getString(PROPERTY_ID) != null) {
                this.id = object.getString(PROPERTY_ID);
            }
//            if (object.getUuid() != null) {
//                this.uuid = object.getUuid().toString();
//            }
            if (object.getString(PROPERTY_PRODUCT_NAME) != null) {
                this.product_name = object.getString(PROPERTY_PRODUCT_NAME);
            }
//            if (object.getProperty(PROPERTY_USER_UUID) != null) {
//                this.user_uuid = object.getProperty(PROPERTY_USER_UUID).asText();
//            }
            if (object.getString(PROPERTY_USER_ID) != null) {
                this.user_id = object.getString(PROPERTY_USER_ID);
            }
            if (!object.get(PROPERTY_SCHOOL_ID).equals(null)) {
                this.school_id = object.getInt(PROPERTY_SCHOOL_ID);
            }
            if (!object.get(PROPERTY_CATEGORY).equals(null)) {
                this.category = object.getInt(PROPERTY_CATEGORY);
            }
            if (!object.get(PROPERTY_SIZE).equals(null)) {
                this.size = object.getInt(PROPERTY_SIZE);
            }
            if (!object.get(PROPERTY_CONDITION).equals(null)) {
                this.condition = object.getInt(PROPERTY_CONDITION);
            }
            if (!object.get(PROPERTY_SEX).equals(null)) {
                this.sex = object.getInt(PROPERTY_SEX);
            }
            if (object.getString(PROPERTY_CONTENTS) != null) {
                this.contents = object.getString(PROPERTY_CONTENTS);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Bundle getBundle() {
        Bundle bundle = new Bundle();
        bundle.putLong(PROPERTY_CREATED, this.created);
//        bundle.putString(PROPERTY_UUID, this.uuid);
        bundle.putString(PROPERTY_ID, this.id);
        bundle.putString(PROPERTY_PRODUCT_NAME, this.product_name);
//        bundle.putString(PROPERTY_USER_UUID, this.user_uuid);
        bundle.putString(PROPERTY_USER_ID, this.user_id);
        bundle.putInt(PROPERTY_SCHOOL_ID, this.school_id);
        bundle.putInt(PROPERTY_CATEGORY, this.category);
        bundle.putInt(PROPERTY_SIZE, this.size);
        bundle.putInt(PROPERTY_CONDITION, this.condition);
        bundle.putInt(PROPERTY_SEX, this.sex);
        bundle.putString(PROPERTY_CONTENTS, this.contents);

        return bundle;
    }

    public JSONObject getJson() {
        JSONObject object = new JSONObject();
//        baasioEntity.setType(COLLECTION_NAME);
//        if (this.uuid != null) {
//            baasioEntity.setUuid(UUID.fromString(this.uuid));
//        }
        try {
            object.put(PROPERTY_CREATED, this.created);
            object.put(PROPERTY_PRODUCT_NAME, this.product_name);
            object.put(PROPERTY_SCHOOL_ID, this.school_id);
            object.put(PROPERTY_ID, this.id);
//        object.setProperty(PROPERTY_USER_UUID, this.user_uuid);
            object.put(PROPERTY_USER_ID, this.user_id);
            object.put(PROPERTY_CATEGORY, this.category);
            object.put(PROPERTY_SIZE, this.size);
            object.put(PROPERTY_CONDITION, this.condition);
            object.put(PROPERTY_SEX, this.sex);
            object.put(PROPERTY_CONTENTS, this.contents);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return object;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeBundle(getBundle());
    }
}
