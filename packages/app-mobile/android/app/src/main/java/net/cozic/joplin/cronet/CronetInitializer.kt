package net.cozic.joplin.cronet

import android.content.Context
import android.util.Log
import com.google.android.gms.net.CronetProviderInstaller
import com.google.net.cronet.okhttptransport.CronetInterceptor
import okhttp3.OkHttpClient
import org.chromium.net.CronetEngine

// null until Cronet is ready, or if unavailable
@Volatile
private var cronetInterceptor: CronetInterceptor? = null

object CronetInitializer {

	fun install(context: Context) {
		CronetProviderInstaller.installProvider(context).addOnCompleteListener { task ->
			if (task.isSuccessful) {
				try {
					val engine = CronetEngine.Builder(context)
						.enableBrotli(true)
						.build()
					cronetInterceptor = CronetInterceptor.newBuilder(engine).build()
					Log.i("JOPLIN", "Cronet initialised")
				} catch (e: Exception) {
					Log.w("JOPLIN", "Cronet init failed", e)
				}
			} else {
				Log.w("JOPLIN", "Cronet provider unavailable", task.exception)
			}
		}
	}

	// no-op if Cronet unavailable
	fun applyToBuilder(builder: OkHttpClient.Builder) {
		cronetInterceptor?.let { builder.addInterceptor(it) }
	}
}
