package org.modelio.juniper.data.partitioning;

import java.util.ArrayList;
import java.util.Collection;

public class BroadcastDataPartitioner implements DataPartitioner {

	@Override
	public ArrayList<Collection> partition(int nbPartitions, Collection data) {
		ArrayList<Collection> ret = new ArrayList<Collection>(nbPartitions);
		for(int i=0;i<nbPartitions;++i) {
			ret.add(data);
		}
		return ret;
	}

}
