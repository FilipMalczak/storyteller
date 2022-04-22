package com.github.filipmalczak.storyteller.api.visualize;

import com.github.filipmalczak.storyteller.api.tree.task.TaskType;

import java.io.File;

public interface HtmlReportGenerator<Id, Definition, Type extends TaskType> {

    void generateReport(File resultDirectory, StartingPoint<Id> startingPoint, ReportOptions<Id, Definition, Type> options);

    default void generateReport(File resultDirectory, StartingPoint<Id> startingPoint){
        generateReport(resultDirectory, startingPoint, ReportOptions.<Id, Definition, Type>builder().build());
    }
}
