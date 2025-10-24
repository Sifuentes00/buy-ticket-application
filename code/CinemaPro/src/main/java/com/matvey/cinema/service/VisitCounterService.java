package com.matvey.cinema.service;

public interface VisitCounterService {

    void writeVisit(String url);

    int getVisitCount(String url);
}
