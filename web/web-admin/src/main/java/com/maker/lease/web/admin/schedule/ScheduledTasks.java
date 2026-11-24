package com.maker.lease.web.admin.schedule;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.maker.lease.model.entity.LeaseAgreement;
import com.maker.lease.model.enums.LeaseStatus;
import com.maker.lease.web.admin.mapper.LeaseAgreementMapper;
import com.maker.lease.web.admin.service.LeaseAgreementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;


@Component
public class ScheduledTasks {
    @Autowired
    private LeaseAgreementService leaseAgreementService;
    @Scheduled(cron = "0 0 0 * * *")
    public void checkLeaseStatus(){
        LambdaUpdateWrapper<LeaseAgreement> updateWrapper = new LambdaUpdateWrapper<>();
        Date now=new Date();
        updateWrapper
                .set(LeaseAgreement::getStatus, LeaseStatus.EXPIRED)
                .le(LeaseAgreement::getLeaseEndDate, new Date())
                .in(LeaseAgreement::getStatus, LeaseStatus.SIGNED, LeaseStatus.WITHDRAWING);
        leaseAgreementService.update(updateWrapper);
    }

}
