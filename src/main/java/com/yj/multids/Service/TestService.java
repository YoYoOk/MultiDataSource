package com.yj.multids.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import com.yj.multids.entity.User;

@Service
public class TestService {
	
	@Autowired
    private JdbcTemplate jdbcTemplate;

    public List<User> getList(){
        String sql = "select *from user";
        return (List<User>) jdbcTemplate.query(sql, new RowMapper<User>(){
            @Override
            public User mapRow(ResultSet rs, int rowNum) throws SQLException {
                User demo = new User();
                demo.setId(rs.getLong("id"));
                demo.setName(rs.getString("name"));;
                return demo;
            }
        });
    }
	
}
