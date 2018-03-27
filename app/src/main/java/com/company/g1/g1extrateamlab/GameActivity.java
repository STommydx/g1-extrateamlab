package com.company.g1.g1extrateamlab;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.Timer;
import java.util.TimerTask;

public class GameActivity extends AppCompatActivity {

	GameRound mGameRound = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);

		mGameRound = new GameRound(this);
		mGameRound.start();
	}


	public class GameRound {

		private static final int NO_OF_LANES = 3;

		private ImageView player;
		private TimerTask ticker;
		private Timer mTimer;

		public GameRound(Context context) {
			player = new ImageView(context);
			ConstraintLayout layout = findViewById(R.id.game_layout);

			layout.addView(player);
			player.setImageResource(R.mipmap.ic_launcher);

			ticker = new TimerTask() {
				@Override
				public void run() {
					tick();
				}
			};
			mTimer = new Timer();
			setPlayerLane(0);
		}

		public void start() {
			mTimer = new Timer();
			mTimer.scheduleAtFixedRate(ticker, 0, 50);
		}

		public void stop() {
			mTimer.cancel();
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

		}

	}

}
