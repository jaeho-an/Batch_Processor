package main.java.repository.external;

import org.apache.ibatis.annotations.Mapper;

import main.java.domain.User;
import main.java.domain.Group;

import java.util.List;

@Mapper
public interface ExternalRepository {
	
	List<User> selectExternalUsers();
	
	List<Group> selectExternalGroups();
	
}