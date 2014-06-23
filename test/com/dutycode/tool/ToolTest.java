package com.dutycode.tool;

import org.junit.Test;

import junit.framework.TestCase;

public class ToolTest extends TestCase {
	
	@Test
	public void testIsCorrectIpFunction(){
		System.out.println("---" + Tools.isCorrectIp("192.168.206.1"));
	}
}
