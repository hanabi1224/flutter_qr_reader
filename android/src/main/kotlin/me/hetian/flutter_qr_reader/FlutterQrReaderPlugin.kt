package me.hetian.flutter_qr_reader

import android.annotation.SuppressLint
import android.app.Activity
import android.os.AsyncTask
import android.os.Build
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.PluginRegistry.Registrar
import io.flutter.plugin.platform.PlatformViewRegistry
import me.hetian.flutter_qr_reader.factorys.QrReaderFactory
import java.io.File

/** FlutterQrReaderPlugin  */
@RequiresApi(Build.VERSION_CODES.CUPCAKE)
class FlutterQrReaderPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        if (call.method == "imgQrCode") {
            imgQrCode(call, result)
        } else {
            result.notImplemented()
        }
    }

    @SuppressLint("StaticFieldLeak")
    fun imgQrCode(call: MethodCall, result: MethodChannel.Result) {
        val filePath = call.argument<String>("file")
        if (filePath == null) {
            result.error("Not found data", null, null)
            return
        }
        val file = File(filePath)
        if (!file.exists()) {
            result.error("File not found", null, null)
        }
        val task = object: AsyncTask<String?, Int?, String?>() {
            override fun doInBackground(vararg params: String?): String? { // 解析二维码/条码
                return QRCodeDecoder.syncDecodeQRCode(filePath)
            }

            override fun onPostExecute(s: String?) {
                super.onPostExecute(s)
                if (null == s) {
                    result.error("not data", null, null)
                } else {
                    result.success(s)
                }
            }
        }
        task.execute(filePath)
    }

    companion object {
        private const val CHANNEL_NAME = "me.hetian.flutter_qr_reader"
        private const val CHANNEL_VIEW_NAME = "me.hetian.flutter_qr_reader.reader_view"
        private var activity: Activity? = null

        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val channel = MethodChannel(registrar.messenger(), CHANNEL_NAME)
            registerViewFactory(registrar.platformViewRegistry(), registrar.activity(), registrar.messenger())
            channel.setMethodCallHandler(FlutterQrReaderPlugin())
        }

        @JvmStatic
        fun registerViewFactory(platformViewRegistry: PlatformViewRegistry, activity: Activity, binaryMessenger:BinaryMessenger){
            val qrReaderFactory = QrReaderFactory(activity, binaryMessenger)
            platformViewRegistry.registerViewFactory(CHANNEL_VIEW_NAME, qrReaderFactory)
        }
    }

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        val channel = MethodChannel(flutterPluginBinding.binaryMessenger, CHANNEL_NAME)
        registerViewFactory(flutterPluginBinding.platformViewRegistry, activity!!, flutterPluginBinding.binaryMessenger)
        channel.setMethodCallHandler(FlutterQrReaderPlugin())
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivity() {
    }

    override fun onDetachedFromActivityForConfigChanges() {
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    }
}
