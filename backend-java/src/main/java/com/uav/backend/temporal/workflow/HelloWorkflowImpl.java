package com.uav.backend.temporal.workflow;

public class HelloWorkflowImpl implements HelloWorkflow {
  @Override
  public String run(String name) {
    return "Temporal 已处理：" + name;
  }
}
