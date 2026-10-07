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
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

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
            Toast.makeText(this, "⚡ HAX Panel Activated! Minimize and open Chess.", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Screen capture permission is required for auto-moves.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // बैकग्राउंड ग्रेडिएंट
        val bgGradient = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(Color.parseColor("#060814"), Color.parseColor("#0D1527"))
        )

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            background = bgGradient
            setPadding(48, 64, 48, 48)
        }

        // 1. ऐप हेडर
        val title = TextView(this).apply {
            text = "⚡ KESHRI HAX"
            textSize = 28f
            setTextColor(Color.parseColor("#00FFA3"))
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "GRANDMASTER ASSISTANT • STOCKFISH 16"
            textSize = 10f
            setTextColor(Color.parseColor("#38BDF8"))
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(0, 10, 0, 50)
        }

        // 2. स्टेटस कार्ड
        val statusCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(30, 24, 30, 24)
            val cardBg = GradientDrawable().apply {
                setColor(Color.parseColor("#131C31"))
                cornerRadius = 24f
                setStroke(2, Color.parseColor("#1E293B"))
            }
            background = cardBg
        }

        val txtStatus = TextView(this).apply {
            text = "SYSTEM ENGINE: READY\nACCURACY: 99.8% (SKILL LEVEL 20)"
            textSize = 11f
            setTextColor(Color.parseColor("#94A3B8"))
            typeface = Typeface.MONOSPACE
            gravity = Gravity.CENTER
        }
        statusCard.addView(txtStatus)

        // 3. लॉन्च बटन
        val btnLaunch = Button(this).apply {
            text = "🚀 LAUNCH HAX OVERLAY"
            textSize = 15f
            setTextColor(Color.BLACK)
            typeface = Typeface.DEFAULT_BOLD
            val btnBg = GradientDrawable().apply {
                setColor(Color.parseColor("#00FFA3"))
                cornerRadius = 28f
            }
            background = btnBg
            setOnClickListener { checkOverlayAndLaunch() }
        }

        // 4. सर्विस स्टॉप बटन
        val btnStop = Button(this).apply {
            text = "STOP OVERLAY"
            textSize = 12f
            setTextColor(Color.parseColor("#EF4444"))
            typeface = Typeface.DEFAULT_BOLD
            val stopBg = GradientDrawable().apply {
                setColor(Color.parseColor("#182234"))
                cornerRadius = 24f
            }
            background = stopBg
            setOnClickListener {
                stopService(Intent(this@MainActivity, LiveOverlayService::class.java))
                Toast.makeText(this@MainActivity, "Overlay service stopped", Toast.LENGTH_SHORT).show()
            }
        }

        // 5. सोशल / डेवलपर कॉन्टैक्ट सेक्शन
        val contactLabel = TextView(this).apply {
            text = "DEVELOPER CONTACT & SUPPORT"
            textSize = 10f
            setTextColor(Color.parseColor("#64748B"))
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(0, 40, 0, 14)
        }

        val btnTelegram = Button(this).apply {
            text = "✈️ Telegram @ikeshri"
            textSize = 12f
            setTextColor(Color.WHITE)
            val tgBg = GradientDrawable().apply {
                setColor(Color.parseColor("#0284C7"))
                cornerRadius = 20f
            }
            background = tgBg
            setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/ikeshri")))
            }
        }

        val btnInstagram = Button(this).apply {
            text = "📸 Instagram @_ikeshri"
            textSize = 12f
            setTextColor(Color.WHITE)
            val igBg = GradientDrawable().apply {
                setColor(Color.parseColor("#BE185D"))
                cornerRadius = 20f
            }
            background = igBg
            setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/_ikeshri")))
            }
        }

        // लेआउट असेंबली
        root.addView(title)
        root.addView(subtitle)
        root.addView(statusCard)
        root.addView(createSpacer(40))
        root.addView(btnLaunch)
        root.addView(createSpacer(16))
        root.addView(btnStop)
        root.addView(createSpacer(30))
        root.addView(contactLabel)
        root.addView(btnTelegram)
        root.addView(createSpacer(12))
        root.addView(btnInstagram)

        setContentView(root)
    }

    private fun createSpacer(height: Int): LinearLayout {
        return LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(1, height)
        }
    }

    private fun checkOverlayAndLaunch() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Enable 'Display over other apps' first", Toast.LENGTH_SHORT).show()
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
