package com.keshri.hax

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.AudioManager
import android.media.ImageReader
import android.media.ToneGenerator
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.*
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
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
    private var toneGenerator: ToneGenerator? = null

    override fun onCreate() {
        super.onCreate()
        startNotification()
        toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
        setupStockfish()
        createFloatingUI()
    }

    private fun startNotification() {
        val channelId = "ikeshri_hax_live"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "ikeshri hax Overlay",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("ikeshri hax")
            .setContentText("Chess GM Overlay is active")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .build()
        startForeground(1001, notification)
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
                val planes = image.planes
                val buffer = planes[0].buffer
                val currentHash = buffer.hashCode()

                if (currentHash != previousBoardHash) {
                    previousBoardHash = currentHash
                    serviceScope.launch {
                        val best = engine?.computeBestMove("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1", 500)
                        withContext(Dispatchers.Main) {
                            txtStatus.text = "⚡ LIVE MOVE"
                            txtMove.text = best
                            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
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

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 30
            y = 250
        }

        windowManager.addView(floatingView, params)

        val bubble = floatingView.findViewById<LinearLayout>(R.id.floatingBubble)
        val panel = floatingView.findViewById<LinearLayout>(R.id.expandedPanel)
        val btnClose = floatingView.findViewById<TextView>(R.id.btnClosePanel)
        val tabEngine = floatingView.findViewById<TextView>(R.id.tabEngine)
        val tabDev = floatingView.findViewById<TextView>(R.id.tabDev)
        val viewEngine = floatingView.findViewById<LinearLayout>(R.id.viewEngine)
        val viewDev = floatingView.findViewById<LinearLayout>(R.id.viewDev)
        val btnTelegram = floatingView.findViewById<Button>(R.id.btnTelegram)
        val btnInstagram = floatingView.findViewById<Button>(R.id.btnInstagram)

        bubble.setOnClickListener {
            bubble.visibility = View.GONE
            panel.visibility = View.VISIBLE
        }

        btnClose.setOnClickListener {
            panel.visibility = View.GONE
            bubble.visibility = View.VISIBLE
        }

        tabEngine.setOnClickListener {
            viewEngine.visibility = View.VISIBLE
            viewDev.visibility = View.GONE
        }

        tabDev.setOnClickListener {
            viewEngine.visibility = View.GONE
            viewDev.visibility = View.VISIBLE
        }

        btnTelegram.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/ikeshri")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        }

        btnInstagram.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/_ikeshri")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        }

        bubble.setOnTouchListener(object : View.OnTouchListener {
            var initX = 0; var initY = 0; var touchX = 0f; var touchY = 0f
            var isDrag = false

            override fun onTouch(v: View?, e: MotionEvent): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initX = params.x; initY = params.y
                        touchX = e.rawX; touchY = e.rawY
                        isDrag = false
                        return false
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (e.rawX - touchX).toInt()
                        val dy = (e.rawY - touchY).toInt()
                        if (Math.abs(dx) > 10 || Math.abs(dy) > 10) isDrag = true
                        params.x = initX + dx
                        params.y = initY + dy
                        windowManager.updateViewLayout(floatingView, params)
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (!isDrag) v?.performClick()
                        return true
                    }
                }
                return false
            }
        })
    }

    private fun setupStockfish() {
        val binFile = File(filesDir, "stockfish")
        if (!binFile.exists()) {
            try {
                assets.open("stockfish").use { input ->
                    FileOutputStream(binFile).use { output -> input.copyTo(output) }
                }
                binFile.setExecutable(true)
            } catch (e: Exception) { e.printStackTrace() }
        }

        val nnueFile = File(filesDir, "nnue.nnue")
        if (!nnueFile.exists()) {
            try {
                assets.open("nnue.nnue").use { input ->
                    FileOutputStream(nnueFile).use { output -> input.copyTo(output) }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }

        engine = ChessEngine(binFile.absolutePath, nnueFile.absolutePath)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        toneGenerator?.release()
        virtualDisplay?.release()
        imageReader?.close()
        mediaProjection?.stop()
        if (::floatingView.isInitialized) windowManager.removeView(floatingView)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
