package com.example.arigo.presentation.filter_health

data class FilterHealthState(
    val deviceId: String = "",
    val deviceName: String = "Filter Pro",
    val isLoading: Boolean = false,
    val error: String? = null,
    val efficiencyPercent: Int = 97,
    val usageHours: Int = 0,
    val maxHours: Int = 2000,
    val lastReplacedDate: String = "",
    val filterDescription: String = "AriGo AirGuardPro's 3-stage filtration—General, HEPA, and Activated Carbon—removes dust, PM2.5, allergens, and odors. The HEPA filter traps fine pollutants, while the Activated Carbon filter neutralizes harmful gases. Regular cleaning boosts efficiency, and filter replacement is quick and easy."
)
