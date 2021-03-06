package com.songjin.usum.controllers.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.helper.SharedPreferencesCache;
import com.songjin.usum.Global;
import com.songjin.usum.R;
import com.songjin.usum.controllers.views.UserProfileForm;
import com.songjin.usum.entities.UserEntity;
import com.songjin.usum.managers.RequestManager;
import com.songjin.usum.socketIo.SocketIO;


public class SignUpActivity extends BaseActivity {
    private static final String TAG = "SignUpActivity";
    private long id;
    private Activity activity = this;

    private class ViewHolder {
        public CheckBox termsAgreementCheckBox;
        public UserProfileForm userProfileForm;

        public ViewHolder(View view) {
            termsAgreementCheckBox = (CheckBox) view.findViewById(R.id.terms_agreement);
            userProfileForm = (UserProfileForm) view.findViewById(R.id.user_profile_form);
        }
    }

    private ViewHolder viewHolder;

    protected String getDeviceUUID() {
        SharedPreferencesCache cache = Session.getAppCache();
        String curId = cache.getString("device_id");
        if (curId == null) {
            Bundle bundle = new Bundle();
            curId = getUniqueId();
            bundle.putString("device_id", curId);
            cache.save(bundle);
        }
        return curId;
    }

    private String getUniqueId() {
        return Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ANDROID_ID
        ) + System.currentTimeMillis();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestCheckConfirmedUser();
    }


    public void initViews(int layoutResID) {
        setContentView(layoutResID);

        // 컨트롤변수 초기화
        viewHolder = new ViewHolder(getWindow().getDecorView());
        viewHolder.userProfileForm.setUserId(id);
        viewHolder.userProfileForm.setOnSubmitListener(new UserProfileForm.OnSubmitListener() {
            @Override
            public void onClick(View v) {
                UserEntity userEntity = viewHolder.userProfileForm.getUserEntity();

                // 약관에 동의 했는지 체크하기
                if (!viewHolder.termsAgreementCheckBox.isChecked()) {
                    new MaterialDialog.Builder(activity)
                            .title(R.string.app_name)
                            .content("약관에 동의해주세요.")
                            .show();
                    return;
                }
                String validateMsg = viewHolder.userProfileForm.validateForm();
                if (!validateMsg.isEmpty()) {
                    new MaterialDialog.Builder(activity)
                            .title(R.string.app_name)
                            .content(validateMsg)
                            .show();
                    return;
                }

                // 회원가입 요청
                showProgress();

                RequestManager.signUp(userEntity, new RequestManager.OnSignUp() {
                    @Override
                    public void onSuccess(UserEntity userEntity) {
                        hideLoadingView();
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.putExtra(Global.USER, userEntity);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onException(int code) {
                        new MaterialDialog.Builder(activity)
                                .title(R.string.app_name)
                                .content("회원가입하는데 문제가 발생하였습니다.")
                                .show();
                    }
                });
            }
        });
    }



    // TODO RequestManager에 넣기
    private void requestCheckConfirmedUser() {
        setContentView(R.layout.activity_login);
        final SharedPreferencesCache cache = Session.getAppCache();
        final String kakaoToken = cache.getString(Global.TOKEN);

        UserManagement.requestMe(new MeResponseCallback() {
            @Override
            public void onSessionClosed(ErrorResult errorResult) {

            }

            @Override
            public void onNotSignedUp() {

            }

            @Override
            public void onSuccess(UserProfile userProfile) {
                final String nickName = userProfile.getNickname();
                final String profileImage = userProfile.getProfileImagePath();
                final String thumbnailImage = userProfile.getThumbnailImagePath();

                id = userProfile.getId();

                RequestManager.signInKakao(id, nickName, profileImage, thumbnailImage, new RequestManager.OnSignInKakao() {
                    @Override
                    public void onSuccess(final UserEntity userEntity) {

                        Global.userEntity = userEntity;

                        if (!userEntity.hasExtraProfile) {
                            initViews(R.layout.activity_sign_up);
                        } else if (userEntity.deviceId == null) {
                            SocketIO.setDeviceId(userEntity.id, getDeviceUUID(), new RequestManager.OnSetDeviceId() {
                                @Override
                                public void onSuccess() {
                                    userEntity.setDeviceId(userEntity.id);
                                    String token = kakaoToken;
                                    if (token == null) {
                                        token = Session.getCurrentSession().getAccessToken();
                                        Bundle bundle = new Bundle();
                                        bundle.putString(Global.TOKEN, token);
                                        cache.save(bundle);
                                    }

                                    Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
                                    intent1.putExtra(Global.USER, userEntity);
                                    startActivity(intent1);
                                    finish();
                                }

                                @Override
                                public void onException() {
                                }
                            });
                        } else if (!getDeviceUUID().equals(userEntity.deviceId)) {
                            Toast.makeText(SignUpActivity.this, "이미 다른 기기에서 접속 중입니다.", Toast.LENGTH_SHORT).show();
                            requestLogout();
                        } else {
                            String token = kakaoToken;
                            if (token == null) {
                                token = Session.getCurrentSession().getAccessToken();
                                Bundle bundle = new Bundle();
                                bundle.putString(Global.TOKEN, token);
                                cache.save(bundle);
                            }

                            Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
                            intent1.putExtra(Global.USER, userEntity);
                            startActivity(intent1);
                            finish();
                        }
                    }

                    @Override
                    public void onException(int code) {
                        if (code == 482) {
                            RequestManager.signInKakao(id, nickName, profileImage, thumbnailImage, new RequestManager.OnSignInKakao() {
                                @Override
                                public void onSuccess(final UserEntity userEntity) {

                                    Global.userEntity = userEntity;

                                    if (!userEntity.hasExtraProfile) {
                                        initViews(R.layout.activity_sign_up);
                                    } else if (userEntity.deviceId == null) {
                                        SocketIO.setDeviceId(userEntity.id, getDeviceUUID(), new RequestManager.OnSetDeviceId() {
                                            @Override
                                            public void onSuccess() {
                                                userEntity.setDeviceId(userEntity.id);

                                                String token = kakaoToken;
                                                if (token == null) {
                                                    token = Session.getCurrentSession().getAccessToken();
                                                    Bundle bundle = new Bundle();
                                                    bundle.putString(Global.TOKEN, token);
                                                    cache.save(bundle);
                                                }

                                                Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
                                                intent1.putExtra(Global.USER, userEntity);
                                                startActivity(intent1);
                                                finish();
                                            }

                                            @Override
                                            public void onException() {
                                            }
                                        });
                                    } else if (!getDeviceUUID().equals(userEntity.deviceId)) {
                                        Toast.makeText(SignUpActivity.this, "이미 다른 기기에서 접속 중입니다.", Toast.LENGTH_SHORT).show();
                                        requestLogout();
                                    } else {
                                        String token = kakaoToken;
                                        if (token == null) {
                                            token = Session.getCurrentSession().getAccessToken();
                                            Bundle bundle = new Bundle();
                                            bundle.putString(Global.TOKEN, token);
                                            cache.save(bundle);
                                        }

                                        Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
                                        intent1.putExtra(Global.USER, userEntity);
                                        startActivity(intent1);
                                        finish();
                                    }
                                }

                                @Override
                                public void onException(int code) {

                                }
                            });
                        } else if (code == 483) {
                            initViews(R.layout.activity_sign_up);
                        } else {
                            new MaterialDialog.Builder(activity)
                                    .title(R.string.app_name)
                                    .content("로그인하는데 문제가 발생하였습니다.")
                                    .show();
                        }
                    }
                });
            }
        });
    }

    MaterialDialog materialDialog;

    private void showProgress() {
        materialDialog = new MaterialDialog.Builder(this)
                .title(R.string.app_name)
                .content("Waiting...")
                .progress(true, 0)
                .cancelable(false)
                .show();
    }


    private void requestLogout() {
        try {
            BaseActivity.showLoadingView();
            new MaterialDialog.Builder(this)
                    .title("이미 다른 기기에서 접속 중 입니다.")
                    .content("로그아웃 시키고 접속 하시겠습니까?")
                    .positiveText("예")
                    .negativeText("아니오")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            SocketIO.setDeviceId(Global.userEntity.id, getDeviceUUID(), new RequestManager.OnSetDeviceId() {
                                @Override
                                public void onSuccess() {
                                    Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
                                    intent1.putExtra(Global.USER, Global.userEntity);
                                    startActivity(intent1);
                                    finish();
                                }

                                @Override
                                public void onException() {

                                }
                            });
                        }
                    }).onNegative(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    dialog.dismiss();
                    finish();
                }
            }).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}