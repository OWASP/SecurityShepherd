package listeners;

import dbProcs.ConnectionPool;
import dbProcs.MongoDatabase;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import servlets.Setup;

/**
 * Servlet context listener for managing database connection pool lifecycle. Initializes connection
 * pools when the application starts and shuts them down when the application stops.
 *
 * <p>This file is part of the Security Shepherd Project.
 *
 * <p>The Security Shepherd project is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * @author Paul
 */
@WebListener
public class DatabaseLifecycleListener implements ServletContextListener {

  private static final Logger log = LogManager.getLogger(DatabaseLifecycleListener.class);

  /**
   * Called when the servlet context is initialized (application startup). Initializes the database
   * connection pools.
   *
   * @param sce The servlet context event
   */
  @Override
  public void contextInitialized(ServletContextEvent sce) {
    log.info("Application starting - initializing database connection pools...");

    try {
      // Initialize MySQL/MariaDB connection pool
      ConnectionPool.initialize();
      log.info("MySQL/MariaDB connection pool initialized successfully");

      // Note: MongoDB connections are lazy-initialized on first use
      // since MongoClient has its own internal connection pool
      log.info("MongoDB connections will be initialized on first use");

      boolean installed = Setup.isInstalled();
      log.info("Setup.isInstalled() cached at startup: " + installed);

    } catch (RuntimeException e) {
      // Catch RuntimeException (including configuration errors) to prevent
      // application startup from failing completely
      log.error("Failed to initialize database connection pools: " + e.getMessage(), e);
      log.warn(
          "Application will continue without database connectivity. "
              + "Some features may not work until database is properly configured.");
    } catch (Exception e) {
      log.error("Failed to initialize database connection pools: " + e.getMessage(), e);
      // Don't prevent application startup, but log the error
      // Some modules may still work without database connectivity
    }
  }

  /**
   * Called when the servlet context is destroyed (application shutdown). Shuts down all database
   * connection pools to release resources.
   *
   * @param sce The servlet context event
   */
  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    log.info("Application shutting down - closing database connection pools...");

    try {
      // Shutdown MySQL/MariaDB connection pool
      ConnectionPool.shutdown();
      log.info("MySQL/MariaDB connection pool shut down successfully");

      // Shutdown MongoDB connections
      MongoDatabase.shutdown();
      log.info("MongoDB connections shut down successfully");

    } catch (Exception e) {
      log.error("Error during database connection pool shutdown: " + e.getMessage(), e);
    }
  }
}
