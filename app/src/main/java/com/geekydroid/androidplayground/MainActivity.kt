package com.geekydroid.androidplayground


import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {

    private lateinit var executor: Executor
    private lateinit var bioMetricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var btn_authenticate: Button
    private lateinit var btn_setAuthentication: Button
    private var activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d("TAG", ": ${result.resultCode} result ok "+Activity.RESULT_OK)
            if (result.resultCode == Activity.RESULT_OK) {
                bioMetricPrompt.authenticate(promptInfo)
            }
        }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        executor = ContextCompat.getMainExecutor(this)
        bioMetricPrompt = BiometricPrompt(this, executor, object :
            BiometricPrompt.AuthenticationCallback() {

            /**
             * Called when an unrecoverable error has been encountered and authentication has stopped.
             *
             *
             * After this method is called, no further events will be sent for the current
             * authentication session.
             *
             * @param errorCode An integer ID associated with the error.
             * @param errString A human-readable string that describes the error.
             */
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(applicationContext, "Error", Toast.LENGTH_SHORT).show()
                btn_setAuthentication.visibility = View.VISIBLE

            }

            /**
             * Called when a biometric (e.g. fingerprint, face, etc.) is recognized, indicating that the
             * user has successfully authenticated.
             *
             *
             * After this method is called, no further events will be sent for the current
             * authentication session.
             *
             * @param result An object containing authentication-related data.
             */
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Toast.makeText(applicationContext, "Sucess", Toast.LENGTH_SHORT).show()
            }

            /**
             * Called when a biometric (e.g. fingerprint, face, etc.) is presented but not recognized as
             * belonging to the user.
             */
            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(applicationContext, "Failed", Toast.LENGTH_SHORT).show()
            }
        })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Test biometric Prompt")
            .setSubtitle("Authenticate")
            .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
            .build()

        btn_authenticate = findViewById(R.id.authenticate)
        btn_setAuthentication = findViewById(R.id.setAuthentication)
        btn_authenticate.setOnClickListener {
            bioMetricPrompt.authenticate(promptInfo)
        }

        btn_setAuthentication.setOnClickListener {
            checkForAuthentication()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun checkForAuthentication() {
        val bioMetricManager = BiometricManager.from(this)
        when (bioMetricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS ->
                Log.d("TAG", "checkForAuthentication: biometric success")
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                Log.d("TAG", "checkForAuthentication: Error no hardware")
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                Log.d("TAG", "checkForAuthentication: Error biometric hardware unavailable")
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                    putExtra(
                        Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                        BIOMETRIC_STRONG or DEVICE_CREDENTIAL
                    )
                }
                activityResultLauncher.launch(enrollIntent)
            }
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {

            }
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {

            }
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {

            }
        }
    }
}