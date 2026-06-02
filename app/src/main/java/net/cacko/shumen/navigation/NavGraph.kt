package net.cacko.shumen.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Screen : NavKey {
    @Serializable
    data object Monitor : Screen
    
    @Serializable
    data object Settings : Screen
}
