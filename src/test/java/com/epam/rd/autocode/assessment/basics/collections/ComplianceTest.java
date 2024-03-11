package com.epam.rd.autocode.assessment.basics.collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import spoon.Launcher;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;

@Disabled("This test is used as a trigger to fail all other tests")
class ComplianceTest {
	
	private static Factory spoon;
	
	static {
		final String[] args = {"-i", "src/main/java/"};
		final Launcher launcher = new Launcher();
		launcher.setArgs(args);
		launcher.buildModel();
		spoon = launcher.getFactory();
	}

	@Test
	void testComplianceShouldAllEntityTypesHaveAppropriateMethods() throws IOException {
		checkMethods("com.epam.rd.autocode.assessment.basics.entity", "entity-methods.txt");
	}

	@Test
	void testComplianceShouldAllCollectionsTypesHaveAppropriateMethods() throws IOException {
		checkMethods("com.epam.rd.autocode.assessment.basics.collections", "collections-methods.txt");
	}
	
	private static void checkMethods(String packageName, String methodsFile) throws IOException {
		Path file = Paths.get(".").resolve("src/test/resources/" + methodsFile);
        String expectedContent = Files.readString(file);
        String actualContent = spoon.Package().get(packageName)
        		.getTypes().stream()
					.sorted(Comparator.comparing(CtType::getSimpleName))
					.map(type -> type.getMethods().stream()
							.flatMap(Stream::of)
							.map(m -> String.format("%s %s %s", type.getSimpleName(), m.getType(), m.getSignature()))
							.sorted()
							.collect(Collectors.joining("\n"))
					)
					.collect(Collectors.joining("\n")).trim();
        List<String> expected = Arrays.asList(expectedContent.split("\r?\n"));
        List<String> actual = Arrays.asList(actualContent.split("\n"));
        for (int j = 0; j < expected.size(); j++) {
        	assertEquals(expected.get(j), actual.get(j), () -> "Declarations must be the same");
        }
	}

	@Test
	void testComplianceFindAndSortMethodsInAgencyShouldUseForbiddenAPI() {
		CtType<Agency> agencyClass = spoon.Type().get(Agency.class.getName());
		Class<?>[] classes = {Find.class, Sort.class};
		Arrays.stream(classes)
			.map(Class::getDeclaredMethods)
			.flatMap(Stream::of)
			.map(m -> agencyClass.getMethodsByName(m.getName()).stream())
			.flatMap(Function.identity())
			.forEach(m -> assertTrue(
					m.getReferencedTypes().stream()
						.map(el -> el.getQualifiedName())
						.filter(name -> name.startsWith("java.util.stream")
								|| name.startsWith("java.util.function"))
						.map(el -> Boolean.FALSE)
						.findAny().orElse(Boolean.TRUE),
					() -> "Method " + m.getSignature() + " must not use Stream API and types from the "
							+ "java.util.function package")
			);
	}
	
	@Test
	void testComplianceShouldAppropriateNumberOfPackagesAndClasses() throws IOException {
		String expected = "Agency Find Sort\nBodyType Client Employee Order User Vehicle";
		String actual = spoon.Package().getAll().stream()
			.filter(p -> p.getTypes().size() != 0)
			.map(p -> p.getTypes().stream()
					.map(CtType::getSimpleName)
					.sorted()
					.collect(Collectors.joining(" ")))
			.collect(Collectors.joining("\n"));
			
		assertEquals(expected, actual);
	}

}
