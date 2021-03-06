package org.insightech.er.editor.controller.editpart.element.connection;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ConnectionEndpointLocator;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.RelativeBendpoint;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.ui.PlatformUI;
import org.insightech.er.editor.controller.command.diagram_contents.element.connection.relation.ChangeRelationPropertyCommand;
import org.insightech.er.editor.controller.editpart.element.node.ERTableEditPart;
import org.insightech.er.editor.controller.editpolicy.element.connection.RelationBendpointEditPolicy;
import org.insightech.er.editor.controller.editpolicy.element.connection.RelationEditPolicy;
import org.insightech.er.editor.model.ERDiagram;
import org.insightech.er.editor.model.diagram_contents.element.connection.Bendpoint;
import org.insightech.er.editor.model.diagram_contents.element.connection.Relation;
import org.insightech.er.editor.view.dialog.element.relation.RelationDialog;
import org.insightech.er.editor.view.figure.connection.ERDiagramConnection;
import org.insightech.er.editor.view.figure.connection.decoration.DecorationFactory;
import org.insightech.er.editor.view.figure.connection.decoration.DecorationFactory.Decoration;
import org.insightech.er.util.Format;

public class RelationEditPart extends AbstractERDiagramConnectionEditPart {

    private Label targetLabel;

    /**
     * {@inheritDoc}
     */
    @Override
    protected IFigure createFigure() {
        final ERDiagramConnection connection = createERDiagramConnection();

        final ConnectionEndpointLocator targetLocator = new ConnectionEndpointLocator(connection, true);
        targetLabel = new Label("");
        connection.add(targetLabel, targetLocator);

        return connection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createEditPolicies() {
        super.createEditPolicies();

        installEditPolicy(EditPolicy.CONNECTION_ROLE, new RelationEditPolicy());
        installEditPolicy(EditPolicy.CONNECTION_BENDPOINTS_ROLE, new RelationBendpointEditPolicy());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<org.eclipse.draw2d.Bendpoint> getRealBendpoint(final Bendpoint bendPoint) {

        if (!bendPoint.isRelative()) {
            return super.getRealBendpoint(bendPoint);
        }

        final List<org.eclipse.draw2d.Bendpoint> constraint = new ArrayList<org.eclipse.draw2d.Bendpoint>();

        final Relation relation = (Relation) getModel();

        final ERTableEditPart tableEditPart = (ERTableEditPart) getSource();

        if (tableEditPart != null) {
            Rectangle bounds = tableEditPart.getFigure().getBounds();
            int width = bounds.width;
            int height = bounds.height;

            if (width == 0) {
                // tableEditPart.getFigure().getUpdateManager()
                // .performUpdate();

                bounds = tableEditPart.getFigure().getBounds();
                width = bounds.width;
                height = bounds.height;
            }

            RelativeBendpoint point = new RelativeBendpoint();

            final int xp = relation.getTargetXp();
            int x;

            if (xp == -1) {
                x = bounds.x + bounds.width;
            } else {
                x = bounds.x + (bounds.width * xp / 100);
            }

            point.setRelativeDimensions(new Dimension(width * bendPoint.getX() / 100 - bounds.x - bounds.width + x, 0), new Dimension(width * bendPoint.getX() / 100 - bounds.x - bounds.width + x, 0));
            point.setWeight(0);
            point.setConnection(getConnectionFigure());

            constraint.add(point);

            point = new RelativeBendpoint();
            point.setRelativeDimensions(new Dimension(width * bendPoint.getX() / 100 - bounds.x - bounds.width + x, height * bendPoint.getY() / 100), new Dimension(width * bendPoint.getX() / 100 - bounds.x - bounds.width + x, height * bendPoint.getY() / 100));
            point.setWeight(0);
            point.setConnection(getConnectionFigure());

            constraint.add(point);

            point = new RelativeBendpoint();
            point.setRelativeDimensions(new Dimension(x - bounds.x - bounds.width, height * bendPoint.getY() / 100), new Dimension(x - bounds.x - bounds.width, height * bendPoint.getY() / 100));
            point.setWeight(0);
            point.setConnection(getConnectionFigure());

            constraint.add(point);
        }

        return constraint;
    }

    /**
     * {@inheritDoc}
     */
    // @Override
    // public void refreshVisuals() {
    // super.refreshVisuals();
    //
    // }

    @Override
    protected void decorateRelation() {
        final ERDiagram diagram = getDiagram();

        if (diagram != null) {
            final Relation relation = (Relation) getModel();

            final PolylineConnection connection = (PolylineConnection) getConnectionFigure();

            final String notation = diagram.getDiagramContents().getSettings().getNotation();

            final Decoration decoration = DecorationFactory.getDecoration(notation, relation.getParentCardinality(), relation.getChildCardinality());

            connection.setSourceDecoration(decoration.getSourceDecoration());
            connection.setTargetDecoration(decoration.getTargetDecoration());

            targetLabel.setText(Format.null2blank(decoration.getTargetLabel()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void performRequest(final Request request) {
        final Relation relation = (Relation) getModel();

        if (request.getType().equals(RequestConstants.REQ_OPEN)) {
            final Relation copy = relation.copy();

            final RelationDialog dialog = new RelationDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), copy);

            if (dialog.open() == IDialogConstants.OK_ID) {
                final ChangeRelationPropertyCommand command = new ChangeRelationPropertyCommand(relation, copy);
                getViewer().getEditDomain().getCommandStack().execute(command);
            }
        }

        super.performRequest(request);
    }

}
