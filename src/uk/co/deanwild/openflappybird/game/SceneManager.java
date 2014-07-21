package uk.co.deanwild.openflappybird.game;

import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.ParallaxBackground;
import org.andengine.entity.scene.background.ParallaxBackground.ParallaxEntity;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.HorizontalAlign;

public class SceneManager {

	private SimpleBaseGameActivity mContext;
	private ResourceManager mResourceManager;
	private ParallaxBackground mParallaxBackground;

	// text objects
	Text mScoreText;
	Text mGetReadyText;
	Sprite mInstructionsSprite;	
	Text mCopyText;	
	Text mYouSuckText;
	
	Bird mBird;

	public SceneManager(SimpleBaseGameActivity context, ResourceManager resourceManager, ParallaxBackground parallaxBackground){
		this.mContext = context;	
		this.mResourceManager = resourceManager;
		this.mParallaxBackground = parallaxBackground;
	}	
		
	public Scene createScene(){

		Scene mScene = new Scene();
		VertexBufferObjectManager vbo = mContext.getVertexBufferObjectManager();

		Sprite backgroundSprite = new Sprite(0, 0 , mResourceManager.mBackgroundTextureRegion, vbo);	
		mParallaxBackground.attachParallaxEntity(new ParallaxEntity(1, backgroundSprite));

		mScene.setBackground(mParallaxBackground);
		mScene.setBackgroundEnabled(true);


		// bird		
		float birdStartXOffset = (MainActivity.CAMERA_WIDTH / 4) - (Bird.BIRD_WIDTH / 4);
		float birdYOffset = (MainActivity.CAMERA_HEIGHT / 2) - (Bird.BIRD_HEIGHT / 4);
		mBird = new Bird(birdStartXOffset, birdYOffset, mContext.getVertexBufferObjectManager(), mScene);

		//score
		mScoreText = new Text(0, 60, mResourceManager.mScoreFont, "        ", new TextOptions(HorizontalAlign.CENTER), mContext.getVertexBufferObjectManager());
		mScoreText.setZIndex(3);
		mScene.attachChild(mScoreText);		

		// get ready text
		mGetReadyText = new Text(0, 220, mResourceManager.mGetReadyFont, "Get Ready!", new TextOptions(HorizontalAlign.CENTER), mContext.getVertexBufferObjectManager());
		mGetReadyText.setZIndex(3);
		mScene.attachChild(mGetReadyText);
		centerText(mGetReadyText);

		// instructions image
		mInstructionsSprite = new Sprite(0, 0, 200, 172, mResourceManager.mInstructionsTexture, mContext.getVertexBufferObjectManager());
		mInstructionsSprite.setZIndex(3);
		mScene.attachChild(mInstructionsSprite);
		centerSprite(mInstructionsSprite);
		mInstructionsSprite.setY(mInstructionsSprite.getY() + 20);

		// copy text
		mCopyText = new Text(0, 750, mResourceManager.mCopyFont, "(c) Dean Wild 2014", new TextOptions(HorizontalAlign.CENTER), mContext.getVertexBufferObjectManager());
		mCopyText.setZIndex(3);
		mScene.attachChild(mCopyText);
		centerText(mCopyText);


		// you suck text		
		mYouSuckText = new Text(0, MainActivity.CAMERA_HEIGHT / 2 - 100, mResourceManager.mYouSuckFont, "You Suck!", new TextOptions(HorizontalAlign.CENTER), mContext.getVertexBufferObjectManager());
		mYouSuckText.setZIndex(3);		
		centerText(mYouSuckText);

		return mScene;
	}
	
	public static void centerSprite(Sprite sprite){
		sprite.setX((MainActivity.CAMERA_WIDTH / 2) - (sprite.getWidth() / 2));	
		sprite.setY((MainActivity.CAMERA_HEIGHT / 2) - (sprite.getHeight() / 2));	
	}
	
	public void displayCurrentScore(int score){		
			mScoreText.setText("" + score);
			centerText(mScoreText);				
	}
	
	public void displayBestScore(int score){
		mScoreText.setText("Best - " + score);
		centerText(mScoreText);
	}

	private void centerText(Text text){
		text.setX((MainActivity.CAMERA_WIDTH / 2) - (text.getWidth() / 2));		
	}
}
