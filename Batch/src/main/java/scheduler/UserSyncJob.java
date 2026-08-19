package main.java.scheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import main.java.domain.SyncResult;
import main.java.synchronizer.UserSynchronizer;

@Component
public class UserSyncJob implements Job {
	
	private static final Logger log = LoggerFactory.getLogger(UserSyncJob.class);
	
	private final UserSynchronizer userSynchronizer;
	
	public UserSyncJob(UserSynchronizer userSynchronizer) {
		this.userSynchronizer = userSynchronizer;
	}
	
	@Override
	public void execute(JobExecutionContext context) {
		
		log.info("========================================");
		log.info("User Synchronizer started.");
		log.info("========================================");
		
		try {
			SyncResult result = userSynchronizer.synchronize();
			log.info("User Synchronizer completed.");
			log.info("Total : {}" + result.getTotalCount());
			log.info("Success : {}" + result.getTotalSuccess());
			log.info("Fail : {}" + result.getTotalFail());
        } catch (Exception e) {
        	log.error("User Synchronizer failed.", e.getMessage());
        }
	}
}