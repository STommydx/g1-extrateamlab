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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
		private static final int GAME_TICKS = 1000;

		private ImageView player;
		private TimerTask ticker;
		private Timer mTimer;

		private int ticks;
		private int spawn;
		private boolean running = false;

		private Context mContext;
		private ConstraintLayout gameLayout;

		private List<GameEntity> enemyList;

		public GameRound(Context context) {
			mContext = context;

			player = new ImageView(context);
			gameLayout = findViewById(R.id.game_layout);

			gameLayout.addView(player);
			player.setImageResource(R.mipmap.ic_launcher);

			mTimer = new Timer();
			setPlayerLane(0);

			enemyList = new ArrayList<>();
		}

		public void start() {
			ticks = 0;
			spawn = 1;

			for(GameEntity gameEntity: enemyList) {
				gameLayout.removeView(gameEntity);
			}
			enemyList.clear();

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
			mTimer.scheduleAtFixedRate(ticker = new Ticker(), 0, 20);
		}

		public void stop() {
			pause();
			startActivity(new Intent(mContext, GameOverActivity.class));
			finish();
		}

		public void setPlayerLane(int x) {
			player.post(() -> {
				int layoutWidth = gameLayout.getWidth();
				int layoutHeight = gameLayout.getHeight();

				int laneWidth = layoutWidth / NO_OF_LANES;

				player.setX(laneWidth * x + laneWidth / 2 - player.getWidth() / 2);
				player.setY(layoutHeight - player.getHeight());

			});
		}

		private void tick() {

			// Update timer
			int timeRemaining = GAME_TICKS - ticks;
			String remString = getString(R.string.time_remain, timeRemaining / 50.0);
			TextView mTextView = findViewById(R.id.textView);
			mTextView.post(() -> mTextView.setText(remString));

			// Update position
			for (GameEntity enemy: enemyList) {
				enemy.post(() -> enemy.updatePosition());
			}

			// Check for collision
			for (GameEntity enemy: enemyList) {
				if (enemy.isCollided(player)) {
					ProgressBar progressBar = findViewById(R.id.progressBar);
					final int newHP = progressBar.getProgress() - 16;
					if(newHP < 0) stop();
					progressBar.post(() -> progressBar.setProgress(newHP));
					gameLayout.post(() -> gameLayout.removeView(enemy));
				}
			}

			/*
			// Remove entities out of bound
			for (GameEntity enemy: enemyList) {
				if (!enemy.isCollided(gameLayout)) {
					gameLayout.post(() -> gameLayout.removeView(enemy));
				}
			}
			*/

			// new enemy after certain interval

			if (--spawn == 0) {
				Random random = new Random();

				int layoutWidth = gameLayout.getWidth();
				int laneWidth = layoutWidth / NO_OF_LANES;

				int lane = random.nextInt(NO_OF_LANES);

				GameEntity newEnemy = new GameEntity(mContext);
				newEnemy.setImageResource(R.mipmap.ic_launcher_round);
				newEnemy.setX(laneWidth * lane + laneWidth / 2 - player.getWidth() / 2);
				newEnemy.setY(0);
				gameLayout.post(() -> gameLayout.addView(newEnemy));
				newEnemy.setAccelerationY(0.0002f * ticks);
				newEnemy.setVelocityY(8.0f);
				enemyList.add(newEnemy);

				spawn = random.nextInt((GAME_TICKS - ticks + 1500) / 20) + 1;
			}

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
