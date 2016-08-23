package za.dams.mariage;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

public class PhotoActivity extends Activity implements PhotoCameraFragment.Callback, PhotoListFragment.Callback, PhotoImageDialog.Callback {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    this.getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME|ActionBar.DISPLAY_HOME_AS_UP);

    setContentView(R.layout.photo_activity);
  }

  @Override
  public void onBackPressed() {
      Log.d("DAMS", this + " onBackPressed");

      super.onBackPressed();

  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    Log.d("DAMS", this + " onOptionsItemSelected");
    switch( item.getItemId() ) {
      case android.R.id.home:
        this.finish();
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onPhotoSave(Uri photoUri) {
    Log.w("DAMS","PhotoAdd") ;
    FragmentManager ft = getFragmentManager() ;
    PhotoListFragment plf = (PhotoListFragment)ft.findFragmentById(R.id.photo_list_fragment) ;
    if( plf != null && plf.isAdded() ) {
      plf.addPhoto(photoUri);
      plf.clearText();
    }
  }

  @Override
  public void onPhotoTrigger() {
    Log.w("DAMS","PhotoTrigger") ;
    FragmentManager ft = getFragmentManager() ;
    PhotoCameraFragment pcf = (PhotoCameraFragment)ft.findFragmentById(R.id.photo_camera_fragment) ;
    if( pcf != null && pcf.isAdded() ) {
      pcf.takePicture();
    }
  }


  @Override
  public void onPhotoDelete(Uri photoUri) {
    Log.w("DAMS","PhotoDelete") ;
    FragmentManager ft = getFragmentManager() ;
    PhotoListFragment plf = (PhotoListFragment)ft.findFragmentById(R.id.photo_list_fragment) ;
    if( plf != null && plf.isAdded() ) {
      plf.deletePhoto(photoUri);
      plf.clearText();
    }
  }
}