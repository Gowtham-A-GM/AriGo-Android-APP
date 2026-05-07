package com.example.arigo.data.repository

import com.example.arigo.core.common.Constants
import com.example.arigo.core.common.Resource
import com.example.arigo.domain.model.UserProfile
import com.example.arigo.domain.repository.UserRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class UserRepositoryImpl(
    private val firebaseDatabase: FirebaseDatabase
) : UserRepository {

    private val usersRef
        get() = firebaseDatabase.getReference(Constants.FIREBASE_USERS_PATH)

    override suspend fun saveUserProfile(profile: UserProfile): Resource<Unit> {
        return try {
            usersRef.child(profile.uid).setValue(profile).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to save profile.")
        }
    }

    override suspend fun getUserProfile(uid: String): Resource<UserProfile> {
        return try {
            val snapshot = usersRef.child(uid).get().await()
            val profile = snapshot.getValue(UserProfile::class.java)
                ?: return Resource.Error("Profile not found.")
            Resource.Success(profile)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to load profile.")
        }
    }

    override fun observeUserProfile(uid: String): Flow<UserProfile?> = callbackFlow {
        val ref = usersRef.child(uid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.getValue(UserProfile::class.java))
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }
}
