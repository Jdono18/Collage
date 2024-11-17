package com.example.collage

import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media
import android.util.Log
import android.view.View
import android.widget.ImageButton
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Date

private const val TAG = "MAIN_ACTIVITY"

class MainActivity : AppCompatActivity() {

//    private lateinit var imageButton1: ImageButton
    private lateinit var imageButtons: List<ImageButton>
    private lateinit var mainView: View

    private var photoPaths: ArrayList<String?> = arrayListOf(null, null, null, null)  // array list instead of kotlin list due to being saved in instanceState
//    private var newPhotoPath: String? = null
//    private var visibleImagePath: String? = null

    private var whichImageIndex: Int? = null

    private var currentPhotoPath: String? = null

//    private val NEW_PHOTO_PATH_KEY = "new photo path key"
//    private val VISIBLE_IMAGE_PATH_KEY = "visible image path key"

    private val PHOTO_PATH_LIST_ARRAY_KEY = "photo path list key"
    private val IMAGE_INDEX_KEY = "image index key"
    private val CURRENT_PHOTO_PATH_KEY = "current photo path key"

    // result launcher cameraActivityLauncher - used to launch the camera app and receives the result
    private val cameraActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result -> handleImage(result)
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        whichImageIndex = savedInstanceState?.getInt(IMAGE_INDEX_KEY)
        currentPhotoPath = savedInstanceState?.getString(CURRENT_PHOTO_PATH_KEY)
        photoPaths = savedInstanceState?.getStringArrayList(PHOTO_PATH_LIST_ARRAY_KEY) ?: arrayListOf(null, null, null, null)

//        newPhotoPath = savedInstanceState?.getString(NEW_PHOTO_PATH_KEY)
//        visibleImagePath = savedInstanceState?.getString(VISIBLE_IMAGE_PATH_KEY)

        mainView = findViewById(R.id.content)
        imageButtons = listOf<ImageButton>(
            findViewById(R.id.imageButton1),
            findViewById(R.id.imageButton2),
            findViewById(R.id.imageButton3),
            findViewById(R.id.imageButton4)
        )

        for (imageButton in imageButtons) {
            imageButton.setOnClickListener { ib ->
                takePictureFor(ib as ImageButton)
            }
        }

//        imageButton1 = findViewById(R.id.imageButton1)
//        imageButton1.setOnClickListener {
//            takePicture()
//        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList(PHOTO_PATH_LIST_ARRAY_KEY, photoPaths)
        outState.putString(CURRENT_PHOTO_PATH_KEY, currentPhotoPath)
        whichImageIndex?.let { index -> outState.putInt(IMAGE_INDEX_KEY, index)}
    }

    private fun takePictureFor(imageButton: ImageButton) {  // before the intent is created and launched, setup a file to write the image to, find the uri for the file, provide uri as an extra as an intent

        val index = imageButtons.indexOf(imageButton) // indexOf - where in the list of imageButtons is this imageButton
        whichImageIndex = index

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)


        val (photoFile, photoFilePath) = createImageFile()

        if (photoFile != null) {
            currentPhotoPath = photoFilePath
            val photoUri = FileProvider.getUriForFile(
                this, "com.example.collage.fileprovider", photoFile
            )
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            cameraActivityLauncher.launch(takePictureIntent)
        }
    }

    private fun createImageFile(): Pair<File?, String?> {
        try {
            val dateTime = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val imageFileName = "COLLAGE_$dateTime"
            val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val file= File.createTempFile(imageFileName, ".jpg", storageDir)
            val filePath = file.absolutePath
            return file to filePath

        } catch (ex: IOException) {
            return null to null

        }
    }

    // defines function handleImage that takes ActivityResult parameter
    private fun handleImage(result: ActivityResult) {
        when (result.resultCode) {
            RESULT_OK -> {
                Log.d(TAG, "Result ok, user took picture image at $currentPhotoPath")
                whichImageIndex?.let { index -> photoPaths[index] = currentPhotoPath}
            }
            RESULT_CANCELED -> {
                Log.d(TAG, "Result cancelled, no picture taken")
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        Log.d(TAG, "on window focus changed $hasFocus visible image at ${currentPhotoPath}")
        if (hasFocus) {
//            visibleImagePath?.let { imagePath ->
//                loadImage(imageButton1,imagePath) }

            imageButtons.zip(photoPaths) { imageButton, photoPath ->
                photoPath?.let {
                    loadImage(imageButton, photoPath)
                }
            }
        }
    }

    private fun loadImage(imageButton: ImageButton, imagePath: String) {
        Picasso.get()
            .load(File(imagePath))
            .error(android.R.drawable.stat_notify_error)
            .fit()
            .centerCrop()
            .into(imageButton, object: Callback {
                override fun onSuccess() {
                    Log.d(TAG, "Loaded image $imagePath")
                }

                override fun onError(e: Exception?) {
                    Log.e(TAG, "Error loading image $imagePath", e)
                }
            })
    }
}