package com.gos.monitor.server.manager.service.impl;
import java.util.List;
import javax.annotation.Resource;
import com.gos.monitor.server.manager.entity.Graph;
import com.gos.monitor.server.manager.dao.GraphDao;
import com.gos.monitor.server.manager.service.GraphService;
import com.gos.monitor.server.manager.dao.SqlBuilder;
import org.springframework.stereotype.Service;
/**


@author Robot.Xue on 2017-05-25 17:41:41
*/
    @Service
    public class GraphServiceImpl implements GraphService{
    @Resource
    private GraphDao graphDao;
    @Override
    public void insert(Graph  entity){
        this.graphDao.insert(entity);
    }
    @Override
    public int update(Graph entity){
        return this.graphDao.update(entity);
    }
    @Override
    public int delete(  Long id){
        return this.graphDao.delete(  id);
    }
    @Override
    public Graph getGraph(  Long id){
        return this.graphDao.queryFirst(  id);
    }
    @Override
    public List<Graph> query(SqlBuilder builder){
        return this.graphDao.query(builder);
    }
}