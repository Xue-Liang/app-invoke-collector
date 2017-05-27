package com.gos.monitor.server.manager.service.impl;
import java.util.List;
import javax.annotation.Resource;
import com.gos.monitor.server.manager.entity.Hosts;
import com.gos.monitor.server.manager.dao.HostsDao;
import com.gos.monitor.server.manager.service.HostsService;
import com.gos.monitor.server.manager.dao.SqlBuilder;
import org.springframework.stereotype.Service;
/**


@author Robot.Xue on 2017-05-25 17:41:41
*/
    @Service
    public class HostsServiceImpl implements HostsService{
    @Resource
    private HostsDao hostsDao;
    @Override
    public void insert(Hosts  entity){
        this.hostsDao.insert(entity);
    }
    @Override
    public int update(Hosts entity){
        return this.hostsDao.update(entity);
    }
    @Override
    public int delete(  Integer id){
        return this.hostsDao.delete(  id);
    }
    @Override
    public Hosts getHosts(  Integer id){
        return this.hostsDao.queryFirst(  id);
    }
    @Override
    public List<Hosts> query(SqlBuilder builder){
        return this.hostsDao.query(builder);
    }
}