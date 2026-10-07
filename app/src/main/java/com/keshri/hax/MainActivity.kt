package com.keshri.hax

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : ComponentActivity() {

    private val captureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val serviceIntent = Intent(this, LiveOverlayService::class.java).apply {
                putExtra("RESULT_CODE", result.resultCode)
                putExtra("DATA_INTENT", result.data)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
            Toast.makeText(this, "⚡ KESHRI HAX ACTIVATED! Chess kholo ab.", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Permission Cancel ho gayi!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Deep Space Cyberpunk Dark Background
        val bgGradient = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(Color.parseColor("#070A13"), Color.parseColor("#0D1322"), Color.parseColor("#050811"))
        )

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            background = bgGradient
            setPadding(40, 50, 40, 40)
        }

        // 2. Neon Cyber Title
        val title = TextView(this).apply {
            text = "⚡ KESHRI HAX GM ⚡"
            textSize = 26f
            setTextColor(Color.parseColor("#00FFA3")) // Neon Emerald Green
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            gravity = Gravity.CENTER
            setShadowLayer(16f, 0f, 0f, Color.parseColor("#00FFA3"))
        }

        val subtitle = TextView(this).apply {
            text = "AI-POWERED GRANDMASTER ENGINE • LEVEL 20"
            textSize = 10f
            setTextColor(Color.parseColor("#38BDF8"))
            typeface = Typeface.MONOSPACE
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 32)
        }

        // 3. Status Glowing Card
        val statusCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(30, 24, 30, 24)
            val cardBg = GradientDrawable().apply {
                setColor(Color.parseColor("#111827"))
                cornerRadius = 24f
                setStroke(2, Color.parseColor("#1E293B"))
            }
            background = cardBg
        }

        val txtStatus = TextView(this).apply {
            text = "● STOCKFISH ENGINE: READY\n● LIVE SCREEN EYE: ARMED\n● CALCULATION DEPTH: UNBEATABLE"
            textSize = 11f
            setTextColor(Color.parseColor("#94A3B8"))
            typeface = Typeface.MONOSPACE
            lineSpacingMultiplier = 1.3f
            gravity = Gravity.CENTER
        }
        statusCard.addView(txtStatus)

        // 4. Main Launch Glowing Button
        val btnLaunch = Button(this).apply {
            text = "🚀 LAUNCH HAX OVERLAY"
            textSize = 15f
            setTextColor(Color.parseColor("#050811"))
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val btnBg = GradientDrawable().apply {
                setColor(Color.parseColor("#00FFA3"))
                cornerRadius = 28f
            }
            background = btnBg
            setPadding(30, 24, 30, 24)
            elevation = 16f
            setOnClickListener { checkOverlayAndLaunch() }
        }

        // 5. Stop Button
        val btnStop = Button(this).apply {
            text = "STOP OVERLAY"
            textSize = 12f
            setTextColor(Color.parseColor("#EF4444"))
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val stopBg = GradientDrawable().apply {
                setColor(Color.parseColor("#1E1B2E"))
                cornerRadius = 22f
                setStroke(2, Color.parseColor("#7F1D1D"))
            }
            background = stopBg
            setPadding(24, 16, 24, 16)
            setOnClickListener {
                stopService(Intent(this@MainActivity, LiveOverlayService::class.java))
                Toast.makeText(this@MainActivity, "Overlay Service Stopped", Toast.LENGTH_SHORT).show()
            }
        }

        // 6. Developer Section Header
        val devHeader = TextView(this).apply {
            text = "── DEVELOPER CONTACT ──"
            textSize = 11f
            setTextColor(Color.parseColor("#64748B"))
            typeface = Typeface.MONOSPACE
            gravity = Gravity.CENTER
            setPadding(0, 36, 0, 14)
        }

        // 7. Telegram Button
        val btnTg = Button(this).apply {
            text = "✈️ Telegram: @ikeshri"
            textSize = 12f
            setTextColor(Color.WHITE)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val tgBg = GradientDrawable().apply {
                setColor(Color.parseColor("#0284C7"))
                cornerRadius = 22f
            }
            background = tgBg
            setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/ikeshri")))
            }
        }

        // 8. Instagram Button
        val btnIg = Button(this).apply {
            text = "📸 Instagram: @_ikeshri"
            textSize = 12f
            setTextColor(Color.WHITE)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val igBg = GradientDrawable().apply {
                setColor(Color.parseColor("#BE185D"))
                cornerRadius = 22f
            }
            background = igBg
            setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/_ikeshri")))
            }
        }

        // View Assembly
        root.addView(title)
        root.addView(subtitle)
        root.addView(statusCard)
        root.addView(View(this).apply { layoutParams = LinearLayout.LayoutParams(1, 36) })
        root.addView(btnLaunch)
        root.addView(View(this).apply { layoutParams = LinearLayout.LayoutParams(1, 16) })
        root.addView(btnStop)
        root.addView(devHeader)
        root.addView(btnTg)
        root.addView(View(this).apply { layoutParams = LinearLayout.LayoutParams(1, 14) })
        root.addView(btnIg)

        setContentView(root)
    }

    private fun checkOverlayAndLaunch() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Pehle 'Display over other apps' allow karo!", Toast.LENGTH_LONG).show()
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
            return
        }

        val mpManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        captureLauncher.launch(mpManager.createScreenCaptureIntent())
    }
}
