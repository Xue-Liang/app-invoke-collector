package com.gos.monitor.server.manager.entity;


import java.io.Serializable;
import java.util.Date;


/**


@author Robot.Xue on 2017-05-25 17:41:41
*/
public class Graph implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
    
    */
    protected Long  id;
    /**
    
    */
    protected Integer  counterId;
    /**
    
    */
    protected Long  value;
    /**
    间隔时间 单位:秒
    */
    protected Integer  interval;
    /**
    
    */
    protected Date  createTime;
    /**
    
    */
    protected Date  updateTime;

    public void setId(Long id){
        this.id = id;
    }
    public Long getId (){
        return this.id;
    }
    public void setCounterId(Integer counterId){
        this.counterId = counterId;
    }
    public Integer getCounterId (){
        return this.counterId;
    }
    public void setValue(Long value){
        this.value = value;
    }
    public Long getValue (){
        return this.value;
    }
    public void setInterval(Integer interval){
        this.interval = interval;
    }
    public Integer getInterval (){
        return this.interval;
    }
    public void setCreateTime(Date createTime){
        this.createTime = createTime;
    }
    public Date getCreateTime (){
        return this.createTime;
    }
    public void setUpdateTime(Date updateTime){
        this.updateTime = updateTime;
    }
    public Date getUpdateTime (){
        return this.updateTime;
    }
}