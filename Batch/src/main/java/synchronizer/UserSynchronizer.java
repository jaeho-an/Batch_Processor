package main.java.synchronizer;

import main.java.domain.SyncResult;
import main.java.domain.User;
import main.java.service.UserService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserSynchronizer {
	
	private static final Logger logger = LoggerFactory.getLogger(UserSynchronizer.class);
	
	private UserService userService;
	
	public UserSynchronizer(UserService userService) {
		this.userService = userService;
	}
	
	public SyncResult synchronize() throws Exception {
		
		SyncResult result = new SyncResult();
		
		/*
		 * 1. 외부 사용자 조회 (동기화 대상 사용자 조회)
		 */
		List<User> externalUsers = userService.getExternalUsers();
		
		/*
		 * 2. 동기화 되어있는 사용자 조회
		 */
		List<User> internalUsers = userService.getUsers();
		
		/*
		 * 3. 사용자 생성/수정/삭제 처리에 대한 결과 값 세팅
		 */
		List<User> createdUser = new ArrayList<User>();
		List<User> updatedUser = new ArrayList<User>();
		List<User> deletedUser = new ArrayList<User>();
		List<User> existingUser = new ArrayList<User>();
		
		// 동기화된 사용자에 대한 userId 추출 (비교용)
		Map<String, User> internalUserMap = new HashMap<String, User>();
		
		for (User user : internalUsers) {
			internalUserMap.put(user.getUserId(), user);
		}
		
		/*
		 * 4. External 사용자 기준 신규 생성 사용자 추출 및 기존 사용자 분리
		 */
		for (User externalUser : externalUsers) {
			
			String userId = externalUser.getUserId();
			User internalUser = internalUserMap.get(userId);
			
			// 신규 생성 사용자 부분으로 추가
			if (internalUser == null) {
				createdUser.add(externalUser);
				continue;
			}
			
			// 이미 동기화된 사용자
			existingUser.add(externalUser);
		}
		
		/*
		 * 4-1. External 사용자 기준 수정/삭제 사용자 추출
		 */
		for (User externalUser : existingUser) {
			User internalUser = internalUserMap.get(externalUser.getUserId());
			
			if (!isSameUser(externalUser, internalUser)) {
				continue;
			}
			
			if(isRetired(externalUser, internalUser)) {
				deletedUser.add(externalUser);
				continue;
			}
			
			if(isChanged(externalUser, internalUser)) {
				updatedUser.add(externalUser);
			}
		}
		
		/*
		 * 5. 신규 생성 사용자 처리
		 */
		for (User user : createdUser) {
			try {
				int count = userService.createUser(user);
				
				if (count > 0) {
					result.addCreatedSuccess();
				} else {
					result.addCreatedFail();
				}
			} catch (Exception e) {
				result.addCreatedFail();
				logger.error("사용자 생성 실패 : " + user.getUserId());
			}
		}
		
		/*
		 * 6. 수정 사용자 처리
		 */
		for (User user : updatedUser) {
			try {
				int count = userService.updateUser(user);
				
				if (count > 0) {
					result.addUpdatedSuccess();
				} else {
					result.addUpdatedFail();
				}
			} catch (Exception e) {
				result.addUpdatedFail();
				logger.error("사용자 수정 실패 : " + user.getUserId());
			}
		}
		
		/*
		 * 7. 삭제(퇴사) 사용자 처리
		 */
		for (User user : deletedUser) {
			try {
				int count = userService.deleteUser(user.getUserId());
				
				if (count > 0) {
					result.addDeletedSuccess();
				} else {
					result.addDeletedFail();
				}
			} catch (Exception e) {
				result.addDeletedFail();
				logger.error("사용자 퇴사 처리 실패 : " + user.getUserId());
			}
		}
		
		logger.info(result.getMessage());
		return result;
	}
	
	/*
	 * 동일한 사용자 여부 확인
	 */
	private boolean isSameUser(User externalUser, User internalUser) throws Exception {
		return equals(externalUser.getUserId(), internalUser.getUserId());
	}
	
	/*
	 * 퇴사 여부 확인
	 * External / Internal의 employstatus가 다르고, External 사용자 상태가 퇴사 상태인 경우
	 *
	 */
	private boolean isRetired(User externalUser, User internalUser) throws Exception {
		// 재직 상태 동일 시 변경 x
		if (equals(externalUser.getEmployStatus(), internalUser.getEmployStatus())) {
			return false;
		}
		
		return isRetiredStatus(externalUser.getEmployStatus());
	}
	
	/*
	 * External 사용자 상태가 퇴사 상태인지 확인
	 */
	private boolean isRetiredStatus(String employStatus) throws Exception {
		return "RETIRED".equals(employStatus);
	}
	
	/*
	 * 사용자 정보 변경 여부 확인
	 */
	private boolean isChanged(User externalUser, User internalUser) throws Exception {
		// 부서 정보 변경
		if (!equals(externalUser.getGroupId(), internalUser.getGroupId())) {
			return true;
		}
		// 이름 변경
		if (!equals(externalUser.getUserName(), internalUser.getUserName())) {
			return true;
		}
		// 비밀번호 변경
		if (!equals(externalUser.getPassword(), internalUser.getPassword())) {
			return true;
		}
		// 이메일 변경
		if (!equals(externalUser.getEmail(), internalUser.getEmail())) {
			return true;
		}
		// 휴대폰 번호 변경
		if (!equals(externalUser.getPhone(), internalUser.getPhone())) {
			return true;
		}
		return false;
	}
	
	private boolean equals(String value1, String value2) throws Exception {
		if (value1 == null && value2 == null) {
			return true;
		}
		
		if (value1 == null || value2 == null) {
			return false;
		}
		
		return value1.equals(value2);
	}
}