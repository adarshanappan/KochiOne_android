package com.kochione.kochi_one

import android.app.Activity
import android.content.Context
import com.google.android.gms.common.moduleinstall.ModuleInstall
import com.google.android.gms.common.moduleinstall.ModuleInstallRequest
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

object QrScanner {
    fun installGoogleScanner(
        context: Context,
        onInstalled: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        val moduleInstall = ModuleInstall.getClient(context)
        val request = ModuleInstallRequest.newBuilder()
            .addApi(GmsBarcodeScanning.getClient(context))
            .build()

        moduleInstall.installModules(request)
            .addOnSuccessListener { onInstalled() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun startScanning(
        context: Context,
        onResult: (String) -> Unit,
        onCancelled: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        val activity = context as? Activity
            ?: run {
                onFailure(IllegalStateException("QrScanner requires an Activity context"))
                return
            }

        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .enableAutoZoom()
            .build()

        val scanner = GmsBarcodeScanning.getClient(activity, options)

        scanner.startScan()
            .addOnSuccessListener { barcode ->
                val value = barcode.rawValue
                if (value != null) onResult(value) else onFailure(IllegalStateException("Empty QR result"))
            }
            .addOnCanceledListener { onCancelled() }
            .addOnFailureListener { e -> onFailure(e) }
    }
}

