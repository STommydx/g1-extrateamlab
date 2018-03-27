package com.company.g1.g1extrateamlab;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class GameActivity extends AppCompatActivity {

	private GameRound mGameRound;

	private SensorManager mSensorManager;
	private Sensor mSensor;
	private SensorListener mSensorListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);

		setupSensor();

		ProgressBar mProgressBar = findViewById(R.id.progressBar);
		mProgressBar.getProgressDrawable().setColorFilter(Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
		mProgressBar.setMax(100);
		mProgressBar.setProgress(100);

		mGameRound = new GameRound(this);
		mGameRound.start();
	}

	private void setupSensor() {
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		assert mSensorManager != null;
		if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
			mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		}

		mSensorListener = new SensorListener();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mSensorListener.stopSensor();
		mGameRound.pause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mSensorListener.startSensor();
		mGameRound.resume();
	}

	public class GameRound {

		private static final int NO_OF_LANES = 3;
		private static final int GAME_TICKS = 600;

		private ImageView player;
		private TimerTask ticker;
		private Timer mTimer;

		private int ticks;
		private boolean running = false;

		private Context mContext;

		public GameRound(Context context) {
			mContext = context;

			player = new ImageView(context);
			ConstraintLayout layout = findViewById(R.id.game_layout);

			layout.addView(player);
			player.setImageResource(R.mipmap.ic_launcher);

			mTimer = new Timer();
			setPlayerLane(0);
		}

		public void start() {
			ticks = 0;
			resume();
		}

		public void pause() {
			if (!running) return;
			running = false;
			ticker.cancel();
		}

		public void resume() {
			if (running) return;
			running = true;
			mTimer.scheduleAtFixedRate(ticker = new Ticker(), 0, 50);
		}

		public void stop() {
			pause();
			startActivity(new Intent(mContext, GameOverActivity.class));
			finish();
		}

		public void setPlayerLane(int x) {
			player.post(() -> {
				ConstraintLayout layout = findViewById(R.id.game_layout);
				int layoutWidth = layout.getWidth();
				int layoutHeight = layout.getHeight();

				int laneWidth = layoutWidth / NO_OF_LANES;

				player.setX(laneWidth * x + laneWidth / 2 - player.getWidth() / 2);
				player.setY(layoutHeight - player.getHeight());

			});
		}

		private void tick() {
			int timeRemaining = GAME_TICKS - ticks;
			String remString = getString(R.string.time_remain, timeRemaining / 20.0);
			TextView mTextView = findViewById(R.id.textView);
			mTextView.post(() -> mTextView.setText(remString));

			ticks++;
			if (ticks >= GAME_TICKS) stop();
		}

		private class Ticker extends TimerTask {
			@Override
			public void run() {
				tick();
			}
		}

	}

	private class SensorListener implements SensorEventListener {

		private static final float THRESHOLD = 3.0f;

		@Override
		public void onSensorChanged(SensorEvent sensorEvent) {
			float acc = sensorEvent.values[0];
			if(acc < -THRESHOLD)
				mGameRound.setPlayerLane(2);
			else if(acc > THRESHOLD)
				mGameRound.setPlayerLane(0);
			else
				mGameRound.setPlayerLane(1);
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int i) {

		}

		void startSensor() {
			mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME);
		}

		void stopSensor() {
			mSensorManager.unregisterListener(this);
		}

	}

}
