/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.plugins.cpog;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.math.MathNode;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanOperations;

@VisualClass(org.workcraft.plugins.cpog.VisualRhoClause.class)
public class RhoClause extends MathNode {
	public static final String PROPERTY_FORMULA = "Formula";

	private BooleanFormula formula = BooleanOperations.ONE;

	public void setFormula(BooleanFormula formula) {
		this.formula = formula;
		sendNotification(new PropertyChangedEvent(this, PROPERTY_FORMULA));
	}

	public BooleanFormula getFormula() {
		return formula;
	}

}
