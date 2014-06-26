'use strict';

angular.module('milestogo')
    .controller('DashboardCtrl', function ($scope, ProgressService, ConfigService, activeProfile, $http, $timeout, $filter, activeGoal) {
        $scope.currentUser = activeProfile;
        $scope.error = null;
        $scope.data = {};

        ProgressService.progress(activeGoal.id).success(function (data, status, headers, config) {
            $scope.error = null;
            $scope.status = status;
            $scope.data = data;
            $scope.style = "width:" + data.percentage + "%";
        }).error(function (data, status, headers, config) {
            toastr.error("Unable to fetch your progress. Please try later.");
            $scope.error = {message: "Unable to fetch your progress. Please try later."};
            console.log(data);
            console.log(status);
        });

        function calendarStartDate() {
            var d = new Date();
            d.setMonth(d.getMonth() - 2);
            return d;
        }

        $scope.calendarConfig = {
            minDate: calendarStartDate(),
            maxDate: new Date(),
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


        var progressChart = c3.generate({
            bindto: '#progressChart',
            size: {
                height: 300
            },
            data: {
                columns: [
                    ['completed', 10],
                    ['remaining', 90]
                ],
                type: 'donut',
                colors: {
                    remaining: '#ff0000',
                    completed: '#00ff00'
                }
            },
            donut: {
                title: "100 km",
                onclick: function (d, i) {
                    console.log(d, i);
                },
                onmouseover: function (d, i) {
                    console.log(d, i);
                },
                onmouseout: function (d, i) {
                    console.log(d, i);
                }
            }
        });


        var buildDailyChart = function () {
            var chart = c3.generate({
                bindto: "#distance-pace-chart",
                data: {
                    colors: {
                        'pace (mins/km)': "#1b9e77",
                        'distance (km)': "#d95f02"
                    },
                    x: 'x1',
                    x_format: null,
                    columns: [
                        ['x1', 1403742803773, 1403116200000, 1403375400000, 1403548200000],
                        ['pace (mins/km)', 21, 13, 12, 10],
                        ['distance (km)', 4, 3, 5, 2]
                    ]
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
                            text: 'Distance (km)',
                            position: 'outer-middle'
                        }

                    }

                }
            });
        }

        var buildMonthlyChart = function () {
            var chart = c3.generate({
                bindto: "#distance-pace-chart",
                bar: {
                    width: {
                        ratio: 0.25
                    }
                },
                data: {
                    colors: {
                        'pace (mins/km)': "#1b9e77",
                        'distance (km)': "#d95f02"
                    },
                    x: 'x1',
                    x_format: null,
                    columns: [
                        ['x1', "Feb", "March", "April", "May", "June"],
                        ['pace (mins/km)', 10, 14, 16, 18, 20],
                        ['distance (km)', 20, 40, 60, 80, 75]
                    ],
                    types: {
                        "distance (km)": "bar"
                    }
                },
                axis: {
                    x: {
                        type: 'category'
                    },
                    y: {
                        label: { // ADD
                            text: 'Distance (km)',
                            position: 'outer-middle'
                        }

                    }

                }
            });
        }

        $scope.distancePaceChart = function (interval) {
            if (interval === "day") {
                buildDailyChart();
            } else if (interval === "month") {
                buildMonthlyChart();
            } else {
                buildDailyChart();
            }
        }


        buildDailyChart();

    });