package com.keshri.hax

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.*
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream

class LiveOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private var engine: ChessEngine? = null

    private var previousBoardHash: Int = 0
    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())

    override fun onCreate() {
        super.onCreate()
        setupStockfish()
        createFloatingUI()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val resultCode = intent?.getIntExtra("RESULT_CODE", Activity.RESULT_CANCELED) ?: Activity.RESULT_CANCELED
        val dataIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra("DATA_INTENT", Intent::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent?.getParcelableExtra("DATA_INTENT")
        }

        if (resultCode == Activity.RESULT_OK && dataIntent != null) {
            startLiveCapture(resultCode, dataIntent)
        }
        return START_NOT_STICKY
    }

    private fun startLiveCapture(resultCode: Int, data: Intent) {
        val mpManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = mpManager.getMediaProjection(resultCode, data)

        val metrics = resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels

        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "LiveHaxEye",
            width, height, metrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface, null, null
        )

        val txtMove = floatingView.findViewById<TextView>(R.id.txtNextMove)
        val txtStatus = floatingView.findViewById<TextView>(R.id.txtStatus)

        imageReader?.setOnImageAvailableListener({ reader ->
            val image = reader.acquireLatestImage() ?: return@setOnImageAvailableListener
            try {
                // 1. लाइव फ्रेम से बोर्ड का Hash चेक करना (Pixel Diffing)
                val planes = image.planes
                val buffer = planes[0].buffer
                val currentHash = buffer.hashCode()

                // अगर पिक्सेल बदले हैं मतलब चाल चली गई है
                if (currentHash != previousBoardHash) {
                    previousBoardHash = currentHash

                    serviceScope.launch {
                        // यहाँ बोर्ड से FEN एक्सट्रेक्ट होता है (डिफ़ॉल्ट FEN टेस्ट/फ्लो के लिए)
                        val best = engine?.computeBestMove("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1", 300)

                        withContext(Dispatchers.Main) {
                            txtStatus.text = "⚡ MOVE FOUND"
                            txtMove.text = best
                        }
                    }
                }
            } finally {
                image.close()
            }
        }, Handler(Looper.getMainLooper()))
    }

    private fun createFloatingUI() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
       floatingView = LayoutInflater.from(this).inflate(R.layout.ikeshri_panel, null) 

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 80
            y = 220
        }

        windowManager.addView(floatingView, params)

        val btnSettings = floatingView.findViewById<TextView>(R.id.btnSettingsToggle)
        val settingsLayout = floatingView.findViewById<LinearLayout>(R.id.settingsContainer)

        btnSettings.setOnClickListener {
            settingsLayout.visibility = if (settingsLayout.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        // ड्रैग हैंडलर
        floatingView.setOnTouchListener(object : View.OnTouchListener {
            var initX = 0; var initY = 0; var touchX = 0f; var touchY = 0f
            override fun onTouch(v: View?, e: MotionEvent): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initX = params.x; initY = params.y
                        touchX = e.rawX; touchY = e.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initX + (e.rawX - touchX).toInt()
                        params.y = initY + (e.rawY - touchY).toInt()
                        windowManager.updateViewLayout(floatingView, params)
                        return true
                    }
                }
                return false
            }
        })
    }

    private fun setupStockfish() {
        val file = File(filesDir, "stockfish")
        if (!file.exists()) {
            try {
                assets.open("stockfish").use { input ->
                    FileOutputStream(file).use { output -> input.copyTo(output) }
                }
                file.setExecutable(true)
            } catch (e: Exception) { e.printStackTrace() }
        }
        engine = ChessEngine(file.absolutePath)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        virtualDisplay?.release()
        imageReader?.close()
        mediaProjection?.stop()
        if (::floatingView.isInitialized) windowManager.removeView(floatingView)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
