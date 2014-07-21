package uk.co.deanwild.openflappybird.game;

import android.app.Activity;
import android.util.DisplayMetrics;

public class ScreenSizeHelper {

	public static float calculateScreenWidth(Activity context, float windowHeight){		
				DisplayMetrics dm = new DisplayMetrics();
				context.getWindowManager().getDefaultDisplay().getMetrics(dm);
				final int realHeight = dm.heightPixels;
				final int realWidth = dm.widthPixels;
				float ratio = (float)realWidth / (float)realHeight;
				return windowHeight * ratio;
	}
}
