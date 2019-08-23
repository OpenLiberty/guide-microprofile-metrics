// tag::copyright[]
/*******************************************************************************
 * Copyright (c) 2017, 2019 IBM Corporation and others.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import io.openliberty.guides.inventory.model.InventoryList;
import io.openliberty.guides.inventory.model.SystemData;

import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.metrics.annotation.Gauge;
import org.eclipse.microprofile.metrics.MetricUnits;

// tag::ApplicationScoped[]
@ApplicationScoped
// end::ApplicationScoped[]
public class InventoryManager {

  private List<SystemData> systems = Collections.synchronizedList(new ArrayList<>());
  private InventoryUtils invUtils = new InventoryUtils();

  @Timed(name = "inventoryPropertiesRequestTime", 
    absolute = true,
    description = "Time needed to get the properties of" +
      "a system from the given hostname")
  public Properties get(String hostname) {
    return invUtils.getProperties(hostname);
  }

  public void add(String hostname, Properties systemProps) {
    Properties props = new Properties();
    props.setProperty("os.name", systemProps.getProperty("os.name"));
    props.setProperty("user.name", systemProps.getProperty("user.name"));

    SystemData host = new SystemData(hostname, props);
    if (!systems.contains(host))
      systems.add(host);
  }


  @Counted(name = "inventoryAccessCount", 
    absolute = true, 
    description = "Number of times the list of systems method is requested")
  public InventoryList list() {
    return new InventoryList(systems);
  }

  @Gauge(unit = MetricUnits.NONE, 
    name = "inventorySizeGuage", 
    absolute = true,
    description = "Number of systems in the inventory")
  public int getTotal() {
    return systems.size();
  }

}
// end::InventoryManager[]
