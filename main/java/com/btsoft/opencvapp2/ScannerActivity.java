package com.btsoft.opencvapp2;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.barteksc.pdfviewer.PDFView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import static com.btsoft.opencvapp2.PDFViewer.pdfView;

public class ScannerActivity extends Fragment implements SurfaceHolder.Callback, CameraBridgeViewBase.CvCameraViewListener2 {

    private View view;
    private SurfaceHolder sfHolder;
    private static CameraBridgeViewBase mOpenCvCameraView;
    private String TAG = "ScannerActivity";
    private Button takeShot;

    private Mat mRgba;
    private Mat mRgbaView;
    private Mat mRgbaCrop;
    private Mat mGray;
    private Mat mOut;
    private Rect cropRect;
    private Mat outputImg = new Mat();


    private FeatureDetector featureDetector = FeatureDetector.create(FeatureDetector.ORB);
    private DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
    private DescriptorExtractor descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.ORB);

    private int max_match = 0;
    private int best_image = 0;

    private Bitmap.Config conf = Bitmap.Config.ARGB_8888;

    private Mat mBlur = new Mat();
    private Mat mCanny = new Mat();
    private Mat hierarchy = new Mat();

    private ProgressBar pgsBar;

    private int w, h;
    private int savedContour;

    public static BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(MainActivity.getContextOfApp()) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Log.i("BaseLoaderCallback", "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    try {
                        initializeOpenCVDependencies();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    super.onManagerConnected(status);
                    break;

            }
        }
    };



    private static void initializeOpenCVDependencies() throws IOException {
        mOpenCvCameraView.enableView();
    }

    public ScannerActivity() throws IOException {
    }

    private AssetManager assets = MainActivity.getContextOfApp().getAssets();
    private int databaseLength = assets.list("bookCover_db").length;

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_main, container, false);

        pgsBar = view.findViewById(R.id.loading_bar);

        mOpenCvCameraView = view.findViewById(R.id.scannerFrame);
        takeShot = view.findViewById(R.id.buttonTakePicture);
        sfHolder = mOpenCvCameraView.getHolder();
        sfHolder.addCallback(this);
        sfHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_BACK);
        takeShot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(savedContour != -1) {
                    pgsBar.setVisibility(View.VISIBLE);
                    try {
                        feature_homography();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        return view;
    }



    @Override
    public void onCameraViewStopped() {

    }

    public void feature_homography() throws IOException {
        mRgbaCrop = mRgbaCrop.submat(cropRect);
        mOpenCvCameraView.disableView();
        // LINK POINTS
        Scalar RED = new Scalar(255, 0, 0);
        Scalar GREEN = new Scalar(0, 255, 0);

        // READ SRC IMAGE AND COMPUTE KEYPOINTS
        Mat src_img = mRgbaCrop.clone();
        Imgproc.cvtColor(src_img, src_img, Imgproc.COLOR_BGRA2GRAY);
        Imgproc.equalizeHist(src_img, src_img);

        MatOfKeyPoint keys = new MatOfKeyPoint();
        Mat dess = new Mat();

        Matrix transition = new Matrix();
        transition.preRotate(90);
        Bitmap src_img_bmp = Bitmap.createBitmap((int)src_img.size().width, (int)src_img.size().height, conf);
        Utils.matToBitmap(src_img, src_img_bmp);
        src_img_bmp = Bitmap.createBitmap(src_img_bmp, 0, 0, src_img_bmp.getWidth(),
                src_img_bmp.getHeight(), transition, true);
        Utils.bitmapToMat(src_img_bmp, src_img);

        featureDetector.detect(src_img, keys, dess);
        descriptorExtractor.compute(src_img, keys, dess);

        max_match = 0;
        best_image = 0;

        for(int img_idx = 0; img_idx < databaseLength; img_idx++) {
            InputStream is = assets.open("bookCover_db/" + assets.list("bookCover_db")[img_idx]);
            Bitmap dest_img_bmp = BitmapFactory.decodeStream(is);

            Mat dest_img = new Mat();
            Utils.bitmapToMat(dest_img_bmp, dest_img);
            if(src_img.size().width < dest_img.size().width){
                Imgproc.resize(dest_img, dest_img, new Size(src_img.size().width, src_img.size().height));
            }
            Imgproc.cvtColor(dest_img, dest_img, Imgproc.COLOR_BGRA2GRAY);
            Imgproc.equalizeHist(dest_img, dest_img);
            for(int rotate = 0; rotate < 4; rotate++) {
                // READ DEST IMAGE AND COMPUTE KEYPOINTS
                Mat rotMat;
                Point center = new Point(dest_img.rows() / 2, dest_img.cols() / 2);
                rotMat = Imgproc.getRotationMatrix2D(center, 30 * rotate, 1);
                Imgproc.warpAffine(dest_img, dest_img, rotMat, dest_img.size());

                MatOfKeyPoint keyd = new MatOfKeyPoint();
                Mat desd = new Mat();
                featureDetector.detect(dest_img, keyd, desd);
                descriptorExtractor.compute(dest_img, keyd, desd);

                // MATCH POINTS
                List<MatOfDMatch> knnMatches = new ArrayList<>();
                matcher.knnMatch(desd, dess, knnMatches, 2);

                List<DMatch> good_matches = new ArrayList<>();

                for(int i = 0; i < knnMatches.size(); i++){
                    if(knnMatches.get(i).rows() > 1) {
                        DMatch[] matches = knnMatches.get(i).toArray();
                        if(matches[0].distance < 0.89f * matches[1].distance) {
                            good_matches.add(matches[0]);
                        }
                    }
                }

                // PRINTING
                MatOfDMatch goodMatches = new MatOfDMatch();
                goodMatches.fromList(good_matches);

                outputImg = new Mat();
                MatOfByte drawnMatches = new MatOfByte();

                if(good_matches.size() >= 10) {
                    List<Point> objList = new ArrayList<>();
                    List<Point> sceneList = new ArrayList<>();

                    List<KeyPoint> kpd_objectList = keyd.toList();
                    List<KeyPoint> kps_objectList = keys.toList();

                    for(int i = 0; i < good_matches.size(); i++){
                        objList.add(kpd_objectList.get(good_matches.get(i).queryIdx).pt);
                        sceneList.add(kps_objectList.get(good_matches.get(i).trainIdx).pt);
                    }

                    Mat outputMask = new Mat();
                    MatOfPoint2f objMat = new MatOfPoint2f();
                    objMat.fromList(objList);

                    MatOfPoint2f sceneMat = new MatOfPoint2f();
                    sceneMat.fromList(sceneList);

                    Calib3d.findHomography(objMat, sceneMat, Calib3d.RANSAC, 15, outputMask, 2000, 0.995);
                    LinkedList<DMatch> better_matches = new LinkedList<>();

                    for(int i = 0; i < good_matches.size(); i++) {
                        if(outputMask.get(i, 0)[0] != 0.0)
                            better_matches.add(good_matches.get(i));
                    }

                    MatOfDMatch matches_final_mat = new MatOfDMatch();
                    matches_final_mat.fromList(better_matches);

                    Features2d.drawMatches(dest_img, keyd, src_img, keys,
                            goodMatches, outputImg, GREEN, RED,
                            drawnMatches, Features2d.NOT_DRAW_SINGLE_POINTS);

                    Bitmap outMatch = Bitmap.createBitmap((int)outputImg.size().width,
                            (int)outputImg.size().height, conf);
                    Imgproc.cvtColor(outputImg, outputImg, Imgproc.COLOR_BGR2RGB);
                    Utils.matToBitmap(outputImg, outMatch);

                    if(better_matches.size() > max_match){
                        max_match = better_matches.size();
                        best_image = img_idx;
                    }

                }
            }
        }
        if(max_match > 40){
            String foundBookName = assets.list("bookCover_db")[best_image].substring(1, assets.list("bookCover_db")[best_image].length() - 4);
            Toast.makeText(MainActivity.getContextOfApp(),
                    max_match + " eslesmeyle kitap " + foundBookName + " olarak tespit edildi!",
                    Toast.LENGTH_LONG).show();
            Intent intent = new Intent(MainActivity.getContextOfApp(), PDFViewer.class);
            Bundle pdf_name = new Bundle();
            pdf_name.putString("name", foundBookName);
            intent.putExtras(pdf_name);
            MainActivity.getContextOfApp().startActivity(intent);
        } else {
            Toast.makeText(MainActivity.getContextOfApp(),
                    "Kitap bulunamadı",
                    Toast.LENGTH_LONG).show();
        }

        outputImg.release();
        dess.release();
        mOpenCvCameraView.enableView();
        pgsBar.setVisibility(View.GONE);
        System.gc();
        System.runFinalization();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
        w = width;
        h = height;
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();

        Imgproc.cvtColor(mRgba, mGray, Imgproc.COLOR_BGRA2GRAY);
        Imgproc.GaussianBlur(mGray, mBlur, new Size(7,7 ), 0);
        Imgproc.adaptiveThreshold(mBlur, mBlur, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 15, 12);
        Imgproc.Canny(mBlur, mCanny, 80, 120);
        Imgproc.GaussianBlur(mCanny, mCanny, new Size(21,21 ), 0);
        Imgproc.threshold(mCanny, mCanny, 20, 255, Imgproc.THRESH_BINARY);
        List<MatOfPoint> contours = new ArrayList<>();

        Imgproc.dilate(mCanny, mCanny, new Mat(), new Point(-1, -1), 1);
        Imgproc.erode(mCanny, mCanny, new Mat(), new Point(-1, -1), 3);

        Imgproc.findContours(mCanny.clone(), contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        Rect[] boundRect = new Rect[contours.size()];
        savedContour = -1;
        double area = 0;
        int object_limit = 5;
        mOut = mGray.clone();
        Collections.sort(contours, new Comparator<MatOfPoint>() {
            @Override
            public int compare(MatOfPoint o1, MatOfPoint o2) {
                double a1 = Imgproc.contourArea(o1);
                double a2 = Imgproc.contourArea(o2);
                if(a1 > a2) {
                    return -1;
                } else if (a2 > a1){
                    return 1;
                } else{
                    return 0;
                }
            }
        });

        if(contours.size() > 5) {
            contours.subList(5, contours.size()).clear();
        }
        mRgbaView = mRgba.clone();

        for(int i = 0; i < contours.size(); i++) {
            MatOfPoint2f contoursPoly = new MatOfPoint2f();
            double epsilon = 0.01 * Imgproc.arcLength(new MatOfPoint2f(contours.get(i).toArray()), true);
            Imgproc.approxPolyDP(new MatOfPoint2f(contours.get(i).toArray()), contoursPoly, epsilon, true);

            if((contoursPoly.size().height >= 3) && (contoursPoly.size().height <= 5)){
                boundRect[i] = Imgproc.boundingRect(new MatOfPoint(contoursPoly.toArray()));
                Imgproc.drawContours(mRgbaView, contours, i, new Scalar(0,255,0), 2);
                Imgproc.drawContours(mGray, contours, i, new Scalar(255,255,255), -1);
                if(area < Imgproc.contourArea(contours.get(i))) {
                    area = Imgproc.contourArea(contours.get(i));
                    savedContour = i;
                }
                object_limit--;
                if(object_limit == 0)
                    break;
            }
        }

        if(savedContour != -1) {
            cropRect = new Rect(boundRect[savedContour].x, boundRect[savedContour].y,boundRect[savedContour].width, boundRect[savedContour].height);
        }

        // Fill inside of tectangle
        Core.absdiff(mGray, mOut, mOut);
        Mat im_th = new Mat();
        Imgproc.threshold(mOut, im_th, 20, 255, Imgproc.THRESH_BINARY_INV);
        Mat im_ff = im_th.clone();
        Imgproc.floodFill(im_ff, new Mat(), new Point(0, 0), new Scalar(0));
        Mat im_ff_inv = new Mat();
        Core.bitwise_not(im_ff, im_ff_inv);
        Core.bitwise_xor(im_th, im_ff_inv, mOut);

        // Crop rect from rgb frame
        mRgbaCrop = new Mat(mRgba.rows(), mRgba.cols(), CvType.CV_8UC3, Scalar.all(0));
        mRgba.copyTo(mRgbaCrop, mOut);

        mRgba.release();
        mGray.release();
        mOut.release();
        mCanny.release();
        mBlur.release();

        System.gc();
        System.runFinalization();

        return mRgbaView;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, MainActivity.getContextOfApp(), ScannerActivity.mLoaderCallback);
    }
}
