package ecdar.mutation;

import com.sun.javaws.exceptions.InvalidArgumentException;
import ecdar.abstractions.Component;
import ecdar.abstractions.Edge;
import ecdar.abstractions.EdgeStatus;
import ecdar.abstractions.Location;
import ecdar.mutation.models.ActionRule;
import ecdar.utility.ExpressionHelper;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Simulation of a component.
 * It simulates the current location and clock valuations.
 * It does not simulate local variables.
 */
public class ComponentSimulation {
    private final Component component;

    private Location currentLocation;
    private final Map<String, Double> valuations = new HashMap<>();
    private final List<String> clocks = new ArrayList<>();

    public ComponentSimulation(final Component component) {
        this.component = component;
        currentLocation = component.getInitialLocation();

        component.getClocks().forEach(clock -> {
            clocks.add(clock);
            valuations.put(clock, 0.0);
        });
    }


    /* Getters and setters */

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public Map<String, Double> getValuations() {
        return valuations;
    }

    private void setCurrentLocation(final Location currentLocation) {
        this.currentLocation = currentLocation;
    }


    /* Other methods */

    /**
     * Delays.
     * The delay is run successfully if the invariant of the current location still holds.
     * @param time the amount to delay in engine time units
     * @return true iff the delay was run successfully
     */
    public boolean delay(final double time) {
        clocks.forEach(c -> valuations.put(c, valuations.get(c) + time));

        final String invariant = getCurrentLocation().getInvariant();

        return invariant.isEmpty() || ExpressionHelper.evaluateBooleanExpression(invariant, valuations);
    }

    /**
     * Runs an action rule.
     * This method does not check for guards.
     * It simply updates the current location and runs the update property.
     * @param rule the rule to run
     */
    public void runActionRule(final ActionRule rule) {
        final String backendLocId = rule.getEndLocationName();
        final Location newLocation;

        if (backendLocId.equals("Universal")) {
            newLocation = component.getUniversalLocation();

            if (newLocation == null) throw new IllegalArgumentException("End location was the Universal location, but this component has no universal locations.");
        } else {
            newLocation = component.findLocation(backendLocId);

            if (newLocation == null) throw new IllegalArgumentException("End location " + backendLocId + " was not found");
        }

        setCurrentLocation(newLocation);

        ExpressionHelper.parseUpdateProperty(rule.getUpdateProperty()).forEach(valuations::put);
    }

    /**
     * Runs an update property by updating valuations.
     * @param property the update property
     */
    private void runUpdateProperty(final String property) {
        ExpressionHelper.parseUpdateProperty(property).forEach(valuations::put);
    }

    /**
     * Returns if the current state is deterministic with respect to a specified output.
     * The state is deterministic iff at most one transition with the specified output is available.
     * @param output synchronization property without !
     * @return true iff the state is deterministic
     */
    public boolean isDeterministic(final String output) {
        return getAvailableOutputEdgeStream(output).count() > 1;
    }

    /**
     * Gets a stream containing the available output edges matching a specified output.
     * @param output the specified synchronization output without !
     * @return the stream
     */
    private Stream<Edge> getAvailableOutputEdgeStream(final String output) {
        return component.getOutgoingEdges(currentLocation).stream()
                .filter(e -> e.getStatus() == EdgeStatus.OUTPUT)
                .filter(e -> e.getSync().equals(output))
                .filter(e -> e.getGuard().trim().isEmpty() ||
                        ExpressionHelper.evaluateBooleanExpression(e.getGuard(), getValuations()));
    }

    /**
     * If a valid output edge is available, runs a transition of that edge.
     * The edge must be outgoing from the current location,
     * must be an output edge with the given synchronization property,
     * and its guard must be satisfied.
     * @param output synchronization property without !
     * @return true iff the action succeeded
     * @throws MutationTestingException if multiple transitions with the specified output are available
     */
    public boolean triggerOutput(final String output) throws MutationTestingException {
        final List<Edge> edges = getAvailableOutputEdgeStream(output).collect(Collectors.toList());

        if (edges.size() > 1) throw new MutationTestingException("Simulation of output " + output + " yields a non-deterministic choice");

        if (edges.size() < 1) return false;

        currentLocation = edges.get(0).getTargetLocation();
        runUpdateProperty(edges.get(0).getUpdate());
        return true;
    }
}
