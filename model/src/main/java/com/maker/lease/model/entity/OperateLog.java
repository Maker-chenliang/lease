package com.maker.lease.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 操作日志
 * @TableName operate_log
 */
@TableName(value ="operate_log")
@Data
public class OperateLog {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 操作人id
     */
    private Long userId;

    /**
     * 操作人
     */
    private String username;

    /**
     * 操作时间
     */
    private LocalDateTime operateTime;

    /**
     * 类名
     */
    private String className;

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 请求参数(已脱敏)
     */
    private String methodParams;

    /**
     * 返回值
     */
    private String returnValue;

    /**
     * 返回码
     */
    private Integer resultCode;

    /**
     * 异常信息
     */
    private String errorMsg;

    /**
     * 耗时ms
     */
    private Long costTime;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        OperateLog other = (OperateLog) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getUsername() == null ? other.getUsername() == null : this.getUsername().equals(other.getUsername()))
            && (this.getOperateTime() == null ? other.getOperateTime() == null : this.getOperateTime().equals(other.getOperateTime()))
            && (this.getClassName() == null ? other.getClassName() == null : this.getClassName().equals(other.getClassName()))
            && (this.getMethodName() == null ? other.getMethodName() == null : this.getMethodName().equals(other.getMethodName()))
            && (this.getMethodParams() == null ? other.getMethodParams() == null : this.getMethodParams().equals(other.getMethodParams()))
            && (this.getReturnValue() == null ? other.getReturnValue() == null : this.getReturnValue().equals(other.getReturnValue()))
            && (this.getResultCode() == null ? other.getResultCode() == null : this.getResultCode().equals(other.getResultCode()))
            && (this.getErrorMsg() == null ? other.getErrorMsg() == null : this.getErrorMsg().equals(other.getErrorMsg()))
            && (this.getCostTime() == null ? other.getCostTime() == null : this.getCostTime().equals(other.getCostTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getUsername() == null) ? 0 : getUsername().hashCode());
        result = prime * result + ((getOperateTime() == null) ? 0 : getOperateTime().hashCode());
        result = prime * result + ((getClassName() == null) ? 0 : getClassName().hashCode());
        result = prime * result + ((getMethodName() == null) ? 0 : getMethodName().hashCode());
        result = prime * result + ((getMethodParams() == null) ? 0 : getMethodParams().hashCode());
        result = prime * result + ((getReturnValue() == null) ? 0 : getReturnValue().hashCode());
        result = prime * result + ((getResultCode() == null) ? 0 : getResultCode().hashCode());
        result = prime * result + ((getErrorMsg() == null) ? 0 : getErrorMsg().hashCode());
        result = prime * result + ((getCostTime() == null) ? 0 : getCostTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", userId=").append(userId);
        sb.append(", username=").append(username);
        sb.append(", operateTime=").append(operateTime);
        sb.append(", className=").append(className);
        sb.append(", methodName=").append(methodName);
        sb.append(", methodParams=").append(methodParams);
        sb.append(", returnValue=").append(returnValue);
        sb.append(", resultCode=").append(resultCode);
        sb.append(", errorMsg=").append(errorMsg);
        sb.append(", costTime=").append(costTime);
        sb.append("]");
        return sb.toString();
    }
}