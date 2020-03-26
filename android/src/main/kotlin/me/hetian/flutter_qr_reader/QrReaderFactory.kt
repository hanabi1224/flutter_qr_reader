package me.hetian.flutter_qr_reader.factorys

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.PluginRegistry.Registrar
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory
import me.hetian.flutter_qr_reader.views.QrReaderView

class QrReaderFactory(private val activity: Activity, private val binaryMessenger: BinaryMessenger) : PlatformViewFactory(StandardMessageCodec.INSTANCE) {
    @SuppressLint("NewApi")
    override fun create(context: Context, id: Int, args: Any): PlatformView {
        val params = args as Map<String, Any>
        return QrReaderView(activity, binaryMessenger, id, params)
    }
}
