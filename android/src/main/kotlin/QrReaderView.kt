package me.hetian.flutter_qr_reader.views

import android.app.ActionBar
import android.app.Activity
import android.app.Application
import android.content.Context
import android.hardware.Camera
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.Intents
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.BarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import com.journeyapps.barcodescanner.camera.CameraParametersCallback
import com.journeyapps.barcodescanner.camera.CenterCropStrategy
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.platform.PlatformView
import java.util.*

@RequiresApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
class QrReaderView(private val context: Context, private val registrar: PluginRegistry.Registrar, id: Int, params:Map<String, Object>) :
        PlatformView, MethodChannel.MethodCallHandler {
    var barcodeView: BarcodeView? = null
    private var isTorchOn: Boolean = false
    private val width:Int
    private val height:Int
    val channel: MethodChannel

    init {
        channel = MethodChannel(registrar.messenger(), "me.hetian.flutter_qr_reader.reader_view_$id")
        channel.setMethodCallHandler(this)
        width = params.get("width") as Int
        height = params.get("height") as Int
        registrar.activity().application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityPaused(p0: Activity?) {
                if (p0 == registrar.activity()) {
                    barcodeView?.pause()
                }
            }

            override fun onActivityResumed(p0: Activity?) {
                if (p0 == registrar.activity()) {
                    barcodeView?.resume()
                }
            }

            override fun onActivityStarted(p0: Activity?) {
            }

            override fun onActivityDestroyed(p0: Activity?) {
            }

            override fun onActivitySaveInstanceState(p0: Activity?, p1: Bundle?) {
            }

            override fun onActivityStopped(p0: Activity?) {
            }

            override fun onActivityCreated(p0: Activity?, p1: Bundle?) {
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    override fun getView(): View {
        return initBarCodeView()?.apply {
            resume()
        }!!
    }

    @RequiresApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private fun initBarCodeView(): BarcodeView? {
        if (barcodeView == null) {
            barcodeView = createBarCodeView()
            val layoutParams = ActionBar.LayoutParams(width, height)
            barcodeView?.setLayoutParams(layoutParams)
        }

        return barcodeView
    }

    private fun createBarCodeView(): BarcodeView? {
        val barcode = BarcodeView(registrar.activity())
        barcode.decoderFactory = DefaultDecoderFactory (listOf(BarcodeFormat.QR_CODE), null, null, Intents.Scan.MIXED_SCAN)
        barcode.previewScalingStrategy = CenterCropStrategy()
        val settings = barcode.cameraSettings
        settings.isBarcodeSceneModeEnabled = true
        settings.isAutoFocusEnabled = true
        //settings.isAutoTorchEnabled = true
        settings.isContinuousFocusEnabled = true
        settings.isExposureEnabled = true
        settings.isMeteringEnabled = true
        barcode.cameraSettings = settings
        barcode.changeCameraParameters(CustomCameraParametersCallbackCallback());
        barcode.decodeContinuous(
                object : BarcodeCallback {
                    override fun barcodeResult(result: BarcodeResult) {
                        val rest = HashMap<String, Any>()
                        rest["text"] = result.text
                        val poi = ArrayList<String>()
                        for (point in result.resultPoints) {
                            poi.add("${point.x},${point.y}")
                        }
                        rest["points"] = poi
                        channel.invokeMethod("onQRCodeRead", rest)
                    }

                    override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
                }
        )
        return barcode
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when(call?.method){
            "flashlight" -> {
                isTorchOn = !isTorchOn
                barcodeView?.setTorch(isTorchOn)
                result.success(isTorchOn)
            }
            "stopCamera" -> {
                if (barcodeView!!.isPreviewActive) {
                    barcodeView?.pause()
                }
            }
            "startCamera" -> {
                if (!barcodeView!!.isPreviewActive) {
                    barcodeView?.resume()
                }
            }
        }
    }

    override fun dispose() {
        barcodeView?.pause()
        barcodeView = null
    }
}

private class CustomCameraParametersCallbackCallback: CameraParametersCallback {
    override fun changeCameraParameters(parameters: Camera.Parameters?): Camera.Parameters {
        val ret = parameters!!
        ret.removeGpsData()
        if(ret.isZoomSupported){
            ret.zoom = minOf(2, ret.maxZoom)
        }

        if (ret.isAutoWhiteBalanceLockSupported) {
            ret.autoWhiteBalanceLock = false
        }

        if (ret.isAutoExposureLockSupported) {
            ret.autoExposureLock = false
        }

        if (ret.supportedSceneModes.contains(Camera.Parameters.SCENE_MODE_BARCODE)) {
            ret.sceneMode = Camera.Parameters.SCENE_MODE_BARCODE
        }

        return ret
    }
}
