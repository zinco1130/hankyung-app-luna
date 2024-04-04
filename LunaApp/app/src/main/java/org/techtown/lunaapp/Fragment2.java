package org.techtown.lunaapp;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import org.techtown.lunaapp.AppConstants;
import org.techtown.lunaapp.NoteDatabase;
import org.techtown.lunaapp.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import lib.kingja.switchbutton.BuildConfig;

public class Fragment2 extends Fragment {
    private static final String TAG = "Fragment2";

    int mMode = AppConstants.MODE_INSERT;

    Note item;

    Context context;
    OnTabItemSelectedListener listener;

    TextView dateTextView;

    EditText contentsInput;
    ImageView pictureImageView;

    boolean isPhotoCaptured;
    boolean isPhotoFileSaved;
    boolean isPhotoCanceled;

    int selectedPhotoMenu;

    Uri uri;
    File file;
    Bitmap resultPhotoBitmap;

    SimpleDateFormat todayDateFormat;
    String currentDateString;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        this.context = context;

        if (context instanceof OnTabItemSelectedListener) {
            listener = (OnTabItemSelectedListener) context;
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (context != null) {
            context = null;
            listener = null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment2, container, false);

        initUI(rootView);

        applyItem();

        return rootView;
    }

    private void initUI(ViewGroup rootView) {

        dateTextView = rootView.findViewById(R.id.dateTextView);

        contentsInput = rootView.findViewById(R.id.contentsInput);
        pictureImageView = rootView.findViewById(R.id.pictureImageView);
        pictureImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPhotoCaptured || isPhotoFileSaved) {
                    showDialog(AppConstants.CONTENT_PHOTO_EX);
                } else {
                    showDialog(AppConstants.CONTENT_PHOTO);
                }
            }
        });

        Button saveButton = rootView.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mMode == AppConstants.MODE_INSERT) {
                    saveNote();
                } else if(mMode == AppConstants.MODE_MODIFY) {
                    modifyNote();
                }

                if (listener != null) {
                    listener.onTabSelected(0);
                }
            }
        });

        Button deleteButton = rootView.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteNote();

                if (listener != null) {
                    listener.onTabSelected(0);
                }
            }
        });

        Button closeButton = rootView.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onTabSelected(0);
                }
            }
        });
    }


    public void setDateString(String dateString) {
        dateTextView.setText(dateString);
    }

    public void setContents(String data) {
        contentsInput.setText(data);
    }

    public void setPicture(String picturePath, int sampleSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;
        resultPhotoBitmap = BitmapFactory.decodeFile(picturePath, options);

        pictureImageView.setImageBitmap(resultPhotoBitmap);
    }


    public void setItem(Note item) {
        this.item = item;
    }

    public void applyItem() {
        AppConstants.println("applyItem called.");

        if (item != null) {
            mMode = AppConstants.MODE_MODIFY;

            setDateString(item.getCreateDateStr());
            setContents(item.getContents());

            String picturePath = item.getPicture();
            AppConstants.println("picturePath : " + picturePath);

            if (picturePath == null || picturePath.equals("")) {
                pictureImageView.setImageResource(R.drawable.noimagefound);
            } else {
                setPicture(item.getPicture(), 1);
            }

        } else {
            mMode = AppConstants.MODE_INSERT;

            Date currentDate = new Date();
            if (todayDateFormat == null) {
                todayDateFormat = new SimpleDateFormat(getResources().getString(R.string.today_date_format));
            }
            currentDateString = todayDateFormat.format(currentDate);
            AppConstants.println("currentDateString : " + currentDateString);
            setDateString(currentDateString);

            contentsInput.setText("");
            pictureImageView.setImageResource(R.drawable.noimagefound);
        }
    }


    public void showDialog(int id) {
        AlertDialog.Builder builder = null;

        switch(id) {

            case AppConstants.CONTENT_PHOTO:
                builder = new AlertDialog.Builder(context);

                builder.setTitle("사진 메뉴 선택");
                builder.setSingleChoiceItems(R.array.array_photo, 0, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        selectedPhotoMenu = whichButton;
                    }
                });
                builder.setPositiveButton("선택", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if(selectedPhotoMenu == 0 ) {
                            showPhotoCaptureActivity();
                        } else if(selectedPhotoMenu == 1) {
                            showPhotoSelectionActivity();
                        }
                    }
                });
                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                });

                break;

            case AppConstants.CONTENT_PHOTO_EX:
                builder = new AlertDialog.Builder(context);

                builder.setTitle("사진 메뉴 선택");
                builder.setSingleChoiceItems(R.array.array_photo_ex, 0, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        selectedPhotoMenu = whichButton;
                    }
                });
                builder.setPositiveButton("선택", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if(selectedPhotoMenu == 0) {
                            showPhotoCaptureActivity();
                        } else if(selectedPhotoMenu == 1) {
                            showPhotoSelectionActivity();
                        } else if(selectedPhotoMenu == 2) {
                            isPhotoCanceled = true;
                            isPhotoCaptured = false;

                            pictureImageView.setImageResource(R.drawable.picture1);
                        }
                    }
                });
                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                });

                break;

            default:
                break;
        }

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void showPhotoCaptureActivity() {
        try {
            file = createFile();
            if (file.exists()) {
                file.delete();
            }

            file.createNewFile();
        } catch(Exception e) {
            e.printStackTrace();
        }

        if(Build.VERSION.SDK_INT >= 24) {
            uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID, file);
        } else {
            uri = Uri.fromFile(file);
        }

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

        startActivityForResult(intent, AppConstants.REQ_PHOTO_CAPTURE);

    }

    private File createFile() {
        String filename = createFilename();
        File outFile = new File(context.getFilesDir(), filename);
        Log.d("Main", "File path : " + outFile.getAbsolutePath());

        return outFile;
    }

    public void showPhotoSelectionActivity() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(intent, AppConstants.REQ_PHOTO_SELECTION);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (intent != null) {
            switch (requestCode) {
                case AppConstants.REQ_PHOTO_CAPTURE:
                    Log.d(TAG, "onActivityResult() for REQ_PHOTO_CAPTURE.");

                    Log.d(TAG, "resultCode : " + resultCode);

                    resultPhotoBitmap = decodeSampledBitmapFromResource(file, pictureImageView.getWidth(), pictureImageView.getHeight());
                    pictureImageView.setImageBitmap(resultPhotoBitmap);

                    break;

                case AppConstants.REQ_PHOTO_SELECTION:
                    Log.d(TAG, "onActivityResult() for REQ_PHOTO_SELECTION.");

                    Uri fileUri = intent.getData();

                    ContentResolver resolver = context.getContentResolver();

                    try {
                        InputStream instream = resolver.openInputStream(fileUri);
                        resultPhotoBitmap = BitmapFactory.decodeStream(instream);
                        pictureImageView.setImageBitmap(resultPhotoBitmap);

                        instream.close();

                        isPhotoCaptured = true;
                    } catch(Exception e) {
                        e.printStackTrace();
                    }

                    break;

            }
        }
    }

    public static Bitmap decodeSampledBitmapFromResource(File res, int reqWidth, int reqHeight) {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(res.getAbsolutePath(),options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(res.getAbsolutePath(),options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height;
            final int halfWidth = width;

            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }


    private String createFilename() {
        Date curDate = new Date();
        String curDateStr = String.valueOf(curDate.getTime());

        return curDateStr;
    }


    private void saveNote() {
        String contents = contentsInput.getText().toString();

        String picturePath = savePicture();

        String sql = "insert into " + NoteDatabase.TABLE_NOTE +
                "(CONTENTS, PICTURE) values(" +
                "'"+ contents + "', " +
                "'"+ picturePath + "')";

        Log.d(TAG, "sql : " + sql);
        NoteDatabase database = NoteDatabase.getInstance(context);
        database.execSQL(sql);

    }

    private void modifyNote() {
        if (item != null) {

            String contents = contentsInput.getText().toString();

            String picturePath = savePicture();

            String sql = "update " + NoteDatabase.TABLE_NOTE +
                    " set " +
                    "   ,CONTENTS = '" + contents + "'" +
                    "   ,PICTURE = '" + picturePath + "'" +
                    " where " +
                    "   _id = " + item._id;

            Log.d(TAG, "sql : " + sql);
            NoteDatabase database = NoteDatabase.getInstance(context);
            database.execSQL(sql);
        }
    }


    private void deleteNote() {
        AppConstants.println("deleteNote called.");

        if (item != null) {

            String sql = "delete from " + NoteDatabase.TABLE_NOTE +
                    " where " +
                    "   _id = " + item._id;

            Log.d(TAG, "sql : " + sql);
            NoteDatabase database = NoteDatabase.getInstance(context);
            database.execSQL(sql);
        }
    }

    private String savePicture() {
        if (resultPhotoBitmap == null) {
            AppConstants.println("No picture to be saved.");
            return "";
        }

        File photoFolder = new File(AppConstants.FOLDER_PHOTO);

        if(!photoFolder.isDirectory()) {
            Log.d(TAG, "creating photo folder : " + photoFolder);
            photoFolder.mkdirs();
        }

        String photoFilename = createFilename();
        String picturePath = photoFolder + File.separator + photoFilename;

        try {
            FileOutputStream outstream = new FileOutputStream(picturePath);
            resultPhotoBitmap.compress(Bitmap.CompressFormat.PNG, 100, outstream);
            outstream.close();
        } catch(Exception e) {
            e.printStackTrace();
        }

        return picturePath;
    }
}

