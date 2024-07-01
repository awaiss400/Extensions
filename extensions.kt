
suspend fun Bitmap.saveToGallery(
    context: Context,
    mimeType: String = "image/*",
    done: () -> Unit
) {
    withContext(Dispatchers.IO) {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "Image_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, mimeType)
            put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
        }

        val contentResolver = context.contentResolver
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(
                MediaStore.Images.Media.RELATIVE_PATH,
                "${Environment.DIRECTORY_PICTURES}/PhotoRecoveryAppsIO"
            )

            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            val directory =
                File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "PhotoRecoveryAppsIO"
                )
            contentValues.put(MediaStore.Images.Media.DATA, directory.absolutePath)
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val uri = contentResolver.insert(collection, contentValues)
        uri?.let { imageUri ->
            contentResolver.openOutputStream(imageUri)?.use { outputStream ->
                this@saveToGallery.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                outputStream.flush()
                done.invoke()
            }
        } ?: run {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.shakeView() {
    val shakeAnimator = ObjectAnimator.ofFloat(this, "translationX", -10f, 10f)
    shakeAnimator.duration = 2000
    shakeAnimator.interpolator = CycleInterpolator(5f)
    shakeAnimator.repeatCount = ObjectAnimator.INFINITE
    shakeAnimator.repeatMode = ObjectAnimator.REVERSE
    shakeAnimator.start()
}

fun ByteArray.toBitmap(): Bitmap? {
    return BitmapFactory.decodeByteArray(this, 0, size)
}

fun View.gone() {
    visibility = View.GONE
}

fun Any.logd(message: String, tag: String? = "SignatureMakerApp") {
    Log.d(tag, "mCustomLog:$message ")
}

fun EditText.onDone(callback: () -> Unit) {
    setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            callback.invoke()
            return@setOnEditorActionListener true
        }
        false
    }
}

/**
 * Applies system window insets to the specified view, adjusting the margins as specified.
 *
 * @param view The view to which the window insets should be applied.
 * @param applyTopMargin Whether to apply the top margin.
 * @param applyBottomMargin Whether to apply the bottom margin.
 * @param applyStartMargin Whether to apply the start margin.
 * @param applyEndMargin Whether to apply the end margin.
 */
fun View.applyInsets(
    applyTopMargin: Boolean = false,
    topExtraMarginDp: Int = 0,
    applyBottomMargin: Boolean = false,
    bottomExtraMarginDp: Int = 0,
    applyStartMargin: Boolean = false,
    startExtraMarginDp: Int = 0,
    applyEndMargin: Boolean = false,
    endExtraMarginDp: Int = 0
) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, windowInsets ->
        val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
        val density = Resources.getSystem().displayMetrics.density

        v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            if (applyTopMargin) topMargin = insets.top + (topExtraMarginDp * density).toInt()
            if (applyBottomMargin) bottomMargin =
                insets.bottom + (bottomExtraMarginDp * density).toInt()
            if (applyStartMargin) marginStart = insets.left + (startExtraMarginDp * density).toInt()
            if (applyEndMargin) marginEnd = insets.right + (endExtraMarginDp * density).toInt()
        }
//use .Consumed if dont want others to take insets
        windowInsets
    }
}

fun Fragment.disableMultipleClicking(view: View, delay: Long = 750) {
    view.isEnabled = false
    this.lifecycleScope.launch {
        delay(delay)
        view.isEnabled = true
    }
}
// Extension function for TextView to set colored text spans
// Extension function for TextView to set colored text spans
fun TextView.setColoredText(text: String, vararg coloredWords: Pair<String, Int>) {
    val spannable = SpannableStringBuilder(text)

    coloredWords.forEach { (word, colorResId) ->
        val color = ContextCompat.getColor(context, colorResId)
        val startIndex = text.indexOf(word)
        val endIndex = startIndex + word.length
        if (startIndex != -1) { // Ensure the word exists in the text
            spannable.setSpan(ForegroundColorSpan(color), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    setText(spannable, TextView.BufferType.SPANNABLE)
}

fun Activity.shareFileWithOtherApps(uri: Uri?,mimeType:String="application/*",shareMessage:String?=null) {
    try {
        val intent = Intent(Intent.ACTION_SEND)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        intent.setDataAndType(uri, mimeType)
        intent.putExtra(Intent.EXTRA_TEXT, shareMessage)
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        startActivity(intent)
    } catch (_: Exception) {
    }
}
/*/  binding.textView.makeLinks(Pair("Privacy policy", View.OnClickListener {
            it.isEnabled = false
            Handler().postDelayed({ it.isEnabled = true }, 500)
            val uri =
                Uri.parse(Constants.PRIVACYPOLICY_LINK); // missing 'http://' will cause crashed
            val intent = Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent)
        }), Pair("Terms and Conditions", View.OnClickListener {
            it.isEnabled = false
            Handler().postDelayed({ it.isEnabled = true }, 500)
            val uri =
                Uri.parse(Constants.terms); // missing 'http://' will cause crashed
            val intent = Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent)
        })
        )*/
fun TextView.makeLinks(vararg links: Pair<String, View.OnClickListener>) {
    val spannableString = SpannableString(this.text)
    var startIndexOfLink = -1
    for (link in links) {
        val clickableSpan = object : ClickableSpan() {
            override fun updateDrawState(textPaint: TextPaint) {
                textPaint.color = textPaint.linkColor
                textPaint.isUnderlineText=true
//                textPaint.typeface = Typeface.DEFAULT_BOLD
                textPaint.color = Color.parseColor("#EE0038")
            }

            override fun onClick(view: View) {
                Selection.setSelection((view as TextView).text as Spannable, 0)
                view.invalidate()
                link.second.onClick(view)
            }
        }
        startIndexOfLink = this.text.toString().indexOf(link.first, startIndexOfLink + 1)
        spannableString.setSpan(
            clickableSpan,
            startIndexOfLink,
            startIndexOfLink + link.first.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
    this.movementMethod = LinkMovementMethod.getInstance()
    this.setText(spannableString, TextView.BufferType.SPANNABLE)
}
fun BottomSheetDialogFragment.openBottomSheet(activity: FragmentActivity?) {
    if (this.isAdded) {
        this.dismiss()
    }
    activity?.supportFragmentManager?.let { fragmentManager ->
        this.show(fragmentManager, this.tag)
    }
}
fun BottomSheetDialogFragment.dismissSafely() {
    if (this.isAdded) {
        this.dismiss()
    }
}

fun Fragment.handleBackPress(onBackPressed: () -> Unit) {
    var lastBackPressedTime = 0L  // Variable to store the last back button press time

    requireView().isFocusableInTouchMode = true
    requireView().requestFocus()
    requireView().setOnKeyListener { _, keyCode, event ->
        if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastBackPressedTime > 1000) {  // Check if more than 2 seconds have passed
                lastBackPressedTime = currentTime
                onBackPressed() // Call the provided callback function
            }
            true
        } else false
    }
}

fun Resources.decodeSampledBitmapFromResource(
    resId: Int, reqWidth: Int, reqHeight: Int
): Bitmap {
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
        BitmapFactory.decodeResource(this@decodeSampledBitmapFromResource, resId, this)
        inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)
        inJustDecodeBounds = false
    }
    return BitmapFactory.decodeResource(this@decodeSampledBitmapFromResource, resId, options)
}

fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val (height: Int, width: Int) = options.run { outHeight to outWidth }
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
        val halfHeight: Int = height / 2
        val halfWidth: Int = width / 2

        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }

    return inSampleSize
}

fun View.setSampledBitmapAsBackground(resources: Resources, resId: Int) {
    val scaledBitmap = resources.decodeSampledBitmapFromResource(resId, 100, 100)
    this.background = BitmapDrawable(resources, scaledBitmap)
}

fun Fragment.navigateTo(actionId: Int, destinationName: Int) {
    findNavController().navigate(
        actionId, null, NavOptions.Builder().setPopUpTo(destinationName, true).build()
    )
}

/**
 * for sending bundle along with navigation
 * */
fun Fragment.navigateSafe(
    actionId: Int, currentDestinationFragmentId: Int, bundle: Bundle? = null
) {
    if (findNavController().currentDestination?.id == currentDestinationFragmentId) {
        findNavController().navigate(
            actionId, bundle
        )
    } else {
        Log.d("TAG", "navigateSafe: ")
    }
}

fun Fragment.printLogs(msg: Any, tag: String? = null) {
    val fragmentName = tag ?: this.javaClass.simpleName
    Log.d(fragmentName, "$fragmentName: $msg")
}

fun Any.printLogs(msg: Any, tag: String? = null) {
    Log.d("SignatureMakerApp", "SignatureMakerApp: $msg")
}

fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)
fun Fragment.showToast(string: String) {
    Toast.makeText(this.requireContext(), string, Toast.LENGTH_SHORT).show()
}

fun Fragment.showLongToast(string: String) {
    Toast.makeText(this.requireContext(), string, Toast.LENGTH_LONG).show()
}

fun Fragment.showKeyboard(view: View?) {
    view?.let {
        val imm = it.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(it, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }
}

fun EditText.textWatcher(onTextChanged: (String?) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//            onTextChanged(s)
        }

        override fun afterTextChanged(s: Editable?) {
            s?.let {
                onTextChanged(s.toString())
            } ?: onTextChanged(null)

        }
    })
}
fun TextView.applyTextShader(
    colors: List<Int> = listOf(
        Color.parseColor("#3363F2"), Color.parseColor("#FF48E0")
    )
) {
    val width = paint?.measureText(text.toString())
    val textShader: Shader = LinearGradient(
        0f, 0f, width ?: 0f, textSize, colors.toIntArray(), null, Shader.TileMode.REPEAT
    )

    paint.setShader(textShader)
}
fun Fragment.hideKeyboard(view: View?): Boolean {
    val inputMethodManager =
        view?.context?.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as? InputMethodManager
    return inputMethodManager?.hideSoftInputFromWindow(view.windowToken, 0) ?: false
}

fun Context.showEmailChooser(supportEmail: String, subject: String, body: String? = null) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(supportEmail))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }

    try {
        val chooser = Intent.createChooser(intent, "Send Email")
        if (chooser.resolveActivity(packageManager) != null) {
            startActivity(chooser)
        } else {
            Toast.makeText(this, "No email client found", Toast.LENGTH_SHORT).show()
        }
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(this, "No email client found", Toast.LENGTH_SHORT).show()
    }
}

fun View.animateView() {
    val scaleX: ObjectAnimator = ObjectAnimator.ofFloat(this, "scaleX", 0.9f, 1.1f)
    val scaleY: ObjectAnimator = ObjectAnimator.ofFloat(this, "scaleY", 0.9f, 1.1f)
    scaleX.repeatCount = ObjectAnimator.INFINITE
    scaleX.repeatMode = ObjectAnimator.REVERSE
    scaleY.repeatCount = ObjectAnimator.INFINITE
    scaleY.repeatMode = ObjectAnimator.REVERSE
    val scaleAnim = AnimatorSet()
    scaleAnim.duration = 1000
    scaleAnim.play(scaleX).with(scaleY)
    scaleAnim.start()

}

fun View.animateMinorly() {
    val scaleX: ObjectAnimator = ObjectAnimator.ofFloat(this, "scaleX", 0.95f, 1.05f)
    val scaleY: ObjectAnimator = ObjectAnimator.ofFloat(this, "scaleY", 0.95f, 1.05f)
    scaleX.repeatCount = ObjectAnimator.INFINITE
    scaleX.repeatMode = ObjectAnimator.REVERSE
    scaleY.repeatCount = ObjectAnimator.INFINITE
    scaleY.repeatMode = ObjectAnimator.REVERSE
    val scaleAnim = AnimatorSet()
    scaleAnim.duration = 1000
    scaleAnim.play(scaleX).with(scaleY)
    scaleAnim.start()
}

fun Fragment.onBackPressDispatcherOverride(func: () -> Unit) {
    // This callback will only be called when MyFragment is at least Started.
    val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            func.invoke()
        }
    }
    requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
}

fun TextView.applyTextShader(
    colors: List<Int> = listOf(
        Color.parseColor("#3363F2"), Color.parseColor("#FF48E0")
    )
) {
    val width = paint?.measureText(text.toString())
    val textShader: Shader = LinearGradient(
        0f, 0f, width ?: 0f, textSize, colors.toIntArray(), null, Shader.TileMode.REPEAT
    )

    paint.setShader(textShader)
}
fun String.getDayOfWeekOrToday(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val date = LocalDate.parse(this, formatter)
    val today = LocalDate.now()

    return when {
        date == today -> "Today"
        else -> {
            when (date.dayOfWeek) {
                DayOfWeek.MONDAY -> "Monday"
                DayOfWeek.TUESDAY -> "Tuesday"
                DayOfWeek.WEDNESDAY -> "Wednesday"
                DayOfWeek.THURSDAY -> "Thursday"
                DayOfWeek.FRIDAY -> "Friday"
                DayOfWeek.SATURDAY -> "Saturday"
                DayOfWeek.SUNDAY -> "Sunday"
                else -> "-----"
            }
        }
    }
}
fun String.getHoursAndMinutesFromDateTime(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    val dateTime = LocalDateTime.parse(this, formatter)
    return "${dateTime.hour}:${dateTime.minute}"
}

fun EditText.onSearch(callback: () -> Unit) {
    setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            callback.invoke()
            return@setOnEditorActionListener true
        }
        false
    }
}
fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)
fun Fragment.onBackPressDispatcherOverride(func:() -> Unit){
    // This callback will only be called when MyFragment is at least Started.
    val callback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                func.invoke()
            }
        }
    requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
}

inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
    SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
}

inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? = when {
    SDK_INT >= 33 -> getParcelable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelable(key) as? T
}
suspend fun String.saveAudioToGallery(
    context: Context, mimeType: String = "audio/x-wav", done: (String?) -> Unit, error: () -> Unit
) = withContext(Dispatchers.IO) {
    var savedPath: String? = null
    val contentValues = ContentValues().apply {
        put(MediaStore.Audio.Media.DISPLAY_NAME, "Vc_${System.currentTimeMillis()}.wav")
        put(MediaStore.Audio.Media.MIME_TYPE, mimeType)
        put(MediaStore.Audio.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
    }
    val collection: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        contentValues.put(
            MediaStore.Audio.Media.RELATIVE_PATH,
            "${Environment.DIRECTORY_MUSIC}"
        )
        MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    } else {
        val directory =
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "Vc_${System.currentTimeMillis()}.wav")
        /*if (!directory.exists()) {
            directory.mkdirs()
        }*/
        contentValues.put(MediaStore.Audio.Media.DATA, directory.absolutePath)
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    }

    val contentResolver = context.contentResolver

    try {
        // Insert the audio file into the MediaStore and get the URI
        val audioUri = contentResolver.insert(collection, contentValues)
        Log.d("inputStram", "saveAudioToGalleryUri:$audioUri ")
try {
    audioUri?.let { uri ->
        // Open an output stream using the ContentResolver
        contentResolver.openOutputStream(uri)?.use { os ->
            // Open input stream from the file path
            val file = File(this@saveAudioToGallery)
            val inputStream = FileInputStream(file)
            // Write audio data to the output stream
            inputStream.copyTo(os)
            Log.d("inputStram", "saveAudioToGallery:$savedPath ")
            savedPath = file.absolutePath // Save the file path for later use
            Log.d("inputStram", "saveAudioToGallery:$savedPath ")
            os.close()
            inputStream.close()
        }
    }
}catch(e: FileNotFoundException){
    Log.d("TAG", "saveAudioToGallery: ")
}


    } catch (e: Exception) {
        // Handle exceptions
        Log.e("TAG", "saveAudioToGallery: ${e.message}")

    }

    withContext(Dispatchers.Main) {
        if (savedPath != null) {
            Log.d("TAGGing", "saveAudioToGallery:$savedPath ")
            done.invoke(savedPath)
        } else {
            Toast.makeText(context.applicationContext, "Failed to save audio to gallery", Toast.LENGTH_SHORT).show()
            error.invoke()
        }
    }
}

fun Context.getAudiosFromFolder(folderPath: String): List<AudioDetails> {
    val audioList = mutableListOf<AudioDetails>()
    val selection = "${MediaStore.Audio.Media.DATA} like ? AND ${MediaStore.Audio.Media.DISPLAY_NAME} LIKE 'Vc_%'"
    val selectionArgs = arrayOf("$folderPath%")
    val projection = arrayOf(
        MediaStore.Audio.Media.DISPLAY_NAME,
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.DATA
    )
    val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"
    contentResolver.query(
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        projection,
        selection,
        selectionArgs,
        sortOrder
    )?.use { cursor ->
        val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
        val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
        while (cursor.moveToNext()) {
            val name = cursor.getString(nameColumn)
            val id = cursor.getLong(idColumn)
            val duration = cursor.getLong(durationColumn)
            val path = cursor.getString(pathColumn)
            // Check if the file exists using the file path
            val file = File(path)
            if (file.exists()) {
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                val audioDetails = AudioDetails(name, contentUri, path)
                audioList.add(audioDetails)
            }
        }
    }
    return audioList
}
data class AudioDetails(val name: String, val contentUri: Uri, val path: String)
data class AudioModel(
    val id: Long,
    val title: String?,
    val artist: String?,
    val data: String?,
    val uri: Uri
)
