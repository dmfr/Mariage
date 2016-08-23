package za.dams.mariage;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by root on 7/14/16.
 */
public class PhotoCameraFragment extends Fragment {
  public interface Callback {
    public void onPhotoSave(Uri photoUri);
  }

  Callback mCallback ;

  ViewGroup mCameraFrame ;
  Camera mCamera;

  /** A safe way to get an instance of the Camera object. */
  private Camera openFrontFacingCameraGingerbread() {
    int cameraCount = 0;
    Camera cam = null;
    Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
    cameraCount = Camera.getNumberOfCameras();
    for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
      Camera.getCameraInfo(camIdx, cameraInfo);
      if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
        try {
          cam = Camera.open(camIdx);
        } catch (RuntimeException e) {
          Log.e("DAMS", "Camera failed to open: " + e.getLocalizedMessage());
        }
      }
    }

    return cam;
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    Activity activity = getActivity() ;
    try {
      mCallback = (PhotoCameraFragment.Callback) activity;
    } catch( ClassCastException e ) {
      throw new ClassCastException(activity.toString()
              + " must implement OnHeadlineSelectedListener");

    }
  }
  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
      mCallback = (PhotoCameraFragment.Callback) activity;
    } catch( ClassCastException e ) {
      throw new ClassCastException(activity.toString()
              + " must implement OnHeadlineSelectedListener");

    }
  }

  public static Camera getCameraInstance(){
    Camera c = null;
    try {
      c = Camera.open(); // attempt to get a Camera instance
    }
    catch (Exception e){
      // Camera is not available (in use or does not exist)
    }
    return c; // returns null if camera is unavailable
  }


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    mCameraFrame = (ViewGroup)inflater.inflate(R.layout.photo_camera_frame, container, false) ;
    return mCameraFrame ;
  }
  @Override
  public void onResume() {
    super.onResume();

    // Use mCurrentCamera to select the camera desired to safely restore
    // the fragment after the camera has been changed
    mCamera = openFrontFacingCameraGingerbread();
    if( mCamera == null ) {
      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
      builder.setTitle("Error")
              .setMessage("Cannot open device camera")
              .setCancelable(false)
              .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                  dialog.cancel();
                  getFragmentManager().popBackStack() ;
                }
              });
      AlertDialog alert = builder.create();
      alert.show();

      return ;
    }

    Camera.Parameters mCamParams = mCamera.getParameters() ;

    //Query pictures sizes
    List<Camera.Size> pictureSizes = mCamParams.getSupportedPictureSizes() ;
    Camera.Size optimalSize = findBiggestSize(pictureSizes);

    mCamParams.setPictureSize(optimalSize.width, optimalSize.height) ;
    mCamParams.setJpegQuality(90) ;
    //mCamParams.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO) ;
    mCamera.setParameters(mCamParams) ;


    CameraPreview cameraPreview = new CameraPreview(this.getActivity(),mCamera) ;
    mCameraFrame.removeAllViews() ;
    mCameraFrame.addView(cameraPreview) ;
  }
  @Override
  public void onPause() {
    super.onPause();

    mCameraFrame.removeAllViews() ;

    // Because the Camera object is a shared resource, it's very
    // important to release it when the activity is paused.
    if (mCamera != null) {
      mCamera.release();
      mCamera = null;
    }
  }

  private static Camera.Size findOptimalSize(List<Camera.Size> sizes, int w, int h) {
    final double ASPECT_TOLERANCE = 0.1;
    double targetRatio = (double) w / h;
    if (sizes == null)
      return null;

    Camera.Size optimalSize = null;
    double minDiff = Double.MAX_VALUE;

    int targetHeight = h;

    // Try to find an size match aspect ratio and size
    for (Camera.Size size : sizes) {
      double ratio = (double) size.width / size.height;
      if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
        continue;
      if (Math.abs(size.height - targetHeight) < minDiff) {
        optimalSize = size;
        minDiff = Math.abs(size.height - targetHeight);
      }
    }

    // Cannot find the one match the aspect ratio, ignore the requirement
    if (optimalSize == null) {
      minDiff = Double.MAX_VALUE;
      for (Camera.Size size : sizes) {
        if (Math.abs(size.height - targetHeight) < minDiff) {
          optimalSize = size;
          minDiff = Math.abs(size.height - targetHeight);
        }
      }
    }
    return optimalSize;
  }
  private static Camera.Size findBiggestSize(List<Camera.Size> sizes) {
    if (sizes == null)
      return null;

    Camera.Size targetSize = null ;
    for( Camera.Size cs : sizes ) {
      if( targetSize == null ) {
        targetSize = cs ;
        continue ;
      }
      if( cs.height+cs.width > targetSize.width+targetSize.height ) {
        targetSize = cs ;
      }
    }

    return targetSize;
  }


  private class CameraPreview extends ViewGroup implements SurfaceHolder.Callback {
    private SurfaceView mSurfaceView;
    private SurfaceHolder mHolder;

    private Camera mCamera;
    List<Camera.Size> mSupportedPreviewSizes;

    Camera.Size mPreviewSize;

    public CameraPreview(Context context, Camera camera) {
      super(context);

      mCamera = camera;
      mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();

      mSurfaceView = new SurfaceView(context);
      addView(mSurfaceView);

      // Install a SurfaceHolder.Callback so we get notified when the
      // underlying surface is created and destroyed.
      mHolder = mSurfaceView.getHolder();
      mHolder.addCallback(this);
      // deprecated setting, but required on Android versions prior to 3.0
      mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
      //Log.w(TAG,"surfaceCreated ");
      // The Surface has been created, now tell the camera where to draw the preview.
      try {
        mCamera.setPreviewDisplay(holder);
      } catch (IOException e) {
        Log.d("DAMS", "Error setting camera preview: " + e.getMessage());
      }
      mCamera.startPreview() ;
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
      // empty. Take care of releasing the Camera preview in your activity.
      mCamera.stopPreview() ;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
	    	/* Note :
	    	 * La taille de la surface est imposée par onLayout,
	    	 * on ne récupère jamais rien de nouveau ici
	    	 */

      //Log.w(TAG,"surfaceChanged "+w+" x "+h);

	    	/*
	    	Camera.Parameters parameters = mCamera.getParameters();
	        parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
	        requestLayout();

	        mCamera.setParameters(parameters);
	        mCamera.startPreview();
	        */
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
      final int width = r - l;
      final int height = b - t;

      int previewWidth = width;
      int previewHeight = height;
      if (mPreviewSize != null) {
        previewWidth = mPreviewSize.width;
        previewHeight = mPreviewSize.height;
      }

      // Center the child SurfaceView within the parent.
      if (width * previewHeight > height * previewWidth) {
        final int scaledChildWidth = previewWidth * height
                / previewHeight;
        mSurfaceView.layout((width - scaledChildWidth) / 2, 0,
                (width + scaledChildWidth) / 2, height);
      } else {
        final int scaledChildHeight = previewHeight * width
                / previewWidth;
        mSurfaceView.layout(0, (height - scaledChildHeight) / 2, width,
                (height + scaledChildHeight) / 2);
      }
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
      // We purposely disregard child measurements because act as a
      // wrapper to a SurfaceView that centers the camera preview instead
      // of stretching it.
      final int width = resolveSize(getSuggestedMinimumWidth(),
              widthMeasureSpec);
      final int height = resolveSize(getSuggestedMinimumHeight(),
              heightMeasureSpec);
      setMeasuredDimension(width, height);

      Log.w("DAMS","onMeasure "+width+" x "+height);
      if( width<=0 || height<=0 ) {
        return ;
      }

      mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);

      Log.w("DAMS","Setting size is "+mPreviewSize.width+" x "+mPreviewSize.height);
      Camera.Parameters parameters = mCamera.getParameters();
      parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
      mCamera.setParameters(parameters);
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
      return findOptimalSize(sizes,w,h);
    }
  }

  public File getAlbumStorageDir() {
    // Get the directory for the user's public pictures directory.
    File file = new File(Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES), "Mariage");
    if (!file.mkdirs()) {
      Log.e("DAMS", "Directory not created");
    }
    return file;
  }
  public File getNewPhotoFile() {
    File dir = this.getAlbumStorageDir() ;
    File file = new File(dir,String.format("mariage_%d.jpg", System.currentTimeMillis())) ;
    return file ;
  }

  Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
    public void onPictureTaken(byte[] data, Camera camera) {
      FileOutputStream outStream = null;
      try {
        // write to local sandbox file system
        long tstamp = System.currentTimeMillis() ;
        String tmpFileName = String.format("%d",tstamp) ;
        String tmpFileNameThumb = String.format("%d.thumb",tstamp) ;


        File photoFile = getNewPhotoFile() ;
        //outStream = getActivity().openFileOutput(tmpFileName, 0);
        // Or write to sdcard
        outStream = new FileOutputStream(photoFile);
        outStream.write(data);
        outStream.close();

        //Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length);

        /*
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inSampleSize = 4;
        Bitmap preview_bitmap=BitmapFactory.decodeByteArray(data,0,data.length,options);

        outStream = getActivity().openFileOutput(tmpFileNameThumb, 0);
        preview_bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outStream);
        outStream.close();
        */


        mCallback.onPhotoSave(Uri.fromFile(photoFile));
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
      }
      // Log.d(TAG, "onPictureTaken - jpeg");

      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
      }




      camera.startPreview();
    }
  };


  public void takePicture() {
    if( mCamera != null ) {
      mCamera.takePicture(null, null, jpegCallback);
    }
  }

}
