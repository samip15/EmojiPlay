package com.example.emojiplay;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BitmapUtils {

    private final static String FILE_PROVIDER_AUTHORITY = "com.example.android.fileprovider";

    //==========================    Image Functions  =========================

    /**
     * Helps to create a temporary file for us
     */
    public static File createTempImageFile(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat(
                "yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalCacheDir();

        return File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
    }

    /**
     * Delete the given image file
     * @param context
     * @param imagePath
     * @return
     */

    public static boolean deleteImageFile(Context context,String imagePath){
        // get image file
        File imageFile = new File(imagePath);
        boolean deleted = imageFile.delete();
        if (!deleted){
            String errorMessage = context.getString(R.string.error);
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
        }
        return deleted;
    }

    /**
     * Helps to save the image in public directory
     * @param context
     * @param image
     * @return
     */

    public static String saveImage(Context context,Bitmap image){
        String savedImagePath = null;
        // location
        String timeStamp = new SimpleDateFormat(
                "yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory
                (Environment.DIRECTORY_PICTURES)+"/EmojiiPlay");
        boolean success = true;
        if (storageDir.exists()){
            success = storageDir.mkdirs();
        }
        if (success){
            File imageFile = new File(storageDir,imageFileName);
            savedImagePath = imageFile.getAbsolutePath();
            try {
                OutputStream fOut = new FileOutputStream(imageFile);
                image.compress(Bitmap.CompressFormat.JPEG,100,fOut);
                fOut.close();
            }catch (Exception e){
                e.printStackTrace();
            }

            // add to gallery

            galleryAddPic(context,savedImagePath);

            // save message
            String savedMsg = context.getString(R.string.saved_message,savedImagePath);
            Toast.makeText(context, savedMsg, Toast.LENGTH_SHORT).show();
        }
        return savedImagePath;
    }

    /**
     * helps system access our new photo in the system gallery app
     * @param context
     * @param imagePath
     */

    private static void galleryAddPic(Context context,String imagePath){
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(imagePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

    /**
     * helps to share the image file
     * @param context
     * @param imagePath
     */

    public static void shareImage(Context context,String imagePath){
        File imageFile = new File(imagePath);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        Uri photoUri = FileProvider.getUriForFile(context,FILE_PROVIDER_AUTHORITY,imageFile);
        shareIntent.putExtra(Intent.EXTRA_STREAM,photoUri);
        context.startActivity(shareIntent);
    }



    /**
     * Re samples the captured photo and makes it fit to the screen for better memory usage.
     *
     * @param context
     * @param imagePath
     * @return
     */
    public static Bitmap resamplePic(Context context, String imagePath) {

        // Get device screen size information
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        manager.getDefaultDisplay().getMetrics(metrics);

        int targetH = metrics.heightPixels;
        int targetW = metrics.widthPixels;

        // Get the dimensions of the original bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        return BitmapFactory.decodeFile(imagePath);
    }


}
