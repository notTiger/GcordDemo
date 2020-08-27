package com.example.gcorddemo.dialogFragment;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gcorddemo.R;
import com.example.gcorddemo.databinding.DialogRecordPlayingBinding;

import java.io.FileInputStream;

import cn.com.geartech.gcordsdk.dataType.CallLogItem;

public class RecordPlayingDialogFragment extends BaseDialogFragment {
    private DialogRecordPlayingBinding dialogRecordPlayingBinding;
    private CallLogItem callLogItem;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    private CountDownTimer countDownTimer;
    private boolean ticking = false;
    private int duration = 0;

    @SuppressWarnings("UnusedReturnValue")
    public static RecordPlayingDialogFragment showDialog(AppCompatActivity parentActivity, CallLogItem callLogItem) {
        RecordPlayingDialogFragment recordPlayingDialogFragment = new RecordPlayingDialogFragment();
        recordPlayingDialogFragment.parentActivity = parentActivity;
        recordPlayingDialogFragment.callLogItem = callLogItem;
        recordPlayingDialogFragment.show();
        return recordPlayingDialogFragment;
    }

    @Override
    public int getWidth() {
        return 430;
    }

    @Override
    public int getHeight() {
        return WindowManager.LayoutParams.WRAP_CONTENT;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        dialogRecordPlayingBinding = DialogRecordPlayingBinding.inflate(inflater, container, false);
        return dialogRecordPlayingBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initViews();
    }

    private void initViews() {
        dialogRecordPlayingBinding.ivPauseOrPlay.setOnClickListener(v -> {
            boolean isSelected = v.isSelected();
            if (isSelected) {
                isPlaying = false;
                pausePlaying();
            } else {
                startPlaying();
            }
            v.setSelected(!isSelected);
        });
    }

    private void startTicking() {
        countDownTimer = new CountDownTimer(mediaPlayer.getDuration() + 1000, 500L) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (ticking) {
                    setupTimeStr(mediaPlayer.getCurrentPosition());
                }
            }

            @Override
            public void onFinish() {

            }
        };
        countDownTimer.start();
        ticking = true;

    }

    private void stopTicking() {
        if (countDownTimer == null) return;
        ticking = false;
        countDownTimer.cancel();
        countDownTimer = null;
    }

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        try {
            String filePath = callLogItem.getAudioRecordPath();
            Log.e("initMediaPlayer",filePath);
            FileInputStream is = new FileInputStream(filePath);
            mediaPlayer.setDataSource(is.getFD());
            mediaPlayer.setOnPreparedListener(mp -> parentActivity.runOnUiThread(() -> {
                duration = mediaPlayer.getDuration();
                setupTimeStr(0);
            }));
            mediaPlayer.setLooping(false);
            mediaPlayer.setVolume(1f, 1f);
            mediaPlayer.setOnCompletionListener(mp -> parentActivity.runOnUiThread(RecordPlayingDialogFragment.this::stopPlaying));
            mediaPlayer.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopPlaying() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        dialogRecordPlayingBinding.ivPauseOrPlay.setSelected(false);
        isPlaying = false;
        stopTicking();
        setupTimeStr(0);
    }


    private void setupTimeStr(int progress) {
        if (progress > duration) progress = duration;
        int total = duration;
        final int MILLIS_OF_AN_HOUR = 60 * 60 * 1000;
        int hours = total / MILLIS_OF_AN_HOUR;
        total %= MILLIS_OF_AN_HOUR;
        final int MILLIS_OF_A_MINUTE = 60 * 1000;
        int minutes = total / MILLIS_OF_A_MINUTE;
        total %= MILLIS_OF_A_MINUTE;
        int seconds = total / 1000;

        int hoursOfProgress = progress / MILLIS_OF_AN_HOUR;
        progress %= MILLIS_OF_AN_HOUR;
        int minutesOfProgress = progress / MILLIS_OF_A_MINUTE;
        progress %= MILLIS_OF_A_MINUTE;
        int secondsOfProgress = progress / 1000;
        String str;
        if (hours == 0) {
            str = parentActivity.getString(R.string.time_string_format1,
                    minutesOfProgress, secondsOfProgress,
                    minutes, seconds);
        } else {
            str = parentActivity.getString(R.string.time_string_format2,
                    hoursOfProgress, minutesOfProgress, secondsOfProgress,
                    hours, minutes, seconds);
        }
        dialogRecordPlayingBinding.tvTime.setText(str);
    }

    private void pausePlaying() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            stopTicking();
        }
    }

    private void startPlaying() {
        if (mediaPlayer == null) {
            initMediaPlayer();
        }
        mediaPlayer.start();
        isPlaying = true;
        startTicking();
        dialogRecordPlayingBinding.ivPauseOrPlay.setSelected(true);
    }

    @Override
    public void onPause() {
        pausePlaying();
        super.onPause();
    }

    @Override
    public void onResume() {
        if (isPlaying)
            startPlaying();
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroyView();
    }
}
