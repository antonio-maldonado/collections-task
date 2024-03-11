package com.epam.rd.autocode.assessment.basics.collections;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.epam.rd.autocode.assessment.basics.entity.BodyType;
import com.epam.rd.autocode.assessment.basics.entity.Client;
import com.epam.rd.autocode.assessment.basics.entity.Vehicle;

public interface Find {

	Set<String> findMakers();

	Set<BodyType> findBodytypes();

	Map<String, List<Vehicle>> findVehicleGrouppedByMake();
	
	List<Client> findTopClientsByPrices(List<Client> clients, int maxCount);
	
	List<Client> findClientsWithAveragePriceNoLessThan(List<Client> clients, int average);
	
	List<Vehicle> findMostOrderedVehicles(int maxCount);

}
