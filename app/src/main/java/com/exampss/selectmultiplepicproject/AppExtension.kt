package com.exampss.selectmultiplepicproject

import android.app.Activity
import android.content.*
import android.content.res.Configuration
import android.database.Cursor
import android.database.CursorIndexOutOfBoundsException
import android.graphics.*
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Geocoder
import android.media.ExifInterface
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Patterns
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import java.io.*
import java.time.DayOfWeek
import java.time.temporal.WeekFields
import java.util.*
import kotlin.math.roundToInt

/**
 * Develop By Messagemuse
 */
var fileUri: Uri? = null

val Context.glide: RequestManager?
    get() = Glide.with(this)

fun ImageView.load(path: String) {
    try {
        context.glide!!.load(path).thumbnail(0.1f).into(this)
    } catch (ex: IllegalArgumentException) {

    }
}


fun Context.showToastMessage(message: CharSequence) =
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()


internal fun Context.getColorCompat(@ColorRes color: Int) = ContextCompat.getColor(this, color)

internal fun TextView.setTextColorRes(@ColorRes color: Int) =
    setTextColor(context.getColorCompat(color))

fun dpToPx(dp: Int, context: Context): Int =
    TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(),
        context.resources.displayMetrics
    ).toInt()

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}


internal val Context.layoutInflater: LayoutInflater
    get() = LayoutInflater.from(this)

internal val Context.inputMethodManager
    get() = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

fun View.showKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
}


fun getCapsSentences(tagName: String): String? {
    val splits = tagName.toLowerCase(Locale.ROOT).split(" ".toRegex()).toTypedArray()
    val sb = StringBuilder()
    for (i in splits.indices) {
        val eachWord = splits[i]
        if (i > 0 && eachWord.length > 0) {
            sb.append(" ")
        }
        val cap = (eachWord.substring(0, 1).toUpperCase()
                + eachWord.substring(1))
        sb.append(cap)
    }
    return sb.toString()
}

fun convertCountToMonth(month: Int): String {
    return when (month) {
        1 -> "Jan"
        2 -> "Feb"
        3 -> "Mar"
        4 -> "Apr"
        5 -> "May"
        6 -> "Jun"
        7 -> "Jul"
        8 -> "Aug"
        9 -> "Sep"
        10 -> "Oct"
        11 -> "Nov"
        12 -> "Dec"
        else -> ""
    }
}

fun daysOfWeekFromLocale(): Array<DayOfWeek> {
    val firstDayOfWeek = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        WeekFields.of(Locale.getDefault()).firstDayOfWeek
    } else {
        TODO("VERSION.SDK_INT < O")
    }
    var daysOfWeek = DayOfWeek.values()
    // Order `daysOfWeek` array so that firstDayOfWeek is at index 0.
    // Only necessary if firstDayOfWeek != DayOfWeek.MONDAY which has ordinal 0.
    if (firstDayOfWeek != DayOfWeek.MONDAY) {
        val rhs = daysOfWeek.sliceArray(firstDayOfWeek.ordinal..daysOfWeek.indices.last)
        val lhs = daysOfWeek.sliceArray(0 until firstDayOfWeek.ordinal)
        daysOfWeek = rhs + lhs
    }
    return daysOfWeek
}

fun Activity.startCamera(requestCode: Int) {
    val values = ContentValues(1)
    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
    fileUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    if (intent.resolveActivity(packageManager) != null) {
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        startActivityForResult(intent, requestCode)
    }
}

fun Activity.showGallery(requestCode: Int) {
    val galleryIntent: Intent?
    galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    galleryIntent.type = "image/*"
    if (galleryIntent.resolveActivity(packageManager) == null) {
        showToastMessage("This Application do not have Gallery Application")
        return
    }
    startActivityForResult(galleryIntent, requestCode)
}


private fun calculateInSampleSize(
    options: BitmapFactory.Options,
    reqWidth: Int,
    reqHeight: Int
): Int {
    // Raw height and width of image
    val height = options.outHeight
    val width = options.outWidth
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
        val heightRatio = (height.toFloat() / reqHeight.toFloat()).roundToInt()
        val widthRatio = (width.toFloat() / reqWidth.toFloat()).roundToInt()
        inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
        val totalPixels = (width * height).toFloat()
        val totalReqPixelsCap = (reqWidth * reqHeight * 2).toFloat()
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++
        }
    }
    return inSampleSize
}

private fun rotateImageIfRequired(img: Bitmap, selectedImage: Uri): Bitmap {
    val ei = selectedImage.path?.let { ExifInterface(it) }
    val orientation =
        ei!!.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    return when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 ->
            rotateImage(img, 90)
        ExifInterface.ORIENTATION_ROTATE_180 ->
            rotateImage(img, 180)
        ExifInterface.ORIENTATION_ROTATE_270 ->
            rotateImage(img, 270)
        else -> {
            img
        }
    }
}

private fun rotateImage(img: Bitmap, degree: Int): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(degree.toFloat())
    val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
    img.recycle()
    return rotatedImg
}

fun Activity.processCapturedPhoto(callback: ICallBackUri) {
    try {
        val cursor = contentResolver.query(
            Uri.parse(fileUri.toString()),
            Array(1) { MediaStore.Images.ImageColumns.DATA },
            null,
            null,
            null
        )
        cursor!!.moveToFirst()
        val photoPath = cursor.getString(0)
        cursor.close()
        val file = File(photoPath)
        callback.imageUri(Uri.fromFile(file))
    } catch (ex: CursorIndexOutOfBoundsException) {
        ex.printStackTrace()

        val columns: Array<String> =
            arrayOf(MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DATE_ADDED)
        val cursor = contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            columns,
            null,
            null,
            "${MediaStore.MediaColumns.DATE_ADDED} DESC"
        )

        val coloumnIndex = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        val photoPath = cursor.getString(coloumnIndex)
        cursor.close()
        val file = File(photoPath)
        callback.imageUri(Uri.fromFile(file))
    }

}


fun Context.isEmailValid(email: String): Boolean {
    return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

fun Context.convertBitmapToUri(bitmap: Bitmap): Uri? {
    val file = File(this.cacheDir, System.currentTimeMillis().toString() + ".png")
    try {
        file.createNewFile()
    } catch (e: IOException) {
        e.printStackTrace()
    }
    val bos = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos)
    val bitmapdata = bos.toByteArray()
    try {
        val fos = FileOutputStream(file)
        fos.write(bitmapdata)
        fos.flush()
        fos.close()
    } catch (e: IOException) {

        e.printStackTrace()
    }

    return Uri.fromFile(file)
}


fun saveBitmap(path: String, bitmap: Bitmap, context: Context) {
    var myDir = File(path)
    try {
        var outStream = FileOutputStream(myDir)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
        outStream.flush()
        outStream.close()
        val paths = arrayOf<String>(myDir.toString())
        MediaScannerConnection.scanFile(context, paths, null, null)
    } catch (ex: java.lang.Exception) {
        ex.printStackTrace()
    }
}

fun getBitmap(v: View): Bitmap? {
    try {
        v.clearFocus()
        v.isPressed = false
        val willNotCache = v.willNotCacheDrawing()
        v.setWillNotCacheDrawing(false)
        val color = v.drawingCacheBackgroundColor
        v.drawingCacheBackgroundColor = 0
        if (color != 0) {
            v.destroyDrawingCache()
        }
        v.isDrawingCacheEnabled = true
        v.buildDrawingCache(true)
        val bitmap = Bitmap.createBitmap(v.drawingCache)
        v.isDrawingCacheEnabled = false
        v.destroyDrawingCache()
        v.setWillNotCacheDrawing(willNotCache)
        v.drawingCacheBackgroundColor = color
        return bitmap
    } catch (ex: NullPointerException) {
        ex.printStackTrace()
        return null
    }
}


fun currentOS(): String {
    val builder = StringBuilder()
    val fields = Build.VERSION_CODES::class.java.fields
    for (field in fields) {
        val fieldName = field.name
        var fieldValue = -1

        try {
            fieldValue = field.getInt(Any())
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }

        if (fieldValue == Build.VERSION.SDK_INT) {
            builder.append(fieldName)
        }
    }
    return builder.toString()
}

fun Activity.startVideoCamera(requestCode: Int) {
    val values = ContentValues(1)
    values.put(MediaStore.Video.Media.MIME_TYPE, "video/*")
    fileUri = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
    val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
    if (intent.resolveActivity(packageManager) != null) {
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 20)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        startActivityForResult(intent, requestCode)
    }
}


fun Activity.showGalleryVideo(requestCode: Int) {
    val galleryIntent: Intent?
    galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
    galleryIntent.type = "video/*"
    if (galleryIntent.resolveActivity(packageManager) == null) {
        showToastMessage("This Application do not have Gallery Application")
        return
    }
    startActivityForResult(galleryIntent, requestCode)
}


fun Context.getRootParentFragment(fragment: Fragment): Fragment {
    val parent = fragment.parentFragment
    return if (parent == null)
        fragment
    else
        getRootParentFragment(parent)
}

fun Context.copyText(text: String) {
    val clipboard: ClipboardManager = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip: ClipData = ClipData.newPlainText("label", text)
    clipboard.setPrimaryClip(clip)
}


fun adjustFontScale(configuration: Configuration, context: Context) {
    configuration.fontScale = 1.0.toFloat()
    val metrics = context.resources.displayMetrics
    val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    wm.defaultDisplay.getMetrics(metrics)
    metrics.scaledDensity = configuration.fontScale * metrics.density
    context.resources.updateConfiguration(configuration, metrics)
}


fun getBitmapFromView(view: View): Bitmap? {
    val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(returnedBitmap)
    val bgDrawable: Drawable? = view.background
    if (bgDrawable != null) bgDrawable.draw(canvas) else canvas.drawColor(Color.WHITE)
    view.draw(canvas)
    return returnedBitmap
}


fun getMediaPath(context: Context, uri: Uri): String {

    val resolver = context.contentResolver
    val projection = arrayOf(MediaStore.Video.Media.DATA)
    var cursor: Cursor? = null
    try {
        cursor = resolver.query(uri, projection, null, null, null)
        return if (cursor != null) {
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(columnIndex)

        } else ""

    } catch (e: Exception) {
        resolver.let {
            val filePath = (context.applicationInfo.dataDir + File.separator
                    + System.currentTimeMillis())
            val file = File(filePath)

            resolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    val buf = ByteArray(4096)
                    var len: Int
                    while (inputStream.read(buf).also { len = it } > 0) outputStream.write(
                        buf,
                        0,
                        len
                    )
                }
            }
            return file.absolutePath
        }
    } finally {
        cursor?.close()
    }
}

@Suppress("DEPRECATION")
fun saveVideoFile(filePath: String?): File? {
    filePath?.let {
        val videoFile = File(filePath)
        val videoFileName = "${System.currentTimeMillis()}_${videoFile.name}"
        val folderName = Environment.DIRECTORY_MOVIES
        if (Build.VERSION.SDK_INT >= 30) {

            val values = ContentValues().apply {

                put(
                    MediaStore.Images.Media.DISPLAY_NAME,
                    videoFileName
                )
                put(MediaStore.Images.Media.MIME_TYPE, "video/mp4")
                put(MediaStore.Images.Media.RELATIVE_PATH, folderName)
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }

            val collection =
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

            val fileUri = ActivityBase.activity.contentResolver.insert(collection, values)

            fileUri?.let {
                ActivityBase.activity.contentResolver.openFileDescriptor(fileUri, "rw")
                    .use { descriptor ->
                        descriptor?.let {
                            FileOutputStream(descriptor.fileDescriptor).use { out ->
                                FileInputStream(videoFile).use { inputStream ->
                                    val buf = ByteArray(4096)
                                    while (true) {
                                        val sz = inputStream.read(buf)
                                        if (sz <= 0) break
                                        out.write(buf, 0, sz)
                                    }
                                }
                            }
                        }
                    }

                values.clear()
                values.put(MediaStore.Video.Media.IS_PENDING, 0)
                ActivityBase.activity.contentResolver.update(fileUri, values, null, null)

                return File(getMediaPath(ActivityBase.activity, fileUri))
            }
        } else {
            val downloadsPath =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val desFile = File(downloadsPath, videoFileName)

            if (desFile.exists())
                desFile.delete()

            try {
                desFile.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return desFile
        }
    }
    return null
}


fun shareCard(path: String, text: String) {
    val uri = Uri.parse(path)
    val shareIntent = Intent()
    shareIntent.action = Intent.ACTION_SEND
    shareIntent.putExtra(Intent.EXTRA_TEXT, text)
    shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
    shareIntent.type = "image/*"
    ActivityBase.activity.startActivity(Intent.createChooser(shareIntent, "Share Card"))
}

fun shareEmailIntent(mailTo: String) {
    val uriText = "mailto:${mailTo}" +
            "?subject=" + Uri.encode("") +
            "&body=" + Uri.encode("")

    val uri = Uri.parse(uriText)

    val sendIntent = Intent(Intent.ACTION_SENDTO)
    sendIntent.data = uri
    ActivityBase.activity.startActivity(Intent.createChooser(sendIntent, "Send email"))

}


fun validCellPhone(number: String): Boolean {
    return number.isNotEmpty() && Patterns.PHONE.matcher(number).matches()
}

fun openCreateContact() {
    val intent = Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI)
    ActivityBase.activity.startActivity(intent)
}


fun watchYoutubeVideo(id: String) {
    val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$id"))
    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=$id"))
    try {
        ActivityBase.activity.startActivity(appIntent)
    } catch (ex: ActivityNotFoundException) {
        ActivityBase.activity.startActivity(webIntent)
    }
}


fun Context.getAddressFromLatLng(latitude: Double, longitude: Double): String {

    var geocoder: Geocoder = Geocoder(this, Locale.getDefault())
    var addresses: List<Address>
    var isLahore: Boolean = false
    var completeAddress = ""

    var addressToShow = ""

    var featureName = ""
    var locatlity = ""
    var premises = ""
    var subAdminArea = ""
    var subLocality = ""
    var thoroughfare = ""
    var subThoroughfare = ""

    var lahoreString = ""

    try {
        addresses = geocoder.getFromLocation(latitude, longitude, 1) // addresses list
        completeAddress = addresses[0].getAddressLine(0) // gives the complete address
        val splitAddress = completeAddress.split(",")
        val mainLane = splitAddress[0]
        val customLine = splitAddress[1]
        if (addresses[0].featureName != null) {
            featureName = addresses[0].featureName
        }
        if (addresses[0].locality != null) {
            locatlity = addresses[0].locality
        }
        if (addresses[0].premises != null) {
            premises = addresses[0].premises
        }
        if (addresses[0].subAdminArea != null) {
            subAdminArea = addresses[0].subAdminArea
        }
        if (addresses[0].subLocality != null) {
            subLocality = addresses[0].subLocality
        }
        if (addresses[0].thoroughfare != null) {
            thoroughfare = addresses[0].thoroughfare
        }
        if (addresses[0].subThoroughfare != null) {
            subThoroughfare = addresses[0].subThoroughfare
        }

        lahoreString = featureName + locatlity + premises + subAdminArea + subLocality + thoroughfare + subThoroughfare
        isLahore = lahoreString.contains("lahore", true)


        addressToShow = mainLane

        if (!TextUtils.isEmpty(subLocality)) {
            addressToShow += ", $subLocality"
        } else {
            addressToShow += ", $customLine"
        }

    } catch (ex: Exception) {
        addressToShow = "Unnamed Road, Lahore"
    }
    return addressToShow

}