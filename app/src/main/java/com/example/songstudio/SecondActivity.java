package com.example.songstudio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;


public class SecondActivity extends AppCompatActivity {
    // Start play audio button.
    private Button startButton;

    // Pause playing audio button.
    private Button pauseButton;

    // Stop playing audio button.
    private Button stopButton;

    // Show played audio progress.
    private SeekBar playAudioProgress;

    // Used when user select audio file.
    private static final int REQUEST_CODE_SELECT_AUDIO_FILE = 1;

    // Used when update audio progress thread send message to progress bar handler.
    private static final int UPDATE_AUDIO_PROGRESS_BAR = 3;

    // Used to control audio (start, pause , stop etc).
    private MediaPlayer audioPlayer = null;

    // Audio file url.
    private Uri audioFileUri = null;

    // Used to distinguish log data.
    public static final String TAG_PLAY_AUDIO = "PLAY_AUDIO";

    // Wait update audio progress thread sent message, then update audio play progress.
    private Handler audioProgressHandler = null;

    // The thread that send message to audio progress handler to update progress every one second.
    private Thread updateAudioPlayerProgressThread = null;

    // Record whether audio is playing or not.
    private boolean audioIsPlaying = false;

    private Song_details song;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        TextView txtSong = (TextView) findViewById(R.id.song_label);

        Intent i = getIntent();
        // getting attached intent data
        song = getIntent().getParcelableExtra("song");
        // displaying selected song name
        txtSong.setText(song.name);

        startButton = (Button)findViewById(R.id.play_audio_start_button);

        pauseButton = (Button)findViewById(R.id.play_audio_pause_button);

        stopButton = (Button)findViewById(R.id.play_audio_stop_button);

        playAudioProgress = (SeekBar) findViewById(R.id.play_audio_seekbar);

        /* Initialize audio progress handler. */
        if(audioProgressHandler==null) {
            audioProgressHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == UPDATE_AUDIO_PROGRESS_BAR) {

                        if(audioPlayer!=null) {
                            // Get current play time.
                            int currPlayPosition = audioPlayer.getCurrentPosition();

                            // Get total play time.
                            int totalTime = audioPlayer.getDuration();

                            // Calculate the percentage.
                            int currProgress = (currPlayPosition * 1000) / totalTime;

                            // Update progressbar.
                            playAudioProgress.setProgress(currProgress);
                        }
                    }
                }
            };
        }


        // When start button is clicked.
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startButton.setEnabled(false);

                pauseButton.setEnabled(true);

                stopButton.setEnabled(true);

                String audioFilePath = song.url;
                if(!TextUtils.isEmpty(audioFilePath)) {

                    stopCurrentPlayAudio();

                    initAudioPlayer();

                    audioPlayer.start();


                    audioIsPlaying = true;

                    // Display progressbar.
                    playAudioProgress.setVisibility(ProgressBar.VISIBLE);

                    if(updateAudioPlayerProgressThread == null) {

                        // Create the thread.
                        updateAudioPlayerProgressThread = new Thread() {
                            @Override
                            public void run() {
                                try {
                                    while (audioIsPlaying) {

                                        if (audioProgressHandler != null) {
                                            // Send update audio player progress message to main thread message queue.
                                            Message msg = new Message();
                                            msg.what = UPDATE_AUDIO_PROGRESS_BAR;
                                            audioProgressHandler.sendMessage(msg);

                                            Thread.sleep(1000);
                                        }
                                    }
                                } catch (InterruptedException ex) {
                                    Log.e(TAG_PLAY_AUDIO, ex.getMessage(), ex);
                                }
                            }
                        };
                        updateAudioPlayerProgressThread.start();
                    }
                }else
                {
                    Toast.makeText(getApplicationContext(), "Please specify an audio file to play.", Toast.LENGTH_LONG).show();
                }
            }
        });


        /* When pause button is clicked. */
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(audioIsPlaying)
                {
                    audioPlayer.pause();
                    startButton.setEnabled(true);
                    pauseButton.setEnabled(false);
                    stopButton.setEnabled(true);

                    audioIsPlaying = false;

                    updateAudioPlayerProgressThread = null;
                }
            }
        });

        /* When stop button is clicked. */
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(audioIsPlaying)
                {
                    audioPlayer.stop();
                    audioPlayer.release();
                    audioPlayer = null;
                    startButton.setEnabled(true);
                    pauseButton.setEnabled(false);
                    stopButton.setEnabled(false);

                    updateAudioPlayerProgressThread = null;
                    playAudioProgress.setProgress(0);
                    playAudioProgress.setVisibility(ProgressBar.INVISIBLE);

                    audioIsPlaying = false;
                }
            }
        });



    }

    /* Initialize media player. */
    private void initAudioPlayer()
    {
        try {
            if(audioPlayer == null)
            {
                audioPlayer = new MediaPlayer();

                String audioFilePath = song.url;

                Log.d(TAG_PLAY_AUDIO, audioFilePath);

                if(audioFilePath.toLowerCase().startsWith("http://"))
                {
                    // Web audio from a url is stream music.
                    audioPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    // Play audio from a url.
                    audioPlayer.setDataSource(audioFilePath);
                }
                audioPlayer.prepare();
            }
        }catch(IOException ex)
        {
            Log.e(TAG_PLAY_AUDIO, ex.getMessage(), ex);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE_SELECT_AUDIO_FILE)
        {
            if(resultCode==RESULT_OK)
            {

                audioFileUri = data.getData();
                String audioFileName = audioFileUri.getLastPathSegment();

                initAudioPlayer();

                startButton.setEnabled(true);

                pauseButton.setEnabled(false);

                stopButton.setEnabled(false);
            }
        }
    }

    /* Stop current play audio before play another. */
    private void stopCurrentPlayAudio()
    {
        if(audioPlayer!=null && audioPlayer.isPlaying())
        {
            audioPlayer.stop();
            audioPlayer.release();
            audioPlayer = null;
        }
    }

    @Override
    protected void onDestroy() {
        if(audioPlayer!=null)
        {
            if(audioPlayer.isPlaying())
            {
                audioPlayer.stop();
            }

            audioPlayer.release();
            audioPlayer = null;
        }

        super.onDestroy();
    }
}
