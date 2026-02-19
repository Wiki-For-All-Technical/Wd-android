package com.example.myapp.ui.screens

import android.annotation.SuppressLint
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebResourceError
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.myapp.ui.components.SidebarNavigationDrawer
import com.example.myapp.viewmodel.MainViewModel

/** Official Wikidata login page - same as on the web. */
private const val WIKIDATA_LOGIN_URL = "https://www.wikidata.org/wiki/Special:UserLogin?returnto=Main_Page"

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LoginScreen(viewModel: MainViewModel) {
    SidebarNavigationDrawer(viewModel = viewModel) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.userAgentString = "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                    // Ensure cookies work for cross-site login redirects
                    CookieManager.getInstance().setAcceptCookie(true)
                    CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            return false
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            if (url == null || !url.contains("wikidata.org")) return
                            // Skip when still on login/create-account form
                            if (url.contains("Special:UserLogin") ||
                                url.contains("Special:CreateAccount") ||
                                url.contains("UserLogin") ||
                                url.contains("CreateAccount")
                            ) return
                            // User navigated away from login form - verify via Wikidata API
                            CookieManager.getInstance().flush()
                            viewModel.checkLoginFromCookies()
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?
                        ) {
                            super.onReceivedError(view, request, error)
                            android.util.Log.e("LoginWebView", "Error: ${error?.description}")
                        }
                    }
                    loadUrl(WIKIDATA_LOGIN_URL)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
