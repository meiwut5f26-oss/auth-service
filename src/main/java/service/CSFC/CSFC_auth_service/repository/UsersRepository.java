package service.CSFC.CSFC_auth_service.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import service.CSFC.CSFC_auth_service.model.entity.Users;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsersRepository extends JpaRepository<Users, UUID> {
      Optional<Users> findByEmail(String email);
      Optional<Users> findById(UUID id);
      boolean existsByEmail(String email);

      @Query("SELECT u FROM Users u " +
              "WHERE (:name IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
              "AND (:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) " +
              "AND (:phone IS NULL OR u.phone LIKE CONCAT('%', :phone, '%')) " +
              "AND (:status IS NULL OR LOWER(u.status) = LOWER(:status))")
      Page<Users> searchUsers(String name, String email, String phone, String status, Pageable pageable);
}