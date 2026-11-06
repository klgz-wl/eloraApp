package yumo.achat.app.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import yumo.achat.app.R
import yumo.achat.app.ui.theme.*

@Composable
internal fun AccountLoginScreen(
    account: String, password: String, showPassword: Boolean, submitting: Boolean, error: String?,
    onAccountChange: (String) -> Unit, onPasswordChange: (String) -> Unit,
    onTogglePassword: () -> Unit, onSubmit: () -> Unit, onBack: () -> Unit,
) {
    val passwordFocus = remember { FocusRequester() }
    val backLabel = stringResource(R.string.login_back)
    Box(Modifier.fillMaxSize().background(EloraAccountBackground).drawBehind {
        drawRect(Brush.radialGradient(listOf(EloraWelcomeViolet.copy(alpha = .24f),
            EloraWelcomeMagenta.copy(alpha = .10f), Color.Transparent),
            center = Offset(size.width * .5f, size.height * .20f), radius = size.width * 1.25f))
        drawRect(Brush.radialGradient(listOf(EloraWelcomeViolet.copy(alpha = .10f), Color.Transparent),
            center = Offset(0f, size.height), radius = size.width * .85f))
    }) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().imePadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)) {
                Box(Modifier.size(48.dp).clip(CircleShape).background(Color.White.copy(alpha = .08f))
                    .border(1.dp, Color.White.copy(alpha = .10f), CircleShape)
                    .clickable(role = Role.Button, onClick = onBack)
                    .semantics { contentDescription = backLabel }, contentAlignment = Alignment.Center) {
                    Image(painterResource(R.drawable.elora_account_back), null, Modifier.size(20.dp))
                }
            }
            Box(Modifier.weight(1f)) {
                Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(Modifier.height(45.dp))
                    EloraLoginOrb()
                    Spacer(Modifier.height(28.dp))
                    Text(stringResource(R.string.login_account_title), color = Color.White, fontSize = 24.sp,
                        lineHeight = 32.sp, fontWeight = FontWeight.Bold, letterSpacing = (-.6).sp, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(6.dp))
                    Text(stringResource(R.string.login_account_caption), color = EloraAccountMuted, fontSize = 12.sp,
                        lineHeight = 19.5.sp, textAlign = TextAlign.Center, modifier = Modifier.widthIn(max = 290.dp))
                    Spacer(Modifier.height(56.dp))
                    AccountLabel(stringResource(R.string.login_account_label))
                    Spacer(Modifier.height(6.dp))
                    AccountField(account, onAccountChange, R.string.login_account_hint, R.drawable.elora_account_user,
                        enabled = !submitting, imeAction = ImeAction.Next, onNext = { passwordFocus.requestFocus() })
                    Spacer(Modifier.height(16.dp))
                    AccountLabel(stringResource(R.string.login_password_label))
                    Spacer(Modifier.height(6.dp))
                    AccountField(password, onPasswordChange, R.string.login_password_hint, R.drawable.elora_account_lock,
                        enabled = !submitting, imeAction = ImeAction.Done, password = true,
                        showPassword = showPassword, onToggle = onTogglePassword, onDone = onSubmit, focusRequester = passwordFocus)
                    Spacer(Modifier.height(22.dp))
                    val submitLabel = stringResource(R.string.login_sign_in)
                    Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).clip(RoundedCornerShape(16.dp))
                        .background(Brush.horizontalGradient(listOf(EloraAccountButtonStart, EloraAccountButtonMiddle, EloraAccountButtonEnd)))
                        .clickable(enabled = !submitting, role = Role.Button, onClick = onSubmit)
                        .semantics { if (submitting) contentDescription = submitLabel }
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        if (submitting) CircularProgressIndicator(Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        else {
                            Text(submitLabel, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, letterSpacing = .4.sp)
                            Spacer(Modifier.width(8.dp))
                            Image(painterResource(R.drawable.elora_account_arrow), null, Modifier.size(20.dp))
                        }
                    }
                    error?.let { Text(it, color = EloraWelcomeError, fontSize = 12.sp, textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }

}

@Composable
private fun AccountLabel(text: String) {
    Text(text, color = EloraAccountLabel, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
        letterSpacing = .55.sp, modifier = Modifier.fillMaxWidth())
}

@Composable
private fun AccountField(
    value: String, onChange: (String) -> Unit, hint: Int, icon: Int, enabled: Boolean,
    imeAction: ImeAction, password: Boolean = false, showPassword: Boolean = false,
    onToggle: () -> Unit = {}, onNext: () -> Unit = {}, onDone: () -> Unit = {},
    focusRequester: FocusRequester? = null,
) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(16.dp)
    val hintText = stringResource(hint)
    Row(Modifier.fillMaxWidth().heightIn(min = 52.dp).clip(shape).background(Color.White.copy(alpha = .05f))
        .border(1.dp, if (focused) EloraWelcomeLilac else Color.White.copy(alpha = .10f), shape)
        .padding(start = 16.dp, end = if (password) 4.dp else 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Image(painterResource(icon), null, Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        BasicTextField(value = value, onValueChange = onChange, enabled = enabled, singleLine = true,
            textStyle = TextStyle(color = Color.White, fontSize = 14.sp), cursorBrush = SolidColor(EloraWelcomeLilac),
            visualTransformation = if (password && !showPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(imeAction = imeAction,
                keyboardType = if (password) KeyboardType.Password else KeyboardType.Text, autoCorrectEnabled = false),
            keyboardActions = KeyboardActions(onNext = { onNext() }, onDone = { onDone() }),
            modifier = Modifier.weight(1f).then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier).onFocusChanged { focused = it.isFocused }.padding(vertical = 16.dp)
                .semantics { contentDescription = hintText },
            decorationBox = { inner -> Box { if (value.isEmpty()) Text(hintText, color = EloraAccountHint, fontSize = 14.sp); inner() } })
        if (password) {
            val toggleLabel = stringResource(if (showPassword) R.string.login_hide_password else R.string.login_show_password)
            Box(Modifier.size(48.dp).clickable(enabled = enabled, role = Role.Button, onClick = onToggle)
                .semantics { contentDescription = toggleLabel }, contentAlignment = Alignment.Center) {
                Image(painterResource(R.drawable.elora_account_eye), null, Modifier.size(20.dp))
            }
        }
    }
}
