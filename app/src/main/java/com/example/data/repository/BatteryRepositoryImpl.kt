package com.example.data.repository

import com.example.data.local.BatterySessionDao
import com.example.data.local.BatterySessionEntity
import com.example.data.remote.BatteryTipsApi
import com.example.domain.model.BatteryTip
import com.example.domain.model.ChargingSession
import com.example.domain.repository.BatteryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BatteryRepositoryImpl(
    private val dao: BatterySessionDao,
    private val api: BatteryTipsApi
) : BatteryRepository {

    override fun getAllSessions(): Flow<List<ChargingSession>> {
        return dao.getAllSessionsFlow().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun saveSession(session: ChargingSession): Long {
        return dao.insertSession(BatterySessionEntity.fromDomain(session))
    }

    override suspend fun deleteSession(id: Long) {
        dao.deleteSessionById(id)
    }

    override suspend fun clearAllSessions() {
        dao.deleteAllSessions()
    }

    override suspend fun fetchBatteryTips(): List<BatteryTip> {
        return try {
            api.getBatteryTips().map { it.toDomain() }
        } catch (e: Exception) {
            // Failure fallback
            listOf(
                BatteryTip(
                    1, 
                    "Avoid Extreme Temperatures", 
                    "Battery charging is most efficient and least damaging between 15°C and 35°C. Avoid leaving your device in hot cars or direct sunlight.", 
                    "Temperature"
                ),
                BatteryTip(
                    2, 
                    "Optimal 20-80% Charging Range", 
                    "To prolong lithium-ion battery health, keep the charge between 20% and 80%. Repeated 0-100% full cycles increase structural degradation speed.", 
                    "Lifespan"
                ),
                BatteryTip(
                    3, 
                    "Fast Charging Thermal Awareness", 
                    "Fast charging inputs high wattage (generating heat). Keep the device on a hard surface during fast charging so heat can dissipate.", 
                    "Efficiency"
                ),
                BatteryTip(
                    4, 
                    "Understanding Wake Locks", 
                    "Background apps that keep wake locks prevent CPU sleep states. Restricting extreme background processes improves screen-on ratios.", 
                    "Discharge"
                ),
                BatteryTip(
                    5, 
                    "Charger Quality & Voltage Noise", 
                    "Certified power bricks minimize battery stress by supplying clean, ripple-free direct currents. Cheap chargers can introduce voltage oscillation.", 
                    "Safety"
                )
            )
        }
    }
}
