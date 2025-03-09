package `is`.hi.hbv601_team_12.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters  
import `is`.hi.hbv601_team_12.data.converters.DateTimeConverters  
import `is`.hi.hbv601_team_12.data.daos.CommentDao
import `is`.hi.hbv601_team_12.data.daos.EventDao
import `is`.hi.hbv601_team_12.data.daos.GroupDao
import `is`.hi.hbv601_team_12.data.daos.InvitationDao
import `is`.hi.hbv601_team_12.data.daos.LogEntryDao
import `is`.hi.hbv601_team_12.data.daos.TagDao
import `is`.hi.hbv601_team_12.data.daos.UserDao
import `is`.hi.hbv601_team_12.data.entities.Comment
import `is`.hi.hbv601_team_12.data.entities.Event
import `is`.hi.hbv601_team_12.data.entities.Group
import `is`.hi.hbv601_team_12.data.entities.Invitation
import `is`.hi.hbv601_team_12.data.entities.LogEntry
import `is`.hi.hbv601_team_12.data.entities.Tag
import `is`.hi.hbv601_team_12.data.entities.User
import `is`.hi.hbv601_team_12.data.entities.EventParticipant

@Database(entities = [Comment::class, Event::class, Group::class, Invitation::class, LogEntry::class, Tag::class, User::class, EventParticipant::class], version =10, exportSchema = false)
@TypeConverters(DateTimeConverters::class)  
abstract class AppDatabase : RoomDatabase() {
    abstract fun commentDao(): CommentDao
    abstract fun eventDao(): EventDao
    abstract fun groupDao(): GroupDao
    abstract fun invitationDao(): InvitationDao
    abstract fun logEntryDao(): LogEntryDao
    abstract fun tagDao(): TagDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return Instance?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "app_database")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
