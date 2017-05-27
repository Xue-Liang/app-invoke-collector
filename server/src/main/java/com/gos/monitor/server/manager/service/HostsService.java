package com.gos.monitor.server.manager.service;
import java.util.List;
import com.gos.monitor.server.manager.dao.SqlBuilder;
import com.gos.monitor.server.manager.entity.Hosts;
/**


@author Robot.Xue on 2017-05-25 17:41:41
*/
public interface HostsService{

    public void insert(Hosts entity);

    public int update(Hosts entity);

    public int delete(Integer id);

    public Hosts getHosts(Integer id);

    public List<Hosts> query(SqlBuilder builder);
}