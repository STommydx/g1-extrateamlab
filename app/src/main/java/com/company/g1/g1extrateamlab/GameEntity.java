package com.company.g1.g1extrateamlab;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.AppCompatImageView;
import android.view.View;

public class GameEntity extends AppCompatImageView {

	private float velocityX, velocityY;
	private float accelerationX, accelerationY;

	public GameEntity(Context context) {
		super(context);

		velocityX = 0.0f;
		velocityY = 0.0f;
		accelerationX = 0.0f;
		accelerationY = 0.0f;
	}

	public boolean isCollided(View view) {
		Rect mRect = new Rect();
		Rect oRect = new Rect();
		getHitRect(mRect);
		view.getHitRect(oRect);
		return mRect.intersect(oRect);
	}

	public void updatePosition() {
		velocityX += accelerationX;
		velocityY += accelerationY;
		setX(getX() + velocityX);
		setY(getY() + velocityY);
	}

	public void setVelocityX(float velocityX) {
		this.velocityX = velocityX;
	}

	public void setVelocityY(float velocityY) {
		this.velocityY = velocityY;
	}

	public void setAccelerationX(float accelerationX) {
		this.accelerationX = accelerationX;
	}

	public void setAccelerationY(float accelerationY) {
		this.accelerationY = accelerationY;
	}

}
