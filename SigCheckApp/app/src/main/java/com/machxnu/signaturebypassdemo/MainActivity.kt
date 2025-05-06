package com.machxnu.signaturebypassdemo

import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.machxnu.signaturebypassdemo.ui.theme.SignatureBypassDemoTheme
import java.security.MessageDigest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Set expected signature here
        val expectedSignature = "MIIC5DCCAcwCAQEwDQYJKoZIhvcNAQEFBQAwNzEWMBQGA1UEAwwNQW5kcm9pZCBEZWJ1ZzEQMA4GA1UECgwHQW5kcm9pZDELMAkGA1UEBhMCVVMwIBcNMjQwMzE2MTAzODM5WhgPMjA1NDAzMDkxMDM4MzlaMDcxFjAUBgNVBAMMDUFuZHJvaWQgRGVidWcxEDAOBgNVBAoMB0FuZHJvaWQxCzAJBgNVBAYTAlVTMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAneCEI25z2y9kuBSxl6d59dfqqA4r1E15MaVurlsOJRFw7XQSY6xYXz09gabeMjUnUfEgPX4E13Hr0ka931PZ80cKf69vp++OJH1IeRIpFT2qydwVflUujzH+2l6HOhQSLhGSTf5BZEY6XoyvqGk7/qww4Wyfr4i8u74ACmTBdY7XcXEQdaf/SBbBFlr9JwJuWr8w0A76WcUCLsTPn/vEabKkswt+84Wapkd73Nvgou63n1Z5i04iJI3qUIXaaSjlgHKh3B7ot1w0Xl/7vN5izINT3Yr5dJDHp4TUYbUzJq3poBC6v+983Cwr+/WrPuygu/AVir43NTXGWeaxeF7ObQIDAQABMA0GCSqGSIb3DQEBBQUAA4IBAQAKh38nXRSA8GHVU5DjUJVYIyYgWxKigKxm4hpvcCfYS5kpNsDhjaLPVudgNgS5W1tkoxE3PnelpwkncT+HEtV35utbKLa8xZJnhPHjZ4rxxhXq/1JnacoUJLJKnxM/PVdfgaCXD4JIbHLNhYmxNYcwv/l3B86pn4UFQgenWa45lRJxh/pYW+Sbduf4++7B0L3UVe1ChvNUtcGtqbf7qADQFmoEufgIY2LgJl/mAU21WOl2PqjXmpMDtnHzOqUap5hMjMigDdiC8boBHhCHyXfBy6WQWLYUPG7WMgvyoZJfmrsvVpUOjbfaXVlCKsQqVrdoJtVgyQv2Uip4zaVMG5Vf"
        val expectedSignatureSHA256 = "7f4cf33b4d2953d996f9dfd400ed8f8f89046cc6e023d0f33e86daa4b91e56d3"

        // State to hold signature check result
        var signatureValid by mutableStateOf(false)

        // Check signature on app start
        signatureValid = verifyAppSignature(packageName, expectedSignature, expectedSignatureSHA256)

        setContent {
            SignatureBypassDemoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        Greeting("Android")
                        // Display signature validation result
                        Text(
                            text = if (signatureValid) "Signature is VALID!" else "Signature is INVALID!",
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }
        }
    }

    // Method to verify the signature
    private fun verifyAppSignature(packageName: String, expectedBase64Cert: String, expectedSHA256Cert: String): Boolean {
        try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            } else {
                packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            }

            val rawSignatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                packageInfo.signatures
            }

            if (rawSignatures == null) return false
            val signatures = arrayOfNulls<Signature>(rawSignatures.size)
            for (i in rawSignatures.indices) {
                signatures[i] = rawSignatures[i]
            }

            for (signature in signatures) {
                var certBytes = signature?.toByteArray() ?: continue
                val base64Cert = Base64.encodeToString(certBytes, Base64.NO_WRAP)
                Log.d("AppSignature", "App certificate Base64: $base64Cert")

                val sha256 = MessageDigest.getInstance("SHA-256").digest(certBytes)
                val sha256Hex = sha256.joinToString("") { "%02X".format(it) }
                Log.d("AppSignature", "SHA-256 fingerprint: $sha256Hex")

                if (base64Cert == expectedBase64Cert) {
                    Log.d("AppSignature", "Signature is valid! (Base64 cert match)")
                    //return true
                }
                if (sha256Hex.lowercase() == expectedSHA256Cert) {
                    Log.d("AppSignature", "Signature is valid! (SHA256 cert match)")
                    return true
                }
            }
        } catch (e: Exception) {
            Log.e("AppSignature", "Error verifying app signature", e)
        }

        Log.d("AppSignature", "Signature is invalid!")
        return false
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SignatureBypassDemoTheme {
        Greeting("Android")
    }
}