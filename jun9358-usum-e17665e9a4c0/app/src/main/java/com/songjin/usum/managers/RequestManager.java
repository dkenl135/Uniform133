package com.songjin.usum.managers;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.kakao.LogoutResponseCallback;
import com.kakao.UnlinkResponseCallback;
import com.kakao.UserManagement;
import com.kth.baasio.Baas;
import com.kth.baasio.callback.BaasioCallback;
import com.kth.baasio.callback.BaasioDownloadCallback;
import com.kth.baasio.callback.BaasioQueryCallback;
import com.kth.baasio.callback.BaasioSignInCallback;
import com.kth.baasio.callback.BaasioUploadCallback;
import com.kth.baasio.entity.BaasioBaseEntity;
import com.kth.baasio.entity.entity.BaasioEntity;
import com.kth.baasio.entity.file.BaasioFile;
import com.kth.baasio.entity.user.BaasioUser;
import com.kth.baasio.exception.BaasioException;
import com.kth.baasio.query.BaasioQuery;
import com.songjin.usum.controllers.activities.BaseActivity;
import com.songjin.usum.controllers.fragments.SettingFragment;
import com.songjin.usum.dtos.ProductCardDto;
import com.songjin.usum.dtos.SchoolRanking;
import com.songjin.usum.dtos.TimelineCardDto;
import com.songjin.usum.dtos.TimelineCommentCardDto;
import com.songjin.usum.entities.CommentEntity;
import com.songjin.usum.entities.FileEntity;
import com.songjin.usum.entities.LikeEntity;
import com.songjin.usum.entities.ProductEntity;
import com.songjin.usum.entities.SchoolEntity;
import com.songjin.usum.entities.SchoolPointEntity;
import com.songjin.usum.entities.TimelineEntity;
import com.songjin.usum.entities.TransactionEntity;
import com.songjin.usum.entities.UserEntity;

import java.io.File;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class RequestManager {
    public static void getSchoolsInBackground(BaasioQuery query, BaasioQueryCallback callback) {
        query.queryInBackground(callback);
    }

    public static void getNextSchoolsInBackground(BaasioQuery query, BaasioQueryCallback callback) {
        query.nextInBackground(callback);
    }

    public static void unlinkAppInBackground(UnlinkResponseCallback callback) {
        UserManagement.requestUnlink(callback);
    }

    public static void logoutAppInBackground(LogoutResponseCallback callback) {
        UserManagement.requestLogout(callback);
    }

    public static void getUserProfile(String uuid, BaasioCallback<BaasioEntity> callback) {
        BaasioEntity entity = new BaasioEntity(UserEntity.COLLECTION_NAME);
        entity.setUuid(UUID.fromString(uuid));
        entity.getInBackground(callback);
    }

    public static void getTimelineComments(String timelineUuid, final TypedBaasioQueryCallback<TimelineCommentCardDto> callback) {
        BaasioQuery query = new BaasioQuery();
        query.setType(CommentEntity.COLLECTION_NAME);
        query.setWheres(CommentEntity.PROPERTY_TIMELINE_ITEM_UUID + " = " + timelineUuid);
        query.setOrderBy(
                BaasioEntity.PROPERTY_CREATED,
                BaasioQuery.ORDER_BY.DESCENDING
        );
        query.queryInBackground(new BaasioQueryCallback() {
            @Override
            public void onResponse(List<BaasioBaseEntity> baasioBaseEntities, List<Object> objects, BaasioQuery query, long l) {
                final ArrayList<TimelineCommentCardDto> timelineCommentCardDtos = new ArrayList<>();
                for (BaasioBaseEntity entity : baasioBaseEntities) {
                    TimelineCommentCardDto timelineCommentCardDto = new TimelineCommentCardDto();
                    timelineCommentCardDto.commentEntity = new CommentEntity(entity);
                    timelineCommentCardDtos.add(timelineCommentCardDto);
                }

                for (final TimelineCommentCardDto timelineCommentCardDto : timelineCommentCardDtos) {
                    CommentEntity commentEntity = timelineCommentCardDto.commentEntity;

                    getUserProfile(commentEntity.user_uuid, new BaasioCallback<BaasioEntity>() {
                        @Override
                        public void onResponse(BaasioEntity baasioEntity) {
                            timelineCommentCardDto.userEntity = new UserEntity(baasioEntity);
                        }

                        @Override
                        public void onException(BaasioException e) {
                            timelineCommentCardDto.userEntity = new UserEntity();
                        }
                    });
                }

                AsyncTask<TimelineCommentCardDto, Void, Boolean> asyncTask = new AsyncTask<TimelineCommentCardDto, Void, Boolean>() {
                    @Override
                    protected Boolean doInBackground(TimelineCommentCardDto... params) {
                        if (params == null) {
                            return false;
                        }

                        for (TimelineCommentCardDto commentCardDto : timelineCommentCardDtos) {
                            while (true) {
                                if (commentCardDto.isAllDataLoaded()) {
                                    break;
                                }
                            }
                        }

                        return true;
                    }

                    @Override
                    protected void onPostExecute(Boolean result) {
                        callback.onResponse(timelineCommentCardDtos);
                    }
                };
                asyncTask.execute(timelineCommentCardDtos.toArray(new TimelineCommentCardDto[timelineCommentCardDtos.size()]));
            }

            @Override
            public void onException(BaasioException e) {

            }
        });
    }

    public static void insertTimelineComment(String timelineItemUuid, String commentContents, BaasioCallback<BaasioEntity> callback) {
        BaasioUser user = Baas.io().getSignedInUser();

        BaasioEntity entity = new BaasioEntity(CommentEntity.COLLECTION_NAME);
        entity.setProperty(CommentEntity.PROPERTY_CONTENTS, commentContents);
        entity.setProperty(CommentEntity.PROPERTY_TIMELINE_ITEM_UUID, timelineItemUuid);
        entity.setProperty(CommentEntity.PROPERTY_USER_UUID, user.getUuid().toString());
        entity.saveInBackground(callback);
    }

    public static void insertTimeline(int schoold, String contents, BaasioCallback<BaasioEntity> callback) {
        BaasioUser user = Baas.io().getSignedInUser();

        BaasioEntity entity = new BaasioEntity(TimelineEntity.COLLECTION_NAME);
        entity.setProperty(TimelineEntity.PROPERTY_USER_UUID, user.getUuid().toString());
        entity.setProperty(TimelineEntity.PROPERTY_CONTENTS, contents);
        // FIXME: school_id proprty 추가 및 상수화
        entity.setProperty("school_id", schoold);
        entity.saveInBackground(callback);
    }

    public static void insertFile(String parentUuid, Uri uri, BaasioUploadCallback callback) {
        String path = uri.getEncodedPath();
        String fileName = path.substring(path.lastIndexOf('/') + 1);

        BaasioFile baasioFile = new BaasioFile();
        baasioFile.setProperty(FileEntity.PROPERTY_PARENT_UUID, parentUuid);
        baasioFile.fileUploadInBackground(path, fileName, callback);
    }

    public static void insertLike(String timelineUuid, BaasioCallback<BaasioEntity> callback) {
        BaasioUser user = Baas.io().getSignedInUser();

        BaasioEntity entity = new BaasioEntity(LikeEntity.COLLECTION_NAME);
        entity.setProperty(LikeEntity.PROPERTY_USER_UUID, user.getUuid().toString());
        entity.setProperty(LikeEntity.PROPERTY_TIMELINE_UUID, timelineUuid);
        entity.saveInBackground(callback);
    }

    public static void deleteLike(LikeEntity likeEntity, BaasioCallback<BaasioEntity> callback) {
        BaasioEntity entity = new BaasioEntity(LikeEntity.COLLECTION_NAME);
        entity.setUuid(UUID.fromString(likeEntity.uuid));
        entity.deleteInBackground(callback);
    }

    public static void requestSignUp(UserEntity userEntity, BaasioSignInCallback callback) {
        BaasioUser signedUser = Baas.io().getSignedInUser();

        signedUser.setProperty(UserEntity.PROPERTY_REAL_NAME, userEntity.realName);
        signedUser.setProperty(UserEntity.PROPERTY_SEX, userEntity.sex.ordinal());
        signedUser.setProperty(UserEntity.PROPERTY_USER_TYPE, userEntity.userType.ordinal());
        signedUser.setProperty(UserEntity.PROPERTY_PHONE, userEntity.phone);
        signedUser.setProperty(UserEntity.PROPERTY_SCHOOL_ID, userEntity.schoolId);
        signedUser.setProperty(UserEntity.PROPERTY_HAS_EXTRA_PROFILE, true);
        signedUser.updateInBackground(callback);
    }

    // FIXME: schoolManager, 인자에서 빼도록 재설계
    public static void getSchoolRankingsInBackground(final SchoolManager schoolManager, final BaasioQueryCallback callback) {
        if (!SettingFragment.getSchoolsLoaded()) {
            return;
        }

        BaasioQueryCallback schoolPointsCallback = new BaasioQueryCallback() {
            @Override
            public void onResponse(List<BaasioBaseEntity> baasioBaseEntities, List<Object> objects, BaasioQuery baasioQuery, long l) {
                List<BaasioBaseEntity> entities = new ArrayList<>();
                for (BaasioBaseEntity entity : baasioBaseEntities) {
                    SchoolPointEntity schoolPointEntity = new SchoolPointEntity(entity);
                    SchoolEntity schoolEntity = schoolManager.selectSchool(schoolPointEntity.school_id);
                    SchoolRanking schoolRanking = new SchoolRanking(schoolPointEntity, schoolEntity);

                    entities.add(schoolRanking.getBaasioBaseEntity());
                }

                callback.onResponse(entities, objects, baasioQuery, l);
            }

            @Override
            public void onException(BaasioException e) {
                callback.onException(e);
            }
        };

        BaasioQuery query = new BaasioQuery();
        query.setType(SchoolPointEntity.COLLECTION_NAME);
        query.setOrderBy(
                SchoolPointEntity.PROPERTY_POINT,
                BaasioQuery.ORDER_BY.DESCENDING
        );
        query.setLimit(100);
        query.queryInBackground(schoolPointsCallback);
    }

    public static void getTimelinesInBackground(BaasioQuery query, final TypedBaasioQueryCallback<TimelineCardDto> callback) {
        query.queryInBackground(getTimelineEntitiesCallback(callback));
    }

    public static void getNextTimelinesInBackground(BaasioQuery query, final TypedBaasioQueryCallback<TimelineCardDto> callback) {
        query.nextInBackground(getTimelineEntitiesCallback(callback));
    }

//    timeline_uuid=3dff8be7-f284-11e4-8a46-06fd000000c2 OR timeline_uuid=61feba0b-f024-11e4-88d5-06530c0000b4 OR timeline_uuid=e0827889-e695-11e4-b903-06f4fe0000b5 OR timeline_uuid=df5ae409-e682-11e4-8878-06a6fa0000b9 OR timeline_uuid=34091f36-e681-11e4-b903-06f4fe0000b5 OR timeline_uuid=9097d4c0-e67c-11e4-8a46-06fd000000c2 OR timeline_uuid=8241942f-c006-11e4-86a9-06a6fa0000b9 OR timeline_uuid=328cd53a-c004-11e4-86a9-06a6fa0000b9 AND user_uuid=ff1bc2ef-e67c-11e4-88d5-06530c0000b4

    private static BaasioQueryCallback getTimelineEntitiesCallback(final TypedBaasioQueryCallback<TimelineCardDto> callback) {
        final ArrayList<TimelineCardDto> timelineCardDtos = new ArrayList<>();
        return new BaasioQueryCallback() {
            @Override
            public void onResponse(List<BaasioBaseEntity> baasioBaseEntities, final List<Object> objects, final BaasioQuery baasioQuery, final long l) {
                if (baasioBaseEntities.isEmpty()) {
                    callback.onResponse(new ArrayList<TimelineCardDto>());
                    return;
                }

                final HashMap<String, TimelineCardDto> timelineCardDtoHashMap = new HashMap<>();
                for (BaasioBaseEntity entity : baasioBaseEntities) {
                    TimelineCardDto timelineCardDto = new TimelineCardDto();
                    timelineCardDto.timelineEntity = new TimelineEntity(entity);
                    timelineCardDtos.add(timelineCardDto);
                    timelineCardDtoHashMap.put(timelineCardDto.timelineEntity.uuid, timelineCardDto);
                }

                BaasioQuery query;
                String where;

                query = new BaasioQuery();
                where = "(";
                for (int i = 0; i < timelineCardDtos.size(); i++) {
                    where += LikeEntity.PROPERTY_TIMELINE_UUID + "=" + timelineCardDtos.get(i).timelineEntity.uuid;
                    if (i != timelineCardDtos.size() - 1) {
                        where += " OR ";
                    }
                }
                where += ") AND " + LikeEntity.PROPERTY_USER_UUID + "=" + Baas.io().getSignedInUser().getUuid().toString();
                Log.d("USUM", where);
                query.setType(LikeEntity.COLLECTION_NAME);
                query.setWheres(where);
                query.setLimit(timelineCardDtos.size());
                query.queryInBackground(new BaasioQueryCallback() {
                    @Override
                    public void onResponse(List<BaasioBaseEntity> baasioBaseEntities, List<Object> objects, BaasioQuery baasioQuery, long l) {
                        for (BaasioBaseEntity entity : baasioBaseEntities) {
                            LikeEntity likeEntity = new LikeEntity(entity);
                            TimelineCardDto timelineCardDto = timelineCardDtoHashMap.get(likeEntity.timeline_uuid);
                            timelineCardDto.likeEntity = likeEntity;
                        }
                        for (TimelineCardDto timelineCardDto : timelineCardDtos) {
                            if (timelineCardDto.likeEntity == null) {
                                timelineCardDto.likeEntity = new LikeEntity();
                            }
                        }

                    }

                    @Override
                    public void onException(BaasioException e) {

                    }
                });

                query = new BaasioQuery();
                where = "";
                for (int i = 0; i < timelineCardDtos.size(); i++) {
                    where += FileEntity.PROPERTY_PARENT_UUID + "=" + timelineCardDtos.get(i).timelineEntity.uuid;
                    if (i != timelineCardDtos.size() - 1) {
                        where += " OR ";
                    }
                }
                query.setType(FileEntity.COLLECTION_NAME);
                query.setWheres(where);
                query.setLimit(timelineCardDtos.size());
                query.queryInBackground(new BaasioQueryCallback() {
                    @Override
                    public void onResponse(List<BaasioBaseEntity> baasioBaseEntities, List<Object> objects, BaasioQuery query, long l) {
                        for (TimelineCardDto timelineCardDto : timelineCardDtos) {
                            timelineCardDto.fileEntities = new ArrayList<>();
                        }

                        for (BaasioBaseEntity entity : baasioBaseEntities) {
                            FileEntity fileEntity = new FileEntity(entity);
                            TimelineCardDto timelineCardDto = timelineCardDtoHashMap.get(fileEntity.parent_uuid);
                            timelineCardDto.fileEntities.add(fileEntity);
                        }
                    }

                    @Override
                    public void onException(BaasioException e) {

                    }
                });


                for (final TimelineCardDto timelineCardDto : timelineCardDtos) {
                    TimelineEntity timelineEntity = timelineCardDto.timelineEntity;

                    getUserProfile(timelineEntity.user_uuid, new BaasioCallback<BaasioEntity>() {
                        @Override
                        public void onResponse(BaasioEntity baasioEntity) {
                            timelineCardDto.userEntity = new UserEntity(baasioEntity);
                        }

                        @Override
                        public void onException(BaasioException e) {
                            timelineCardDto.userEntity = new UserEntity();
                        }
                    });
                }

                AsyncTask<TimelineCardDto, Void, Boolean> asyncTask = new AsyncTask<TimelineCardDto, Void, Boolean>() {
                    @Override
                    protected Boolean doInBackground(TimelineCardDto... params) {
                        if (params == null) {
                            return false;
                        }

                        for (TimelineCardDto timelineCardDto : timelineCardDtos) {
                            while (true) {
                                if (timelineCardDto.isAllDataLoaded()) {
                                    break;
                                }
                            }
                        }

                        return true;
                    }

                    @Override
                    protected void onPostExecute(Boolean result) {
                        callback.onResponse(timelineCardDtos);
                    }
                };
                asyncTask.execute(timelineCardDtos.toArray(new TimelineCardDto[timelineCardDtos.size()]));
            }

            @Override
            public void onException(BaasioException e) {
                callback.onException(e);
            }
        };
    }

    public static void getProductsInBackground(BaasioQuery query, boolean includeCompletedTransaction, TypedBaasioQueryCallback<ProductCardDto> callback) {
        query.queryInBackground(getProductCardDtosCallback(includeCompletedTransaction, callback));
    }

    public static void getNextProductsInBackground(BaasioQuery query, boolean includeCompletedTransaction, TypedBaasioQueryCallback<ProductCardDto> callback) {
        query.nextInBackground(getProductCardDtosCallback(includeCompletedTransaction, callback));
    }

    private static BaasioQueryCallback getProductCardDtosCallback(final boolean includeCompletedTransaction, final TypedBaasioQueryCallback<ProductCardDto> callback) {
        final ArrayList<ProductCardDto> productCardDtos = new ArrayList<>();
        final HashMap<String, ProductCardDto> productCardDtoHashMap = new HashMap<>();

        return new BaasioQueryCallback() {
            @Override
            public void onResponse(List<BaasioBaseEntity> baasioBaseEntities, List<Object> objects, BaasioQuery query, long l) {
                if (baasioBaseEntities.isEmpty()) {
                    callback.onResponse(new ArrayList<ProductCardDto>());
                    return;
                }

                for (BaasioBaseEntity baasioBaseEntity : baasioBaseEntities) {
                    ProductCardDto productCardDto = new ProductCardDto();
                    productCardDto.productEntity = new ProductEntity(baasioBaseEntity);

                    productCardDtos.add(productCardDto);
                    productCardDtoHashMap.put(productCardDto.productEntity.uuid, productCardDto);
                }

                final HashMap<String, Boolean> responseChecker = new HashMap<>();
                BaasioQuery subQuery;
                String where;

                responseChecker.put(FileEntity.COLLECTION_NAME, false);
                subQuery = new BaasioQuery();
                where = "";
                for (int i = 0; i < productCardDtos.size(); i++) {
                    where += FileEntity.PROPERTY_PARENT_UUID + "=" + productCardDtos.get(i).productEntity.uuid;
                    if (i != productCardDtos.size() - 1) {
                        where += " OR ";
                    }
                }
                subQuery.setType(FileEntity.COLLECTION_NAME);
                subQuery.setWheres(where);
                subQuery.setLimit(productCardDtos.size() * 10);
                subQuery.queryInBackground(new BaasioQueryCallback() {
                    @Override
                    public void onResponse(List<BaasioBaseEntity> baasioBaseEntities, List<Object> objects, BaasioQuery query, long l) {
                        ArrayList<FileEntity> fileEntities = new ArrayList<>();
                        for (BaasioBaseEntity baasioBaseEntity : baasioBaseEntities) {
                            fileEntities.add(new FileEntity(baasioBaseEntity));
                        }

                        for (FileEntity fileEntity : fileEntities) {
                            for (ProductCardDto productCardDto : productCardDtos) {
                                if (fileEntity.parent_uuid.equals(productCardDto.productEntity.uuid)) {
                                    productCardDto.fileEntities.add(fileEntity);
                                }
                            }
                        }

                        responseChecker.put(FileEntity.COLLECTION_NAME, true);
                    }

                    @Override
                    public void onException(BaasioException e) {
                        responseChecker.put(FileEntity.COLLECTION_NAME, true);
                    }
                });

                responseChecker.put(TransactionEntity.COLLECTION_NAME, false);
                subQuery = new BaasioQuery();
                where = "";
                for (int i = 0; i < productCardDtos.size(); i++) {
                    where += TransactionEntity.PROPERTY_PRODUCT_UUID + "=" + productCardDtos.get(i).productEntity.uuid;
                    if (i != productCardDtos.size() - 1) {
                        where += " OR ";
                    }
                }
                subQuery.setType(TransactionEntity.COLLECTION_NAME);
                subQuery.setWheres(where);
                subQuery.setLimit(productCardDtos.size() * 10);
                subQuery.queryInBackground(new BaasioQueryCallback() {
                    @Override
                    public void onResponse(List<BaasioBaseEntity> baasioBaseEntities, List<Object> objects, BaasioQuery query, long l) {
                        ArrayList<TransactionEntity> transactionEntities = new ArrayList<>();
                        for (BaasioBaseEntity baasioBaseEntity : baasioBaseEntities) {
                            transactionEntities.add(new TransactionEntity(baasioBaseEntity));
                        }


                        for (TransactionEntity transactionEntity : transactionEntities) {
                            ProductCardDto productCardDto = productCardDtoHashMap.get(transactionEntity.product_uuid);
                            productCardDto.transactionEntity = transactionEntity;

                            if (!includeCompletedTransaction) {
                                if (transactionEntity.status != TransactionEntity.STATUS_TYPE.REGISTERED) {
                                    productCardDtos.remove(productCardDto);
                                }
                            }
                        }

                        responseChecker.put(TransactionEntity.COLLECTION_NAME, true);
                    }

                    @Override
                    public void onException(BaasioException e) {
                        responseChecker.put(TransactionEntity.COLLECTION_NAME, true);
                    }
                });

                AsyncTask<HashMap<String, Boolean>, Void, Boolean> asyncTask = new AsyncTask<HashMap<String, Boolean>, Void, Boolean>() {
                    @Override
                    protected Boolean doInBackground(HashMap<String, Boolean>... params) {
                        if (params == null) {
                            return false;
                        }

                        for (HashMap<String, Boolean> param : params) {
                            while (param.containsValue(false)) {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    return false;
                                }
                            }
                        }

                        return true;
                    }

                    @Override
                    protected void onPostExecute(Boolean result) {
                        callback.onResponse(productCardDtos);
                    }
                };
                asyncTask.execute(responseChecker);
            }

            @Override
            public void onException(BaasioException e) {
                callback.onException(e);
            }
        };
    }

    public static void insertProductsInBackground(ArrayList<ProductCardDto> productCardDtos, BaasioCallback<List<BaasioEntity>> callback) {
        ArrayList<BaasioEntity> baasioEntities = new ArrayList<>();
        for (ProductCardDto productCardDto : productCardDtos) {
            baasioEntities.add(productCardDto.productEntity.getBaasioEntity());
        }
        BaasioEntity.saveInBackground(ProductEntity.COLLECTION_NAME, baasioEntities, callback);
    }

    public static void insertTransactionsInBackground(ArrayList<ProductEntity> productEntities, BaasioCallback<List<BaasioEntity>> callback) {
        ArrayList<BaasioEntity> baasioEntities = new ArrayList<>();
        for (ProductEntity productEntity : productEntities) {
            TransactionEntity transactionEntity = new TransactionEntity();
            transactionEntity.status = TransactionEntity.STATUS_TYPE.REGISTERED;
            transactionEntity.donator_uuid = productEntity.user_uuid;
            transactionEntity.receiver_uuid = "";
            transactionEntity.product_uuid = productEntity.uuid;
            transactionEntity.product_name = productEntity.product_name;
            baasioEntities.add(transactionEntity.getBaasioEntity());
        }
        BaasioEntity.saveInBackground(TransactionEntity.COLLECTION_NAME, baasioEntities, callback);
    }

    public static void updateTransactionStatus(final TransactionEntity localTransactionEntity, final TransactionEntity.STATUS_TYPE statusType, final BaasioCallback<BaasioEntity> callback) {
        BaasioEntity baasioEntity = new BaasioEntity(TransactionEntity.COLLECTION_NAME);
        baasioEntity.setUuid(UUID.fromString(localTransactionEntity.uuid));
        baasioEntity.getInBackground(new BaasioCallback<BaasioEntity>() {
            @Override
            public void onResponse(BaasioEntity baasioEntity) {
                TransactionEntity serverTransactionEntity = new TransactionEntity(baasioEntity);
                if (localTransactionEntity.status != serverTransactionEntity.status) {
                    callback.onException(new BaasioException("서버에 요청하는 도중에 문제가 발생하였습니다."));
                    return;
                }

                serverTransactionEntity.status = statusType;
                switch (statusType) {
                    case REGISTERED:
                        serverTransactionEntity.receiver_uuid = "";
                        break;
                    case REQUESTED:
                        serverTransactionEntity.receiver_uuid = Baas.io().getSignedInUser().getUuid().toString();
                        break;
                    case SENDED:
                        break;
                    case RECEIVED:
                        break;
                }
                BaasioEntity newEntity = serverTransactionEntity.getBaasioEntity();
                newEntity.setType(TransactionEntity.COLLECTION_NAME);
                newEntity.updateInBackground(callback);
            }

            @Override
            public void onException(BaasioException e) {
                callback.onException(new BaasioException("서버에 요청하는 도중에 문제가 발생하였습니다."));
            }
        });
    }

    public static void getMyProductsInBackground(final TypedBaasioQueryCallback<ProductCardDto> callback) {
        UserEntity signedUserEntity = new UserEntity(Baas.io().getSignedInUser());

        BaasioQuery query = new BaasioQuery();
        query.setType(TransactionEntity.COLLECTION_NAME);
        query.setWheres(
                TransactionEntity.PROPERTY_DONATOR_UUID + "=" + signedUserEntity.uuid + " OR " +
                        TransactionEntity.PROPERTY_RECEIVER_UUID + "=" + signedUserEntity.uuid
        );
        query.setOrderBy(
                TransactionEntity.PROPERTY_MODIFIED,
                BaasioQuery.ORDER_BY.DESCENDING
        );
        query.queryInBackground(new BaasioQueryCallback() {
            @Override
            public void onResponse(List<BaasioBaseEntity> entities, List<Object> objects, BaasioQuery baasioQuery, long l) {
                ArrayList<TransactionEntity> transactionEntities = new ArrayList<>();
                for (BaasioBaseEntity entity : entities) {
                    transactionEntities.add(new TransactionEntity(entity));
                }
                if (transactionEntities.size() == 0) {
                    callback.onResponse(new ArrayList<ProductCardDto>());
                    return;
                }

                String subWhere = "";
                for (int i = 0; i < transactionEntities.size(); i++) {
                    subWhere += ProductEntity.PROPERTY_PRODUCT_NAME + "=" + transactionEntities.get(i).product_name;
                    if (i != transactionEntities.size() - 1) {
                        subWhere += " OR ";
                    }
                }

                BaasioQuery subQuery = new BaasioQuery();
                subQuery.setType(ProductEntity.COLLECTION_NAME);
                subQuery.setWheres(subWhere);
                RequestManager.getProductsInBackground(subQuery, true, callback);
            }

            @Override
            public void onException(BaasioException e) {
                callback.onException(e);
            }
        });
    }

    public static void updateUserProfile(UserEntity userEntity, BaasioCallback<BaasioUser> callback) {
        BaasioUser baasioUser = Baas.io().getSignedInUser();
        baasioUser.setProperty(UserEntity.PROPERTY_PHONE, userEntity.phone);
        baasioUser.setProperty(UserEntity.PROPERTY_SCHOOL_ID, userEntity.schoolId);
        baasioUser.updateInBackground(callback);
    }

    public static void downloadFile(FileEntity fileEntity, BaasioDownloadCallback baasioDownloadCallback) {
        File file = new File(BaseActivity.context.getCacheDir() + fileEntity.uuid);
        if (file.exists()) {
            baasioDownloadCallback.onResponse("exists");
            return;
        }

        BaasioFile baasioFile = fileEntity.getBaasioFile();
        baasioFile.fileDownloadInBackground(BaseActivity.context.getCacheDir() + fileEntity.uuid, baasioDownloadCallback);
    }

    public static void updateTimeline(TimelineCardDto timelineCardDto, BaasioCallback<BaasioEntity> callback) {
        BaasioEntity entity = timelineCardDto.timelineEntity.getBaasioEntity();
        entity.updateInBackground(callback);
    }

    public static void updateProduct(ProductCardDto productCardDto, BaasioCallback<BaasioEntity> callback) {
        BaasioEntity entity = productCardDto.productEntity.getBaasioEntity();
        entity.updateInBackground(callback);
    }

    public static void deleteFileEntities(ArrayList<FileEntity> fileEntities) {
        for (FileEntity fileEntity : fileEntities) {
            BaasioFile baasioFile = fileEntity.getBaasioFile();
            baasioFile.deleteInBackground(new BaasioCallback<BaasioFile>() {
                @Override
                public void onResponse(BaasioFile baasioFile) {

                }

                @Override
                public void onException(BaasioException e) {

                }
            });
        }
    }

    public static void deleteTimeline(TimelineCardDto timelineCardDto, BaasioCallback<BaasioEntity> callback) {
        BaasioEntity entity = timelineCardDto.timelineEntity.getBaasioEntity();
        entity.deleteInBackground(callback);
    }

    public static void deleteComment(TimelineCommentCardDto timelineCommentCardDto, BaasioCallback<BaasioEntity> callback) {
        BaasioEntity entity = timelineCommentCardDto.commentEntity.getBaasioEntity();
        entity.deleteInBackground(callback);
    }

    public static void deleteProduct(ProductCardDto productCardDto, BaasioCallback<BaasioEntity> callback) {
        BaasioEntity entity = productCardDto.productEntity.getBaasioEntity();
        entity.deleteInBackground(callback);
    }

    public interface TypedBaasioQueryCallback<T> {
        void onResponse(List<T> entities);

        void onException(BaasioException e);
    }
}