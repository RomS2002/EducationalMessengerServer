package ru.roms2002.messenger.server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ru.roms2002.messenger.server.entity.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {

	List<UserEntity> findByEmail(String email);

//    UserEntity getUserByFirstNameOrMail(String firstName, String mail);
//
//    @Query(value = "SELECT u.firstname, u.lastname FROM user u WHERE u.id = :userId", nativeQuery = true)
//    String getUsernameByUserId(@Param(value = "userId") int id);
//
//    @Query(value = "SELECT u.firstname FROM user u WHERE u.id = :userId", nativeQuery = true)
//    String getFirstNameByUserId(@Param(value = "userId") int id);
//
//    @Query(value = "SELECT u.firstname FROM user u WHERE u.wstoken = :token", nativeQuery = true)
//    String getUsernameWithWsToken(@Param(value = "token") String token);
//
//    @Query(value = "SELECT u.id FROM user u WHERE u.wstoken = :token", nativeQuery = true)
//    int getUserIdWithWsToken(@Param(value = "token") String token);

//    @Query(value = "SELECT * FROM user u WHERE u.id NOT IN :ids", nativeQuery = true)
//    List<UserEntity> getAllUsersNotAlreadyInConversation(@Param(value = "ids") int[] ids);

//    int countAllByFirstNameOrMail(String firstName, String mail);
//
//    int countAllByShortUrl(String shortUrl);
}
