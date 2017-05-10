
package com.bruno.facerecognitionapp;

/**
 * Created by brunoc2 and dsgonza2
 */


import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.opencv.imgproc.Imgproc.createCLAHE;

public class FaceRecognitionAppActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = FaceRecognitionAppActivity.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST_CODE = 0;
    ArrayList<Mat> images;
    ArrayList<String> imagesLabels;
    ArrayList<String> addLabels;
    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat mRgba, mGray;
    private Toast mToast;
    private boolean useEigenfaces = false;
    private SharedPreferences prefs;
    private TinyDB tinydb;
    private Toolbar mToolbar;
    private File                   mCascadeFile;
    private File                   mCascadeFile2;
    private File                   mCascadeFile3;
    private File                   mCascadeFile4;
    private CascadeClassifier      faceDet;
    private CascadeClassifier      faceDet2;
    private CascadeClassifier      faceDet3;
    private CascadeClassifier      faceDet4;
    private int                    mAbsoluteFaceSize   = 0;
    private float                  mRelativeFaceSize   = 0.2f;
    private boolean                isFaceDetected = false;
    private MediaPlayer mpObject1 = new MediaPlayer();
    private MediaPlayer mpObject2 = new MediaPlayer();
    private MediaPlayer mpObject3 = new MediaPlayer();
    private MediaPlayer mpObject4 = new MediaPlayer();
    private MediaPlayer mpObject5 = new MediaPlayer();
    private int stop1 = 0;
    private int stop2 = 0;
    private int stop3 = 0;
    private int stop4 = 0;
    private int stop5 = 0;

    public Cursor getPlaylistTracks(Context context, Long playlist_id) {
        Uri newuri = MediaStore.Audio.Playlists.Members.getContentUri(
                "external", playlist_id);
        ContentResolver resolver = context.getContentResolver();
        String _id = MediaStore.Audio.Playlists.Members._ID;
        String audio_id = MediaStore.Audio.Playlists.Members.AUDIO_ID;
        String artist = MediaStore.Audio.Playlists.Members.ARTIST;
        String album = MediaStore.Audio.Playlists.Members.ALBUM;
        String album_id = MediaStore.Audio.Playlists.Members.ALBUM_ID;
        String title = MediaStore.Audio.Playlists.Members.TITLE;
        String duration = MediaStore.Audio.Playlists.Members.DURATION;
        String location = MediaStore.Audio.Playlists.Members.DATA;
        String composer = MediaStore.Audio.Playlists.Members.COMPOSER;
        String playorder = MediaStore.Audio.Playlists.Members.PLAY_ORDER;
        String date_modified = MediaStore.Audio.Playlists.Members.DATE_MODIFIED;
        String[] columns = { _id, audio_id, artist, album_id,album, title, duration,
                location, date_modified, playorder, composer };
        Cursor cursor = resolver.query(newuri, columns, null, null, null);
        return cursor;
    }

    private int idForplaylist(String name) {
        Cursor c = MusicUtils.query(this, MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                new String[] {MediaStore.Audio.Playlists._ID},
                MediaStore.Audio.Playlists.NAME + "=?", new String[] {name},
                MediaStore.Audio.Playlists.NAME);
        int id = -1;
        if (c != null) {
            c.moveToFirst();
            if (!c.isAfterLast()) {
                id = c.getInt(0);
            }
            c.close();
        }
        return id;
    }

    // --- GET SONGS PER PLAYLIST -- //
    private void play (int playlistID){

        String[] proj2 = { "SourceId", MediaStore.Audio.Playlists.Members.TITLE, MediaStore.Audio.Playlists.Members.PLAYLIST_ID };
        String playListRef = "content://com.google.android.music.MusicContent/playlists/"+ playlistID +"/members";
        Uri songUri = Uri.parse(playListRef);
        Cursor songCursor = getContentResolver().query(songUri, proj2, null, null, null);

        long audioId = -1;
        String title = "";
        while (songCursor.moveToNext()) {
            audioId = songCursor.getLong(songCursor.getColumnIndex("SourceId"));
            title = songCursor.getString(songCursor.getColumnIndex(MediaStore.Audio.Playlists.Members.TITLE));
            Log.v("", "audioId: " + audioId + ", title: " + title);
        }
        songCursor.close();

        try {
            if(audioId > 0) {
                if (playlistID == 1) {
                    if(stop1 == 1){
                        //mpObject1.prepare();
                        mpObject1.start();
                        stop1 = 0;
                    }else{
                        Uri contentUri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, Long.valueOf(audioId));
                        mpObject1.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mpObject1.setDataSource(getApplicationContext(), contentUri);
                        mpObject1.prepare();
                        mpObject1.start();
                    }
                    Log.i("", "please start1!!");
                } else if (playlistID == 2) {
                    Log.i("", "please start2!!");
                    if(stop2 == 1){
                        mpObject2.start();
                        stop2 = 0;
                    }else{
                        Uri contentUri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, Long.valueOf(audioId));
                        mpObject2.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mpObject2.setDataSource(getApplicationContext(), contentUri);
                        mpObject2.prepare();
                        mpObject2.start();}
                } else if (playlistID == 3) {
                    Log.i("", "please start3!!");
                    if(stop3 == 1){
                        mpObject3.start();
                        stop3 = 0;
                    }else{
                        Uri contentUri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, Long.valueOf(audioId));
                        mpObject3.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mpObject3.setDataSource(getApplicationContext(), contentUri);
                        mpObject3.prepare();
                        mpObject3.start();
                    }
                } else if (playlistID == 4) {
                    Log.i("", "please start4!!");
                    if(stop4 == 1){
                        mpObject4.start();
                        stop4 = 0;
                    }else {
                        Uri contentUri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, Long.valueOf(audioId));
                        mpObject4.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mpObject4.setDataSource(getApplicationContext(), contentUri);
                        mpObject4.prepare();
                        mpObject4.start();
                    }
                } else if (playlistID == 5) {
                    Log.i("", "please start5!!");
                    if(stop5 == 1){
                        //mpObject5.prepare();
                        mpObject5.start();
                        stop5 = 0;
                    }else {
                        Uri contentUri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, Long.valueOf(audioId));
                        mpObject5.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mpObject5.setDataSource(getApplicationContext(), contentUri);
                        mpObject5.prepare();
                        mpObject5.start();
                    }
                }
            }
        } catch (Exception e) {
            Log.i("", "please work");
            e.printStackTrace();
        }
    }

    private void updateTrainingData(){
        TextView status = (TextView) findViewById(R.id.emoImages);
        TextView label1 = (TextView) findViewById(R.id.emoView1);
        TextView label2 = (TextView) findViewById(R.id.emoView2);
        TextView label3 = (TextView) findViewById(R.id.emoView3);
        TextView label4 = (TextView) findViewById(R.id.emoView4);
        TextView label5 = (TextView) findViewById(R.id.emoView5);
        if(imagesLabels != null) {
            Log.i(TAG, "Set size: " + imagesLabels.size());
            if (imagesLabels.size() > 0) {
                Log.i(TAG, "Updating training data");
                Set<String> uniqueLabelsSet = new HashSet<>(imagesLabels); // Get all unique labels
                String[] uniqueLabels = uniqueLabelsSet.toArray(new String[uniqueLabelsSet.size()]);
                Log.i(TAG, "Set size: " + imagesLabels.size() + " classes: " + uniqueLabels.length);
                int[] class_amount = new int[uniqueLabels.length];
                for (int i = 0; i < imagesLabels.size(); i++) {
                    String label = imagesLabels.get(i);
                    for (int j = 0; j < uniqueLabels.length; j++) {
                        if (label.equals(uniqueLabels[j])) {
                            class_amount[j] += 1; // Insert corresponding number
                        }
                    }
                }
                status.setText("Training Set");
                label1.setText(uniqueLabels[0] + ": " + class_amount[0]);
                if (uniqueLabels.length >= 2)
                    label2.setText(uniqueLabels[1] + ": " + class_amount[1]);
                if (uniqueLabels.length >= 3)
                    label3.setText(uniqueLabels[2] + ": " + class_amount[2]);
                if (uniqueLabels.length >= 4)
                    label4.setText(uniqueLabels[3] + ": " + class_amount[3]);
                if (uniqueLabels.length >= 5)
                    label5.setText(uniqueLabels[4] + ": " + class_amount[4]);


            } else {
                status.setText("No images in training set");
                label1.setText(" ");
                label2.setText(" ");
                label3.setText(" ");
                label4.setText(" ");
                label5.setText(" ");
            }
        }
        else
            Log.i(TAG, "Failed to update training data");
    }

    private void showToast(String message, int duration) {
        if (duration != Toast.LENGTH_SHORT && duration != Toast.LENGTH_LONG)
            throw new IllegalArgumentException();
        if (mToast != null && mToast.getView().isShown())
            mToast.cancel(); // Close the toast if it is already open
        mToast = Toast.makeText(this, message, duration);
        mToast.show();
    }

    private void addLabel(String string) {
        String label = string.substring(0, 1).toUpperCase(Locale.US) + string.substring(1).trim().toLowerCase(Locale.US); // Make sure that the name is always uppercase and rest is lowercase
        imagesLabels.add(label); // Add label to list of labels
        Log.i(TAG, "Label: " + label);
        updateTrainingData();

        trainFaces(); // When we have finished setting the label, then retrain faces
    }

    private void trainFaces() {
        if (images.isEmpty())
            return; // The array might be empty if the method is changed in the OnClickListener

        Mat imagesMatrix = new Mat((int) images.get(0).total(), images.size(), images.get(0).type());
        for (int i = 0; i < images.size(); i++)
            images.get(i).copyTo(imagesMatrix.col(i)); // Create matrix where each image is represented as a column vector

        Log.i(TAG, "Images height: " + imagesMatrix.height() + " Width: " + imagesMatrix.width() + " total: " + imagesMatrix.total());

        // Train the face recognition algorithms in an asynchronous task, so we do not skip any frames
        if (useEigenfaces)
            new NativeMethods.TrainFacesTask(imagesMatrix).execute();
        else {
            Set<String> uniqueLabelsSet = new HashSet<>(imagesLabels); // Get all unique labels
            String[] uniqueLabels = uniqueLabelsSet.toArray(new String[uniqueLabelsSet.size()]); // Convert to String array, so we can read the values from the indices

            int[] classesNumbers = new int[uniqueLabels.length];
            for (int i = 0; i < classesNumbers.length; i++)
                classesNumbers[i] = i + 1; // Create incrementing list for each unique label starting at 1

            int[] classes = new int[imagesLabels.size()];
            for (int i = 0; i < imagesLabels.size(); i++) {
                String label = imagesLabels.get(i);
                for (int j = 0; j < uniqueLabels.length; j++) {
                    if (label.equals(uniqueLabels[j])) {
                        classes[i] = classesNumbers[j]; // Insert corresponding number
                        break;
                    }
                }
            }

            Mat vectorClasses = new Mat(classes.length, 1, CvType.CV_32S); // CV_32S == int
            vectorClasses.put(0, 0, classes); // Copy int array into a vector

            new NativeMethods.TrainFacesTask(imagesMatrix, vectorClasses).execute();
        }
    }

    private void showLabelsDialog() {
        Set<String> uniqueLabelsSet = new HashSet<>(imagesLabels); // Get all unique labels
        if (!uniqueLabelsSet.isEmpty()) { // Make sure that there are any labels
            // Inspired by: http://stackoverflow.com/questions/15762905/how-can-i-display-a-list-view-in-an-android-alert-dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(FaceRecognitionAppActivity.this);
            builder.setTitle("What was your emotion?");
            builder.setPositiveButton("New Emotion", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    showAddLabelDialog();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    images.remove(images.size() - 1); // Remove last image
                }
            });
            builder.setCancelable(false); // Prevent the user from closing the dialog

            String[] uniqueLabels = uniqueLabelsSet.toArray(new String[uniqueLabelsSet.size()]); // Convert to String array for ArrayAdapter
            Arrays.sort(uniqueLabels); // Sort labels alphabetically
            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(FaceRecognitionAppActivity.this, android.R.layout.simple_list_item_1, uniqueLabels) {
                @Override
                public @NonNull View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    TextView textView = (TextView) super.getView(position, convertView, parent);
                    if (getResources().getBoolean(R.bool.isTablet))
                        textView.setTextSize(20); // Make text slightly bigger on tablets compared to phones
                    else
                        textView.setTextSize(18); // Increase text size a little bit
                    return textView;
                }
            };
            ListView mListView = new ListView(FaceRecognitionAppActivity.this);
            mListView.setAdapter(arrayAdapter); // Set adapter, so the items actually show up
            builder.setView(mListView); // Set the ListView

            final AlertDialog dialog = builder.show(); // Show dialog and store in final variable, so it can be dismissed by the ListView

            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    dialog.dismiss();
                    addLabel(arrayAdapter.getItem(position));
                }
            });
        } else
            showAddLabelDialog(); // If there is no existing labels, then ask the user for a new label
    }
    private void showAddLabelDialog() {

        Set<String> choiceLabelsSet = new HashSet<>(addLabels); // Get all unique labels
        // Inspired by: http://stackoverflow.com/questions/15762905/how-can-i-display-a-list-view-in-an-android-alert-dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(FaceRecognitionAppActivity.this);
        builder.setTitle("Select a new emotion to be trained");
        builder.setPositiveButton("Enter your own", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                showEnterLabelDialog();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                images.remove(images.size() - 1); // Remove last image
            }
        });
        builder.setCancelable(false); // Prevent the user from closing the dialog

        String[] choiceLabels = choiceLabelsSet.toArray(new String[choiceLabelsSet.size()]); // Convert to String array for ArrayAdapter
        Arrays.sort(choiceLabels); // Sort labels alphabetically
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(FaceRecognitionAppActivity.this, android.R.layout.simple_list_item_1, choiceLabels) {
            @Override
            public @NonNull View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                if (getResources().getBoolean(R.bool.isTablet))
                    textView.setTextSize(20); // Make text slightly bigger on tablets compared to phones
                else
                    textView.setTextSize(18); // Increase text size a little bit
                return textView;
            }
        };
        ListView mListView = new ListView(FaceRecognitionAppActivity.this);
        mListView.setAdapter(arrayAdapter); // Set adapter, so the items actually show up
        builder.setView(mListView); // Set the ListView

        final AlertDialog dialog = builder.show(); // Show dialog and store in final variable, so it can be dismissed by the ListView

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dialog.dismiss();
                addLabel(arrayAdapter.getItem(position));
            }
        });

    }

    private void showEnterLabelDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(FaceRecognitionAppActivity.this);
        builder.setTitle("Please enter an emotion:");

        final EditText input = new EditText(FaceRecognitionAppActivity.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Submit", null); // Set up positive button, but do not provide a listener, so we can check the string before dismissing the dialog
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                images.remove(images.size() - 1); // Remove last image
            }
        });
        builder.setCancelable(false); // User has to input a name
        AlertDialog dialog = builder.create();

        // Source: http://stackoverflow.com/a/7636468/2175837
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button mButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                mButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String string = input.getText().toString().trim();
                        if (!string.isEmpty()) { // Make sure the input is valid
                            // If input is valid, dismiss the dialog and add the label to the array
                            dialog.dismiss();
                            addLabel(string);
                            updateTrainingData();

                        }
                    }
                });
            }
        });

        // Show keyboard, so the user can start typing straight away
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        dialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        updateTrainingData();

        addLabels = new ArrayList<>(5);
        addLabels.add("Happy");
        addLabels.add("Sad");
        addLabels.add("Angry");
        addLabels.add("Surprised");
        addLabels.add("Neutral");

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_face_recognition_app);


        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar); // Sets the Toolbar to act as the ActionBar for this Activity window

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        // Set radio button based on value stored in shared preferences
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        useEigenfaces = prefs.getBoolean("useEigenfaces", false);


        tinydb = new TinyDB(this); // Used to store ArrayLists in the shared preferences


        findViewById(R.id.clear_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Cleared training set");
                images.clear(); // Clear both arrays, when new instance is created
                imagesLabels.clear();
                showToast("Training set cleared", Toast.LENGTH_SHORT);
                updateTrainingData();
            }
        });

        findViewById(R.id.take_picture_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Gray height: " + mGray.height() + " Width: " + mGray.width() + " total: " + mGray.total());
                if (mGray.total() == 0)
                    return;
                Size imageSize = new Size(200, 200.0f / ((float) mGray.width() / (float) mGray.height())); // Scale image in order to decrease computation time

                Core.flip(mGray.t(), mGray, 0); //Rotate input image 90 degrees CCW

                ///////////
                if (mAbsoluteFaceSize == 0) {
                    int height = mGray.rows();
                    if (Math.round(height * mRelativeFaceSize) > 0) {
                        mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
                    }
                }
                Log.i(TAG, "Face Size: " + mAbsoluteFaceSize);

                MatOfRect face = new MatOfRect();
                MatOfRect face2 = new MatOfRect();
                MatOfRect face3 = new MatOfRect();
                MatOfRect face4 = new MatOfRect();

                if (faceDet != null && faceDet2 != null && faceDet3 != null && faceDet4 != null) {
                    Log.i(TAG, "Classifiers Exist");
                    faceDet.detectMultiScale(mGray, face, 1.1, 10, 2,
                            new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());

                    faceDet2.detectMultiScale(mGray, face2, 1.1, 10, 2,
                            new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());

                    faceDet3.detectMultiScale(mGray, face3, 1.1, 10, 2,
                            new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());

                    faceDet4.detectMultiScale(mGray, face4, 1.1, 10, 2,
                            new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
                }

                MatOfRect facefeatures = new MatOfRect();
                if(!face.empty()){
                    facefeatures = face;
                    Log.i(TAG, "Using face 1");
                }

                else if(!face2.empty()) {
                    facefeatures = face2;
                    Log.i(TAG, "Using face 2");
                }
                else if(!face3.empty()) {
                    facefeatures = face3;
                    Log.i(TAG, "Using face 3");
                }
                else if(!face4.empty()){
                    facefeatures = face4;
                    Log.i(TAG, "Using face 4");
                }
                else
                    Log.i(TAG, "Face containers are empty");


                Rect[] faceArray = facefeatures.toArray();
                CLAHE equalizer = createCLAHE(2, new Size(8,8));
                Mat Face = new Mat();
                isFaceDetected = false;
                try {
                    Rect facerect = faceArray[0];
                    Face = new Mat(mGray, facerect);
                    isFaceDetected = true;
                    Imgproc.resize(Face, Face, imageSize); //reshape //face
                    equalizer.apply(Face, Face); //equalize face
                    Mat tmp = new Mat(350, 350, CvType.CV_8U, new Scalar(4));

                    Bitmap bmp = null;

                    //STORE IMAGE
                    try{
                        Imgproc.cvtColor(Face, tmp, Imgproc.COLOR_GRAY2RGB,4);
                        bmp = Bitmap.createBitmap(tmp.cols(), tmp.rows(), Bitmap.Config.ARGB_8888);
                        Utils.matToBitmap(tmp,bmp);
                    }
                    catch (CvException e){Log.d("Exception",e.getMessage());}
                    MediaStore.Images.Media.insertImage(getContentResolver(), bmp, "croppedface","turtle");
                    String root = Environment.getExternalStorageDirectory().toString();
                    File myDir = new File(root);
                    myDir.mkdirs();
                    String fname = "Image-" + "croppedface" + ".jpg";
                    File file = new File(myDir, fname);
                    if (file.exists()) file.delete();
                    Log.i("LOAD", root + fname);
                    try {
                        FileOutputStream out = new FileOutputStream(file);
                        bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
                        out.flush();
                        out.close();
                    } catch (Exception e) {
                        e.printStackTrace();

                    }
                }catch(Exception e) {
                    Log.i(TAG, "No Face Detected");
                    isFaceDetected = false;
                }

                ///////////

                Log.i(TAG, "Small gray height: " + Face.height() + " Width: " + Face.width() + " total: " + Face.total());


                Mat image = Face.reshape(0, (int) Face.total()); // Create column vector
                Log.i(TAG, "Vector height: " + image.height() + " Width: " + image.width() + " total: " + image.total());
                if(!isFaceDetected){
                    showToast("No face detected. Please try again.", Toast.LENGTH_LONG);
                    return;
                }

                images.add(image); // Add current image to the array

                // Calculate normalized Euclidean distance
                showToast("Calculating", Toast.LENGTH_LONG);
                new NativeMethods.MeasureDistTask(useEigenfaces, new NativeMethods.MeasureDistTask.Callback() {
                    @Override
                    public void onMeasureDistComplete(Bundle bundle) {
                        float minDist = bundle.getFloat(NativeMethods.MeasureDistTask.MIN_DIST_FLOAT);
                        if (minDist != -1) {

                            int minIndex = bundle.getInt(NativeMethods.MeasureDistTask.MIN_DIST_INDEX_INT);
                            float faceDist = bundle.getFloat(NativeMethods.MeasureDistTask.DIST_FACE_FLOAT);
                            if (imagesLabels.size() > minIndex) { // Just to be sure
                                Log.i(TAG, /*"dist[" + minIndex + "]: " + minDist + */", face dist: " + faceDist + ", label: " + imagesLabels.get(minIndex));

                                showToast("Emotion detected: " + imagesLabels.get(minIndex), Toast.LENGTH_LONG);
                                if(imagesLabels.get(minIndex).contains("Happy")) {
                                    if(mpObject1.isPlaying())
                                    {
                                        //stop2 = 1;
                                        mpObject1.pause();
                                    }else if(mpObject2.isPlaying()){
                                        stop2 = 1;
                                        mpObject2.pause();
                                    }else if(mpObject3.isPlaying()){
                                        //stop2 = 1;
                                        mpObject3.pause();
                                    }else if(mpObject4.isPlaying()){
                                        //stop2 = 1;
                                        mpObject4.pause();
                                    }else if(mpObject5.isPlaying()){
                                        //stop2 = 1;
                                        mpObject5.pause();
                                    }
                                    Log.i("", "in Happy");
                                    play(2);
                                }
                                else if(imagesLabels.get(minIndex).contains("Sad")) {
                                    if(mpObject1.isPlaying())
                                    {
                                        //stop3 = 1;
                                        mpObject1.pause();
                                    }else if(mpObject2.isPlaying()){
                                        //stop3 = 1;
                                        mpObject2.pause();
                                    }else if(mpObject3.isPlaying()){
                                        stop3 = 1;
                                        mpObject3.pause();
                                    }else if(mpObject4.isPlaying()){
                                        //stop3 = 1;
                                        mpObject4.pause();
                                    }else if(mpObject5.isPlaying()){
                                        //stop3 = 1;
                                        mpObject5.pause();
                                    }
                                    Log.i("", "in Sad");
                                    play(3);
                                }
                                else if(imagesLabels.get(minIndex).contains("Angry")){
                                    if(mpObject1.isPlaying())
                                    {
                                        //stop4 = 1;
                                        mpObject1.pause();
                                    }else if(mpObject2.isPlaying()){
                                        //stop4 = 1;
                                        mpObject2.pause();
                                    }else if(mpObject3.isPlaying()){
                                        //stop4 = 1;
                                        mpObject3.pause();
                                    }else if(mpObject4.isPlaying()){
                                        stop4 = 1;
                                        mpObject4.pause();
                                    }else if(mpObject5.isPlaying()){
                                        //stop4 = 1;
                                        mpObject5.pause();
                                    }
                                    Log.i("", "in Angry");
                                    play(4);

                                }
                                else if(imagesLabels.get(minIndex).contains("Surprised")){
                                    if(mpObject1.isPlaying())
                                    {
                                        //stop5 = 1;
                                        mpObject1.pause();
                                    }else if(mpObject2.isPlaying()){
                                        //stop5 = 1;
                                        mpObject2.pause();
                                    }else if(mpObject3.isPlaying()){
                                        //stop5 = 1;
                                        mpObject3.pause();
                                    }else if(mpObject4.isPlaying()){
                                        //stop5 = 1;
                                        mpObject4.pause();
                                    }else if(mpObject5.isPlaying()){
                                        stop5 = 1;
                                        mpObject5.pause();
                                    }
                                    Log.i("", "in surprised");
                                    play(5);

                                }
                                else if(imagesLabels.get(minIndex).contains("Neutral")){
                                    if(mpObject1.isPlaying())
                                    {
                                        stop1 = 1;
                                        mpObject1.pause();
                                    }else if(mpObject2.isPlaying()){
                                        //stop1 = 1;
                                        mpObject2.pause();
                                    }else if(mpObject3.isPlaying()){
                                        //stop1 = 1;
                                        mpObject3.pause();
                                    }else if(mpObject4.isPlaying()){
                                        //stop1 = 1;
                                        mpObject4.pause();
                                    }else if(mpObject5.isPlaying()){
                                        //stop1 = 1;
                                        mpObject5.pause();
                                    }
                                    Log.i("", "in Neutral");
                                    play(1);
                                }

                            }
                            showLabelsDialog();
                            updateTrainingData();

                        } else {
                            Log.w(TAG, "Array is null");
                            showToast("Continue training to activate face detection", Toast.LENGTH_LONG);
                            showLabelsDialog();
                            updateTrainingData();
                        }
                    }
                }).execute(image);


            }
        });



        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_java_surface_view);
        mOpenCvCameraView.setCameraIndex(prefs.getInt("mCameraIndex", CameraBridgeViewBase.CAMERA_ID_FRONT));
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadOpenCV();
                } else {
                    showToast("Permission required!", Toast.LENGTH_LONG);
                    finish();
                }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();

    }

    @Override
    public void onStart() {
        super.onStart();
        updateTrainingData();

    }

    @Override
    public void onStop() {
        super.onStop();
        // Store threshold values
        Editor editor = prefs.edit();
        editor.putBoolean("useEigenfaces", useEigenfaces);
        editor.putInt("mCameraIndex", mOpenCvCameraView.mCameraIndex);
        editor.apply();

        // Store ArrayLists containing the images and labels
        if (images != null && imagesLabels != null) {
            tinydb.putListMat("images", images);
            tinydb.putListString("imagesLabels", imagesLabels);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateTrainingData();

        // Request permission if needed
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED/* || ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED*/)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA/*, Manifest.permission.WRITE_EXTERNAL_STORAGE*/}, PERMISSIONS_REQUEST_CODE);
        else
            loadOpenCV();
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    NativeMethods.loadNativeLibraries(); // Load native libraries after(!) OpenCV initialization
                    Log.i(TAG, "OpenCV loaded successfully");
                    updateTrainingData();
                    //LOADING XML FILES FOR FACE DETECTION
                    try {
                        // LOAD cascade file1 from application resources
                        InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_default);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "haarcascade_frontalface_default.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        // LOAD cascade file2 from application resources
                        is = getResources().openRawResource(R.raw.haarcascade_frontalface_alt2);
                        cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile2 = new File(cascadeDir, "haarcascade_frontalface_alt2.xml");
                        os = new FileOutputStream(mCascadeFile2);

                        //byte[] buffer = new byte[4096];
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        // LOAD cascade file3 from application resources
                        is = getResources().openRawResource(R.raw.haarcascade_frontalface_alt);
                        cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile3 = new File(cascadeDir, "haarcascade_frontalface_alt.xml");
                        os = new FileOutputStream(mCascadeFile3);

                        //byte[] buffer = new byte[4096];
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        // LOAD cascade file4 from application resources
                        is = getResources().openRawResource(R.raw.haarcascade_frontalface_alt_tree);
                        cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile4 = new File(cascadeDir, "haarcascade_frontalface_alt_tree.xml");
                        os = new FileOutputStream(mCascadeFile4);

                        //byte[] buffer = new byte[4096];
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        faceDet = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        faceDet.load( mCascadeFile.getAbsolutePath());

                        faceDet2 = new CascadeClassifier(mCascadeFile2.getAbsolutePath());
                        faceDet2.load( mCascadeFile2.getAbsolutePath());

                        faceDet3 = new CascadeClassifier(mCascadeFile3.getAbsolutePath());
                        faceDet3.load( mCascadeFile3.getAbsolutePath());

                        faceDet4 = new CascadeClassifier(mCascadeFile4.getAbsolutePath());
                        faceDet4.load( mCascadeFile4.getAbsolutePath());

                        //Log.i(TAG, mCascadeFile.getAbsolutePath());
                        if (faceDet.empty() || faceDet2.empty() || faceDet3.empty() || faceDet4.empty()) {
                            Log.e(TAG, "Failed to load all cascade classifiers");
                            faceDet = null;
                            faceDet2 = null;
                            faceDet3 = null;
                            faceDet4 = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifiers from " + mCascadeFile.getAbsolutePath());



                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }

                    mOpenCvCameraView.enableView();

                    // Read images and labels from shared preferences
                    images = tinydb.getListMat("images");
                    imagesLabels = tinydb.getListString("imagesLabels");

                    Log.i(TAG, "Number of images: " + images.size()  + ". Number of labels: " + imagesLabels.size());
                    updateTrainingData();
                    if (!images.isEmpty())
                        Log.i(TAG, "Images height: " + images.get(0).height() + " Width: " + images.get(0).width() + " total: " + images.get(0).total());
                    Log.i(TAG, "Labels: " + imagesLabels);
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    private void loadOpenCV() {
        if (!OpenCVLoader.initDebug(true)) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
    }

    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mGray = inputFrame.gray();
        mRgba = inputFrame.rgba();

        // Flip image to get mirror effect
        int orientation = mOpenCvCameraView.getScreenOrientation();
        if (mOpenCvCameraView.isEmulator()) // Treat emulators as a special case
            Core.flip(mRgba, mRgba, 1); // Flip along y-axis
        else {
            switch (orientation) {
                case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
                case ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT:
                    if (mOpenCvCameraView.mCameraIndex == CameraBridgeViewBase.CAMERA_ID_FRONT)
                        Core.flip(mRgba, mRgba, 0); // Flip along x-axis
                    else
                        Core.flip(mRgba, mRgba, -1); // Flip along both axis
                    break;
                case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
                case ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE:
                    if (mOpenCvCameraView.mCameraIndex == CameraBridgeViewBase.CAMERA_ID_FRONT)
                        Core.flip(mRgba, mRgba, 1); // Flip along y-axis
                    break;
            }
        }

        return mRgba;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void SaveImage(Mat mat) {
        Mat mIntermediateMat = new Mat();

        if (mat.channels() == 1) // Grayscale image
            Imgproc.cvtColor(mat, mIntermediateMat, Imgproc.COLOR_GRAY2BGR);
        else
            Imgproc.cvtColor(mat, mIntermediateMat, Imgproc.COLOR_RGBA2BGR);

        File path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), TAG); // Save pictures in Pictures directory
        path.mkdir(); // Create directory if needed
        String fileName = "IMG_" + new SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(new Date()) + ".png";
        File file = new File(path, fileName);

        boolean bool = Imgcodecs.imwrite(file.toString(), mIntermediateMat);

        if (bool)
            Log.i(TAG, "SUCCESS writing image to external storage");
        else
            Log.e(TAG, "Failed writing image to external storage");
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_face_recognition_app, menu);
        // Show rear camera icon if front camera is currently used and front camera icon if back camera is used
        MenuItem menuItem = menu.findItem(R.id.flip_camera);
        if (mOpenCvCameraView.mCameraIndex == CameraBridgeViewBase.CAMERA_ID_FRONT)
            menuItem.setIcon(R.drawable.ic_camera_rear_white_24dp);
        else
            menuItem.setIcon(R.drawable.ic_camera_front_white_24dp);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.flip_camera:
                mOpenCvCameraView.flipCamera();

                // Do flip camera animation
                View v = mToolbar.findViewById(R.id.flip_camera);
                ObjectAnimator animator = ObjectAnimator.ofFloat(v, "rotationY", v.getRotationY() + 180.0f);
                animator.setDuration(500);
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        supportInvalidateOptionsMenu();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                animator.start();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
