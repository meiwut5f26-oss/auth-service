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
      Optional<Users> findByPhone(String phone);
      boolean existsByPhone(String phone);

      @Query("SELECT u FROM Users u " +
              "WHERE (:name IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
              "AND (:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) " +
              "AND (:phone IS NULL OR u.phone LIKE CONCAT('%', :phone, '%')) " +
              "AND (:status IS NULL OR LOWER(u.status) = LOWER(:status)) " +
              "AND (:onlyCustomers = false OR LOWER(u.role.name) = 'customer')")
      Page<Users> searchUsers(String name, String email, String phone, String status, boolean onlyCustomers, Pageable pageable);

      @Query("SELECT CASE WHEN COUNT(u)>0 THEN true ELSE false END FROM Users u WHERE u.phone = :phone AND u.id <> :excludeId")
      boolean existsByPhoneAndIdNot(String phone, UUID excludeId);
}