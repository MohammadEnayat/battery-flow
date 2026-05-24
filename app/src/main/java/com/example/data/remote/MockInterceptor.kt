package com.example.data.remote

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class MockInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath

        if (path.contains("battery-insights-tips")) {
            val json = """
                [
                    {
                        "id": 1,
                        "title": "Avoid Extreme Temperatures",
                        "content": "Battery charging is most efficient and least damaging between 15°C and 35°C. Avoid leaving your device in hot cars or direct sunlight.",
                        "category": "Temperature"
                    },
                    {
                        "id": 2,
                        "title": "Optimal 20-80% Charging Range",
                        "content": "To prolong lithium-ion battery health, keep the charge between 20% and 80%. Repeated 0-100% full cycles increase structural degradation speed.",
                        "category": "Lifespan"
                    },
                    {
                        "id": 3,
                        "title": "Fast Charging Thermal Awareness",
                        "content": "Fast charging inputs high wattage (generating heat). Keep the device on a hard surface during fast charging so heat can dissipate.",
                        "category": "Efficiency"
                    },
                    {
                        "id": 4,
                        "title": "Understanding Wake Locks",
                        "content": "Background apps that keep wake locks prevent CPU sleep states. Restricting extreme background processes improves screen-on ratios.",
                        "category": "Discharge"
                    },
                    {
                        "id": 5,
                        "title": "Charger Quality & Voltage Noise",
                        "content": "Certified power bricks minimize battery stress by supplying clean, ripple-free direct currents. Cheap chargers can introduce voltage oscillation.",
                        "category": "Safety"
                    }
                ]
            """.trimIndent()

            return Response.Builder()
                .code(200)
                .message("OK")
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .body(json.toResponseBody("application/json".toMediaTypeOrNull()))
                .addHeader("content-type", "application/json")
                .build()
        }

        return chain.proceed(request)
    }
}
