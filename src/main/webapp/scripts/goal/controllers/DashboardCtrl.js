'use strict';

angular.module('milestogo')
    .controller('DashboardCtrl', function ($scope, ProgressService, ConfigService, activeProfile, $http, $timeout, $filter, activeGoal) {
        $scope.currentUser = activeProfile;
        $scope.error = null;
        $scope.data = {};

        if (activeGoal.goalType.$name === 'DISTANCE_GOAL') {
            $scope.firstTemplate = '../views/goal/partials/goal_distance_first.html';
        } else {
            $scope.firstTemplate = '../views/goal/partials/goal_duration_first.html';
        }

        var goalUnit = activeGoal.goalUnit.$name.toLowerCase();
        var paceUnit = 'mins/' + goalUnit;

        ProgressService.progress(activeGoal.id).success(function (data, status, headers, config) {
            $scope.error = null;
            $scope.goalType = data.goalType;
            $scope.data = data;
            if ($scope.goalType === 'DISTANCE_GOAL') {
                generateProgressChart(data);
            }

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
                        ['completed', $filter('number')(data.totalDistanceCovered > data.goal ? data.goal : data.totalDistanceCovered, 2)],
                        ['remaining', $filter('number')(data.totalDistanceCovered > data.goal ? 0 : data.goal - data.totalDistanceCovered, 2)]
                    ],
                    type: 'donut',
                    colors: {
                        'completed': 'green',
                        'remaining': 'red'
                    },
                    labels: false
                },
                legend: {
                    show: false
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
            $("#distance-pace-chart").empty();
            $scope.distancePaceChartPerDayActive = true;
            $scope.distancePaceChartPerMonthActive = false;
            $http.get(ConfigService.getBaseUrl() + "goal_aggregate/" + activeGoal.id + "/distance_and_pace").success(function (data, status, headers, config) {
                $scope.showNoDistancePaceChartMessage = true;
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
            $("#distance-pace-chart").empty();
            $scope.distancePaceChartPerDayActive = false;
            $scope.distancePaceChartPerMonthActive = true;
            $http.get(ConfigService.getBaseUrl() + "goal_aggregate/" + activeGoal.id + "/distance_and_pace?interval=month").success(function (data, status, headers, config) {
                $scope.showNoDistancePaceChartMessage = true;
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
                            var format = d3.format(".2f");
                            if (id === "distance") {
                                return format(data) + " " + goalUnit;
                            } else {
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
                        ratio: 0.1
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

                },
                tooltip: {
                    format: {
                        value: function (data, ratio, id) {
                            var format = d3.format(".2f");
                            if (id === "distance") {
                                return format(data) + " " + goalUnit;
                            } else {
                                return format(data) + " " + paceUnit;
                            }
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
            start: new Date(),
            minDate: nMonthsBack(12),
            maxDate: new Date(),
            range: 1,
            data: ConfigService.getBaseUrl() + "goal_aggregate/" + activeGoal.id + "/activity_calendar",
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

        var distanceActivityCountChartObj = null;

        var renderAcivityCountChart = function (dataElements) {

            var headerRow = ['month', 'distance', 'activities' ];
            var rows = [headerRow].concat(dataElements);

            $scope.distanceActityCountLineChartActive = false;
            $scope.distanceActityCountBarChartActive = true;

            var data = rows;
            var chart = c3.generate({
                bindto: "#distance-activitycount-chart",
                data: {
                    rows: data,
                    type: 'bar',
                    x: 'month',
                    colors: {
                        "distance": "#d95f02",
                        "activityCount": "#1b9e77"
                    }
                },
                bar: {
                    width: {
                        ratio: 0.4
                    }
                },
                axis: {
                    x: {
                        type: 'category'
                    },
                    y: {
                        label: {
                            text: 'Distance (' + goalUnit + ')',
                            position: 'outer-middle'
                        }

                    }

                },
                tooltip: {
                    format: {
                        value: function (data, ratio, id) {
                            var format = d3.format(".2f");
                            if (id === "distance") {
                                return format(data) + " " + goalUnit;
                            }
                            return data;
                        }
                    }
                }
            });

            distanceActivityCountChartObj = chart;
        }

        var distanceActivityCountChart = function () {
            $http.get(ConfigService.getBaseUrl() + "goal_aggregate/" + activeGoal.id + "/distance_and_activity").success(function (data, status, headers, config) {
                if (data && data.length) {
                    renderAcivityCountChart(data);
                } else {
                    $scope.showNoDistancePaceChartMessage = true;
                }

            }).error(function (data, status, headers, config) {
                $scope.showNoDistancePaceChartMessage = false;
                console.log(data);
            });
        }


        distanceActivityCountChart();

        $scope.distanceAcivityCountChart = distanceActivityCountChart;

        $scope.distanceAcivityCountChartAsLine = function () {
            $scope.distanceActityCountLineChartActive = true;
            $scope.distanceActityCountBarChartActive = false;
            distanceActivityCountChartObj.transform('line');
        }

    });