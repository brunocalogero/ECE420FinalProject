/* Copyright (C) 2016. All rights reserved.

 This software may be distributed and modified under the terms of the GNU
 General Public License version 2 (GPL2) as published by the Free Software
 Foundation and appearing in the file GPL2.TXT included in the packaging of
 this file. Please note that GPL2 Section 2[b] requires that all works based
 on this software must also be made publicly available under the terms of
 the GPL2 ("Copyleft").

modified by brunoc2 and dsgonza2, original: Kristian Sloth Lauszus
*/

package com.bruno.facerecognitionapp;

import android.os.AsyncTask;
import android.os.Bundle;

import org.opencv.core.Mat;

// All computations is done in an asynchronous task, so we do not skip any frames
class NativeMethods {
    static void loadNativeLibraries() {
        System.loadLibrary("face-lib");
    }

    static class TrainFacesTask extends AsyncTask<Void, Void, Void> {
        private final Mat images, classes;

        /**
         * Constructor used for Eigenfaces.
         * @param images Matrix containing all images as column vectors.
         */
        TrainFacesTask(Mat images) {
            this(images, null);
        }

        /**
         * Constructor used for Fisherfaces.
         * @param images  Matrix containing all images as column vectors.
         * @param classes Vector containing classes for each image.
         */
        TrainFacesTask(Mat images, Mat classes) {
            this.images = images;
            this.classes = classes;
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (classes == null)
                TrainFaces(images.getNativeObjAddr(), 0); // Train Eigenfaces
            else
                TrainFaces(images.getNativeObjAddr(), classes.getNativeObjAddr()); // Train Fisherfaces
            return null;
        }
    }

    static class MeasureDistTask extends AsyncTask<Mat, Void, Bundle> {
        static final String MIN_DIST_FLOAT = "minDist";
        static final String MIN_DIST_INDEX_INT = "minDistIndex";
        static final String DIST_FACE_FLOAT = "distFace";

        private final Callback callback;
        private final boolean useEigenfaces;

        interface Callback {
            void onMeasureDistComplete(Bundle bundle);
        }

        MeasureDistTask(boolean useEigenfaces, Callback callback) {
            this.useEigenfaces = useEigenfaces;
            this.callback = callback;
        }

        @Override
        protected Bundle doInBackground(Mat... mat) {
            float[] minDist = new float[] { -1 };
            int[] minDistIndex = new int[1];
            float[] faceDist = new float[1];
            MeasureDist(mat[0].getNativeObjAddr(), minDist, minDistIndex, faceDist, useEigenfaces);
            Bundle bundle = new Bundle();
            bundle.putFloat(MIN_DIST_FLOAT, minDist[0]);
            bundle.putInt(MIN_DIST_INDEX_INT, minDistIndex[0]);
            bundle.putFloat(DIST_FACE_FLOAT, faceDist[0]);
            return bundle;
        }

        @Override
        protected void onPostExecute(Bundle bundle) {
            callback.onMeasureDistComplete(bundle);
        }
    }

    /**
     * Train faces recognition.
     * @param addrImages    Address for matrix containing all images as column vectors.
     * @param addrClasses   Address for vector containing classes for each image.
     *                      This must be a incrementing list starting at 1.
     *                      If set to NULL, then Eigenfaces will be used.
     *                      If this is set, then Fisherfaces will be used.
     */
    private static native void TrainFaces(long addrImages, long addrClasses);

    /**
     * Measure euclidean distance between the weight of the image compared to all weights.
     * @param addrImage     Vector containing the image.
     * @param useEigenfaces Set to true if Eigenfaces are used. If set to false,
     *                      then Fisherfaces will be used.
     * @return              Returns an array of floats of all distances.
     */
    private static native void MeasureDist(long addrImage, float[] minDist, int[] minDistIndex, float[] faceDist, boolean useEigenfaces);
}
