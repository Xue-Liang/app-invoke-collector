package com.gos.monitor.server.manager.service.impl;
import java.util.List;
import javax.annotation.Resource;
import com.gos.monitor.server.manager.entity.App;
import com.gos.monitor.server.manager.dao.AppDao;
import com.gos.monitor.server.manager.service.AppService;
import com.gos.monitor.server.manager.dao.SqlBuilder;
import org.springframework.stereotype.Service;
/**


@author Robot.Xue on 2017-05-25 17:41:41
*/
    @Service
    public class AppServiceImpl implements AppService{
    @Resource
    private AppDao appDao;
    @Override
    public void insert(App  entity){
        this.appDao.insert(entity);
    }
    @Override
    public int update(App entity){
        return this.appDao.update(entity);
    }
    @Override
    public int delete(  Integer id){
        return this.appDao.delete(  id);
    }
    @Override
    public App getApp(  Integer id){
        return this.appDao.queryFirst(  id);
    }
    @Override
    public List<App> query(SqlBuilder builder){
        return this.appDao.query(builder);
    }
}