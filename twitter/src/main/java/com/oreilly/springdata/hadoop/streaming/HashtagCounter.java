/*
 * Copyright 2011-2012 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.oreilly.springdata.hadoop.streaming;

import java.io.IOException;
import java.util.Properties;

import org.apache.hadoop.conf.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.data.hadoop.configuration.ConfigurationUtils;

import cascading.flow.Flow;
import cascading.flow.FlowConnector;
import cascading.flow.FlowConnectorProps;
import cascading.flow.hadoop.HadoopFlowConnector;
import cascading.operation.Aggregator;
import cascading.operation.DebugLevel;
import cascading.operation.Function;
import cascading.operation.aggregator.Count;
import cascading.operation.regex.RegexFilter;
import cascading.operation.regex.RegexParser;
import cascading.operation.regex.RegexSplitGenerator;
import cascading.pipe.Each;
import cascading.pipe.Every;
import cascading.pipe.GroupBy;
import cascading.pipe.Pipe;
import cascading.scheme.hadoop.TextLine;
import cascading.tap.SinkMode;
import cascading.tap.Tap;
import cascading.tap.hadoop.Hfs;
import cascading.tap.hadoop.Lfs;
import cascading.tuple.Fields;

/**
 * Hadoop job for counting words.
 * 
 * @author Costin Leau
 * @author Mark Fisher
 */
public class HashtagCounter implements Runnable {

	private String outputPath;

	private String inputPath;

	private Configuration hadoopConfiguration;


	public HashtagCounter(String input, Resource output, Configuration config) {
		this.inputPath = input;
		try {
			this.outputPath = output.getFile().getAbsolutePath();
		}
		catch (IOException e) {
			throw new IllegalArgumentException("failed to determine output path", e);
		}
		this.hadoopConfiguration = config;
	}

	@SuppressWarnings("rawtypes")
	public void run() {
		TextLine scheme = new TextLine(new Fields("line"));
		//Tap input = inputPath.matches("^[^:]+://.*") ? new Hfs(scheme, inputPath) : new Lfs(scheme, inputPath);
		Tap input = new Hfs(scheme, inputPath);

		// extract the tags through regex and save content in group 1 -> as fields tags
		String tagJsonRegex = "\"tags\":\\[([^\\]]*)";
		Function parse = new RegexParser(new Fields("tags"), tagJsonRegex, new int[] { 1 });
		// for each line get the tags using a regex
		Pipe assembly = new Each("import", new Fields("line"), parse, Fields.RESULTS);

		// split "tags" into "tag"
		Function split = new RegexSplitGenerator(new Fields("tag"), ",");
		assembly = new Each(assembly, new Fields("tags"), split);
		assembly = new Each(assembly, new Fields("tag"), new RegexFilter(".+"));
		// group each tag by name
		assembly = new GroupBy(assembly, new Fields("tag"));
		// count each tag under "count" field
		Aggregator count = new Count(new Fields("count"));
		assembly = new Every(assembly, count);

		// create a SINK tap to write to the default filesystem
		// by default, TextLine writes all fields out
		new TextLine(new Fields("tag", "count"));
		//		Tap output = outputPath.matches("^[^:]+://.*") ? new Hfs(sinkScheme, outputPath, SinkMode.REPLACE) : new Lfs(
		//				sinkScheme, outputPath, SinkMode.REPLACE);

		Tap output = new Lfs(scheme, outputPath, SinkMode.REPLACE);

		// wire the existing Hadoop config into HadoopFlow
		Properties properties = ConfigurationUtils.asProperties(hadoopConfiguration);

		FlowConnector flowConnector = new HadoopFlowConnector(properties);
		FlowConnectorProps.setDebugLevel(properties, DebugLevel.VERBOSE);
		Flow flow = flowConnector.connect("hashtagcount", input, output, assembly);

		flow.start();
		flow.complete();
	}

}
