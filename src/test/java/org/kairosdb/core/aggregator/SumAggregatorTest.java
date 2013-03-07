// KairosDB2
// Copyright (C) 2013 Proofpoint, Inc.
//
// This program is free software: you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 2.1 of the License, or (at your
// option) any later version.  This program is distributed in the hope that it
// will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
// of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
// General Public License for more details.  You should have received a copy
// of the GNU Lesser General Public License along with this program.  If not,
// see <http://www.gnu.org/licenses/>
package org.kairosdb.core.aggregator;

import org.kairosdb.core.DataPoint;
import org.kairosdb.core.datastore.DataPointGroup;
import org.kairosdb.testing.ListDataPointGroup;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class SumAggregatorTest
{
	private SumAggregator aggregator;

	@Before
	public void setup()
	{
		aggregator = new SumAggregator();
	}

	@Test(expected = NullPointerException.class)
	public void test_nullSet_invalid()
	{
		aggregator.createAggregatorGroup((List<DataPointGroup>) null);
	}

	@Test
	public void test_longValues()
	{
		ListDataPointGroup group = new ListDataPointGroup("group");
		group.addDataPoint(new DataPoint(1, 10));
		group.addDataPoint(new DataPoint(1, 20));
		group.addDataPoint(new DataPoint(1, 3));
		group.addDataPoint(new DataPoint(2, 1));
		group.addDataPoint(new DataPoint(2, 3));
		group.addDataPoint(new DataPoint(2, 5));
		group.addDataPoint(new DataPoint(3, 25));

		DataPointGroup results = aggregator.createAggregatorGroup(Collections.singletonList((DataPointGroup) group));

		assertThat(results.hasNext(), equalTo(true));
		DataPoint dataPoint = results.next();
		assertThat(dataPoint.getTimestamp(), equalTo(1L));
		assertThat(dataPoint.getLongValue(), equalTo(33L));

		assertThat(results.hasNext(), equalTo(true));
		dataPoint = results.next();
		assertThat(dataPoint.getTimestamp(), equalTo(2L));
		assertThat(dataPoint.getLongValue(), equalTo(9L));

		assertThat(results.hasNext(), equalTo(true));
		dataPoint = results.next();
		assertThat(dataPoint.getTimestamp(), equalTo(3L));
		assertThat(dataPoint.getLongValue(), equalTo(25L));

		assertThat(results.hasNext(), equalTo(false));
	}

	@Test
	public void test_doubleValues()
	{
		ListDataPointGroup group = new ListDataPointGroup("group");
		group.addDataPoint(new DataPoint(1, 10.0));
		group.addDataPoint(new DataPoint(1, 20.3));
		group.addDataPoint(new DataPoint(1, 3.0));
		group.addDataPoint(new DataPoint(2, 1.0));
		group.addDataPoint(new DataPoint(2, 3.2));
		group.addDataPoint(new DataPoint(2, 5.0));
		group.addDataPoint(new DataPoint(3, 25.1));

		DataPointGroup results = aggregator.createAggregatorGroup(Collections.singletonList((DataPointGroup) group));

		DataPoint dataPoint = results.next();
		assertThat(dataPoint.getTimestamp(), equalTo(1L));
		assertThat(dataPoint.getDoubleValue(), equalTo(33.3));

		dataPoint = results.next();
		assertThat(dataPoint.getTimestamp(), equalTo(2L));
		assertThat(dataPoint.getDoubleValue(), equalTo(9.2));

		dataPoint = results.next();
		assertThat(dataPoint.getTimestamp(), equalTo(3L));
		assertThat(dataPoint.getDoubleValue(), equalTo(25.1));

		assertThat(results.hasNext(), equalTo(false));
	}

	@Test
	public void test_mixedTypeValues()
	{
		ListDataPointGroup group = new ListDataPointGroup("group");
		group.addDataPoint(new DataPoint(1, 10.0));
		group.addDataPoint(new DataPoint(1, 20.3));
		group.addDataPoint(new DataPoint(1, 3));
		group.addDataPoint(new DataPoint(2, 1));
		group.addDataPoint(new DataPoint(2, 3.2));
		group.addDataPoint(new DataPoint(2, 5.0));
		group.addDataPoint(new DataPoint(3, 25.1));

		DataPointGroup results = aggregator.createAggregatorGroup(Collections.singletonList((DataPointGroup)group));

		assertThat(results.hasNext(), equalTo(true));
		DataPoint dataPoint = results.next();
		assertThat(dataPoint.getTimestamp(), equalTo(1L));
		assertThat(dataPoint.getDoubleValue(), equalTo(33.3));

		assertThat(results.hasNext(), equalTo(true));
		dataPoint = results.next();
		assertThat(dataPoint.getTimestamp(), equalTo(2L));
		assertThat(dataPoint.getDoubleValue(), equalTo(9.2));

		assertThat(results.hasNext(), equalTo(true));
		dataPoint = results.next();
		assertThat(dataPoint.getTimestamp(), equalTo(3L));
		assertThat(dataPoint.getDoubleValue(), equalTo(25.1));

		assertThat(results.hasNext(), equalTo(false));
	}

	@Test
	public void test_noValues()
	{
		ListDataPointGroup group = new ListDataPointGroup("group");

		DataPointGroup results = aggregator.createAggregatorGroup(Collections.singletonList((DataPointGroup) group));

		assertThat(results.hasNext(), equalTo(false));
	}


	@Test
	public void test_multipleGroups()
	{
		long time = System.currentTimeMillis();
		ListDataPointGroup group = new ListDataPointGroup("group");
		group.addDataPoint(new DataPoint(time+1, 10));
		group.addDataPoint(new DataPoint(time+1, 3));
		group.addDataPoint(new DataPoint(time+2, 1));
		group.addDataPoint(new DataPoint(time+2, 5));
		group.addDataPoint(new DataPoint(time+3, 25));

		ListDataPointGroup group2 = new ListDataPointGroup("group");

		group2.addDataPoint(new DataPoint(time+1, 20));
		group2.addDataPoint(new DataPoint(time+2, 3));

		List<DataPointGroup> groups = new ArrayList<DataPointGroup>();
		groups.add(group);
		groups.add(group2);

		DataPointGroup results = aggregator.createAggregatorGroup(groups);

		assertThat(results.hasNext(), equalTo(true));
		DataPoint dataPoint = results.next();
		assertThat(dataPoint.getTimestamp(), equalTo(time+1L));
		assertThat(dataPoint.getLongValue(), equalTo(33L));

		assertThat(results.hasNext(), equalTo(true));
		dataPoint = results.next();
		assertThat(dataPoint.getTimestamp(), equalTo(time+2L));
		assertThat(dataPoint.getLongValue(), equalTo(9L));

		assertThat(results.hasNext(), equalTo(true));
		dataPoint = results.next();
		assertThat(dataPoint.getTimestamp(), equalTo(time+3L));
		assertThat(dataPoint.getLongValue(), equalTo(25L));

		assertThat(results.hasNext(), equalTo(false));
	}
}