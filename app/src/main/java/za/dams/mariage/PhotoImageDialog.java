package za.dams.mariage;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.net.URI;
import java.net.URISyntaxException;



public class PhotoImageDialog extends DialogFragment implements View.OnClickListener {
  public interface Callback {
    public void onPhotoDelete(Uri photoUri);
  }
  Callback mCallback ;


  /** Argument name(s) */
  public static final String PHOTOIMAGE_DIALOG_TAG = "PHOTOIMAGE_DIALOG_TAG";

  private static final String ARG_PHOTOURI = "photoUri";
  private static final String ARG_ISDIALOG = "isDialog";

  private Context mContext ;

  private boolean mIsLoaded ;
  private ImageView mImageView ;
  private ProgressBar mProgressBar ;

  public static PhotoImageDialog newInstance(Uri photoUri, boolean isDialog) {
    final PhotoImageDialog instance = new PhotoImageDialog();
    final Bundle args = new Bundle();
    args.putString(ARG_PHOTOURI, photoUri.toString());
    args.putBoolean(ARG_ISDIALOG, isDialog);
    instance.setArguments(args);
    return instance;
  }
  private Uri mImmutablePhotoUri ;
  private boolean mIsDialog ;
  private void initializeArgCache() {
    if (mImmutablePhotoUri != null ) return;
    mImmutablePhotoUri = Uri.parse(getArguments().getString(ARG_PHOTOURI));
    mIsDialog = getArguments().getBoolean(ARG_ISDIALOG);
  }
  public Uri getPhotoUri() {
    initializeArgCache();
    return mImmutablePhotoUri;
  }
  public boolean isDialog() {
    initializeArgCache();
    return mIsDialog ;
  }

  private Runnable mSetImageRunnable = new Runnable()  {
    public void run() {
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {}

      // TODO Auto-generated method stub
      Uri photoUri = PhotoImageDialog.this.getPhotoUri();

      String mPath=null;
      mPath = photoUri.getPath();
      mImageView.post(new Runnable() {
          public void run() {
            //PhotoImageDialog.this.dismiss();
          }
        });

      final Bitmap bitmap = BitmapFactory.decodeFile(mPath);

      mImageView.post(new Runnable() {
        public void run() {
          mProgressBar.setVisibility(View.GONE);
          mImageView.setImageBitmap(bitmap);
          mImageView.setVisibility(View.VISIBLE) ;
          mIsLoaded = true ;
        }
      });
    }
  } ;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mContext = getActivity().getApplicationContext();

    setStyle(DialogFragment.STYLE_NO_TITLE, 0);

    if (savedInstanceState != null) {
      restoreInstanceState(savedInstanceState);
    }
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    Activity activity = getActivity() ;
    try {
      mCallback = (PhotoImageDialog.Callback) activity;
    } catch( ClassCastException e ) {
      throw new ClassCastException(activity.toString()
              + " must implement OnHeadlineSelectedListener");

    }
  }
  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
      mCallback = (PhotoImageDialog.Callback) activity;
    } catch( ClassCastException e ) {
      throw new ClassCastException(activity.toString()
              + " must implement OnHeadlineSelectedListener");

    }
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    mContext = getActivity();
    if( isDialog() ) {
      applyDialogParams() ;
    }
  }
  private void applyDialogParams() {
    Dialog dialog = getDialog();
    dialog.setCanceledOnTouchOutside(true);

    Window window = dialog.getWindow();

    WindowManager.LayoutParams a = window.getAttributes();
    Resources r = mContext.getResources();
    int dialogWidth = (int) r.getDimension(R.dimen.photo_showimage_dialog_width);
    int dialogHeight = (int) r.getDimension(R.dimen.photo_showimage_dialog_height);
    a.width = dialogWidth ;
    a.height = dialogHeight ;
    window.setAttributes(a);
  }
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    View v = inflater.inflate(R.layout.photo_showimage_dialog, container) ;

    mProgressBar = (ProgressBar)v.findViewById(R.id.loading_progress) ;
    mImageView = (ImageView)v.findViewById(R.id.image_view) ;
    mImageView.setVisibility(View.GONE) ;
    mImageView.setOnClickListener(this) ;

    new Thread(mSetImageRunnable).start();

    return v ;
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
  }

  private void restoreInstanceState(Bundle state) {
  }

  @Override
  public void onClick(View view) {
    if( view==mImageView && mIsLoaded ) {} else return ;

    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
    builder.setMessage("Delete this photo ?")
            .setCancelable(true)
            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                onClickDelete();
              }
            })
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
              }
            });
    AlertDialog alert = builder.create();
    alert.show();

  }
  private void onClickDelete() {
    /*
    CrmFileTransactionManager mManager = CrmFileTransactionManager.getInstance( getActivity().getApplicationContext() ) ;
    CrmFileTransaction mTransaction = mManager.getTransaction() ;
    mTransaction.page_delRecord_photo( getPageId(), getRecordId() ) ;

    if( getTargetFragment() instanceof FiledetailGalleryFragment ){
      getTargetFragment().onActivityResult(getTargetRequestCode(), FiledetailGalleryFragment.DELETEIMAGE_RESULT, null) ;
    }

    FiledetailGalleryImageDialog.this.dismiss();
    */
    mCallback.onPhotoDelete(this.getPhotoUri());
    PhotoImageDialog.this.dismiss();
  }

}
