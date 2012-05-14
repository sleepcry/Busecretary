package com.chaos.sleepcry.busecretary.utils;

import com.chaos.sleepcry.busecretary.GlobalSettings;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;

public class SmartMediaPlayer {
	MediaPlayer mPlayer;
	AudioManager mAm;

	public static SmartMediaPlayer create(Context context, int resid) {
		SmartMediaPlayer player = new SmartMediaPlayer();
		try {
			player.mPlayer = MediaPlayer.create(context, resid);
			player.mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			player.mPlayer.setOnCompletionListener(player.mOnCompleteListener);
			player.mAm = (AudioManager) context
					.getSystemService(Context.AUDIO_SERVICE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return player;
	}

	public static void initVolumeType(Activity activity) {
		activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
	}

	public void pause() {
		if (GlobalSettings.need_sound && mPlayer != null) {
			mPlayer.pause();
		}
	}

	public void release() {
		if (mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
		}
	}

	OnCompletionListener mOnCompleteListener = new OnCompletionListener() {

		@Override
		public void onCompletion(MediaPlayer mp) {
			mAm.abandonAudioFocus(null);
		}

	};

	public void start() {
		if (GlobalSettings.need_sound && mPlayer != null) {
			int result = mAm.requestAudioFocus(null,
					// Use the music stream.
					AudioManager.STREAM_MUSIC,
					AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);

			if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
				mPlayer.start();
			}
		}
	}
}