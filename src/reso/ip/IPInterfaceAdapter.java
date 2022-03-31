/*******************************************************************************
 * Copyright (c) 2011 Bruno Quoitin.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * Contributors:
 *     Bruno Quoitin - initial API and implementation
 ******************************************************************************/
package reso.ip;

import reso.common.Interface;

public interface IPInterfaceAdapter
        extends Interface {

    String ATTR_METRIC = "metric";

    String getName();

    IPLayer getIPLayer();

    boolean hasAddress(IPAddress addr);

    void addAddress(IPAddress addr);

    IPAddress getAddress();

    int getMetric();

    void setMetric(int metric)
            throws Exception;

    void send(Datagram datagram, IPAddress nexthop)
            throws Exception;

    void addListener(IPInterfaceListener l);

    void removeListener(IPInterfaceListener l);

}
