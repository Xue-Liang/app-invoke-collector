package com.gos.monitor.server.manager.service;
import java.util.List;
import com.gos.monitor.server.manager.dao.SqlBuilder;
import com.gos.monitor.server.manager.entity.App;
/**


@author Robot.Xue on 2017-05-22 18:14:18
*/
public interface AppService{

    public void insert(App  entity);

    public int update(App entity);

    public int delete(  Integer id);

    public App getApp(  Integer id);

    public List<App> query(SqlBuilder builder);
}