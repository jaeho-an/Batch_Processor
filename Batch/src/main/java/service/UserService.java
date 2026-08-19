package main.java.service;

import main.java.domain.User;
import main.java.repository.external.ExternalRepository;
import main.java.repository.internal.UserRepository;

import java.util.List;

public class UserService {
	
	private ExternalRepository externalRepository;
	private UserRepository userRepository;
	
	public UserService(ExternalRepository externalRepository, UserRepository userRepository) {
		this.externalRepository = externalRepository;
		this.userRepository = userRepository;
	}
	
	public List<User> getExternalUsers() throws Exception {
		List<User> users = externalRepository.selectExternalUsers();
		return users;
	}
	
	public List<User> getUsers() throws Exception {
		List<User> users = userRepository.selectUsers();
		return users;
	}
	
	public int createUser(User user) throws Exception {
		return userRepository.createUser(user);
	}
	
	public int updateUser(User user) throws Exception {
		return userRepository.updateUser(user);
	}
	
	public int deleteUser(String userId) throws Exception {
		return userRepository.deleteUser(userId);
	}
	
}