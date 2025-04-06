package `is`.hi.hbv601_team_12.data.entities

/**
 * Represents notifications retrieved from a remote server.
 *
 * Notification is intentionally not registered as a Room Entity because it is not stored locally in the database.
 */

data class Notification (
    val id: Int = 0,
    val itemType: String,
    val itemID: Int,
    val message: String
)