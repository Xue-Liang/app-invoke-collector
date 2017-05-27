package com.gos.monitor.server.manager.service;
import java.util.List;
import com.gos.monitor.server.manager.dao.SqlBuilder;
import com.gos.monitor.server.manager.entity.Graph;
/**


@author Robot.Xue on 2017-05-25 17:41:41
*/
public interface GraphService{

    public void insert(Graph entity);

    public int update(Graph entity);

    public int delete(Long id);

    public Graph getGraph(Long id);

    public List<Graph> query(SqlBuilder builder);
}