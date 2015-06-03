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

package org.workcraft.dom.visual.connections;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.DrawHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.gui.Coloriser;
import org.workcraft.observation.SelectionChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.util.Geometry;
import org.workcraft.util.Geometry.CurveSplitResult;

public class Bezier implements ConnectionGraphic, ParametricCurve, StateObserver, SelectionObserver {

	private Node parent;
	private VisualConnectionProperties connectionInfo;
	private PartialCurveInfo curveInfo = null;
	private CubicCurve2D curve = null;
	private CubicCurve2D visibleCurve = null;
	private Rectangle2D boundingBox = null;
	private ControlPointScaler scaler = null;
	private BezierControlPoint cp1;
	private BezierControlPoint cp2;

	public Bezier(VisualConnection parent) {
		this.connectionInfo = parent;
		this.parent = parent;
	}

	@Override
	public void setDefaultControlPoints() {
		initControlPoints(new BezierControlPoint(), new BezierControlPoint());
		if (connectionInfo.getFirstCenter().distanceSq(connectionInfo.getSecondCenter()) < 0.0001 ) {
			Point2D p = connectionInfo.getFirstCenter();
			cp1.setPosition(new Point2D.Double(p.getX() - 2.0, p.getY() + 2.0));
			cp2.setPosition(new Point2D.Double(p.getX() + 2.0, p.getY() + 2.0));
		} else {
			cp1.setPosition(Geometry.lerp(connectionInfo.getFirstCenter(), connectionInfo.getSecondCenter(), 0.3));
			cp2.setPosition(Geometry.lerp(connectionInfo.getFirstCenter(), connectionInfo.getSecondCenter(), 0.6));
		}
		finaliseControlPoints();
	}

	public void initControlPoints(BezierControlPoint cp1, BezierControlPoint cp2) {
		this.cp1 = cp1;
		this.cp2 = cp2;
	}

	public void finaliseControlPoints() {
		cp1.setParent(this);
		cp2.setParent(this);

		cp1.addObserver(this);
		cp2.addObserver(this);
	}

	@Override
	public List<ControlPoint> getControlPoints() {
		return Arrays.asList( new ControlPoint[] { cp1, cp2 });
	}

	public BezierControlPoint[] getBezierControlPoints() {
		return new BezierControlPoint[] { cp1, cp2 };
	}

	public void draw(DrawRequest r) {
		Graphics2D g = r.getGraphics();
		PartialCurveInfo curveInfo = getCurveInfo();
		CubicCurve2D visibleCurve = getVisibleCurve();

		Color color = Coloriser.colorise(connectionInfo.getDrawColor(), r.getDecoration().getColorisation());
		g.setColor(color);
		g.setStroke(connectionInfo.getStroke());
		g.draw(visibleCurve);

		if(connectionInfo.hasArrow()) {
			DrawHelper.drawArrowHead(g, curveInfo.headPosition,	curveInfo.headOrientation,
					connectionInfo.getArrowLength(), connectionInfo.getArrowWidth(), color);
		}

		if (connectionInfo.hasBubble()) {
			DrawHelper.drawBubbleHead(g, curveInfo.headPosition, curveInfo.headOrientation,
					connectionInfo.getBubbleSize(),	color, connectionInfo.getStroke());
		}
	}

	@Override
	public Rectangle2D getBoundingBox() {
		if (boundingBox == null) {
			CubicCurve2D curve = getCurve();
			boundingBox = curve.getBounds2D();
			boundingBox.add(boundingBox.getMinX()-VisualConnection.HIT_THRESHOLD, boundingBox.getMinY()-VisualConnection.HIT_THRESHOLD);
			boundingBox.add(boundingBox.getMinX()-VisualConnection.HIT_THRESHOLD, boundingBox.getMaxY()+VisualConnection.HIT_THRESHOLD);
			boundingBox.add(boundingBox.getMaxX()+VisualConnection.HIT_THRESHOLD, boundingBox.getMinY()-VisualConnection.HIT_THRESHOLD);
			boundingBox.add(boundingBox.getMaxX()+VisualConnection.HIT_THRESHOLD, boundingBox.getMaxY()+VisualConnection.HIT_THRESHOLD);
		}
		return boundingBox;
	}

	@Override
	public PartialCurveInfo getCurveInfo() {
		if (curveInfo == null) {
			Point2D origin1 = new Point2D.Double();
			origin1.setLocation(connectionInfo.getFirstCenter());
			cp1.getParentToLocalTransform().transform(origin1, origin1);

			Point2D origin2 = new Point2D.Double();
			origin2.setLocation(connectionInfo.getSecondCenter());
			cp2.getParentToLocalTransform().transform(origin2, origin2);

			cp1.update(origin1);
			cp2.update(origin2);

			curveInfo = Geometry.buildConnectionCurveInfo(connectionInfo, this, 0);
		}
		return curveInfo;
	}

	private CubicCurve2D getCurve() {
		if (curve == null) {
			curve = new CubicCurve2D.Double();
			curve.setCurve(connectionInfo.getFirstCenter(), cp1.getPosition(), cp2.getPosition(), connectionInfo.getSecondCenter());
		}
		return curve;
	}

	private CubicCurve2D getVisibleCurve() {
		if (visibleCurve == null) {
			PartialCurveInfo curveInfo = getCurveInfo();
			visibleCurve = getPartialCurve(curveInfo.tStart, curveInfo.tEnd);
		}
		return visibleCurve;
	}

	private CubicCurve2D getPartialCurve(double tStart, double tEnd) {
		CubicCurve2D fullCurve = new CubicCurve2D.Double();
		fullCurve.setCurve(connectionInfo.getFirstCenter(), cp1.getPosition(), cp2.getPosition(), connectionInfo.getSecondCenter());

		CurveSplitResult firstSplit = Geometry.splitCubicCurve(fullCurve, tStart);
		CurveSplitResult secondSplit = Geometry.splitCubicCurve(firstSplit.curve2, (tEnd-tStart)/(1-tStart) );

		return secondSplit.curve1;
	}

	@Override
	public Collection<Node> getChildren() {
		return Arrays.asList( new Node[] { cp1, cp2 });
	}

	@Override
	public Node getParent() {
		return parent;
	}

	@Override
	public void setParent(Node parent) {
		throw new RuntimeException ("Node does not support reparenting");
	}

	@Override
	public boolean hitTest(Point2D point) {
		return getDistanceToCurve(point) < VisualConnection.HIT_THRESHOLD;
	}

	@Override
	public double getDistanceToCurve(Point2D pt) {
		return pt.distance(getNearestPointOnCurve(pt));
	}

	@Override
	public Point2D getNearestPointOnCurve(Point2D pt) {
		// FIXME: should be done using some proper algorithm
		CubicCurve2D curve = getCurve();
		Point2D nearest = new Point2D.Double(curve.getX1(), curve.getY1());
		double nearestDist = Double.MAX_VALUE;

		for (double t=0.01; t<=1.0; t+=0.01) {
			Point2D samplePoint = Geometry.getPointOnCubicCurve(curve, t);
			double distance = pt.distance(samplePoint);
			if (distance < nearestDist)	{
				nearestDist = distance;
				nearest = samplePoint;
			}
		}

		return nearest;
	}

	@Override
	public Point2D getPointOnCurve(double t) {
		return Geometry.getPointOnCubicCurve(getCurve(), t);
	}

	@Override
	public void notify(StateEvent e) {
		invalidate();
	}

	@Override
	public void notify(SelectionChangedEvent event) {
		boolean controlsVisible = false;
		for (Node n : event.getSelection())
			if ((n == cp1) || (n == cp2) || (n == parent)) {
				controlsVisible = true;
				break;
			}
			cp1.setHidden(!controlsVisible);
			cp2.setHidden(!controlsVisible);
	}

	@Override
	public void componentsTransformChanged() {
		List<ControlPoint> controlPoints = Arrays.asList(new ControlPoint[] {cp1, cp2});
		scaler.scale(connectionInfo.getFirstCenter(), connectionInfo.getSecondCenter(),
				controlPoints, connectionInfo.getScaleMode());

		scaler = new ControlPointScaler(connectionInfo.getFirstCenter(), connectionInfo.getSecondCenter());
		invalidate();
	}

	@Override
	public void componentsTransformChanging() {
		scaler = new ControlPointScaler(connectionInfo.getFirstCenter(), connectionInfo.getSecondCenter());
	}

	@Override
	public void controlPointsChanged() {
		invalidate();
	}

	@Override
	public void invalidate() {
		boundingBox = null;
		curveInfo = null;
		curve = null;
		visibleCurve = null;
	}

	@Override
	public Point2D getDerivativeAt(double t) {
		return Geometry.getDerivativeOfCubicCurve(getCurve(), t);
	}

	@Override
	public Point2D getSecondDerivativeAt(double t) {
		return Geometry.getSecondDerivativeOfCubicCurve(getCurve(), t);
	}

	@Override
	public Point2D getCenter() {
		return getPointOnCurve(0.5);
	}
}