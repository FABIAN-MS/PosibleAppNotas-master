package com.example.inventory.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Note::class], version = 2, exportSchema = false) // Aumentamos la versión a 2
abstract class InventoryDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: InventoryDatabase? = null

        fun getDatabase(context: Context): InventoryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    InventoryDatabase::class.java,
                    "note_database"
                )
                    .addMigrations(MIGRATION_1_2) // Añadimos la migración
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Definimos la migración de la versión 1 a la 2
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Agregamos las columnas `fecha` y `hora` con un valor predeterminado
                database.execSQL("ALTER TABLE notes ADD COLUMN fecha INTEGER DEFAULT 0 NOT NULL")
                database.execSQL("ALTER TABLE notes ADD COLUMN hora INTEGER DEFAULT 0 NOT NULL")
            }
        }
    }
}
