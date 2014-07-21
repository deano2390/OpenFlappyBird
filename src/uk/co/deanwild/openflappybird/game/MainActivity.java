package uk.co.deanwild.openflappybird.game;

import java.util.ArrayList;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.ParallaxBackground;
import org.andengine.input.touch.TouchEvent;
import org.andengine.ui.activity.SimpleBaseGameActivity;

public class MainActivity extends SimpleBaseGameActivity {

	public static float CAMERA_WIDTH = 485; // this is not final because we dynamically set it at runtime based on the device aspect ratio
	public static final float CAMERA_HEIGHT = 800;
	private static final float SCROLL_SPEED = 4.5f;	// game speed
	public static final float FLOOR_BOUND = 601; // 
	protected static final int PIPE_SPAWN_INTERVAL = 100; // distance between pipe obstacles

	// game states
	protected static final int STATE_START = 0;
	protected static final int STATE_READY = 1;
	protected static final int STATE_PLAYING = 2;
	protected static final int STATE_DYING = 3;
	protected static final int STATE_DEAD = 4;

	private int GAME_STATE = STATE_READY;		

	// objects
	private TimerHandler mTimer;
	private SceneManager mSceneManager;
	private ResourceManager mResourceManager;	
	private Scene mScene;
	private Camera mCamera;

	// sprites
	private ParallaxBackground mBackground;
	private ArrayList<PipePair> pipes = new ArrayList<PipePair>();

	// game variables
	private int mScore = 0;
	protected float mCurrentWorldPosition;
	private float mBirdXOffset;
	
	@Override
	public EngineOptions onCreateEngineOptions() {

		CAMERA_WIDTH = ScreenSizeHelper.calculateScreenWidth(this, CAMERA_HEIGHT);

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
				mSceneManager.mBird.hover();		
				
				if(!mResourceManager.mMusic.isPlaying()){
					mResourceManager.mMusic.play();
				}
			}

			private void die(){
				float newY = mSceneManager.mBird.move(); // get the bird to update itself			
				if(newY >= FLOOR_BOUND) dead();
			}

			private void play(){

				mCurrentWorldPosition -= SCROLL_SPEED;				
				float newY = mSceneManager.mBird.move(); // get the bird to update itself			
				if(newY >= FLOOR_BOUND) gameOver(); // check if it game over from twatting the floor		

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
						if(pipe.collidesWith(mSceneManager.mBird.getSprite())){
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
		mResourceManager = new ResourceManager(this);
		mResourceManager.createResources();
	}

	@Override
	protected Scene onCreateScene() {				

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

		mSceneManager = new SceneManager(this, mResourceManager, mBackground);
		mScene = mSceneManager.createScene();	

		mScene.setOnSceneTouchListener(new IOnSceneTouchListener() {

			@Override
			public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
				if(pSceneTouchEvent.isActionDown()){

					switch(GAME_STATE){

					case STATE_READY:
						startPlaying();
						break;

					case STATE_PLAYING:
						mSceneManager.mBird.flap();
						break;

					case STATE_DEAD:
						//restartGame();
						break;	
					}								
				}
				return false;
			}
		});		
		
		updateScore();

		return mScene;
	}

	private void score(){
		mScore++;
		mResourceManager.mScoreSound.play();
		updateScore();
	}

	private void updateScore(){

		if(GAME_STATE == STATE_READY){
			mSceneManager.displayBestScore(ScoreManager.GetBestScore(this));
		}else{
			mSceneManager.displayCurrentScore(mScore);
		}		
	}
	
	// STATE SWITCHES

	private void startPlaying(){
		
		GAME_STATE = STATE_PLAYING;	
		
		mResourceManager.mMusic.pause();
		mResourceManager.mMusic.seekTo(0);
		mScene.detachChild(mSceneManager.mGetReadyText);
		mScene.detachChild(mSceneManager.mInstructionsSprite);
		mScene.detachChild(mSceneManager.mCopyText);
		updateScore();
		mSceneManager.mBird.flap();
	}

	private void gameOver(){
		
		GAME_STATE = STATE_DYING;
		
		mResourceManager.mDieSound.play();
		mScene.attachChild(mSceneManager.mYouSuckText);
		mSceneManager.mBird.getSprite().stopAnimation();		
		ScoreManager.SetBestScore(this, mScore);	
	}

	private void dead(){

		GAME_STATE = STATE_DEAD;	

		mTimer = new TimerHandler(1.6f, false, new ITimerCallback() {
			@Override
			public void onTimePassed(final TimerHandler pTimerHandler) {
				mScene.detachChild(mSceneManager.mYouSuckText);
				restartGame();
				mScene.unregisterUpdateHandler(mTimer);
			}
		});

		mScene.registerUpdateHandler(mTimer);
	}

	private void restartGame(){
		GAME_STATE = STATE_READY;
		mResourceManager.mMusic.resume();
		mSceneManager.mBird.restart();
		mScore = 0;
		updateScore();

		for (int i = 0; i<pipes.size(); i++){
			PipePair pipe = pipes.get(i);
			pipe.destroy();			
		}		
		pipes.clear();

		mScene.attachChild(mSceneManager.mGetReadyText);
		mScene.attachChild(mSceneManager.mInstructionsSprite);
		mScene.attachChild(mSceneManager.mCopyText);		
	}

	@Override
	public final void onPause() {
		super.onPause();
		mResourceManager.mMusic.pause();		
	}

}