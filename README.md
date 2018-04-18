#### [HIRE US](http://vrgsoft.net/)

# SmsView
Custom view for sms code input with customization</br></br>
<img src="https://github.com/VRGsoftUA/SmsView/blob/master/preview.gif" width="270" height="480" />


# Usage

*For a working implementation, Have a look at the Sample Project - app*

1. Include the library as local library project.
```gradle
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
    compile 'com.github.VRGsoftUA:SmsView:1.0'
}
```
2. Include SmsCodeView class in your xml layout. For Example:
```
<net.vrgsoft.smsview.SmsCodeView
        android:layout_width="match_parent"
        android:layout_height="100dp"
        android:layout_marginTop="10dp"
        android:textSize="33sp"
        android:gravity="center"
        app:onSubmit="onSubmit"
        app:itemWidth="40dp"
        app:itemHeight="67dp"
        app:itemBackground="@drawable/sms_digit_bg"
        app:itemTextColor="#757575"
        app:itemTextCursorDrawable="@drawable/edit_cursor"
        app:layout_constraintTop_toTopOf="parent"
        app:smsLength="6" />
```

# Customization
| Attribute | Description |
| ------------- | ------------- |
| app:smsLength | Number of digits in your sms (possible values from 1 to 8) |
| app:itemWidth | Width of one field for digit |
| app:itemHeight | Height of one field for digit |
| app:itemBackground | You can set any drawable for background of one item to give your view unique style |
| app:itemTextCursorDrawable | Sets cursor drawable for one digit field |
| app:itemTextColor | Sets text color for one digit field |
| android:gravity | Sets text alignment for one digit field |
| android:textSize | Sets text size for one digit field |
| app:onSubmit | Sets a listener for ACTION_DONE event on the last digit (data binding method references is also applicable) |

| Method  | Description |
| ------------- | ------------- |
| setOnSubmitListener(OnSubmitListener submitListener) | Sets the listener for keyboard ACTION_DONE event on the last digit |
| setSmsLength(int smsLength) | Sets sms length (number of digits in sms). Possible values: from 1 to 8 |
| requestFirstFocus() | Convenience method for requesting focus on first digit (for example, when view just created) |
| setItemSize(int itemWidth, int itemHeight) | Sets the size for one digit input view |
| setItemBackground(Drawable itemBackground) | Sets the item background for input views |
| setItemTextCursorDrawableRes(int itemTextCursorDrawableRes) | Sets the cursor drawable for input views |
| setItemTextColor(int itemTextColor) | Sets the text color for input views |
| setItemTextSize(int itemTextSize) | Sets the text size for input views |
| setDigitGravity(int gravity) | Sets the text size for input views |

#### Contributing
* Contributions are always welcome
* If you want a feature and can code, feel free to fork and add the change yourself and make a pull request
