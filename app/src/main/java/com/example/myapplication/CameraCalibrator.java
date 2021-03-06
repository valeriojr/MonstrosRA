package com.example.myapplication;

import android.util.Log;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point3;
import org.opencv.core.Size;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class CameraCalibrator {

    interface OnCameraCalibratedListener {
        void OnCameraCalibrated(Mat cameraMatrix, Mat distCoeffs, List<Mat> rvecs, List<Mat> tvecs);
    }

    private int minSamples = 20;
    private int samples;
    private boolean calibrated;
    private final Size patternSize;
    private final List<Mat> objectPoints;
    private final List<Mat> imagePoints;
    private Size imageSize;

    private OnCameraCalibratedListener listener;

    public CameraCalibrator(int minSamples, Size patternSize) {
        calibrated = false;
        samples = 0;
        this.minSamples = minSamples;
        this.patternSize = patternSize;
        // Cria as lista já com a capacidade de MIN_SAMPLES
        objectPoints = new ArrayList<>(minSamples);
        imagePoints = new ArrayList<>(minSamples);

        // Cria um objeto Mat contendo as coordenadas dos pontos no sistema de coordenadas onde a
        // origem é o canto superior esquerdo do primeiro quadrado
        Mat obj = new Mat();
        int squares = (int) patternSize.area();
        int width = (int) patternSize.width;
        Point3 point = new Point3();
        for (int i = 0; i < squares; i++) {
            point.x = (float) i / width;
            point.y = (float) i % width;
            obj.push_back(new MatOfPoint3f(point));
        }

        /*
        Adiciona minSamples obj em objectPoints (Os objectPoints são fixos quando o tabuleiro é a
        origem do sistema de coordenadas)
         */
        for (int i = 0; i < minSamples; i++) {
            objectPoints.add(obj);
        }

    }

    private void calibrate() {
        if (listener != null) {
            Mat cameraMatrix = new Mat();
            Mat distCoeffs = new Mat();
            List<Mat> rvecs = new ArrayList<>(samples);
            List<Mat> tvecs = new ArrayList<>(samples);


            Log.d("Calibrator", "Calibrating camera...");
            Calib3d.calibrateCamera(objectPoints, imagePoints, imageSize, cameraMatrix, distCoeffs,
                    rvecs, tvecs);

            Log.d("Calibrator", "Camera calibrated!");

            calibrated = true;

            listener.OnCameraCalibrated(cameraMatrix, distCoeffs, rvecs, tvecs);
        }
    }


    public boolean isCalibrated() {
        return calibrated;
    }

    public void setOnCameraCalibratedListener(OnCameraCalibratedListener listener) {
        this.listener = listener;
    }

    public boolean tryFindPattern(Mat image, boolean drawPattern) throws Exception {
        if (false && calibrated || samples > minSamples)
            return false;
        else if(samples == minSamples) {
            imagePoints.remove(0);
            samples--;
        }
        MatOfPoint2f corners = new MatOfPoint2f();

        if (imageSize == null) {
            imageSize = image.size();
        } else if (!image.size().equals(imageSize)) {
            String format = "Image size (%.0fx%.0f) is different from previous images (%.0fx%.0f)";
            throw new Exception(String.format(Locale.ROOT, format, image.size().width,
                    image.size().height, imageSize.width, imageSize.height));
        }

        boolean patternWasFound = Calib3d.findChessboardCorners(image, patternSize, corners);

        if (patternWasFound) {
            samples++;
            imagePoints.add(corners);

            if (samples == minSamples) {
                calibrate();
            }

            if (drawPattern) {
                Calib3d.drawChessboardCorners(image, patternSize, corners, true);
            }
        }

        return patternWasFound;
    }

    public int getSamples() {
        return samples;
    }

    public int getMinSamples(){
        return minSamples;
    }
}
