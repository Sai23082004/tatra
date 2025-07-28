package com.mahiya.safegas.data

data class GasLeakData(
    val status: String,
    val gas_level: Float,
    val sensor_count: Int,
    val last_scan: String
)

data class GasLevelData(
    val current_level: Float,
    val estimated_hours: Float,
    val flow_rate: Float,
    val pressure: Float,
    val recent_readings: List<GasReadingItem>
)

data class GasReadingItem(
    val time: String,
    val level: Float,
    val status: String
)

data class PipelineHealthData(
    val overall_health: Float,
    val sections: List<PipelineSectionItem>,
    val maintenance_schedule: List<MaintenanceItem>
)

data class PipelineSectionItem(
    val id: Int,
    val section_name: String,
    val health_percentage: Float,
    val status: String,
    val last_inspection: String?
)

data class MaintenanceItem(
    val id: Int,
    val title: String,
    val description: String,
    val scheduled_date: String,
    val priority: String
)

data class RegulatorData(
    val regulator_id: String?,
    val is_on: Boolean,
    val auto_mode: Boolean,
    val current_pressure: Float,
    val flow_rate: Float,
    val temperature: Float
)

data class EmergencyContactData(
    val id: Int,
    val name: String,
    val phone_number: String,
    val relationship: String,
    val is_primary: Boolean
)

data class ProfileData(
    val username: String,
    val email: String,
    val phone_number: String?,
    val profile_image: String?,
    val device_unique_code: String
)
