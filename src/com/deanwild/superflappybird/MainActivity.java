package com.deanwild.superflappybird;

import java.io.IOException;
import java.util.ArrayList;

import org.andengine.audio.music.Music;
import org.andengine.audio.music.MusicFactory;
import org.andengine.audio.sound.Sound;
import org.andengine.audio.sound.SoundFactory;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.ParallaxBackground;
import org.andengine.entity.scene.background.ParallaxBackground.ParallaxEntity;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.font.StrokeFont;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.HorizontalAlign;
import org.andengine.util.debug.Debug;

import uk.co.deanwild.floppyjoe.R;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;

public class MainActivity extends SimpleBaseGameActivity {

	/**
	 * Set the camera height and width
	 */
	public static float CAMERA_WIDTH = 485;
	public static final float CAMERA_HEIGHT = 800;

	private static final float SCROLL_SPEED = 4.5f;	

	//private static final float SCROLL_SPEED = 0.0f;
	//protected static final float GRAVITY = 0.00f;	

	public static final float FLOOR_BOUND = 601;

	protected static final int PIPE_SPAWN_INTERVAL = 100;

	/**
	 * Constants for assets
	 */

	private Camera mCamera;

	/**
	 * Texture Region for the game graphics
	 */
	//background	
	private BitmapTextureAtlas mBackgroundBitmapTextureAtlas;
	private ITextureRegion mBackgroundTextureRegion;	

	private TextureRegion mInstructionsTexture;

	/**
	 * Game States
	 */
	protected static final int STATE_START = 0;
	protected static final int STATE_READY = 1;
	protected static final int STATE_PLAYING = 2;
	protected static final int STATE_DYING = 3;
	protected static final int STATE_DEAD = 4;


	private int GAME_STATE = STATE_READY;

	/**
	 * Game Variables
	 */

	private int mScore = 0;

	// objects
	private static MainActivity instance;
	private Scene mScene;
	private VertexBufferObjectManager vbo;

	// sprites

	private ParallaxBackground mBackground;
	private ArrayList<PipePair> pipes = new ArrayList<PipePair>();
	private Bird mBird;

	// physics variables
	protected float mCurrentWorldPosition;
	private float mBirdXOffset;

	// fonts
	private Font mScoreFont;	
	private Font mGetReadyFont;	
	private Font mCopyFont;

	// text objects
	private Text mScoreText;
	private Text mGetReadyText;
	private Sprite mInstructionsSprite;
	private TimerHandler mTimer;
	private Text mCopyText;
	private StrokeFont mYouSuckFont;
	private Text mYouSuckText;


	// sounds
	private Sound mScoreSound;
	private Sound mDieSound;	
	private Music mMusic;


	@Override
	public EngineOptions onCreateEngineOptions() {

		// figure out display size
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		final int height = dm.heightPixels;
		final int width = dm.widthPixels;
		float ratio = (float)width / (float)height;
		CAMERA_WIDTH = CAMERA_HEIGHT * ratio;

		instance = this;
		mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT){

			private int mPipeSpawnCounter;

			@Override
			public void onUpdate(float pSecondsElapsed) {

				switch(GAME_STATE){

				case STATE_READY:
					ready();
					break;

				case STATE_PLAYING:
					play();
					break;

				case STATE_DYING:
					die();
					break;
				}

				super.onUpdate(pSecondsElapsed);
			}

			private void ready(){
				mCurrentWorldPosition -= SCROLL_SPEED;	
				mBird.hover();				
				if(!mMusic.isPlaying()){
					mMusic.play();
				}
			}

			private void die(){
				float newY = mBird.move(); // get the bird to update itself			
				if(newY >= FLOOR_BOUND) dead();
			}

			private void play(){

				mCurrentWorldPosition -= SCROLL_SPEED;				
				float newY = mBird.move(); // get the bird to update itself			
				if(newY >= FLOOR_BOUND) gameOver(); // check if it game over form twatting the floor		

				// now create pipes
				mPipeSpawnCounter++;

				if(mPipeSpawnCounter > PIPE_SPAWN_INTERVAL){
					mPipeSpawnCounter = 0;
					spawnNewPipe();						
				}

				// now render the pipes
				for (int i = 0; i<pipes.size(); i++){
					PipePair pipe = pipes.get(i);
					if(pipe.isOnScreen()){
						pipe.move(SCROLL_SPEED);
						if(pipe.collidesWith(mBird.getSprite())){
							gameOver();
						}

						if(pipe.isCleared(mBirdXOffset)){							
							score();
						}
					}else{
						pipe.destroy();
						pipes.remove(pipe);							
					}					
				}	
			}
		};

		EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, 
				new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), mCamera);

		engineOptions.getAudioOptions().setNeedsSound(true);	
		engineOptions.getAudioOptions().setNeedsMusic(true);

		return engineOptions;				
	}	

	protected void spawnNewPipe() {
		int Min = 150;
		int Max = 450;
		int spawn = Min + (int)(Math.random() * ((Max - Min) + 1));
		PipePair newPipes = new PipePair(spawn, this.getVertexBufferObjectManager(), mScene);
		pipes.add(newPipes);		
	}

	@Override
	protected void onCreateResources() {

		SoundFactory.setAssetBasePath("sound/");
		MusicFactory.setAssetBasePath("sound/");
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("img/");		

		// background
		mBackgroundBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 718, 1184, 
				TextureOptions.NEAREST_PREMULTIPLYALPHA);
		mBackgroundTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(mBackgroundBitmapTextureAtlas, this.getAssets(), "background480.png", 0, 0);

		mBackgroundBitmapTextureAtlas.load();		

		// instructions img
		BitmapTextureAtlas instructionsTextureAtlas = new BitmapTextureAtlas(getTextureManager(), 285, 245, TextureOptions.BILINEAR);
		mInstructionsTexture = BitmapTextureAtlasTextureRegionFactory.createFromAsset(instructionsTextureAtlas, this, "instructions.png", 0, 0);
		instructionsTextureAtlas.load();

		PipePair.onCreateResources(this); // let it sort its own resources out
		Bird.onCreateResources(this);

		Typeface typeFace = Typeface.createFromAsset(getAssets(), "flappy.ttf");

		// score board		
		final ITexture scoreFontTexture = new BitmapTextureAtlas(this.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
		mScoreFont = new StrokeFont(this.getFontManager(), scoreFontTexture, typeFace, 60, true, Color.WHITE, 2, Color.BLACK);
		mScoreFont.load();

		// get ready text	
		final ITexture getReadyFontTexture = new BitmapTextureAtlas(this.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
		mGetReadyFont = new StrokeFont(this.getFontManager(), getReadyFontTexture, typeFace, 60, true, Color.WHITE, 2, Color.BLACK);
		mGetReadyFont.load();


		// (c) text
		final ITexture copyFontTexture = new BitmapTextureAtlas(this.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
		mCopyFont = new StrokeFont(this.getFontManager(), copyFontTexture, typeFace, 20, true, Color.WHITE, 2, Color.BLACK);
		mCopyFont.load();

		// (c) you suck text
		final ITexture youSuckTexture = new BitmapTextureAtlas(this.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
		mYouSuckFont = new StrokeFont(this.getFontManager(), youSuckTexture, typeFace, 80, true, Color.WHITE, 2, Color.BLACK);
		mYouSuckFont.load();	

		// sounds
		try {			
			mScoreSound = SoundFactory.createSoundFromAsset(mEngine.getSoundManager(), this, "score.ogg");
			mDieSound = SoundFactory.createSoundFromAsset(mEngine.getSoundManager(), this, "gameover.ogg");			
		} catch (final IOException e) {
			Debug.e(e);
		}	

		// music

		try {
			mMusic = MusicFactory.createMusicFromAsset(mEngine.getMusicManager(), this, "song.ogg");
			mMusic.setLooping(true);
		} catch (final IOException e) {
			Debug.e("Error", e);
		}
	}

	@Override
	protected Scene onCreateScene() {

		this.mScene = new Scene();
		this.vbo = this.getVertexBufferObjectManager();

		Sprite backgroundSprite = new Sprite(0, 0 , this.mBackgroundTextureRegion, this.vbo);
		mBackground = new ParallaxBackground(82/255f, 190/255f, 206/255f){

			float prevX = 0;
			float parallaxValueOffset = 0;

			@Override
			public void onUpdate(float pSecondsElapsed) {

				switch(GAME_STATE){

				case STATE_READY:
				case STATE_PLAYING:
					final float cameraCurrentX = mCurrentWorldPosition;//mCamera.getCenterX();

					if (prevX != cameraCurrentX) {

						parallaxValueOffset +=  cameraCurrentX - prevX;
						this.setParallaxValue(parallaxValueOffset);
						prevX = cameraCurrentX;
					}
					break;
				}		

				super.onUpdate(pSecondsElapsed);
			}

		};

		mBackground.attachParallaxEntity(new ParallaxEntity(1, backgroundSprite));

		this.mScene.setBackground(mBackground);
		this.mScene.setBackgroundEnabled(true);

		mScene.setOnSceneTouchListener(new IOnSceneTouchListener() {

			@Override
			public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
				if(pSceneTouchEvent.isActionDown()){

					switch(GAME_STATE){

					case STATE_READY:
						startPlaying();
						break;

					case STATE_PLAYING:
						mBird.flap();
						break;

					case STATE_DEAD:
						//restartGame();
						break;	
					}								
				}
				return false;
			}
		});

		// bird		
		mBirdXOffset = (CAMERA_WIDTH / 4) - (Bird.BIRD_WIDTH / 4);
		float birdYOffset = (CAMERA_HEIGHT / 2) - (Bird.BIRD_HEIGHT / 4);
		mBird = new Bird(mBirdXOffset, birdYOffset, getVertexBufferObjectManager(), mScene);

		//score
		mScoreText = new Text(0, 60, mScoreFont, "        ", new TextOptions(HorizontalAlign.CENTER), getVertexBufferObjectManager());
		mScoreText.setZIndex(3);
		mScene.attachChild(mScoreText);
		updateScore();

		// get ready text
		mGetReadyText = new Text(0, 220, mGetReadyFont, "Get Ready!", new TextOptions(HorizontalAlign.CENTER), getVertexBufferObjectManager());
		mGetReadyText.setZIndex(3);
		mScene.attachChild(mGetReadyText);
		centerText(mGetReadyText);

		// instructions image
		mInstructionsSprite = new Sprite(0, 0, 200, 172, mInstructionsTexture, getVertexBufferObjectManager());
		mInstructionsSprite.setZIndex(3);
		mScene.attachChild(mInstructionsSprite);
		centerSprite(mInstructionsSprite);
		mInstructionsSprite.setY(mInstructionsSprite.getY() + 20);

		// copy text
		mCopyText = new Text(0, 750, mCopyFont, "(c) Dean Wild 2014", new TextOptions(HorizontalAlign.CENTER), getVertexBufferObjectManager());
		mCopyText.setZIndex(3);
		mScene.attachChild(mCopyText);
		centerText(mCopyText);


		// you suck text		
		mYouSuckText = new Text(0, CAMERA_HEIGHT / 2 - 100, mYouSuckFont, "You Suck!", new TextOptions(HorizontalAlign.CENTER), getVertexBufferObjectManager());
		mYouSuckText.setZIndex(3);		
		centerText(mYouSuckText);

		return mScene;
	}

	private void score(){
		mScore++;
		mScoreSound.play();
		updateScore();
	}

	private void updateScore(){

		if(GAME_STATE == STATE_READY){
			mScoreText.setText("Best - " + ScoreManager.GetBestScore(this));
			centerText(mScoreText);
		}else{
			mScoreText.setText("" + mScore);
			centerText(mScoreText);
		}		
	}

	private void centerText(Text text){
		text.setX((CAMERA_WIDTH / 2) - (text.getWidth() / 2));		
	}

	public static void centerSprite(Sprite sprite){
		sprite.setX((CAMERA_WIDTH / 2) - (sprite.getWidth() / 2));	
		sprite.setY((CAMERA_HEIGHT / 2) - (sprite.getHeight() / 2));	
	}

	// STATE SWITCHES

	private void startPlaying(){
		GAME_STATE = STATE_PLAYING;	
		mMusic.pause();
		mMusic.seekTo(0);
		mScene.detachChild(mGetReadyText);
		mScene.detachChild(mInstructionsSprite);
		mScene.detachChild(mCopyText);
		updateScore();
		mBird.flap();
	}

	private void gameOver(){
		GAME_STATE = STATE_DYING;
		mDieSound.play();
		mScene.attachChild(mYouSuckText);
		mBird.getSprite().stopAnimation();		
		ScoreManager.SetBestScore(this, mScore);	}

	private void dead(){
		GAME_STATE = STATE_DEAD;	

		mTimer = new TimerHandler(1.6f, false, new ITimerCallback() {
			@Override
			public void onTimePassed(final TimerHandler pTimerHandler) {
				mScene.detachChild(mYouSuckText);
				restartGame();
				mScene.unregisterUpdateHandler(mTimer);

			}
		});

		mScene.registerUpdateHandler(mTimer);
	}

	private void restartGame(){
		GAME_STATE = STATE_READY;
		mMusic.resume();
		mBird.restart();
		mScore = 0;
		updateScore();

		for (int i = 0; i<pipes.size(); i++){
			PipePair pipe = pipes.get(i);
			pipe.destroy();			
		}		
		pipes.clear();

		mScene.attachChild(mGetReadyText);
		mScene.attachChild(mInstructionsSprite);
		mScene.attachChild(mCopyText);		
	}

	public static MainActivity getInstance(){
		return instance;
	}

	@Override
	public final void onPause() {
		super.onPause();
		if(mMusic!=null){
			mMusic.pause();
		}
		
	}


}