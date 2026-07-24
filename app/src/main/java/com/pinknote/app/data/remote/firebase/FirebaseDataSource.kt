package com.pinknote.app.data.remote.firebase

import android.content.Context
import android.net.Uri
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.pinknote.app.domain.model.CycleSettings
import com.pinknote.app.domain.model.DailyLog
import com.pinknote.app.domain.model.Reminder
import com.pinknote.app.domain.model.UserProfile
import com.pinknote.app.R
import com.pinknote.app.utils.AdminPolicy
import com.pinknote.app.utils.Constants
import com.pinknote.app.utils.DateUtils.toStorageString
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class FirebaseDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    private val googleSignInClient by lazy {
        GoogleSignIn.getClient(
            context,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(context.getString(R.string.google_web_client_id))
                .build()
        )
    }

    fun currentUid(): String? = auth.currentUser?.uid

    suspend fun registerWithEmail(name: String, email: String, password: String): UserProfile {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = requireNotNull(result.user)
        val profile = UserProfile(
            uid = firebaseUser.uid,
            name = name,
            email = email,
            avatarUrl = firebaseUser.photoUrl?.toString()
        )
        saveUser(profile)
        return profile
    }

    suspend fun loginWithEmail(email: String, password: String): UserProfile {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val firebaseUser = requireNotNull(result.user)
        return getUser(firebaseUser.uid) ?: UserProfile(
            uid = firebaseUser.uid,
            name = firebaseUser.displayName.orEmpty(),
            email = firebaseUser.email.orEmpty(),
            avatarUrl = firebaseUser.photoUrl?.toString()
        ).also { saveUser(it) }
    }

    suspend fun loginWithGoogle(idToken: String): UserProfile {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        val firebaseUser = requireNotNull(result.user)
        return getUser(firebaseUser.uid) ?: UserProfile(
            uid = firebaseUser.uid,
            name = firebaseUser.displayName.orEmpty(),
            email = firebaseUser.email.orEmpty(),
            avatarUrl = firebaseUser.photoUrl?.toString()
        ).also { saveUser(it) }
    }

    suspend fun sendPasswordReset(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    suspend fun logout() {
        auth.signOut()
        runCatching { googleSignInClient.signOut().await() }
    }

    suspend fun deleteAccount() {
        auth.currentUser?.delete()?.await()
    }

    suspend fun uploadAvatar(uid: String, uri: Uri): String {
        val ref = storage.reference.child("avatars/$uid.jpg")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun saveUser(profile: UserProfile) {
        firestore.collection(Constants.USERS_COLLECTION)
            .document(profile.uid)
            .set(profile.toFirestoreMap())
            .await()
    }

    suspend fun getUser(uid: String): UserProfile? {
        val snapshot = firestore.collection(Constants.USERS_COLLECTION).document(uid).get().await()
        val data = snapshot.data ?: return null
        if (!data.containsKey("role")) {
            setUserRole(uid, AdminPolicy.ROLE_USER)
        }
        return data.toUserProfile(uid)
    }

    fun observeUsers(): Flow<List<UserProfile>> = callbackFlow {
        val registration = firestore.collection(Constants.USERS_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val users = snapshot?.documents
                    ?.mapNotNull { document ->
                        val data = document.data ?: return@mapNotNull null
                        if (!data.containsKey("role")) {
                            document.reference.set(
                                mapOf("role" to AdminPolicy.ROLE_USER),
                                SetOptions.merge()
                            )
                        }
                        data.toUserProfile(document.id)
                    }
                    ?.sortedByDescending { it.createdAtEpochMillis }
                    .orEmpty()
                trySend(users)
            }

        awaitClose { registration.remove() }
    }

    suspend fun setUserRole(uid: String, role: String) {
        firestore.collection(Constants.USERS_COLLECTION)
            .document(uid)
            .set(mapOf("role" to AdminPolicy.normalizeRole(role)), SetOptions.merge())
            .await()
    }

    suspend fun saveCycle(settings: CycleSettings) {
        firestore.collection(Constants.CYCLE_COLLECTION)
            .document(settings.uid)
            .set(settings.toFirestoreMap())
            .await()
    }

    suspend fun saveDailyLog(log: DailyLog) {
        firestore.collection(Constants.DAILY_LOGS_COLLECTION)
            .document(log.uid)
            .collection("days")
            .document(log.date.toStorageString())
            .set(log.toFirestoreMap())
            .await()
    }

    suspend fun saveReminder(reminder: Reminder) {
        firestore.collection(Constants.NOTIFICATIONS_COLLECTION)
            .document(reminder.uid)
            .collection("items")
            .document(reminder.id)
            .set(reminder.toFirestoreMap())
            .await()
    }

    private fun UserProfile.toFirestoreMap(): Map<String, Any?> = mapOf(
        "uid" to uid,
        "name" to name,
        "email" to email,
        "birthday" to birthday?.toStorageString(),
        "avatar" to avatarUrl,
        "heightCm" to heightCm,
        "weightKg" to weightKg,
        "healthGoal" to healthGoal,
        "averageCycleLength" to averageCycleLength,
        "periodLength" to periodLength,
        "role" to role,
        "createdAt" to createdAtEpochMillis
    )

    private fun CycleSettings.toFirestoreMap(): Map<String, Any?> = mapOf(
        "lastPeriod" to lastPeriodStart.toStorageString(),
        "cycleLength" to cycleLength,
        "periodLength" to periodLength,
        "updatedAt" to updatedAtEpochMillis
    )

    private fun DailyLog.toFirestoreMap(): Map<String, Any?> = mapOf(
        "date" to date.toStorageString(),
        "mood" to mood,
        "pain" to painLevel,
        "temperature" to bodyTemperature,
        "weight" to weightKg,
        "symptom" to symptoms,
        "discharge" to discharge,
        "medicine" to medicines,
        "sex" to hadSex,
        "note" to note,
        "updatedAt" to updatedAtEpochMillis
    )

    private fun Reminder.toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "type" to type.name,
        "title" to title,
        "message" to message,
        "scheduledAt" to scheduledAt.toString(),
        "enabled" to enabled
    )

    private fun Map<String, Any?>.toUserProfile(uid: String): UserProfile {
        val birthdayText = this["birthday"] as? String
        return UserProfile(
            uid = uid,
            name = this["name"] as? String ?: "",
            email = this["email"] as? String ?: "",
            birthday = birthdayText?.let(LocalDate::parse),
            avatarUrl = this["avatar"] as? String,
            heightCm = (this["heightCm"] as? Number)?.toFloat(),
            weightKg = (this["weightKg"] as? Number)?.toFloat(),
            healthGoal = this["healthGoal"] as? String ?: "",
            averageCycleLength = (this["averageCycleLength"] as? Number)?.toInt() ?: Constants.DEFAULT_CYCLE_LENGTH,
            periodLength = (this["periodLength"] as? Number)?.toInt() ?: Constants.DEFAULT_PERIOD_LENGTH,
            role = AdminPolicy.normalizeRole(this["role"] as? String),
            createdAtEpochMillis = (this["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }
}
