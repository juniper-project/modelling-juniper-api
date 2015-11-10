package org.modelio.juniper.data.partitioning;

import java.util.ArrayList;
import java.util.Collection;

public class RawDataPartitioner implements DataPartitioner {

	@Override
	public ArrayList<Collection> partition(int nbPartitions, Collection data) {
		try {
			ArrayList<Collection> ret = new ArrayList<Collection>(nbPartitions);
			int i = 0;
			for (Object obj : data) {
				// creates an empty collection of the same type on each slot
				// hopes it has a no args constructor
				Collection collection = data.getClass().newInstance();
				collection.add(obj);

				ret.add(collection);					

				i++;
				if (i==nbPartitions) break;
			}
			return ret;
		} catch (InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

}
