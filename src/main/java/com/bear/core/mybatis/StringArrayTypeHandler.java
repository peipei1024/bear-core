package com.bear.core.mybatis;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedJdbcTypes(JdbcType.VARCHAR)
public class StringArrayTypeHandler extends BaseTypeHandler<String[]> {

    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, String[] objects, JdbcType jdbcType) throws SQLException {
        preparedStatement.setString(i, String.join(",", objects));

    }

    @Override
    public String[] getNullableResult(ResultSet resultSet, String s) throws SQLException {
        if(resultSet.wasNull()){
            return null;
        }
        return resultSet.getString(s).split(",");
    }

    @Override
    public String[] getNullableResult(ResultSet resultSet, int i) throws SQLException {
        if(resultSet.wasNull()){
            return null;
        }
        return resultSet.getString(i).split(",");
    }

    @Override
    public String[] getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        if(callableStatement.wasNull()){
            return null;
        }
        return callableStatement.getString(i).split(",");
    }

}
