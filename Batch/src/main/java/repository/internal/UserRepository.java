package main.java.repository.internal;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import main.java.domain.User;

import java.util.List;

@Mapper
public interface UserRepository {
	
	List<User> selectUsers();
	
	int createUser(User user);
	
	int updateUser(User user);
	
	int deleteUser(@Param("userId") String userId);
}