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
package reso.common;

public interface HardwareInterface<M extends Message>
        extends Interface {

    // --- MESSAGE TRANSMISSION / RECEPTION ---

    /* Send a message through that interface (typ. to the connected medium) */
    void receive(M m)
            throws Exception;

    /* Receive a message from this interface (typ. from the connected medium) */
    void send(M m)
            throws Exception;


    // --- PHYSICAL CONNECTION MANAGEMENT ---

    void connectTo(Link<M> link)
            throws Exception;

    boolean isConnected();

    boolean isConnectedTo(Link<M> link);

    Link<M> getLink();


    // --- MESSAGE LISTENERS MANAGEMENT ---

    void addListener(int index, MessageListener<M> l);

    void addListener(MessageListener<M> l);

    void removeListener(MessageListener<M> l);

}
