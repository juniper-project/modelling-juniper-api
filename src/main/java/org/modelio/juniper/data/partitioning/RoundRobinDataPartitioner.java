package org.modelio.juniper.data.partitioning;

import java.util.ArrayList;
import java.util.Collection;

public class RoundRobinDataPartitioner implements DataPartitioner {
			
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ArrayList<Collection> partition(int nbPartitions,
			Collection data) {
		try {
			ArrayList<Collection> ret = new ArrayList<Collection>(nbPartitions);
			for (int i = 0; i < nbPartitions; ++i) {
				// creates an empty collection of the same type on each slot
				// hopes it has a no args constructor
				ret.add(data.getClass().newInstance());
			}

			int i = 0;
			for (Object o : data) {
				ret.get(i % nbPartitions).add(o);
				i++;
			}
			return ret;
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

}
