package com.usv.rqapp.controllers;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;

import id.zelory.compressor.Compressor;

public class ImageController {

    private static Bitmap compressor;
    private Context context;

    public ImageController(Context context) {
        this.context = context;
    }


    /**
     * @param uri
     * @return
     */
    public String getFileExtension(Uri uri) {
        ContentResolver cR = context.getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }


    /**
     * @param storageReference
     * @param imageUri
     */
    public void uploadFile(StorageReference storageReference, String imageUri) {
        if (imageUri != null) {
            // storageReference.child();
        } else {
            Toast.makeText(context, "Nici o imagine selectată", Toast.LENGTH_LONG).show();
        }
    }


    /**
     * @param eventImage
     * @return
     */
    public byte[] compressImage(Bitmap eventImage) {
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {

            Bitmap bitmapImage = eventImage;
            int nh = (int) (bitmapImage.getHeight() * (720.0 / bitmapImage.getWidth()));
            Bitmap scaled = Bitmap.createScaledBitmap(bitmapImage, 720, nh, true);
            byteArrayOutputStream = new ByteArrayOutputStream();
            if (sizeOf(eventImage) < 4568224) {
                scaled.compress(Bitmap.CompressFormat.WEBP, 25, byteArrayOutputStream);
            } else {
                scaled.compress(Bitmap.CompressFormat.WEBP, 5, byteArrayOutputStream);
            }


        } catch (Exception e) {
            e.getMessage();
        }


        return byteArrayOutputStream.toByteArray();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    protected int sizeOf(Bitmap data) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
            return data.getRowBytes() * data.getHeight();
        } else {
            return data.getByteCount();
        }
    }
}
