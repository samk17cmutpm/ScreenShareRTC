package fr.pchab.androidrtc

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.util.CoilUtils
import fr.pchab.androidrtc.font_support.CustomViewWithTypefaceSupport
import fr.pchab.androidrtc.font_support.TextField
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class Application : MultiDexApplication(), ImageLoaderFactory {
	private val application by lazy { this@Application }

	companion object {
		@JvmStatic
		lateinit var appContext: Context
			private set
	}

	override fun onCreate() {
		super.onCreate()

		appContext = applicationContext

		MultiDex.install(application)

		startKoin {
			androidContext(application)
			androidLogger(Level.DEBUG)
			modules(fr.pchab.androidrtc.di.modules)
		}

		ViewPump.init(
			ViewPump.builder()
				.addInterceptor(
					CalligraphyInterceptor(
						CalligraphyConfig.Builder()
							.setDefaultFontPath(getString(R.string.fonts_be_regular))
							.setFontAttrId(R.attr.fontPath)
							.setFontMapper { font -> font }
							.addCustomViewWithSetTypeface(CustomViewWithTypefaceSupport::class.java)
							.addCustomStyle(TextField::class.java, R.attr.textFieldStyle)
							.build())
				)
				.build())

		AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
	}

	override fun newImageLoader(): ImageLoader {
		return ImageLoader.Builder(applicationContext)
			.crossfade(true)
			.okHttpClient {
				OkHttpClient.Builder()
					.cache(CoilUtils.createDefaultCache(applicationContext))
					.build()
			}
			.build()
	}
}