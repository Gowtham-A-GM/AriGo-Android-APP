package com.example.arigo.data.repository

import com.example.arigo.core.common.Resource
import com.example.arigo.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override val isUserLoggedIn: Boolean
        get() = firebaseAuth.currentUser != null

    override val currentUserId: String?
        get() = firebaseAuth.currentUser?.uid

    override suspend fun loginWithEmail(email: String, password: String): Resource<Unit> {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email.trim(), password).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Login failed.")
        }
    }

    override suspend fun signupWithEmail(email: String, password: String): Resource<String> {
        return try {
            val result = firebaseAuth
                .createUserWithEmailAndPassword(email.trim(), password)
                .await()
            val uid = result.user?.uid
                ?: return Resource.Error("Signup succeeded but no user ID was returned.")
            Resource.Success(uid)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Signup failed.")
        }
    }

    override suspend fun loginWithGoogle(idToken: String): Resource<Unit> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            firebaseAuth.signInWithCredential(credential).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Google sign-in failed.")
        }
    }

    override suspend fun sendPasswordReset(email: String): Resource<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email.trim()).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Could not send reset email.")
        }
    }

    override suspend fun logout() {
        firebaseAuth.signOut()
    }
}
