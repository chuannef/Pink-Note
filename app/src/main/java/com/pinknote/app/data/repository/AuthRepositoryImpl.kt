package com.pinknote.app.data.repository

import com.pinknote.app.data.local.dao.CycleDao
import com.pinknote.app.data.local.dao.DailyLogDao
import com.pinknote.app.data.local.dao.ReminderDao
import com.pinknote.app.data.local.dao.UserDao
import com.pinknote.app.data.remote.firebase.FirebaseDataSource
import com.pinknote.app.domain.model.AppResult
import com.pinknote.app.domain.model.UserProfile
import com.pinknote.app.domain.repository.AuthRepository
import com.pinknote.app.worker.ReminderScheduler
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firebaseDataSource: FirebaseDataSource,
    private val userDao: UserDao,
    private val cycleDao: CycleDao,
    private val dailyLogDao: DailyLogDao,
    private val reminderDao: ReminderDao,
    private val reminderScheduler: ReminderScheduler
) : AuthRepository {
    override val currentUser: Flow<UserProfile?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            trySend(user?.let {
                UserProfile(
                    uid = it.uid,
                    name = it.displayName.orEmpty(),
                    email = it.email.orEmpty(),
                    avatarUrl = it.photoUrl?.toString()
                )
            })
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun registerWithEmail(name: String, email: String, password: String): AppResult<UserProfile> {
        return runCatching {
            firebaseDataSource.registerWithEmail(name, email, password).also { userDao.upsert(it.toEntity()) }
        }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { AppResult.Error(it.toAuthMessage("Không thể đăng ký"), it) }
        )
    }

    override suspend fun loginWithEmail(email: String, password: String): AppResult<UserProfile> {
        return runCatching {
            firebaseDataSource.loginWithEmail(email, password).also { userDao.upsert(it.toEntity()) }
        }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { AppResult.Error(it.toAuthMessage("Không thể đăng nhập"), it) }
        )
    }

    override suspend fun loginWithGoogle(idToken: String): AppResult<UserProfile> {
        return runCatching {
            firebaseDataSource.loginWithGoogle(idToken).also { userDao.upsert(it.toEntity()) }
        }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { AppResult.Error(it.toAuthMessage("Không thể đăng nhập Google"), it) }
        )
    }

    override suspend fun sendPasswordReset(email: String): AppResult<Unit> {
        return runCatching { firebaseDataSource.sendPasswordReset(email) }.fold(
            onSuccess = { AppResult.Success(Unit) },
            onFailure = {
                if ((it as? FirebaseAuthException)?.errorCode == "ERROR_USER_NOT_FOUND") {
                    AppResult.Success(Unit)
                } else {
                    AppResult.Error(it.toAuthMessage("Không thể gửi email đặt lại mật khẩu"), it)
                }
            }
        )
    }

    override suspend fun logout() {
        firebaseDataSource.logout()
    }

    override suspend fun deleteAccount(): AppResult<Unit> {
        return runCatching {
            val uid = auth.currentUser?.uid
            firebaseDataSource.deleteAccount()
            if (uid != null) {
                deleteLocalUserData(uid)
            }
        }.fold(
            onSuccess = { AppResult.Success(Unit) },
            onFailure = { AppResult.Error(it.toAuthMessage("Không thể xóa tài khoản"), it) }
        )
    }

    private suspend fun deleteLocalUserData(uid: String) {
        reminderDao.getIdsByUid(uid).forEach(reminderScheduler::cancel)
        reminderDao.deleteByUid(uid)
        dailyLogDao.deleteByUid(uid)
        cycleDao.deleteByUid(uid)
        userDao.deleteByUid(uid)
    }

    private fun Throwable.toAuthMessage(fallback: String): String {
        if (hasCause<FirebaseNetworkException>()) {
            return "Không thể kết nối Firebase. Hãy kiểm tra internet, ngày giờ thiết bị, Google Play Services và thử lại."
        }

        val authCode = (this as? FirebaseAuthException)?.errorCode
        return when (authCode) {
            "ERROR_CONFIGURATION_NOT_FOUND" ->
                "Firebase Authentication chưa được bật hoặc chưa cấu hình provider. Hãy bật Email/Password hoặc Google trong Firebase Console."
            "ERROR_NETWORK_REQUEST_FAILED" ->
                "Không thể kết nối Firebase. Hãy kiểm tra internet, ngày giờ thiết bị, Google Play Services và thử lại."
            "ERROR_EMAIL_ALREADY_IN_USE" -> "Email này đã được đăng ký."
            "ERROR_INVALID_EMAIL" -> "Email không hợp lệ."
            "ERROR_WEAK_PASSWORD" -> "Mật khẩu quá yếu, hãy nhập ít nhất 6 ký tự."
            "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL" -> "Email hoặc mật khẩu không đúng."
            "ERROR_REQUIRES_RECENT_LOGIN" -> "Vì lý do bảo mật, hãy đăng xuất rồi đăng nhập lại trước khi xóa tài khoản."
            "ERROR_USER_NOT_FOUND" -> "Tài khoản không tồn tại."
            else -> message ?: fallback
        }
    }

    private inline fun <reified T : Throwable> Throwable.hasCause(): Boolean {
        var current: Throwable? = this
        while (current != null) {
            if (current is T) return true
            current = current.cause
        }
        return false
    }
}
