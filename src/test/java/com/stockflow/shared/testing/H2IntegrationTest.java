package com.stockflow.shared.testing;

import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base class for integration tests using H2 in-memory database.
 * 
 * <p>
 * Use this base class when Docker/Testcontainers is not available.
 * </p>
 * <p>
 * DirtiesContext ensures a clean context after each test class.
 * </p>
 */
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class H2IntegrationTest {
    // This class applies test profile and ensures clean database state
}
