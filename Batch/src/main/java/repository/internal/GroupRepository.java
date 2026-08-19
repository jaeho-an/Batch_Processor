package main.java.repository.internal;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import main.java.domain.Group;

import java.util.List;

@Mapper
public interface GroupRepository {
	
	List<Group> selectGroups();
	
	int createGroup(Group group);
	
	int updateGroup(Group group);
	
	int deleteGroup(@Param("groupId") String groupId);
	
}