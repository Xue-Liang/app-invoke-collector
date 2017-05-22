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
import com.gos.monitor.server.manager.entity.Graph;
import com.gos.monitor.server.manager.dao.SqlBuilder.MySqlBuilder;
/**
基本的增、删、改查工具类,数据表结构发生变化时,直接用代码生成工具成成代码文件，替换此类。
一些业务方面的查询、更新功能可以在子类中修改扩展实现。
@author Xue Liang  on 2017-05-22 18:14:18
*/

public abstract class AbstractGraphDao{
    @Resource
    private JdbcTemplate jdbcTemplate;

    private static final RowMapper<Graph> mapper = new BeanPropertyRowMapper<Graph>(Graph.class);

    private static final ResultSetExtractor<Graph > extractor = new ResultSetExtractor<Graph>() {
        @Override
        public Graph extractData(ResultSet rs) throws SQLException, DataAccessException {
            Graph row = new Graph();
                        row.setId(rs.getLong("id"));
                        row.setCounterId(rs.getInteger("counter_id"));
                        row.setValue(rs.getLong("value"));
                        row.setSince(rs.getDate("since"));
                        row.setCreateTime(rs.getDate("create_time"));
                        row.setUpdateTime(rs.getDate("update_time"));
                        return row;
        }
    };

    private static final String insert = "insert into graph (counter_id,value,since,create_time,update_time)values(?,?,?,?,?)";
   /**
    插入一条数据
   @author Xue Liang  on 2017-05-22 18:14:18
   */
    public void insert(Graph  entity){
        final Object[] values = new Object[] {
                                                         entity.getCounterId()
                                        , entity.getValue()
                                        , entity.getSince()
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
    @author Xue Liang  on 2017-05-22 18:14:18
    */
    public int update(Graph entity){
        SqlBuilder sql = MySqlBuilder.create().update("graph");
                                                                                
        sql.set("counter_id",entity.getCounterId());

                                                                
        sql.set("value",entity.getValue());

                                                                
        sql.set("since",entity.getSince());

                                                                
        sql.set("create_time",entity.getCreateTime());

                                                                
        sql.set("update_time",entity.getUpdateTime());

                        
        sql.where(" 1=1 ");

                
        sql.and("id").eq(entity.getId());

        
        return this.jdbcTemplate.update(sql.toSql(),sql.getParameters().toArray());
    }

    private static final String delete="delete from graph where  id=? ";
    /*
    删除一条数据
    @author Xue Liang  on 2017-05-22 18:14:18
    */
    public int delete(  Long id){
        return this.jdbcTemplate.update(delete,  id);
    }

        private static final String queryFirst ="select id,counter_id,value,since,create_time,update_time from graph  where  id=? ";
    /**
    根据主键查询一条数据
    @author Xue Liang  on 2017-05-22 18:14:18
    */
    public Graph queryFirst(  Long id){
        Object[]values = new Object[] {
                        
             id

                        
        };
        Graph entity = this.jdbcTemplate.query(queryFirst,values,extractor);
        return entity;
    }
    /**
    自定义的查询,根据sql查询满足条件的记录列表
    @author Xue Liang  on 2017-05-22 18:14:18
    */
   public List<Graph> query(SqlBuilder sqlBuilder){
        List<Graph> entities = this.jdbcTemplate.query(sqlBuilder.toSql(),sqlBuilder.getParameters().toArray(),mapper);
        return entities;
   }
   /**
       自定义的查询,根据sql查询满足条件的记录总数
       @author Xue Liang  on 2017-05-22 18:14:18
   */
   public int queryTotal(SqlBuilder sqlBuilder){
           return this.jdbcTemplate.queryForObject(sqlBuilder.toSql(),sqlBuilder.getParameters().toArray(),Integer.class);
   }
}