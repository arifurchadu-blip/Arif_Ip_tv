package com.arif.iptv

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

data class Channel(val name: String, val url: String)

class MainActivity : AppCompatActivity() {
    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView
    private lateinit var status: TextView
    private lateinit var list: ListView
    private lateinit var urlInput: EditText
    private val channels = mutableListOf<Channel>()
    private lateinit var adapter: ArrayAdapter<String>

    private val openM3u = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            contentResolver.openInputStream(it)?.bufferedReader()?.use { reader ->
                parsePlaylist(reader.readText())
            } ?: showStatus("Could not read the selected file.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        playerView = findViewById(R.id.playerView)
        status = findViewById(R.id.status)
        list = findViewById(R.id.channelList)
        urlInput = findViewById(R.id.playlistUrl)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        list.adapter = adapter

        player = ExoPlayer.Builder(this).build().also { exo ->
            playerView.player = exo
            exo.addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    showStatus("Playback error: ${error.errorCodeName}. Check the stream URL, network and access permission.")
                }
            })
        }

        findViewById<Button>(R.id.loadUrl).setOnClickListener {
            val url = urlInput.text.toString().trim()
            if (url.isBlank()) showStatus("Please enter a playlist URL.")
            else loadPlaylistUrl(url)
        }
        findViewById<Button>(R.id.openFile).setOnClickListener {
            openM3u.launch(arrayOf("audio/*", "application/vnd.apple.mpegurl", "application/x-mpegURL", "text/plain", "*/*"))
        }
        list.setOnItemClickListener { _, _, position, _ -> playChannel(position) }
    }

    private fun loadPlaylistUrl(url: String) {
        showStatus("Loading playlist…")
        thread {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.connectTimeout = 15000
                connection.readTimeout = 20000
                connection.setRequestProperty("User-Agent", "Arif_Ip_Tv/1.0")
                val text = connection.inputStream.bufferedReader().use { it.readText() }
                connection.disconnect()
                runOnUiThread { parsePlaylist(text, url) }
            } catch (e: Exception) {
                runOnUiThread { showStatus("Playlist load failed: ${e.message ?: "network or URL error"}") }
            }
        }
    }

    private fun parsePlaylist(text: String, sourceUrl: String? = null) {
        val parsed = mutableListOf<Channel>()
        var pendingName = "Channel"
        val base = sourceUrl?.substringBeforeLast('/', "")?.let { if (it.isBlank()) "" else "$it/" } ?: ""
        text.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.forEach { line ->
            when {
                line.startsWith("#EXTINF", true) -> {
                    pendingName = line.substringAfterLast(',').trim().ifBlank { "Channel ${parsed.size + 1}" }
                }
                !line.startsWith("#") -> {
                    val stream = if (line.startsWith("http://") || line.startsWith("https://") || line.startsWith("content://")) line
                    else if (base.isNotEmpty()) base + line else line
                    parsed.add(Channel(pendingName, stream))
                    pendingName = "Channel ${parsed.size + 1}"
                }
            }
        }
        channels.clear()
        channels.addAll(parsed)
        adapter.clear()
        adapter.addAll(channels.map { it.name })
        adapter.notifyDataSetChanged()
        showStatus(if (channels.isEmpty()) "No valid channels found. Check that this is an M3U playlist." else "${channels.size} channel(s) loaded. Tap a channel to play.")
        if (channels.isNotEmpty()) playChannel(0)
    }

    private fun playChannel(position: Int) {
        val channel = channels.getOrNull(position) ?: return
        try {
            player?.setMediaItem(MediaItem.fromUri(channel.url))
            player?.prepare()
            player?.play()
            showStatus("Playing: ${channel.name}")
        } catch (e: Exception) {
            showStatus("Cannot play ${channel.name}: ${e.message}")
        }
    }

    private fun showStatus(message: String) {
        if (::status.isInitialized) status.text = message
    }

    override fun onStop() {
        super.onStop()
        player?.pause()
    }

    override fun onDestroy() {
        playerView.player = null
        player?.release()
        player = null
        super.onDestroy()
    }
}
