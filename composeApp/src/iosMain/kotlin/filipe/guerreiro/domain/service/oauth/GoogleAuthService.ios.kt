package filipe.guerreiro.domain.service.oauth

import filipe.guerreiro.domain.model.oauth.AuthResult
import filipe.guerreiro.domain.model.oauth.GoogleUser
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import platform.AuthenticationServices.ASPresentationAnchor
import platform.AuthenticationServices.ASWebAuthenticationPresentationContextProvidingProtocol
import platform.AuthenticationServices.ASWebAuthenticationSession
import platform.Foundation.*
import platform.UIKit.UIApplication
import platform.darwin.NSObject
import platform.posix.arc4random_buf
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalForeignApi::class, ExperimentalEncodingApi::class)
actual class GoogleAuthService actual constructor() {

    private val _authState = MutableStateFlow<AuthResult?>(null)
    actual val authState: StateFlow<AuthResult?> = _authState.asStateFlow()

    private var cachedAccessToken: String? = null
    private var codeVerifier: String? = null

    companion object {
        private val IOS_CLIENT_ID: String
            get() = NSBundle.mainBundle.objectForInfoDictionaryKey("GIDClientID") as? String ?: ""
            
        private val CALLBACK_SCHEME: String
            get() = IOS_CLIENT_ID.split(".").reversed().joinToString(".")

        private val REDIRECT_URI: String
            get() = "$CALLBACK_SCHEME:/oauth2callback"

        private const val DRIVE_SCOPE = "https://www.googleapis.com/auth/drive.appdata"
        private const val USERINFO_SCOPE = "https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile"
    }

    private val contextProvider = object : NSObject(), ASWebAuthenticationPresentationContextProvidingProtocol {
        override fun presentationAnchorForWebAuthenticationSession(session: ASWebAuthenticationSession): ASPresentationAnchor {
            val scenes = UIApplication.sharedApplication.connectedScenes
            val windowScene = scenes.firstOrNull() as? platform.UIKit.UIWindowScene
            return windowScene?.keyWindow ?: ASPresentationAnchor()
        }
    }

    /**
     * Gera um code_verifier (PKCE) de 32 bytes aleatórios, codificado em Base64-URL.
     */
    private fun generateCodeVerifier(): String {
        val bytes = ByteArray(32)
        bytes.usePinned { pinned ->
            arc4random_buf(pinned.addressOf(0), bytes.size.toULong())
        }
        return Base64.UrlSafe.encode(bytes).trimEnd('=')
    }

    /**
     * Gera o code_challenge a partir do code_verifier usando SHA-256 (implementação pura Kotlin).
     */
    private fun generateCodeChallenge(verifier: String): String {
        val hash = sha256(verifier.encodeToByteArray())
        return Base64.UrlSafe.encode(hash).trimEnd('=')
    }

    actual suspend fun signIn(): AuthResult = suspendCoroutine { continuation ->
        _authState.value = AuthResult.Loading

        val verifier = generateCodeVerifier()
        codeVerifier = verifier
        val challenge = generateCodeChallenge(verifier)

        val scopes = "$DRIVE_SCOPE $USERINFO_SCOPE"
        val authUrlString = "https://accounts.google.com/o/oauth2/v2/auth" +
                "?client_id=$IOS_CLIENT_ID" +
                "&redirect_uri=$REDIRECT_URI" +
                "&response_type=code" +
                "&scope=$scopes" +
                "&code_challenge=$challenge" +
                "&code_challenge_method=S256"

        val authUrl = NSURL(string = authUrlString)
        val scheme = CALLBACK_SCHEME

        val session = ASWebAuthenticationSession(
            uRL = authUrl,
            callbackURLScheme = scheme
        ) { callbackUrl: NSURL?, error: NSError? ->
            if (error != null) {
                val authError = AuthResult.Error(error.localizedDescription)
                _authState.value = authError
                continuation.resume(authError)
                return@ASWebAuthenticationSession
            }

            if (callbackUrl != null) {
                val components = NSURLComponents(uRL = callbackUrl, resolvingAgainstBaseURL = false)
                val code = components.queryItems?.filterIsInstance<NSURLQueryItem>()?.find { it.name == "code" }?.value

                if (code != null) {
                    exchangeCodeForToken(code) { result ->
                        _authState.value = result
                        continuation.resume(result)
                    }
                } else {
                    val authError = AuthResult.Error("Authorization code not found in callback URL")
                    _authState.value = authError
                    continuation.resume(authError)
                }
            } else {
                val authError = AuthResult.Cancelled
                _authState.value = authError
                continuation.resume(authError)
            }
        }

        session.presentationContextProvider = contextProvider
        session.prefersEphemeralWebBrowserSession = false
        session.start()
    }

    private fun exchangeCodeForToken(code: String, onResult: (AuthResult) -> Unit) {
        val tokenUrl = NSURL(string = "https://oauth2.googleapis.com/token")

        val request = NSMutableURLRequest(uRL = tokenUrl!!).apply {
            setHTTPMethod("POST")
            setValue("application/x-www-form-urlencoded", forHTTPHeaderField = "Content-Type")

        val bodyString = "client_id=$IOS_CLIENT_ID" +
                "&code=$code" +
                "&grant_type=authorization_code" +
                "&redirect_uri=$REDIRECT_URI" +
                "&code_verifier=${codeVerifier ?: ""}"
        val byteArray = bodyString.encodeToByteArray()
        val bodyData = byteArray.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = byteArray.size.toULong())
        }
            setHTTPBody(bodyData)
        }

        val task = NSURLSession.sharedSession.dataTaskWithRequest(request) { data, _, error ->
            if (error != null || data == null) {
                onResult(AuthResult.Error("Falha ao trocar código por token: ${error?.localizedDescription}"))
                return@dataTaskWithRequest
            }

            val responseString = NSString.create(data = data, encoding = NSUTF8StringEncoding).toString()
            try {
                val json = Json { ignoreUnknownKeys = true }
                val jsonObject = json.parseToJsonElement(responseString).jsonObject

                if (jsonObject.containsKey("access_token")) {
                    val accessToken = jsonObject["access_token"]?.jsonPrimitive?.content ?: ""
                    cachedAccessToken = accessToken

                    // Buscar perfil real do usuário com o token obtido
                    fetchUserInfo(accessToken) { user ->
                        onResult(AuthResult.Success(user, accessToken))
                    }
                } else {
                    onResult(AuthResult.Error("Token de acesso não encontrado na resposta: $responseString"))
                }
            } catch (e: Exception) {
                onResult(AuthResult.Error("Falha ao parsear resposta do token", e))
            }
        }
        task.resume()
    }

    /**
     * Busca informações reais do perfil do usuário via Google Userinfo API.
     */
    private fun fetchUserInfo(accessToken: String, onResult: (GoogleUser) -> Unit) {
        val userinfoUrl = NSURL(string = "https://www.googleapis.com/oauth2/v2/userinfo")

        val request = NSMutableURLRequest(uRL = userinfoUrl!!).apply {
            setHTTPMethod("GET")
            setValue("Bearer $accessToken", forHTTPHeaderField = "Authorization")
        }

        val task = NSURLSession.sharedSession.dataTaskWithRequest(request) { data, _, error ->
            if (error != null || data == null) {
                // Fallback: retornar usuário parcial se o perfil falhar
                onResult(GoogleUser(id = "unknown", email = "unknown", displayName = null, photoUrl = null))
                return@dataTaskWithRequest
            }

            val responseString = NSString.create(data = data, encoding = NSUTF8StringEncoding).toString()
            try {
                val json = Json { ignoreUnknownKeys = true }
                val jsonObject = json.parseToJsonElement(responseString).jsonObject

                val user = GoogleUser(
                    id = jsonObject["id"]?.jsonPrimitive?.content ?: "unknown",
                    email = jsonObject["email"]?.jsonPrimitive?.content ?: "unknown",
                    displayName = jsonObject["name"]?.jsonPrimitive?.content,
                    photoUrl = jsonObject["picture"]?.jsonPrimitive?.content
                )
                onResult(user)
            } catch (e: Exception) {
                onResult(GoogleUser(id = "unknown", email = "unknown", displayName = null, photoUrl = null))
            }
        }
        task.resume()
    }

    actual suspend fun signOut() {
        cachedAccessToken = null
        codeVerifier = null
        _authState.value = AuthResult.SignedOut
    }

    actual suspend fun signInSilently(): AuthResult {
        // iOS não persiste tokens entre sessões; o usuário precisa autenticar novamente.
        return AuthResult.Cancelled
    }

    actual fun getAccessToken(): String? {
        return cachedAccessToken
    }
}

/**
 * Implementação pura Kotlin de SHA-256 para PKCE code_challenge.
 * Evita dependência de CommonCrypto cinterop.
 */
private fun sha256(input: ByteArray): ByteArray {
    val k = intArrayOf(
        0x428a2f98.toInt(), 0x71374491, 0xb5c0fbcf.toInt(), 0xe9b5dba5.toInt(),
        0x3956c25b, 0x59f111f1, 0x923f82a4.toInt(), 0xab1c5ed5.toInt(),
        0xd807aa98.toInt(), 0x12835b01, 0x243185be, 0x550c7dc3,
        0x72be5d74, 0x80deb1fe.toInt(), 0x9bdc06a7.toInt(), 0xc19bf174.toInt(),
        0xe49b69c1.toInt(), 0xefbe4786.toInt(), 0x0fc19dc6, 0x240ca1cc,
        0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
        0x983e5152.toInt(), 0xa831c66d.toInt(), 0xb00327c8.toInt(), 0xbf597fc7.toInt(),
        0xc6e00bf3.toInt(), 0xd5a79147.toInt(), 0x06ca6351, 0x14292967,
        0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13,
        0x650a7354, 0x766a0abb, 0x81c2c92e.toInt(), 0x92722c85.toInt(),
        0xa2bfe8a1.toInt(), 0xa81a664b.toInt(), 0xc24b8b70.toInt(), 0xc76c51a3.toInt(),
        0xd192e819.toInt(), 0xd6990624.toInt(), 0xf40e3585.toInt(), 0x106aa070,
        0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5,
        0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
        0x748f82ee, 0x78a5636f, 0x84c87814.toInt(), 0x8cc70208.toInt(),
        0x90befffa.toInt(), 0xa4506ceb.toInt(), 0xbef9a3f7.toInt(), 0xc67178f2.toInt()
    )

    fun Int.rightRotate(n: Int): Int = (this ushr n) or (this shl (32 - n))

    // Pre-processing: padding
    val originalBitLen = input.size.toLong() * 8
    val padded = buildList {
        addAll(input.toList())
        add(0x80.toByte())
        while ((size % 64) != 56) add(0x00.toByte())
        for (i in 56 downTo 0 step 8) add((originalBitLen ushr i).toByte())
    }.toByteArray()

    var h0 = 0x6a09e667
    var h1 = 0xbb67ae85.toInt()
    var h2 = 0x3c6ef372
    var h3 = 0xa54ff53a.toInt()
    var h4 = 0x510e527f
    var h5 = 0x9b05688c.toInt()
    var h6 = 0x1f83d9ab
    var h7 = 0x5be0cd19

    for (chunkStart in padded.indices step 64) {
        val w = IntArray(64)
        for (i in 0 until 16) {
            w[i] = ((padded[chunkStart + i * 4].toInt() and 0xFF) shl 24) or
                    ((padded[chunkStart + i * 4 + 1].toInt() and 0xFF) shl 16) or
                    ((padded[chunkStart + i * 4 + 2].toInt() and 0xFF) shl 8) or
                    (padded[chunkStart + i * 4 + 3].toInt() and 0xFF)
        }
        for (i in 16 until 64) {
            val s0 = w[i - 15].rightRotate(7) xor w[i - 15].rightRotate(18) xor (w[i - 15] ushr 3)
            val s1 = w[i - 2].rightRotate(17) xor w[i - 2].rightRotate(19) xor (w[i - 2] ushr 10)
            w[i] = w[i - 16] + s0 + w[i - 7] + s1
        }

        var a = h0; var b = h1; var c = h2; var d = h3
        var e = h4; var f = h5; var g = h6; var h = h7

        for (i in 0 until 64) {
            val s1 = e.rightRotate(6) xor e.rightRotate(11) xor e.rightRotate(25)
            val ch = (e and f) xor (e.inv() and g)
            val temp1 = h + s1 + ch + k[i] + w[i]
            val s0 = a.rightRotate(2) xor a.rightRotate(13) xor a.rightRotate(22)
            val maj = (a and b) xor (a and c) xor (b and c)
            val temp2 = s0 + maj
            h = g; g = f; f = e; e = d + temp1
            d = c; c = b; b = a; a = temp1 + temp2
        }
        h0 += a; h1 += b; h2 += c; h3 += d
        h4 += e; h5 += f; h6 += g; h7 += h
    }

    val result = ByteArray(32)
    for ((idx, value) in intArrayOf(h0, h1, h2, h3, h4, h5, h6, h7).withIndex()) {
        result[idx * 4] = (value ushr 24).toByte()
        result[idx * 4 + 1] = (value ushr 16).toByte()
        result[idx * 4 + 2] = (value ushr 8).toByte()
        result[idx * 4 + 3] = value.toByte()
    }
    return result
}
