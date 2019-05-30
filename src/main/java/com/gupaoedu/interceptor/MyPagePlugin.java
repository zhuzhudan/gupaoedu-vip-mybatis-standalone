package com.gupaoedu.interceptor;


import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.lang.reflect.Field;
import java.util.Properties;

@Intercepts({@Signature(
        type = Executor.class,
        method = "query",
        args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})})
public class MyPagePlugin implements Interceptor {
    // 用于覆盖被拦截对象的原有方法（在调用代理对象Plugin 的invoke()方法时被调用）
    public Object intercept(Invocation invocation) throws Throwable {
        System.out.println("将逻辑分页改为物理分页");
        Object[] args = invocation.getArgs();
        MappedStatement statement = (MappedStatement) args[0];
        BoundSql boundSql = statement.getBoundSql(args[1]);
        RowBounds rowBounds = (RowBounds) args[2];

        if (rowBounds == RowBounds.DEFAULT) {
            return invocation.proceed();
        }

        String sql = boundSql.getSql();
        String limit = String.format(" LIMIT %d, %d ", rowBounds.getOffset(), rowBounds.getLimit());
        sql += limit;

        SqlSource sqlSource = new StaticSqlSource(statement.getConfiguration(), sql, boundSql.getParameterMappings());

        Field field = MappedStatement.class.getDeclaredField("sqlSource");
        field.setAccessible(true);
        field.set(statement, sqlSource);

        return invocation.proceed();
    }

    // o 是被拦截对象，这个方法的作用是给被拦截对象生成一个代理对象，并返回它
    public Object plugin(Object o) {
        return Plugin.wrap(o, this);
    }

    // 设置参数
    public void setProperties(Properties properties) {

    }
}
