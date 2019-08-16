/*******************************************************************************
* Copyright (c) 2018 IBM Corporation and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     IBM Corporation - initial API and implementation
*******************************************************************************/
package com.ibm.ws.lars.rest.mongo;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import com.ibm.websphere.crypto.PasswordUtil;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.mongodb.DB;

import java.util.ArrayList;
import java.util.Properties;
import java.util.Vector;
import java.util.Iterator;
import java.util.Collections;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;


@ApplicationScoped
public class MongoProducer {
    private static final Logger logger = Logger.getLogger(MongoProducer.class.getCanonicalName());

    private String dbName = null;

    @Produces
    public MongoClient createMongo() {
        Properties sysprops = System.getProperties();

        ArrayList<ServerAddress> servers = new ArrayList<ServerAddress>(2);

        MongoClientOptions opts;

        // look for all lars.mongo.hostname* properties, in alphabetical order
        Enumeration keysEnum = sysprops.keys();
        Vector<String> keyList = new Vector<String>();
        while(keysEnum.hasMoreElements()){
            keyList.add((String)keysEnum.nextElement());
        }
        Collections.sort(keyList);
        Iterator<String> iterator = keyList.iterator();
        while (iterator.hasNext()) {
            String prop = iterator.next();
            if(prop.startsWith("lars.mongo.hostname")) {
                String hostname = sysprops.getProperty(prop,"localhost");
                int port = Integer.parseInt(sysprops.getProperty(prop.replace("hostname","port"),"27017"));
                ServerAddress sa = new ServerAddress(hostname, port);
                servers.add(sa);
                logger.info("createMongo: found mongodb server setting "+hostname+":"+port+" from property "+prop);
            }
        }

        // add default server if none defined
        if(servers.isEmpty()) {
            ServerAddress sa = new ServerAddress("localhost", 27017);
            servers.add(sa);
            logger.info("createMongo: no mongodb servers specified, defaulting to localhost:27017");
        }

        // database name
        this.dbName = sysprops.getProperty("lars.mongo.dbname", "larsDB");

        // user and password (optional - if not set, use unauthenticated access)
        String user = sysprops.getProperty("lars.mongo.user");
        String encodedPass = sysprops.getProperty("lars.mongo.pass.encoded");

        // writeConcern (optional - if not set use the default "ACKNOWLEDGED")
        String writeConcernStr = sysprops.getProperty("lars.mongo.writeConcern");
        if(writeConcernStr != null) {
            WriteConcern wc = new WriteConcern(writeConcernStr);
            opts = new MongoClientOptions.Builder().writeConcern(wc).build();
            logger.info("createMongo: using write concern "+opts.getWriteConcern().getWString());
        } else {
            opts = new MongoClientOptions.Builder().build();
            logger.info("createMongo: using default write concern");
        }

        if(encodedPass == null) {
            logger.info("createMongo: connecting to database "+dbName+" using unauthenticated access");
            return new MongoClient(servers, opts);
        } else {
            String password = PasswordUtil.passwordDecode(encodedPass);
            MongoCredential creds = MongoCredential.createCredential(user, dbName, password.toCharArray());
            logger.info("createMongo: connecting to database "+dbName+" as user "+user);
            return new MongoClient(servers, creds, opts);
        }
    }

    @Produces
    public DB createDB(MongoClient client) {
        return client.getDB(dbName);
    }

    public void close(@Disposes MongoClient toClose) {
        toClose.close();
    }
}
