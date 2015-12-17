package com.ironfactory.donation.socketIo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.Gson;
import com.ironfactory.donation.Global;
import com.ironfactory.donation.controllers.activities.LoginActivity;
import com.ironfactory.donation.controllers.activities.ProductDetailActivity;
import com.ironfactory.donation.controllers.activities.SignUpActivity;
import com.ironfactory.donation.dtos.ProductCardDto;
import com.ironfactory.donation.dtos.SchoolRanking;
import com.ironfactory.donation.dtos.TimelineCardDto;
import com.ironfactory.donation.dtos.TimelineCommentCardDto;
import com.ironfactory.donation.entities.FileEntity;
import com.ironfactory.donation.entities.LikeEntity;
import com.ironfactory.donation.entities.ProductEntity;
import com.ironfactory.donation.entities.SchoolEntity;
import com.ironfactory.donation.entities.TransactionEntity;
import com.ironfactory.donation.entities.UserEntity;
import com.ironfactory.donation.managers.RequestManager;
import com.ironfactory.donation.reservation.ReservationPushService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by ironFactory on 2015-08-03.
 */
public class SocketIO {

    private static Handler handler = new Handler();
    private static final String SERVER_URL = "http://uniform-test.herokuapp.com";
    private static final String TAG = "SocketIO";

    public static Socket socket;
    private Context context;

    public SocketIO(Context context) {
        this.context = context;
        init();
    }

    private void init() {
        Log.d(TAG, "init");
        try {
            socket = IO.socket(SERVER_URL);
        } catch (Exception e) {
            Log.e(TAG, "init 에러 = " + e.getMessage());
        }

        if (socket != null) {
            socketConnect();
        }

        if (!Global.isCreated)
            setListener();
        Global.isCreated = true;
    }


    private void setListener() {
        Log.d(TAG, "setListener");
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                // 연결
                Log.d(TAG, "소켓 연결");
            }
        }).on(Global.LOGIN, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                // 로그인
                JSONObject object = (JSONObject) args[0];
                processLogin(object);
            }
        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                // 연결 끊김
                Log.d(TAG, "소켓 연결 끊김");
                socketConnect();
            }
        }).on(Global.SIGN_UP, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject object = (JSONObject) args[0];
                processSingUp(object);
            }
        }).on(Global.SIGN_IN, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject object = (JSONObject) args[0];
                processSignIn(object);
            }
        }).on(Global.UPDATE_TRANSACTION_STATUS, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject object = (JSONObject) args[0];
                processUpdateTransactionStatus(object);
            }
        }).on(Global.UPDATE_PRODUCT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject object = (JSONObject) args[0];
                processUpdateProduct(object);
            }
        }).on(Global.GET_PRODUCT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject object = (JSONObject) args[0];
                processGetProduct(object);
            }
        });
    }


    // TODO: 15. 12. 2. 파일 입력 응답
//    private void processInsertFile(JSONObject object) {
//        try {
//            final int code = object.getInt(Global.CODE);
//            handler.post(new Runnable() {
//                @Override
//                public void run() {
//                    if (code == SocketException.SUCCESS)
//                        RequestManager.onInsertFile.onSuccess();
//                    else
//                        RequestManager.onInsertFile.onException(code);
//                }
//            });
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }



//    // TODO: 15. 12. 2. 타임라인 글쓰기 응답
//    private void processInsertTimeline(JSONObject object) {
//        try {
//            int code = object.getInt(Global.CODE);
//            Log.d(TAG, "타임라인 글쓰기 응답");
//            if (code == SocketException.SUCCESS) {
//                final JSONObject timelineJson = object.getJSONObject(Global.TIMELINE);
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        TimelineCardDto dto = new TimelineCardDto(timelineJson);
//                        RequestManager.onInsertTimeline.onSuccess(dto);
//                    }
//                });
//            } else {
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        RequestManager.onInsertTimeline.onException();
//                    }
//                });
//            }
//            Log.d(TAG, "object = " + object);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }


    // TODO: 15. 11. 28. 카카오 로그인 응답
//    private void processSignInKakao(JSONObject object) {
//        try {
//            int code = object.getInt(Global.CODE);
//            Log.d(TAG, "카카오 로그인 응답");
//            Log.d(TAG, "object = " + object);
//
//            Intent intent = new Intent(context, SignUpActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent.putExtra(Global.COMMAND, Global.SIGN_IN_KAKAO);
//            intent.putExtra(Global.CODE, code);
//            if (code == SocketException.SUCCESS) {
//                // 성공
//                JSONObject userObject = object.getJSONObject(Global.USER);
////                String id = userObject.getString(Global.ID);
////                String picture = userObject.getString(Global.PICTURE);
////                String phone = userObject.getString(Global.PHONE);
////                String realName = userObject.getString(Global.REAL_NAME);
////                String name = userObject.getString(Global.NAME);
////                String hasExtraProfile = userObject.getString(Global.HAS_EXTRA_PROFILE);
////                int sex = userObject.getInt(Global.SEX);
////                int userType = userObject.getInt(Global.USER_TYPE);
////                int schoolId = userObject.getInt(Global.SCHOOL_ID);
//                UserEntity user = new UserEntity(userObject);
//                intent.putExtra(Global.USER, user);
//            }
//            context.startActivity(intent);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }


    // TODO: 15. 11. 25. 제품 요청 응답
    private void processGetProduct(JSONObject object) {
        try {
            int code = object.getInt(Global.CODE);
            Log.d(TAG, "제품 요청 응답");
            Log.d(TAG, "object = " + object);

            Intent intent = new Intent(context, ReservationPushService.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Global.COMMAND, Global.GET_PRODUCT);
            intent.putExtra(Global.CODE, code);

            if (code == SocketException.SUCCESS) {
                JSONArray productArray = object.getJSONArray(Global.PRODUCT);
                Gson gson = new Gson();
                ArrayList<ProductCardDto> productCardDtos = new ArrayList<>();

                for (int i = 0; i < productArray.length(); i++) {
                    JSONObject productObject = productArray.getJSONObject(i);
                    ProductCardDto dto = gson.fromJson(productObject.toString(), ProductCardDto.class);
                    productCardDtos.add(dto);
                }
                intent.putExtra(Global.PRODUCT, productCardDtos);
            }
            context.startService(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    // TODO: 15. 11. 25. 좋아요
//    private void processInsertLike(JSONObject object) {
//        try {
//            final int code = object.getInt(Global.CODE);
//            Log.d(TAG, "좋아요 응답");
//            Log.d(TAG, "object = " + object);
//
//            if (code == SocketException.SUCCESS) {
//                JSONObject likeObject = object.getJSONObject(Global.LIKE);
//                final LikeEntity likeEntity = new LikeEntity(likeObject);
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        RequestManager.onInsertLike.onSuccess(likeEntity);
//                    }
//                });
//            } else {
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        RequestManager.onInsertLike.onException(code);
//                    }
//                });
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }


    // TODO: 15. 11. 25. 좋아요 삭제
//    private void processDeleteLike(JSONObject object) {
//        try {
//            final int code = object.getInt(Global.CODE);
//            Log.d(TAG, "좋아요 삭제 응답");
//            Log.d(TAG, "object = " + object);
//
//            handler.post(new Runnable() {
//                @Override
//                public void run() {
//                    if (code == SocketException.SUCCESS)
//                        RequestManager.onDeleteLike.onSuccess();
//                    else
//                        RequestManager.onDeleteLike.onException(code);
//                }
//            });
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }


    // TODO: 15. 11. 25. 타임라인 지우기
//    private void processDeleteTimeline(JSONObject object) {
//        try {
//            final int code = object.getInt(Global.CODE);
//            Log.d(TAG, "타임라인 지우기 응답");
//            Log.d(TAG, "object = " + object);
//
//            Intent intent = new Intent(context, TimelineDetailActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent.putExtra(Global.COMMAND, Global.DELETE_TIMELINE);
//            intent.putExtra(Global.CODE, code);
////            context.startActivity(intent);
//            handler.post(new Runnable() {
//                @Override
//                public void run() {
//                    if (code == SocketException.SUCCESS)
//                        RequestManager.onDeleteTimeline.onSuccess();
//                    else
//                        RequestManager.onDeleteTimeline.onException(code);
//                }
//            });
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }


    // TODO: 15. 11. 25. 제품 업데이트
    private void processUpdateProduct(JSONObject object) {
        try {
            int code = object.getInt(Global.CODE);
            Log.d(TAG, "제품 업데이트 응답");
            Log.d(TAG, "object = " + object);

            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Global.COMMAND, Global.UPDATE_PRODUCT);
            intent.putExtra(Global.CODE, code);
            context.startActivity(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


//    // TODO: 15. 11. 25. 파일 삭제
//    private void processDeleteFile(JSONObject object) {
//        try {
//            final int code = object.getInt(Global.CODE);
//            Log.d(TAG, "파일삭제 응답");
//            Log.d(TAG, "object = " + object);
//            handler.post(new Runnable() {
//                @Override
//                public void run() {
//                    if (code == SocketException.SUCCESS) {
//                        RequestManager.onDeleteFile.onSuccess();
//                    } else {
//                        RequestManager.onDeleteFile.onExection();
//                    }
//                }
//            });
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }


//    // TODO: 15. 11. 25. 제품 삭제 응답
//    private void processDeleteProduct(JSONObject object) {
//        try {
//            int code = object.getInt(Global.CODE);
//            Log.d(TAG, "제품 삭제 응답");
//            Log.d(TAG, "object = " + object);
//
//            Intent intent = new Intent(context, ProductDetailActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent.putExtra(Global.COMMAND, Global.DELETE_PRODUCT);
//            intent.putExtra(Global.CODE, code);
//            context.startActivity(intent);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }


    private void processUpdateTransactionStatus(JSONObject object) {
        try {
            int code = object.getInt(Global.CODE);
            int status = object.getInt(Global.STATUS);
            Log.d(TAG, "updateTransactionStatus 응답");
            Log.d(TAG, "object = " + object);

            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Global.CODE, code);
            intent.putExtra(Global.STATUS, status);

            if (code == SocketException.SUCCESS) {
                // 성공
                JSONObject transactionJson = object.getJSONObject(Global.TRANSACTION);
                TransactionEntity transactionEntity = new TransactionEntity(transactionJson);
                intent.putExtra(Global.TRANSACTION, transactionEntity);
            }
            context.startActivity(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    // TODO: 15. 11. 24. 댓글 삭제
//    private void processDeleteComment(JSONObject object) {
//        try {
//            final int code = object.getInt(Global.CODE);
//            int from = object.getInt(Global.FROM);
//            Log.d(TAG, "댓글 삭제 응답");
//            Log.d(TAG, "object = " + object);
//
//            handler.post(new Runnable() {
//                @Override
//                public void run() {
//                    if (code == SocketException.SUCCESS)
//                        RequestManager.onDeleteComment.onSuccess();
//                    else
//                        RequestManager.onDeleteComment.onException();
//                }
//            });
//
//            Intent intent;
//            if (from == 1)
//                intent = new Intent(context, ProductDetailActivity.class);
//            else
//                intent = new Intent(context, TimelineDetailActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent.putExtra(Global.CODE, code);
//            context.startActivity(intent);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }


    // TODO: 15. 11. 24. 내 제품 요청 응답
//    private void processGetMyProduct(JSONObject object) {
//        try {
//            final int code = object.getInt(Global.CODE);
//            Log.d(TAG, "내 제품 요청 응답");
//            Log.d(TAG, "object = " + object);
//
//            if (code == SocketException.SUCCESS) {
//                final ArrayList<ProductCardDto> productCardDtos = new ArrayList<>();
//                JSONArray array = object.getJSONArray(Global.PRODUCT);
//                for (int i = 0; i < array.length(); i++) {
//                    JSONObject productObject = array.getJSONObject(i);
//                    ProductCardDto dto = new ProductCardDto(productObject);
//                    productCardDtos.add(dto);
//                }
//
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        RequestManager.onGetMyProduct.onSuccess(productCardDtos);
//                    }
//                });
//            } else {
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        RequestManager.onGetMyProduct.onException(code);
//                    }
//                });
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }


    // TODO: 15. 11. 23. 제품 검색 응답
//    private void processSearchProduct(JSONObject object) {
//        try {
//            int code = object.getInt(Global.CODE);
//            Log.d(TAG, "제품 검색 응답");
//            Log.d(TAG, "object = " + object);
//
//            Intent intent = new Intent(context, MainActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent.putExtra(Global.COMMAND, Global.SEARCH_PRODUCT);
//            intent.putExtra(Global.CODE, code);
//
//            if (code == SocketException.SUCCESS) {
//                ArrayList<ProductCardDto> products = new ArrayList<>();
//                JSONArray array = object.getJSONArray(Global.PRODUCT);
//                for (int i = 0; i < array.length(); i++) {
//                    JSONObject productJson = array.getJSONObject(i);
//                    ProductCardDto dto = new ProductCardDto(productJson);
//                    products.add(dto);
//                }
//                intent.putExtra(Global.PRODUCT_CARD, products);
//            }
//            context.startActivity(intent);
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }


//    // TODO: 15. 11. 23. 제품 등록 응답
//    private void processInsertProduct(JSONObject object) {
//        try {
//            int code = object.getInt(Global.CODE);
//            Log.d(TAG, "제품 등록 응답 object = " + object);
//
//            Intent intent = new Intent(context, AddProductsActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent.putExtra(Global.COMMAND, Global.INSERT_PRODUCT);
//            intent.putExtra(Global.CODE, code);
//
//            if (code == SocketException.SUCCESS) {
//                JSONArray array = object.getJSONArray(Global.PRODUCT);
//                ArrayList<ProductEntity> productEntities = new ArrayList<>();
//                for (int i = 0; i < array.length(); i++) {
//                    JSONObject productJson = array.getJSONObject(i);
//                    ProductEntity productEntity = new ProductEntity(productJson);
//                    productEntities.add(productEntity);
//                }
//                intent.putExtra(Global.PRODUCT, productEntities);
//            }
//
//            context.startActivity(intent);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }


//    // TODO: 15. 11. 23. 타임라인 업데이트 응답
//    private void processUpdateTimeline(JSONObject object) {
//        try {
//            final int code = object.getInt(Global.CODE);
//            Log.d(TAG, "타임라인 업데이트 object = " + object);
//
////            Intent intent = new Intent(context, TimelineWriteActivity.class);
////            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
////            intent.putExtra(Global.COMMAND, Global.UPDATE_TIMELINE);
////            intent.putExtra(Global.CODE, code);
////            context.startActivity(intent);
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }


    // TODO: 15. 11. 23. 타임라인 글 모두 불러오기 응답
//    private void processGetAllTimeline(JSONObject object) {
//        try {
//            final int code = object.getInt(Global.CODE);
//            Log.d(TAG, "타임라인 글 모두 불러오기 응답");
//            Log.d(TAG, "object = " + object);
//
//            if (code == SocketException.SUCCESS) {
//                // 성공
//                final ArrayList<TimelineCardDto> timelineCardDtos = new ArrayList<>();
//                JSONArray timelineArray = object.getJSONArray(Global.TIMELINE);
//                for (int i = 0; i < timelineArray.length(); i++) {
//                    JSONObject timelineObject = timelineArray.getJSONObject(i);
//                    TimelineCardDto timelineCardDto = new TimelineCardDto();
//                    timelineCardDto.setTimeline(timelineObject);
//                    timelineCardDto.setUser(timelineObject);
//                    timelineCardDto.setLike(timelineObject);
//                    if (i != 0 && timelineCardDto.isSame(timelineCardDtos.get(i - 1))) {
//                        timelineCardDtos.get(i - 1).addFile(timelineObject);
//                    } else {
//                        timelineCardDto.setFile(timelineObject);
//                        timelineCardDtos.add(timelineCardDto);
//                    }
//                }
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        RequestManager.onGetAllTimeline.onSuccess(timelineCardDtos);
//                    }
//                });
//            } else {
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        RequestManager.onGetAllTimeline.onException(code);
//                    }
//                });
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }


    // TODO: 15. 11. 23. 타임라인 내 글 불러오기 응답
//    private void processGetMyTimeline(JSONObject object) {
//        try {
//            final int code = object.getInt(Global.CODE);
//            Log.d(TAG, "타임라인 내 글 불러오기 응답");
//            Log.d(TAG, "object = " + object);
//
//            if (code == SocketException.SUCCESS) {
//                // 성공
//                final ArrayList<TimelineCardDto> timelineCardDtos = new ArrayList<>();
//                JSONArray timelineArray = object.getJSONArray(Global.TIMELINE);
//                for (int i = 0; i < timelineArray.length(); i++) {
//                    JSONObject timelineObject = timelineArray.getJSONObject(i);
//                    TimelineCardDto timelineCardDto = new TimelineCardDto();
//                    timelineCardDto.setTimeline(timelineObject);
//                    timelineCardDto.setUser(timelineObject);
//                    timelineCardDto.setLike(timelineObject);
//                    timelineCardDto.setFile(timelineObject);
//                    timelineCardDtos.add(timelineCardDto);
//                }
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        RequestManager.onGetMyTimeline.onSuccess(timelineCardDtos);
//                    }
//                });
//            } else {
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        RequestManager.onGetMyTimeline.onException(code);
//                    }
//                });
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }


    // TODO: 15. 11. 23. 타임라인 글 댓글 불러오기 응답
//    private void processGetTimelineComment(JSONObject object) {
//        try {
//            int code = object.getInt(Global.CODE);
//            Log.d(TAG, "타임라인 글 댓글 불러오기 응답");
//            Log.d(TAG, "object = " + object);
//
//            Intent intent;
//            if (object.getInt(Global.FROM) == 1 || object.getInt(Global.FROM) == 3)
//                intent = new Intent(context, ProductDetailActivity.class);
//            else
//                intent = new Intent(context, TimelineDetailActivity.class);
//            intent.putExtra(Global.COMMAND, Global.GET_TIMELINE_COMMENT);
//            intent.putExtra(Global.CODE, code);
//            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//
//            if (code == SocketException.SUCCESS) {
//                // 성공
//                ArrayList<TimelineCommentCardDto> timelineCommentCardDtos = new ArrayList<>();
//                JSONArray timelineCommentArray = object.getJSONArray(Global.TIMELINE_COMMENT);
//                for (int i = 0; i < timelineCommentArray.length(); i++) {
//                    JSONObject timelineCommentObject = timelineCommentArray.getJSONObject(i);
//                    TimelineCommentCardDto comment = new TimelineCommentCardDto(timelineCommentObject);
//                    timelineCommentCardDtos.add(comment);
//                }
//                intent.putExtra(Global.TIMELINE_COMMENT, timelineCommentCardDtos);
//            }
//
//            context.startActivity(intent);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }


    // TODO: 15. 11. 20. 로그인 응답
    private void processSignIn(JSONObject object) {
        Log.d(TAG, "로그인 응답");
        try {
            int code = object.getInt(Global.CODE);

            Intent intent = new Intent(context, LoginActivity.class);
            if (code == SocketException.SUCCESS) {
                // 성공
                JSONObject userObject = object.getJSONObject(Global.USER);
                UserEntity guest = new UserEntity(userObject);
                intent.putExtra(Global.USER, guest);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void processSingUp(JSONObject object) {
        // 회원가입 응답
        Log.d(TAG, "회원가입 응답");
        try {
            int code = object.getInt(Global.CODE);
            int userType = object.getInt(Global.USER_TYPE);

            Intent intent;
            if (userType == 0) {
                // 게스트모드
                intent = new Intent(context, LoginActivity.class);
            } else {
                // 정식모드
                intent = new Intent(context, SignUpActivity.class);
            }

            if (code == SocketException.SUCCESS) {
                // 성공
                JSONObject userObject = object.getJSONObject(Global.USER);
                UserEntity user = new UserEntity(userObject);
                intent.putExtra(Global.USER, user);
            }
            intent.putExtra(Global.COMMAND, Global.SIGN_UP);
            intent.putExtra(Global.CODE, code);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * TODO: 학교 정보
     * */
//    private void processGetSchool(JSONObject object) {
//        try {
//            int code = object.getInt(Global.CODE);
//            Log.d(TAG, "학교 정보 요청 응답");
//            Log.d(TAG, "object = " + object);
//
//            Intent intent = new Intent(context, LoginActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent.putExtra(Global.COMMAND, Global.GET_SCHOOL);
//            intent.putExtra(Global.CODE, 200);
//
//            if (code == SocketException.SUCCESS) {
//                JSONArray array = object.getJSONArray(Global.SCHOOL);
//                ArrayList<SchoolEntity> schoolEntities = new ArrayList<>();
//
//                for (int i = 0; i < array.length(); i++) {
//                    JSONObject schoolObject = array.getJSONObject(i);
//                    schoolEntities.add(new SchoolEntity(schoolObject));
//                }
//                intent.putExtra(Global.SCHOOL, schoolEntities);
//            }
//
//            context.startActivity(intent);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }


    /**
     * TODO : 로그인 응답
     * */
    private void processLogin(JSONObject object) {
        Log.d(TAG, "로그인 응답");
        try {
            int code = object.getInt(Global.CODE);
            SocketException.printErrMsg(code);

            String id = null;
            if (code == SocketException.SUCCESS)
                id = object.getString(Global.ID);

            Intent intent = new Intent(context, LoginActivity.class);
            intent.putExtra(Global.COMMAND, Global.LOGIN);
            intent.putExtra(Global.ID, id);
            intent.putExtra(Global.CODE, code);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(intent);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public static Socket getSocket() {
        return socket;
    }


    private void socketConnect() {
        socket.open();
        socket.connect();
    }


    public void signUp(UserEntity userEntity) {
        // 회원가입
        Log.d(TAG, "회원가입");

        String userId = userEntity.id;
        String realName = userEntity.realName;
        int sex = userEntity.sex;
        int userType = userEntity.userType;
        String phone = userEntity.phone;
        int schoolId = userEntity.schoolId;

        Log.i(TAG, "userId = " + userId);

        try {
            JSONObject object = new JSONObject();
            object.put(Global.USER_ID, userId);
            object.put(Global.REAL_NAME, realName);
            object.put(Global.SEX, sex);
            object.put(Global.USER_TYPE, userType);
            object.put(Global.PHONE, phone);
            object.put(Global.SCHOOL_ID, schoolId);

            socket.emit(Global.SIGN_UP, object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void signIn(String userId) {
        // 로그인
        Log.d(TAG, "로그인");
        Log.i(TAG, "userId = " + userId);

        try {
            JSONObject object = new JSONObject();
            object.put(Global.USER_ID, userId);

            socket.emit(Global.SIGN_IN, object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * TODO: 학교 정보 요청
     * */
    public static void getSchool(final RequestManager.OnGetSchool onGetSchool) {
        Log.d(TAG, "학교 정보 요청");
        socket.emit(Global.GET_SCHOOL, "");
        socket.once(Global.GET_SCHOOL, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    JSONObject resObject = getJson(args);
                    final int code = getCode(resObject);

                    if (code == SocketException.SUCCESS) {
                        JSONArray array = resObject.getJSONArray(Global.SCHOOL);
                        final ArrayList<SchoolEntity> schoolEntities = new ArrayList<>();

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject schoolObject = array.getJSONObject(i);
                            schoolEntities.add(new SchoolEntity(schoolObject));
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                onGetSchool.onSuccess(schoolEntities);
                            }
                        });
                    } else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                onGetSchool.onException();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
//        insertSchool();
    }


    private void insertSchool() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
//                    String serviceKey = "AgIevc%2B9UJQ8VK0tGD%2FcO1BTMIPNnklsq7Vsa7LT%2Bu6aBTy5b42HH2r9Y4cI1mNdf%2Bp%2BZ%2B%2Bsg5Unml1IJcChuw%3D%3D";
//                    serviceKey = URLEncoder.encode(serviceKey, "UTF-8");
                    String urlStr = "http://api.data.go.kr/openapi/4e1a3cda-db21-40b3-b4f8-a1e7de2993bd?s_page=1&s_list=10000&type=xml&encoding=UTF-8&serviceKey=AgIevc%2B9UJQ8VK0tGD%2FcO1BTMIPNnklsq7Vsa7LT%2Bu6aBTy5b42HH2r9Y4cI1mNdf%2Bp%2BZ%2B%2Bsg5Unml1IJcChuw%3D%3D";
//                    urlStr = URLEncoder.encode(urlStr, "UTF-8");
                    URL url = new URL(urlStr);
                    try {
                        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                        XmlPullParser parser = factory.newPullParser();
                        parser.setInput(url.openStream(), "UTF-8");
                        int eventType = parser.getEventType();

                        final String CATEGORY = "학교급";
                        final String MIDDLE_SCHOOL = "중학교";
                        final String HIGH_SCHOOL = "고등학교";

                        final String ADDRESS = "소재지지번주소";
                        final String NAME = "학교명";
                        final String START = "com.google.gson.internal.LinkedTreeMap";

                        ArrayList<SchoolEntity> schoolEntities = new ArrayList<>();
                        SchoolEntity schoolEntity = null;

                        boolean isCategory = false;
                        boolean isAddress = false;
                        boolean isName = false;
                        boolean isTarget = false;
                        boolean isText = false;

                        while (eventType != XmlPullParser.END_DOCUMENT) {
                            switch (eventType) {
                                case XmlPullParser.START_TAG:
                                    String tag = parser.getName();
                                    if (tag.equals(START)) {
                                        schoolEntity = new SchoolEntity();
                                        Log.d(TAG, "시작");
                                    }
                                    isText = true;
                                    break;

                                case XmlPullParser.TEXT:
                                    String text = parser.getText();
                                    if (text.equals(CATEGORY)) {
                                        isCategory = true;
                                    } else if (text.equals(ADDRESS)) {
                                        isAddress = true;
                                    } else if (text.equals(NAME)) {
                                        isName = true;
                                    } else if (isCategory) {
                                        // 학교급
                                        if (text.equals(MIDDLE_SCHOOL) || text.equals(HIGH_SCHOOL)) {
                                            schoolEntity.category = text;
                                            Log.d(TAG, "타겟임 " + text);
                                            isTarget = true;
                                            isCategory = false;
                                        } else if (isText) {
                                            Log.d(TAG, "타겟아님 " + text);
                                            isTarget = false;
                                            isCategory = false;
                                        }
                                    } else if (isAddress) {
                                        // 주소
                                        if (isTarget && isText) {
                                            Log.d(TAG, text);
                                            schoolEntity = setAddress(schoolEntity, text);
                                            isAddress = false;
                                        }
                                    } else if (isName) {
                                        if (isTarget && isText) {
                                            Log.d(TAG, text);
                                            schoolEntity.schoolname = text;
                                            isName = false;
                                        }
                                    }
                                    break;

                                case XmlPullParser.END_TAG:
                                    String endTag = parser.getName();
                                    if (endTag.equals(START)) {
                                        if (isTarget) {
                                            schoolEntity.id = schoolEntities.size() + 1;
                                            schoolEntities.add(schoolEntity);
                                            Log.d(TAG, "끝");
                                        }
                                    }

                                    isText = false;
                                    break;
                            }

                            eventType = parser.next();
                        }

                        Gson gson = new Gson();
                        String json = gson.toJson(schoolEntities);
                        JSONArray array = new JSONArray(json);
                        Log.d(TAG, "array = " + array);
                        socket.emit("insertSchool", array);
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
//                    Log.d(TAG, "urlStr = " + urlStr);
//                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
//                    httpURLConnection.setRequestMethod("GET");
//                    if (httpURLConnection != null) {
//                        int resCode = httpURLConnection.getResponseCode();
//                        if (resCode == HttpURLConnection.HTTP_OK) {
//                            StringBuilder sb = new StringBuilder();
//                            BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8"));
//                            while (true) {
//                                String line = reader.readLine();
//                                if (line == null)
//                                    break;
//                                sb.append(line);
//                            }
//                            String school = sb.toString();
//                            Log.d(TAG, "school = " + school);
//                            reader.close();
//                            processGetSchool(school);
//                    httpURLConnection.disconnect();
//
//
//                        }
//                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }


    private SchoolEntity setAddress(SchoolEntity schoolEntity, String address) {
        schoolEntity.address = address;

        int firstSpace = address.indexOf(" ");
        int secondSpace = address.indexOf(" ", firstSpace + 1);

        schoolEntity.city = address.substring(0, firstSpace).trim();
        schoolEntity.gu = address.substring(firstSpace, secondSpace).trim();

        Log.d(TAG, "city = " + schoolEntity.city);
        Log.d(TAG, "gu = " + schoolEntity.gu);
        return schoolEntity;
    }


    // TODO: 15. 11. 20. 학교 랭킹 요청
    public static void getSchoolRanking(final RequestManager.OnGetSchoolRanking onGetSchoolRanking) {
        Log.d(TAG, "학교 랭킹 요청");
        socket.emit(Global.GET_SCHOOL_RANKING, "");
        socket.once(Global.GET_SCHOOL_RANKING, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    JSONObject resObject = getJson(args);
                    final int code = getCode(resObject);

                    if (code == SocketException.SUCCESS) {
                        // 성공
                        JSONArray schoolArray = resObject.getJSONArray(Global.SCHOOL);
                        final ArrayList<SchoolRanking> schoolRankingList = new ArrayList<>();
                        for (int i = 0; i < schoolArray.length(); i++) {
                            JSONObject schoolObject = schoolArray.getJSONObject(i);
                            SchoolRanking schoolRanking = new SchoolRanking(schoolObject);
                            schoolRankingList.add(schoolRanking);
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                onGetSchoolRanking.onSuccess(schoolRankingList);
                            }
                        });
                    } else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                onGetSchoolRanking.onException();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // TODO: 15. 11. 20. 제품검색
    public static void searchProduct(int schoolId, int sex, int category, int size, int position, final RequestManager.OnSearchProduct onSearchProduct) {
        Log.d(TAG, "제품 검색 ");
        try {
            JSONObject object = new JSONObject();
            object.put(Global.SCHOOL_ID, schoolId);
            object.put(Global.SEX, sex);
            object.put(Global.CATEGORY, category);
            object.put(Global.SIZE, size);
            object.put(Global.POSITION, position);
            Log.d(TAG, "searchProduct Object = " + object);
            socket.emit(Global.SEARCH_PRODUCT, object);
            socket.once(Global.SEARCH_PRODUCT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        JSONObject reqObject = getJson(args);
                        int code = getCode(reqObject);
                        if (code == SocketException.SUCCESS) {
                            final ArrayList<ProductCardDto> products = new ArrayList<>();
                            JSONArray array = reqObject.getJSONArray(Global.PRODUCT);
                            Log.d(TAG, "제품 검색 array = " + array);
                            Log.d(TAG, "제품 검색 arraySize = " + array.length());
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject productJson = array.getJSONObject(i);
                                ProductCardDto dto = new ProductCardDto(productJson);
                                if (i != 0) {
                                    int size = products.size() - 1;
                                    if (products.get(size).isSame(dto)) {
                                        products.get(size).addFile(dto.fileEntities.get(0));
                                        continue;
                                    }
                                }
                                products.add(dto);
                            }
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d(TAG, "제품 검색 크기 = " + products.size());
                                    onSearchProduct.onSuccess(products);
                                }
                            });
                        } else {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    onSearchProduct.onException();
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private static JSONObject getJson(Object... args) {
        JSONObject object = (JSONObject) args[0];
        return object;
    }


    private static int getCode(JSONObject object) throws JSONException{
        return object.getInt(Global.CODE);
    }


    // TODO: 15. 11. 20. 제품 등록
    public static void insertProduct(ProductCardDto productCardDto, final RequestManager.OnInsertProduct onInsertProduct) {
        try {
            Gson gson = new Gson();
            JSONObject object = new JSONObject(gson.toJson(productCardDto.productEntity));
            Log.d(TAG, "insertProduct Object = " + object);
            socket.emit(Global.INSERT_PRODUCT, object);
            socket.once(Global.INSERT_PRODUCT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        JSONObject reqObject = (JSONObject) args[0];
                        int code = reqObject.getInt(Global.CODE);
                        Log.d(TAG, "제품 등록 응답 = " + reqObject);

                        if (code == SocketException.SUCCESS) {
                            JSONObject productJson = reqObject.getJSONObject(Global.PRODUCT);
                            final ProductCardDto resProductCardDto = new ProductCardDto();
                            resProductCardDto.productEntity = new ProductEntity(productJson);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    onInsertProduct.onSuccess(resProductCardDto);
                                }
                            });
                        } else {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    onInsertProduct.onException();
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    // TODO: 15. 11. 21. 유저 프로필 업데이트
    public static void updateUserProfile(UserEntity user, final RequestManager.OnUpdateUserProfile onUpdateUserProfile) {
        try {
            Gson gson = new Gson();
            String json = gson.toJson(user, UserEntity.class);
            JSONObject object = new JSONObject(json);
            Log.d(TAG, "updateUserProfile Object = " + object);
            socket.emit(Global.UPDATE_USER_PROFILE, object);
            socket.once(Global.UPDATE_USER_PROFILE, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        JSONObject resObject = getJson(args);
                        final int code = getCode(resObject);
                        if (code == SocketException.SUCCESS) {
                            // 성공
                            JSONObject userObject = resObject.getJSONObject(Global.USER);
                            final UserEntity user = new UserEntity(userObject);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    onUpdateUserProfile.onSuccess(user);
                                }
                            });
                        } else {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    onUpdateUserProfile.onException();
                                }
                            });
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    // TODO: 15. 11. 23. 타임라인 게시글에 댓글 달기
    public static void insertTimelineComment(String timelineItemId, String commentContent, String userId, final RequestManager.OnInsertTimelineComment onInsertTimelineComment) {
        try {
            JSONObject object = new JSONObject();
            object.put(Global.TIMELINE_ITEM_ID, timelineItemId);
            object.put(Global.COMMENT_CONTENT, commentContent);
            object.put(Global.USER_ID, userId);
            Log.d(TAG, "insertTimelineComment Object = " + object);
            socket.emit(Global.INSERT_TIMELINE_COMMENT, object);
            socket.once(Global.INSERT_TIMELINE_COMMENT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        JSONObject resObject = getJson(args);
                        final int code = getCode(resObject);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (code == SocketException.SUCCESS) {
                                    onInsertTimelineComment.onSuccess();
                                } else {
                                    onInsertTimelineComment.onException();
                                }
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    // TODO: 15. 11. 23. 타임라인 게시글 댓글 불러오기
    public static void getTimelineComment(String timelineId, final RequestManager.OnGetTimelineComment onGetTimelineComment) {
        try {
            JSONObject object = new JSONObject();
            object.put(Global.ID, timelineId);
            Log.d(TAG, "getTimelineComment Object = " + object);
            socket.emit(Global.GET_TIMELINE_COMMENT, object);
            socket.once(Global.GET_TIMELINE_COMMENT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        JSONObject resObject = getJson(args);
                        final int code = getCode(resObject);

                        if (code == SocketException.SUCCESS) {
                            // 성공
                            final ArrayList<TimelineCommentCardDto> timelineCommentCardDtos = new ArrayList<>();
                            JSONArray timelineCommentArray = resObject.getJSONArray(Global.TIMELINE_COMMENT);
                            for (int i = 0; i < timelineCommentArray.length(); i++) {
                                JSONObject timelineCommentObject = timelineCommentArray.getJSONObject(i);
                                TimelineCommentCardDto comment = new TimelineCommentCardDto(timelineCommentObject);
                                timelineCommentCardDtos.add(comment);
                            }
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    onGetTimelineComment.onSuccess(timelineCommentCardDtos);
                                }
                            });
                        } else {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    onGetTimelineComment.onException();
                                }
                            });
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    // TODO: 15. 11. 23. 타임라인 글 모두 불러오기
    public static void getAllTimeline(int schoolId, String userId, final RequestManager.OnGetAllTimeline onGetAllTimeline) {
        try {
            JSONObject object = new JSONObject();
            object.put(Global.SCHOOL_ID, schoolId);
            object.put(Global.USER_ID, userId);
            Log.d(TAG, "getAllTimeline Object = " + object);
            socket.emit(Global.GET_ALL_TIMELINE, object);
            socket.once(Global.GET_ALL_TIMELINE, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        JSONObject resObject = getJson(args);
                        final int code = getCode(resObject);
                        if (code == SocketException.SUCCESS) {
                            // 성공
                            final ArrayList<TimelineCardDto> timelineCardDtos = new ArrayList<>();
                            JSONArray timelineArray = resObject.getJSONArray(Global.TIMELINE);
                            for (int i = 0; i < timelineArray.length(); i++) {
                                JSONObject timelineObject = timelineArray.getJSONObject(i);
                                TimelineCardDto timelineCardDto = new TimelineCardDto();
                                timelineCardDto.setTimeline(timelineObject);
                                timelineCardDto.setUser(timelineObject);
                                timelineCardDto.setLike(timelineObject);
                                int size = timelineCardDtos.size() - 1;
                                if (i != 0 && timelineCardDto.isSame(timelineCardDtos.get(size))) {
                                    timelineCardDtos.get(size).addFile(timelineObject);
                                } else {
                                    timelineCardDto.setFile(timelineObject);
                                    timelineCardDtos.add(timelineCardDto);
                                }
                            }
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    onGetAllTimeline.onSuccess(timelineCardDtos);
                                }
                            });
                        } else {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    onGetAllTimeline.onException(code);
                                }
                            });
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    // TODO: 15. 11. 23. 타임라인 내 글 불러오기
    public static void getMyTimeline(int schoolId, String userId, final RequestManager.OnGetMyTimeline onGetMyTimeline) {
        try {
            JSONObject object = new JSONObject();
            object.put(Global.SCHOOL_ID, schoolId);
            object.put(Global.USER_ID, userId);
            Log.d(TAG, "getMyTimeline Object = " + object);
            socket.emit(Global.GET_MY_TIMELINE, object);
            socket.once(Global.GET_MY_TIMELINE, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        JSONObject resObject = getJson(args);
                        final int code = getCode(resObject);
                        if (code == SocketException.SUCCESS) {
                            // 성공
                            final ArrayList<TimelineCardDto> timelineCardDtos = new ArrayList<>();
                            JSONArray timelineArray = resObject.getJSONArray(Global.TIMELINE);
                            for (int i = 0; i < timelineArray.length(); i++) {
                                JSONObject timelineObject = timelineArray.getJSONObject(i);
                                TimelineCardDto timelineCardDto = new TimelineCardDto();
                                timelineCardDto.setTimeline(timelineObject);
                                timelineCardDto.setUser(timelineObject);
                                timelineCardDto.setLike(timelineObject);
                                timelineCardDto.setFile(timelineObject);
                                timelineCardDtos.add(timelineCardDto);
                            }
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    onGetMyTimeline.onSuccess(timelineCardDtos);
                                }
                            });
                        } else {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    onGetMyTimeline.onException(code);
                                }
                            });
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    // TODO: 15. 11. 23. 타임라인 글 쓰기
    public static void insertTimeline(int schoolId, String timelineContent, String id, ArrayList<Uri> files, final RequestManager.OnInsertTimeline onInsertTimeline) {
        try {
            Log.d(TAG, "schoolId = " + schoolId);
            Log.d(TAG, "timelineContent = " + timelineContent);

            Gson gson = new Gson();
            JSONArray array = new JSONArray(gson.toJson(files));
            JSONObject object = new JSONObject();
            object.put(Global.SCHOOL_ID, schoolId);
            object.put(Global.USER_ID, id);
            object.put(Global.TIMELINE_CONTENT, timelineContent);
            object.put(Global.FILE, array);
            Log.d(TAG, "insertTimeline Object = " + object);
            socket.emit(Global.INSERT_TIMELINE, object);
            socket.once(Global.INSERT_TIMELINE, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        JSONObject resObject = getJson(args);
                        final int code = getCode(resObject);
                        if (code == SocketException.SUCCESS) {
                            final JSONObject timelineJson = resObject.getJSONObject(Global.TIMELINE);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    TimelineCardDto dto = new TimelineCardDto(timelineJson);
                                    onInsertTimeline.onSuccess(dto);
                                }
                            });
                        } else {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    onInsertTimeline.onException();
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    // TODO: 15. 11. 23. 파일 지우기
    public static void deleteFile(ArrayList<FileEntity> files, final RequestManager.OnDeleteFile onDeleteFile) {
        try {
            for (int i = 0; i < files.size(); i++) {
                Log.d(TAG, i + "번째 id = " + files.get(i).id);
                Log.d(TAG, i + "번째 parent_uuid = " + files.get(i).parent_uuid);

                if (files.get(i).id == null || files.get(i).id.equals(null))
                    return;

                if (files.get(i).parent_uuid == null || files.get(i).parent_uuid.equals(null))
                    return;
            }

            Gson gson = new Gson();
            String json = gson.toJson(files);
            JSONArray array = new JSONArray(json);
            JSONObject object = new JSONObject();
            object.put(Global.FILE, array);
            Log.d(TAG, "deleteFile Object = " + object);
            socket.emit(Global.DELETE_FILE, object);
            socket.once(Global.DELETE_FILE, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        JSONObject resObject = getJson(args);
                        final int code = getCode(resObject);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (code == SocketException.SUCCESS) {
                                    onDeleteFile.onSuccess();
                                } else {
                                    onDeleteFile.onException();
                                }
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    // TODO: 15. 11. 23. 타임라인 업데이트
    public static void updateTimeline(TimelineCardDto timelineCardDto, final RequestManager.OnInsertTimeline onInsertTimeline) {
        try {
            Gson gson = new Gson();
            String json = gson.toJson(timelineCardDto);
            JSONObject object = new JSONObject(json);
            Log.d(TAG, "updateTimeline Object = " + object);
            socket.emit(Global.UPDATE_TIMELINE, object);
            socket.once(Global.UPDATE_TIMELINE, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        final JSONObject resObject = getJson(args);
                        final int code = getCode(resObject);
                        if (code == SocketException.SUCCESS) {
                            JSONObject timelineJson = resObject.getJSONObject(Global.TIMELINE);
                            final TimelineCardDto resTimelineCardDto = new TimelineCardDto(timelineJson);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    onInsertTimeline.onSuccess(resTimelineCardDto);
                                }
                            });
                        } else {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    onInsertTimeline.onException();
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    // TODO: 15. 11. 24. 내 제품 요청
    public static void getMyProduct(String userId, final RequestManager.OnGetMyProduct onGetMyProduct) {
        try {
            JSONObject object = new JSONObject();
            object.put(TransactionEntity.PROPERTY_DONATOR_UUID, userId);
            object.put(TransactionEntity.PROPERTY_RECEIVER_UUID, userId);
            Log.d(TAG, "getMyProduct Object = " + object);
            socket.emit(Global.GET_MY_PRODUCT, object);
            socket.once(Global.GET_MY_PRODUCT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        JSONObject reqObject = (JSONObject) args[0];
                        final int code = reqObject.getInt(Global.CODE);
                        if (code == SocketException.SUCCESS) {
                            final ArrayList<ProductCardDto> productCardDtos = new ArrayList<>();
                            JSONArray array = reqObject.getJSONArray(Global.PRODUCT);
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject productObject = array.getJSONObject(i);
                                ProductCardDto dto = new ProductCardDto(productObject);
                                if (i != 0) {
                                    int size = productCardDtos.size() - 1;
                                    if (productCardDtos.get(size).isSame(dto)) {
                                        productCardDtos.get(size).addFile(dto.fileEntities.get(0));
                                        continue;
                                    }
                                }
                                productCardDtos.add(dto);
                            }

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    onGetMyProduct.onSuccess(productCardDtos);
                                }
                            });
                        } else {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    onGetMyProduct.onException(code);
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    // TODO: 15. 11. 24. 댓글 삭제 요청
    public static void deleteComment(TimelineCommentCardDto timelineCommentCardDto, final RequestManager.OnDeleteComment onDeleteComment) {
        try {
            JSONObject object = new JSONObject();
            object.put(Global.ID, timelineCommentCardDto.commentEntity.id);
            object.put(Global.TIMELINE_ITEM_ID, timelineCommentCardDto.commentEntity.timeline_item_id);
            object.put(Global.USER_ID, timelineCommentCardDto.commentEntity.user_id);
            Log.d(TAG, "deleteComment Object = " + object);
            socket.emit(Global.DELETE_COMMENT, object);
            socket.once(Global.DELETE_COMMENT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        JSONObject resObject = getJson(args);
                        final int code = getCode(resObject);

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (code == SocketException.SUCCESS)
                                    onDeleteComment.onSuccess();
                                else
                                    onDeleteComment.onException();
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public  void updateTransactionStatus(int status, TransactionEntity transactionEntity) {
        try {
            Gson gson = new Gson();
            String json = gson.toJson(transactionEntity, TransactionEntity.class);
            JSONObject transJson = new JSONObject(json);

            JSONObject object = new JSONObject();
            object.put(Global.STATUS, status);
            object.put(Global.TRANSACTION, transJson);
            Log.d(TAG, "updateTransactionStatus Object = " + object);
            socket.emit(Global.UPDATE_TRANSACTION_STATUS, object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    // TODO: 15. 11. 25. 제품 삭제
    public static void deleteProduct(String productId, final RequestManager.OnDeleteProduct onDeleteProduct) {
        try {
            JSONObject object = new JSONObject();
            object.put(Global.PRODUCT_ID, productId);
            Log.d(TAG, "deleteProduct Object = " + object);
            socket.emit(Global.DELETE_PRODUCT, object);
            socket.once(Global.DELETE_PRODUCT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        JSONObject resObject = getJson(args);
                        final int code = getCode(resObject);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (code == SocketException.SUCCESS) {
                                    onDeleteProduct.onSuccess();
                                } else {
                                    onDeleteProduct.onException();
                                }
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    // TODO: 15. 11. 25. 제품 수정
    public static void updateProduct(ProductCardDto productCardDto, final RequestManager.OnUpdateProduct onUpdateProduct) {
        try {
            Gson gson = new Gson();
            String json = gson.toJson(productCardDto);
            JSONObject object = new JSONObject(json);
            Log.d(TAG, "updateProduct Object = " + object);
            socket.emit(Global.UPDATE_PRODUCT, object);
            socket.once(Global.UPDATE_PRODUCT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        JSONObject resObject = getJson(args);
                        final int code = getCode(resObject);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (code == SocketException.SUCCESS) {
                                    onUpdateProduct.onSuccess();
                                } else {
                                    onUpdateProduct.onException();
                                }
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    // TODO: 15. 11. 25. 파일 입력
    public static void insertFile(String id, String path, RequestManager.OnInsertFile onInsertFile) {
//        try {
            String serverUrl = SERVER_URL + "/api/photo";

            Log.d(TAG, "productId = " + id);
            Log.d(TAG, "path = " + path);

            upload(serverUrl, path, id, onInsertFile);

//            object.put(Global.PRODUCT_ID, id);
//            object.put(Global.PATH, path);
//            object.put(Global.FILE, fileName);
//            Log.d(TAG, "insertFile Object = " + object);
//            socket.emit(Global.INSERT_FILE, object);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
    }


    private static void upload(final String serverUrl, final String fileUrl, final String id, final RequestManager.OnInsertFile onInsertFile) {
        Log.d(TAG, "fileUrl = " + fileUrl);
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection conn = null;
                DataOutputStream dos = null;

                final String LINE_END = "\r\n";
                final String TWO_HYPHENS = "--";
                final String BOUNDARY = "*****";
                int bytesRead, bytesAvailable, bufferSize;
                byte[] buffer;
                final int MAX_BUFFER_SIZE = 1024 * 1024;
                File file = new File(fileUrl);

                if (!file.isFile()) {
                    Log.e(TAG, "파일아님 = " + fileUrl);
                    return;
                }

                try {
                    Log.d(TAG, "파일은 맞음");
                    FileInputStream fis = new FileInputStream(file);
                    URL url = new URL(serverUrl);

                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setUseCaches(false);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                    conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + BOUNDARY);
                    conn.setRequestProperty("uploaded_file", fileUrl);
                    conn.setRequestProperty("parent_id", id);

                    dos = new DataOutputStream(conn.getOutputStream());
                    dos.writeBytes(TWO_HYPHENS + BOUNDARY + LINE_END);
                    dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                            + fileUrl + "\"" + LINE_END);
                    dos.writeBytes(LINE_END);

                    bytesAvailable = fis.available();

                    bufferSize = Math.min(bytesAvailable, MAX_BUFFER_SIZE);
                    buffer = new byte[bufferSize];

                    bytesRead = fis.read(buffer, 0, bufferSize);

                    while (bytesRead > 0) {
                        dos.write(buffer, 0, bufferSize);
                        bytesAvailable = fis.available();
                        bufferSize = Math.min(bytesAvailable, MAX_BUFFER_SIZE);
                        bytesRead = fis.read(buffer, 0, bufferSize);
                    }

                    dos.writeBytes(LINE_END);
                    dos.writeBytes(TWO_HYPHENS + BOUNDARY + TWO_HYPHENS + LINE_END);
                    Log.d(TAG, "연결");
                    final int serverResCode = conn.getResponseCode();
                    String serverResMsg = conn.getResponseMessage();

                    if (serverResCode == 200) {
                        Log.d(TAG, "서버 메세지 = " + serverResMsg);
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        final StringBuilder sb = new StringBuilder();
                        String str = null;
                        while ((str = bufferedReader.readLine()) != null) {
                            sb.append(str);
                        }
                        serverResMsg = sb.toString();
                        Log.d(TAG, "서버 메세지2 = " + serverResMsg);
                        bufferedReader.close();

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                onInsertFile.onSuccess();
                            }
                        });
                    } else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                onInsertFile.onException(serverResCode);
                            }
                        });
                    }

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    // TODO: 15. 11. 25. 타임라인 지우기
    public static void deleteTimeline(TimelineCardDto timelineCardDto, final RequestManager.OnDeleteTimeline onDeleteTimeline) {
        String timelineId = timelineCardDto.timelineEntity.id;
        String userId = timelineCardDto.userEntity.id;

        try {
            JSONObject object = new JSONObject();
            object.put(Global.TIMELINE_ITEM_ID, timelineId);
            object.put(Global.USER_ID, userId);
            Log.d(TAG, "deleteTimeLine Object = " + object);
            socket.emit(Global.DELETE_TIMELINE, object);
            socket.once(Global.DELETE_TIMELINE, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        JSONObject resObject = getJson(args);
                        final int code = getCode(resObject);

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (code == SocketException.SUCCESS)
                                    onDeleteTimeline.onSuccess();
                                else
                                    onDeleteTimeline.onException(code);
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    // TODO: 15. 11. 25. 좋아요 지우기
    public static void deleteLike(LikeEntity likeEntity, final RequestManager.OnDeleteLike onDeleteLike) {
        try {
            Gson gson = new Gson();
            String json = gson.toJson(likeEntity);
            JSONObject object = new JSONObject(json);
            Log.d(TAG, "deleteLike Object = " + object);
            socket.emit(Global.DELETE_LIKE, object);
            socket.once(Global.DELETE_LIKE, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        JSONObject resObject = getJson(args);
                        final int code = getCode(resObject);

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (code == SocketException.SUCCESS)
                                    onDeleteLike.onSuccess();
                                else
                                    onDeleteLike.onException(code);
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    // TODO: 15. 11. 25. 좋아요
    public static void insertLike(String timelineItemId, String userId, final RequestManager.OnInsertLike onInsertLike) {
        try {
            JSONObject object = new JSONObject();
            object.put(Global.TIMELINE_ITEM_ID, timelineItemId);
            object.put(Global.USER_ID, userId);
            Log.d(TAG, "insertLike Object = " + object);
            socket.emit(Global.INSERT_LIKE, object);
            socket.once(Global.INSERT_LIKE, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        JSONObject resObject = getJson(args);
                        final int code = getCode(resObject);
                        if (code == SocketException.SUCCESS) {
                            JSONObject likeObject = resObject.getJSONObject(Global.LIKE);
                            final LikeEntity likeEntity = new LikeEntity(likeObject);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    onInsertLike.onSuccess(likeEntity);
                                }
                            });
                        } else {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    onInsertLike.onException(code);
                                }
                            });
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    // TODO: 15. 11. 25. 제품 요청
    public void getProduct(String productJson) {
        try {
            JSONArray array = new JSONArray(productJson);
            Log.d(TAG, "getProduct Array = " + array.toString());
            socket.emit(Global.GET_PRODUCT, array);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    // TODO: 15. 11. 28.
    public static void insertTransaction(TransactionEntity transactionEntity) {
        try {
            Gson gson = new Gson();
            JSONObject jsonObject = new JSONObject(gson.toJson(transactionEntity));
            Log.d(TAG, "insertTransaction object = " + jsonObject);
            socket.emit(Global.INSERT_TRANSACTION, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    // TODO: 15. 11. 28. 카카오 로그인
    public static void signInKakao(long id, final RequestManager.OnSignInKakao onSignInKakao) {
        try {
            JSONObject object = new JSONObject();
            object.put(Global.ID, String.valueOf(id));
            Log.d(TAG, "signInKakao Object = " + object);
            socket.emit(Global.SIGN_IN_KAKAO, object);
            socket.once(Global.SIGN_IN_KAKAO, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        JSONObject resObject = getJson(args);
                        final int code = getCode(resObject);

                        if (code == SocketException.SUCCESS) {
                            // 성공
                            JSONObject userObject = resObject.getJSONObject(Global.USER);
//                            String id = userObject.getString(Global.ID);
//                            String picture = userObject.getString(Global.PICTURE);
//                            String phone = userObject.getString(Global.PHONE);
//                            String realName = userObject.getString(Global.REAL_NAME);
//                            String name = userObject.getString(Global.NAME);
//                            String hasExtraProfile = userObject.getString(Global.HAS_EXTRA_PROFILE);
//                            int sex = userObject.getInt(Global.SEX);
//                            int userType = userObject.getInt(Global.USER_TYPE);
//                            int schoolId = userObject.getInt(Global.SCHOOL_ID);
                            UserEntity user = new UserEntity(userObject);
                            onSignInKakao.onSuccess(user);
                        } else {
                            onSignInKakao.onException(code);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public static void transactionPush(ArrayList<String> userIds, String msg) {
        try {
            Gson gson = new Gson();
            String userJson = gson.toJson(userIds);
            JSONArray userArray = new JSONArray(userJson);
            JSONObject object = new JSONObject();
            object.put(Global.USER, userArray);
            object.put(Global.MESSAGE, msg);
            socket.emit(Global.TRANSACTION_PUSH, object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}