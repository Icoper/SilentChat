package com.example.disemk.silentchat.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RotateDrawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.example.disemk.silentchat.R;
import com.example.disemk.silentchat.engine.SingletonCM;
import com.example.disemk.silentchat.models.ChatMessage;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatFragment extends android.app.Fragment implements SoundPool.OnLoadCompleteListener {

    private static final int REQUEST = 1;
    final int MAX_STREAMS = 5;

    private SoundPool sp;
    private int soundIdShot;
    private int soundIdExplosion;

    private Context context;
    private String romName;
    private boolean canPlaySound;

    private DatabaseReference mDatabaseReference;
    private FirebaseRecyclerAdapter<ChatMessage, FirechatMsgViewHolder> mFBAdapter;
    private RecyclerView mMsgRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private ProgressBar mProgressBar;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseStorage mFirebaseStorage;
    private FirebaseUser mFirebaseUser;
    private StorageReference mStorageReference;

    private String mUsername;
    private String mPhotoUrl;
    private String mUid;

    private Button mSendButtn;
    private EditText mMsgEText;
    private Button mAddPhotoBtn;
    private ImageView mPreviewPhoto;
    private String imgBucked;
    private Uri filePath;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        romName = SingletonCM.getInstance().getUserRoom();
        context = SingletonCM.getInstance().getMainContext();
        setBackground(view);
        initialize(view);

        return view;
    }

    // init all components
    private void initialize(View container) {
        // setup sms View elements
        mMsgEText = (EditText) container.findViewById(R.id.msgEditText);
        mSendButtn = (Button) container.findViewById(R.id.sendButton);
        mAddPhotoBtn = (Button) container.findViewById(R.id.addPhoto);
        mPreviewPhoto = (ImageView) container.findViewById(R.id.user_imgMsg_preview);
        mProgressBar = (ProgressBar) container.findViewById(R.id.progressBar);
        mMsgRecyclerView = (RecyclerView) container.findViewById(R.id.messageRecyclerView);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        // FireBase setup
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mFirebaseStorage = FirebaseStorage.getInstance();
        mUid = mFirebaseUser.getUid();
        mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
        mStorageReference = mFirebaseStorage.getReferenceFromUrl("gs://silentchat-5454d.appspot.com");

        // other install
        hasConnection(context);
        imgBucked = "";
        mLayoutManager = new LinearLayoutManager(context);
        mLayoutManager.setStackFromEnd(true);
        mMsgRecyclerView.setLayoutManager(mLayoutManager);
        sp = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
        sp.setOnLoadCompleteListener(this);
        soundIdShot = sp.load(context, R.raw.new_msg_sound, 1);

        try {
            soundIdExplosion = sp.load(context.getAssets().openFd("explosion.ogg"), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        SingletonCM.getInstance();
        setmFBAdapterUn();
        setBackground(container);

        mSendButtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatMessage frendlyMsg = new ChatMessage();

                if (!mMsgEText.getText().toString().isEmpty()) {
                    if (!imgBucked.isEmpty()) {
                        frendlyMsg = new ChatMessage(
                                mMsgEText.getText().toString(), mUsername, mPhotoUrl, romName, mUid, imgBucked);
                        mPreviewPhoto.setVisibility(View.GONE);
                    } else {
                        frendlyMsg = new ChatMessage(
                                mMsgEText.getText().toString(), mUsername, mPhotoUrl, romName, mUid);

                    }
                    mDatabaseReference.child(romName).push().setValue(frendlyMsg);
                    mMsgEText.setText("");
                } else {
                    Toast.makeText(context, "Enter msg first!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mAddPhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhotoOnMsg();
            }
        });

        mFBAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int frendMsgCount = mFBAdapter.getItemCount();
                int lastPosition = mLayoutManager.findLastCompletelyVisibleItemPosition();

                if (lastPosition == -1 || (positionStart >= (frendMsgCount)) && lastPosition == (positionStart - 1)) {
                    mMsgRecyclerView.scrollToPosition(positionStart);
                }
            }
        });


        mMsgRecyclerView.setLayoutManager(mLayoutManager);
        mMsgRecyclerView.setAdapter(mFBAdapter);
    }

    // launch file manager to choise file to download
    private void takePhotoOnMsg() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST);
    }


    private void setmFBAdapterUn() {
        canPlaySound = false;// разрешено ли воспроизводить звук
        mFBAdapter = new FirebaseRecyclerAdapter<ChatMessage, FirechatMsgViewHolder>(
                ChatMessage.class,
                R.layout.message,
                FirechatMsgViewHolder.class,
                mDatabaseReference.child(romName)
        ) {
            @Override
            protected void populateViewHolder(FirechatMsgViewHolder firechatMsgViewHolder, ChatMessage chatMessage, int i) {
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                firechatMsgViewHolder.mMsgImage.setVisibility(View.GONE);

                if (chatMessage != null) {
                    firechatMsgViewHolder.setIsShowImgChat(chatMessage, mStorageReference, context);

                    if (chatMessage.getUid().equals(mUid)) {
                        canPlaySound = false;
                        firechatMsgViewHolder.setIsSender(true);
                    } else {
                        canPlaySound = true;
                        firechatMsgViewHolder.setIsSender(false);
                    }
                    firechatMsgViewHolder.msgText.setText(chatMessage.getText());
                    firechatMsgViewHolder.userText.setText(chatMessage.getName());

                    mUsername = mFirebaseUser.getDisplayName();
                    if (mPhotoUrl.equals("")) {
                        mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
                    }
                    Glide.with(ChatFragment.this).
                            load(chatMessage.getPhotoUrl()).into(firechatMsgViewHolder.userImage);
                }

            }

            @Override
            public ChatMessage getItem(int position) {
                return super.getItem(position);
            }
        };

        mFBAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            int mCurrentItemsCount = 0;

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int chatMsgCount = mFBAdapter.getItemCount();
                int lastVisiblePosition = mLayoutManager.findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (chatMsgCount - 1) && lastVisiblePosition == (positionStart - 1))) {
                    mMsgRecyclerView.scrollToPosition(positionStart);
                }
                if (mCurrentItemsCount < chatMsgCount && canPlaySound) {
                    sp.play(soundIdShot, 1, 1, 0, 0, 1);
                }
                mCurrentItemsCount = chatMsgCount;
            }

            @Override
            public void onChanged() {
                super.onChanged();

                if (mCurrentItemsCount < mFBAdapter.getItemCount()) {
                    //проигрываем звук
                    sp.play(soundIdShot, 1, 1, 0, 0, 1);
                }
                mCurrentItemsCount = mFBAdapter.getItemCount();
            }
        });
        mProgressBar.setVisibility(View.VISIBLE);
        mMsgRecyclerView.setLayoutManager(mLayoutManager);
        mMsgRecyclerView.setAdapter(mFBAdapter);

        mUsername = mFirebaseUser.getDisplayName().toString();
        canPlaySound = true;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), filePath);

                mPreviewPhoto.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Bitmap img = null;

        if (requestCode == REQUEST) {

            Uri selectedImage = null;
            try {
                selectedImage = data.getData();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            if (selectedImage != null) {
                mPreviewPhoto.setVisibility(View.VISIBLE);
                Date currentDate = new Date(System.currentTimeMillis());
                imgBucked = mUid + currentDate.toString();
                try {
                    img = MediaStore.Images.Media.getBitmap(context.getContentResolver(), selectedImage);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (img != null) {
                    mPreviewPhoto.setImageBitmap(img);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    img.compress(Bitmap.CompressFormat.JPEG, 20, baos);

                    uploadFile(selectedImage);
                }

            } else {
                Toast.makeText(context, getString(R.string.cancel_btn_aler), Toast.LENGTH_SHORT).show();
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //this method will upload the file
    private void uploadFile(Uri selectedImage) {
        //if there is a file to upload
        if (selectedImage != null) {
            //displaying a progress dialog while upload is going on
            final ProgressDialog progressDialog = new ProgressDialog(context);
            progressDialog.setTitle("Загрузка фото");
            progressDialog.show();

            mStorageReference.child("image/" + imgBucked).putFile(selectedImage)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //if the upload is successfull
                            //hiding the progress dialog
                            progressDialog.dismiss();

                            //and displaying a success toast
                            Toast.makeText(context.getApplicationContext(), "Загружено", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            //if the upload is not successfull
                            //hiding the progress dialog
                            progressDialog.dismiss();

                            //and displaying error message
                            Toast.makeText(context.getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            //calculating progress percentage
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                            //displaying percentage in progress dialog
                            progressDialog.setMessage("Загрузка " + ((int) progress) + "%...");
                        }
                    });
        }
    }


    @Override
    public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {

    }

    public static class FirechatMsgViewHolder extends RecyclerView.ViewHolder {
        public TextView msgText;
        public TextView userText;
        public CircleImageView userImage;
        private ImageView mMsgImage;
        private final FrameLayout mLeftArrow;
        private final FrameLayout mRightArrow;
        private final RelativeLayout mMessageContainer;
        private final LinearLayout mMessage;
        private final int mGreen300;
        private final int mWhite300;

        public FirechatMsgViewHolder(View view) {
            super(view);
            userImage = (CircleImageView) view.findViewById(R.id.user_msg_icon);
            userText = (TextView) itemView.findViewById(R.id.name_text);
            msgText = (TextView) itemView.findViewById(R.id.message_text);
            mMsgImage = (ImageView) itemView.findViewById(R.id.msg_image_iv);
            mLeftArrow = (FrameLayout) itemView.findViewById(R.id.left_arrow);
            mRightArrow = (FrameLayout) itemView.findViewById(R.id.right_arrow);
            mMessageContainer = (RelativeLayout) itemView.findViewById(R.id.message_container);
            mMessage = (LinearLayout) itemView.findViewById(R.id.message);
            mGreen300 = ContextCompat.getColor(itemView.getContext(), R.color.material_green_300);
            mWhite300 = ContextCompat.getColor(itemView.getContext(), R.color.material_white_300);
        }

        public void setIsSender(boolean isSender) {
            final int color;
            if (isSender) {
                color = mGreen300;
                msgText.setTextColor(Color.WHITE);
                mLeftArrow.setVisibility(View.INVISIBLE);
                mRightArrow.setVisibility(View.VISIBLE);
                mMessageContainer.setGravity(Gravity.END);
            } else {
                color = mWhite300;
                msgText.setTextColor(Color.BLACK);
                mLeftArrow.setVisibility(View.VISIBLE);
                mRightArrow.setVisibility(View.INVISIBLE);
                mMessageContainer.setGravity(Gravity.START);
            }

            ((GradientDrawable) mMessage.getBackground()).setColor(color);
            ((RotateDrawable) mLeftArrow.getBackground()).getDrawable()
                    .setColorFilter(color, PorterDuff.Mode.SRC);
            ((RotateDrawable) mRightArrow.getBackground()).getDrawable()
                    .setColorFilter(color, PorterDuff.Mode.SRC);
        }

        public void setIsShowImgChat(
                ChatMessage chatMessage, final StorageReference reference, Context context) {

            final String url = chatMessage.getMsgPhotoUrl();

            if (url != null) {

                Glide.with(context /* context */)
                        .using(new FirebaseImageLoader())
                        .load(reference.child("image/" + chatMessage.getMsgPhotoUrl()))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(mMsgImage);
                mMsgImage.setVisibility(View.VISIBLE);

                mMsgImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showImgInFullScreen(url, reference);
                    }
                });
            }

        }

        // Show user img in full screen from alert dialog
        private void showImgInFullScreen(String url, StorageReference reference) {

            final Context context = SingletonCM.getInstance().getMainContext();

            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View view = layoutInflater.inflate(R.layout.alert_dialog_full_screen_img, null);

            final ImageView imageView = (ImageView) view.findViewById(R.id.ad_img_full);
            final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.ad_img_ful_progress);

            SimpleTarget target = new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap bitmap, GlideAnimation glideAnimation) {
                    // do something with the bitmap
                    // for demonstration purposes, let's just set it to an ImageView
                    progressBar.setVisibility(View.GONE);
                    imageView.setImageBitmap(bitmap);
                }
            };

            AlertDialog.Builder builder = new AlertDialog
                    .Builder(new ContextThemeWrapper(context, R.style.myDialog));

            if (url != null) {
                Glide.with(context) // could be an issue!
                        .using(new FirebaseImageLoader())
                        .load(reference.child("image/" + url))
                        .asBitmap()
                        .into(target);
            }
            builder.setView(view);

            AlertDialog alertDialog = builder.create();
            alertDialog.setTitle("Просмотр изображения");
            alertDialog.show();
        }
    }


    private void setBackground(View container) {
        RecyclerView view = (RecyclerView) container.findViewById(R.id.messageRecyclerView);
        int id = SingletonCM.getInstance().getBackgroundID();
        if (id != 0) {
            view.setBackgroundResource(id);
        }

    }

    public static void hasConnection(final Context context) {
        boolean connect = false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiInfo != null && wifiInfo.isConnected()) {
            connect = true;
        }
        wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifiInfo != null && wifiInfo.isConnected()) {
            connect = true;
        }
        wifiInfo = cm.getActiveNetworkInfo();
        if (wifiInfo != null && wifiInfo.isConnected()) {
            connect = true;
        }

        if (!connect) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View view = layoutInflater.inflate(R.layout.alert_dialog_no_internet, null);
            AlertDialog.Builder builder = new AlertDialog
                    .Builder(new ContextThemeWrapper(context, R.style.myDialog));
            builder.setView(view);
            builder.setCancelable(false)
                    .setPositiveButton("Выход", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            android.os.Process.killProcess(android.os.Process.myPid());
                        }
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.setTitle("Ошибка");
            alertDialog.show();
        }

    }

    @Override
    public Context getContext() {
        return context;
    }
}
