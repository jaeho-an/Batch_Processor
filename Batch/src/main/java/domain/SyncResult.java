package main.java.domain;

public class SyncResult {
	private int createdSuccess;
	private int createdFail;
	
	private int updatedSuccess;
	private int updatedFail;
	
	private int deletedSuccess;
	private int deletedFail;
	
	public void addCreatedSuccess() {
		createdSuccess++;
	}
	
	public void addCreatedFail() {
		createdFail++;
	}
	
	public void addUpdatedSuccess() {
		updatedSuccess++;
	}
	
	public void addUpdatedFail() {
		updatedFail++;
	}
	
	public void addDeletedSuccess() {
		deletedSuccess++;
	}
	
	public void addDeletedFail() {
		deletedFail++;
	}
	
	public int getCreatedSuccess() {
		return createdSuccess;
	}
	
	public int getCreatedFail() {
		return createdFail;
	}
	
	public int getUpdatedSuccess() {
		return updatedSuccess;
	}
	
	public int getUpdatedFail() {
		return updatedFail;
	}
	
	public int getDeletedSuccess() {
		return deletedSuccess;
	}
	
	public int getDeletedFail() {
		return deletedFail;
	}
	
	public int getTotalSuccess() {
		return createdSuccess
				+ updatedSuccess
				+ deletedSuccess;
	}
	
	public int getTotalFail() {
		return createdFail
				+ updatedFail
				+ deletedFail;
	}
	
	public int getTotalCount() {
		return getTotalSuccess() + getTotalFail();
	}
	
	public String getMessage() {
		return "사용자 동기화 완료"
				+ " [전체 " + getTotalCount() + "건, "
				+ "성공 " + getTotalSuccess() + "건, "
				+ "실패 " + getTotalFail() + "건]"
				+ " / 신규 " + createdSuccess + "건"
				+ " / 수정 " + updatedSuccess + "건"
				+ " / 삭제(퇴사) " + deletedSuccess + "건";
	}
	
}