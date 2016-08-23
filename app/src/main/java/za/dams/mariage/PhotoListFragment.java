package za.dams.mariage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.print.PrintHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class PhotoListFragment extends Fragment implements View.OnClickListener {
  @Override
  public void onClick(View v) {
    if( v==mPrintButton ) {
      this.printPhotos();
    }
  }

  public interface Callback {
    public void onPhotoTrigger();
  }
  public Callback mCallback ;

  public enum PhotoListItemType { TYPE_TRIGGER, TYPE_FILE }
  public class PhotoListItem {
    PhotoListItemType type ;
    Uri photoUri ;
  }

  private TextView mTextView ;
  private TextView mCommentView ;
  private ImageButton mPrintButton ;

  private Thread mTriggerThread ;

  private ArrayList<PhotoListItem> mListPhotos ;
  private ArrayList<PhotoListItem> mListItems ;
  private ArrayList<HashMap<String,Object>> mListAdapterObjs ;

  private static final String TAG = "MARIAGE/PhotoListFragment";

  public static final int SHOWIMAGE_REQUEST = 1 ;
  public static final int DELETEIMAGE_RESULT = 1 ;

  public static PhotoListFragment newInstance() {
    PhotoListFragment f = new PhotoListFragment();

    // Supply index input as an argument.
    Bundle args = new Bundle();
    f.setArguments(args);

    return f;
  }

  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // inflater = getActivity().getLayoutInflater() ;
    View v = inflater.inflate(R.layout.photo_list_fragment, container, false ) ;
    mTextView = (TextView)v.findViewById(R.id.mytextview) ;
    mCommentView = (TextView)v.findViewById(R.id.comment) ;
    mPrintButton = (ImageButton)v.findViewById(R.id.button_print);
    mPrintButton.setOnClickListener(this);
    return v ;
  }
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    //Log.w(TAG,"My Fragment Id is "+mTransaction.getCrmFileCode() );
    mListAdapterObjs = new ArrayList<HashMap<String,Object>>() ;
    String[] adaptFrom = { new String("pictureLink") } ;
    int[] adaptTo = { R.id.mygallerypicture } ;
    GridView mgv = (GridView) getView().findViewById(R.id.mygalleryview) ;
    mgv.setAdapter(new SimpleAdapter(getActivity().getApplicationContext(), mListAdapterObjs, R.layout.photo_list_galleryitem, adaptFrom, adaptTo )) ;

    mgv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      public void onItemClick(AdapterView<?> parentView, View childView, int position, long id) {
        PhotoListFragment.this.handleClickList(position) ;
      }
    }) ;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mListPhotos = new ArrayList<PhotoListItem>() ;
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    Activity activity = getActivity() ;
    try {
      mCallback = (PhotoListFragment.Callback) activity;
    } catch( ClassCastException e ) {
      throw new ClassCastException(activity.toString()
              + " must implement OnHeadlineSelectedListener");

    }
  }
  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
      mCallback = (PhotoListFragment.Callback) activity;
    } catch( ClassCastException e ) {
      throw new ClassCastException(activity.toString()
              + " must implement OnHeadlineSelectedListener");

    }
  }
  @Override
  public void onResume() {
    super.onResume();
    syncWithData() ;
  }

  @Override
  public void onPause() {
    this.abortTrigger();
    super.onPause();
  }

  public void addPhoto( Uri photoUri ) {
    PhotoListItem pli = new PhotoListItem();
    pli.type = PhotoListItemType.TYPE_FILE ;
    pli.photoUri = photoUri ;
    mListPhotos.add(pli) ;

    syncWithData();
  }
  public void deletePhoto( Uri photoUri ) {

    for( PhotoListItem pli : mListPhotos ) {
      if( photoUri.equals(pli.photoUri)) {
        mListPhotos.remove(pli) ;
        break ;
      }
    }

    syncWithData();
  }


  public void syncWithData() {
    mListAdapterObjs.clear() ;
    HashMap<String,Object> mPoint ;

    mListItems = new ArrayList<PhotoListItem>() ;
    for( PhotoListItem pli : mListPhotos ) {
      mListItems.add(pli) ;
    }
    if( mListItems.size() < 4 ) {
      PhotoListItem pliTrigger = new PhotoListItem() ;
      pliTrigger.type = PhotoListItemType.TYPE_TRIGGER ;

      mListItems.add(pliTrigger) ;

    }

    if( mListPhotos.size() == 1 || mListPhotos.size() == 4 ) {
      mPrintButton.setVisibility(View.VISIBLE);
      mCommentView.setVisibility(View.GONE);
    } else {
      mPrintButton.setVisibility(View.GONE);
      mCommentView.setVisibility(View.VISIBLE);
    }

    String s ="" ;
    switch( mListPhotos.size() ) {
      case 0 :
        s = "1 photo ou 4 ? A vous de voir !" ;
        break ;
      case 2 :
        s = "Plus que 2 photos !" ;
        break ;
      case 3 :
        s = "La dernière !" ;
        break ;
    }
    mCommentView.setText(s) ;

    for( PhotoListItem pli : mListItems ) {
      switch(pli.type) {
        case TYPE_TRIGGER:
          mPoint = new HashMap<String,Object>() ;
          if( mTriggerThread != null ) {
            mPoint.put("pictureLink",R.drawable.crm_missing) ;
          } else {
            mPoint.put("pictureLink",R.drawable.camera_photo) ;
          }
          mListAdapterObjs.add(mPoint) ;
          break ;
        case TYPE_FILE:
          mPoint = new HashMap<String,Object>() ;
          mPoint.put("pictureLink",pli.photoUri.toString()) ;
          mListAdapterObjs.add(mPoint) ;
          break ;
      }
    }

    ((SimpleAdapter) ((GridView) getView().findViewById(R.id.mygalleryview)).getAdapter()).notifyDataSetChanged() ;
  }
  public void handleClickList(int position) {
    PhotoListItem pli = mListItems.get(position) ;
    switch( pli.type ) {
      case TYPE_TRIGGER:
        if( mTriggerThread != null ) {
          this.abortTrigger();
        } else {
          this.goTrigger();
        }
        break ;
      case TYPE_FILE:
        PhotoImageDialog fragment = PhotoImageDialog.newInstance(pli.photoUri,true) ;
        fragment.setTargetFragment(this, SHOWIMAGE_REQUEST);
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        // if we have an old popup replace it
        Fragment fOld = fm.findFragmentByTag(PhotoImageDialog.PHOTOIMAGE_DIALOG_TAG);
        if (fOld != null && fOld.isAdded()) {
          ft.remove(fOld);
        }
        ft.add(fragment, PhotoImageDialog.PHOTOIMAGE_DIALOG_TAG);
        ft.commit();
        return ;
    }
    /*
    if( position != 0 ){
      int pageId = getShownIndex() ;
      int recordId = position - 1 ;
      Log.w(TAG,"Displaying page "+pageId+" position "+recordId) ;

      FiledetailGalleryImageDialog fragment = FiledetailGalleryImageDialog.newInstance(pageId,recordId,true) ;
      fragment.setTargetFragment(this, SHOWIMAGE_REQUEST);
      FragmentManager fm = getFragmentManager();
      FragmentTransaction ft = fm.beginTransaction();
      // if we have an old popup replace it
      Fragment fOld = fm.findFragmentByTag(FiledetailGalleryImageDialog.FILEVIEWIMAGE_DIALOG_TAG);
      if (fOld != null && fOld.isAdded()) {
        ft.remove(fOld);
      }
      ft.add(fragment, FiledetailGalleryImageDialog.FILEVIEWIMAGE_DIALOG_TAG);
      ft.commit();
      return ;
    }

    FiledetailCameraFragment cameraFragment = FiledetailCameraFragment.newInstance(getShownIndex());
    FragmentTransaction ft = getFragmentManager().beginTransaction();
    ft.replace(R.id.filedetail, cameraFragment);
    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
    ft.addToBackStack(null) ;
    ft.commit();
    */
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch( requestCode ) {
      case SHOWIMAGE_REQUEST:
        switch( resultCode ) {
          case DELETEIMAGE_RESULT:
            syncWithData();
        }
    }
  }

  private void goTrigger() {
    mTriggerThread = new Thread(new Runnable() {
      public void doClean() {
        PhotoListFragment.this.getActivity().runOnUiThread(new Runnable() {
          @Override
          public void run() {
            mTextView.setText("") ;
          }
        });
        PhotoListFragment.this.mTriggerThread = null ;
        if( PhotoListFragment.this.isAdded() ) {
          PhotoListFragment.this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
              syncWithData();
            }
          });
        }
      }
      public void run() {
        for( int i = 10 ; i>0 ; i-- ) {
          try {
            Thread.sleep(500);
          } catch (InterruptedException e) {
            doClean() ;
            return ;
          }

          String s = "" ;
          if( i >= 10 ) {
            s = "ATTENTION !" ;
          } else if( i >= 9 ) {
            s = "" ;
          } else if( i >= 8 ) {
            s = "ATTENTION !" ;
          } else if( i >= 6 ) {
            s = "3" ;
          } else if( i >= 4 ) {
            s = "2" ;
          } else if( i >= 2 ) {
            s = "1" ;
          }

          final String ss = s ;

          PhotoListFragment.this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
              mTextView.setText(ss) ;
            }
          });

        }
        PhotoListFragment.this.getActivity().runOnUiThread(new Runnable() {
          @Override
          public void run() {
            mTextView.setText("SMILE !") ;
            PhotoListFragment.this.takePicture();
          }
        });
        //PhotoListFragment.this.takePicture() ;
        this.doClean();
      }
    }) ;
    mTriggerThread.start();
    syncWithData();
  }
  private void abortTrigger() {
    if( mTriggerThread != null && mTriggerThread.isAlive() ) {
      mTriggerThread.interrupt() ;
    }
  }
  private void takePicture() {
    mCallback.onPhotoTrigger();
  }

  public void clearText() {
    mTextView.setText("");
  }






  public void printPhotos() {
    if( mListPhotos.size() == 4 ) {
      new PrintFourTask().execute();
      return ;
    }
    if( mListPhotos.size() == 1  ) {
      Bitmap result = BitmapFactory.decodeFile(mListPhotos.get(0).photoUri.getPath());
      PrintHelper photoPrinter = new PrintHelper(getActivity());
      photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
      photoPrinter.printBitmap("Mariage.jpg", result);
      return ;
    }
  }

  private class PrintFourTask extends AsyncTask<Void, Void, Void> {
    ProgressDialog mProgressDialog;

    Bitmap result;

    protected void onPreExecute() {
      mProgressDialog = ProgressDialog.show(
              PhotoListFragment.this.getActivity(),
              "Print",
              "Impression photos...",
              true);

    }

    @Override
    protected Void doInBackground(Void... params) {
      Bitmap[] parts = new Bitmap[4];
      for (int i = 0; i < 4; i++) {
        parts[i] = BitmapFactory.decodeFile(mListPhotos.get(i).photoUri.getPath());
      }


      result = Bitmap.createBitmap(parts[0].getWidth() * 2, parts[0].getHeight() * 2, Bitmap.Config.ARGB_8888);
      Canvas canvas = new Canvas(result);
      Paint paint = new Paint();
      for (int i = 0; i < parts.length; i++) {
        canvas.drawBitmap(parts[i], parts[i].getWidth() * (i % 2), parts[i].getHeight() * (i / 2), paint);
      }

      File dir = new File(Environment.getExternalStoragePublicDirectory(
              Environment.DIRECTORY_PICTURES), "Mariage");
      File file = new File(dir, String.format("mariage_%d.jpg", System.currentTimeMillis()));

      FileOutputStream out = null;
      try {
        out = new FileOutputStream(file);
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
      result.compress(Bitmap.CompressFormat.JPEG, 90, out);
      try {
        out.flush();
      } catch (IOException e) {
        e.printStackTrace();
      }
      try {
        out.close();
      } catch (IOException e) {
        e.printStackTrace();
      }

      result = Bitmap.createScaledBitmap(result,result.getWidth()/2,result.getHeight()/2, true) ;

      return null;
    }

    protected void onPostExecute(Void arg0) {
      if (true) {
        mProgressDialog.dismiss();
      }
      PrintHelper photoPrinter = new PrintHelper(getActivity());
      photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
      photoPrinter.printBitmap("Mariage.jpg", result);




    }
  }
}
