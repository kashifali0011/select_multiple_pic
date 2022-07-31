package com.exampss.selectmultiplepicproject

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity

open class ActivityBase : AppCompatActivity() {


    var lifecycleState: State? = null

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity = this
    }


    fun getRealPathFromURI(contentUri: Uri): String {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(contentUri, projection, null, null, null)
        val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor!!.moveToFirst()
        return cursor!!.getString(column_index)
    }

    override fun onStart() {
        super.onStart()
        lifecycleState = State.STARTED
    }

    override fun onResume() {
        super.onResume()
        lifecycleState = State.RESUMED
    }

    override fun onPause() {
        super.onPause()
        lifecycleState = State.PAUSED
    }


    override fun onStop() {
        super.onStop()
        lifecycleState = State.STOPPED
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleState = State.DESTROYED
    }


    enum class State {
        CREATED, STARTED, RESUMED, PAUSED, STOPPED, DESTROYED
    }


    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    @SuppressLint("ServiceCast")
    fun showKeyboard() {
        (getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager)
            .toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
    }

    fun hideKeyboard() {
        (getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager)
            .toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }


    @Throws(Exception::class)
    private fun RGB565toARGB888(img: Bitmap): Bitmap {
        val numPixels = img.width * img.height
        val pixels = IntArray(numPixels)
        img.getPixels(pixels, 0, img.width, 0, 0, img.width, img.height)
        val result = Bitmap.createBitmap(img.width, img.height, Bitmap.Config.ARGB_8888)
        result.setPixels(pixels, 0, result.width, 0, 0, result.width, result.height)
        return result
    }

    fun noTrailingwhiteLines(text: CharSequence): CharSequence {
        var text = text

        while (text[text.length - 1] == '\n') {
            text = text.subSequence(0, text.length - 1)
        }

        while (text[text.length - 2] == '\n') {
            text = text.subSequence(0, text.length - 2)
        }
        return text
    }


    fun withSuffix(count: Long): String {
        if (count < 1000) return "" + count
        val exp = (Math.log(count.toDouble()) / Math.log(1000.0)).toInt()
        return String.format(
            "%.1f %c",
            count / Math.pow(1000.0, exp.toDouble()),
            "kMGTPE"[exp - 1]
        )
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun setStatusBarGradiant(flag: Int) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                val window = activity.window
//                val background = activity.resources.getDrawable(R.drawable.bg_tour_header)
//                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//                window.statusBarColor = activity.resources.getColor(android.R.color.transparent)
//                //window.setNavigationBarColor(activity.getResources().getColor(android.R.color.transparent));
//                activity.window.decorView.systemUiVisibility = flag
//                window.setBackgroundDrawable(background)
//            }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun setStatusBarColor(color: Int, flag: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = activity.window
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = color
            activity.window.decorView.systemUiVisibility = flag

        }
    }

    companion object {
        lateinit var activity: AppCompatActivity
    }

    override fun onBackPressed() {

        super.onBackPressed()
    }


}