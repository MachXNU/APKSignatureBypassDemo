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
        val expectedSignature = "<you can get it by first getting the raw vertificate with sigtool's toCharsString, then putting it in Cyberchef with recipe: From Hex -> To Base64>"
        val expectedSignatureSHA256 = "<shown by sigtool>"

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