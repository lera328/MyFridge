package com.example.myfridge2.Tools

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.SparseArray
import android.view.SurfaceHolder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.util.isNotEmpty
import com.example.myfridge2.databinding.ActivityScanerBinding
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request

class Scaner : AppCompatActivity() {
    lateinit var binding: ActivityScanerBinding

    private val reguestCodeCameraProgression = 1001
    private lateinit var cameraSource: CameraSource
    private lateinit var detector: BarcodeDetector
    var textQr = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            askForCameraPermission()
        } else {
            setupControls()
        }
    }

    private fun setupControls() {
        detector = BarcodeDetector.Builder(this).build()
        cameraSource = CameraSource.Builder(this, detector)
            .setAutoFocusEnabled(true).build()

        binding.cameraView.holder.addCallback(surgaseCallBack)
        detector.setProcessor(processor)
    }

    private fun askForCameraPermission() {
        ActivityCompat.requestPermissions(
            this@Scaner,
            arrayOf(android.Manifest.permission.CAMERA),
            reguestCodeCameraProgression
        )
    }

    private val surgaseCallBack = object : SurfaceHolder.Callback {

        @SuppressLint("MissingPermission")
        override fun surfaceCreated(holder: SurfaceHolder) {
            try {
                cameraSource.start(holder)

            } catch (exception: Exception) {
                Toast.makeText(applicationContext, exception.message, Toast.LENGTH_SHORT).show()
            }
        }
        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            cameraSource.stop()
        }
    }

    private val processor = object : Detector.Processor<Barcode> {
        override fun release() {
        }
        override fun receiveDetections(detections: Detector.Detections<Barcode>) {
            if (detections != null && detections.detectedItems.isNotEmpty()) {
                val qrCodes: SparseArray<Barcode> = detections.detectedItems
                val code = qrCodes.valueAt(0)
                textQr = code.displayValue

                val client = OkHttpClient()

                val url = "https://proverkacheka.com/api/v1/check/get"

                // Параметры формата запроса
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("token", "24417.ANKHaP74jX8796mPG")
                    .addFormDataPart("qrraw", textQr)
                    .build()

                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string().toString()

                val resultIntent = Intent()
                resultIntent.putExtra("textQr", responseBody)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()

            }
        }

    }


}