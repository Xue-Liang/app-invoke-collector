package com.gos.monitor.server.manager.entity;


import java.io.Serializable;
import java.util.Date;


/**


@author Robot.Xue on 2017-05-22 18:14:18
*/
public class Counter implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
    
    */
    protected Integer  id;
    /**
    
    */
    protected Integer  hostId;
    /**
    
    */
    protected String  counter;
    /**
    
    */
    protected Date  createTime;
    /**
    
    */
    protected Date  updateTime;

    public void setId(Integer id){
        this.id = id;
    }
    public Integer getId (){
        return this.id;
    }
    public void setHostId(Integer hostId){
        this.hostId = hostId;
    }
    public Integer getHostId (){
        return this.hostId;
    }
    public void setCounter(String counter){
        this.counter = counter;
    }
    public String getCounter (){
        return this.counter;
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