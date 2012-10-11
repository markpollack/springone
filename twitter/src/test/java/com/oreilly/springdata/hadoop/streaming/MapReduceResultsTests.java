package com.oreilly.springdata.hadoop.streaming;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

public class MapReduceResultsTests {

	@Test
	public void test() {
		MapReduceResults mrr = new MapReduceResults();
		mrr.getResults("/home/mpollack/projects/springone/twitter/public/mrout/part-00000");
	}

}
