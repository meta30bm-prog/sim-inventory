package br.com.siminventory.app

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.graphics.Color
import android.view.Gravity

class MainActivity : Activity() {
    private lateinit var webView: WebView
    private lateinit var progress: ProgressBar
    private lateinit var offline: LinearLayout
    private val appUrl = "https://sim-inventory-meta30bm-8124.vercel.app/"
    private val allowedHost = "sim-inventory-meta30bm-8124.vercel.app"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(16, 27, 49)
        window.navigationBarColor = Color.rgb(16, 27, 49)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(Color.WHITE) }
        progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply { max = 100; visibility = View.GONE }
        root.addView(progress, LinearLayout.LayoutParams(-1, 4))
        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            settings.setSupportZoom(false)
            settings.javaScriptCanOpenWindowsAutomatically = false
            settings.allowFileAccess = false
            settings.allowContentAccess = false
            CookieManager.getInstance().setAcceptCookie(true)
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, false)
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    val uri = request.url
                    return uri.scheme != "https" || uri.host != allowedHost
                }
                override fun onPageFinished(view: WebView, url: String) { offline.visibility = View.GONE }
                override fun onReceivedError(view: WebView, request: WebResourceRequest, error: android.webkit.WebResourceError) {
                    if (request.isForMainFrame) offline.visibility = View.VISIBLE
                }
            }
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView, newProgress: Int) {
                    progress.progress = newProgress
                    progress.visibility = if (newProgress in 1..99) View.VISIBLE else View.GONE
                }
            }
        }
        root.addView(webView, LinearLayout.LayoutParams(-1, 0, 1f))
        offline = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; setPadding(32, 32, 32, 32); visibility = View.GONE
            addView(TextView(this@MainActivity).apply { text = "Não foi possível conectar ao SIM Inventory. Verifique sua internet."; textSize = 16f; setTextColor(Color.rgb(35, 45, 60)); gravity = Gravity.CENTER })
            addView(Button(this@MainActivity).apply { text = "Tentar novamente"; setOnClickListener { webView.reload(); offline.visibility = View.GONE } })
        }
        root.addView(offline, LinearLayout.LayoutParams(-1, -1))
        setContentView(root)
        if (savedInstanceState != null) webView.restoreState(savedInstanceState) else webView.loadUrl(appUrl)
    }

    override fun onSaveInstanceState(outState: Bundle) { webView.saveState(outState); super.onSaveInstanceState(outState) }
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() { if (webView.canGoBack()) webView.goBack() else super.onBackPressed() }
    override fun onDestroy() { webView.destroy(); super.onDestroy() }
}
