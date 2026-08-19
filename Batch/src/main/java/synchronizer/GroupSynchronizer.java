package main.java.synchronizer;

import main.java.domain.SyncResult;
import main.java.domain.Group;
import main.java.service.GroupService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroupSynchronizer {
	
	private static final Logger logger = LoggerFactory.getLogger(GroupSynchronizer.class);
	
	private GroupService groupService;
	
	public GroupSynchronizer(GroupService groupService) {
		this.groupService = groupService;
	}
	
	public SyncResult synchronize() throws Exception {
		
		SyncResult result = new SyncResult();
		
		/*
		 * 1. 외부 부서 조회 (동기화 대상 부서 조회)
		 */
		List<Group> externalGroups = groupService.getExternalGroups();
		
		/*
		 * 2. 동기화 되어있는 부서 조회
		 */
		List<Group> internalGroups = groupService.getGroups();
		
		/*
		 * 3. 부서 생성/수정/삭제 처리에 대한 결과 값 세팅
		 */
		List<Group> createdGroup = new ArrayList<Group>();
		List<Group> updatedGroup = new ArrayList<Group>();
		List<Group> deletedGroup = new ArrayList<Group>();
		List<Group> existingGroup = new ArrayList<Group>();
		
		// 동기화된 부서에 대한 groupId 추출 (비교용)
		Map<String, Group> internalGroupMap = new HashMap<String, Group>();
		
		for (Group group : internalGroups) {
			internalGroupMap.put(group.getGroupId(), group);
		}
		
		/*
		 * 4. External 부서 기준 신규 생성 부서 추출 및 기존 부서 분리
		 */
		for (Group externalGroup : externalGroups) {
			
			String groupId = externalGroup.getGroupId();
			Group internalGroup = internalGroupMap.get(groupId);
			
			// 신규 생성 부서 부분으로 추가
			if (internalGroup == null) {
				createdGroup.add(internalGroup);
				continue;
			}
			
			// 이미 동기화된 부서
			existingGroup.add(internalGroup);
		}
		
		/*
		 * 4-1. External 부서 기준 수정/삭제 부서 추출
		 */
		for (Group externalGroup : existingGroup) {
			Group internalGroup = internalGroupMap.get(externalGroup.getGroupId());
			
			if (!isSameGroup(externalGroup, internalGroup)) {
				continue;
			}
			
			if(isUnActive(externalGroup, internalGroup)) {
				deletedGroup.add(externalGroup);
				continue;
			}
			
			if(isChanged(externalGroup, internalGroup)) {
				updatedGroup.add(externalGroup);
			}
		}
		
		/*
		 * 5. 신규 생성 부서 처리
		 */
		for (Group group : createdGroup) {
			try {
				int count = groupService.createGroup(group);
				
				if (count > 0) {
					result.addCreatedSuccess();
				} else {
					result.addCreatedFail();
				}
			} catch (Exception e) {
				result.addCreatedFail();
				logger.error("부서 생성 실패 : " + group.getGroupId());
			}
		}
		
		/*
		 * 6. 수정 부서 처리
		 */
		for (Group group : updatedGroup) {
			try {
				int count = groupService.updateGroup(group);
				
				if (count > 0) {
					result.addUpdatedSuccess();
				} else {
					result.addUpdatedFail();
				}
			} catch (Exception e) {
				result.addUpdatedFail();
				logger.error("부서 수정 실패 : " + group.getGroupId());
			}
		}
		
		/*
		 * 7. 삭제(비활성화) 부서 처리
		 */
		for (Group group : deletedGroup) {
			try {
				int count = groupService.deleteGroup(group.getGroupId());
				
				if (count > 0) {
					result.addDeletedSuccess();
				} else {
					result.addDeletedFail();
				}
			} catch (Exception e) {
				result.addDeletedFail();
				logger.error("부서 비활성화 처리 실패 : " + group.getGroupId());
			}
		}
		
		logger.info(result.getMessage());
		return result;
	}
	
	/*
	 * 동일한 부서 여부 확인
	 */
	private boolean isSameGroup(Group externalGroup, Group internalGroup) throws Exception {
		return equals(externalGroup.getGroupId(), internalGroup.getGroupId());
	}
	
	/*
	 * 비활성화 여부 확인
	 * External / Internal의 status가 다르고, External 부서 상태가 비활성화 상태인 경우
	 *
	 */
	private boolean isUnActive(Group externalGroup, Group internalGroup) throws Exception {
		// 활성화 상태 동일 시 변경 x
		if (equals(externalGroup.getStatus(), internalGroup.getStatus())) {
			return false;
		}
		
		return isUnActiveStatus(externalGroup.getStatus());
	}
	
	/*
	 * External 부서 상태가 비활성화 상태인지 확인
	 */
	private boolean isUnActiveStatus(String status) throws Exception {
		return "UN_ACTIVE".equals(status);
	}
	
	/*
	 * 부서 정보 변경 여부 확인
	 */
	private boolean isChanged(Group externalGroup, Group internalGroup) throws Exception {
		// 이름 변경
		if (!equals(externalGroup.getGroupName(), internalGroup.getGroupName())) {
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