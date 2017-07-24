#!/bin/bash

#Pac4j Social Configuration
export BASE_URL="http://localhost:9000"
export GOOGLE_CLIENT_ID=put_google_client_id_here
export GOOGLE_SECRET=put_google_secret_here

# Database configuration
export JDBC_SLICK_DRIVER="slick.driver.H2Driver$"
export JDBC_DB_DRIVER ="org.h2.Driver"
export JDBC_DATABASE_URL ="jdbc:h2:mem:play;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=FALSE"
export JDBC_USER="sa"
export JDBC_PASSWORD =""

# Test Login should be disabled in any no test run
export TEST_AUTH=false