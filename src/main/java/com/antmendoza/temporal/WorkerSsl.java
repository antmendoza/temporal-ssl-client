package com.antmendoza.temporal;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.SimpleSslContextBuilder;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;

import java.io.FileInputStream;
import java.io.InputStream;

public class WorkerSsl {

  static final String TASK_QUEUE = "MyTaskQueue";

  public static void main(String[] args) throws Exception {

    // Create SSL enabled client by passing SslContext, created by SimpleSslContextBuilder.
    SslContextBuilderProvider sslContextBuilderProvider = new SslContextBuilderProvider();
    WorkflowServiceStubs service =
        WorkflowServiceStubs.newServiceStubs(
            WorkflowServiceStubsOptions.newBuilder()
                .setSslContext(sslContextBuilderProvider.getSslContext())
                .setTarget(sslContextBuilderProvider.getTargetEndpoint())
                .build());

    // Now setup and start workflow worker, which uses SSL enabled gRPC service to communicate with
    // backend.
    // client that can be used to start and signal workflows.
    WorkflowClient client =
        WorkflowClient.newInstance(
            service, WorkflowClientOptions.newBuilder().setNamespace(sslContextBuilderProvider.getNamespace()).build());
    // worker factory that can be used to create workers for specific task queues
    WorkerFactory factory = WorkerFactory.newInstance(client);
    // Worker that listens on a task queue and hosts both workflow and activity implementations.
    Worker worker = factory.newWorker(TASK_QUEUE);

    worker.registerWorkflowImplementationTypes(HelloActivity.GreetingWorkflowImpl.class);
    worker.registerActivitiesImplementations(new HelloActivity.GreetingActivitiesImpl());
    factory.start();
  }
}
