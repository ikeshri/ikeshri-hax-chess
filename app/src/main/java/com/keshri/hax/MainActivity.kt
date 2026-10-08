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

    private var selectedSide: String = "WHITE"

    private val captureLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->

            val captureData = result.data

            if (result.resultCode == Activity.RESULT_OK && captureData != null) {

                val serviceIntent =
                    Intent(this@MainActivity, LiveOverlayService::class.java)

                serviceIntent.putExtra(
                    "RESULT_CODE",
                    result.resultCode
                )

                serviceIntent.putExtra(
                    "DATA_INTENT",
                    captureData
                )

                serviceIntent.putExtra(
                    "SIDE",
                    selectedSide
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent)
                } else {
                    startService(serviceIntent)
                }

                Toast.makeText(
                    this,
                    "Keshri Hax analysis started",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                Toast.makeText(
                    this,
                    "Screen capture permission cancelled",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        buildPremiumInterface()
    }

    private fun buildPremiumInterface() {

        val root = LinearLayout(this)

        root.orientation = LinearLayout.VERTICAL
        root.gravity = Gravity.CENTER_HORIZONTAL

        root.setPadding(30, 55, 30, 35)

        val background = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(
                Color.rgb(4, 7, 15),
                Color.rgb(7, 15, 27),
                Color.rgb(3, 6, 12)
            )
        )

        root.background = background

        val title = TextView(this)

        title.text = "♛  KESHRI HAX"
        title.textSize = 30f
        title.gravity = Gravity.CENTER
        title.setTextColor(Color.rgb(0, 255, 165))
        title.typeface =
            Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)

        title.setShadowLayer(
            18f,
            0f,
            0f,
            Color.rgb(0, 255, 165)
        )

        root.addView(
            title,
            LinearLayout.LayoutParams(
                -1,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val subtitle = TextView(this)

        subtitle.text = "NEURAL CHESS ANALYSIS SYSTEM"
        subtitle.textSize = 11f
        subtitle.gravity = Gravity.CENTER
        subtitle.setTextColor(Color.rgb(100, 190, 220))
        subtitle.typeface = Typeface.MONOSPACE

        subtitle.setPadding(0, 8, 0, 28)

        root.addView(
            subtitle,
            LinearLayout.LayoutParams(
                -1,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val coreCard = createCard()

        val coreTitle = TextView(this)

        coreTitle.text = "●  ANALYSIS CORE"
        coreTitle.textSize = 15f
        coreTitle.setTextColor(Color.rgb(0, 255, 165))
        coreTitle.typeface =
            Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)

        coreCard.addView(coreTitle)

        val status = TextView(this)

        status.text =
            """
            VISION ENGINE       READY
            FEN PROCESSOR       READY
            POSITION CHECK      READY
            STOCKFISH ENGINE    READY
            OVERLAY SYSTEM      READY
            """.trimIndent()

        status.textSize = 11f
        status.setTextColor(Color.rgb(150, 165, 180))
        status.typeface = Typeface.MONOSPACE
        status.setPadding(0, 18, 0, 0)

        coreCard.addView(status)

        root.addView(
            coreCard,
            LinearLayout.LayoutParams(
                -1,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        addSpace(root, 18)

        val sideTitle = TextView(this)

        sideTitle.text = "YOUR SIDE"
        sideTitle.textSize = 12f
        sideTitle.gravity = Gravity.CENTER
        sideTitle.setTextColor(Color.LTGRAY)
        sideTitle.typeface = Typeface.MONOSPACE

        root.addView(sideTitle)

        addSpace(root, 10)

        val sideRow = LinearLayout(this)

        sideRow.orientation = LinearLayout.HORIZONTAL

        val whiteButton =
            createSideButton("♙  WHITE")

        val blackButton =
            createSideButton("♟  BLACK")

        sideRow.addView(
            whiteButton,
            LinearLayout.LayoutParams(
                0,
                65,
                1f
            ).apply {
                rightMargin = 8
            }
        )

        sideRow.addView(
            blackButton,
            LinearLayout.LayoutParams(
                0,
                65,
                1f
            ).apply {
                leftMargin = 8
            }
        )

        root.addView(
            sideRow,
            LinearLayout.LayoutParams(
                -1,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        fun refreshSideButtons() {

            setSelectedButton(
                whiteButton,
                selectedSide == "WHITE"
            )

            setSelectedButton(
                blackButton,
                selectedSide == "BLACK"
            )
        }

        whiteButton.setOnClickListener {

            selectedSide = "WHITE"
            refreshSideButtons()
        }

        blackButton.setOnClickListener {

            selectedSide = "BLACK"
            refreshSideButtons()
        }

        refreshSideButtons()

        addSpace(root, 22)

        val launchButton =
            createPrimaryButton("SCAN BOARD  •  START ANALYSIS")

        launchButton.setOnClickListener {

            startCaptureFlow()
        }

        root.addView(
            launchButton,
            LinearLayout.LayoutParams(
                -1,
                70
            )
        )

        addSpace(root, 12)

        val stopButton =
            createSecondaryButton("STOP ANALYSIS")

        stopButton.setOnClickListener {

            stopService(
                Intent(
                    this@MainActivity,
                    LiveOverlayService::class.java
                )
            )

            Toast.makeText(
                this,
                "Analysis stopped",
                Toast.LENGTH_SHORT
            ).show()
        }

        root.addView(
            stopButton,
            LinearLayout.LayoutParams(
                -1,
                58
            )
        )

        addSpace(root, 22)

        val infoCard = createCard()

        val info = TextView(this)

        info.text =
            """
            OFFLINE ANALYSIS MODE

            • Screen board detection
            • 8×8 square extraction
            • Piece recognition
            • FEN generation
            • Position validation
            • Stockfish analysis
            • Floating result panel
            """.trimIndent()

        info.textSize = 11f
        info.setTextColor(Color.rgb(135, 150, 165))
        info.typeface = Typeface.MONOSPACE
        info.setLineSpacing(4f, 1f)

        infoCard.addView(info)

        root.addView(
            infoCard,
            LinearLayout.LayoutParams(
                -1,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        addSpace(root, 20)

        val developer = TextView(this)

        developer.text = "KESHRI HAX  •  OFFLINE CHESS INTELLIGENCE"
        developer.textSize = 9f
        developer.gravity = Gravity.CENTER
        developer.setTextColor(Color.rgb(70, 90, 105))
        developer.typeface = Typeface.MONOSPACE

        root.addView(
            developer,
            LinearLayout.LayoutParams(
                -1,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        setContentView(root)
    }

    private fun startCaptureFlow() {

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            !Settings.canDrawOverlays(this)
        ) {

            val overlayIntent =
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )

            startActivity(overlayIntent)

            Toast.makeText(
                this,
                "Allow Display over other apps, then press Start again.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val manager =
            getSystemService(
                MEDIA_PROJECTION_SERVICE
            ) as MediaProjectionManager

        captureLauncher.launch(
            manager.createScreenCaptureIntent()
        )
    }

    private fun createCard(): LinearLayout {

        val card = LinearLayout(this)

        card.orientation = LinearLayout.VERTICAL
        card.setPadding(24, 22, 24, 22)

        val drawable = GradientDrawable()

        drawable.setColor(
            Color.rgb(13, 20, 32)
        )

        drawable.cornerRadius = 24f

        drawable.setStroke(
            1,
            Color.rgb(28, 52, 67)
        )

        card.background = drawable

        return card
    }

    private fun createSideButton(
        label: String
    ): Button {

        val button = Button(this)

        button.text = label
        button.textSize = 13f
        button.typeface =
            Typeface.create(
                Typeface.MONOSPACE,
                Typeface.BOLD
            )

        button.isAllCaps = false

        return button
    }

    private fun setSelectedButton(
        button: Button,
        selected: Boolean
    ) {

        val drawable = GradientDrawable()

        drawable.cornerRadius = 18f

        if (selected) {

            drawable.setColor(
                Color.rgb(0, 105, 78)
            )

            drawable.setStroke(
                2,
                Color.rgb(0, 255, 165)
            )

            button.setTextColor(
                Color.WHITE
            )

        } else {

            drawable.setColor(
                Color.rgb(16, 24, 36)
            )

            drawable.setStroke(
                1,
                Color.rgb(45, 60, 75)
            )

            button.setTextColor(
                Color.rgb(170, 185, 195)
            )
        }

        button.background = drawable
    }

    private fun createPrimaryButton(
        label: String
    ): Button {

        val button = Button(this)

        button.text = label
        button.textSize = 13f
        button.isAllCaps = false

        button.typeface =
            Typeface.create(
                Typeface.MONOSPACE,
                Typeface.BOLD
            )

        button.setTextColor(
            Color.rgb(2, 10, 12)
        )

        val drawable = GradientDrawable()

        drawable.setColor(
            Color.rgb(0, 255, 165)
        )

        drawable.cornerRadius = 22f

        button.background = drawable

        return button
    }

    private fun createSecondaryButton(
        label: String
    ): Button {

        val button = Button(this)

        button.text = label
        button.textSize = 12f
        button.isAllCaps = false

        button.typeface =
            Typeface.create(
                Typeface.MONOSPACE,
                Typeface.BOLD
            )

        button.setTextColor(
            Color.rgb(255, 100, 100)
        )

        val drawable = GradientDrawable()

        drawable.setColor(
            Color.rgb(24, 18, 28)
        )

        drawable.setStroke(
            1,
            Color.rgb(110, 45, 55)
        )

        drawable.cornerRadius = 20f

        button.background = drawable

        return button
    }

    private fun addSpace(
        root: LinearLayout,
        height: Int
    ) {

        root.addView(
            View(this),
            LinearLayout.LayoutParams(
                1,
                height
            )
        )
    }
}
