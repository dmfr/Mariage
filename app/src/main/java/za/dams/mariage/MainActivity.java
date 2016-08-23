package za.dams.mariage;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.Button;

import java.io.File;

public class MainActivity extends Activity implements View.OnClickListener {

  View mButtonCamera ;
  View mButtonVideo ;
  boolean mAvailable ;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mButtonCamera = findViewById(R.id.button_camera) ;
    mButtonCamera.setOnClickListener(this);

    mButtonVideo = findViewById(R.id.button_video) ;
    mButtonVideo.setOnClickListener(this);

  }

  @Override
  protected void onResume() {
    super.onResume();
    mAvailable = true ;
  }

  private void goCamera() {
    Animation ranim = (Animation) AnimationUtils.loadAnimation(this, R.anim.rotate);

    ranim.setAnimationListener(new Animation.AnimationListener() {
      @Override
      public void onAnimationStart(Animation animation) {
      }

      @Override
      public void onAnimationEnd(Animation animation) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setClass(MainActivity.this, PhotoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        MainActivity.this.startActivity(intent);
      }

      @Override
      public void onAnimationRepeat(Animation animation) {

      }
    });
    mButtonCamera.startAnimation(ranim);
  }

  private void goVideo() {
    Animation ranim = (Animation) AnimationUtils.loadAnimation(this, R.anim.rotate);
    ranim.setAnimationListener(new Animation.AnimationListener() {
      @Override
      public void onAnimationStart(Animation animation) {
      }

      @Override
      public void onAnimationEnd(Animation animation) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setClass(MainActivity.this, VideoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        MainActivity.this.startActivity(intent);
      }

      @Override
      public void onAnimationRepeat(Animation animation) {

      }
    });
    mButtonVideo.startAnimation(ranim);
    Log.w("DAMS","goVideo");
  }

  @Override
  public void onClick(View v) {
    if( !mAvailable ) {
      return ;
    }
    mAvailable = false ;
    if( v == mButtonCamera ) {
      goCamera() ;
    }
    if( v == mButtonVideo ) {
      goVideo() ;
    }
  }
}
