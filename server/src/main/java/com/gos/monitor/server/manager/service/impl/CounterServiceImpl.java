package com.gos.monitor.server.manager.service.impl;
import java.util.List;
import javax.annotation.Resource;
import com.gos.monitor.server.manager.entity.Counter;
import com.gos.monitor.server.manager.dao.CounterDao;
import com.gos.monitor.server.manager.service.CounterService;
import com.gos.monitor.server.manager.dao.SqlBuilder;
import org.springframework.stereotype.Service;
/**


@author Robot.Xue on 2017-05-22 18:14:18
*/
    @Service
    public class CounterServiceImpl implements CounterService{
    @Resource
    private CounterDao counterDao;
    @Override
    public void insert(Counter  entity){
        this.counterDao.insert(entity);
    }
    @Override
    public int update(Counter entity){
        return this.counterDao.update(entity);
    }
    @Override
    public int delete(  Integer id){
        return this.counterDao.delete(  id);
    }
    @Override
    public Counter getCounter(  Integer id){
        return this.counterDao.queryFirst(  id);
    }
    @Override
    public List<Counter> query(SqlBuilder builder){
        return this.counterDao.query(builder);
    }
}