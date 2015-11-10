package org.modelio.juniper.data.partitioning;

import java.util.ArrayList;
import java.util.Collection;

public interface DataPartitioner {
	@SuppressWarnings("rawtypes")
	ArrayList<Collection> partition(int nbPartitions, Collection data);
}
