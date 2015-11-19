package com.songjin.usum.reservation;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.kth.baasio.Baas;
import com.kth.baasio.exception.BaasioException;
import com.kth.baasio.query.BaasioQuery;
import com.songjin.usum.HashBiMap;
import com.songjin.usum.constants.Category;
import com.songjin.usum.constants.Sex;
import com.songjin.usum.controllers.fragments.SettingFragment;
import com.songjin.usum.dtos.ProductCardDto;
import com.songjin.usum.entities.ProductEntity;
import com.songjin.usum.entities.ReservedCategoryEntity;
import com.songjin.usum.entities.UserEntity;
import com.songjin.usum.gcm.PushManager;
import com.songjin.usum.managers.RequestManager;

import java.util.ArrayList;
import java.util.List;

public class ReservationPushService extends IntentService {
    private ReservationCheckThread reservationCheckThread;

    public ReservationPushService() {
        super("ReservationPushService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        reservationCheckThread = new ReservationCheckThread();
        reservationCheckThread.run();
    }

    private class ReservationCheckThread extends Thread {
        public void run() {
            while (true) {
                try {
                    checkRegisteredNewProduct();
                    sleep(60 * 1000);   // 60초마다
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void checkRegisteredNewProduct() {
        ArrayList<ReservedCategoryEntity> reservedCategories = SettingFragment.getReservedCategories();
        if (reservedCategories.size() == 0) {
            return;
        }

        BaasioQuery query = new BaasioQuery();
        query.setType(ProductEntity.COLLECTION_NAME);
        String where = "";
        UserEntity userEntity = new UserEntity(Baas.io().getSignedInUser());
        if (userEntity.uuid == null) {
            return;
        }
        for (ReservedCategoryEntity reservedCategory : reservedCategories) {
            where += "(" +
                    ProductEntity.PROPERTY_SCHOOL_ID + "=" + reservedCategory.schoolId + " AND " +
                    ProductEntity.PROPERTY_CATEGORY + "=" + reservedCategory.category + " AND " +
                    ProductEntity.PROPERTY_CREATED + ">=" + reservedCategory.lastCheckedTimestamp +
                    ") OR ";
        }
        where = where.substring(0, where.length() - String.valueOf(" OR ").length());
        query.setWheres(where);
        Log.d("USUM", where);

        RequestManager.getProductsInBackground(query, false, new RequestManager.TypedBaasioQueryCallback<ProductCardDto>() {
            @Override
            public void onResponse(List<ProductCardDto> entities) {
                HashBiMap<Integer, String> categories = Category.getHashBiMap(Sex.ALL);
                for (ProductCardDto productCardDto : entities) {
                    if (productCardDto.productEntity.user_uuid.equals(Baas.io().getSignedInUser().getUuid().toString())) {
                        continue;
                    }

                    String msg = "";
                    msg += categories.get(productCardDto.productEntity.category);
                    msg += "에 해당하는 상품이 등록되었습니다.";
                    PushManager.sendReservationPushToMe(msg);

                    SettingFragment.updateReservedCategoryTimestamp(
                            productCardDto.productEntity.school_id,
                            productCardDto.productEntity.category
                    );
                }
            }

            @Override
            public void onException(BaasioException e) {

            }
        });
    }
}