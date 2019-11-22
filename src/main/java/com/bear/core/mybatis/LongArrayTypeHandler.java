package com.bear.core.mybatis;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@MappedJdbcTypes(JdbcType.VARCHAR)
public class LongArrayTypeHandler extends BaseTypeHandler<Long[]> {

    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, Long[] strings, JdbcType jdbcType) throws SQLException {
        List<String> list = new ArrayList<>();
        for (long item : strings) {
            list.add(String.valueOf(item));
        }
        preparedStatement.setString(i, String.join(",", list));
    }

    @Override
    public Long[] getNullableResult(ResultSet resultSet, String s) throws SQLException {
        String str = resultSet.getString(s);
        if (resultSet.wasNull() || str == null || str.isEmpty()){
            return null;
        }
        return Arrays.stream(str.split(",")).map(ss -> Long.valueOf(ss)).collect(Collectors.toList()).toArray(new Long[]{});
    }

    @Override
    public Long[] getNullableResult(ResultSet resultSet, int i) throws SQLException {
        String str = resultSet.getString(i);
        if (resultSet.wasNull() || str == null || str.isEmpty()){
            return null;
        }
        return Arrays.stream(resultSet.getString(i).split(",")).map(ss -> Long.valueOf(ss)).collect(Collectors.toList()).toArray(new Long[]{});
    }

    @Override
    public Long[] getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        String str = callableStatement.getString(i);
        if (callableStatement.wasNull() || str == null || str.isEmpty()){
            return null;
        }
        return Arrays.stream(callableStatement.getString(i).split(",")).map(ss -> Long.valueOf(ss)).collect(Collectors.toList()).toArray(new Long[]{});
    }
}
