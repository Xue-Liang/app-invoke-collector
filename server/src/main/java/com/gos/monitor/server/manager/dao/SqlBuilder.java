package com.gos.monitor.server.manager.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.LinkedList;

/**
 * @author Robot.Xue on 2017-05-25 17:41:41
 */

public interface SqlBuilder {
    /**
     * @param table  table name
     * @param fs     field list
     * @param values sql parameter values
     */
    public SqlBuilder insert(String table, String[] fs, Object[] values);

    public SqlBuilder select(String... fs);

    public SqlBuilder delete();

    public SqlBuilder update(String table);

    public SqlBuilder set(String field, Object value);

    public SqlBuilder from(String table);

    public SqlBuilder where(String field);

    public SqlBuilder and(String field);

    public SqlBuilder or(String field);

    public SqlBuilder eq(Object value);

    public SqlBuilder neq(Object value);

    public SqlBuilder gt(Object value);

    public SqlBuilder gte(Object value);

    public SqlBuilder lt(Object value);

    public SqlBuilder lte(Object value);

    public SqlBuilder like(String key);

    public SqlBuilder in(Collection<Object> vs);

    public SqlBuilder in(SqlBuilder sqlBuilder);

    /**
     * not in creiteria
     *
     * @param values
     * @return
     */
    public SqlBuilder nin(Collection<Object> values);

    /**
     * order by <field> asc
     *
     * @param field
     * @return
     */
    public SqlBuilder orderByAsc(String field);

    /**
     * order by <field> desc
     *
     * @param field
     * @return
     */
    public SqlBuilder orderByDesc(String field);

    public SqlBuilder orderBy(String field, String order);

    public SqlBuilder exists(SqlBuilder builder);

    public SqlBuilder skip(int skip);

    public SqlBuilder limit(int limit);

    public String toSql();

    public List<Object> getParameters();

    public void clear();

    public static class MySqlBuilder implements SqlBuilder {
        protected List<String> buff = new LinkedList<String>();
        protected List<Object> values = new LinkedList<>();
        protected int skip = 0;
        protected int limit = 0;
        protected int capacity = 0;
        private static final String comma = ",";
        private static final String qm = "?";
        private static final String lb = "(";
        private static final String rb = ")";
        private static final String space = " ";

        private MySqlBuilder() {

        }

        private MySqlBuilder put(String str) {
            this.buff.add(str);
            this.buff.add(space);
            this.capacity += str.length() + space.length();
            return this;
        }

        public static MySqlBuilder create() {
            return new MySqlBuilder();
        }

        private static final String insert = "insert into";

        @Override
        public SqlBuilder insert(String table, String[] fs, Object[] vs) {
            if (fs == null || fs.length < 1) {
                throw new IllegalArgumentException("fields name is null or empty,please set field names!");
            } else if (vs == null || vs.length < 1) {
                throw new IllegalArgumentException("values is null or empty,please set values");
            } else if (fs.length != vs.length) {
                throw new IllegalArgumentException("fields length must equals the values length");
            }
            this.put(insert).put(table).put(lb);

            int i = 0;
            for (; i < fs.length - 1; i++) {
                this.put(fs[i]).put(comma);
            }
            if (i == fs.length - 1) {
                this.put(fs[i]);
            }
            this.put(rb).put("values").put(lb);
            i = 0;
            for (; i < fs.length - 1; i++) {
                this.put(qm).put(comma);
                this.values.add(vs[i]);
            }
            if (i == fs.length - 1) {
                this.put(qm);
                this.values.add(vs[i]);
            }
            this.put(rb);
            return this;
        }

        private static final String del = "delete ";

        @Override
        public SqlBuilder delete() {
            this.put(del);
            return this;
        }

        private static final String update = "update";

        @Override
        public SqlBuilder update(String table) {
            if (table == null || (table = table.trim()).length() < 1) {
                throw new IllegalArgumentException("table name can not be null or empty,please set the table name!");
            }
            return this.put(update).put(table);
        }

        private static final String set = "set";

        @Override
        public SqlBuilder set(String field, Object value) {
            if (field == null || (field = field.trim()).length() < 1) {
                throw new IllegalArgumentException(" field can not be null or empty.");
            }
            if (values.size() < 1) {
                this.put(set);
            } else {
                this.put(comma);
            }
            return this.put(field).eq(value);
        }

        private static final String from = "from";

        @Override
        public SqlBuilder from(String table) {
            return this.put(from).put(table);
        }

        private static final String where = "where";

        @Override
        public SqlBuilder where(String exp) {
            this.put(where);
            if (exp != null) {
                this.put(exp);
            }
            return this;
        }

        private static final String select = "select";

        @Override
        public SqlBuilder select(String... fs) {
            this.put(select);
            if (fs == null || fs.length < 1) {
                return this;
            }
            int ix = 0;
            for (String f : fs) {
                this.put(f);
                if (ix < fs.length - 1) {
                    this.put(comma);
                }
                ix++;
            }
            if (ix < fs.length) {
                this.put(fs[ix]);
            }
            return this;
        }

        private static final String and = "and";

        @Override
        public SqlBuilder and(String field) {
            return this.put(and).put(field);
        }

        private static final String or = "or";

        @Override
        public SqlBuilder or(String field) {
            return this.put(or).put(field);
        }

        private static final String eq = "=?";

        @Override
        public SqlBuilder eq(Object value) {
            this.values.add(value);
            return this.put(eq);
        }

        private static final String neq = "<>?";

        @Override
        public SqlBuilder neq(Object value) {
            this.values.add(value);
            return this.put(neq);
        }

        private static final String gt = ">?";

        @Override
        public SqlBuilder gt(Object value) {
            return this.put(gt);
        }

        private static final String gte = ">=?";

        @Override
        public SqlBuilder gte(Object value) {
            return this.put(gte);
        }

        private static final String lt = "<?";

        @Override
        public SqlBuilder lt(Object value) {
            if (values == null) {
                values = new ArrayList<Object>();
            }
            return this.put(lt);
        }

        private static final String lte = "<=?";

        @Override
        public SqlBuilder lte(Object value) {
            return this.put(lte);
        }

        private static final String like = "like ?";

        @Override
        public SqlBuilder like(String key) {
            this.values.add(key);
            return this.put(like);
        }

        private static final String in = "in";

        @Override
        public SqlBuilder in(Collection<Object> vs) {
            if (vs == null) {
                return this;
            }
            this.put(in).put(lb);
            java.util.Iterator<Object> it = vs.iterator();
            int pointer = 0;
            while (it.hasNext()) {
                Object obj = it.next();
                this.put(qm);
                pointer++;
                if (pointer < vs.size()) {
                    this.put(comma);
                }
                this.values.add(obj);
            }
            return this.put(rb);
        }

        @Override
        public SqlBuilder in(SqlBuilder builder) {
            return this.put(in).put(lb).put(builder.toSql()).put(rb);
        }

        private static final String nin = "not in";

        @Override
        public SqlBuilder nin(Collection<Object> vs) {
            if (vs == null) {
                return this;
            }
            this.put(nin).put(lb);
            java.util.Iterator<Object> it = vs.iterator();
            int pointer = 0;
            while (it.hasNext()) {
                Object obj = it.next();
                this.put(qm);
                pointer++;
                if (pointer < vs.size()) {
                    this.put(comma);
                }
                this.values.add(obj);
            }
            return this.put(rb);
        }

        @Override
        public SqlBuilder exists(SqlBuilder builder) {
            return this.put(" exists").put(lb).put(builder.toSql()).put(rb);
        }

        private static final String asc = "asc";

        @Override
        public SqlBuilder orderByAsc(String field) {
            return this.orderBy(field, asc);
        }

        private static final String desc = "desc";

        @Override
        public SqlBuilder orderByDesc(String field) {
            return this.orderBy(field, desc);
        }

        private static final String orderBy = "order by";

        @Override
        public SqlBuilder orderBy(String field, String order) {
            return this.put(orderBy).put(field).put(order);
        }

        @Override
        public SqlBuilder skip(int skip) {
            this.skip = skip;
            return this;
        }

        @Override
        public SqlBuilder limit(int limit) {
            this.limit = limit;
            return this;
        }


        @Override
        public List<Object> getParameters() {
            return this.values;
        }

        @Override
        public String toSql() {
            StringBuilder cup = new StringBuilder(this.capacity);
            int i = 0;
            for (String t : this.buff) {
                cup.append(t);
            }
            if (limit > 0) {
                cup.append(" limit ");
                if (skip >= 0) {
                    cup.append(Integer.toString(skip));
                    cup.append(",");
                }
                cup.append(Integer.toString(limit));
            }
            return cup.toString();
        }

        @Override
        public String toString() {
            return this.toSql();
        }

        @Override
        public void clear() {
            this.buff.clear();
            this.values.clear();
            this.skip = 0;
            this.limit = 0;
            this.capacity = 0;
        }
    }
}