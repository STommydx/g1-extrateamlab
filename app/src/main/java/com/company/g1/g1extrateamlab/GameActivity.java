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
import android.view.View;
import android.widget.FrameLayout;
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

	private ConstraintLayout gameLayout;
	private boolean isImmersive;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);

		setupSensor();
//
//		gameLayout = findViewById(R.id.game_layout);
//		setImmersiveUi(true);
//		gameLayout.setOnClickListener((view -> toggleImmersiveUi()));

		ProgressBar mProgressBar = findViewById(R.id.progressBar);
		mProgressBar.getProgressDrawable().setColorFilter(Color.GREEN, android.graphics.PorterDuff.Mode.SRC_IN);
		mProgressBar.setMax(100);
		mProgressBar.setProgress(100);


        gameLayout = findViewById(R.id.game_layout);
        setImmersiveUi(true);
        gameLayout.setOnClickListener((view -> toggleImmersiveUi()));
        mGameRound = new GameRound(this);
        mGameRound.start();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState){
        super.onPostCreate(savedInstanceState);
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
		mGameRound.pause();
	}

	@Override
	protected void onResume() {
		super.onResume();
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
        private int entityWidth;

		private List<GameEntity> enemyList;

		public GameRound(Context context) {
			mContext = context;
            player = new ImageView(context);


			gameLayout.post(()-> {
			    entityWidth = (int)(gameLayout.getWidth() / NO_OF_LANES * 0.9);

                gameLayout.addView(player);
                player.setImageResource(R.drawable.me);
                player.getLayoutParams().width = entityWidth;
            });


//			player.setScaleX(0.8f);
//			player.setScaleY(0.8f);

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
			mSensorListener.stopSensor();
			ticker.cancel();
		}

		public void resume() {
			if (running) return;
			running = true;
			mSensorListener.startSensor();
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

				player.setX(laneWidth * x + laneWidth / 2 - entityWidth / 2);
				player.setY(layoutHeight - entityWidth * 2);

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

				int lane = random.nextInt(NO_OF_LANES);


				GameEntity newEnemy = new GameEntity(mContext);
				newEnemy.setImageResource(R.drawable.gameenemy);

                gameLayout.post(()-> {
                    gameLayout.addView(newEnemy);
                    int layoutWidth = gameLayout.getWidth();
                    int laneWidth = layoutWidth / NO_OF_LANES;
                    final int dogWidth = (int)(laneWidth * 0.9);
                    newEnemy.getLayoutParams().width = dogWidth;
                    newEnemy.setX(laneWidth * lane + laneWidth / 2 - entityWidth / 2);
                });

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

	private void setImmersiveUi(boolean immersive) {
//		FrameLayout gameLayout = findViewById(R.id.game_layout);

		if(immersive) {
			gameLayout.setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE
							| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
							| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_FULLSCREEN
							| View.SYSTEM_UI_FLAG_IMMERSIVE);
		}
		else {
			gameLayout.setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE
							| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
		}
		isImmersive = !isImmersive;
	}

	private void toggleImmersiveUi() {
		if (isImmersive) {
			setImmersiveUi(false);
			mGameRound.pause();
		}
		else {
			setImmersiveUi(true);
			mGameRound.resume();
		}
	}
}
