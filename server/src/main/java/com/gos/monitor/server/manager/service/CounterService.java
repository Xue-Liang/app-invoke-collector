package com.gos.monitor.server.manager.service;
import java.util.List;
import com.gos.monitor.server.manager.dao.SqlBuilder;
import com.gos.monitor.server.manager.entity.Counter;
/**


@author Robot.Xue on 2017-05-25 17:41:41
*/
public interface CounterService{

    public void insert(Counter entity);

    public int update(Counter entity);

    public int delete(Integer id);

    public Counter getCounter(Integer id);

    public List<Counter> query(SqlBuilder builder);
}