package com.linex.musicapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class Play extends AppCompatActivity {

    Button btnPlay,btnNext, btnPause, btnPrevious, btnFastForward, btnFastBackword;
    TextView txtSongName, txtSongStart, txtSongEnd;
    SeekBar seekMusicBar;

    ImageView imageView;

    String songName;
    public static final String EXTRA_NAME = "song_name";
    static MediaPlayer mediaPlayer;
    int position;
    ArrayList<File> mySongs;

    Thread updateSeekBar;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        getSupportActionBar().setTitle("Music App");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        btnPrevious = (Button) findViewById(R.id.btnPriveous);
        btnNext = (Button) findViewById(R.id.btnNext);
        btnPlay = (Button) findViewById(R.id.btnPlay);
        btnFastBackword = (Button) findViewById(R.id.btnFastBackward);
        btnFastForward = (Button) findViewById(R.id.btnFastForward);

       txtSongName = (TextView) findViewById(R.id.txtSongs);
       txtSongStart = (TextView) findViewById(R.id.txtSongStart);
       txtSongEnd = (TextView) findViewById(R.id.txtSongEnd);

       seekMusicBar = (SeekBar) findViewById(R.id.seekBar);

       imageView = (ImageView) findViewById(R.id.imgView);

       if(mediaPlayer != null){
           mediaPlayer.start();
           mediaPlayer.release();
       }

       Intent intent = getIntent();
       Bundle bundle = intent.getExtras();

       mySongs = (ArrayList) bundle.getParcelableArrayList("songs");
       String sName = intent.getStringExtra("songname");
       position = bundle.getInt("pos",0);
       txtSongName.setSelected(true);
       Uri uri = Uri.parse(mySongs.get(position).toString());
       songName = mySongs.get(position).getName();
       txtSongName.setText(songName);

       mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
       mediaPlayer.start();

       updateSeekBar = new Thread(){
           @Override
           public void run(){
               int totalDuration = mediaPlayer.getDuration();
               int currentPosition = 0;

               while(currentPosition<totalDuration){
                   try{
                       sleep(500);
                       currentPosition = mediaPlayer.getCurrentPosition();
                       seekMusicBar.setProgress(currentPosition);

                   }catch (InterruptedException | IllegalStateException e){
                       e.printStackTrace();
                   }
               }
           }
       };

       seekMusicBar.setMax(mediaPlayer.getDuration());
       updateSeekBar.start();
       seekMusicBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.purple_500), PorterDuff.Mode.MULTIPLY);
       seekMusicBar.getThumb().setColorFilter(getResources().getColor(R.color.purple_500), PorterDuff.Mode.SRC_IN);

       seekMusicBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
           @Override
           public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

           }

           @Override
           public void onStartTrackingTouch(SeekBar seekBar) {

           }

           @Override
           public void onStopTrackingTouch(SeekBar seekBar) {

               mediaPlayer.seekTo(seekBar.getProgress());
           }
       });

       String endTime = createTime(mediaPlayer.getDuration());
       txtSongEnd.setText(endTime);

       final Handler handler = new Handler();
       final int delay = 1000;

       handler.postDelayed(new Runnable() {
           @Override
           public void run() {
               String currentTime = createTime(mediaPlayer.getCurrentPosition());
               txtSongStart.setText(currentTime);
               handler.postDelayed(this, delay);
           }
       }, delay);

       btnPlay.setOnClickListener(view -> {
           if(mediaPlayer.isPlaying()){
               btnPlay.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24);
               mediaPlayer.pause();
           }else{
               btnPlay.setBackgroundResource(R.drawable.ic_baseline_pause_24);
               mediaPlayer.start();

               TranslateAnimation moveAim = new TranslateAnimation(-30, 30, -30, 30);
               moveAim.setInterpolator(new AccelerateInterpolator());
               moveAim.setDuration(600);
               moveAim.setFillEnabled(true);
               moveAim.setFillAfter(true);
               moveAim.setRepeatMode(Animation.REVERSE);
               moveAim.setRepeatCount(1);
             imageView.startAnimation(moveAim);
           }
       });

       mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
           @Override
           public void onCompletion(MediaPlayer mediaPlayer) {
               btnNext.performClick();
           }
       });



       btnNext.setOnClickListener(view ->{
           mediaPlayer.stop();
           mediaPlayer.release();
           position = ((position + 1)%mySongs.size());
           Uri uri1 = Uri.parse(mySongs.get(position).toString());
           mediaPlayer = MediaPlayer.create(getApplicationContext(), uri1);
           songName = mySongs.get(position).getName();
           txtSongName.setText(songName);
           mediaPlayer.start();

           String endTimeNext = createTime(mediaPlayer.getDuration());
           txtSongEnd.setText(endTimeNext);

           startAnimation(imageView, 360f);
       });

       btnPrevious.setOnClickListener(view -> {
           mediaPlayer.stop();
           mediaPlayer.release();
           position = ((position - 1)< 0) ? (mySongs.size()-1):position-1;
           Uri uri2 = Uri.parse(mySongs.get(position).toString());
           mediaPlayer = MediaPlayer.create(getApplicationContext(), uri2);
           songName = mySongs.get(position).getName();
           txtSongName.setText(songName);
           mediaPlayer.start();

           String endTimePrevious = createTime(mediaPlayer.getDuration());
           txtSongEnd.setText(endTimePrevious);

           startAnimation(imageView, -360f);
       });

       btnFastForward.setOnClickListener(view ->{

           if(mediaPlayer.isPlaying()){
               mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+10000);
           }

       });

        btnFastBackword.setOnClickListener(view ->{

            if(mediaPlayer.isPlaying()){
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-10000);
            }

        });

    }

    public void startAnimation(View view, Float degree){

        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(imageView, "rotation", 0f, degree );
        objectAnimator.setDuration(1000);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(objectAnimator);
        animatorSet.start();

    }

    public String createTime(int duration){
        String time = "";
        int min = duration/1000/60;
        int sec = duration/1000%60;

        time = time+min+":";
        if(sec < 10){
            time+="0";
        }
        time+=sec;
        return  time;
    }
}