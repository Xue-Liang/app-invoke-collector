package com.gos.monitor.server.manager.entity;


import java.io.Serializable;
import java.util.Date;


/**


@author Robot.Xue on 2017-05-22 18:14:18
*/
public class App implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
    主键，项目id
    */
    protected Integer  id;
    /**
    应用程序名称
    */
    protected String  name;
    /**
    创建时间
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
    public void setName(String name){
        this.name = name;
    }
    public String getName (){
        return this.name;
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