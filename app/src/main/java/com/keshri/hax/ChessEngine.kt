package com.keshri.hax

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class ChessEngine(private val binaryPath: String) {

    fun computeBestMove(fen: String, moveTimeMs: Int = 300): String {
        return try {
            val process = ProcessBuilder(binaryPath).redirectErrorStream(true).start()
            val writer = OutputStreamWriter(process.outputStream)
            val reader = BufferedReader(InputStreamReader(process.inputStream))

            writer.write("uci\n")
            writer.write("isready\n")
            writer.write("position fen $fen\n")
            writer.write("go movetime $moveTimeMs\n")
            writer.flush()

            var line: String?
            var bestMove = "Searching..."

            while (reader.readLine().also { line = it } != null) {
                if (line!!.startsWith("bestmove")) {
                    val parts = line!!.split(" ")
                    if (parts.size >= 2) {
                        bestMove = parts[1]
                    }
                    break
                }
            }

            writer.write("quit\n")
            writer.flush()
            process.destroy()

            formatMoveString(bestMove)
        } catch (e: Exception) {
            "Engine Error"
        }
    }

    private fun formatMoveString(uci: String): String {
        if (uci.length < 4) return uci
        val from = uci.substring(0, 2)
        val to = uci.substring(2, 4)
        return "$from ➔ $to"
    }
}
