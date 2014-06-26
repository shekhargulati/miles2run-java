'use strict';

angular.module('milestogo')
    .controller('DashboardCtrl', function ($scope, ProgressService, ConfigService, activeProfile, $http, $timeout, $filter, activeGoal) {
        $scope.currentUser = activeProfile;
        $scope.error = null;
        $scope.data = {};

        var goalUnit = activeGoal.goalUnit.$name.toLowerCase();
        var paceUnit = 'mins/' + goalUnit;

        ProgressService.progress(activeGoal.id).success(function (data, status, headers, config) {
            $scope.error = null;
            $scope.data = data;
            generateProgressChart(data);
        }).error(function (data, status, headers, config) {
            toastr.error("Unable to fetch your progress. Please try later.");
            $scope.error = {message: "Unable to fetch your progress. Please try later."};
            console.log(data);
            console.log(status);
        });


        var generateProgressChart = function (data) {
            var chart = c3.generate({
                bindto: "#progressChart",
                size: {
                    height: 300
                },
                data: {
                    columns: [
                        ['completed', data.totalDistanceCovered],
                        ['remaining', (data.goal - data.totalDistanceCovered)]
                    ],
                    type: 'donut',
                    colors: {
                        'completed': 'green',
                        'remaining': 'red'
                    }
                },
                donut: {
                    label: {
                        format: function (value, ratio) {
                            return value + " " + data.goalUnit.toLowerCase();
                        },
                        units: data.goalUnit
                    },
                    title: "Goal " + data.goal + " " + data.goalUnit.toLowerCase()

                },
                tooltip: {
                    format: {
                        value: function (value) {
                            return value + " " + data.goalUnit.toLowerCase();
                        }
                    },
                    grouped: false
                }
            });
        };


        var distancePaceChartPerDay = function () {
            $scope.showNoDistancePaceChartMessage = true;
            $scope.distancePaceChartPerDayActive = true;
            $scope.distancePaceChartPerMonthActive = false;
            $http.get(ConfigService.getBaseUrl() + "goals/" + activeGoal.id + "/dashboard/charts/distanceandpace").success(function (data, status, headers, config) {
                if (data && data.length) {
                    $scope.showNoDistancePaceChartMessage = false;
                    buildDailyChart(data);
                } else {
                    $scope.showNoDistancePaceChartMessage = true;
                }

            }).error(function (data, status, headers, config) {
                $scope.showNoDistancePaceChartMessage = false;
                console.log(data);
            });
        }

        var distancePaceChartPerMonth = function () {
            $scope.showNoDistancePaceChartMessage = true;
            $scope.distancePaceChartPerDayActive = false;
            $scope.distancePaceChartPerMonthActive = true;
            $http.get(ConfigService.getBaseUrl() + "goals/" + activeGoal.id + "/dashboard/charts/distanceandpace?interval=month").success(function (data, status, headers, config) {
                if (data && data.length) {
                    $scope.showNoDistancePaceChartMessage = false;
                    buildMonthlyChart(data);
                } else {
                    $scope.showNoDistancePaceChartMessage = true;
                }

            }).error(function (data, status, headers, config) {
                $scope.showNoDistancePaceChartMessage = false;
                console.log(data);
            });
        }


        var buildDailyChart = function (dataElements) {

            var headerRow = ['x1', 'distance', 'pace' ];
            var rows = [headerRow].concat(dataElements);

            var chart = c3.generate({
                bindto: "#distance-pace-chart",
                data: {
                    colors: {
                        'distance': "#d95f02",
                        'pace': "#1b9e77"
                    },
                    x: 'x1',
                    x_format: null,
                    rows: rows
                },
                axis: {
                    x: {
                        type: 'timeseries',
                        tick: {
                            format: function (x) {
                                var dateFormat = d3.time.format("%b %d")
                                return dateFormat(x);
                            }
                        }
                    },
                    y: {
                        label: { // ADD
                            text: 'Distance (' + goalUnit + ')',
                            position: 'outer-middle'
                        }

                    }

                },
                tooltip: {
                    format: {
                        value: function (data, ratio, id) {
                            if (id === "distance") {
                                return data + " " + goalUnit;
                            } else {
                                var format = d3.format(".2f");
                                return format(data) + " " + paceUnit;
                            }
                        }
                    }
                }
            });
        };

        distancePaceChartPerDay();

        var buildMonthlyChart = function (dataElements) {

            var headerRow = ['x1', 'distance', 'pace' ];
            var rows = [headerRow].concat(dataElements);

            var chart = c3.generate({
                bindto: "#distance-pace-chart",
                bar: {
                    width: {
                        ratio: 0.25
                    }
                },
                data: {
                    colors: {
                        'distance': "#d95f02",
                        'pace': "#1b9e77"
                    },
                    x: 'x1',
                    x_format: null,
                    rows: rows,
                    types: {
                        "distance": "bar"
                    }
                },
                axis: {
                    x: {
                        type: 'category'
                    },
                    y: {
                        label: { // ADD
                            text: 'Distance (' + goalUnit + ')',
                            position: 'outer-middle'
                        }

                    }

                }
            });
        }

        $scope.distancePaceChart = function (interval) {
            if (interval === "day") {
                distancePaceChartPerDay();
            } else if (interval === "month") {
                distancePaceChartPerMonth();
            } else {
                distancePaceChartPerDay();
            }
        }


        function nMonthsBack(n) {
            var d = new Date();
            d.setMonth(d.getMonth() - n);
            return d;
        }

        $scope.calendarConfig = {
            start: nMonthsBack(2),
            minDate: nMonthsBack(5),
            maxDate: new Date(),
            range: 3,
            data: ConfigService.getBaseUrl() + "goals/" + activeGoal.id + "/activities/calendar",
            itemName: [activeGoal.goalUnit.$name.toLowerCase(), activeGoal.goalUnit.$name.toLowerCase()]
        };

        var dateFormat = function (x) {
            var date = new Date(x);
            var yyyy = date.getFullYear().toString();
            var mm = (date.getMonth() + 1).toString();
            var dd = date.getDate().toString();
            return yyyy + "-" + (mm[1] ? mm : "0" + mm[0]) + "-" + (dd[1] ? dd : "0" + dd[0]);
        }

        function paceFormatter(v, axis) {
            return v.toFixed(axis.tickDecimals) + 'mins/' + activeGoal.goalUnit.$name.toLowerCase();
        }

        function distanceFormatter(v, axis) {
            return v.toFixed(axis.tickDecimals) + activeGoal.goalUnit.$name.toLowerCase();
        }

        function dateFormatter(val, axis) {
            var d = new Date(val);
            return d.getUTCDate() + "/" + (d.getUTCMonth() + 1);
        }
    });