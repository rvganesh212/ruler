package com.example.zhixuanlai.ruler;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

/*
http://developer.android.com/training/custom-views/making-interactive.html
*/

/**
 * Screen size independent ruler view.
 */
public class RulerView extends View {

    private static final float PADDING = 40;
    private static final float TEXT_SIZE = 40;
    private static final float STROKE_WIDTH = 4;
    private static final String TAG = "RulerView";

    private DisplayMetrics dm;

    public Paint paint;
    public Paint backgroundPaint;

    public SparseArray<PointF> activePointers;

    public RulerView(Context context) {
        this(context, null);
        setup();
    }

    public RulerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public void setup() {
        dm = getResources().getDisplayMetrics();

        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(STROKE_WIDTH);
        paint.setTextSize(TEXT_SIZE);
        paint.setAntiAlias(true);

        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.YELLOW);

        activePointers = new SparseArray<>();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int pointerIndex = event.getActionIndex();
        int pointerId = event.getPointerId(pointerIndex);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN: {
                PointF position = new PointF(event.getX(pointerIndex), event.getY(pointerIndex));
                activePointers.put(pointerId, position);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                int numberOfPointers = event.getPointerCount();
                for (int i = 0; i < numberOfPointers; i++) {
                    PointF point = activePointers.get(event.getPointerId(i));
                    if (point == null) {
                        continue;
                    }
                    point.x = event.getX(i);
                    point.y = event.getY(i);
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL: {
                activePointers.remove(pointerId);
                break;
            }
        }
        invalidate();

        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawBackground(canvas);
        drawActivePointers(canvas);
    }



    private void drawBackground(Canvas canvas) {
        canvas.drawPaint(backgroundPaint);

        float width = getWidth();
        float height = getHeight();

        float yIncrement = dm.ydpi / 4;
        float y = TEXT_SIZE;
        int i = 0;
        while (y < height) {


            i ++;
            y += yIncrement;
        }
        float precisionInches = 0.25f;
        float precisionDp = precisionInches * dm.ydpi;

        float numberOfLines = precisionDp;

        for (int i = 0; i < numberOfLines; i++) {
            float lineLength;
            if (i % 4 == 0) {
                lineLength = 100;
            } else if (i % 2 == 0) {
                lineLength = 75;
            } else {
                lineLength = 50;
            }


            float p1x = width - lineLength;
            float p2x = width;

            float y = paddingTop + i * precisionDp;

            float p1y = y;
            float p2y = p1y;

            // draw lines
            canvas.drawLine(p1x, p1y, p2x, p2y, paint);


            // draw label
            if (i % 4 == 0) {
                String text = i / 4 + "";
                canvas.drawText(text, p1x - paint.measureText(text), p1y + TEXT_SIZE / 2, paint);
            }
        }

    }

    private void drawActivePointers(Canvas canvas) {
        float width = getWidth();

        PointF topPoint = null;
        PointF bottomPoint = null;
        for (int size = activePointers.size(), i = 0; i < size; i++) {
            PointF point = activePointers.valueAt(i);
            if (point == null) {
                continue;
            }
            if (topPoint == null || topPoint.y < point.y) {
                topPoint = point;
            }
            if (bottomPoint == null || bottomPoint.y > point.y) {
                bottomPoint = point;
            }
            canvas.drawCircle(point.x, point.y, 69, paint);
        }

        if (topPoint != null) {
            canvas.drawLine(topPoint.x, topPoint.y, width, topPoint.y, paint);
        }

        if (bottomPoint != null) {
            canvas.drawLine(bottomPoint.x, bottomPoint.y, width, bottomPoint.y, paint);
        }

        float distance = 0;
        if (topPoint != null && bottomPoint != null) {
            distance = Math.abs(topPoint.y - bottomPoint.y);
        }
        String text = "distance: " + distance + " inches";
        canvas.drawText(text, 100, 100 + TEXT_SIZE / 2, paint);
    }

}
