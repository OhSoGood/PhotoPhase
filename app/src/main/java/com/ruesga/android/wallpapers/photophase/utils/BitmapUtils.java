/*
 * Copyright (C) 2015 Jorge Ruesga
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ruesga.android.wallpapers.photophase.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * A helper class for deal with Bitmaps
 */
public class BitmapUtils {

    /**
     * Method that decodes a bitmap
     *
     * @param bitmap The bitmap buffer to decode
     * @return Bitmap The decoded bitmap
     */
    public static Bitmap decodeBitmap(InputStream bitmap) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferQualityOverSpeed = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inDither = false;
        return BitmapFactory.decodeStream(bitmap, null, options);
    }

    /**
     * Method that decodes a bitmap
     *
     * @param file The bitmap file to decode
     * @param reqWidth The request width
     * @param reqHeight The request height
     * @return Bitmap The decoded bitmap
     */
    @SuppressWarnings("deprecation")
    public static Bitmap decodeBitmap(File file, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);

        // Calculate inSampleSize (use 1024 as maximum size, the minimum supported
        // by all the gles20 devices)
        options.inSampleSize = calculateBitmapRatio(
                                    options,
                                    Math.min(reqWidth, 1024),
                                    Math.min(reqHeight, 1024));

        // Decode the bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        options.inPreferQualityOverSpeed = false;
        // Deprecated, but still valid for KitKat and lower apis
        options.inPurgeable = true;
        options.inInputShareable = true;
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        if (bitmap == null) {
            return null;
        }

        // Test if the bitmap has exif format, and decode properly
        Bitmap out = decodeExifBitmap(file, bitmap);
        if (!out.equals(bitmap)) {
            bitmap.recycle();
        }
        return out;
    }

    /**
     * Method that decodes an Exif bitmap
     *
     * @param file The file to decode
     * @param src The bitmap reference
     * @return Bitmap The decoded bitmap
     */
    private static Bitmap decodeExifBitmap(File file, Bitmap src) {
        try {
            // Try to load the bitmap as a bitmap file
            ExifInterface exif = new ExifInterface(file.getAbsolutePath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            if (orientation == ExifInterface.ORIENTATION_UNDEFINED
                    || orientation == ExifInterface.ORIENTATION_NORMAL) {
                return src;
            }
            Matrix matrix = new Matrix();
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                matrix.postRotate(90);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                matrix.postRotate(180);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                matrix.postRotate(270);
            } else if (orientation == ExifInterface.ORIENTATION_FLIP_HORIZONTAL) {
                matrix.setScale(-1, 1);
                matrix.postTranslate(src.getWidth(), 0);
            } else if (orientation == ExifInterface.ORIENTATION_FLIP_VERTICAL) {
                matrix.setScale(1, -1);
                matrix.postTranslate(0, src.getHeight());
            }
            // Rotate the bitmap
            return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
        } catch (IOException e) {
            // Ignore
        }
        return src;
    }

    /**
     * Method that calculate the bitmap size prior to decode
     *
     * @param options The bitmap factory options
     * @param reqWidth The request width
     * @param reqHeight The request height
     * @return int The picture ratio
     */
    private static int calculateBitmapRatio(Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * Check if the bitmap is a power of two
     *
     * @param bitmap The bitmap to check
     * @return boolean if the size is power of two
     */
    public static boolean isPowerOfTwo(Bitmap bitmap){
        return isPowerOfTwo(bitmap.getWidth(), bitmap.getHeight());
    }

    /**
     * Check if the width and height are power of two
     *
     * @param w Width
     * @param h Height
     * @return boolean if the size is power of two
     */
    public static boolean isPowerOfTwo(int w, int h){
        return isPowerOfTwo(w) && isPowerOfTwo(h);
    }

    private static boolean isPowerOfTwo(int x) {
        while (((x % 2) == 0) && x > 1) {
            x /= 2;
        }
        return (x == 1);
    }

    /**
     * Return the nearest upper power of two size
     *
     * @param v The original value
     * @return The nearest upper power of two
     */
    public static int calculateUpperPowerOfTwo(int v) {
        v--;
        v |= v >>> 1;
        v |= v >>> 2;
        v |= v >>> 4;
        v |= v >>> 8;
        v |= v >>> 16;
        v++;
        return v;
    }
}
