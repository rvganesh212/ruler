package com.example.zhixuanlai.ruler;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import java.util.Iterator;

/*
Documentation referenced:

http://developer.android.com/training/custom-views/index.html
http://developer.android.com/training/custom-views/create-view.html
http://developer.android.com/training/custom-views/custom-drawing.html
http://developer.android.com/training/custom-views/making-interactive.html
http://developer.android.com/training/custom-views/optimizing-view.html

http://developer.android.com/reference/android/graphics/Canvas.html
http://developer.android.com/reference/android/graphics/Paint.html
*/
/**
 * Screen size independent ruler view.
 */

public class RulerView extends View {

    private Unit unit;

    private DisplayMetrics dm;
    private SparseArray<PointF> activePointers;

    private Paint scalePaint;
    private Paint labelPaint;
    private Paint backgroundPaint;
    private Paint pointerPaint;

    private float guideScaleTextSize;
    private float graduatedScaleWidth;
    private float graduatedScaleBaseLength;
    private int scaleColor;

    private float labelTextSize;
    private String defaultLabelText;
    private int labelColor;
    private boolean vertical;

    private int backgroundColor;

    private float pointerRadius;
    private float pointerStrokeWidth;
    private int pointerColor;
    private Typeface type;

    public void setDefaultLabelText(String defaultLabelText) {
        this.defaultLabelText = defaultLabelText;
    }

    /**
     * Creates a new RulerView.
     */

    public RulerView(Context context) {
        this(context, null);
        type = Typeface.createFromAsset(context.getAssets(), "fonts/iransans.ttf");
    }


    public RulerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        type = Typeface.createFromAsset(context.getAssets(), "fonts/iransans.ttf");
    }

    public RulerView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
        type = Typeface.createFromAsset(context.getAssets(), "fonts/iransans.ttf");
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RulerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        type = Typeface.createFromAsset(context.getAssets(), "fonts/iransans.ttf");

        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.RulerView, defStyleAttr, defStyleRes);

        guideScaleTextSize = a.getDimension(R.styleable.RulerView_guideScaleTextSize, 50);
        graduatedScaleWidth = a.getDimension(R.styleable.RulerView_graduatedScaleWidth, 10);
        graduatedScaleBaseLength =
                a.getDimension(R.styleable.RulerView_graduatedScaleBaseLength, 100);
        scaleColor = a.getColor(R.styleable.RulerView_scaleColor, 0xFF03070A);

        labelTextSize = a.getDimension(R.styleable.RulerView_labelTextSize, 60);
        defaultLabelText = a.getString(R.styleable.RulerView_defaultLabelText);
        if (defaultLabelText == null) {
            defaultLabelText = "2 fingers :) !!";
        }
        labelColor = a.getColor(R.styleable.RulerView_labelColor, 0xFF03070A);

        backgroundColor = a.getColor(R.styleable.RulerView_backgroundColor, 0xFFFACC31);

        pointerColor = a.getColor(R.styleable.RulerView_pointerColor, 0xFF03070A);
        pointerRadius = a.getDimension(R.styleable.RulerView_pointerRadius, 40);
        pointerStrokeWidth = a.getDimension(R.styleable.RulerView_pointerStrokeWidth, 8);
        vertical = a.getBoolean(R.styleable.RulerView_vertical, true);
        dm = getResources().getDisplayMetrics();
        unit = new Unit(dm.ydpi);
        unit.setType(a.getInt(R.styleable.RulerView_unit, 0));

        a.recycle();

        initRulerView();
    }

    public void setUnitType(int type) {
        unit.type = type;
        invalidate();
    }

    public int getUnitType() {
        return unit.type;
    }

    private void initRulerView() {
        activePointers = new SparseArray();

        scalePaint = new Paint(Paint.EMBEDDED_BITMAP_TEXT_FLAG);
        scalePaint.setStrokeWidth(graduatedScaleWidth);
        scalePaint.setTypeface(type);
        scalePaint.setTextSize(guideScaleTextSize);
        scalePaint.setColor(scaleColor);

        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setTextSize(labelTextSize);
        labelPaint.setColor(labelColor);

        backgroundPaint = new Paint();
        backgroundPaint.setColor(backgroundColor);

        pointerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointerPaint.setColor(pointerColor);
        pointerPaint.setStrokeWidth(pointerStrokeWidth);
        pointerPaint.setStyle(Paint.Style.STROKE);
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        int paddingTop = getPaddingTop();
        int paddingLeft = getPaddingLeft();
        float startX = 0;
        float startY = 0;
        float endX = 0;
        float endY = 0;


        // Draw background.
        canvas.drawPaint(backgroundPaint);

        // Draw scale.
        if (vertical) {
            Iterator<Unit.Graduation> pixelsIterator = unit.getPixelIterator(height - paddingTop);
            while (pixelsIterator.hasNext()) {
                Unit.Graduation graduation = pixelsIterator.next();

                startX = width - graduation.relativeLength * graduatedScaleBaseLength;
                startY = paddingTop + graduation.pixelOffset;
                endX = width;
                endY = startY;
                canvas.drawLine(startX, startY, endX, endY, scalePaint);
                if (graduation.value % 1 == 0) {
                    String text = (int) graduation.value + "";

                    canvas.save();
                    canvas.translate(
                            startX - guideScaleTextSize, startY - scalePaint.measureText(text) / 2);
                    canvas.rotate(90);
                    canvas.drawText(text, 0, 0, scalePaint);
                    canvas.restore();
                }
            }
        } else {
            Iterator<Unit.Graduation> pixelsIterator = unit.getPixelIterator(width - paddingLeft);
            while (pixelsIterator.hasNext()) {
                Unit.Graduation graduation = pixelsIterator.next();
                startX = height;
                startY = (paddingTop + graduation.pixelOffset);
                endX = height - (graduation.relativeLength * graduatedScaleBaseLength);
                endY = startY;
                canvas.drawLine(startY, startX, endY, endX, scalePaint);
                if (graduation.value % 1 == 0) {
                    String text = (int) graduation.value + "";

                    canvas.save();
                    canvas.translate(
                            startY, endX / 2);
                    canvas.rotate(0);
                    canvas.drawText(text, 0, 0, scalePaint);
                    canvas.restore();
                }


            }
        }


        // Draw active pointers.
        PointF topPointer = null;
        PointF bottomPointer = null;
        for (int i = 0, numberOfPointers = activePointers.size(); i < numberOfPointers; i++) {
            PointF pointer = activePointers.valueAt(i);
            if (topPointer == null || topPointer.y < pointer.y) {
                topPointer = pointer;
            }
            if (bottomPointer == null || bottomPointer.y > pointer.y) {
                bottomPointer = pointer;
            }
            canvas.drawArc(
                    pointer.x - pointerRadius,
                    pointer.y - pointerRadius,
                    pointer.x + pointerRadius,
                    pointer.y + pointerRadius,
                    0f,
                    360f,
                    false,
                    pointerPaint);
        }

        if (topPointer != null) {
            canvas.drawLine(
                    topPointer.x + pointerRadius,
                    topPointer.y,
                    width,
                    topPointer.y,
                    pointerPaint);
            canvas.drawLine(
                    bottomPointer.x + pointerRadius,
                    bottomPointer.y,
                    width,
                    bottomPointer.y,
                    pointerPaint);
        }

        // Draw Text label.
        String labelText = defaultLabelText;

        if (topPointer != null) {
            float distanceInPixels = Math.abs(topPointer.y - bottomPointer.y);
            labelText = unit.getStringRepresentation(distanceInPixels / unit.getPixelsPerUnit());
        }
        canvas.drawText(labelText, paddingLeft, paddingTop + labelTextSize, labelPaint);
    }

    @Override
    protected int getSuggestedMinimumWidth() {
        return getWidth();
    }

    @Override
    protected int getSuggestedMinimumHeight() {
        return 200;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int minWidth = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
        int width = Math.max(minWidth, MeasureSpec.getSize(widthMeasureSpec));

        int minHeight = getPaddingBottom() + getPaddingTop() + getSuggestedMinimumHeight();
        int height = Math.max(minHeight, MeasureSpec.getSize(heightMeasureSpec));

        setMeasuredDimension(width, height);
    }

    public class Unit {

        class Graduation {
            float value;
            int pixelOffset;
            float relativeLength;
        }

        public static final int INCH = 0;
        public static final int CM = 1;

        private int type = INCH;
        private float dpi;

        Unit(float dpi) {
            this.dpi = dpi;
        }

        public void setType(int type) {
            if (type == INCH || type == CM) {
                this.type = type;
            }
        }

        public String getStringRepresentation(float value) {
            String suffix = "";
            if (type == INCH) {
                suffix = value > 1 ? "INCH" : "INCHS";
            } else if (type == CM) {
                suffix = "CM";
            }
            return String.format("%.3f %s", value, suffix);
        }

        public Iterator<Graduation> getPixelIterator(final int numberOfPixels) {
            return new Iterator<Graduation>() {
                int graduationIndex = 0;
                Graduation graduation = new Graduation();

                private float getValue() {
                    return graduationIndex * getPrecision();
                }

                private int getPixels() {
                    return (int) (getValue() * getPixelsPerUnit());
                }

                @Override
                public boolean hasNext() {
                    return getPixels() <= numberOfPixels;
                }

                @Override
                public Graduation next() {
                    // Returns the same Graduation object to avoid allocation.
                    graduation.value = getValue();
                    graduation.pixelOffset = getPixels();
                    graduation.relativeLength = getGraduatedScaleRelativeLength(graduationIndex);

                    graduationIndex++;
                    return graduation;
                }

                @Override
                public void remove() {

                }
            };
        }

        public float getPixelsPerUnit() {
            if (type == INCH) {
                return dpi;
            } else if (type == CM) {
                return dpi / 2.54f;
            }
            return 0;
        }

        private float getPrecision() {
            if (type == INCH) {
                return 1 / 4f;
            } else if (type == CM) {
                return 1 / 10f;
            }
            return 0;
        }

        private float getGraduatedScaleRelativeLength(int graduationIndex) {
            if (type == INCH) {
                if (graduationIndex % 4 == 0) {
                    return 1f;
                } else if (graduationIndex % 2 == 0) {
                    return 3 / 4f;
                } else {
                    return 1 / 2f;
                }
            } else if (type == CM) {
                if (graduationIndex % 10 == 0) {
                    return 1;
                } else if (graduationIndex % 5 == 0) {
                    return 3 / 4f;
                } else {
                    return 1 / 2f;
                }
            }
            return 0;
        }

    }
}

