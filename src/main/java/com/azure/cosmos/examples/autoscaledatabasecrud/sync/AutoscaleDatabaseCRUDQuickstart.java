// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.examples.autoscaledatabasecrud.sync;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.examples.changefeed.SampleChangeFeedProcessor;
import com.azure.cosmos.examples.common.AccountSettings;
import com.azure.cosmos.models.CosmosDatabaseProperties;
import com.azure.cosmos.models.CosmosDatabaseRequestOptions;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.util.CosmosPagedIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoscaleDatabaseCRUDQuickstart {

    private CosmosClient client;

    private final String databaseName = "AzureSampleFamilyDB";

    private CosmosDatabase database;

    protected static Logger logger = LoggerFactory.getLogger(SampleChangeFeedProcessor.class.getSimpleName());

    public void close() {
        client.close();
    }

    /**
     * Sample to demonstrate the following AUTOSCALE database CRUD operations:
     * -Create
     * -Read by ID
     * -Read all
     * -Delete
     */
    public static void main(String[] args) {
        AutoscaleDatabaseCRUDQuickstart p = new AutoscaleDatabaseCRUDQuickstart();

        try {
            logger.info("Starting SYNC main");
            p.autoscaleDatabaseCRUDDemo();
            logger.info("Demo complete, please hold while resources are released");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(String.format("Cosmos getStarted failed with %s", e));
        } finally {
            logger.info("Closing the client");
            p.shutdown();
        }
    }

    private void autoscaleDatabaseCRUDDemo() throws Exception {

        logger.info("Using Azure Cosmos DB endpoint: " + AccountSettings.HOST);

        //  Create sync client
        client = new CosmosClientBuilder()
                .endpoint(AccountSettings.HOST)
                .key(AccountSettings.MASTER_KEY)
                .consistencyLevel(ConsistencyLevel.EVENTUAL)
                .contentResponseOnWriteEnabled(true)
                .buildClient();


        createDatabaseIfNotExists();
        readDatabaseById();
        readAllDatabases();
        // deleteADatabase() is called at shutdown()

    }

    // Database Create
    private void createDatabaseIfNotExists() throws Exception {
        logger.info("Create database " + databaseName + " if not exists...");

        // Autoscale throughput settings
        ThroughputProperties autoscaleThroughputProperties = ThroughputProperties.createAutoscaledThroughput(4000); //Set autoscale max RU/s

        //Create the database with autoscale enabled
        CosmosDatabaseResponse databaseResponse = client.createDatabaseIfNotExists(databaseName, autoscaleThroughputProperties);
        database = client.getDatabase(databaseResponse.getProperties().getId());

        logger.info("Done.");
    }

    // Database read
    private void readDatabaseById() throws Exception {
        logger.info("Read database " + databaseName + " by ID.");

        //  Read database by ID
        database = client.getDatabase(databaseName);

        logger.info("Done.");
    }

    // Database read all
    private void readAllDatabases() throws Exception {
        logger.info("Read all databases in the account.");

        //  Read all databases in the account
        CosmosPagedIterable<CosmosDatabaseProperties> databases = client.readAllDatabases();

        // Print
        String msg="Listing databases in account:\n";
        for(CosmosDatabaseProperties dbProps : databases) {
            msg += String.format("-Database ID: %s\n",dbProps.getId());
        }
        logger.info(msg + "\n");

        logger.info("Done.");
    }

    // Database delete
    private void deleteADatabase() throws Exception {
        logger.info("Last step: delete database " + databaseName + " by ID.");

        // Delete database
        CosmosDatabaseResponse dbResp = client.getDatabase(databaseName).delete(new CosmosDatabaseRequestOptions());
        logger.info("Status code for database delete: {}",dbResp.getStatusCode());

        logger.info("Done.");
    }

    // Cleanup before close
    private void shutdown() {
        try {
            //Clean shutdown
            deleteADatabase();
        } catch (Exception err) {
            logger.error("Deleting Cosmos DB resources failed, will still attempt to close the client. See stack trace below.");
            err.printStackTrace();
        }
        client.close();
        logger.info("Done with sample.");
    }

}
