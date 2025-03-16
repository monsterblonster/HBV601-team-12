package `is`.hi.hbv601_team_12.data.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import `is`.hi.hbv601_team_12.data.converters.LocalDateTimeDeserializer
import `is`.hi.hbv601_team_12.data.converters.LocalDateTimeSerializer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDateTime

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8080/"

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeSerializer())
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeDeserializer())
        .create()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val userApiService: UserApiService by lazy {
        retrofit.create(UserApiService::class.java)
    }

    val groupsApiService: GroupsApiService by lazy {
        retrofit.create(GroupsApiService::class.java)
    }

    val eventsApiService: EventsApiService by lazy {
        retrofit.create(EventsApiService::class.java)
    }
}
