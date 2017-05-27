package com.gos.monitor.server.manager.dao;
                                          import java.util.List;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.annotation.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.dao.DataAccessException;
import java.sql.Statement;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.util.CollectionUtils;
import com.gos.monitor.server.manager.entity.App;
import com.gos.monitor.server.manager.dao.SqlBuilder.MySqlBuilder;
/**
基本的增、删、改查工具类,数据表结构发生变化时,直接用代码生成工具成成代码文件，替换此类。
一些业务方面的查询、更新功能可以在子类中修改扩展实现。
@author Xue Liang  on 2017-05-25 17:41:41
*/

public abstract class AbstractAppDao{
    @Resource
    protected JdbcTemplate jdbcTemplate;

    protected static final RowMapper<App> mapper = new BeanPropertyRowMapper<App>(App.class);

    protected static final ResultSetExtractor<App > extractor = new ResultSetExtractor<App>() {
        @Override
        public App extractData(ResultSet rs) throws SQLException, DataAccessException {
            App row = new App();
                        row.setId(rs.getInt("id"));
                        row.setName(rs.getString("name"));
                        row.setStatus(rs.getInt("status"));
                        row.setCreateTime(rs.getDate("create_time"));
                        row.setUpdateTime(rs.getDate("update_time"));
                        return row;
        }
    };

    private static final String insert = "insert into app (name,status,create_time,update_time)values(?,?,?,?)";
   /**
    插入一条数据
   @author Xue Liang  on 2017-05-25 17:41:41
   */
    public void insert(App  entity){
        final Object[] values = new Object[] {
                                                         entity.getName()
                                        , entity.getStatus()
                                        , entity.getCreateTime()
                                        , entity.getUpdateTime()
                        
        };

        PreparedStatementCreator creator = new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement ps = con.prepareStatement(insert,Statement.RETURN_GENERATED_KEYS);
                for (int i = 0; i < values.length; i++) {
                    ps.setObject(i + 1, values[i]);
                }
                return ps;
	        }
        };
   
        KeyHolder holder = new GeneratedKeyHolder();

        this.jdbcTemplate.update(creator,holder);

        entity.setId(holder.getKey().intValue());

   
    }
    /**
    修改一条数据
    @author Xue Liang  on 2017-05-25 17:41:41
    */
    public int update(App entity){
        SqlBuilder sql = MySqlBuilder.create().update("app");
                                                                                
        sql.set("name",entity.getName());

                                                                
        sql.set("status",entity.getStatus());

                                                                
        sql.set("create_time",entity.getCreateTime());

                                                                
        sql.set("update_time",entity.getUpdateTime());

                        
        sql.where(" 1=1 ");

                
        sql.and("id").eq(entity.getId());

        
        return this.jdbcTemplate.update(sql.toSql(),sql.getParameters().toArray());
    }

    private static final String delete="delete from app where  id=? ";
    /**
    根据主键删除一条数据
    @author Xue Liang  on 2017-05-25 17:41:41
    */
    public int delete(  Integer id){
        return this.jdbcTemplate.update(delete,  id);
    }

        private static final String queryFirst ="select id,name,status,create_time,update_time from app  where  id=? ";
    /**
    根据主键查询一条数据
    @author Xue Liang  on 2017-05-25 17:41:41
    */
    public App queryFirst(  Integer id){
        Object[]values = new Object[] {
                        
             id

                        
        };
        App entity = this.jdbcTemplate.query(queryFirst,values,extractor);
        return entity;
    }
    /**
    自定义的查询,根据sql查询满足条件的记录列表
    @author Xue Liang  on 2017-05-25 17:41:41
    */
   public List<App> query(SqlBuilder sqlBuilder){
        List<App> entities = this.jdbcTemplate.query(sqlBuilder.toSql(),sqlBuilder.getParameters().toArray(),mapper);
        return entities;
   }
   /**
       自定义的查询,根据sql查询满足条件的记录总数
       @author Xue Liang  on 2017-05-25 17:41:41
   */
   public int queryTotal(SqlBuilder sqlBuilder){
           return this.jdbcTemplate.queryForObject(sqlBuilder.toSql(),sqlBuilder.getParameters().toArray(),Integer.class);
   }
}