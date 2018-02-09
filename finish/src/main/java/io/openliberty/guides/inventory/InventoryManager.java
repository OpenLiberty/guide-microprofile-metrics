// tag::copyright[]
/*******************************************************************************
 * Copyright (c) 2017, 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial implementation
 *******************************************************************************/
// end::copyright[]
// tag::InventoryManager[]
package io.openliberty.guides.inventory;

import java.util.Properties;
import io.openliberty.guides.inventory.client.SystemClient;
import io.openliberty.guides.inventory.model.InventoryList;

//CDI
import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;

// tag::ApplicationScoped[]
@ApplicationScoped
// end::ApplicationScoped[]
public class InventoryManager {

  private InventoryList invList = new InventoryList();
  private SystemClient systemClient = new SystemClient();

  @Timed(name = "getPropertiesTime",
    description = "Time needed to get the properties of a system from the given hostname")
  public Properties get(String hostname) {
    systemClient.init(hostname);

    Properties properties = systemClient.getProperties();
    if (properties != null) {
      invList.addToInventoryList(hostname, properties);
    }
    return properties;

  }

  @Counted(absolute = true, monotonic = true,
    description = "Number of times the list of systems method is requested")
  public InventoryList list() {
    return invList;
  }
}
// end::InventoryManager[]
