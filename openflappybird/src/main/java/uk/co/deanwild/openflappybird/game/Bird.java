package uk.co.deanwild.openflappybird.game;

import java.io.IOException;

import org.andengine.audio.sound.Sound;
import org.andengine.audio.sound.SoundFactory;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
import org.andengine.opengl.texture.atlas.buildable.builder.BlackPawnTextureAtlasBuilder;
import org.andengine.opengl.texture.atlas.buildable.builder.ITextureAtlasBuilder.TextureAtlasBuilderException;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.debug.Debug;

import android.app.Activity;


public class Bird {

	public static final float BITMAP_WIDTH = 1047f;
	public static final float BITMAP_HEIGHT = 903f;
	
	public static final float BIRD_WIDTH = 55.8f;
	public static final float BIRD_HEIGHT = 40f;
	
	protected static final float MAX_DROP_SPEED = 12.0f;
	protected static final float GRAVITY = 0.04f;
	protected static final float FLAP_POWER = 6f;

	protected static final float BIRD_MAX_FLAP_ANGLE = -20;
	protected static final float BIRD_MAX_DROP_ANGLE = 90;
	protected static final float FLAP_ANGLE_DRAG = 4.0f;
	protected static final float BIRD_FLAP_ANGLE_POWER = 15.0f;

	private AnimatedSprite mSprite;	

	protected float mAcceleration = GRAVITY;
	protected float mVerticalSpeed;	
	protected float mCurrentBirdAngle = BIRD_MAX_FLAP_ANGLE;


	//bird
	private static BuildableBitmapTextureAtlas mBirdBitmapTextureAtlas;
	private static TiledTextureRegion mBirdTextureRegion;
	
	// sounds
	private static Sound mJumpSound;	

	public static void onCreateResources(SimpleBaseGameActivity activity){
		// bird
		mBirdBitmapTextureAtlas = new BuildableBitmapTextureAtlas(activity.getTextureManager(), (int)BITMAP_WIDTH, (int)BITMAP_HEIGHT, TextureOptions.NEAREST);
		mBirdTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mBirdBitmapTextureAtlas, activity, "birdmap.png", 3, 3);
		try {
			mBirdBitmapTextureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 0, 0));
			mBirdBitmapTextureAtlas.load();
		} catch (TextureAtlasBuilderException e) {
			e.printStackTrace();
		}
		
		try {
			mJumpSound = SoundFactory.createSoundFromAsset(activity.getSoundManager(), activity, "jump.ogg");			
		} catch (final IOException e) {
			Debug.e(e);
		}

	}

	
	private float mBirdYOffset, mBirdXOffset;
	
	public Bird(float birdXOffset, float birdYOffset, VertexBufferObjectManager mVertexBufferObjectManager, Scene mScene) {

		this.mBirdXOffset = birdXOffset;
		this.mBirdYOffset = birdYOffset;		
		
		mSprite = new AnimatedSprite(mBirdXOffset, mBirdYOffset, 55.8f, 40, mBirdTextureRegion, mVertexBufferObjectManager);
		mSprite.animate(25);
		mSprite.setZIndex(2);
		mScene.attachChild(mSprite);
		
	}
	
	public void restart(){
		mSprite.animate(25);
		mSprite.setY(mBirdYOffset);
		mSprite.setX(mBirdXOffset);
		mCurrentBirdAngle = 0;
		mSprite.setRotation(mCurrentBirdAngle);
	}

	public float move(){

		float newY = mSprite.getY() + mVerticalSpeed; // calculate the birds new height based on the current vertical speed
		newY = Math.max(newY, 0); // don't allow through the ceiling
		newY = Math.min(newY, MainActivity.FLOOR_BOUND); // don't allow through the floor
		mSprite.setY(newY); //apply the new position

		// now calculate the new speed
		mAcceleration += GRAVITY; // always applying gravity to current acceleration
		mVerticalSpeed += mAcceleration; // always applying the current acceleration tp the current speed
		mVerticalSpeed = Math.min(mVerticalSpeed, MAX_DROP_SPEED); // but capping it to a terminal velocity (science bitch)

		if(mVerticalSpeed <= (FLAP_POWER)){
			mCurrentBirdAngle -= BIRD_FLAP_ANGLE_POWER;						
		}else{
			mCurrentBirdAngle += FLAP_ANGLE_DRAG;
		}

		mCurrentBirdAngle = Math.max(mCurrentBirdAngle, BIRD_MAX_FLAP_ANGLE);
		mCurrentBirdAngle = Math.min(mCurrentBirdAngle, BIRD_MAX_DROP_ANGLE);

		// now apply bird angle based on current speed
		mSprite.setRotation(mCurrentBirdAngle);

		return newY;
	}

	public void flap(){
		mVerticalSpeed = (-FLAP_POWER);
		mAcceleration = 0;
		mJumpSound.play();
	}	
	
	// hover stuff	
	private static float WRAPAROUND_POINT = (float) (2 * Math.PI);
	
	private float mHoverStep = 0;
	
	public void hover(){
		mHoverStep+=0.13f;	
		if(mHoverStep > WRAPAROUND_POINT) mHoverStep = 0;
		
		float newY = mBirdYOffset + ((float) (7 * Math.sin(mHoverStep)));		
		mSprite.setY(newY);			
		
	}

	public AnimatedSprite getSprite() {
		return mSprite;
	}

}
