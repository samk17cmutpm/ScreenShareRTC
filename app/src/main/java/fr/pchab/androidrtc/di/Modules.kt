package fr.pchab.androidrtc.di

import fr.pchab.androidrtc.Application
import fr.pchab.androidrtc.BuildConfig
import fr.pchab.androidrtc.base.UIThread
import fr.pchab.androidrtc.data.apis.APIService
import fr.pchab.androidrtc.data.executor.PostExecutionThread
import fr.pchab.androidrtc.data.network.httpClient
import fr.pchab.androidrtc.data.network.retrofitClient
import fr.pchab.androidrtc.data.repository.Repository
import org.koin.core.module.Module
import org.koin.dsl.module
import retrofit2.Retrofit

const val BASE_URL = BuildConfig.BASE_URL

val uiThreadModule: Module = module {
	single { UIThread() as PostExecutionThread }
}

val networkModule: Module = module {
	single { httpClient(BuildConfig.DEBUG, Application.appContext) }
	single { retrofitClient(BASE_URL, get()) }

	single {
		val retrofit: Retrofit = get()
		retrofit.create(APIService::class.java)
	}
}

val dataModule: Module = module {
	single { Repository(apiService = get()) }
}

val modules = listOf(
	networkModule,
	uiThreadModule,
	dataModule,
)