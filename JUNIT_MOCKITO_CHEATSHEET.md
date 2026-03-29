# JUnit 5 & Mockito Complete Cheat Sheet

## Table of Contents

- [JUnit 5 Annotations](#junit-5-annotations)
- [JUnit 5 Assertions](#junit-5-assertions)
- [AssertJ Assertions (Fluent Style)](#assertj-assertions-fluent-style)
- [JUnit 5 Lifecycle](#junit-5-lifecycle)
- [JUnit 5 Parameterized Tests](#junit-5-parameterized-tests)
- [JUnit 5 Conditional Tests](#junit-5-conditional-tests)
- [Mockito Core](#mockito-core)
- [Mockito Verify](#mockito-verify)
- [Mockito Argument Matchers](#mockito-argument-matchers)
- [Mockito Argument Captors](#mockito-argument-captors)
- [Spring Boot Test Annotations](#spring-boot-test-annotations)
- [Spring Security Test](#spring-security-test)
- [Full Example: Service Unit Test](#full-example-service-unit-test)
- [Full Example: Controller Integration Test](#full-example-controller-integration-test)
- [Gradle Commands](#gradle-commands)

---

## JUnit 5 Annotations

```java
import org.junit.jupiter.api.*;

@Test                       // Marks a method as a test
@DisplayName("descriptive") // Custom name shown in test reports
@Disabled("reason")         // Skips this test
@Tag("fast")                // Tags for filtering tests
@Timeout(5)                 // Fails if test takes > 5 seconds
@RepeatedTest(3)            // Runs the test 3 times
@Nested                     // Groups tests inside inner classes
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // Orders tests
@Order(1)                   // Specifies test execution order
```

### Example:

```java
class MyTest {

    @Test
    @DisplayName("Should add two numbers correctly")
    void shouldAddNumbers() {
        assertEquals(4, 2 + 2);
    }

    @Test
    @Disabled("Bug #123 - fix pending")
    void brokenTest() {
        // This test will be skipped
    }

    @Nested
    @DisplayName("When user is logged in")
    class WhenLoggedIn {
        @Test
        void shouldAccessDashboard() {
            // grouped test
        }
    }
}
```

---

## JUnit 5 Assertions

```java
import static org.junit.jupiter.api.Assertions.*;

// Basic assertions
assertEquals(expected, actual);                 // Checks equality
assertEquals(expected, actual, "custom msg");   // With message on failure
assertNotEquals(unexpected, actual);            // Checks NOT equal

// Boolean
assertTrue(condition);                          // Must be true
assertFalse(condition);                         // Must be false

// Null checks
assertNull(object);                             // Must be null
assertNotNull(object);                          // Must NOT be null

// Same reference
assertSame(expected, actual);                   // Same object reference (==)
assertNotSame(unexpected, actual);              // Different references

// Arrays
assertArrayEquals(expectedArray, actualArray);  // Arrays equal element by element

// Exceptions
assertThrows(IllegalArgumentException.class, () -> {
    myMethod(-1);  // This should throw
});

// Does NOT throw
assertDoesNotThrow(() -> {
    myMethod(1);
});

// Timeout
assertTimeout(Duration.ofSeconds(2), () -> {
    slowMethod();  // Must finish in 2 seconds
});

// Group multiple assertions (ALL are checked even if one fails)
assertAll("person",
    () -> assertEquals("John", person.getName()),
    () -> assertEquals(30, person.getAge()),
    () -> assertNotNull(person.getEmail())
);
```

---

## AssertJ Assertions (Fluent Style)

> AssertJ comes included in `spring-boot-starter-test`. It's more readable than JUnit assertions.

```java
import static org.assertj.core.api.Assertions.*;

// Basic
assertThat(actual).isEqualTo(expected);
assertThat(actual).isNotEqualTo(unexpected);

// Null
assertThat(object).isNull();
assertThat(object).isNotNull();

// Boolean
assertThat(result).isTrue();
assertThat(result).isFalse();

// Strings
assertThat(name).isEqualTo("John");
assertThat(name).contains("oh");
assertThat(name).startsWith("Jo");
assertThat(name).endsWith("hn");
assertThat(name).isNotEmpty();
assertThat(name).isNotBlank();
assertThat(name).hasSize(4);
assertThat(name).matches("[A-Za-z]+");           // regex
assertThat(name).containsIgnoringCase("john");

// Numbers
assertThat(age).isEqualTo(30);
assertThat(age).isGreaterThan(18);
assertThat(age).isLessThan(100);
assertThat(age).isBetween(18, 65);
assertThat(age).isPositive();
assertThat(age).isNegative();
assertThat(age).isZero();

// Lists / Collections
assertThat(list).isEmpty();
assertThat(list).isNotEmpty();
assertThat(list).hasSize(3);
assertThat(list).contains("a", "b");
assertThat(list).containsExactly("a", "b", "c");        // exact order
assertThat(list).containsExactlyInAnyOrder("c", "a", "b"); // any order
assertThat(list).doesNotContain("z");
assertThat(list).containsOnly("a", "b", "c");           // only these, any order

// Objects
assertThat(user).isInstanceOf(User.class);
assertThat(user).isNotSameAs(otherUser);
assertThat(user).extracting(User::getName).isEqualTo("John");

// Exceptions
assertThatThrownBy(() -> myMethod(-1))
    .isInstanceOf(IllegalArgumentException.class)
    .hasMessage("Value must be positive")
    .hasMessageContaining("positive");

assertThatCode(() -> myMethod(1))
    .doesNotThrowAnyException();

// Maps
assertThat(map).containsKey("name");
assertThat(map).containsValue("John");
assertThat(map).containsEntry("name", "John");
assertThat(map).hasSize(2);
```

---

## JUnit 5 Lifecycle

```java
import org.junit.jupiter.api.*;

class LifecycleTest {

    @BeforeAll   // Runs ONCE before ALL tests (must be static)
    static void initAll() {
        System.out.println("Before all tests");
    }

    @BeforeEach  // Runs before EACH test
    void init() {
        System.out.println("Before each test");
    }

    @Test
    void test1() { }

    @Test
    void test2() { }

    @AfterEach   // Runs after EACH test
    void tearDown() {
        System.out.println("After each test");
    }

    @AfterAll    // Runs ONCE after ALL tests (must be static)
    static void tearDownAll() {
        System.out.println("After all tests");
    }
}
```

**Execution order:** `@BeforeAll` → `@BeforeEach` → `@Test` → `@AfterEach` → `@BeforeEach` → `@Test` → `@AfterEach` → `@AfterAll`

---

## JUnit 5 Parameterized Tests

```java
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

// Test with multiple values
@ParameterizedTest
@ValueSource(ints = {1, 2, 3, 4, 5})
void shouldBePositive(int number) {
    assertTrue(number > 0);
}

// Test with strings
@ParameterizedTest
@ValueSource(strings = {"hello", "world", "java"})
void shouldNotBeEmpty(String text) {
    assertFalse(text.isEmpty());
}

// Test with null and empty
@ParameterizedTest
@NullAndEmptySource
@ValueSource(strings = {"  ", "\t", "\n"})
void shouldBeBlank(String text) {
    assertTrue(text == null || text.isBlank());
}

// Test with enum values
@ParameterizedTest
@EnumSource(Month.class)
void shouldHaveValidMonth(Month month) {
    assertNotNull(month);
}

// Test with CSV values
@ParameterizedTest
@CsvSource({
    "1, 1, 2",
    "2, 3, 5",
    "10, 20, 30"
})
void shouldAdd(int a, int b, int expected) {
    assertEquals(expected, a + b);
}

// Test with method as source
@ParameterizedTest
@MethodSource("provideUsers")
void shouldHaveName(String name, int age) {
    assertNotNull(name);
    assertTrue(age > 0);
}

static Stream<Arguments> provideUsers() {
    return Stream.of(
        Arguments.of("John", 30),
        Arguments.of("Jane", 25)
    );
}
```

---

## JUnit 5 Conditional Tests

```java
import org.junit.jupiter.api.condition.*;

@EnabledOnOs(OS.WINDOWS)                    // Only on Windows
@EnabledOnOs(OS.LINUX)                      // Only on Linux
@DisabledOnOs(OS.MAC)                       // Skip on Mac

@EnabledOnJre(JRE.JAVA_21)                  // Only on Java 21
@EnabledForJreRange(min = JRE.JAVA_17)      // Java 17+

@EnabledIfEnvironmentVariable(named = "ENV", matches = "dev")
@EnabledIfSystemProperty(named = "os.arch", matches = ".*64.*")
```

---

## Mockito Core

```java
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)   // Enables Mockito annotations
class MyServiceTest {

    @Mock                              // Creates a mock (fake) object
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks                       // Creates real object, injects mocks into it
    private UserService userService;

    // --- STUBBING (defining mock behavior) ---

    @Test
    void stubbingExamples() {

        // when().thenReturn() - Return a value when method is called
        when(userRepository.findById(1L))
            .thenReturn(Optional.of(new User("John")));

        // when().thenReturn() - Multiple returns
        when(userRepository.count())
            .thenReturn(1L)     // First call returns 1
            .thenReturn(2L)     // Second call returns 2
            .thenReturn(3L);    // Third call returns 3

        // when().thenThrow() - Throw exception
        when(userRepository.findById(999L))
            .thenThrow(new RuntimeException("Not found"));

        // doReturn() - Alternative syntax (needed for void methods)
        doReturn(Optional.of(new User("John")))
            .when(userRepository).findById(1L);

        // doThrow() - For void methods
        doThrow(new RuntimeException("Error"))
            .when(emailService).sendEmail(anyString());

        // doNothing() - For void methods (default, but explicit)
        doNothing()
            .when(emailService).sendEmail("test@test.com");

        // thenAnswer() - Dynamic responses
        when(userRepository.save(any(User.class)))
            .thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(1L);  // Simulate DB setting ID
                return user;
            });
    }
}
```

---

## Mockito Verify

```java
import static org.mockito.Mockito.*;

// --- VERIFY (check that methods were called) ---

// Was the method called?
verify(userRepository).findById(1L);

// Was it called exactly 2 times?
verify(userRepository, times(2)).findById(1L);

// Was it NEVER called?
verify(userRepository, never()).deleteById(anyLong());

// Was it called at least once?
verify(userRepository, atLeastOnce()).save(any());

// Was it called at least 3 times?
verify(userRepository, atLeast(3)).findAll();

// Was it called at most 5 times?
verify(userRepository, atMost(5)).findAll();

// Verify order of calls
InOrder inOrder = inOrder(userRepository, emailService);
inOrder.verify(userRepository).save(any());        // First this
inOrder.verify(emailService).sendEmail(anyString()); // Then this

// Verify no more interactions with the mock
verifyNoMoreInteractions(userRepository);

// Verify zero interactions with the mock
verifyNoInteractions(emailService);
```

---

## Mockito Argument Matchers

```java
import static org.mockito.ArgumentMatchers.*;

// Any value of a type
when(repo.findById(anyLong())).thenReturn(Optional.empty());
when(repo.findByName(anyString())).thenReturn(null);
when(repo.save(any(User.class))).thenReturn(new User());
when(repo.findAll(any())).thenReturn(List.of());

// Specific matchers
when(repo.findById(eq(1L))).thenReturn(Optional.of(user));       // equals
when(service.process(contains("hello"))).thenReturn(true);        // contains
when(service.process(startsWith("hello"))).thenReturn(true);      // starts with
when(service.process(endsWith("world"))).thenReturn(true);        // ends with
when(service.process(matches("[A-Z].*"))).thenReturn(true);       // regex

// Null matchers
when(repo.findByName(isNull())).thenReturn(null);
when(repo.findByName(isNotNull())).thenReturn(user);

// IMPORTANT RULE: If you use a matcher for ONE argument,
// you must use matchers for ALL arguments
when(repo.findByNameAndAge(eq("John"), eq(30))).thenReturn(user);  // ✅
// when(repo.findByNameAndAge("John", eq(30)));  // ❌ WRONG! Mixed
```

---

## Mockito Argument Captors

> Capture the actual arguments passed to a mock to inspect them later.

```java
import org.mockito.ArgumentCaptor;

@Test
void shouldCaptureArgument() {
    // Arrange
    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

    // Act
    userService.createUser("John", "john@email.com");

    // Capture the argument
    verify(userRepository).save(userCaptor.capture());

    // Inspect the captured value
    User savedUser = userCaptor.getValue();
    assertThat(savedUser.getName()).isEqualTo("John");
    assertThat(savedUser.getEmail()).isEqualTo("john@email.com");
}

// Capture multiple calls
@Test
void shouldCaptureMultiple() {
    userService.createUser("John", "john@email.com");
    userService.createUser("Jane", "jane@email.com");

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository, times(2)).save(captor.capture());

    List<User> allSaved = captor.getAllValues();
    assertThat(allSaved).hasSize(2);
    assertThat(allSaved.get(0).getName()).isEqualTo("John");
    assertThat(allSaved.get(1).getName()).isEqualTo("Jane");
}
```

---

## Spring Boot Test Annotations

```java
// Full integration test (loads entire Spring context)
@SpringBootTest
class FullIntegrationTest {
    @Autowired
    private UserService userService;
}

// Web layer only (controllers, filters, security)
@WebMvcTest(UserController.class)
class ControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean   // Spring-aware mock (replaces bean in context)
    private UserService userService;
}

// JPA layer only (repositories, entities)
@DataJpaTest
class RepositoryTest {
    @Autowired
    private UserRepository userRepository;
}

// Custom test configuration
@TestConfiguration
class TestConfig {
    @Bean
    public UserService userService() {
        return new UserService(mockRepo);
    }
}

// Test properties
@SpringBootTest
@TestPropertySource(properties = {
    "jwt.secret=test-secret-key-for-testing",
    "jwt.expiration-ms=3600000"
})
class WithCustomProperties { }

// Use test application.properties
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class WithTestPropertiesFile { }

// Active profile
@SpringBootTest
@ActiveProfiles("test")
class WithTestProfile { }
```

---

## Spring Security Test

```java
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

// Test as authenticated user
@Test
@WithMockUser(username = "admin", roles = {"ADMIN"})
void shouldAccessAdminEndpoint() throws Exception {
    mockMvc.perform(get("/api/admin/users"))
        .andExpect(status().isOk());
}

// Test as specific user
@Test
@WithMockUser(username = "john", password = "pass", authorities = {"ROLE_USER"})
void shouldAccessUserEndpoint() throws Exception {
    mockMvc.perform(get("/api/profile"))
        .andExpect(status().isOk());
}

// Test without authentication
@Test
void shouldRejectUnauthenticated() throws Exception {
    mockMvc.perform(get("/api/admin/users"))
        .andExpect(status().isUnauthorized());
}

// Test with CSRF
@Test
void shouldAcceptWithCsrf() throws Exception {
    mockMvc.perform(post("/api/users")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"name\":\"John\"}"))
        .andExpect(status().isCreated());
}
```

---

## Full Example: Service Unit Test

```java
package devcourses.backvue.back.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CourseService Tests")
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseService courseService;

    private Course sampleCourse;

    @BeforeEach
    void setUp() {
        sampleCourse = new Course();
        sampleCourse.setId(1L);
        sampleCourse.setTitle("Spring Boot");
        sampleCourse.setDescription("Learn Spring Boot");
    }

    @Test
    @DisplayName("Should return all courses")
    void shouldReturnAllCourses() {
        // Arrange (prepare mock behavior)
        when(courseRepository.findAll())
            .thenReturn(List.of(sampleCourse));

        // Act (call the method under test)
        List<Course> courses = courseService.getAllCourses();

        // Assert (check the result)
        assertThat(courses).hasSize(1);
        assertThat(courses.get(0).getTitle()).isEqualTo("Spring Boot");
        verify(courseRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should find course by ID")
    void shouldFindCourseById() {
        when(courseRepository.findById(1L))
            .thenReturn(Optional.of(sampleCourse));

        Optional<Course> found = courseService.getCourseById(1L);

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Spring Boot");
    }

    @Test
    @DisplayName("Should throw when course not found")
    void shouldThrowWhenNotFound() {
        when(courseRepository.findById(999L))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.getCourseById(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("Should save new course")
    void shouldSaveNewCourse() {
        when(courseRepository.save(any(Course.class)))
            .thenReturn(sampleCourse);

        Course saved = courseService.createCourse(sampleCourse);

        assertThat(saved.getId()).isNotNull();
        verify(courseRepository).save(sampleCourse);
    }

    @Test
    @DisplayName("Should delete course by ID")
    void shouldDeleteCourse() {
        when(courseRepository.existsById(1L)).thenReturn(true);
        doNothing().when(courseRepository).deleteById(1L);

        courseService.deleteCourse(1L);

        verify(courseRepository).deleteById(1L);
    }
}
```

---

## Full Example: Controller Integration Test

```java
package devcourses.backvue.back.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourseController.class)
@DisplayName("CourseController Tests")
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CourseService courseService;

    // You may also need to mock security beans
    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("GET /api/courses - Should return all courses")
    void shouldReturnAllCourses() throws Exception {
        Course course = new Course(1L, "Spring Boot", "Learn Spring");
        when(courseService.getAllCourses()).thenReturn(List.of(course));

        mockMvc.perform(get("/api/courses"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].title").value("Spring Boot"))
            .andExpect(jsonPath("$[0].description").value("Learn Spring"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/courses - Admin should create course")
    void adminShouldCreateCourse() throws Exception {
        Course course = new Course(null, "Vue.js", "Learn Vue");
        Course saved = new Course(1L, "Vue.js", "Learn Vue");
        when(courseService.createCourse(any())).thenReturn(saved);

        mockMvc.perform(post("/api/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(course)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("Vue.js"));
    }

    @Test
    @DisplayName("POST /api/courses - Unauthenticated should be rejected")
    void unauthenticatedShouldBeRejected() throws Exception {
        mockMvc.perform(post("/api/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Test\"}"))
            .andExpect(status().isUnauthorized());
    }
}
```

---

## MockMvc Request & Response Matchers

```java
// --- REQUEST BUILDERS ---
get("/api/users")
post("/api/users")
put("/api/users/1")
patch("/api/users/1")
delete("/api/users/1")

// With query params
get("/api/users").param("page", "0").param("size", "10")

// With headers
get("/api/users").header("Authorization", "Bearer token123")

// With JSON body
post("/api/users")
    .contentType(MediaType.APPLICATION_JSON)
    .content("{\"name\":\"John\"}")

// --- RESPONSE MATCHERS ---
.andExpect(status().isOk())                    // 200
.andExpect(status().isCreated())               // 201
.andExpect(status().isNoContent())             // 204
.andExpect(status().isBadRequest())            // 400
.andExpect(status().isUnauthorized())          // 401
.andExpect(status().isForbidden())             // 403
.andExpect(status().isNotFound())              // 404

// JSON response assertions (using JsonPath)
.andExpect(jsonPath("$.name").value("John"))
.andExpect(jsonPath("$.age").value(30))
.andExpect(jsonPath("$.items").isArray())
.andExpect(jsonPath("$.items", hasSize(3)))
.andExpect(jsonPath("$.items[0].name").value("first"))
.andExpect(jsonPath("$.address").doesNotExist())
.andExpect(jsonPath("$.name").isNotEmpty())

// Content type
.andExpect(content().contentType(MediaType.APPLICATION_JSON))

// Print full response for debugging
.andDo(print())
```

---

## Gradle Commands

```bash
# Run ALL tests
./gradlew test

# Run tests with console output
./gradlew test --info

# Run a specific test class
./gradlew test --tests "devcourses.backvue.back.service.CourseServiceTest"

./gradlew test --tests "devcourses.backvue.back.service.AuthServiceTest"
# Run a specific test method
./gradlew test --tests "*.CourseServiceTest.shouldReturnAllCourses"

# Run tests matching a pattern
./gradlew test --tests "*Service*"

# Run tests and always re-run (no cache)
./gradlew test --rerun

# Run tests and show stdout/stderr
./gradlew test -i

# Generate HTML test report (found at build/reports/tests/test/index.html)
./gradlew test
# Report is auto-generated after running tests
```

**Windows (use gradlew.bat):**

```powershell
.\gradlew.bat test
.\gradlew.bat test --tests "*CourseServiceTest"
```

---

## Quick Reference Table

| What you want        | JUnit 5                          | Mockito                               |
| -------------------- | -------------------------------- | ------------------------------------- |
| Mark as test         | `@Test`                          | -                                     |
| Create fake object   | -                                | `@Mock`                               |
| Inject fakes         | -                                | `@InjectMocks`                        |
| Enable Mockito       | -                                | `@ExtendWith(MockitoExtension.class)` |
| Define behavior      | -                                | `when().thenReturn()`                 |
| Check method called  | -                                | `verify()`                            |
| Check equals         | `assertEquals()`                 | -                                     |
| Check true/false     | `assertTrue()` / `assertFalse()` | -                                     |
| Check exception      | `assertThrows()`                 | -                                     |
| Run before each test | `@BeforeEach`                    | -                                     |
| Run before all tests | `@BeforeAll`                     | -                                     |
| Skip test            | `@Disabled`                      | -                                     |
| Fluent assertions    | AssertJ: `assertThat()`          | -                                     |
| Spring mock bean     | -                                | `@MockBean`                           |
| Test as user         | `@WithMockUser`                  | -                                     |
