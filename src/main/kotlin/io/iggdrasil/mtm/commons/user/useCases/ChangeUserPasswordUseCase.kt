package io.iggdrasil.mtm.commons.user.useCases

import io.iggdrasil.mtm.commons.repository.UserRepositoryPort
import io.iggdrasil.mtm.commons.user.UserRole
import io.iggdrasil.mtm.commons.user.password.Password
import io.iggdrasil.mtm.commons.user.password.PasswordEncoder
import models.ID
import utils.ResourceNotFoundException
import utils.UnauthorizedException

class ChangeUserPasswordUseCase(
    private val repository: UserRepositoryPort,
    private val passwordEncoder: PasswordEncoder
) {

    suspend fun execute(requesterId: ID, targetUserId: ID, rawPassword: String) {
        val requester = repository.findById(requesterId)
            ?: throw ResourceNotFoundException("Requester with id $requesterId not found")

        val target = repository.findById(targetUserId)
            ?: throw ResourceNotFoundException("User with id $targetUserId not found")

        validatePermission(requesterId, requester.role, targetUserId)

        val hashedPassword = passwordEncoder.encode(rawPassword)
        val newPassword = Password.ofHashed(hashedPassword)

        val updatedUser = target.changePassword(newPassword)
        repository.update(updatedUser)
    }

    private fun validatePermission(requesterId: ID, requesterRole: UserRole, targetId: ID) {
        val isSelfChange = requesterId == targetId
        val isAdmin = requesterRole == UserRole.ADMIN

        if (!isSelfChange && !isAdmin) {
            throw UnauthorizedException(
                "User $requesterId is not allowed to change password for $targetId"
            )
        }
    }
}