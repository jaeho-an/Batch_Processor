package main.java.scheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import main.java.domain.SyncResult;
import main.java.synchronizer.GroupSynchronizer;

@Component
public class GroupSyncJob implements Job {
	
	private static final Logger log = LoggerFactory.getLogger(GroupSyncJob.class);
	
	private final GroupSynchronizer groupSynchronizer;
	
	public GroupSyncJob(GroupSynchronizer groupSynchronizer) {
		this.groupSynchronizer = groupSynchronizer;
	}
	
	@Override
	public void execute(JobExecutionContext context) {
		
		log.info("========================================");
		log.info("Group Synchronizer started.");
		log.info("========================================");
		
		try {
			SyncResult result = groupSynchronizer.synchronize();
			log.info("Group Synchronizer completed.");
			log.info("Total : {}" + result.getTotalCount());
			log.info("Success : {}" + result.getTotalSuccess());
			log.info("Fail : {}" + result.getTotalFail());
        } catch (Exception e) {
        	log.error("Group Synchronizer failed.", e.getMessage());
        }
	}
}