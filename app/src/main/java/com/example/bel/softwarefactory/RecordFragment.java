package com.example.bel.softwarefactory;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

/**
 * Created by Bel on 24.02.2016.
 */
public class RecordFragment extends Fragment {

    private MediaPlayer mediaPlayer;
    private MediaRecorder recorder;
    private RelativeLayout sfLayout;
    private RecordButton sfRecordButton;
    private PlayRecordingButton sfPlayButton;
    private LinearLayout lShareEditDelete;
    //private StopButton sfStopButton;

    private TextView tvSeconds;
    private TextView tvMinutes;
    private TextView tvHours;

    private UserLocalStore userLocalStore;

    private final File EXTERNAL_STORAGE_PATH = Environment.getExternalStorageDirectory();
    private String recordName = "audio";

    private final String RECORD_TAG = "Debug_Record";

    //Create buttons and layout
    public void createLayout() {

        //create button with changeable images
        sfRecordButton = new RecordButton(getContext());
        //set ID defined in ids.xml file
        sfRecordButton.setId(R.id.RecordButton);
        //set picture background to transparent
        //sfRecordButton.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        sfRecordButton.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.transparent));

        // set layout parameters for button
        RelativeLayout.LayoutParams paramsRecord = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //add button BELOW + centralizing last button with name Stop Play audio
        paramsRecord.addRule(RelativeLayout.CENTER_IN_PARENT);
        paramsRecord.addRule(RelativeLayout.BELOW, R.id.Timer);
        //add button for Recording to the layout + parameters
        sfLayout.addView(sfRecordButton, paramsRecord);

//        //create button with changeable images
//        sfStopButton = new StopButton(getContext());
//        //set ID defined in ids.xml file
//        sfStopButton.setId(R.id.StopButton);
//        //set picture background to transparent
//        sfStopButton.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        // set layout parameters for button
        RelativeLayout.LayoutParams paramsStop = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //add button BELOW + centralizing last button with name Stop Play audio
        paramsStop.addRule(RelativeLayout.LEFT_OF, sfRecordButton.getId());
        paramsStop.addRule(RelativeLayout.BELOW, R.id.Timer);
        paramsStop.addRule(RelativeLayout.CENTER_HORIZONTAL);
        //add button for Recording to the layout + parameters
        //sfLayout.addView(sfStopButton, paramsStop);

        //create button with changeable images
        sfPlayButton = new PlayRecordingButton(getContext());
        //set picture background to transparent
        sfPlayButton.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.transparent));

        // set layout parameters for button
        RelativeLayout.LayoutParams paramsPlay = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //add button BELOW + centralizing last button with name Stop Play audio
        paramsPlay.addRule(RelativeLayout.RIGHT_OF, sfRecordButton.getId());
        paramsPlay.addRule(RelativeLayout.BELOW, R.id.Timer);
        paramsPlay.addRule(RelativeLayout.CENTER_HORIZONTAL);
        //add button for Recording to the layout + parameters
        sfLayout.addView(sfPlayButton, paramsPlay);
    }

    class RecordButton extends ImageButton {
        private boolean isRecording;
        private MessageHandler messageHandler = new MessageHandler();
        private int curTime;
        private boolean isRecordSaved = false;

        public RecordButton(Context ctx) {
            super(ctx);
            setImageResource(R.mipmap.ic_rec);
            setOnClickListener(clicker);
            setRecording(true);
        }

        public void setRecording(boolean recording) {
            this.isRecording = recording;
        }

        public boolean getRecording() {
            return isRecording;
        }

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                //function to check recording
                if (isRecording) {
                    setRecording(!isRecording);
                    //change the image to stop
                    setImageResource(R.mipmap.ic_rec_stop);
                    //Start recording the sound
                    StartRecord();
                    sfPlayButton.disable();
                } else {
                    setRecording(!isRecording);
                    //change image to recording
                    setImageResource(R.mipmap.ic_rec);
                    //function to Stop recording
                    StopRecord();
                    sfPlayButton.enable();
                }
                Log.d(RECORD_TAG, "RecordButton onClick() isRecording : " + isRecording);
            }
        };

        public void StartRecord() {
            ditchMediarecorder();
            /*
            * Check if there is existing file, then delete
            * */
            File outputFile = new File(getRecordPath());
            if (outputFile.exists()) {
                outputFile.delete();
            }
            //initialize file name of the time when the recording is done
            recordName = getCurrentDate();

            /*
            * Initialize media recorder and start recording
            * */
            recorder = new MediaRecorder();
            recorder.setAudioSource(AppConstants.AUDIO_SOURCE);
            recorder.setOutputFormat(AppConstants.OUTPUT_FORMAT);
            recorder.setAudioEncoder(AppConstants.AUDIO_ENCODER);
            recorder.setOutputFile(getRecordPath());
            try {
                recorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            recorder.start();
            startTimerCounting();
            lShareEditDelete.setVisibility(View.GONE);
        }

        public void ditchMediarecorder() {
            if (recorder != null) recorder.release();
        }

        public void StopRecord() {
            if (recorder != null) {
                recorder.reset();
                recorder.release();
                recorder = null;
            }
            //show additional menu for editing file
            lShareEditDelete.setVisibility(View.VISIBLE);
            editRecordName();
        }

        public void startTimerCounting() {
            curTime = 0;
            fillTimer("0","0","0");
            Thread thread = new Thread(new TimerRecord());
            thread.start();
        }

        public void enable() {
            setEnabled(true);
            setAlpha(1f);
        }

        public void disable() {
            setEnabled(false);
            setAlpha(0.5f);
        }

        private class TimerRecord implements Runnable {
            @Override
            public void run() {
                Log.d(RECORD_TAG, "TimeRecord run() isRecording" + getRecording());
                while (!getRecording()) {
                    ++curTime;
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Bundle bundle = new Bundle();
                    bundle.putInt("count", curTime);

                    Message message = new Message();
                    message.setData(bundle);
                    messageHandler.sendMessage(message);
                }
                Bundle bundle = new Bundle();
                bundle.putInt("count", --curTime);
                Message message = new Message();
                message.setData(bundle);
                messageHandler.sendMessage(message);
            }
        }

        // Handler for Runnable class + Timer filling
        private class MessageHandler extends Handler {
            @Override
            public void handleMessage(Message msg) {
                convertSecondsToTime(msg.getData().getInt("count"));
            }

            public void convertSecondsToTime(int duration) {
                String hours = String.valueOf(duration / 3600);
                String minutes = String.valueOf((duration % 3600) / 60);
                String seconds = String.valueOf((duration % 3600) % 60);
                fillTimer(hours, minutes, seconds);
            }
        }

    }

    class PlayRecordingButton extends ImageButton {
        boolean sfPlayRecording = true;

        public PlayRecordingButton(Context ctx) {
            super(ctx);
            setImageResource(R.mipmap.ic_rec_play);
            setOnClickListener(clicker);
            setVisibility(View.GONE);
        }

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                //function to check recording
                if (sfPlayRecording) {
                    setImageResource(R.mipmap.ic_rec_pause);
                    try {
                        PlayRecord();
                        sfRecordButton.disable();
                        //sfStopButton.enable();
//                        Toast toast = Toast.makeText(getContext(), "Playing the audio...", Toast.LENGTH_LONG);
//                        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
//                        toast.show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    setImageResource(R.mipmap.ic_rec_play);
                    PausePlay();
                    sfRecordButton.enable();
                    //sfStopButton.disable();
                }
                sfPlayRecording = !sfPlayRecording;
            }
        };

        public void PlayRecord() throws IOException {
            ditchMediaPlayer();

            mediaPlayer = new MediaPlayer();
            mediaPlayer.start();
            try {
                mediaPlayer.setDataSource(getRecordPath());
                //mediaPlayer.setOnCompletionListener(getContext());
                mediaPlayer.prepare();
                mediaPlayer.start();
                fillTimer("0", "0", "0");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void ditchMediaPlayer() {
            if (mediaPlayer != null) mediaPlayer.release();
        }

        public void PausePlay() {
            if (mediaPlayer != null) mediaPlayer.pause();
        }

        public void StopPlay() {
            if (mediaPlayer != null) mediaPlayer.stop();
        }

        public void enable() {
            setVisibility(View.VISIBLE);
            //setEnabled(true);
            //setAlpha(1f);
        }

        public void disable() {
            setVisibility(View.GONE);
            //setEnabled(false);
            //setAlpha(0.5f);
        }

    }

//    class StopButton extends ImageButton {
//        //boolean sfStartRecording = true;
//        int whatToStop;
//
//        public StopButton(Context ctx) {
//            super(ctx);
//            setImageResource(R.mipmap.ic_rec_stop);
//            setOnClickListener(clicker);
//            disable();
//        }
//
//        OnClickListener clicker = new OnClickListener() {
//            public void onClick(View v) {
//                if (getWhatToStop() == 1)
//                    StopRecord();
//                else if (getWhatToStop() == 2)
//                    StopPlay();
//            }
//        };
//
//        public void enable() {
//            setEnabled(true);
//            setAlpha(1f);
//        }
//
//        public void disable() {
//            setEnabled(false);
//            setAlpha(0.5f);
//        }
//
//        public void setWhatToStop(int whatToStop) {
//            this.whatToStop = whatToStop;
//            enable();
//        }
//
//        public int getWhatToStop() {
//            return whatToStop;
//        }
//    }

    // functions for MEDIA RECORDING


    //finish of MEDIA RECORDING functions

    // functions for MEDIA PLAYER


    //Start timer functions

    public void fillTimer(String h, String m, String s) {
        tvSeconds.setText(s.length() == 1 ? "0" + s : s);
        tvMinutes.setText(m.length() == 1 ? "0" + m : m);
        tvHours.setText(h.length() == 1 ? "0" + h : h);
    }

    //get file duration
    //code taken from http://stackoverflow.com/questions/15394640/get-duration-of-audio-file
    public long getDuration(String dataSource) {
        MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
        metaRetriever.setDataSource(dataSource);
        String durationString =
                metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long durationLong = Long.parseLong(durationString);
        convertMillisecondsToTime(durationLong);
        //Toast.makeText(getContext(), durationString, Toast.LENGTH_LONG).show();
        return durationLong;
    }

    public void convertMillisecondsToTime(long duration) {
        String hours = String.valueOf(((duration / (1000 * 60 * 60)) % 24));
        String minutes = String.valueOf((duration / (1000 * 60)) % 60);
        String seconds = String.valueOf((duration / 1000) % 60);
        fillTimer(hours, minutes, seconds);
    }

    // Upload file to server for BUTTON SAVE
    public void uploadRecordingToServer(File file, String owner) {
        Log.d(RECORD_TAG, "uploadRecordingToServer()");
        SendFileToServer sendFileToServer = new SendFileToServer(getContext());

        if(userLocalStore.getLastLatitude().isEmpty() && userLocalStore.getLastLongitude().isEmpty()){
            Toast.makeText(getActivity(), "Impossible to save file. Possibly GPS is not enabled.", Toast.LENGTH_LONG).show();
            return;
        }

        sendFileToServer.uploadFile(file, owner, userLocalStore.getLastLatitude(), userLocalStore.getLastLongitude());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(RECORD_TAG, "onCreateView()");

        View view = inflater.inflate(R.layout.fragment_record, container, false);
        sfLayout = (RelativeLayout) view.findViewById(R.id.sfLayout);
        tvSeconds = (TextView) view.findViewById(R.id.tvTimerSeconds);
        tvMinutes = (TextView) view.findViewById(R.id.tvTimerMinutes);
        tvHours = (TextView) view.findViewById(R.id.tvTimerHours);
        lShareEditDelete = (LinearLayout) view.findViewById(R.id.lShareEditDelete);
        lShareEditDelete.setVisibility(View.GONE);

        userLocalStore = new UserLocalStore(getActivity());
/*
*   @    Create layout with record buttons
*/
        createLayout();

/*
*   @    Share button & function
*/
        Button buttonShare = (Button) view.findViewById(R.id.bShareRecord);
        buttonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File outputFile = new File(getRecordPath());
                if (outputFile.exists()){
                    String owner = userLocalStore.isFacebookLoggedIn() ? userLocalStore.getFaceboookId()+"" : userLocalStore.getEmail();
                    Log.d(RECORD_TAG, "uploadRecordingToServer for owner " + owner);

                    /*
                    * Change fragment to map in order to get the location
                    * */
                    FragmentManager mFragmentManager = getFragmentManager();
                    mFragmentManager.beginTransaction()
                            .replace(R.id.frameLayoutMainContent, new Map(), "Map")
                            .addToBackStack(null)
                            .commit();

                    uploadRecordingToServer(outputFile, owner);
                }
            }
        });

/*
   @     Edit button & function
*/
        Button buttonEdit = (Button) view.findViewById(R.id.bEditRecord);
        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editRecordName();
            }
        });

/*
  @      Delete button & function
*/
        Button buttonDelete = (Button) view.findViewById(R.id.bDeleteRecord);
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Delete this file
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
                alertBuilder.setTitle(R.string.is_delete_file);
                alertBuilder.setCancelable(true);
                alertBuilder.setPositiveButton(R.string.delete_record, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        File file = new File(getRecordPath());
                        file.delete();
                        lShareEditDelete.setVisibility(View.GONE);
                        fillTimer("0","0","0");
                    }
                });
                alertBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alertBuilder.show();
            }
        });

        //file location
        //OUTPUT_FILE = EXTERNAL_STORAGE_PATH + File.separator + recordName + AppConstants.AUDIO_EXTENSION;

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            getActivity().getActionBar().setTitle("Record ambient sounds");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void editRecordName(){
        File file = new File(getRecordPath());
        Log.d(RECORD_TAG, getRecordPath());
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
        alertBuilder.setTitle(R.string.is_edit_file);
        alertBuilder.setCancelable(true);

        final EditText etChangeRecordName = new EditText(getActivity());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        etChangeRecordName.setLayoutParams(params);
        etChangeRecordName.setText(file.getName());
        alertBuilder.setView(etChangeRecordName);

        alertBuilder.setPositiveButton(R.string.edit_record, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newRecordName = etChangeRecordName.getText().toString();
                if (newRecordName.isEmpty())
                    dialog.dismiss();

                newRecordName = newRecordName.replace(AppConstants.AUDIO_EXTENSION, "");
                //Log.d(RECORD_TAG, newRecordName);

                File from = new File(EXTERNAL_STORAGE_PATH, recordName + AppConstants.AUDIO_EXTENSION);
                File to = new File(EXTERNAL_STORAGE_PATH, newRecordName + AppConstants.AUDIO_EXTENSION);

                if (from.exists()) {
                    from.renameTo(to);
                    recordName = newRecordName;
                }
            }
        });
        alertBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertBuilder.show();
    }

    //get the path of the record if the name was changed, path for already recorded sound and future changes
    private String getRecordPath(){
        return EXTERNAL_STORAGE_PATH + File.separator + recordName + AppConstants.AUDIO_EXTENSION;
    }

    private String getCurrentDate(){
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.DAY_OF_MONTH) + "_" + calendar.get(Calendar.MONTH) + "_"
                +  calendar.get(Calendar.YEAR) + "_" + calendar.get(Calendar.HOUR) + ":" + calendar.get(Calendar.MINUTE) + ":"
                + calendar.get(Calendar.SECOND);
    }

}
