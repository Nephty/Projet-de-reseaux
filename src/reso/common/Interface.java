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

public interface Interface {

    String STATE = "state";

    String getName();

    String getType();

    int getIndex();

    void setIndex(int index);

    Node getNode();

    void up();

    void down();


    // --- ATTRIBUTES MANAGEMENT ---

    boolean isActive();

    Object getAttribute(String attr);

    void addAttrListener(InterfaceAttrListener l);

    void removeAttrListener(InterfaceAttrListener l);

}
