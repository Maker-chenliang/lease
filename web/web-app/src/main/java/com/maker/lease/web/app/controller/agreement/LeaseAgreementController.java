package com.maker.lease.web.app.controller.agreement;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.maker.lease.common.login.LoginUserHolder;
import com.maker.lease.common.result.Result;
import com.maker.lease.common.result.ResultCodeEnum;
import com.maker.lease.model.entity.LeaseAgreement;
import com.maker.lease.model.entity.ViewAppointment;
import com.maker.lease.model.enums.LeaseStatus;
import com.maker.lease.web.app.service.LeaseAgreementService;
import com.maker.lease.web.app.vo.agreement.AgreementDetailVo;
import com.maker.lease.web.app.vo.agreement.AgreementItemVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/app/agreement")
@Tag(name = "租约信息")
public class LeaseAgreementController {
    @Autowired
    private LeaseAgreementService service;
    @Operation(summary = "获取个人租约基本信息列表")
    @GetMapping("listItem")
    public Result<List<AgreementItemVo>> listItem() {
        List<AgreementItemVo> agreementItemVoList=service.getAgreementItemList(LoginUserHolder.getLoginUser().getUsername());
        return Result.ok(agreementItemVoList);
    }

    @Operation(summary = "根据id获取租约详细信息")
    @GetMapping("getDetailById")
    public Result<AgreementDetailVo> getDetailById(@RequestParam Long id) {
        LeaseAgreement leaseAgreement = service.getById(id);
        if (leaseAgreement == null
                || !Objects.equals(LoginUserHolder.getLoginUser().getUsername(), leaseAgreement.getPhone())) {
            return Result.build(null, ResultCodeEnum.APP_ACCESS_FORBIDDEN);
        }
        AgreementDetailVo agreementDetailVo = service.getDetailById(id);
        return Result.ok(agreementDetailVo);
    }

    @Operation(summary = "根据id更新租约状态", description = "用于确认租约和提前退租")
    @PostMapping("updateStatusById")
    public Result updateStatusById(@RequestParam Long id, @RequestParam LeaseStatus leaseStatus) {
        LeaseAgreement leaseAgreement = service.getById(id);
        if (leaseAgreement == null
                || !Objects.equals(LoginUserHolder.getLoginUser().getUsername(), leaseAgreement.getPhone())) {
            return Result.build(null, ResultCodeEnum.APP_ACCESS_FORBIDDEN);
        }
        LambdaUpdateWrapper<LeaseAgreement> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(LeaseAgreement::getId, id);
        updateWrapper.set(LeaseAgreement::getStatus, leaseStatus);
        service.update(updateWrapper);
        return Result.ok();
    }

    @Operation(summary = "保存或更新租约", description = "用于续约")
    @PostMapping("saveOrUpdate")
    public Result saveOrUpdate(@RequestBody LeaseAgreement leaseAgreement) {
        String phone = LoginUserHolder.getLoginUser().getUsername();

        // 更新：用数据库里的记录判断归属，不能用请求体里的 phone
        if (leaseAgreement.getId() != null) {
            LeaseAgreement old = service.getById(leaseAgreement.getId());
            if (old == null || !Objects.equals(phone, old.getPhone())) {
                return Result.build(null, ResultCodeEnum.APP_ACCESS_FORBIDDEN);
            }
        }
        service.saveOrUpdate(leaseAgreement);
        return Result.ok();
    }

}
