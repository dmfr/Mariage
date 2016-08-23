package za.dams.mariage ;

import android.app.Activity;
import android.os.Bundle;

public class VideoActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.video_activity);
    if (null == savedInstanceState) {
      getFragmentManager().beginTransaction()
              .replace(R.id.container, VideoFragment.newInstance())
              .commit();
    }
  }

}
