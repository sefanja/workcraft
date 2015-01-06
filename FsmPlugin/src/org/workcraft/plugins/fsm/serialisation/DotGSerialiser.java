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

package org.workcraft.plugins.fsm.serialisation;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.plugins.fsm.Event;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fsm.State;
import org.workcraft.plugins.fsm.Symbol;
import org.workcraft.serialisation.Format;
import org.workcraft.serialisation.ModelSerialiser;
import org.workcraft.serialisation.ReferenceProducer;

public class DotGSerialiser implements ModelSerialiser {

	class ReferenceResolver implements ReferenceProducer {
		HashMap<Object, String> refMap = new HashMap<Object, String>();

		@Override
		public String getReference(Object obj) {
			return refMap.get(obj);
		}
	}

	@Override
	public ReferenceProducer serialise(Model model, OutputStream outStream, ReferenceProducer inRef) {
		PrintWriter out = new PrintWriter(outStream);
		out.print("# SG file generated by Workcraft -- http://workcraft.org\n");

		ReferenceResolver resolver = new ReferenceResolver();

		if (model instanceof Fsm) {
			writeFsm(out, (Fsm)model);
		} else {
			throw new ArgumentException ("Model class not supported: " + model.getClass().getName());
		}
		out.close();
		return resolver;
	}

	@Override
	public boolean isApplicableTo(Model model) {
		return (model instanceof Fsm);
	}

	@Override
	public String getDescription() {
		return "Workcraft SG serialiser";
	}

	@Override
	public String getExtension() {
		return ".g";
	}

	@Override
	public UUID getFormatUUID() {
		return Format.SG;
	}

	private String getSrialisedName(Fsm fsm, Node node) {
		String result = null;
		if (node instanceof Event) {
			Event event = (Event)node;
			Symbol symbol = event.getSymbol();
			if (symbol == null) {
				result = Fsm.EPSILON_SERIALISATION;
			} else {
				result = fsm.getName(symbol);
			}
		} else {
			String ref = fsm.getNodeReference(node);
			result = NamespaceHelper.hierarchicalToFlatName(ref);

		}
		return result;
	}

	private void writeHeader(PrintWriter out, Fsm fsm, String header) {
		HashSet<String> names = new HashSet<String>();
		for (Event event: fsm.getEvents()) {
			String eventStr = getSrialisedName(fsm, event);
			names.add(eventStr);
		}
		if ( !names.isEmpty() ) {
			out.write(header);
			for (String s: names) {
				out.write(" " + s);
			}
			out.write("\n");
		}
	}

	private void writeGraphEntry(PrintWriter out, Fsm fsm, Event event) {
		if (event != null) {
			State firstState = (State)event.getFirst();
			State secondState = (State)event.getSecond();
			if ((firstState != null) && (secondState != null)) {
				String eventStr = getSrialisedName(fsm, event);
				String firstStateStr = getSrialisedName(fsm, firstState);
				String secondStateStr = getSrialisedName(fsm, secondState);
				out.write(firstStateStr + " " + eventStr + " " + secondStateStr + "\n");
			}
		}
	}

	private void writeMarking(PrintWriter out, Fsm fsm, State state) {
		if (state != null) {
			String stateStr = getSrialisedName(fsm, state);
			out.print(".marking {" + stateStr + "}\n");
		}
	}

	private void writeFsm(PrintWriter out, Fsm fsm) {
		writeHeader(out, fsm, ".dummy");

		out.print(".state graph\n");
		for (Event event : fsm.getEvents()) {
			writeGraphEntry(out, fsm, event);
		}

		writeMarking(out, fsm, fsm.getInitialState());
		out.print(".end\n");
	}

}