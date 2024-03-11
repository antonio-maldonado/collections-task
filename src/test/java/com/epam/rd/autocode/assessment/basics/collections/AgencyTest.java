package com.epam.rd.autocode.assessment.basics.collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.epam.rd.autocode.assessment.basics.entity.BodyType;
import com.epam.rd.autocode.assessment.basics.entity.Client;
import com.epam.rd.autocode.assessment.basics.entity.Order;
import com.epam.rd.autocode.assessment.basics.entity.Vehicle;
import com.opencsv.CSVReader;

import spoon.Launcher;

class AgencyTest {

	private static final boolean MAKE_ALL_TESTS_FAILED;

	private static Throwable t;

	static {
		L: {
			try {
				ComplianceTest ctest = new ComplianceTest();
				for (Method m : ComplianceTest.class.getDeclaredMethods()) {
					if (Modifier.isPrivate(m.getModifiers())) {
						continue;
					}
					Test[] ar = m.getAnnotationsByType(Test.class);
					if (ar.length > 0 && m.getAnnotationsByType(Test.class)[0] != null) {
						m.invoke(ctest);
					}
				}
			} catch (ReflectiveOperationException ex) {
				MAKE_ALL_TESTS_FAILED = true;
				t = ex.getCause();
				break L;
			}
			MAKE_ALL_TESTS_FAILED = false;
		}

	}

	{
		if (MAKE_ALL_TESTS_FAILED) {
			Assertions.fail("Compliance tests have not been passed", t);
		}
	}

	////////////////////////////////////////////////////////////////////////////

	private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd kk:mm");

	private static List<Vehicle> vehicles;
	private static List<Order> orders;
	private static List<Client> clients;

	@BeforeAll
	static void setUpGlobal() throws Exception {
		vehicles = obtainListFromCSV(Vehicle.class);
		orders = obtainListFromCSV(Order.class);
		clients = obtainListFromCSV(Client.class);

		final String[] args = {"-i", "src/main/java/"};
		final Launcher launcher = new Launcher();
		launcher.setArgs(args);
		launcher.buildModel();
	}

	private static Object toObject( Class<?> clazz, String value ) {
	    if( boolean.class == clazz ) return Boolean.parseBoolean( value );
	    if( byte.class == clazz ) return Byte.parseByte( value );
	    if( short.class == clazz ) return Short.parseShort( value );
	    if( int.class == clazz ) return Integer.parseInt( value );
	    if( long.class == clazz ) return Long.parseLong( value );
	    if( float.class == clazz ) return Float.parseFloat( value );
	    if( double.class == clazz ) return Double.parseDouble( value );
	    if( char.class == clazz ) return value.charAt(0);
	    return value;
	}

	@SuppressWarnings("unchecked")
	private static <T> List<T> obtainListFromCSV(Class<T> clazz) throws Exception {
		String name = clazz.getSimpleName().toLowerCase();
		List<T> list  = new ArrayList<>();
		Constructor<?> constr = Arrays.stream(clazz.getConstructors())
			.filter(c -> c.getParameterTypes().length > 0)
			.limit(1)
			.findFirst()
			.get();
		Class<?>[] types = constr.getParameterTypes();
		Path path = Paths.get("src/test/resources/" + name + ".csv");
		Reader reader = Files.newBufferedReader(path);
	    CSVReader csvReader =  new CSVReader(reader);
	    csvReader.skip(1);
	    List<String[]> lines = csvReader.readAll();
	    for (String[] f : lines) {
	    	Object[] args = new Object[types.length];
	    	for (int j = 0; j < types.length; j++) {
	    		if (types[j].isPrimitive()) {
	    			args[j] = toObject(types[j], f[j]);
	    		} else if (types[j].isEnum()) {
	    			args[j] = types[j].getMethod("valueOf", String.class).invoke(null, new Object[] {f[j]});
	    		} else if (types[j] == java.time.LocalDateTime.class) {
	    	        args[j] = LocalDateTime.parse(f[j], dateTimeFormatter);
	    		} else {
	    			args[j] = types[j].getConstructor(String.class).newInstance(f[j]);
	    		}
	    	}
	    	list.add((T)constr.newInstance(args));
	    }
	    csvReader.close();
	    return list;
	}

	private Agency agency;

	@BeforeEach
	void setUp() {
		agency = new Agency();
		vehicles.forEach(agency::addVehicle);
		orders.forEach(agency::addOrder);
	}

	@ParameterizedTest
	@CsvSource(value = {
			"1 : HATCHBACK, SALOON, MUV, SUV, COUPE",
			"3 : HATCHBACK, SALOON, SUV",
			"9 : HATCHBACK"
		}, delimiter = ':')
	void testFindBodytypes(int k, String makers) {
		agency = new Agency();
		vehicles.stream()
			.filter(o -> o.getId() % k == 0)
			.forEach(agency::addVehicle);
		List<BodyType> actual = agency.findBodytypes().stream()
				.sorted()
				.toList();
		List<BodyType> expected = Arrays.asList(makers.split(", ")).stream()
				.map(BodyType::valueOf)
				.sorted()
				.toList();
		assertIterableEquals(expected, actual);
	}

	@ParameterizedTest
	@CsvSource(value = {
			"80 : 9, 10, 12, 14, 15, 17, 18, 19, 21, 23",
			"100 : 10, 12, 14, 18, 19",
			"120 : 10, 12"}, delimiter = ':')
	void testFindClientsWithAveragePriceNoLessThan(int level, String ids) {
		List<Long> actual = agency.findClientsWithAveragePriceNoLessThan(clients, level).stream()
				.map(Client::getId)
				.sorted()
				.toList();
		List<Long> expected = Arrays.stream(ids.split(", "))
				.map(Long::valueOf).toList();

		assertIterableEquals(expected, actual);
	}

	@ParameterizedTest
	@CsvSource(value = {
			"2 : Alfa Romeo, Audi, BMW, Ford, KIA, Peugeot, Renault, Volkswagen, Škoda Motorsport",
			"3 : Audi, BMW, Fiat Chrysler Automobiles N.V., KIA, Peugeot, Vauxhall Motors, Škoda Motorsport",
			"5 : Fiat Chrysler Automobiles N.V., Ford, Honda, Renault"
		}, delimiter = ':')
	void testFindMakers(int k, String makers) {
		agency = new Agency();
		vehicles.stream()
			.filter(o -> o.getId() % k == 0)
			.forEach(agency::addVehicle);
		List<String> actual = agency.findMakers().stream()
				.sorted()
				.toList();
		List<String> expected = Arrays.asList(makers.split(", ")).stream()
				.sorted()
				.toList();
		assertIterableEquals(expected, actual);
	}

	@ParameterizedTest
	@CsvSource(value = {
			"2 : 4 : 2, 4, 6, 16",
			"3 : 3 : 3, 4, 8",
			"7 : 3 : 2, 4, 18"
		}, delimiter = ':')
	void testFindMostOrderedVehicles(int k, int maxCount, String vehiclesIds) {
		agency = new Agency();

		vehicles.forEach(agency::addVehicle);
		orders.stream()
			.filter(o -> o.getId() % k == 0)
			.forEach(agency::addOrder);
		List<Long> actual = agency.findMostOrderedVehicles(maxCount).stream()
				.map(Vehicle::getId)
				.sorted()
				.toList();
		List<Long> expected = Arrays.asList(vehiclesIds.split(", ")).stream()
				.map(Long::valueOf)
				.sorted()
				.toList();
		assertIterableEquals(expected, actual);
	}

	@ParameterizedTest
	@CsvSource(value = {
			"2 : 5 : 10, 12, 14, 17, 21",
			"5 : 3 : 14, 15, 18",
			"7 : 3 : 17, 23, 24"
		}, delimiter = ':')
	void testFindTopClientsByPrices(int k, int maxCount, String clientsIds) {
		agency = new Agency();
		orders.stream()
			.filter(o -> o.getId() % k == 0)
			.forEach(agency::addOrder);
		List<Long> actual = agency.findTopClientsByPrices(clients, maxCount).stream()
				.map(Client::getId)
				.sorted()
				.toList();
		List<Long> expected = Arrays.asList(clientsIds.split(", ")).stream()
				.map(Long::valueOf)
				.sorted()
				.toList();
		assertIterableEquals(expected, actual);
	}

	@ParameterizedTest
	@CsvSource(value = {
			"5 ; Renault : 20 ~ Fiat Chrysler Automobiles N.V. : 15 ~ Ford : 10 ~ Honda : 5",
			"6 ; KIA : 24 ~ Audi : 12 ~ Škoda Motorsport : 18 ~ BMW : 6",
			"7 ; Renault : 14 ~ Vauxhall Motors : 7 ~ Fiat Chrysler Automobiles N.V. : 21"
		}, delimiter = ';')
	void testfindVehicleGrouppedByMake(int k, String expected) {
		agency = new Agency();
		vehicles.stream()
			.filter(o -> o.getId() % k == 0)
			.forEach(agency::addVehicle);
		String actual = agency.findVehicleGrouppedByMake().entrySet().stream()
				.map(e -> new StringBuilder()
						.append(e.getKey())
						.append(" : ")
						.append(e.getValue().stream()
									.map(Vehicle::getId)
									.map(Object::toString)
									.collect(Collectors.joining(", "))))
				.collect(Collectors.joining(" ~ "));
		assertEquals(expected, actual);
	}

	@ParameterizedTest
	@CsvSource(value = {
			"2; 2, 4, 6, 7, 8, 10, 11, 14, 16",
			"3; 3, 4, 8, 10, 15",
			"4; 2, 4, 8, 11, 16",
		}, delimiter = ';')
	void testSortById(int k, String vehiclesIds) {
		agency = new Agency();
		List<Long> idList = orders.stream()
			.filter(o -> o.getId() % k == 0)
			.map(Order::getVehicleId)
			.toList();
		vehicles.stream()
			.filter(v -> idList.contains(v.getId()))
			.forEach(agency::addVehicle);
		List<Long> actual = agency.sortByID().stream()
				.map(Vehicle::getId)
				.toList();
		List<Long> expected = Arrays.asList(vehiclesIds.split(", ")).stream()
				.map(Long::valueOf)
				.toList();
		assertIterableEquals(expected, actual);
	}

	@ParameterizedTest
	@CsvSource(value = {
			"2; 14, 6, 10, 8, 7, 2, 11, 4, 16",
			"3; 10, 15, 8, 3, 4",
			"5; 6, 3, 2, 5"
		}, delimiter = ';')
	void testSortByOdometer(int k, String vehiclesIds) {
		agency = new Agency();
		List<Long> idList = orders.stream()
			.filter(o -> o.getId() % k == 0)
			.map(Order::getVehicleId)
			.toList();
		vehicles.stream()
			.filter(v -> idList.contains(v.getId()))
			.forEach(agency::addVehicle);
		List<Long> actual = agency.sortByOdometer().stream()
				.map(Vehicle::getId)
				.toList();
		List<Long> expected = Arrays.asList(vehiclesIds.split(", ")).stream()
				.map(Long::valueOf)
				.toList();
		assertIterableEquals(expected, actual);
	}

	@ParameterizedTest
	@CsvSource(value = {
			"2; 2, 14, 4, 8, 6, 7, 11, 10, 16",
			"3; 4, 8, 10, 3, 15",
			"5; 2, 5, 6, 3"
		}, delimiter = ';')
	void testSortByYearOfProduction(int k, String vehiclesIds) {
		agency = new Agency();
		List<Long> idList = orders.stream()
			.filter(o -> o.getId() % k == 0)
			.map(Order::getVehicleId)
			.toList();
		vehicles.stream()
			.filter(v -> idList.contains(v.getId()))
			.forEach(agency::addVehicle);
		List<Long> actual = agency.sortByYearOfProduction().stream()
				.map(Vehicle::getId)
				.toList();
		List<Long> expected = Arrays.asList(vehiclesIds.split(", ")).stream()
				.map(Long::valueOf)
				.toList();
		assertIterableEquals(expected, actual);
	}


}

