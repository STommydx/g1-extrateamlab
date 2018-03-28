package com.company.g1.g1extrateamlab;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	public void startGame(View view) {
		startActivity(new Intent(this, GameActivity.class));
	}

	public void story(View view){
		startActivity(new Intent(this, StoryBehind.class));
	}

  public void startAboutUs(View view) {
	    startActivity(new Intent(this, AboutUsActivity.class));
	}

}
