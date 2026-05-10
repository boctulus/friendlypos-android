package cl.friendlypos.mypos.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import cl.friendlypos.mypos.db.dao.PendingCashboxOperationsDao
import cl.friendlypos.mypos.db.entity.PendingCashboxOperation

@Database(entities = [PendingCashboxOperation::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun pendingCashboxOperationsDao(): PendingCashboxOperationsDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "friendlypos_db"
                ).build().also { instance = it }
            }
    }
}
