package com.gos.monitor.server.manager.entity;


import java.io.Serializable;
import java.util.Date;


/**


@author Robot.Xue on 2017-05-22 18:14:18
*/
public class Hosts implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
    
    */
    protected Integer  id;
    /**
    
    */
    protected Integer  appId;
    /**
    应用程序所在机器的ip地址
    */
    protected String  host;
    /**
    监控插件监听的端口
    */
    protected Integer  port;
    /**
    0:新应用 1:正常
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
    public void setAppId(Integer appId){
        this.appId = appId;
    }
    public Integer getAppId (){
        return this.appId;
    }
    public void setHost(String host){
        this.host = host;
    }
    public String getHost (){
        return this.host;
    }
    public void setPort(Integer port){
        this.port = port;
    }
    public Integer getPort (){
        return this.port;
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