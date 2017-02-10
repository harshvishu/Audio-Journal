package com.brotherpowers.audiojournal.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.SparseArray;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by harsh_v on 10/27/16.
 */

public class FileUtils {
    public static FileUtils sharedInstance = new FileUtils();

    public enum Type {
        UNKNOWN(0, "file", ""), IMAGE(1, "img", "image"), AUDIO(2, "rec", "audio");

        public final int value;
        public final String extension;
        public final String path;

        Type(int value, String extension, String path) {
            this.value = value;
            this.extension = extension;
            this.path = path;
        }

        private static final SparseArray<Type> ARRAY = new SparseArray<>(values().length);

        static {
            for (Type type : values()) {
                ARRAY.append(type.value, type);
            }
        }

        public static Type valueOf(int value) {
            return ARRAY.get(value);
        }

    }

    public File getFile(@NonNull Type fileType, @Nullable String name, Context context) {
        if (TextUtils.isEmpty(name)) {
            name = String.valueOf(System.currentTimeMillis());
        }
        return new File(getRoot(context, fileType), name);
    }

    private File getRoot(Context context, Type fileType) {
        //noinspection ConstantConditions
        File file = new File(context.getExternalFilesDir(null), fileType.path);
        if (!file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.mkdirs();
        }
        return file;
    }

    public File saveImageFile(Context context, File tempFile) throws Exception {
        return saveImageFile(context, tempFile, null);
    }


    @NonNull
    public File saveImageFile(Context context, File tempFile, @Nullable String newFileName) throws Exception {
        if (TextUtils.isEmpty(newFileName)) {
            newFileName = String.valueOf(System.currentTimeMillis());
        }

        File newFile = getFile(Type.IMAGE, newFileName, context);
        String tempPath = tempFile.getPath();
        int angle = 0;

        ExifInterface ei = new ExifInterface(tempPath);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                angle = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                angle = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                angle = 270;
                break;
        }

        Bitmap bitmap = BitmapFactory.decodeFile(tempPath);
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        FileOutputStream fos = new FileOutputStream(newFile);
        Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true)
                .compress(Bitmap.CompressFormat.JPEG, 100, fos);
        fos.flush();
        fos.close();
        bitmap.recycle();

        return newFile;
    }

    public String generateMD5(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(Constants.MD5);
        InputStream inputStream = new FileInputStream(file);

        byte[] buffer = new byte[8192];
        int read;

        while ((read = inputStream.read(buffer)) > 0) {
            md.update(buffer, 0, read);
        }
        byte[] md5sum = md.digest();
        BigInteger bigInteger = new BigInteger(1, md5sum);
        String output = bigInteger.toString(16);
        // Fill to 32 chars
        output = String.format("%32s", output).replace(' ', '0');
        return output;

    }

    public static short[] getAudioSamples(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        byte[] data;
        try {
            data = IOUtils.toByteArray(is);
        } finally {
            is.close();
        }

        ShortBuffer sb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
        short[] samples = new short[sb.limit()];
        sb.get(samples);
        return samples;
    }
}
