package main.java.service;

import main.java.domain.Group;
import main.java.repository.external.ExternalRepository;
import main.java.repository.internal.GroupRepository;

import java.util.List;

public class GroupService {
	
	private ExternalRepository externalRepository;
	private GroupRepository groupRepository;
	
	public GroupService(ExternalRepository externalRepository, GroupRepository groupRepository) {
		this.externalRepository = externalRepository;
		this.groupRepository = groupRepository;
	}
	
	public List<Group> getExternalGroups() throws Exception {
		List<Group> groups = externalRepository.selectExternalGroups();
		return groups;
	}
	
	public List<Group> getGroups() throws Exception {
		List<Group> groups = groupRepository.selectGroups();
		return groups;
	}
	
	public int createGroup(Group group) throws Exception {
		return groupRepository.createGroup(group);
	}
	
	public int updateGroup(Group group) throws Exception {
		return groupRepository.updateGroup(group);
	}
	
	public int deleteGroup(String groupId) throws Exception {
		return groupRepository.deleteGroup(groupId);
	}
	
}