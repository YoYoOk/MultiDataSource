package com.yj.multids.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yj.multids.Service.TestService;
import com.yj.multids.entity.User;


@RestController
public class TestController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TestController.class);
	
	@Autowired
	private TestService testService;
	
	@RequestMapping("/test")
	public String test(){
		return "OK";
	}
	
	@RequestMapping("/list")
	public List<User> getList(){
		return testService.getList();
	}
	
}
