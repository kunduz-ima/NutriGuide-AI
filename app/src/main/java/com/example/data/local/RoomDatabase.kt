package com.example.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import com.example.data.model.FoodLogEntry
import com.example.data.model.UserAccount
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface UserAccountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: UserAccount)

    @Query("SELECT * FROM user_accounts WHERE email = :email LIMIT 1")
    suspend fun getAccountByEmail(email: String): UserAccount?
}

@Dao
interface UserProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)

    @Query("SELECT * FROM user_profiles WHERE email = :email LIMIT 1")
    suspend fun getProfileByEmail(email: String): UserProfile?

    @Query("SELECT * FROM user_profiles WHERE email = :email LIMIT 1")
    fun getProfileByEmailFlow(email: String): Flow<UserProfile?>
}

@Dao
interface FoodLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodLog(entry: FoodLogEntry)

    @Query("DELETE FROM food_log_entries WHERE id = :id")
    suspend fun deleteFoodLogById(id: Long)

    @Query("SELECT * FROM food_log_entries WHERE userEmail = :email AND dateString = :dateString")
    fun getFoodLogsByDate(email: String, dateString: String): Flow<List<FoodLogEntry>>

    @Query("SELECT * FROM food_log_entries WHERE userEmail = :email AND dateString = :dateString")
    suspend fun getFoodLogsByDateSync(email: String, dateString: String): List<FoodLogEntry>
    
    @Query("SELECT * FROM food_log_entries WHERE userEmail = :email")
    fun getAllFoodLogs(email: String): Flow<List<FoodLogEntry>>
    
    @Query("SELECT * FROM food_log_entries WHERE userEmail = :email")
    suspend fun getAllFoodLogsSync(email: String): List<FoodLogEntry>
}

@Dao
interface AIRecommendationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecommendation(rec: com.example.data.model.AIRecommendation)

    @Query("SELECT * FROM ai_recommendations WHERE email = :email LIMIT 1")
    fun getRecommendationFlow(email: String): Flow<com.example.data.model.AIRecommendation?>

    @Query("SELECT * FROM ai_recommendations WHERE email = :email LIMIT 1")
    suspend fun getRecommendationSync(email: String): com.example.data.model.AIRecommendation?
}

@Database(
    entities = [
        UserAccount::class,
        UserProfile::class,
        FoodLogEntry::class,
        com.example.data.model.AIRecommendation::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userAccountDao(): UserAccountDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun foodLogDao(): FoodLogDao
    abstract fun aiRecommendationDao(): AIRecommendationDao
}
