package net.vrgsoft.smsview;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;

import static android.support.constraint.ConstraintLayout.LayoutParams.CHAIN_SPREAD;
import static android.support.constraint.ConstraintLayout.LayoutParams.PARENT_ID;

public class SmsCodeView extends ConstraintLayout implements View.OnKeyListener, TextView.OnEditorActionListener {
    private static final int START_INDEX = 1000;
    private static final int DEFAULT_SMS_LENGTH = 4;
    private static final int MIN_SMS_LENGTH = 1;
    private static final int MAX_SMS_LENGTH = 8;
    private OnSubmitListener mSubmitListener;
    private int mSmsLength;
    private int mItemWidth;
    private int mItemHeight;
    private LinkedHashMap<Integer, EditText> mDigits;
    private DigitsKeyListener mDigitsKeyListener;
    private Drawable mItemBackground;
    private int mItemTextCursorDrawableRes;
    private int mItemTextColor;
    private int mItemTextSize;
    private int mDigitGravity;

    public SmsCodeView(Context context) {
        this(context, null);
    }

    public SmsCodeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SmsCodeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mDigits = new LinkedHashMap<>();

        //default values
        mSmsLength = DEFAULT_SMS_LENGTH;
        mItemWidth = context.getResources().getDimensionPixelSize(R.dimen.default_item_width);
        mItemHeight = context.getResources().getDimensionPixelSize(R.dimen.default_item_height);
        mItemTextSize = context.getResources().getDimensionPixelSize(R.dimen.default_text_size);
        mDigitGravity = Gravity.CENTER;
        mItemTextColor = Color.BLACK;

        mDigitsKeyListener = DigitsKeyListener.getInstance("0123456789");

        initAttrs(context, attrs);
        initDigits(context);
        initTextWatchers();
        initKeyListeners();

        mDigits.get(START_INDEX + mSmsLength - 1).setOnEditorActionListener(this);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SmsCodeView);

            mSmsLength = a.getInteger(R.styleable.SmsCodeView_smsLength, DEFAULT_SMS_LENGTH);
            if (mSmsLength > MAX_SMS_LENGTH || mSmsLength < MIN_SMS_LENGTH) {
                throw new IllegalArgumentException("Sms length should be in range from 1 to 8");
            }
            final String handlerName = a.getString(R.styleable.SmsCodeView_onSubmit);
            if (handlerName != null) {
                setOnSubmitListener(new DeclaredOnSubmitListener(this, handlerName));
            }

            mItemWidth = a.getDimensionPixelSize(R.styleable.SmsCodeView_itemWidth, mItemWidth);
            mItemHeight = a.getDimensionPixelSize(R.styleable.SmsCodeView_itemHeight, mItemHeight);
            mItemTextSize = a.getDimensionPixelSize(R.styleable.SmsCodeView_android_textSize, mItemTextSize);

            mItemBackground = a.getDrawable(R.styleable.SmsCodeView_itemBackground);
            mItemTextCursorDrawableRes = a.getResourceId(R.styleable.SmsCodeView_itemTextCursorDrawable, 0);
            mItemTextColor = a.getColor(R.styleable.SmsCodeView_itemTextColor, Color.BLACK);
            mDigitGravity = a.getInt(R.styleable.SmsCodeView_android_gravity, Gravity.CENTER);

            a.recycle();
        }
    }

    private void initDigits(Context context) {
        for (int i = START_INDEX; i < START_INDEX + mSmsLength; i++) {
            EditText editText = new EditText(context);
            editText.setId(i);
            mDigits.put(i, editText);
        }

        //if only one field presented - align in the center
        if (mSmsLength == 1) {
            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            params.width = mItemWidth;
            params.height = mItemHeight;
            params.startToStart = PARENT_ID;
            params.endToEnd = PARENT_ID;
            params.topToTop = PARENT_ID;
            params.bottomToBottom = PARENT_ID;

            EditText editText = mDigits.get(START_INDEX);
            configureEditText(editText);
            editText.setLayoutParams(params);
            addView(editText);
        } else {
            for (int i = START_INDEX; i < START_INDEX + mSmsLength; i++) {
                EditText editText = mDigits.get(i);
                configureEditText(editText);
                setEditCursorDrawable(editText);

                ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.width = mItemWidth;
                params.height = mItemHeight;
                params.topToTop = PARENT_ID;
                params.bottomToBottom = PARENT_ID;
                params.horizontalChainStyle = CHAIN_SPREAD;
                if (i == START_INDEX) {
                    //first element alignment
                    params.startToStart = PARENT_ID;
                    params.endToStart = mDigits.get(i + 1).getId();
                } else if (i == START_INDEX + mSmsLength - 1) {
                    //middle element alignment
                    editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
                    params.startToEnd = mDigits.get(i - 1).getId();
                    params.endToEnd = PARENT_ID;
                } else {
                    //last element alignment
                    params.startToEnd = mDigits.get(i - 1).getId();
                    params.endToStart = mDigits.get(i + 1).getId();
                }

                editText.setLayoutParams(params);
                addView(editText);
            }
        }
    }

    private void configureEditText(EditText editText) {
        editText.setGravity(mDigitGravity);
        editText.setHint(null);
        editText.setKeyListener(mDigitsKeyListener);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        editText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, mItemTextSize);
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
        if (mItemBackground != null) {
            editText.setBackground(mItemBackground);
        }
        editText.setTextColor(mItemTextColor);
    }

    private void setEditCursorDrawable(EditText editText) {
        try {
            Field f = TextView.class.getDeclaredField("mCursorDrawableRes");
            f.setAccessible(true);
            f.set(editText, mItemTextCursorDrawableRes);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public String getText() {
        StringBuilder builder = new StringBuilder();
        for (int i = START_INDEX; i < START_INDEX + mSmsLength; i++) {
            builder.append(mDigits.get(i).getText());
        }
        return builder.toString();
    }

    private void initTextWatchers() {
        for (int i = START_INDEX; i < START_INDEX + mSmsLength - 1; i++) {
            final int id = i;
            mDigits.get(i).addTextChangedListener(new TextWatcherAdapter() {
                @Override
                public void onTextChanged(CharSequence charSequence, int i1, int i2, int i3) {
                    if (!TextUtils.isEmpty(charSequence)) {
                        mDigits.get(id + 1).requestFocus();
                    }
                }
            });
        }
    }

    private void initKeyListeners() {
        for (int i = START_INDEX; i < START_INDEX + mSmsLength; i++) {
            mDigits.get(i).setOnKeyListener(this);
        }
    }

    @Override
    public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
        if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) return false;
        if (mSmsLength == 1) return false;

        if (keyCode == KeyEvent.KEYCODE_DEL) {
            if (view.getId() == START_INDEX) return false;
            else if (view.getId() == START_INDEX + mSmsLength - 1) {
                if (TextUtils.isEmpty(mDigits.get(view.getId()).getText())) {
                    EditText editText = mDigits.get(view.getId() - 1);
                    editText.setText("");
                    editText.requestFocus();
                }
            } else {
                EditText editText = mDigits.get(view.getId() - 1);
                editText.setText("");
                editText.requestFocus();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if ((actionId & EditorInfo.IME_MASK_ACTION) != 0) {
            if (mSubmitListener != null) {
                mSubmitListener.onSubmit(getText());
                KeyboardHelper.hideKeyboard(getContext(), mDigits.get(START_INDEX + mSmsLength - 1).getWindowToken());
            }
            return true;
        } else {
            return false;
        }
    }

    public interface OnSubmitListener {
        void onSubmit(String text);
    }

    private static class DeclaredOnSubmitListener implements OnSubmitListener {
        private final View mHostView;
        private final String mMethodName;

        private Method mResolvedMethod;
        private Context mResolvedContext;

        DeclaredOnSubmitListener(@NonNull View hostView, @NonNull String methodName) {
            mHostView = hostView;
            mMethodName = methodName;
        }

        @Override
        public void onSubmit(String text) {
            if (mResolvedMethod == null) {
                resolveMethod(mHostView.getContext());
            }

            try {
                mResolvedMethod.invoke(mResolvedContext, text);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(
                        "Could not execute non-public method for onSubmit", e);
            } catch (InvocationTargetException e) {
                throw new IllegalStateException(
                        "Could not execute method for onSubmit", e);
            }
        }

        private void resolveMethod(@Nullable Context context) {
            while (context != null) {
                try {
                    if (!context.isRestricted()) {
                        final Method method = context.getClass().getMethod(mMethodName, String.class);
                        if (method != null) {
                            mResolvedMethod = method;
                            mResolvedContext = context;
                            return;
                        }
                    }
                } catch (NoSuchMethodException e) {
                    // Failed to find method, keep searching up the hierarchy.
                }

                if (context instanceof ContextWrapper) {
                    context = ((ContextWrapper) context).getBaseContext();
                } else {
                    // Can't search up the hierarchy, null out and fail.
                    context = null;
                }
            }

            final int id = mHostView.getId();
            final String idText = id == NO_ID ? "" : " with id '"
                    + mHostView.getContext().getResources().getResourceEntryName(id) + "'";
            throw new IllegalStateException("Could not find method " + mMethodName
                    + "(View) in a parent or ancestor Context for onSubmit "
                    + "attribute defined on view " + mHostView.getClass() + idText);
        }
    }

    /**
     * Sets the listener for keyboard ACTION_DONE event on the last digit
     * @see OnSubmitListener
     * @param submitListener
     */
    public void setOnSubmitListener(OnSubmitListener submitListener) {
        this.mSubmitListener = submitListener;
    }

    /**
     * Sets sms length (number of digits in sms). Possible values: from 1 to 8
     * @param smsLength
     */
    public void setSmsLength(@IntRange(from = 1, to = 8) int smsLength) {
        mSmsLength = smsLength;
        requestLayout();
    }

    /**
     * Convenience method for requesting focus on first digit (for example, when view just created)
     */
    public void requestFirstFocus() {
        mDigits.get(START_INDEX).requestFocus();
        KeyboardHelper.showKeyboard(getContext(), mDigits.get(START_INDEX));
    }

    /**
     * Sets the size for one digit input view
     * @param itemWidth
     * @param itemHeight
     */
    public void setItemSize(int itemWidth, int itemHeight) {
        mItemWidth = itemWidth;
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(itemWidth, itemHeight);
        for (int i = START_INDEX; i < START_INDEX + mSmsLength; i++) {
            mDigits.get(i).setLayoutParams(params);
        }
    }

    /**
     * Sets the item background for input views
     * @param itemBackground
     */
    public void setItemBackground(Drawable itemBackground) {
        mItemBackground = itemBackground;
        for (int i = START_INDEX; i < START_INDEX + mSmsLength; i++) {
            mDigits.get(i).setBackground(itemBackground);
        }
    }

    /**
     * Sets the cursor drawable for input views
     * @param itemTextCursorDrawableRes
     */
    public void setItemTextCursorDrawableRes(@DrawableRes int itemTextCursorDrawableRes) {
        mItemTextCursorDrawableRes = itemTextCursorDrawableRes;
        for (int i = START_INDEX; i < START_INDEX + mSmsLength; i++) {
            setEditCursorDrawable(mDigits.get(i));
        }
    }


    /**
     * Sets the text color for input views
     * @param itemTextColor
     */
    public void setItemTextColor(@ColorInt int itemTextColor) {
        mItemTextColor = itemTextColor;
        for (int i = START_INDEX; i < START_INDEX + mSmsLength; i++) {
            mDigits.get(i).setTextColor(itemTextColor);
        }
    }


    /**
     * Sets the text size for input views
     * @param itemTextSize
     */
    public void setItemTextSize(int itemTextSize) {
        mItemTextSize = itemTextSize;
        for (int i = START_INDEX; i < START_INDEX + mSmsLength; i++) {
            mDigits.get(i).setTextSize(itemTextSize);
        }
    }


    /**
     * Sets the text gravity for input views
     * @param gravity
     */
    public void setDigitGravity(int gravity) {
        mDigitGravity = gravity;
        for (int i = START_INDEX; i < START_INDEX + mSmsLength; i++) {
            mDigits.get(i).setGravity(gravity);
        }
    }
}

