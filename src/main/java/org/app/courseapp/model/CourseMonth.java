package org.app.courseapp.model;

import lombok.Getter;

@Getter
public enum CourseMonth {
    JANUARY(31, "Январь"),
    FEBRUARY(28, "Февраль"),
    MARCH(31, "Март"),
    APRIL(30, "Апрель"),
    MAY(31, "Май"),
    JUNE(30, "Июнь"),
    JULY(31, "Июль"),
    AUGUST(31, "Август"),
    SEPTEMBER(30, "Сентябрь"),
    OCTOBER(31, "Октябрь"),
    NOVEMBER(30, "Ноябрь"),
    DECEMBER(31, "Декабрь");

    private final int days;
    private final String displayName;

    CourseMonth(int days, String displayName) {
        this.days = days;
        this.displayName = displayName;
    }
}