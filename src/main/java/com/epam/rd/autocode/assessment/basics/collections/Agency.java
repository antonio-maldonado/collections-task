package com.epam.rd.autocode.assessment.basics.collections;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import com.epam.rd.autocode.assessment.basics.entity.BodyType;
import com.epam.rd.autocode.assessment.basics.entity.Client;
import com.epam.rd.autocode.assessment.basics.entity.Order;
import com.epam.rd.autocode.assessment.basics.entity.Vehicle;

public class Agency implements Find, Sort, Serializable {

	private List<Vehicle> vehicles;

	private List<Order> orders;

	public Agency() {
		this.vehicles = new ArrayList<>();
		this.orders = new ArrayList<>();
	}

	public void addVehicle(Vehicle vehicle){
		this.vehicles.add(vehicle);
	}

	public void addOrder(Order order){
		this.orders.add(order);
	}

	@Override
	public List<Vehicle> sortByID() {
		if(this.vehicles.isEmpty()){
			throw new UnsupportedOperationException("Vehicles is Empty");
		}
		Collections.sort(this.vehicles, new Comparator<Vehicle>() {
			@Override
			public int compare(Vehicle v1, Vehicle v2) {
				return Long.compare(v1.getId(), v2.getId());
			}
		});
		return this.vehicles;

	}

	@Override
	public List<Vehicle> sortByYearOfProduction() {
		if(this.vehicles.isEmpty()){
			throw new UnsupportedOperationException("Vehicles is Empty");
		}
		Collections.sort(this.vehicles, new Comparator<Vehicle>() {
			@Override
			public int compare(Vehicle v1, Vehicle v2) {
				return Integer.compare(v1.getYearOfProduction(), v2.getYearOfProduction());
			}
		});
		return this.vehicles;
	}

	@Override
	public List<Vehicle> sortByOdometer() {
		if(this.vehicles.isEmpty()){
			throw new UnsupportedOperationException("Vehicles is Empty");
		}

		Collections.sort(this.vehicles, new Comparator<Vehicle>() {
			@Override
			public int compare(Vehicle v1, Vehicle v2) {
				return Long.compare(v1.getOdometer(), v2.getOdometer());
			}
		});
		return this.vehicles;
	}

	@Override
	public Set<String> findMakers() {
		if(this.vehicles.isEmpty()){
			throw new UnsupportedOperationException("Vehicles is Empty");
		}

		Set<String> makersList = new HashSet<>();

		for(Vehicle vehicle : this.vehicles){
			makersList.add(vehicle.getMake()) ;
		}

		return makersList;
	}

	@Override
	public Set<BodyType> findBodytypes() {
		if(this.vehicles.isEmpty()){
			throw new UnsupportedOperationException("Vehicles is Empty");
		}

		Set<BodyType> bodyTypesList = new HashSet<>();

		for(Vehicle vehicle : this.vehicles){
			bodyTypesList.add(vehicle.getBodyType()) ;
		}

		return bodyTypesList;
	}

	@Override
	public Map<String, List<Vehicle>> findVehicleGrouppedByMake() {
		if(this.vehicles.isEmpty()){
			throw new UnsupportedOperationException("Vehicles is Empty");
		}

		Map<String,List<Vehicle>> vehiclesByMaker = new HashMap<>();
		List<Vehicle> vehicleList = new ArrayList<>();

		for(Vehicle vehicle : this.vehicles){
			vehicleList.clear();

			if(vehiclesByMaker.containsKey(vehicle.getMake())){
				vehicleList = vehiclesByMaker.get(vehicle.getMake());
			}

			vehicleList.add(vehicle);
			vehiclesByMaker.put(vehicle.getMake(),vehicleList);
		}

		return vehiclesByMaker;
	}

	@Override
	public List<Client> findTopClientsByPrices(List<Client> clients, int maxCount) {
		if(clients.isEmpty()){
			throw new UnsupportedOperationException("Vehicles is Empty");
		}

		List<Client> topClients = new ArrayList<>();
		Collections.sort(clients, new Comparator<Client>() {
			@Override
			public int compare(Client c1,  Client c2) {
				return c2.getBalance().compareTo(c1.getBalance());
			}
		});

		topClients = clients.subList(0,Math.min(maxCount,clients.size()));

		return topClients;
	}

	@Override
	public List<Client> findClientsWithAveragePriceNoLessThan(List<Client> clients, int average) {
		if(clients.isEmpty()){
			throw new UnsupportedOperationException("Vehicles is Empty");
		}

		Map<Long,Integer> clientsOrders = new HashMap<>();
		Map<Long,BigDecimal> clientsAverage = new HashMap<>();
		int counter = 0;

		for(Order order : this.orders){
			counter = 0;
			if(clientsOrders.containsKey(order.getClientId())){
				counter = clientsOrders.get(order.getClientId()) + 1;
			}

			clientsOrders.put(order.getClientId(),counter);
		}

		BigDecimal aux = BigDecimal.valueOf(0);
		BigDecimal clientBalance = BigDecimal.valueOf(0);
		Client clientFound = null;

		for(Map.Entry<Long,Integer> clientsOrder : clientsOrders.entrySet()){
			clientFound = null;
			for(Client client : clients){
				if(client.getId() == clientsOrder.getKey()) {
					clientFound = client;

				}
			}
			clientBalance = clientFound.getBalance();
			aux = clientBalance.divide( BigDecimal.valueOf(clientsOrder.getValue()),2, RoundingMode.HALF_UP);

			if(aux.compareTo(BigDecimal.valueOf(average))>0){
				clientsAverage.put(clientsOrder.getKey(),aux);
			}
		}

		List<Map.Entry<Long,BigDecimal>> topClients = new ArrayList<>(clientsAverage.entrySet());
		Collections.sort(topClients, new Comparator<Map.Entry<Long,BigDecimal>>() {
			@Override
			public int compare(Map.Entry<Long,BigDecimal> c1, Map.Entry<Long, BigDecimal> c2) {
				return c2.getValue().compareTo(c1.getValue());
			}
		});

		List<Client> clientsFound = new ArrayList<>();

		for(Map.Entry<Long,BigDecimal> topClient : topClients){
			for(Client client : clients){
				if(client.getId() == topClient.getKey()) {
					clientsFound.add(client);
					break;
				}
			}
		}

		return clientsFound;
	}

	@Override
	public List<Vehicle> findMostOrderedVehicles(int maxCount) {
		if(this.vehicles.isEmpty()){
			throw new UnsupportedOperationException("Vehicles is Empty");
		}

		List<Vehicle> mostOrderedVehicles = new ArrayList<>(this.vehicles);

		Map<Long, Integer> vehiclesFrequency = new HashMap<>();
		for (Order order : orders) {
			long vehicleId = order.getVehicleId();
			vehiclesFrequency.put(vehicleId, vehiclesFrequency.getOrDefault(vehicleId, 0) + 1);
		}
		Collections.sort(mostOrderedVehicles, new Comparator<Vehicle>() {
			@Override
			public int compare(Vehicle v1, Vehicle v2) {
				int frequencyV1 = vehiclesFrequency.getOrDefault(v1.getId(), 0);
				int frequencyV2 = vehiclesFrequency.getOrDefault(v2.getId(), 0);
				return Integer.compare(frequencyV2, frequencyV1);
			}
		});

		mostOrderedVehicles = mostOrderedVehicles.subList(0,Math.min(maxCount,mostOrderedVehicles.size()));

		return mostOrderedVehicles;
	}

}