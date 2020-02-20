/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ora.android.eyecup;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.AudioManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaActionSound;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import static android.widget.ImageView.ScaleType.CENTER_CROP;
import static com.ora.android.eyecup.Globals.APP_DIR_PARTICIPANTS;
import static com.ora.android.eyecup.Globals.APP_DIR_PARTICIPANT_PICS;
import static com.ora.android.eyecup.Globals.DT_FMT_FULL_FILENAME;
import static java.lang.Thread.sleep;

public class Camera2BasicFragment extends Fragment
        implements View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private TextView txtInstruction;
    private String mstrPicCode;

    private boolean bWaitReview = false;
    private boolean bReviewImageSet = false;

    private ImageView mImageView;
    private static final int PIC_DELAY_SECONDS = 10;

    private static final boolean bManualCamera = true;

    private static final float PIC_FOCUS_CM = 11.5f;
    private static final long SHUTTER_FACTOR = 1500;
//    private static final long SHUTTER_FACTOR = 1750;
//    private static final int SENS_SENSITIVITY = 4000;
    private static final int SENS_SENSITIVITY = 100;
//    private static final long FRAME_DURATION = 33333333;
    private static final long FRAME_DURATION = 25000000;
//20200110 end
    /**
     * Conversion from screen rotation to JPEG orientation.
     */
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final String FRAGMENT_DIALOG = "dialog";

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    /**
     * Tag for the {@link Log}.
     */
    private static final String TAG = "Camera2BasicFragment";

    /**
     * Camera state: Showing camera preview.
     */
    private static final int STATE_PREVIEW = 0;

    /**
     * Camera state: Waiting for the focus to be locked.
     */
    private static final int STATE_WAITING_LOCK = 1;

    /**
     * Camera state: Waiting for the exposure to be precapture state.
     */
    private static final int STATE_WAITING_PRECAPTURE = 2;

    /**
     * Camera state: Waiting for the exposure state to be something other than precapture.
     */
    private static final int STATE_WAITING_NON_PRECAPTURE = 3;

    /**
     * Camera state: Picture was taken.
     */
    private static final int STATE_PICTURE_TAKEN = 4;

    /**
     * Max preview width that is guaranteed by Camera2 API
     */
    private static final int MAX_PREVIEW_WIDTH = 1920;

    /**
     * Max preview height that is guaranteed by Camera2 API
     */
    private static final int MAX_PREVIEW_HEIGHT = 1080;

    /**
     * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a
     * {@link TextureView}.
     */
    private final TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }

    };

    /**
     * ID of the current {@link CameraDevice}.
     */
    private String mCameraId;

    /**
     * An {@link AutoFitTextureView} for camera preview.
     */
    private AutoFitTextureView mTextureView;

    /**
     * A {@link CameraCaptureSession } for camera preview.
     */
    private CameraCaptureSession mCaptureSession;

    /**
     * A reference to the opened {@link CameraDevice}.
     */
    private CameraDevice mCameraDevice;

    /**
     * The {@link android.util.Size} of camera preview.
     */
    private Size mPreviewSize;

    /**
     * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its state.
     */
    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            // This method is called when the camera is opened.  We start camera preview here.

            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
//20200113
            createCameraPreviewSession();
//            createCameraCaptureSession();
//20200113 end
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            Activity activity = getActivity();
            if (null != activity) {
                activity.finish();
            }
        }
    };

//    //jlr update text field from CameraActivity
//    public void setActvityData(String strTxt, String mstrPicCode) {
//        txtInstruction.setText(strTxt);
//
////        setControls();
//    }

    //JLR set control visibility for review or take picture
    private void setControls() {

        //todo Warning:(280, 44) Method invocation 'findViewById' may produce 'NullPointerException'
        try {
            LinearLayout picLayout = getView().findViewById(R.id.layoutTakePic);
            LinearLayout accLayout = getView().findViewById(R.id.layoutAccRej);
            LinearLayout preLayout = getView().findViewById(R.id.layoutPreview);
            LinearLayout revLayout = getView().findViewById(R.id.layoutReview);

            if (bWaitReview) {
                picLayout.setVisibility(View.GONE);
                preLayout.setVisibility(View.GONE);
                accLayout.setVisibility(View.VISIBLE);
                revLayout.setVisibility(View.VISIBLE);
            } else {
                picLayout.setVisibility(View.VISIBLE);
                preLayout.setVisibility(View.VISIBLE);
                accLayout.setVisibility(View.GONE);
                revLayout.setVisibility(View.GONE);
            }
        } catch (NullPointerException e){
            Log.e("C2BF:SetControls:NPEx", e.toString());
            //todo handle
        }
    }
    //JLR end
    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private HandlerThread mBackgroundThread;

    /**
     * A {@link Handler} for running tasks in the background.
     */
    private Handler mBackgroundHandler;

    /**
     * An {@link ImageReader} that handles still image capture.
     */
    private ImageReader mImageReader;

    /**
     * This is the output file for our picture.
     */
    private File mFile;

    /**
     * This a callback object for the {@link ImageReader}. "onImageAvailable" will be called when a
     * still image is ready to be saved.
     */
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {

            Globals glob = new Globals();
            Date dt = Calendar.getInstance().getTime();
            CameraActivity activity = (CameraActivity) getActivity();

            //todo Warning:(326, 22) Method invocation 'getPatNumber' may produce 'NullPointerException'
            try {
                activity.getPatNumber();
            } catch (NullPointerException e) {
                Log.e("C2BF:OnImageAvailable.activity.getPatNumber:NPEx", e.toString());
                //todo handle
            }

            String strDir = APP_DIR_PARTICIPANTS + "/" + activity.getPatNumber() + APP_DIR_PARTICIPANT_PICS;
            String strFile = activity.getPatNumber();
            strFile = strFile + "_" + mstrPicCode;
            strFile = strFile + "_" + glob.GetDateStr(DT_FMT_FULL_FILENAME,dt);
            //toto full picture file name
            strFile = strFile + ".jpg";

            //todo Warning:(330, 52) Method invocation 'getExternalFilesDir' may produce 'NullPointerException'
            try {
//                File fNewFile = new File(activity.getExternalFilesDir(null), strFile);
//                File fNewFile = new File(activity.getExternalFilesDir(activity.getPatNumber()), strFile);
                File fNewFile = new File(activity.getExternalFilesDir(strDir), strFile);
                mBackgroundHandler.post(new ImageSaver(reader.acquireNextImage(), fNewFile));
                mFile = fNewFile;
            } catch (NullPointerException e) {
                Log.e("C2BF:OnImageAvailable.activity.getExternalFilesDir:NPEx", e.toString());
                //todo handle
            }
        }
    };

    /**
     * {@link CaptureRequest.Builder} for the camera preview
     */
    private CaptureRequest.Builder mPreviewRequestBuilder;

    /**
     * {@link CaptureRequest} generated by {@link #mPreviewRequestBuilder}
     */
    private CaptureRequest mPreviewRequest;

    /**
     * The current state of camera state for taking pictures.
     *
     * @see #mCaptureCallback
     */
    private int mState = STATE_PREVIEW;

    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
     */
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    /**
     * Whether the current camera device supports Flash or not.
     */
    private boolean mFlashSupported;

    /**
     * Orientation of the camera sensor
     */
    private int mSensorOrientation;

    /**
     * A {@link CameraCaptureSession.CaptureCallback} that handles events related to JPEG capture.
     */
    private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {

        private void process(CaptureResult result) {
            switch (mState) {
                case STATE_PREVIEW: {
                    // We have nothing to do when the camera preview is working normally.

                    //todo clean this up, remove a try-catch
                    if (mFile.exists()) {
                        if (mImageView != null) {
//                            try {
                            try {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (bReviewImageSet != true) {
                                            BitmapFactory.Options o = new BitmapFactory.Options();
                                            o.inJustDecodeBounds = true;
                                            try {
                                                BitmapFactory.decodeStream(new FileInputStream(mFile), null, o);

                                                final int REQUIRED_SIZE = 256;     // The new size we want to scale to

                                                int scale = 1;                  // Find the correct scale value. It should be the power of 2.
                                                while (o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                                                        o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                                                    scale *= 2;
                                                }

                                                BitmapFactory.Options o2 = new BitmapFactory.Options(); // Decode with inSampleSize
                                                o2.inSampleSize = scale;
                                                Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(mFile), null, o2);

                                                //crop (match image rotation below)
                                                int iCropW = 10;
                                                int iCropH = 20;
                                                if (bitmap.getWidth() > bitmap.getHeight()) {
//                                                    iCropW = 2 * iCropH;
                                                    iCropH = 2 * iCropW;
                                                } else {
//                                                    iCropH = 2 * iCropW;
                                                    iCropW = 2 * iCropH;
                                                }
                                                bitmap=Bitmap.createBitmap(bitmap
                                                        , (int)(bitmap.getWidth()/iCropW)
                                                        , (int)(bitmap.getHeight()/iCropH)
                                                        , bitmap.getWidth() - (2 * (int)(bitmap.getWidth()/iCropW))
                                                        , bitmap.getHeight() - (2 * (int)(bitmap.getHeight()/iCropH)));

//                                                mImageView.setRotation(90);
                                                mImageView.setScaleType(CENTER_CROP);

                                                mImageView.setImageBitmap(bitmap);
                                                bReviewImageSet = true;

                                            } catch (FileNotFoundException e) {
                                                Log.e("C2BF:ImageView:FNFEx", e.toString());
                                            } catch (NullPointerException e) {
                                                Log.e("C2BF:ImageView:NPEx", e.toString());
                                            }
                                        }
                                    }
                                });
                            } catch (NullPointerException e) {
                                Log.e("C2BF:CaptureCallback.activity.runOnUiThread:NPEx", e.toString());
                                //todo handle
                            }
                            catch (Exception e2) {
                                Log.e("C2BF:CaptureCallback.activity.runOnUiThread:Ex", e2.toString());
                                //todo Warning:(414, 35) Some important exceptions might be ignored in a 'catch' block
                                e2.getMessage();
                            }
                        }

                        //////////////// Wait for Review (Click Accept or Reject) ///////////////
                        int iCnt = 0;
                        while (bWaitReview) {
                            try {
                                sleep(100);
                            } catch (InterruptedException e) {
                                Log.e("C2BF:CaptureCallback.activity.runOnUiThread:Sleep.IntEx", e.toString());
                            }
                            iCnt = iCnt + 1;
                            if (iCnt > 100) {
                                iCnt = 0;
                            }
                        }

                        //////////////// Continue (Accept or Reject was clicked) ///////////////
                        if (mImageView != null) {
                            if (bReviewImageSet) {
                                bReviewImageSet = false;
                                try {
                                    getActivity().runOnUiThread(new Runnable() {

                                        @Override
                                        public void run() {
                                            mImageView.setImageBitmap(null);
                                        }
                                    });
                                } catch (NullPointerException e) {
                                    Log.e("C2BF:After accept/reject.runOnUiThread:NPEx", e.toString());
                                    //todo handle
                                }
                            }
                        }
                    }
                    break;
                }
                case STATE_WAITING_LOCK: {
                    Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                    if (afState == null) {
                        captureStillPicture();
                    } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||
                            CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                        // CONTROL_AE_STATE can be null on some devices
                        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                        if (aeState == null ||
                                aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                            mState = STATE_PICTURE_TAKEN;
                            captureStillPicture();
                        } else {
                            runPrecaptureSequence();
                        }
                    }
                    break;
                }
                case STATE_WAITING_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null ||
                            aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                            aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        mState = STATE_WAITING_NON_PRECAPTURE;
                    }
                    break;
                }
                case STATE_WAITING_NON_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        mState = STATE_PICTURE_TAKEN;
                        captureStillPicture();
                    }
                    break;
                }
            }
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                        @NonNull CaptureRequest request,
                                        @NonNull CaptureResult partialResult) {
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            process(result);
        }

    };

    /**
     * Shows a {@link Toast} on the UI thread.
     *
     * @param text The message to show
     */
    private void showToast(final String text) {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Given {@code choices} of {@code Size}s supported by a camera, choose the smallest one that
     * is at least as large as the respective texture view size, and that is at most as large as the
     * respective max size, and whose aspect ratio matches with the specified value. If such size
     * doesn't exist, choose the largest one that is at most as large as the respective max size,
     * and whose aspect ratio matches with the specified value.
     *
     * @param choices           The list of sizes that the camera supports for the intended output
     *                          class
     * @param textureViewWidth  The width of the texture view relative to sensor coordinate
     * @param textureViewHeight The height of the texture view relative to sensor coordinate
     * @param maxWidth          The maximum width that can be chosen
     * @param maxHeight         The maximum height that can be chosen
     * @param aspectRatio       The aspect ratio
     * @return The optimal {@code Size}, or an arbitrary one if none were big enough
     */
    private static Size chooseOptimalSize(Size[] choices, int textureViewWidth,
            int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {

        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions that are smaller than the preview Surface
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
                    option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth &&
                    option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            Log.i(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    public static Camera2BasicFragment newInstance() {
        return new Camera2BasicFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera2_basic, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {

        CameraActivity activity = (CameraActivity) getActivity();

        view.findViewById(R.id.btnPicture).setOnClickListener(this);
        view.findViewById(R.id.btn_accept).setOnClickListener(this);
        view.findViewById(R.id.btn_reject).setOnClickListener(this);

        txtInstruction = view.findViewById(R.id.txtInstruction);

        //todo Warning:(600, 41) Method invocation 'getActTxt' may produce 'NullPointerException'
        try {
            txtInstruction.setText(activity.getActTxt());
            mstrPicCode = activity.getPictureCode();
        } catch (NullPointerException e) {
            Log.e("C2BF:OnViewCreated.activity.getActTxt:Ex", e.toString());
            //todo handle
        }

        mTextureView = view.findViewById(R.id.texture);
        mImageView = view.findViewById(R.id.imageView);
        //20200202 comment clickable and listener
//        mImageView.setClickable(true);
//        //mImageView.bringToFront();
//        mImageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mImageView.setRotation(mImageView.getRotation() + 90);  //rotate 90 degrees
//            }
//        });

//20200111
//20200111 end
        bWaitReview = false;
        setControls();     //not reviewing
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //todo Warning:(611, 40) Method invocation 'getExternalFilesDir' may produce 'NullPointerException'
        try {
            mFile = new File(getActivity().getExternalFilesDir(null), "tmp.jpg");
        } catch (NullPointerException e) {
            Log.e("C2BF:OnActivityCreated.activity.getExternalFilesDir:NPEx", e.toString());
            //todo handle
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();

        // When the screen is turned off and turned back on, the SurfaceTexture is already
        // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
        // a camera and start preview from here (otherwise, we wait until the surface is ready in
        // the SurfaceTextureListener).
        if (mTextureView.isAvailable()) {
            openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    public void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    private void requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            new ConfirmationDialog().show(getChildFragmentManager(), FRAGMENT_DIALOG);
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ErrorDialog.newInstance(getString(R.string.request_permission))
                        .show(getChildFragmentManager(), FRAGMENT_DIALOG);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * Sets up member variables related to camera.
     *
     * @param width  The width of available size for camera preview
     * @param height The height of available size for camera preview
     */
    @SuppressWarnings("SuspiciousNameCombination")
    private void setUpCameraOutputs(int width, int height) {
        Activity activity = getActivity();

        try {
            CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);

            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics
                        = manager.getCameraCharacteristics(cameraId);

                // We don't use a front facing camera in this sample.
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }

                StreamConfigurationMap map = characteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    continue;
                }

                // For still image captures, we use the largest available size.
                //todo image format?
                Size largest = Collections.max(
                        Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                        new CompareSizesByArea());
                mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(),
                        ImageFormat.JPEG, /*maxImages*/2);
                mImageReader.setOnImageAvailableListener(
                        mOnImageAvailableListener, mBackgroundHandler);

                // Find out if we need to swap dimension to get the preview size relative to sensor
                // coordinate.
                int displayRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
                //noinspection ConstantConditions
                mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                boolean swappedDimensions = false;
                switch (displayRotation) {
                    case Surface.ROTATION_0:
                    case Surface.ROTATION_180:
                        if (mSensorOrientation == 90 || mSensorOrientation == 270) {
                            swappedDimensions = true;
                        }
                        break;
                    case Surface.ROTATION_90:
                    case Surface.ROTATION_270:
                        if (mSensorOrientation == 0 || mSensorOrientation == 180) {
                            swappedDimensions = true;
                        }
                        break;
                    default:
                        Log.i(TAG, "Display rotation is invalid: " + displayRotation);
                }

                Point displaySize = new Point();
                activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
                int rotatedPreviewWidth = width;
                int rotatedPreviewHeight = height;
                int maxPreviewWidth = displaySize.x;
                int maxPreviewHeight = displaySize.y;

                if (swappedDimensions) {
                    rotatedPreviewWidth = height;
                    rotatedPreviewHeight = width;
                    maxPreviewWidth = displaySize.y;
                    maxPreviewHeight = displaySize.x;
                }

                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                    maxPreviewWidth = MAX_PREVIEW_WIDTH;
                }

                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                    maxPreviewHeight = MAX_PREVIEW_HEIGHT;
                }

                // Danger, W.R.! Attempting to use too large a preview size could  exceed the camera
                // bus' bandwidth limitation, resulting in gorgeous previews but the storage of
                // garbage capture data.
                //todo preview size
                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                        rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                        maxPreviewHeight, largest);

                // We fit the aspect ratio of TextureView to the size of preview we picked.
                int orientation = getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mTextureView.setAspectRatio(
                            mPreviewSize.getWidth(), mPreviewSize.getHeight());
                } else {
                    mTextureView.setAspectRatio(
                            mPreviewSize.getHeight(), mPreviewSize.getWidth());
                }

                // Check if the flash is supported.
                Boolean available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                mFlashSupported = available == null ? false : available;

                mCameraId = cameraId;
                return;
            }
        } catch (CameraAccessException e) {
            Log.e("C2BF:setUpCameraOutputs:CAEx", e.toString());
            //todo handle
            e.printStackTrace();
        } catch (NullPointerException e) {
            Log.e("C2BF:setUpCameraOutputs:NPEx", e.toString());
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            ErrorDialog.newInstance(getString(R.string.camera_error))
                    .show(getChildFragmentManager(), FRAGMENT_DIALOG);
        }
    }

    /**
     * Opens the camera specified by {@link Camera2BasicFragment#mCameraId}.
     */
    private void openCamera(int width, int height) {

        //todo Warning:(777, 47) Argument 'getActivity()' might be null
        if (getActivity() == null) {
            //todo you need to handle better
            //todo log error
            return;
        }

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
            return;
        }
        setUpCameraOutputs(width, height);
        configureTransform(width, height);
        Activity activity = getActivity();
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            manager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            Log.e("C2BF:openCamera:CAEx", e.toString());
            //todo handle
            e.printStackTrace();
        } catch (InterruptedException e) {
            Log.e("C2BF:openCamera:IntExEx", e.toString());
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        }
    }

    /**
     * Closes the current {@link CameraDevice}.
     */
    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCaptureSession) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mImageReader) {
                mImageReader.close();
                mImageReader = null;
            }
        } catch (InterruptedException e) {
            Log.e("C2BF:closeCamera:IntExEx", e.toString());
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            Log.e("C2BF:stopBackgroundThread:IntExEx", e.toString());
            //todo handle
            e.printStackTrace();
        }
    }

    /**
     * Creates a new {@link CameraCaptureSession} for camera preview.
     */
    private void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;

            // We configure the size of default buffer to be the size of camera preview we want.
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

            // This is the output Surface we need to start preview.
            Surface surface = new Surface(texture);

            // We set up a CaptureRequest.Builder with the output Surface.
//
//20200122 error not here
            if (bManualCamera) {
                Log.d("C2BF:createCameraPreviewSession", "createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)");
                mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            } else {
                Log.d("C2BF:createCameraPreviewSession", "createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)");
                mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            }
//20200111 end
            mPreviewRequestBuilder.addTarget(surface);

            // Here, we create a CameraCaptureSession for camera preview.
            Log.d("C2BF:createCameraPreviewSession", "createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()");
            mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            // The camera is already closed
                            if (null == mCameraDevice) {
                                return;
                            }

                            Log.d("C2BF:CameraCaptureSession", "OnConfigured");
                            // When the session is ready, we start displaying the preview.
                            mCaptureSession = cameraCaptureSession;
                            try {
                                // Auto focus should be continuous for camera preview.
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                // Flash is automatically enabled when necessary.
                                setAutoFlash(mPreviewRequestBuilder);
                                // Finally, we start displaying the camera preview.
                                mPreviewRequest = mPreviewRequestBuilder.build();
                                Log.d("C2BF:CameraCaptureSession", "OnConfigured:setRepeatingRequest");
                                mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mBackgroundHandler);

//                            } catch (CameraAccessException | NullPointerException | Exception e) {
                            } catch (CameraAccessException e) {
                                Log.e("C2BF:createCaptureSession:OnConfigured:CAEx", e.toString());
                                //todo handle
                            } catch (NullPointerException e) {
                                Log.e("C2BF:createCaptureSession:OnConfigured:NPEx", e.toString());
                                e.printStackTrace();
                                //todo handle
                            }
                            catch (Exception e) {
                                Log.e("C2BF:createCaptureSession:OnConfigured:Ex", e.toString());
                                //todo handle
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(
                                @NonNull CameraCaptureSession cameraCaptureSession) {
                            Log.i("C2BF:createCaptureSession:", "onConfigureFailed");
                            showToast("Failed");
                        }
                    }, null
            );
        } catch (CameraAccessException e) {
            Log.e("C2BF:createCameraPreviewSession:CAEx", e.toString());
            //todo handle
            e.printStackTrace();
        }
    }

    /**
     * Configures the necessary {@link android.graphics.Matrix} transformation to `mTextureView`.
     * This method should be called after the camera preview size is determined in
     * setUpCameraOutputs and also the size of `mTextureView` is fixed.
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */
    private void configureTransform(int viewWidth, int viewHeight) {
        Activity activity = getActivity();
        if (null == mTextureView || null == mPreviewSize || null == activity) {
            return;
        }
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    /**
     * Initiate a still image capture.
     */
//    private void takePicture() {
    private void takePicture() {
        // add sleep to try to avoid java.lang.NullPointerException:
        //  Attempt to invoke virtual method 'android.hardware.camera2.CaptureRequest
        //  android.hardware.camera2.impl.CameraDeviceImpl$CaptureCallbackHolder.getRequest(int)'
        //  on a null object reference
        // https://stackoverflow.com/questions/37509237/taking-images-camera2api/40904906#40904906
//        try {
//            sleep(1000);
//        } catch (InterruptedException e) {
//            Log.e("C2BF:takePicture:sleep:IntExEx", e.toString());
//            //todo handle
//            e.printStackTrace();
//        }

        lockFocus();
    }

    /**
     * Lock the focus as the first step for a still image capture.
     */
    private void lockFocus() {
        try {
            // This is how to tell the camera to lock focus.
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
            // Tell #mCaptureCallback to wait for the lock.
            mState = STATE_WAITING_LOCK;

            Log.d("C2BF:lockFocus", "mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler)");
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            Log.e("C2BF:lockFocus:CAEx", e.toString());
            //todo handle
            e.printStackTrace();
        }
    }

    /**
     * Run the precapture sequence for capturing a still image. This method should be called when
     * we get a response in {@link #mCaptureCallback} from {@link #lockFocus()}.
     */
    private void runPrecaptureSequence() {
        try {
            // This is how to tell the camera to trigger.
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
            // Tell #mCaptureCallback to wait for the precapture sequence to be set.
            mState = STATE_WAITING_PRECAPTURE;
            Log.d("C2BF:runPrecaptureSequence", "mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler)");
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            Log.e("C2BF:runPrecaptureSequence:CAEx", e.toString());
            //todo handle
            e.printStackTrace();
        }
    }

    /**
     * Capture a still picture. This method should be called when we get a response in
     * {@link #mCaptureCallback} from both {@link #lockFocus()}.
     */
    private void captureStillPicture() {
        try {
            final Activity activity = getActivity();
            if (null == activity || null == mCameraDevice) {
                Log.d("C2BF:captureStillPicture", "(null == activity || null == mCameraDevice)");
                return;
            }

            // This is the CaptureRequest.Builder that we use to take a picture.
            Log.d("C2BF:captureStillPicture", "CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)");
            final CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader.getSurface());
//20200111
            try {
                if (bManualCamera) {
                    Log.d("C2BF:Manual", "captureBuilder.get(CaptureRequest.CONTROL_AWB_MODE)");
                    int iWB = captureBuilder.get(CaptureRequest.CONTROL_AWB_MODE);                          //get default white balance mode

                    captureBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_OFF);       //auto-everything off (Exposure, White Balance, Focus)
                    captureBuilder.set(CaptureRequest.CONTROL_AWB_MODE, iWB);                                //set white balance back to default
                    captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);    //autofocus off
                    captureBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, 100f / PIC_FOCUS_CM);                //100/cm

                    captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);

                    captureBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_SINGLE);

                    captureBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, (long) 1000000000 / SHUTTER_FACTOR);   //nanoseconds

                    captureBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, SENS_SENSITIVITY);
                    captureBuilder.set(CaptureRequest.SENSOR_FRAME_DURATION, FRAME_DURATION);

                } else {
                    // Use the same AE and AF modes as the preview.
                    Log.d("C2BF:Not Manual", "captureBuilder.get(CaptureRequest.CONTROL_AWB_MODE)");
                    captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                    setAutoFlash(captureBuilder);
                }

                Log.d("C2BF", "captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ...)");
            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation));
//            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, 2);
//                captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, 90);

            } catch (NullPointerException e) {
                Log.e("C2BF:captureStillPicture:NPEx", e.toString());
                //todo handle
            }

            CameraManager manager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(mCameraDevice.getId());
            Size[] jpegSizes = null;

            //todo Warning:(1084, 104) Method invocation 'getOutputSizes' may produce 'NullPointerException'
            try {
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            } catch (NullPointerException e) {
                Log.e("C2BF:captureStillPicture:jpegSizes:NPEx", e.toString());
                //todo handle
            }

            int width = 640;
            int height = 480;
            if (jpegSizes != null && 0 < jpegSizes.length) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }
//20200110 comment above?
//20200202
//            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
//            //todo Warning contents of collection 'outputSurfaces' are updated bt never queried
//            List<Surface> outputSurfaces = new ArrayList<>(2);
//            outputSurfaces.add(reader.getSurface());
//            outputSurfaces.add(new Surface(mTextureView.getSurfaceTexture()));
            //todo Warning contents of collection 'outputSurfaces' are updated bt never queried
            List<Surface> outputSurfaces = new ArrayList<>(2);
            outputSurfaces.add(mImageReader.getSurface());
            outputSurfaces.add(new Surface(mTextureView.getSurfaceTexture()));

            final File tmpFile = new File(getActivity().getExternalFilesDir(null), "temp.jpg");

//            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
//                @Override
//                public void onImageAvailable(ImageReader reader) {
//                    Image image = null;
//                    try {
//                        Log.d("C2BF:ImageReader.OnImageAvailableListener", "onImageAvailable");
//                        image = reader.acquireLatestImage();
//                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
//
//                        //https://stackoverflow.com/questions/28003186/capture-picture-without-preview-using-camera2-api
////                        byte[] bytes = new byte[buffer.capacity()];
////                        buffer.get(bytes);
////                        save(bytes);
//                        byte[] bytes = new byte[buffer.remaining()];
//                        buffer.get(bytes);
//                        save(bytes, tmpFile);
//
//                    } catch (FileNotFoundException e) {
//                        Log.e("C2BF:onImageAvailable:FNFEx", e.toString());
//                        //todo handle
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        Log.e("C2BF:onImageAvailable:IOEx", e.toString());
//                        //todo handle
//                        //todo Warning:(1110, 23) 'catch' branch identical to 'FileNotFoundException' branch
//                        e.printStackTrace();
//                    } finally {
//                        if (image != null) {
//                            image.close();
//                        }
//                    }
//                }

//                private void save(byte[] bytes) throws IOException {
//                private void save(byte[] bytes, File fFile) throws IOException {
//                    OutputStream output = null;
//                    try {
////                        output = new FileOutputStream(file);
//                        output = new FileOutputStream(fFile);
//                        output.write(bytes);
//                    } finally {
//                        if (null != output) {
//                            output.close();
//                        }
//                    }
//                }
//            };
//            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
            mImageReader.setOnImageAvailableListener(mOnImageAvailableListener,mBackgroundHandler);

//20200108 end
            CameraCaptureSession.CaptureCallback CaptureCallback = new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
//20200110
//20200113
                    Log.d(TAG, "request-before: " + request.get(CaptureRequest.SENSOR_SENSITIVITY));
                    Log.d(TAG, "result-before: " + result.get(CaptureResult.SENSOR_SENSITIVITY));
//20200113 end
                    super.onCaptureCompleted(session, request, result);
//20200110 end
//20200113
                    Log.d(TAG, "request-after: " + request.get(CaptureRequest.SENSOR_SENSITIVITY));
                    Log.d(TAG, "result-after: " + result.get(CaptureResult.SENSOR_SENSITIVITY));
//20200113 end
//20200219 comment Toast
//                    showToast("Saved: " + mFile);
                    Log.i(TAG, mFile.toString());
                    unlockFocus();
                }
            };

            mCaptureSession.stopRepeating();
            mCaptureSession.abortCaptures();

//20200201
            MediaActionSound sound = new MediaActionSound();
            Log.d("C2BF", "sound.play(MediaActionSound.SHUTTER_CLICK) before");
            sound.play(MediaActionSound.SHUTTER_CLICK);
            Log.d("C2BF", "sound.play(MediaActionSound.SHUTTER_CLICK) after");
//20200201 end

            Log.d("C2BF", "mCaptureSession.capture(captureBuilder.build(), CaptureCallback, null)");
            CaptureRequest mCaptRequest;
            try {
                Log.d("C2BF", "mCaptRequest = captureBuilder.build())");
                mCaptRequest = captureBuilder.build();
                Log.d("C2BF", "mCaptureSession.capture(mCaptRequest, CaptureCallback, null)");
                mCaptureSession.capture(mCaptRequest, CaptureCallback, null);
            } catch (NullPointerException ex) {
                Log.e("C2BF:CaptureStillPicture:NPEx", ex.toString());
            }
        } catch (NullPointerException e) {
            Log.e("C2BF:CaptureStillPicture:NPEx", e.toString());
            //todo handle
        } catch (CameraAccessException e) {
            Log.e("C2BF:CaptureStillPicture:CAEx", e.toString());
            //todo handle
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the JPEG orientation from the specified screen rotation.
     *
     * @param rotation The screen rotation.
     * @return The JPEG orientation (one of 0, 90, 270, and 360)
     */
    private int getOrientation(int rotation) {
        // Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
        // We have to take that into account and rotate JPEG properly.
        // For devices with orientation of 90, we simply return our mapping from ORIENTATIONS.
        // For devices with orientation of 270, we need to rotate the JPEG 180 degrees.
        return (ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360;
    }

    /**
     * Unlock the focus. This method should be called when still image capture sequence is
     * finished.
     */
    private void unlockFocus() {
        try {
            Log.d("C2BF", "unlockFocus");

            // Reset the auto-focus trigger

            Log.d("C2BF:unlockFocus: ", "mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL)");
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);

            Log.d("C2BF:unlockFocus: ", "setAutoFlash(mPreviewRequestBuilder)");
            setAutoFlash(mPreviewRequestBuilder);

            Log.d("C2BF:unlockFocus: ", "mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler)");
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler);
            // After this, the camera will go back to the normal state of preview.
            mState = STATE_PREVIEW;

            Log.d("C2BF:unlockFocus: ", "mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mBackgroundHandler)");
            mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mBackgroundHandler);
//        } catch (CameraAccessException e) {
        } catch (Exception e) {
            Log.e("C2BF:unlockFocus:Ex", e.toString());
            //todo handle
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnPicture: {
//20200110
                final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
                long endTime = System.currentTimeMillis() + PIC_DELAY_SECONDS * 1000;
                while (System.currentTimeMillis() < endTime) {
                    try {
                        sleep(1000);
                        tg.startTone(ToneGenerator.TONE_PROP_BEEP);
                    } catch (InterruptedException e) {
                        Log.e("C2BF:onClick:btnPicture:IntExEx", e.toString());
                        //todo handle
                        e.printStackTrace();
                    }
                }
//20200110 end
                bWaitReview = true;
                setControls();     // reviewing

                takePicture();
                break;
            }

            case R.id.btn_accept: {
                try {
                    CameraActivity activity = (CameraActivity) getActivity();
                    activity.AcceptPicture(mFile.getName());
                    bWaitReview = false;
//                    setControls();     //not reviewing
                } catch (NullPointerException e) {
                    Log.e("C2BF:onClick:brnAccept:NPEx", e.toString());
                    //todo handle
                }
                break;
            }
            case R.id.btn_reject: {
                try {
                    CameraActivity activity = (CameraActivity) getActivity();
                    activity.RejectPicture();
                    bWaitReview = false;
//                    setControls();     //not reviewing
                } catch (NullPointerException e) {
                    Log.e("C2BF:onClick:btnReject:NPEx", e.toString());
                    //todo handle
                }
                break;
            }
//            case R.id.imageView: {
//
//                mImageView.setRotation(mImageView.getRotation() + 90);  //rotate 90 degrees
//                break;
//            }
        }
    }

    private void setAutoFlash(CaptureRequest.Builder requestBuilder) {
        Log.d("C2BF", "setAutoFlash");

        if (mFlashSupported) {
            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
        }
    }

    /**
     * Saves a JPEG {@link Image} into the specified {@link File}.
     */
    private static class ImageSaver implements Runnable {
        /**
         * The JPEG image
         */
        private final Image mImage;
        /**
         * The file we save the image into.
         */
        private final File mFile;

        ImageSaver(Image image, File file) {
            mImage = image;
            mFile = file;
        }

        @Override
        public void run() {
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            FileOutputStream output = null;
            try {
                output = new FileOutputStream(mFile);
                output.write(bytes);
            } catch (IOException e) {
                Log.e("C2BF:run:IOEx", e.toString());
                //todo handle
                e.printStackTrace();
            } finally {
                mImage.close();
//20200110
//                loadImageFromStorage(mFile);
//20200110 end
                if (null != output) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        Log.e("C2BF:run:output.close:IOEx", e.toString());
                        //todo handle
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }

    /**
     * Shows an error message dialog.
     */
    public static class ErrorDialog extends DialogFragment {

        private static final String ARG_MESSAGE = "message";

        public static ErrorDialog newInstance(String message) {
            ErrorDialog dialog = new ErrorDialog();
            Bundle args = new Bundle();
            args.putString(ARG_MESSAGE, message);
            dialog.setArguments(args);
            return dialog;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            String strMsg = "message";
            final Activity activity = getActivity();
            return new AlertDialog.Builder(activity)

                    //todo Warning:(1339, 48) Method invocation 'getString' may produce 'NullPointerException'
                    .setMessage(getArguments().getString(strMsg))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            try {
                                //todo Warning:(1343, 38) Method invocation 'finish' may produce 'NullPointerException'
                                activity.finish();
                            } catch (NullPointerException e) {
                                Log.e("C2BF:onCreateDialog:NPEx", e.toString());
                                //todo handle
                            }
                        }
                    })
                    .create();
        }

    }

    /**
     * Shows OK/Cancel confirmation dialog about camera permission.
     */
    public static class ConfirmationDialog extends DialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Fragment parent = getParentFragment();
            return new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.request_permission)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            try {
                                parent.requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                            } catch (NullPointerException e) {
                                Log.e("C2BF:onCreateDialog:onClick:NPEx", e.toString());
                                //todo handle
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    try {
                                        Activity activity = parent.getActivity();
                                        if (activity != null) {
                                            activity.finish();
                                        }
                                    } catch (NullPointerException e) {
                                        Log.e("C2BF:newDialogInterface:onClick:NPEx", e.toString());
                                        //todo handle
                                    }
                                }
                            })
                    .create();
        }
    }

}
