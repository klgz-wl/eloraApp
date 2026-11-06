package yumo.achat.app.ui.login

import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import yumo.achat.app.BuildConfig
import yumo.achat.app.R

private val cyan = Color(0xFF00E5F5)

private enum class LoginPage { Choice, Account, Terms, Privacy }

@Composable
fun LoginScreen(
    submitting: Boolean,
    serverError: String?,
    onDeviceLogin: () -> Unit,
    onAccountLogin: (String, String) -> Unit,
    onNavigate: () -> Unit = {},
) {
    var page by rememberSaveable { mutableStateOf(LoginPage.Choice) }
    var agreed by rememberSaveable { mutableStateOf(true) }
    var account by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var localError by remember { mutableStateOf<Int?>(null) }
    val keyboard = LocalSoftwareKeyboardController.current
    val focus = LocalFocusManager.current
    fun closeKeyboard() { keyboard?.hide(); focus.clearFocus() }
    fun submitAccount() {
        if (submitting) return
        closeKeyboard()
        localError = if (!agreed) R.string.login_agreement_required
        else if (!validLoginInput(account, password)) {
            if (account.length !in 3..50) R.string.login_account_invalid else R.string.login_password_invalid
        } else null
        if (localError == null) onAccountLogin(account, password)
    }
    BackHandler(page != LoginPage.Choice) {
        page = LoginPage.Choice
        localError = null
        onNavigate()
    }
    if (page == LoginPage.Terms || page == LoginPage.Privacy) {
        PolicyWebView(
            url = if (page == LoginPage.Terms) BuildConfig.TERMS_OF_SERVICE_URL else BuildConfig.PRIVACY_POLICY_URL,
            onBack = { page = LoginPage.Choice; onNavigate() },
        )
        return
    }
    if (page == LoginPage.Choice) {
        LoginWelcomeScreen(
            agreed = agreed,
            submitting = submitting,
            error = localError?.let { stringResource(it) } ?: serverError,
            onAgreementChange = { agreed = it; localError = null },
            onDeviceLogin = {
                closeKeyboard()
                localError = if (agreed) null else R.string.login_agreement_required
                if (agreed) onDeviceLogin()
            },
            onAccount = { page = LoginPage.Account; localError = null; onNavigate() },
            onTerms = { page = LoginPage.Terms; onNavigate() },
            onPrivacy = { page = LoginPage.Privacy; onNavigate() },
        )
        return
    }
    AccountLoginScreen(
        account = account, password = password, showPassword = showPassword,
        submitting = submitting, error = localError?.let { stringResource(it) } ?: serverError,
        onAccountChange = { account = cleanAccountInput(it); localError = null },
        onPasswordChange = { password = cleanPasswordInput(it); localError = null },
        onTogglePassword = { showPassword = !showPassword },
        onSubmit = ::submitAccount,
        onBack = { page = LoginPage.Choice; localError = null; closeKeyboard(); onNavigate() },
    )
}

@Composable private fun BackCircle(onClick: () -> Unit) {
    val description = stringResource(R.string.login_back)
    Box(Modifier.size(38.dp).clip(CircleShape).background(Color(0xFF111429)).clickable(onClick = onClick)
        .semantics { contentDescription = description }, contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(18.dp)) {
            drawLine(Color.White, Offset(size.width * .68f, size.height * .18f), Offset(size.width * .32f, size.height * .5f), 2.dp.toPx(), StrokeCap.Round)
            drawLine(Color.White, Offset(size.width * .32f, size.height * .5f), Offset(size.width * .68f, size.height * .82f), 2.dp.toPx(), StrokeCap.Round)
        }
    }
}

@Composable internal fun PolicyWebView(url: String, onBack: () -> Unit) {
    var loading by remember(url) { mutableStateOf(true) }
    var failed by remember(url) { mutableStateOf(false) }
    var view by remember(url) { mutableStateOf<WebView?>(null) }
    val loadingDescription = stringResource(R.string.login_loading_page)
    DisposableEffect(view) { onDispose { view?.destroy() } }
    Column(Modifier.fillMaxSize().background(Color(0xFF090C1D)).statusBarsPadding().navigationBarsPadding()) {
        Box(Modifier.padding(16.dp)) { BackCircle(onBack) }
        Box(Modifier.fillMaxSize()) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = false
                        settings.domStorageEnabled = true
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) { loading = false }
                            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                                if (request?.isForMainFrame == true) { failed = true; loading = false }
                            }
                        }
                        loadUrl(url)
                        view = this
                    }
                }, modifier = Modifier.fillMaxSize(),
            )
            if (loading) CircularProgressIndicator(
                Modifier.align(Alignment.Center).semantics { contentDescription = loadingDescription },
                color = cyan,
            )
            if (failed) Text(stringResource(R.string.login_page_unavailable), Modifier.align(Alignment.Center), color = Color.White)
        }
    }
}
