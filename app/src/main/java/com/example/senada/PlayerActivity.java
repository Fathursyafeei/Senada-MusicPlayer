package com.example.senada;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import com.gauravk.audiovisualizer.visualizer.BarVisualizer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PlayerActivity extends AppCompatActivity {

    AppCompatButton btnplay, btnpause, btnprev, btnnext, btnback;
    TextView txtsname, tvTime;
    SeekBar seekmusic;
    ImageView imageView;
    public String txtsstop;
    Animation animation;
    BarVisualizer visualizer;

    String sname;
    public static final String EXTRA_NAME = "song_name";
    static MediaPlayer mediaPlayer;
    int position;
    boolean isPlaying = false;
    ArrayList<File> mySongs;
    Thread updateseekbar;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (visualizer != null){
            visualizer.release();
        }
        super.onDestroy();

    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);


//        this.getSupportActionBar().setTitle("Now Playing");
//        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        this.getSupportActionBar().setDisplayShowHomeEnabled(true);

        btnplay = findViewById(R.id.playbtn);
        btnprev = findViewById(R.id.btnprev);
        btnnext = findViewById(R.id.btnnext);
        txtsname = findViewById(R.id.txtsn);
        tvTime = findViewById(R.id.tvTime);
        seekmusic = findViewById(R.id.seekbar);
        imageView = findViewById(R.id.imgCover);
        visualizer = findViewById(R.id.blast);
        btnback = findViewById(R.id.btn_back);




        if (mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        // mendapatkan jumlah nama dan info dari activity sebelumnya
        Intent i = getIntent();
        Bundle bundle = i.getExtras();

        //mengambil informasi dari intent
        mySongs = (ArrayList) bundle.getParcelableArrayList("songs");
        String songName = i.getStringExtra("songName");   //Mengambil nama lagu
        position = bundle.getInt("pos",0);      //Mendapatkan /mengambil posisi
        txtsname.setSelected(true);   //Memasukkan nama lagu di dalam textview
        Uri uri = Uri.parse(mySongs.get(position).toString());
        sname = mySongs.get(position).getName();
        txtsname.setText(sname);

        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        mediaPlayer.start();

        updateseekbar = new Thread(){
            @Override
            public void run() {
                int totalDuration = mediaPlayer.getDuration();
                int currentposition = 0;

                while(currentposition<totalDuration){
                    try{
                        sleep(500);
                        currentposition = mediaPlayer.getCurrentPosition();
                        seekmusic.setProgress(currentposition);
                    }
                    catch (InterruptedException | IllegalStateException e){
                        e.printStackTrace();
                    }
                }

//
            }
        };



        mediaPlayer.seekTo(0);
        int duration = mediaPlayer.getDuration();
        final ObjectAnimator rotate = ObjectAnimator.ofFloat(imageView, View.ROTATION, 0f, 360f).setDuration(30000);
        rotate.setInterpolator(new LinearInterpolator());
        rotate.setRepeatCount(animation.INFINITE);
        rotate.start();


        seekmusic.setMax(mediaPlayer.getDuration());
        updateseekbar.start();
        seekmusic.getProgressDrawable().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        seekmusic.getThumb().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);

        seekmusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser){
                    mediaPlayer.seekTo(progress);
                    seekBar.setProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                
            }

        });

        int endTime = mediaPlayer.getCurrentPosition();
        int seek = seekmusic.getProgress();
        if(endTime >= seek){
            seekmusic.setProgress(0);
        }



        final Handler handler = new Handler();
        final int delay = 1000;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentTime = createTime(mediaPlayer.getCurrentPosition());
                tvTime.setText(currentTime);
                handler.postDelayed(this, delay);

            }
        }, delay);


        btnplay.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isPlaying()){
                    btnplay.setBackgroundResource(R.drawable.ic_play);
                    mediaPlayer.pause();
                    rotate.pause();

                }
                else {
                    btnplay.setBackgroundResource(R.drawable.ic_pause);
                    mediaPlayer.start();
                    rotate.resume();
                    if(endTime >= seek){
                        seekmusic.setProgress(0);
                    }

                }

            }
        });

        // next listener
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                btnnext.performClick();
                seekmusic.setProgress(0);

            }
        });

        int audiosessionId = mediaPlayer.getAudioSessionId();
        if (audiosessionId != -1){
            visualizer.setAudioSessionId(audiosessionId);
        }

        //Button untuk kembali ke list lagu
        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent back = new Intent(PlayerActivity.this, LokalActivity.class);
                startActivity(back);
                finish();
            }
        });

        btnnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                rotate.reverse();
                rotate.start();
                position = ((position+1)%mySongs.size());
                Uri u = Uri.parse(mySongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), u);
                sname = mySongs.get(position).getName();
                txtsname.setText(sname);
                mediaPlayer.start();
                seekmusic.setProgress(0);
                btnplay.setBackgroundResource(R.drawable.ic_pause);

                startAnimation(imageView);

                int audiosessionId = mediaPlayer.getAudioSessionId();
                if (audiosessionId != -1){
                    visualizer.setAudioSessionId(audiosessionId);
                }

            }
        });

        btnprev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                rotate.reverse();
                rotate.start();
                position = ((position-1)<0)?(mySongs.size()-1):(position-1);
                Uri u = Uri.parse(mySongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), u);
                sname = mySongs.get(position).getName();
                txtsname.setText(sname);
                mediaPlayer.start();
                seekmusic.setProgress(0);
                btnplay.setBackgroundResource(R.drawable.ic_pause);

                startAnimation(imageView);

                int audiosessionId = mediaPlayer.getAudioSessionId();
                if (audiosessionId != -1){
                    visualizer.setAudioSessionId(audiosessionId);
                }

            }
        });




    }


    public void startAnimation(View view){
        ObjectAnimator animator = ObjectAnimator.ofFloat(imageView, "rotation", 0f, 360f);
        animator.setDuration(1000);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animator);
        animatorSet.start();
    }

    public String createTime(int duration){
        String time = "";
        int min = duration/1000/60;
        int sec = duration/1000%60;

        time+=min+":";
        if (sec<10){
            time+="0";
        }
        time+=sec;

        return time;
    }



}