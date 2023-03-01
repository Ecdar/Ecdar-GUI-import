package ecdar.backend;

import EcdarProtoBuf.QueryProtos;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ecdar.Ecdar;
import ecdar.abstractions.Component;
import ecdar.abstractions.Query;
import ecdar.abstractions.QueryState;
import ecdar.utility.UndoRedoStack;
import ecdar.utility.helpers.StringValidator;
import io.grpc.stub.StreamObserver;
import javafx.application.Platform;
import javafx.collections.ObservableList;

import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

public class QueryHandler {
    private final BackendDriver backendDriver;

    public QueryHandler(BackendDriver backendDriver) {
        this.backendDriver = backendDriver;
    }

    /**
     * Executes the specified query
     * @param query             query to be executed
     */
    public void executeQuery(Query query) throws NoSuchElementException {
        if (query.getQueryState().equals(QueryState.RUNNING) || !StringValidator.validateQuery(query.getQuery())) return;

        if (query.getQuery().isEmpty()) {
            query.setQueryState(QueryState.SYNTAX_ERROR);
            query.addError("Query is empty");
            return;
        }

        query.setQueryState(QueryState.RUNNING);
        query.errors().set("");

        GrpcRequest request = new GrpcRequest(backendConnection -> {
            StreamObserver<QueryProtos.QueryResponse> responseObserver = new StreamObserver<>() {
                @Override
                public void onNext(QueryProtos.QueryResponse value) {
                    handleQueryResponse(value, query);
                }

                @Override
                public void onError(Throwable t) {
                    handleQueryBackendError(t, query);
                    backendDriver.addBackendConnection(backendConnection);
                }

                @Override
                public void onCompleted() {
                    // Release backend connection
                    backendDriver.addBackendConnection(backendConnection);
                }
            };

            var queryBuilder = QueryProtos.Query.newBuilder()
                    .setId(0)
                    .setQuery(query.getType().getQueryName() + ": " + query.getQuery());

            backendConnection.getStub().withDeadlineAfter(backendDriver.getResponseDeadline(), TimeUnit.MILLISECONDS)
                    .sendQuery(queryBuilder.build(), responseObserver);
        }, query.getBackend());

        backendDriver.addRequestToExecutionQueue(request);
    }

    private void handleQueryResponse(QueryProtos.QueryResponse value, Query query) {
        // If the query has been cancelled, ignore the result
        if (query.getQueryState() == QueryState.UNKNOWN) return;

        if (value.hasRefinement() && value.getRefinement().getSuccess()) {
            query.setQueryState(QueryState.SUCCESSFUL);
            query.getSuccessConsumer().accept(true);
        } else if (value.hasConsistency() && value.getConsistency().getSuccess()) {
            query.setQueryState(QueryState.SUCCESSFUL);
            query.getSuccessConsumer().accept(true);
        } else if (value.hasDeterminism() && value.getDeterminism().getSuccess()) {
            query.setQueryState(QueryState.SUCCESSFUL);
            query.getSuccessConsumer().accept(true);
        } else if (value.hasComponent()) {
            query.setQueryState(QueryState.SUCCESSFUL);
            query.getSuccessConsumer().accept(true);
            JsonObject returnedComponent = (JsonObject) JsonParser.parseString(value.getComponent().getComponent().getJson());
            addGeneratedComponent(Ecdar.getProject().getTempComponents(), new Component(returnedComponent)); // ToDo NIELS: Remove dependency
        } else {
            query.setQueryState(QueryState.ERROR);
            query.getSuccessConsumer().accept(false);
        }
    }

    private void handleQueryBackendError(Throwable t, Query query) {
        // If the query has been cancelled, ignore the error
        if (query.getQueryState() == QueryState.UNKNOWN) return;

        // Each error starts with a capitalized description of the error equal to the gRPC error type encountered
        String errorType = t.getMessage().split(":\\s+", 2)[0];

        if ("DEADLINE_EXCEEDED".equals(errorType)) {
            query.setQueryState(QueryState.ERROR);
            query.getFailureConsumer().accept(new BackendException.QueryErrorException("The backend did not answer the request in time"));
        } else {
            try {
                query.setQueryState(QueryState.ERROR);
                query.getFailureConsumer().accept(new BackendException.QueryErrorException("The execution of this query failed with message:" + System.lineSeparator() + t.getLocalizedMessage()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void addGeneratedComponent(ObservableList<Component> tempComponents, Component newComponent) {
        Platform.runLater(() -> {
            newComponent.setTemporary(true);
            Component matchedComponent = null;

            for (Component currentGeneratedComponent : tempComponents) {
                int comparisonOfNames = currentGeneratedComponent.getName().compareTo(newComponent.getName());

                if (comparisonOfNames == 0) {
                    matchedComponent = currentGeneratedComponent;
                    break;
                } else if (comparisonOfNames < 0) {
                    break;
                }
            }

            if (matchedComponent == null) {
                UndoRedoStack.pushAndPerform(() -> { // Perform
                    tempComponents.add(newComponent);
                }, () -> { // Undo
                    tempComponents.remove(newComponent);
                }, "Created new component: " + newComponent.getName(), "add-circle");
            } else {
                // Remove current component with name and add the newly generated one
                Component finalMatchedComponent = matchedComponent; // Potentially null
                UndoRedoStack.pushAndPerform(() -> { // Perform
                    tempComponents.remove(finalMatchedComponent);
                    tempComponents.add(newComponent);
                }, () -> { // Undo
                    tempComponents.remove(newComponent);
                    tempComponents.add(finalMatchedComponent);
                }, "Created new component: " + newComponent.getName(), "add-circle");
            }
        });
    }
}
