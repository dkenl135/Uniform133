package com.songjin.usum.controllers.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.afollestad.materialdialogs.MaterialDialog;
import com.kth.baasio.Baas;
import com.kth.baasio.callback.BaasioCallback;
import com.kth.baasio.callback.BaasioDownloadCallback;
import com.kth.baasio.callback.BaasioUploadCallback;
import com.kth.baasio.entity.entity.BaasioEntity;
import com.kth.baasio.entity.file.BaasioFile;
import com.kth.baasio.exception.BaasioException;
import com.songjin.usum.R;
import com.songjin.usum.controllers.views.AttachedImageRecyclerView;
import com.songjin.usum.controllers.views.WriterView;
import com.songjin.usum.dtos.TimelineCardDto;
import com.songjin.usum.entities.FileEntity;
import com.songjin.usum.entities.UserEntity;
import com.songjin.usum.managers.RequestManager;

import java.io.File;
import java.util.ArrayList;

import nl.changer.polypicker.ImagePickerActivity;

public class TimelineWriteActivity extends BaseActivity {
    private class ViewHolder {
        public WriterView writerView;
        public EditText timelineContents;
        public AttachedImageRecyclerView selectedImages;
        public Button attachPhotoButton;

        public ViewHolder(View view) {
            writerView = (WriterView) view.findViewById(R.id.writer_view);
            timelineContents = (EditText) view.findViewById(R.id.contents);
            selectedImages = (AttachedImageRecyclerView) view.findViewById(R.id.selected_images);
            attachPhotoButton = (Button) view.findViewById(R.id.attach_photo);
        }
    }

    private ViewHolder viewHolder;
    private Boolean isUpdate;
    private TimelineCardDto timelineCardDtoForUpdate;

    private static final int INTENT_REQUEST_GET_IMAGES = 1;
    private static final int MAXIMUM_IMAGES = 10;
    private Menu menu;
    private ArrayList<Uri> selectedImageUris;
    private TimelineInsertCallback timelineInsertCallback = new TimelineInsertCallback();
    private FileUploadCallback fileUploadCallback = new FileUploadCallback();
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isUpdate = getIntent().getBooleanExtra("isUpdate", false);

        context = this;
        selectedImageUris = new ArrayList<>();
        initViews(R.layout.activity_timeline_write);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, 0, 0, "쓰기").setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        if (this.menu == null) {
            this.menu = menu;
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case 0:
                String contents = viewHolder.timelineContents.getText().toString();
                if (contents.isEmpty()) {
                    new MaterialDialog.Builder(BaseActivity.context)
                            .title(R.string.app_name)
                            .content("내용을 입력해주세요.")
                            .show();
                    return true;
                }

                UserEntity userEntity = new UserEntity(Baas.io().getSignedInUser());

                onWriteBefore();
                if (isUpdate) {
                    timelineCardDtoForUpdate.timelineEntity.contents = contents;
                    RequestManager.deleteFileEntities(timelineCardDtoForUpdate.fileEntities);
                    RequestManager.updateTimeline(timelineCardDtoForUpdate, timelineInsertCallback);
                } else {
                    RequestManager.insertTimeline(userEntity.schoolId, contents, timelineInsertCallback);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onWriteBefore() {
        menu.getItem(0).setEnabled(false);
        showLoadingView();
    }

    private void onWriteAfter(boolean isSuccess) {
        menu.getItem(0).setEnabled(true);
        hideLoadingView();
        if (isSuccess) {
            finish();
        } else {
            new MaterialDialog.Builder(BaseActivity.context)
                    .title(R.string.app_name)
                    .content("타임라인을 쓰는 중에 문제가 발생하였습니다.")
                    .show();
        }
    }

    @Override
    protected void initViews(int layoutResID) {
        setContentView(layoutResID);

        // 액션바 설정
        getSupportActionBar().setTitle("타임라인 쓰기");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewHolder = new ViewHolder(getWindow().getDecorView());

        viewHolder.attachPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ImagePickerActivity.class);
                intent.putExtra(ImagePickerActivity.EXTRA_SELECTION_LIMIT, MAXIMUM_IMAGES);
                startActivityForResult(intent, INTENT_REQUEST_GET_IMAGES);
            }
        });

        if (isUpdate) {
            timelineCardDtoForUpdate = getIntent().getParcelableExtra("timelineCardDto");
            viewHolder.timelineContents.setText(timelineCardDtoForUpdate.timelineEntity.contents);
            viewHolder.writerView.setUserEntity(timelineCardDtoForUpdate.userEntity);
            viewHolder.writerView.setWrittenTime(timelineCardDtoForUpdate.timelineEntity.created);

            for (FileEntity fileEntity : timelineCardDtoForUpdate.fileEntities) {
                Uri uri = Uri.fromFile(new File(BaseActivity.context.getCacheDir() + fileEntity.uuid));
                selectedImageUris.add(uri);
            }
            viewHolder.selectedImages.setUris(selectedImageUris);
        } else {
            viewHolder.writerView.setUserEntity(new UserEntity(Baas.io().getSignedInUser()));
            viewHolder.writerView.setWrittenTime(System.currentTimeMillis());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resuleCode, Intent intent) {
        super.onActivityResult(requestCode, resuleCode, intent);

        if (resuleCode != RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case INTENT_REQUEST_GET_IMAGES:
                Parcelable[] parcelableUris = intent.getParcelableArrayExtra(ImagePickerActivity.EXTRA_IMAGE_URIS);

                if (parcelableUris == null) {
                    return;
                }

                selectedImageUris.clear();
                for (Parcelable parcelableUri : parcelableUris) {
                    selectedImageUris.add(Uri.parse(parcelableUri.toString()));
                }

                viewHolder.selectedImages.setUris(selectedImageUris);
                break;
        }
    }

    private class TimelineInsertCallback implements BaasioCallback<BaasioEntity> {
        @Override
        public void onResponse(BaasioEntity baasioEntity) {
            if (0 < selectedImageUris.size()) {
                Uri selectedUri = selectedImageUris.get(0);
                selectedImageUris.remove(0);

                String parentUuid = baasioEntity.getUuid().toString();
                RequestManager.insertFile(parentUuid, selectedUri, fileUploadCallback);
            } else {
                onWriteAfter(true);
            }
        }

        @Override
        public void onException(BaasioException e) {
            onWriteAfter(false);
        }
    }

    private class FileUploadCallback implements BaasioUploadCallback {
        @Override
        public void onResponse(BaasioFile baasioFile) {
            if (0 < selectedImageUris.size()) {
                Uri selectedUri = selectedImageUris.get(0);
                selectedImageUris.remove(0);

                String parentUuid = baasioFile.getProperty("parent_uuid").asText();
                RequestManager.insertFile(parentUuid, selectedUri, fileUploadCallback);
            } else {
                onWriteAfter(true);
            }
        }

        @Override
        public void onException(BaasioException e) {
            onWriteAfter(false);
        }

        @Override
        public void onProgress(long l, long l2) {
            // TODO 진행상황 표시
        }
    }
}
