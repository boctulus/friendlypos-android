package cl.friendlypos.mypos.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

public class BitmapUtils {

    public static Bitmap scaleBitmap(Bitmap bitmap, float scale) {
        if (bitmap == null) {
            return null;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
    }

    public static byte rgb2Gray(int r, int g, int b, boolean colored) {
        return rgb2Gray(r, g, b, colored, false);
    }

    public static byte rgb2Gray(int r, int g, int b) {
        return rgb2Gray(r, g, b, false, false);
    }

    public static byte rgb2Gray(int r, int g, int b, boolean colored, boolean reverse) {
        int color = colored ? 140: 95;
        double gray = 0.29900 * r + 0.58700 * g + 0.11400 * b;
        return (reverse ? ((int) gray > color)
                : ((int) gray < color)) ? (byte) 1 : (byte) 0;
    }

    public static Bitmap getPicFromBytes(byte[] bytes, BitmapFactory.Options opts) {
        if (bytes != null)
            if (opts != null)
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length,
                        opts);
            else
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return null;
    }

    public static Bitmap stringToBitmapForLCD(String toShow) {
        TextPaint textPaint = new TextPaint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(24);
        textPaint.setUnderlineText(false);
        textPaint.setTextScaleX(1.0f);

        StaticLayout layout = new StaticLayout(toShow, textPaint, 128, Layout.Alignment.ALIGN_CENTER, 0.0f, 0.0f, false);
        Bitmap bitmap = Bitmap.createBitmap(128, 56, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.translate(1, 0);
        canvas.drawColor(Color.WHITE);
        layout.draw(canvas);
        return bitmap;
    }
}
