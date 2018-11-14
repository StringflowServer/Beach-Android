package abs.sf.beach.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import abs.ixi.client.core.Platform;
import abs.ixi.client.core.Session;
import abs.ixi.client.xmpp.JID;
import abs.ixi.client.xmpp.packet.UserProfileData;
import abs.sf.beach.android.R;
import abs.sf.beach.utils.CommonConstants;
import abs.sf.client.android.managers.AndroidUserManager;
import eu.janmuller.android.simplecropimage.CropImage;

public class ProfileActivity extends StringflowActivity {
    private ImageView ivBack, ivNext, editPic;
    private TextView tvHeaders, tvCreatGrp;
    private EditText etMessage;
    private TextView tvUserName, tvUserFirstName, tvUserMiddleName, tvUserLastName, tvUserNickName, tvUserGender, tvUserBday,
            tvUserPhoneNo, tvUserEmail, tvUserAddress, tvUserHome, tvUserStreet, tvUserLocality, tvUserPinCode, tvUserCity, tvUserState, tvUserCountry, tvUserAbout;
    private ScrollView scroller;
    private ImageView ivProfilePic;

    protected static final int CAMERA_REQUEST = 3;
    protected static final int GALLERY_PICTURE = 4;
    protected static final int CROP_PICTURE = 5;
    protected Uri mFileTempUri;
    protected ImageView mPhotoView;
    public ArrayList<String> galleryList = new ArrayList<String>();
    protected static File mFileTemp = null;
    private String isImagePresent = "0";
    private JID jid, userJID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        initView();
        initOnClicklitener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.jid = (JID) getIntent().getSerializableExtra(CommonConstants.JID);
        this.userJID = (JID) Platform.getInstance().getSession().get(Session.KEY_USER_JID);
        showData();

    }


    private void initView() {
        ivBack = (ImageView) findViewById(R.id.ivBack);
        tvCreatGrp = (TextView) findViewById(R.id.tvCreateGroup);
        ivNext = (ImageView) findViewById(R.id.ivNext);
        ivNext.setVisibility(View.GONE);
        tvHeaders = (TextView) findViewById(R.id.tvHeaders);
        tvHeaders.setText("User Profile");
        tvHeaders.setVisibility(View.VISIBLE);
        tvHeaders.setGravity(Gravity.CENTER);
        tvCreatGrp.setVisibility(View.GONE);
        ivProfilePic = (ImageView) findViewById(R.id.ivUser);
        tvUserName = (TextView) findViewById(R.id.tvUsername);
        tvUserFirstName = (TextView) findViewById(R.id.etUserFirstName);
        tvUserMiddleName = (TextView) findViewById(R.id.etUserMiddletName);
        tvUserLastName = (TextView) findViewById(R.id.etUserLastName);
        tvUserNickName = (TextView) findViewById(R.id.etUserNickName);
        tvUserGender = (TextView) findViewById(R.id.etUserGender);
        tvUserBday = (TextView) findViewById(R.id.etUserBday);
        tvUserPhoneNo = (TextView) findViewById(R.id.etUserPhone);
        tvUserEmail = (TextView) findViewById(R.id.etUserEmail);
        tvUserHome = (TextView) findViewById(R.id.etUserHome);
        tvUserStreet = (TextView) findViewById(R.id.etUserStreet);
        tvUserLocality = (TextView) findViewById(R.id.etUserLocality);
        tvUserPinCode = (TextView) findViewById(R.id.etUserPinCode);
        tvUserCity = (TextView) findViewById(R.id.etUserCity);
        tvUserState = (TextView) findViewById(R.id.etUserState);
        tvUserCountry = (TextView) findViewById(R.id.etUserCountry);
        tvUserAbout = (TextView) findViewById(R.id.etUserAbout);
        scroller = (ScrollView) findViewById(R.id.scroller);


        editPic = (ImageView) findViewById(R.id.iv_edit_pic);
        editPic.setVisibility(View.GONE);
        if (jid == userJID) {
            editPic.setVisibility(View.VISIBLE);
            editPic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        showPicImageDialog();
                    }
                }
            });
        }


        scroller.post(new Runnable() {
            public void run() {
                scroller.fullScroll(ScrollView.FOCUS_UP);
            }
        });

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.hide();
        }

    }

    private void initOnClicklitener() {
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProfileActivity.this.finish();
            }
        });


    }

    private UserProfileData showData() {
        AndroidUserManager userManager = (AndroidUserManager) Platform.getInstance().getUserManager();
        UserProfileData userProfileData = userManager.getCachedUserProfileData(jid);

        if (userProfileData == null) {
            userProfileData = userManager.getUserProfileData(jid);

        }
        tvUserName.setText(jid.getNode());
        tvUserFirstName.setText(userProfileData.getFirstName());
        tvUserMiddleName.setText(userProfileData.getMiddleName());
        tvUserNickName.setText(userProfileData.getNickName());
        tvUserLastName.setText(userProfileData.getLastName());
        tvUserGender.setText(userProfileData.getGender());
        tvUserBday.setText(userProfileData.getBday());
        tvUserPhoneNo.setText(userProfileData.getPhone());
        tvUserEmail.setText(userProfileData.getEmail());

        if (userProfileData.getAddress() != null) {
            tvUserHome.setText(userProfileData.getAddress().getHome());
            tvUserStreet.setText(userProfileData.getAddress().getStreet());
            tvUserLocality.setText(userProfileData.getAddress().getLocality());
            tvUserPinCode.setText(userProfileData.getAddress().getPcode());
            tvUserCity.setText(userProfileData.getAddress().getCity());
            tvUserState.setText(userProfileData.getAddress().getState());
            tvUserCountry.setText(userProfileData.getAddress().getCountry());
        }

        tvUserAbout.setText(userProfileData.getDescription());

        this.ivProfilePic.setImageBitmap(userManager.getUserAvatar(this.jid));


        return userProfileData;
    }

    private void showPicImageDialog() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            mFileTemp = new File(Environment.getExternalStorageDirectory(),
                    CommonConstants.TEMP_PHOTO_FILE_NAME);
        } else {
            mFileTemp = new File(context().getFilesDir(),
                    CommonConstants.TEMP_PHOTO_FILE_NAME);
        }
        Intent attachmentIntent = new Intent(context(),
                AttachmentOptionActivity.class);
        attachmentIntent.putExtra("ispresent", isImagePresent);
        startActivityForResult(attachmentIntent,
                AttachmentOptionActivity.REQUEST_ATTACHMENT_OPTION);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != Activity.RESULT_OK) {
            return;
        }


        switch (requestCode) {
            case 1:
                ivProfilePic.setImageBitmap((Bitmap) data.getExtras().get(String.valueOf(mFileTempUri)));  //use this if you trying to set image on Imageview

                break;
            case GALLERY_PICTURE:
                try {
                    InputStream inputStream = context().getContentResolver()
                            .openInputStream(data.getData());
                    FileOutputStream fileOutputStream = new FileOutputStream(
                            mFileTemp);
                    copyStream(inputStream, fileOutputStream);
                    fileOutputStream.close();
                    inputStream.close();
                    startCropImage();
                } catch (Exception e) {
                    Log.e(CommonConstants.SETTINGS_FRAGMENT_PROFILE_TAG,
                            CommonConstants.ERR_CREATING_TEMP_FILE, e);
                }
                break;
//            case CAMERA_REQUEST:
//                String[] projection = {MediaStore.Images.ImageColumns.SIZE,
//                        MediaStore.Images.ImageColumns.DISPLAY_NAME,
//                        MediaStore.Images.ImageColumns.DATA, BaseColumns._ID,};
//                //
//                // intialize the Uri and the Cursor, and the current expected size.
//                Cursor c = null;
//                Uri u = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//                //
//                if (mFileTemp != null) {
//                    // Query the Uri to get the data path. Only if the Uri is valid,
//                    // and we had a valid size to be searching for.
//                    if ((u != null) && (mFileTemp.length() > 0)) {
//                        c = getContentResolver().query(u, projection, null, null, ContactsContract.Contacts.DISPLAY_NAME);
//                    }
//                    //
//                    // If we found the cursor and found a record in it (we also have
//                    // the size).
//                    if ((c != null) && (c.moveToFirst())) {
//                        do {
//                            // Check each area in the gallary we built before.
//                            boolean bFound = false;
//                            for (String sGallery : galleryList) {
//                                if (sGallery.equalsIgnoreCase(c.getString(1))) {
//                                    bFound = true;
//                                    break;
//                                }
//                            }
//                            //
//                            // To here we looped the full gallery.
//                            if (!bFound) {
//                                // This is the NEW image. If the size is bigger,
//                                // copy it.
//                                // Then delete it!
//                                File f = new File(c.getString(2));
//
//                                // Ensure it's there, check size, and delete!
//                                if ((f.exists())
//                                        && (mFileTemp.length() < c.getLong(0))
//                                        && (mFileTemp.delete())) {
//                                    // Finally we can stop the copy.
//                                    try {
//                                        mFileTemp.createNewFile();
//                                        FileChannel source = null;
//                                        FileChannel destination = null;
//                                        try {
//                                            source = new FileInputStream(f)
//                                                    .getChannel();
//                                            destination = new FileOutputStream(
//                                                    mFileTemp).getChannel();
//                                            destination.transferFrom(source, 0,
//                                                    source.size());
//                                        } finally {
//                                            if (source != null) {
//                                                source.close();
//                                            }
//                                            if (destination != null) {
//                                                destination.close();
//                                            }
//                                        }
//                                    } catch (IOException e) {
//                                        // Could not copy the file over.
//                                        e.printStackTrace();
//                                    }
//                                }
//                                //
//                                ContentResolver cr = context().getContentResolver();
//                                cr.delete(
//                                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                                        BaseColumns._ID + CommonConstants.EQUAL + c.getString(3),
//                                        null);
//                                break;
//                            }
//                        } while (c.moveToNext());
//                    }
//                }
//                try {
//                    //startCropImage();
//                } catch (Exception e) {
//                    Log.e(CommonConstants.SETTINGS_FRAGMENT_PROFILE_TAG,
//                            CommonConstants.ERR_CREATING_TEMP_FILE, e);
//                }
//                break;
            case CROP_PICTURE:
                String path = data.getStringExtra(CropImage.IMAGE_PATH);
                if (path == null) {
                    return;
                }
                Bitmap bitmap = BitmapFactory.decodeFile(mFileTemp.getPath());
                // mPhotoView.setImageBitmap(XpCommonUtil.decodeWithSampling(mFileTemp, 200, 200));
                mFileTemp = null;

                break;
            case AttachmentOptionActivity.REQUEST_ATTACHMENT_OPTION:
                if (null != data) {
                    int action = data.getIntExtra(
                            AttachmentOptionActivity.INTENT_ACTION, 0);
                    if (action == AttachmentOptionActivity.TAKE_PHOTO) {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        mFileTempUri = Uri.fromFile(mFileTemp);
                        try {
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, mFileTempUri);
                            fillPhotoList();
                            startActivityForResult(intent, CAMERA_REQUEST);
                        } catch (ActivityNotFoundException e) {
                            Log.d(CommonConstants.CHAT_FRAGMENT_MAIN_TAG,
                                    CommonConstants.CANNOT_TAKE_PIC, e);
                        }
                    } else if (action == AttachmentOptionActivity.CHOOSE_EXISTING) {
                        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                        photoPickerIntent.setType(CommonConstants.IMAGE_);
                        //ContentActivity._tempFragment = this;
                        startActivityForResult(photoPickerIntent, GALLERY_PICTURE);
                    } else if (action == AttachmentOptionActivity.REMOVE_EXISTING) {
                        //CommonUtil.showAlert(context(),"remove image");
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
        //ContentActivity._tempFragment = null;
    }

    public static void copyStream(InputStream input, OutputStream output)
            throws IOException {
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
    }

    protected void startCropImage() {

        Log.d("", "file path is" + mFileTemp.getPath());

        Intent intent = new Intent(context(), CropImage.class);
        intent.putExtra(CropImage.IMAGE_PATH, mFileTemp.getPath());
        intent.putExtra(CropImage.SCALE, true);
        intent.putExtra(CropImage.ASPECT_X, 2);
        intent.putExtra(CropImage.ASPECT_Y, 2);
        startActivityForResult(intent, CROP_PICTURE);
    }

    private void fillPhotoList() {
        // initialize the list!
        galleryList.clear();
        String[] projection = {MediaStore.Images.ImageColumns.DISPLAY_NAME};
        // intialize the Uri and the Cursor, and the current expected size.
        Cursor c = null;
        Uri u = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        //
        // Query the Uri to get the data path. Only if the Uri is valid.
        if (u != null) {
            c = getContentResolver().query(u, projection, null, null, ContactsContract.Contacts.DISPLAY_NAME);
        }

        // If we found the cursor and found a record in it (we also have the
        // id).
        if ((c != null) && (c.moveToFirst())) {
            do {
                // Loop each and add to the list.
                galleryList.add(c.getString(0));
            } while (c.moveToNext());
        }
    }
}
